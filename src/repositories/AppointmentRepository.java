package repositories;

import database.DatabaseConfiguration;
import models.address.Address;
import models.appointment.Appointment;
import models.appointment.AppointmentStatus;
import models.person.Doctor;
import models.person.Patient;
import models.treatment.Treatment;
import models.treatment.TreatmentCategory;
import services.Audit;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static models.person.Doctor.deserializeSpecializations;

public class AppointmentRepository {
    private static AppointmentRepository instance;
    private static Audit audit = Audit.getInstance();
    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;
    private TreatmentRepository treatmentRepository;

    private AppointmentRepository() {
        this.patientRepository = PatientRepository.getInstance();
        this.doctorRepository = DoctorRepository.getInstance();
        this.treatmentRepository = TreatmentRepository.getInstance();
    }

    static {
        try {
            instance = new AppointmentRepository();
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred in creating DBFunctions: AppointmentRepository singleton instance");
        }
    }

    public static AppointmentRepository getInstance() {
        if (instance == null) {
            instance = new AppointmentRepository();
        }
        return instance;
    }
    public void createTable() {
        String query1 = "CREATE TABLE IF NOT EXISTS APPOINTMENT (" +
                "id_appointment INT AUTO_INCREMENT PRIMARY KEY, " +
                "date DATETIME, " +
                "id_patient INT, " +
                "id_treatment INT, " +
                "id_doctor INT, " +
                "status VARCHAR(20), " +
                "FOREIGN KEY (id_patient) REFERENCES PATIENT(id_patient), " +
                "FOREIGN KEY (id_treatment) REFERENCES TREATMENT(id_treatment), " +
                "FOREIGN KEY (id_doctor) REFERENCES DOCTOR(id_doctor));";

        try {
            Connection connection = DatabaseConfiguration.getDatabaseConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(query1);
            audit.logAction("Appointment table has been crated");
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            System.out.println(e);
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }

    public void addAppointment(Appointment appointment) {
        String insertAppointmentSql = "INSERT INTO APPOINTMENT(date, id_patient, id_treatment, id_doctor, status) VALUES(?, ?, ?, ?, ?);";

        try {
            Connection connection = DatabaseConfiguration.getDatabaseConnection();
            PreparedStatement appointmentStatement = connection.prepareStatement(insertAppointmentSql, Statement.RETURN_GENERATED_KEYS);

            // Set the appointment date
            appointmentStatement.setTimestamp(1, java.sql.Timestamp.valueOf(appointment.getDate()));

            // Get the IDs from the database
            int patientId = patientRepository.getPatientId(appointment.getPatient()); // Get patient ID
            int treatmentId = treatmentRepository.getTreatmentId(appointment.getTreatment()); // Get treatment ID
            int doctorId = doctorRepository.getDoctorId(appointment.getDoctor()); // Get doctor ID

            // Set the IDs and status
            appointmentStatement.setInt(2, patientId);
            appointmentStatement.setInt(3, treatmentId);
            appointmentStatement.setInt(4, doctorId);
            appointmentStatement.setString(5, appointment.getStatus().toString()); // Assuming status is an enum with a proper toString() method

            int rowsInserted = appointmentStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("\nAppointment inserted successfully.");
                audit.logAction("Added new Appointment");
            } else {
                throw new SQLException("\nInserting appointment failed.");
            }
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            System.out.println(e);
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public void viewAppointments() {
        String selectSql = "SELECT a.id_appointment, a.date, a.status, p.firstName AS patientFirstName, p.lastName AS patientLastName, "
                + "d.firstName AS doctorFirstName, d.lastName AS doctorLastName, t.name AS treatmentName "
                + "FROM APPOINTMENT a "
                + "JOIN PATIENT p ON a.id_patient = p.id_patient "
                + "JOIN DOCTOR d ON a.id_doctor = d.id_doctor "
                + "JOIN TREATMENT t ON a.id_treatment = t.id_treatment;";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();

        try (Statement stmt = connection.createStatement()) {
            boolean empty = true;
            ResultSet resultSet = stmt.executeQuery(selectSql);
            while (resultSet.next()) {
                if (empty) {
                    System.out.println("\nList of all Appointments:");
                    System.out.println("----------------------------------------------------------------------------------------------------------------------------");
                    empty = false;
                }
                StringBuilder appointmentInfo = new StringBuilder();
                appointmentInfo.append("Appointment ID: ").append(resultSet.getInt("id_appointment")).append(" | ")
                        .append("Date: ").append(resultSet.getTimestamp("date")).append(" | ")
                        .append("Patient: ").append(resultSet.getString("patientFirstName")).append(" ").append(resultSet.getString("patientLastName")).append(" | ")
                        .append("Doctor: ").append(resultSet.getString("doctorFirstName")).append(" ").append(resultSet.getString("doctorLastName")).append(" | ")
                        .append("Treatment: ").append(resultSet.getString("treatmentName")).append(" | ")
                        .append("Status: ").append(resultSet.getString("status"));
                System.out.println(appointmentInfo);
                System.out.println("----------------------------------------------------------------------------------------------------------------------------");
            }
            DatabaseConfiguration.closeDatabaseConnection();
            if (empty) {
                System.out.println("\nNo existing Appointments!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String selectSql = "SELECT a.id_appointment, a.date, a.status, "
                + "p.id_patient, p.firstName AS patientFirstName, p.lastName AS patientLastName, "
                + "p.email AS patientEmail, p.phoneNumber AS patientPhoneNumber, p.birthDate AS patientBirthDate, "
                + "p.age AS patientAge, p.gender AS patientGender, p.medicalHistory AS patientMedicalHistory, "
                + "pa.country AS patientCountry, pa.city AS patientCity, pa.street AS patientStreet, pa.number_address AS patientNumberAddress, "
                + "d.id_doctor, d.firstName AS doctorFirstName, d.lastName AS doctorLastName, "
                + "d.email AS doctorEmail, d.phoneNumber AS doctorPhoneNumber, d.specializations AS doctorSpecializations, "
                + "da.country AS doctorCountry, da.city AS doctorCity, da.street AS doctorStreet, da.number_address AS doctorNumberAddress, "
                + "t.id_treatment, t.name AS treatmentName, t.category AS treatmentCategory, t.price AS treatmentPrice "
                + "FROM APPOINTMENT a "
                + "JOIN PATIENT p ON a.id_patient = p.id_patient "
                + "JOIN ADDRESS pa ON p.id_address = pa.id_address "
                + "JOIN DOCTOR d ON a.id_doctor = d.id_doctor "
                + "JOIN ADDRESS da ON d.id_address = da.id_address "
                + "JOIN TREATMENT t ON a.id_treatment = t.id_treatment;";

        try (Connection connection = DatabaseConfiguration.getDatabaseConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(selectSql)) {

            while (resultSet.next()) {
                int appointmentId = resultSet.getInt("id_appointment");
                LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                AppointmentStatus status = AppointmentStatus.valueOf(resultSet.getString("status"));

                // Create Patient Address
                Address patientAddress = new Address(
                        resultSet.getString("patientCountry"),
                        resultSet.getString("patientCity"),
                        resultSet.getString("patientStreet"),
                        resultSet.getInt("patientNumberAddress")
                );

                // Create Patient object
                Patient patient = new Patient(
                        resultSet.getString("patientFirstName"),
                        resultSet.getString("patientLastName"),
                        resultSet.getString("patientEmail"),
                        patientAddress,
                        resultSet.getString("patientPhoneNumber"),
                        resultSet.getTimestamp("patientBirthDate").toLocalDateTime(),
                        resultSet.getInt("patientAge"),
                        resultSet.getString("patientGender"),
                        resultSet.getString("patientMedicalHistory")
                );

                // Create Doctor Address
                Address doctorAddress = new Address(
                        resultSet.getString("doctorCountry"),
                        resultSet.getString("doctorCity"),
                        resultSet.getString("doctorStreet"),
                        resultSet.getInt("doctorNumberAddress")
                );

                // Create Doctor object
                Doctor doctor = new Doctor(
                        resultSet.getString("doctorFirstName"),
                        resultSet.getString("doctorLastName"),
                        resultSet.getString("doctorEmail"),
                        doctorAddress,
                        resultSet.getString("doctorPhoneNumber")
                );
                doctor.setSpecializations(deserializeSpecializations(resultSet.getString("doctorSpecializations")));

                // Create Treatment object
                Treatment treatment = new Treatment(
                        TreatmentCategory.valueOf(resultSet.getString("treatmentCategory")),
                        resultSet.getString("treatmentName"),
                        resultSet.getDouble("treatmentPrice")
                );

                // Create Appointment object
                Appointment appointment = new Appointment(date, patient, treatment, doctor, status);
                appointments.add(appointment);
                audit.logAction("View All Appointments");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
        return appointments;
    }
    public Appointment getAppointmentById(int id){
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        Appointment appointment = null;
        String selectSql = "SELECT a.id_appointment, a.date, a.status, "
                + "p.id_patient, p.firstName AS patientFirstName, p.lastName AS patientLastName, "
                + "p.email AS patientEmail, p.phoneNumber AS patientPhoneNumber, p.birthDate AS patientBirthDate, "
                + "p.age AS patientAge, p.gender AS patientGender, p.medicalHistory AS patientMedicalHistory, "
                + "pa.country AS patientCountry, pa.city AS patientCity, pa.street AS patientStreet, pa.number_address AS patientNumberAddress, "
                + "d.id_doctor, d.firstName AS doctorFirstName, d.lastName AS doctorLastName, "
                + "d.email AS doctorEmail, d.phoneNumber AS doctorPhoneNumber, d.specializations AS doctorSpecializations, "
                + "da.country AS doctorCountry, da.city AS doctorCity, da.street AS doctorStreet, da.number_address AS doctorNumberAddress, "
                + "t.id_treatment, t.name AS treatmentName, t.category AS treatmentCategory, t.price AS treatmentPrice "
                + "FROM APPOINTMENT a "
                + "JOIN PATIENT p ON a.id_patient = p.id_patient "
                + "JOIN ADDRESS pa ON p.id_address = pa.id_address "
                + "JOIN DOCTOR d ON a.id_doctor = d.id_doctor "
                + "JOIN ADDRESS da ON d.id_address = da.id_address "
                + "JOIN TREATMENT t ON a.id_treatment = t.id_treatment "
                + "WHERE a.id_appointment = ?;";


        try {
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int appointmentId = resultSet.getInt("id_appointment");
                LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                AppointmentStatus status = AppointmentStatus.valueOf(resultSet.getString("status"));

                // Create Patient Address
                Address patientAddress = new Address(
                        resultSet.getString("patientCountry"),
                        resultSet.getString("patientCity"),
                        resultSet.getString("patientStreet"),
                        resultSet.getInt("patientNumberAddress")
                );

                // Create Patient object
                Patient patient = new Patient(
                        resultSet.getString("patientFirstName"),
                        resultSet.getString("patientLastName"),
                        resultSet.getString("patientEmail"),
                        patientAddress,
                        resultSet.getString("patientPhoneNumber"),
                        resultSet.getTimestamp("patientBirthDate").toLocalDateTime(),
                        resultSet.getInt("patientAge"),
                        resultSet.getString("patientGender"),
                        resultSet.getString("patientMedicalHistory")
                );

                // Create Doctor Address
                Address doctorAddress = new Address(
                        resultSet.getString("doctorCountry"),
                        resultSet.getString("doctorCity"),
                        resultSet.getString("doctorStreet"),
                        resultSet.getInt("doctorNumberAddress")
                );

                // Create Doctor object
                Doctor doctor = new Doctor(
                        resultSet.getString("doctorFirstName"),
                        resultSet.getString("doctorLastName"),
                        resultSet.getString("doctorEmail"),
                        doctorAddress,
                        resultSet.getString("doctorPhoneNumber")
                );
                doctor.setSpecializations(deserializeSpecializations(resultSet.getString("doctorSpecializations")));

                // Create Treatment object
                Treatment treatment = new Treatment(
                        TreatmentCategory.valueOf(resultSet.getString("treatmentCategory")),
                        resultSet.getString("treatmentName"),
                        resultSet.getDouble("treatmentPrice")
                );
                // Create Appointment object
                appointment = new Appointment(date, patient, treatment, doctor, status);

            }

            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // catch (IOException e){
        //     System.out.println("Error with audit: " + e);
        // }

        return appointment;
    }
    public List<Appointment> searchAppointmentsByPatient(String name) {
        List<Appointment> appointments = new ArrayList<>();
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        String selectSql = "SELECT a.id_appointment, a.date, a.status, "
                + "p.id_patient, p.firstName AS patientFirstName, p.lastName AS patientLastName, "
                + "p.email AS patientEmail, p.phoneNumber AS patientPhoneNumber, p.birthDate AS patientBirthDate, "
                + "p.age AS patientAge, p.gender AS patientGender, p.medicalHistory AS patientMedicalHistory, "
                + "pa.country AS patientCountry, pa.city AS patientCity, pa.street AS patientStreet, pa.number_address AS patientNumberAddress, "
                + "d.id_doctor, d.firstName AS doctorFirstName, d.lastName AS doctorLastName, "
                + "d.email AS doctorEmail, d.phoneNumber AS doctorPhoneNumber, d.specializations AS doctorSpecializations, "
                + "da.country AS doctorCountry, da.city AS doctorCity, da.street AS doctorStreet, da.number_address AS doctorNumberAddress, "
                + "t.id_treatment, t.name AS treatmentName, t.category AS treatmentCategory, t.price AS treatmentPrice "
                + "FROM APPOINTMENT a "
                + "JOIN PATIENT p ON a.id_patient = p.id_patient "
                + "JOIN ADDRESS pa ON p.id_address = pa.id_address "
                + "JOIN DOCTOR d ON a.id_doctor = d.id_doctor "
                + "JOIN ADDRESS da ON d.id_address = da.id_address "
                + "JOIN TREATMENT t ON a.id_treatment = t.id_treatment "
                + "WHERE p.firstName LIKE ? OR p.lastName LIKE ?;";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, "%" + name + "%");
            stmt.setString(2, "%" + name + "%");

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int appointmentId = resultSet.getInt("id_appointment");
                LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                AppointmentStatus status = AppointmentStatus.valueOf(resultSet.getString("status"));

                // Create Patient Address
                Address patientAddress = new Address(
                        resultSet.getString("patientCountry"),
                        resultSet.getString("patientCity"),
                        resultSet.getString("patientStreet"),
                        resultSet.getInt("patientNumberAddress")
                );

                // Create Patient object
                Patient patient = new Patient(
                        resultSet.getString("patientFirstName"),
                        resultSet.getString("patientLastName"),
                        resultSet.getString("patientEmail"),
                        patientAddress,
                        resultSet.getString("patientPhoneNumber"),
                        resultSet.getTimestamp("patientBirthDate").toLocalDateTime(),
                        resultSet.getInt("patientAge"),
                        resultSet.getString("patientGender"),
                        resultSet.getString("patientMedicalHistory")
                );

                // Create Doctor Address
                Address doctorAddress = new Address(
                        resultSet.getString("doctorCountry"),
                        resultSet.getString("doctorCity"),
                        resultSet.getString("doctorStreet"),
                        resultSet.getInt("doctorNumberAddress")
                );

                // Create Doctor object
                Doctor doctor = new Doctor(
                        resultSet.getString("doctorFirstName"),
                        resultSet.getString("doctorLastName"),
                        resultSet.getString("doctorEmail"),
                        doctorAddress,
                        resultSet.getString("doctorPhoneNumber")
                );
                doctor.setSpecializations(deserializeSpecializations(resultSet.getString("doctorSpecializations")));

                // Create Treatment object
                Treatment treatment = new Treatment(
                        TreatmentCategory.valueOf(resultSet.getString("treatmentCategory")),
                        resultSet.getString("treatmentName"),
                        resultSet.getDouble("treatmentPrice")
                );

                // Create Appointment object
                Appointment appointment = new Appointment(date, patient, treatment, doctor, status);
                appointments.add(appointment);
            }
            audit.logAction("Searched Appointment by Patient name "+name.toUpperCase());
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
        return appointments;
    }
    public List<Appointment> searchAppointmentsByDoctor(String name) {
        List<Appointment> appointments = new ArrayList<>();
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        String selectSql = "SELECT a.id_appointment, a.date, a.status, "
                + "p.id_patient, p.firstName AS patientFirstName, p.lastName AS patientLastName, "
                + "p.email AS patientEmail, p.phoneNumber AS patientPhoneNumber, p.birthDate AS patientBirthDate, "
                + "p.age AS patientAge, p.gender AS patientGender, p.medicalHistory AS patientMedicalHistory, "
                + "pa.country AS patientCountry, pa.city AS patientCity, pa.street AS patientStreet, pa.number_address AS patientNumberAddress, "
                + "d.id_doctor, d.firstName AS doctorFirstName, d.lastName AS doctorLastName, "
                + "d.email AS doctorEmail, d.phoneNumber AS doctorPhoneNumber, d.specializations AS doctorSpecializations, "
                + "da.country AS doctorCountry, da.city AS doctorCity, da.street AS doctorStreet, da.number_address AS doctorNumberAddress, "
                + "t.id_treatment, t.name AS treatmentName, t.category AS treatmentCategory, t.price AS treatmentPrice "
                + "FROM APPOINTMENT a "
                + "JOIN PATIENT p ON a.id_patient = p.id_patient "
                + "JOIN ADDRESS pa ON p.id_address = pa.id_address "
                + "JOIN DOCTOR d ON a.id_doctor = d.id_doctor "
                + "JOIN ADDRESS da ON d.id_address = da.id_address "
                + "JOIN TREATMENT t ON a.id_treatment = t.id_treatment "
                + "WHERE d.firstName LIKE ? OR d.lastName LIKE ?;";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, "%" + name + "%");
            stmt.setString(2, "%" + name + "%");

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int appointmentId = resultSet.getInt("id_appointment");
                LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                AppointmentStatus status = AppointmentStatus.valueOf(resultSet.getString("status"));

                // Create Patient Address
                Address patientAddress = new Address(
                        resultSet.getString("patientCountry"),
                        resultSet.getString("patientCity"),
                        resultSet.getString("patientStreet"),
                        resultSet.getInt("patientNumberAddress")
                );

                // Create Patient object
                Patient patient = new Patient(
                        resultSet.getString("patientFirstName"),
                        resultSet.getString("patientLastName"),
                        resultSet.getString("patientEmail"),
                        patientAddress,
                        resultSet.getString("patientPhoneNumber"),
                        resultSet.getTimestamp("patientBirthDate").toLocalDateTime(),
                        resultSet.getInt("patientAge"),
                        resultSet.getString("patientGender"),
                        resultSet.getString("patientMedicalHistory")
                );

                // Create Doctor Address
                Address doctorAddress = new Address(
                        resultSet.getString("doctorCountry"),
                        resultSet.getString("doctorCity"),
                        resultSet.getString("doctorStreet"),
                        resultSet.getInt("doctorNumberAddress")
                );

                // Create Doctor object
                Doctor doctor = new Doctor(
                        resultSet.getString("doctorFirstName"),
                        resultSet.getString("doctorLastName"),
                        resultSet.getString("doctorEmail"),
                        doctorAddress,
                        resultSet.getString("doctorPhoneNumber")
                );
                doctor.setSpecializations(deserializeSpecializations(resultSet.getString("doctorSpecializations")));

                // Create Treatment object
                Treatment treatment = new Treatment(
                        TreatmentCategory.valueOf(resultSet.getString("treatmentCategory")),
                        resultSet.getString("treatmentName"),
                        resultSet.getDouble("treatmentPrice")
                );

                // Create Appointment object
                Appointment appointment = new Appointment(date, patient, treatment, doctor, status);
                appointments.add(appointment);
            }
            audit.logAction("Searched Appointments by Doctor Name "+name.toUpperCase());
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
        return appointments;
    }
    public void updateAppointment(int id, Appointment appointment) {
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        try {
            // Retrieve the treatment details
            String selectAppointmentSql = "SELECT * FROM APPOINTMENT WHERE id_appointment = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectAppointmentSql);
            selectStatement.setInt(1, id);
            ResultSet resultSet = selectStatement.executeQuery();

            boolean empty = true;
            if (resultSet.next()) {
                empty = false;

                String updateAppointmentSql = "UPDATE APPOINTMENT SET date = ?, id_patient = ?, id_treatment = ? , id_doctor = ?, status = ? WHERE id_appointment = ?";
                PreparedStatement appointmentStatement = connection.prepareStatement(updateAppointmentSql);
                appointmentStatement.setTimestamp(1, java.sql.Timestamp.valueOf(appointment.getDate()));

                // Get the IDs from the database
                int patientId = patientRepository.getPatientId(appointment.getPatient()); // Get patient ID
                int treatmentId = treatmentRepository.getTreatmentId(appointment.getTreatment()); // Get treatment ID
                int doctorId = doctorRepository.getDoctorId(appointment.getDoctor()); // Get doctor ID

                // Set the IDs and status
                appointmentStatement.setInt(2, patientId);
                appointmentStatement.setInt(3, treatmentId);
                appointmentStatement.setInt(4, doctorId);
                appointmentStatement.setString(5, appointment.getStatus().toString()); // Assuming status is an enum with a proper toString() method
                appointmentStatement.setInt(6,id);

                appointmentStatement.executeUpdate();
                System.out.println("\nThe appointment was updated.");
                audit.logAction("Updated Appointment with id "+id);
            }

            DatabaseConfiguration.closeDatabaseConnection();
            if (empty) {
                System.out.println("\nNo existing appointment with this ID!");
                audit.logAction("No appointment found for update with the given ID "+id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
         catch (IOException e){
             System.out.println("Error with audit: " + e);
         }
    }
    public boolean appointmentExists(int id) {
        String checkTreatmentSql = "SELECT 1 FROM APPOINTMENT WHERE id_appointment = ?";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(checkTreatmentSql);
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
    public void deleteAppointmentById(int id) {
        String deleteAppointmentSql = "DELETE FROM APPOINTMENT WHERE id_appointment = ?";

        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(deleteAppointmentSql);
            statement.setInt(1, id);

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("\nAppointment deleted successfully.");
                audit.logAction("Deleted appointment with ID: " + id);
            } else {
                System.out.println("\nNo appointment found with ID: " + id);
                audit.logAction("No appointment found for deletion with the given ID: " + id);
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

