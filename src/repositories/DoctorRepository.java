package repositories;

import database.DatabaseConfiguration;
import models.address.Address;
import models.person.Doctor;
import models.treatment.TreatmentCategory;
import services.Audit;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static models.person.Doctor.deserializeSpecializations;

public class DoctorRepository {

    private static DoctorRepository instance;
    private static Audit audit = Audit.getInstance();
    private DoctorRepository(){}

    static {
        try {
            instance = new DoctorRepository();
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred in creating DBFunctions: DoctorRepository singleton instance");
        }
    }

    public static DoctorRepository getInstance() {
        if (instance == null) {
            instance = new DoctorRepository();
        }
        return instance;
    }
    public void createTable() {
        String query1 = "CREATE TABLE IF NOT EXISTS ADDRESS (" +
                "id_address INT AUTO_INCREMENT PRIMARY KEY, " +
                "country VARCHAR(100), " +
                "city VARCHAR(100), " +
                "street VARCHAR(100), " +
                "number_address INT);";

        String query2 = "CREATE TABLE IF NOT EXISTS DOCTOR (" +
                "id_doctor INT AUTO_INCREMENT PRIMARY KEY, " +
                "firstName VARCHAR(100), " +
                "lastName VARCHAR(100), " +
                "email VARCHAR(100), " +
                "id_address INT, " +
                "phoneNumber VARCHAR(20), " +
                "specializations TEXT, " + // Câmp pentru specializările doctorului, de tip text
                "FOREIGN KEY (id_address) REFERENCES ADDRESS(id_address) ON DELETE CASCADE);";

        try {
            Connection connection = DatabaseConfiguration.getDatabaseConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(query1);
            statement.executeUpdate(query2);
            audit.logAction("Doctor and Address tables have been crated");
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            System.out.println(e);
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public int getDoctorId(Doctor doctor) throws SQLException {
        String query = "SELECT id_doctor FROM DOCTOR WHERE firstName = ? AND lastName = ? AND email = ?";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, doctor.getFirstName());
        statement.setString(2, doctor.getLastName());
        statement.setString(3, doctor.getEmail());
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            try {
                audit.logAction("Got Doctor Id");
            } catch (IOException e){
                System.out.println("Error with audit: " + e);
            }
            return resultSet.getInt("id_doctor");
        } else {
            throw new SQLException("Doctor not found in the database.");
        }
    }
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();

        String selectSql = "SELECT * FROM DOCTOR INNER JOIN ADDRESS ON DOCTOR.id_address=ADDRESS.id_address;";

        try (Connection connection = DatabaseConfiguration.getDatabaseConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(selectSql)) {

            while (resultSet.next()) {
                Doctor doctor = new Doctor(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        new Address(
                                resultSet.getString("country"),
                                resultSet.getString("city"),
                                resultSet.getString("street"),
                                resultSet.getInt("number_address")
                        ),
                        resultSet.getString("phoneNumber")
                );

                // Deserializare specializări
                Set<TreatmentCategory> specializations = deserializeSpecializations(resultSet.getString("specializations"));
                doctor.setSpecializations(specializations);

                doctors.add(doctor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return doctors;
    }
    public void addDoctor(Doctor doctor) {
        Address address = doctor.getAddress();
        String insertAddressSql = "INSERT INTO ADDRESS(country, city, street, number_address) VALUES(?, ?, ?, ?);";

        try {
            Connection connection = DatabaseConfiguration.getDatabaseConnection();
            PreparedStatement addressStatement = connection.prepareStatement(insertAddressSql, Statement.RETURN_GENERATED_KEYS);
            addressStatement.setString(1,address.getCountry());
            addressStatement.setString(2,address.getCity());
            addressStatement.setString(3,address.getStreet());
            addressStatement.setInt(4,address.getNumber());

            int rows = addressStatement.executeUpdate();
            if(rows==0){
                throw new SQLException("\nInserting address failed, no rows affected!");
            }
            ResultSet generatedKeys = addressStatement.getGeneratedKeys();
            int addressId;
            if(generatedKeys.next()){
                addressId = generatedKeys.getInt(1);
                String insertDoctorSql="INSERT INTO DOCTOR (firstName, lastName, email, id_address, phoneNumber, specializations) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement doctorStatement = connection.prepareStatement(insertDoctorSql, Statement.RETURN_GENERATED_KEYS);
                doctorStatement.setString(1, doctor.getFirstName());
                doctorStatement.setString(2, doctor.getLastName());
                doctorStatement.setString(3,  doctor.getEmail());
                doctorStatement.setInt(4,  addressId);
                doctorStatement.setString(5,  doctor.getPhoneNumber());
                doctorStatement.setString(6, doctor.serializeSpecializations()); // Serializare specializări

                int rowsInserted = doctorStatement.executeUpdate();
                generatedKeys = doctorStatement.getGeneratedKeys();

                DatabaseConfiguration.closeDatabaseConnection();
                if (rowsInserted > 0) {
                    System.out.println("\nDoctor inserted successfully.");
                    audit.logAction("Added new Address and Doctor");

                } else {
                    throw new SQLException("\nInserting doctor failed.");
                }
            }else {
                throw new SQLException("\nInserting address failed, no ID obtained.");
            }

        } catch (SQLException e) {
            System.out.println(e);
        }
        catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public void viewDoctors() {
        String selectSql = "SELECT * FROM DOCTOR INNER JOIN ADDRESS ON DOCTOR.id_address=ADDRESS.id_address;";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();

        try (Statement stmt = connection.createStatement()) {
            boolean empty = true;
            ResultSet resultSet = stmt.executeQuery(selectSql);
            while (resultSet.next()) {
                if (empty) {
                    System.out.println("\nList of all Doctors:");
                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    empty = false;
                }

                StringBuilder doctorInfo = new StringBuilder();
                doctorInfo.append("Doctor ID: ").append(resultSet.getInt("id_doctor")).append(" | ")
                        .append("Full Name: ").append(resultSet.getString("firstName")).append(" ")
                        .append(resultSet.getString("lastName")).append(" | ")
                        .append("Email: ").append(resultSet.getString("email")).append(" | ")
                        .append("Phone Number: ").append(resultSet.getString("phoneNumber")).append(" | ")
                        .append("Specializations: ").append(resultSet.getString("specializations")).append(" | ")
                        .append("Address: ").append(resultSet.getString("country")).append(", ")
                        .append(resultSet.getString("city")).append(", ")
                        .append(resultSet.getString("street")).append(" ")
                        .append(resultSet.getInt("number_address"));

                System.out.println(doctorInfo);
                System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

            }
            DatabaseConfiguration.closeDatabaseConnection();
            if (empty) {
                System.out.println("\nNo existing Doctors!");
            }
            audit.logAction("View All Doctors");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public void searchDoctorsBySpecialization(String specialization) {
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        String selectSql = "SELECT * FROM DOCTOR INNER JOIN ADDRESS ON DOCTOR.ID_ADDRESS=ADDRESS.ID_ADDRESS " +
                "WHERE specializations LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, "%" + specialization + "%");

            ResultSet resultSet = stmt.executeQuery();

            boolean empty = true;

            while (resultSet.next()) {
                if (empty) {
                    System.out.println("\nDoctors with Specialization '" + specialization + "':");
                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    empty = false;
                }

                StringBuilder doctorInfo = new StringBuilder();
                doctorInfo.append("Doctor ID: ").append(resultSet.getInt("id_doctor")).append(" | ")
                        .append("Full Name: ").append(resultSet.getString("firstName")).append(" ")
                        .append(resultSet.getString("lastName")).append(" | ")
                        .append("Email: ").append(resultSet.getString("email")).append(" | ")
                        .append("Phone Number: ").append(resultSet.getString("phoneNumber")).append(" | ")
                        .append("Specializations: ").append(resultSet.getString("specializations")).append(" | ")
                        .append("Address: ").append(resultSet.getString("country")).append(", ")
                        .append(resultSet.getString("city")).append(", ")
                        .append(resultSet.getString("street")).append(" ")
                        .append(resultSet.getInt("number_address"));

                System.out.println(doctorInfo);
                System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            }

            if (empty) {
                System.out.println("\nNo doctors found with Specialization '" + specialization.toUpperCase() + "'.");
            }
        audit.logAction("Searched doctors by specialization " + specialization.toUpperCase());
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public void searchDoctorsByName(String name){
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        String selectSql = "SELECT * FROM DOCTOR INNER JOIN ADDRESS ON DOCTOR.ID_ADDRESS=ADDRESS.ID_ADDRESS " +
                "WHERE firstName LIKE ? OR lastName LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, "%" + name + "%");
            stmt.setString(2, "%" + name + "%");

            ResultSet resultSet = stmt.executeQuery();

            boolean empty = true;
            while (resultSet.next()) {
                if (empty) {
                    System.out.println("Search results for doctors with names containing '" + name.toUpperCase() + "':");
                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    empty = false;
                }

                StringBuilder doctorInfo = new StringBuilder();
                doctorInfo.append("Doctor ID: ").append(resultSet.getInt("id_doctor")).append(" | ")
                        .append("Full Name: ").append(resultSet.getString("firstName")).append(" ")
                        .append(resultSet.getString("lastName")).append(" | ")
                        .append("Email: ").append(resultSet.getString("email")).append(" | ")
                        .append("Phone Number: ").append(resultSet.getString("phoneNumber")).append(" | ")
                        .append("Specializations: ").append(resultSet.getString("specializations")).append(" | ")
                        .append("Address: ").append(resultSet.getString("country")).append(", ")
                        .append(resultSet.getString("city")).append(", ")
                        .append(resultSet.getString("street")).append(" ")
                        .append(resultSet.getInt("number_address"));

                System.out.println(doctorInfo);
                System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            }

            if (empty) {
                System.out.println("\nNo doctors found with the name '" + name.toUpperCase() + "'.");
            }
            audit.logAction("Searched doctors by name " + name.toUpperCase());
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    private void updateAddress(int ID, Address a) {
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        try {

            String updateAddressSql = "UPDATE ADDRESS SET country = ?, city = ?, street = ?, number_address = ? WHERE id_address = ?";
            PreparedStatement addressStatement = connection.prepareStatement(updateAddressSql);
            addressStatement.setString(1, a.getCountry());
            addressStatement.setString(2, a.getCity());
            addressStatement.setString(3, a.getStreet());
            addressStatement.setInt(4,  a.getNumber());
            addressStatement.setInt(5, ID);

            addressStatement.executeUpdate();
        }
        catch (SQLException e){
            System.out.println(e);
        }
    }
    public void updateDoctor(int id, Doctor doctor) {
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        try {
            // Retrieve the doctor and address details
            String selectDoctorSql = "SELECT * FROM DOCTOR WHERE id_doctor = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectDoctorSql);
            selectStatement.setInt(1, id);
            ResultSet resultSet = selectStatement.executeQuery();

            boolean empty = true;
            if (resultSet.next()) {
                empty = false;
                int idAddress = resultSet.getInt("id_address");

                // Update the address
                updateAddress(idAddress, doctor.getAddress());

                // Update the doctor details
                String updateDoctorSql = "UPDATE DOCTOR SET firstName = ?, lastName = ?, email = ?, phoneNumber = ?, specializations = ? WHERE id_doctor = ?";
                PreparedStatement doctorStatement = connection.prepareStatement(updateDoctorSql);
                doctorStatement.setString(1, doctor.getFirstName());
                doctorStatement.setString(2, doctor.getLastName());
                doctorStatement.setString(3, doctor.getEmail());
                doctorStatement.setString(4, doctor.getPhoneNumber());
                doctorStatement.setString(5, doctor.serializeSpecializations()); // Serializarea specializărilor
                doctorStatement.setInt(6, id);

                doctorStatement.executeUpdate();
                System.out.println("\nThe doctor and address were updated.");
                audit.logAction("The doctor and address were updated" + id);
            }

            DatabaseConfiguration.closeDatabaseConnection();
            if (empty) {
                System.out.println("\nNo existing doctor with this ID!");
                audit.logAction("No doctor found for update with the given ID "+id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
         catch (IOException e){
             System.out.println("Error with audit: " + e);
         }
    }
    public Doctor getDoctorById(int id) {
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        Doctor doctor = null;
        String selectSql = "SELECT d.*, a.country, a.city, a.street, a.number_address FROM DOCTOR d " +
                "INNER JOIN ADDRESS a ON d.id_address = a.id_address WHERE d.id_doctor = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Address address = new Address(
                        resultSet.getString("country"),
                        resultSet.getString("city"),
                        resultSet.getString("street"),
                        resultSet.getInt("number_address")
                );

                doctor = new Doctor(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        address,
                        resultSet.getString("phoneNumber")
                );

                // Deserializarea specializărilor
                String specializationsString = resultSet.getString("specializations");
                Set<TreatmentCategory> specializations = deserializeSpecializations(specializationsString);
                doctor.setSpecializations(specializations);
            }
            audit.logAction("Got Doctor by id "+id);
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
         catch (IOException e){
             System.out.println("Error with audit: " + e);
         }
        return doctor;
    }
    public boolean doctorExists(int id) {
        String checkDoctorSql = "SELECT 1 FROM DOCTOR WHERE id_doctor = ?";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(checkDoctorSql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            boolean exists = resultSet.next();

            DatabaseConfiguration.closeDatabaseConnection();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void deleteDoctorById(int id) {
        String deleteDoctorSql = "DELETE FROM DOCTOR WHERE id_doctor = ?";
        String deleteAddressSql = "DELETE FROM ADDRESS WHERE id_address = (SELECT id_address FROM DOCTOR WHERE id_doctor = ?)";


        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        try {

            // Delete the address associated with the patient
            PreparedStatement deleteAddressStatement = connection.prepareStatement(deleteAddressSql);
            deleteAddressStatement.setInt(1, id);
            int addressRowsDeleted = deleteAddressStatement.executeUpdate();

            // Delete the patient
            PreparedStatement deleteDoctorStatement = connection.prepareStatement(deleteDoctorSql);
            deleteDoctorStatement.setInt(1, id);
            int doctorRowsDeleted = deleteDoctorStatement.executeUpdate();

            if (doctorRowsDeleted > 0 || addressRowsDeleted > 0) {
                System.out.println("\nDoctor deleted successfully.");
                audit.logAction("Deleted doctor with ID: " + id);
            } else {
                System.out.println("\nNo doctor found with ID: " + id);
                audit.logAction("No doctor found for deletion with the given ID: " + id);
            }

            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
         catch (IOException e){
             System.out.println("Error with audit: " + e);
         }
    }
}
