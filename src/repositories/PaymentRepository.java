package repositories;

import database.DatabaseConfiguration;
import models.address.Address;
import models.payment.Payment;
import models.person.Patient;
import models.treatment.Treatment;
import models.treatment.TreatmentCategory;
import services.Audit;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository {
    private static PaymentRepository instance;
    private static Audit audit = Audit.getInstance();
    private PatientRepository patientRepository;
    private TreatmentRepository treatmentRepository;
    private PaymentRepository(){
        this.patientRepository = PatientRepository.getInstance();
        this.treatmentRepository = TreatmentRepository.getInstance();
    }

    static {
        try {
            instance = new PaymentRepository();
        } catch (Exception e) {
            throw new RuntimeException("\nException occurred in creating DBFunctions: PaymentRepository singleton instance");
        }
    }

    public static PaymentRepository getInstance() {
        if (instance == null) {
            instance = new PaymentRepository();
        }
        return instance;
    }
    public void createTables() {
        createPaymentTable();
        createPaymentTreatmentTable();
    }
    private void createPaymentTable() {
        String query = "CREATE TABLE IF NOT EXISTS PAYMENT (" +
                "id_payment INT AUTO_INCREMENT PRIMARY KEY, " +
                "paymentDate TIMESTAMP, " +
                "totalAmount DOUBLE, " +
                "id_patient INT, " +
                "FOREIGN KEY (id_patient) REFERENCES PATIENT(id_patient));";

        try {
            Connection connection = DatabaseConfiguration.getDatabaseConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            DatabaseConfiguration.closeDatabaseConnection();
            audit.logAction("Payment table has been crated");
        } catch (SQLException e) {
            System.out.println(e);
        } catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    private void createPaymentTreatmentTable() {
        String query = "CREATE TABLE IF NOT EXISTS PAYMENT_TREATMENT (" +
                "id_payment INT, " +
                "id_treatment INT, " +
                "PRIMARY KEY (id_payment, id_treatment), " +
                "FOREIGN KEY (id_payment) REFERENCES PAYMENT(id_payment), " +
                "FOREIGN KEY (id_treatment) REFERENCES TREATMENT(id_treatment));";

        try {
            Connection connection = DatabaseConfiguration.getDatabaseConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            DatabaseConfiguration.closeDatabaseConnection();
            audit.logAction("Payment_Treatment table has been crated");

        } catch (SQLException e) {
            System.out.println(e);
        } catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public void viewPayments() {
        String selectSql = "SELECT p.id_payment, p.totalAmount, " +
                "pa.firstName AS patientFirstName, pa.lastName AS patientLastName, " +
                "t.name AS treatmentName " +
                "FROM PAYMENT p " +
                "JOIN PATIENT pa ON p.id_patient = pa.id_patient " +
                "LEFT JOIN PAYMENT_TREATMENT pt ON p.id_payment = pt.id_payment " +
                "LEFT JOIN TREATMENT t ON pt.id_treatment = t.id_treatment " +
                "ORDER BY p.id_payment;";

        Connection connection = DatabaseConfiguration.getDatabaseConnection();

        try (Statement stmt = connection.createStatement()) {
            boolean empty = true;
            ResultSet resultSet = stmt.executeQuery(selectSql);

            int currentPaymentId = -1;
            StringBuilder paymentInfo = new StringBuilder();

            while (resultSet.next()) {
                int paymentId = resultSet.getInt("id_payment");

                if (empty) {
                    System.out.println("\nList of all Payments:");
                    System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    empty = false;
                }

                if (paymentId != currentPaymentId) {
                    if (currentPaymentId != -1) {
                        System.out.println(paymentInfo.toString());
                        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    }
                    currentPaymentId = paymentId;
                    paymentInfo = new StringBuilder();
                    paymentInfo.append("Payment ID: ").append(paymentId).append(" | ")
                            .append("Patient: ").append(resultSet.getString("patientFirstName")).append(" ").append(resultSet.getString("patientLastName")).append(" | ")
                            .append("Total Amount: ").append(resultSet.getDouble("totalAmount")).append("$ ").append(" | ")
                            .append("Treatments: ");
                }

                String treatmentName = resultSet.getString("treatmentName");
                if (treatmentName != null) {
                    paymentInfo.append(treatmentName).append(", ");
                }
            }

            if (!empty) {
                System.out.println(paymentInfo.toString().replaceAll(", $", ""));
                System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            }

            DatabaseConfiguration.closeDatabaseConnection();

            if (empty) {
                System.out.println("\nNo existing Payments!");
            }
            audit.logAction("View all payments");
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public void addPayment(Payment payment) {
        String insertPaymentSql = "INSERT INTO PAYMENT(paymentDate, totalAmount, id_patient) VALUES(?, ?, ?);";
        String insertPaymentTreatmentSql = "INSERT INTO PAYMENT_TREATMENT(id_payment, id_treatment) VALUES(?, ?);";

        try {
            Connection connection = DatabaseConfiguration.getDatabaseConnection();
            connection.setAutoCommit(false); // Start transaction

            // Insert into PAYMENT table
            PreparedStatement paymentStatement = connection.prepareStatement(insertPaymentSql, Statement.RETURN_GENERATED_KEYS);
            paymentStatement.setTimestamp(1, java.sql.Timestamp.valueOf(payment.getPaymentDate()));
            paymentStatement.setDouble(2, payment.getTotalAmount());
            int patientId = patientRepository.getPatientId(payment.getPatient()); // Get patient ID
            paymentStatement.setInt(3, patientId);

            int rowsInserted = paymentStatement.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = paymentStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int paymentId = generatedKeys.getInt(1);

                    // Insert into PAYMENT_TREATMENT table for each treatment
                    PreparedStatement paymentTreatmentStatement = connection.prepareStatement(insertPaymentTreatmentSql);
                    for (Treatment treatment : payment.getTreatments()) {
                        int treatmentId = treatmentRepository.getTreatmentId(treatment); // Get treatment ID
                        paymentTreatmentStatement.setInt(1, paymentId);
                        paymentTreatmentStatement.setInt(2, treatmentId);
                        paymentTreatmentStatement.addBatch(); // Add to batch for batch processing
                    }
                    paymentTreatmentStatement.executeBatch();

                    connection.commit(); // Commit transaction
                    System.out.println("\nPayment inserted successfully.");
                    audit.logAction("Added new Payment");
                } else {
                    throw new SQLException("\nInserting payment failed, no ID obtained.");
                }
            } else {
                audit.logAction("Adding new Payment failed");
                throw new SQLException("\nInserting payment failed.");
            }

            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public List<Payment> searchPaymentsByPatient(String name) {
        List<Payment> payments = new ArrayList<>();
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        String selectSql = "SELECT p.id_payment, p.paymentDate, p.totalAmount, " +
                "pa.id_patient, pa.firstName AS patientFirstName, pa.lastName AS patientLastName, " +
                "pa.email AS patientEmail, pa.phoneNumber AS patientPhoneNumber, pa.birthDate AS patientBirthDate, " +
                "pa.age AS patientAge, pa.gender AS patientGender, pa.medicalHistory AS patientMedicalHistory, " +
                "adr.country AS patientCountry, adr.city AS patientCity, adr.street AS patientStreet, adr.number_address AS patientNumberAddress, " +
                "t.id_treatment, t.name AS treatmentName, t.category AS treatmentCategory, t.price AS treatmentPrice " +
                "FROM PAYMENT p " +
                "JOIN PATIENT pa ON p.id_patient = pa.id_patient " +
                "JOIN ADDRESS adr ON pa.id_address = adr.id_address " +
                "LEFT JOIN PAYMENT_TREATMENT pt ON p.id_payment = pt.id_payment " +
                "LEFT JOIN TREATMENT t ON pt.id_treatment = t.id_treatment " +
                "WHERE pa.firstName LIKE ? OR pa.lastName LIKE ?;";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, "%" + name + "%");
            stmt.setString(2, "%" + name + "%");

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int paymentId = resultSet.getInt("id_payment");
                LocalDateTime paymentDate = resultSet.getTimestamp("paymentDate").toLocalDateTime();
                double totalAmount = resultSet.getDouble("totalAmount");

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

                // Create Payment object
                Payment payment = new Payment(paymentDate, totalAmount, patient);

                // Add treatments to payment
                List<Treatment> treatments = new ArrayList<>();
                do {
                    if (resultSet.getInt("id_payment") == paymentId) {
                        Treatment treatment = new Treatment(
                                TreatmentCategory.valueOf(resultSet.getString("treatmentCategory")),
                                resultSet.getString("treatmentName"),
                                resultSet.getDouble("treatmentPrice")
                        );
                        treatments.add(treatment);
                    } else {
                        break;
                    }
                } while (resultSet.next());

                payment.setTreatments(treatments);
                payments.add(payment);

            }
            audit.logAction("Searched payment by patient name: "+name.toUpperCase());
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
        return payments;
    }
    public Payment getPaymentById(int id) {
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        Payment payment = null;
        String selectSql = "SELECT p.id_payment, p.paymentDate, p.totalAmount, " +
                "pa.id_patient, pa.firstName AS patientFirstName, pa.lastName AS patientLastName, " +
                "pa.email AS patientEmail, pa.phoneNumber AS patientPhoneNumber, pa.birthDate AS patientBirthDate, " +
                "pa.age AS patientAge, pa.gender AS patientGender, pa.medicalHistory AS patientMedicalHistory, " +
                "adr.country AS patientCountry, adr.city AS patientCity, adr.street AS patientStreet, adr.number_address AS patientNumberAddress, " +
                "t.id_treatment, t.name AS treatmentName, t.category AS treatmentCategory, t.price AS treatmentPrice " +
                "FROM PAYMENT p " +
                "JOIN PATIENT pa ON p.id_patient = pa.id_patient " +
                "JOIN ADDRESS adr ON pa.id_address = adr.id_address " +
                "LEFT JOIN PAYMENT_TREATMENT pt ON p.id_payment = pt.id_payment " +
                "LEFT JOIN TREATMENT t ON pt.id_treatment = t.id_treatment " +
                "WHERE p.id_payment = ?;";

        try {
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                LocalDateTime paymentDate = resultSet.getTimestamp("paymentDate").toLocalDateTime();
                double totalAmount = resultSet.getDouble("totalAmount");

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

                // Create a list to hold treatments
                List<Treatment> treatments = new ArrayList<>();

                do {
                    int treatmentId = resultSet.getInt("id_treatment");
                    if (treatmentId != 0) {
                        Treatment treatment = new Treatment(
                                TreatmentCategory.valueOf(resultSet.getString("treatmentCategory")),
                                resultSet.getString("treatmentName"),
                                resultSet.getDouble("treatmentPrice")
                        );
                        treatments.add(treatment);
                    }
                } while (resultSet.next());

                // Create Payment object
                payment = new Payment(paymentDate, totalAmount, patient);
                payment.setTreatments(treatments);
            }
            audit.logAction("Got payment by Id "+id);
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
        return payment;
    }
    public void updatePayment(int id, Payment payment) {
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        try {
            // Retrieve the payment details
            String selectPaymentSql = "SELECT * FROM PAYMENT WHERE id_payment = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectPaymentSql);
            selectStatement.setInt(1, id);
            ResultSet resultSet = selectStatement.executeQuery();

            boolean empty = true;
            if (resultSet.next()) {
                empty = false;

                String updatePaymentSql = "UPDATE PAYMENT SET paymentDate = ?, totalAmount = ?, id_patient = ? WHERE id_payment = ?";
                PreparedStatement paymentStatement = connection.prepareStatement(updatePaymentSql);
                paymentStatement.setTimestamp(1, java.sql.Timestamp.valueOf(payment.getPaymentDate()));
                paymentStatement.setDouble(2, payment.getTotalAmount());

                // Get the patient ID from the database
                int patientId = patientRepository.getPatientId(payment.getPatient());

                // Set the patient ID
                paymentStatement.setInt(3, patientId);

                paymentStatement.setInt(4, id);

                paymentStatement.executeUpdate();
                System.out.println("\nThe payment was updated.");
                audit.logAction("Updated payment with id "+id);
                // Update the payment_treatment table
                updatePaymentTreatments(connection, id, payment.getTreatments());
            }

            DatabaseConfiguration.closeDatabaseConnection();
            if (empty) {
                System.out.println("\nNo existing payment with this ID!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    private void updatePaymentTreatments(Connection connection, int paymentId, List<Treatment> treatments) throws SQLException {
        // Delete existing payment treatments
        String deleteSql = "DELETE FROM PAYMENT_TREATMENT WHERE id_payment = ?";
        PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
        deleteStatement.setInt(1, paymentId);
        deleteStatement.executeUpdate();

        // Insert new payment treatments
        String insertSql = "INSERT INTO PAYMENT_TREATMENT (id_payment, id_treatment) VALUES (?, ?)";
        PreparedStatement insertStatement = connection.prepareStatement(insertSql);
        for (Treatment treatment : treatments) {
            insertStatement.setInt(1, paymentId);
            insertStatement.setInt(2, treatmentRepository.getTreatmentId(treatment)); // Assuming treatment has an ID
            insertStatement.executeUpdate();
        }
    }
    public boolean paymentExists(int id) {
        String checkPaymentSql = "SELECT 1 FROM PAYMENT WHERE id_payment = ?";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(checkPaymentSql);
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
    public void deletePaymentById(int id) {
        String deletePaymentSql = "DELETE FROM PAYMENT WHERE id_payment = ?";
        String deletePaymentTreatmentSql = "DELETE FROM PAYMENT_TREATMENT WHERE id_payment = (SELECT id_payment FROM PAYMENT WHERE id_payment = ?)";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        try {

            //Delete Payment_Treatment first
            PreparedStatement deletePaymentTreatmentStatement = connection.prepareStatement(deletePaymentTreatmentSql);
            deletePaymentTreatmentStatement.setInt(1, id);
            int paymentTreatmentRowsDeleted = deletePaymentTreatmentStatement.executeUpdate();

            //Delete Payment
            PreparedStatement deletePaymentStatement = connection.prepareStatement(deletePaymentSql);
            deletePaymentStatement.setInt(1, id);
            int paymentRowsDeleted = deletePaymentStatement.executeUpdate();

            if (paymentRowsDeleted > 0 || paymentTreatmentRowsDeleted > 0) {
                System.out.println("\nPayment deleted successfully.");
                audit.logAction("Deleted payment with ID: " + id);
            } else {
                System.out.println("\nNo payment found with ID: " + id);
                audit.logAction("No payment found for deletion with the given ID: " + id);
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
