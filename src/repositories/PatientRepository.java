package repositories;

import database.DatabaseConfiguration;
import models.address.Address;
import models.person.Patient;
import services.Audit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class PatientRepository {
    private static PatientRepository instance;
    private static Audit audit = Audit.getInstance();
    private PatientRepository(){}

    static {
        try {
            instance = new PatientRepository();
        } catch (Exception e) {
            throw new RuntimeException("\nException occurred in creating DBFunctions: PatientRepository singleton instance");
        }
    }

    public static PatientRepository getInstance() {
        if (instance == null) {
            instance = new PatientRepository();
        }
        return instance;
    }
    public void createTable() {
        String query1 = "CREATE TABLE IF NOT EXISTS ADDRESS " +
                "(id_address INT AUTO_INCREMENT PRIMARY KEY, " +
                "country VARCHAR(100), " +
                "city VARCHAR(100), " +
                "street VARCHAR(100), " +
                "number_address INT);";

        String query2 = "CREATE TABLE IF NOT EXISTS PATIENT " +
                "(id_patient INT AUTO_INCREMENT PRIMARY KEY, " +
                "firstName VARCHAR(100), " +
                "lastName VARCHAR(100), " +
                "email VARCHAR(100), " +
                "id_address INT, " +
                "phoneNumber VARCHAR(20), " +
                "birthDate DATETIME, " +
                "age INT, " +
                "gender VARCHAR(10), " +
                "medicalHistory TEXT, " +
                "FOREIGN KEY (id_address) REFERENCES ADDRESS(id_address) ON DELETE CASCADE);";

        try {
            Connection connection = DatabaseConfiguration.getDatabaseConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(query1);
            statement.executeUpdate(query2);
            audit.logAction("Patient and address tables have been crated");
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void addPatient(Patient patient){
        Address address = patient.getAddress();
        String insertAddressSql = "INSERT INTO ADDRESS(country, city, street, number_address) VALUES(?, ?, ?, ?);";

        try{
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
                String insertPatientSql="INSERT INTO PATIENT(firstName,lastName,email,id_address,phoneNumber,birthDate,age,gender,medicalHistory) VALUES(?,?,?,?,?,?,?,?,?);";
                PreparedStatement patientStatement = connection.prepareStatement(insertPatientSql, Statement.RETURN_GENERATED_KEYS);
                patientStatement.setString(1, patient.getFirstName());
                patientStatement.setString(2, patient.getLastName());
                patientStatement.setString(3,  patient.getEmail());
                patientStatement.setInt(4,  addressId);
                patientStatement.setString(5,  patient.getPhoneNumber());
                patientStatement.setTimestamp(6, java.sql.Timestamp.valueOf(patient.getBirthDate()));
                patientStatement.setInt(7,  patient.getAge());
                patientStatement.setString(8,  patient.getGender());
                patientStatement.setString(9,  patient.getMedicalHistory());

                int rowsInserted = patientStatement.executeUpdate();
                generatedKeys = patientStatement.getGeneratedKeys();

                DatabaseConfiguration.closeDatabaseConnection();
                if (rowsInserted > 0) {
                    System.out.println("\nPatient inserted successfully.");
                    audit.logAction("Added new Address and Patient");

                } else {
                    throw new SQLException("\nInserting patient failed.");
                }
            }else {
                throw new SQLException("\nInserting address failed, no ID obtained.");
            }
        }
        catch(SQLException e){
            System.out.println(e);
        }
        catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public int getPatientId(Patient patient) throws SQLException {
        String query = "SELECT id_patient FROM PATIENT WHERE firstName = ? AND lastName = ? AND email = ?";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, patient.getFirstName());
        statement.setString(2, patient.getLastName());
        statement.setString(3, patient.getEmail());

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            try {
                audit.logAction("Get Patient Id");
            } catch (IOException e){
                System.out.println("Error with audit: " + e);
            }
            return resultSet.getInt("id_patient");
        } else {
            throw new SQLException("Patient not found in the database.");
        }
    }
    public void viewPatients() {
        String selectSql = "SELECT * FROM PATIENT INNER JOIN ADDRESS ON PATIENT.ID_ADDRESS=ADDRESS.ID_ADDRESS;";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();

        try (Statement stmt = connection.createStatement()) {
            boolean empty = true;
            ResultSet resultSet = stmt.executeQuery(selectSql);
            while (resultSet.next()) {
                if (empty) {
                    System.out.println("\nList of all Patients:");
                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    empty = false;
                }

                StringBuilder patientInfo = new StringBuilder();
                patientInfo.append("PatientID: ").append(resultSet.getInt("id_patient")).append(" | ")
                        .append("Full Name: ").append(resultSet.getString("firstName")).append(" ")
                        .append(resultSet.getString("lastName")).append(" | ")
                        .append("Email: ").append(resultSet.getString("email")).append(" | ")
                        .append("Phone Number: ").append(resultSet.getString("phoneNumber")).append(" | ")
                        .append("Birth Date: ").append(resultSet.getTimestamp("birthDate")).append(" | ")
                        .append("Age: ").append(resultSet.getInt("age")).append(" | ")
                        .append("Gender: ").append(resultSet.getString("gender")).append(" | ")
                        .append("Medical History: ").append(resultSet.getString("medicalHistory")).append(" | ")
                        .append("Address: ").append(resultSet.getString("country")).append(", ")
                        .append(resultSet.getString("city")).append(", ")
                        .append(resultSet.getString("street")).append(" ")
                        .append(resultSet.getInt("number_address"));

                System.out.println(patientInfo);
                System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

            }
            audit.logAction("View all Patients");
            DatabaseConfiguration.closeDatabaseConnection();
            if (empty) {
                System.out.println("No existing Patients!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
         catch (IOException e){
             System.out.println("Error with audit: " + e);
         }
    }
    public void searchPatientsByName(String name){
        String selectSql = "SELECT * FROM PATIENT INNER JOIN ADDRESS ON PATIENT.ID_ADDRESS=ADDRESS.ID_ADDRESS " +
                "WHERE firstName LIKE ? OR lastName LIKE ?";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, "%" + name + "%");
            stmt.setString(2, "%" + name + "%");

            ResultSet resultSet = stmt.executeQuery();

            boolean empty = true;
            while (resultSet.next()) {
                if (empty) {
                    System.out.println("Search results for patients with names containing '" + name.toUpperCase() + "':");
                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    empty = false;
                }

                StringBuilder patientInfo = new StringBuilder();
                patientInfo.append("PatientID: ").append(resultSet.getInt("id_patient")).append(" | ")
                        .append("Full Name: ").append(resultSet.getString("firstName")).append(" ")
                        .append(resultSet.getString("lastName")).append(" | ")
                        .append("Email: ").append(resultSet.getString("email")).append(" | ")
                        .append("Phone Number: ").append(resultSet.getString("phoneNumber")).append(" | ")
                        .append("Birth Date: ").append(resultSet.getTimestamp("birthDate")).append(" | ")
                        .append("Age: ").append(resultSet.getInt("age")).append(" | ")
                        .append("Gender: ").append(resultSet.getString("gender")).append(" | ")
                        .append("Medical History: ").append(resultSet.getString("medicalHistory")).append(" | ")
                        .append("Address: ").append(resultSet.getString("country")).append(", ")
                        .append(resultSet.getString("city")).append(", ")
                        .append(resultSet.getString("street")).append(" ")
                        .append(resultSet.getInt("number_address"));

                System.out.println(patientInfo);
                System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

            }

            if (empty) {
                System.out.println("\nNo patients found with the name '" + name.toUpperCase() + "'.");
            }
            audit.logAction("Searched Patients By Name " + name.toUpperCase());
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String selectSql = "SELECT * FROM PATIENT INNER JOIN ADDRESS ON PATIENT.id_address=ADDRESS.id_address;";

        try (Connection connection = DatabaseConfiguration.getDatabaseConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(selectSql)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id_patient");
                String firstName = resultSet.getString("firstName");
                String lastName = resultSet.getString("lastName");
                String email = resultSet.getString("email");
                String phoneNumber = resultSet.getString("phoneNumber");
                // Converting java.sql.Date to LocalDateTime
                java.sql.Date sqlBirthDate = resultSet.getDate("birthDate");
                LocalDateTime birthDate = sqlBirthDate.toLocalDate().atStartOfDay();
                int age = resultSet.getInt("age");
                String gender = resultSet.getString("gender");
                String medicalHistory = resultSet.getString("medicalHistory");

                // Adresa pacientului
                String country = resultSet.getString("country");
                String city = resultSet.getString("city");
                String street = resultSet.getString("street");
                int number = resultSet.getInt("number_address");
                Address address = new Address(country, city, street, number);

                // Creare obiect Patient
                Patient patient = new Patient(firstName, lastName, email, address, phoneNumber, birthDate, age, gender, medicalHistory);
                patient.setAddress(address);

                // Adăugare pacient în listă
                patients.add(patient);
            }
            audit.logAction("Get all Patients");
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
        return patients;
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
    public void updatePatient(int id, Patient patient) {
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        try {
            // Retrieve the patient and address details
            String selectPatientSql = "SELECT * FROM PATIENT WHERE id_patient = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectPatientSql);
            selectStatement.setInt(1, id);
            ResultSet resultSet = selectStatement.executeQuery();

            boolean empty = true;
            if (resultSet.next()) {
                empty = false;
                int idAddress = resultSet.getInt("id_address");

                // Update the address
                updateAddress(idAddress, patient.getAddress());

                // Update the patient details
                String updatePatientSql = "UPDATE PATIENT SET firstName = ?, lastName = ?, email = ?, phoneNumber = ?, birthDate = ?, age = ?, gender = ?, medicalHistory = ? WHERE id_patient = ?";
                PreparedStatement patientStatement = connection.prepareStatement(updatePatientSql);
                patientStatement.setString(1, patient.getFirstName());
                patientStatement.setString(2, patient.getLastName());
                patientStatement.setString(3, patient.getEmail());
                patientStatement.setString(4, patient.getPhoneNumber());
                patientStatement.setTimestamp(5, java.sql.Timestamp.valueOf(patient.getBirthDate()));
                patientStatement.setInt(6, patient.getAge());
                patientStatement.setString(7, patient.getGender());
                patientStatement.setString(8, patient.getMedicalHistory());
                patientStatement.setInt(9, id);

                patientStatement.executeUpdate();
                System.out.println("\nThe patient and address were updated.");
                audit.logAction("The patient with id " + id +" was updated");
            }

            DatabaseConfiguration.closeDatabaseConnection();
            if (empty) {
                System.out.println("\nNo existing patient with this ID!");
                audit.logAction("No patient found for update with the given ID " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
         catch (IOException e){
             System.out.println("Error with audit: " + e);
         }
    }
    public Patient getPatientById(int id) {
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        Patient patient = null;
        String selectSql = "SELECT p.*, a.country, a.city, a.street, a.number_address FROM PATIENT p " +
                "INNER JOIN ADDRESS a ON p.id_address = a.id_address WHERE p.id_patient = ?";

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

                patient = new Patient(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        address,
                        resultSet.getString("phoneNumber"),
                        resultSet.getTimestamp("birthDate").toLocalDateTime(),
                        resultSet.getInt("age"),
                        resultSet.getString("gender"),
                        resultSet.getString("medicalHistory")
                );
            }
            audit.logAction("Get Patient By Id " + id);
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
         catch (IOException e){
             System.out.println("Error with audit: " + e);
         }
        return patient;
    }
    public boolean patientExists(int id) {
        String checkPatientSql = "SELECT 1 FROM PATIENT WHERE id_patient = ?";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(checkPatientSql);
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
    public void deletePatientById(int id) {
        String deletePatientSql = "DELETE FROM PATIENT WHERE id_patient = ?";
        String deleteAddressSql = "DELETE FROM ADDRESS WHERE id_address = (SELECT id_address FROM PATIENT WHERE id_patient = ?)";

        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        try {
            // Delete the address associated with the patient
            PreparedStatement deleteAddressStatement = connection.prepareStatement(deleteAddressSql);
            deleteAddressStatement.setInt(1, id);
            int addressRowsDeleted = deleteAddressStatement.executeUpdate();

            // Delete the patient
            PreparedStatement deletePatientStatement = connection.prepareStatement(deletePatientSql);
            deletePatientStatement.setInt(1, id);
            int patientRowsDeleted = deletePatientStatement.executeUpdate();


            // Check if both patient and address were deleted successfully
            if (patientRowsDeleted > 0 || addressRowsDeleted > 0) {
                System.out.println("\nPatient deleted successfully.");
                audit.logAction("Deleted patient with ID: " + id);
            } else {
                System.out.println("\nNo patient found with ID: " + id);
                audit.logAction("No patient found for deletion with the given ID: " + id);
            }

            // Close the database connection
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
         catch (IOException e){
             System.out.println("Error with audit: " + e);
         }
    }


}
