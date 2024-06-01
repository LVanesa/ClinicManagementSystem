package repositories;

import database.DatabaseConfiguration;
import models.treatment.Treatment;
import models.treatment.TreatmentCategory;
import services.Audit;

import java.io.IOException;
import java.util.ArrayList;
import java.sql.*;
import java.util.List;

public class TreatmentRepository {
    private static TreatmentRepository instance;
    private static Audit audit = Audit.getInstance();
    private TreatmentRepository(){}

    static {
        try {
            instance = new TreatmentRepository();
        } catch (Exception e) {
            throw new RuntimeException("\nException occurred in creating DBFunctions: TreatmentRepository singleton instance");
        }
    }

    public static TreatmentRepository getInstance() {
        if (instance == null) {
            instance = new TreatmentRepository();
        }
        return instance;
    }
    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS TREATMENT (" +
                "id_treatment INT AUTO_INCREMENT PRIMARY KEY, " +
                "category VARCHAR(50), " +
                "name VARCHAR(100), " +
                "price DOUBLE);";

        try {
            Connection connection = DatabaseConfiguration.getDatabaseConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            audit.logAction("Treatment table has been created");
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            System.out.println(e);
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public int getTreatmentId(Treatment treatment) throws SQLException {
        String query = "SELECT id_treatment FROM TREATMENT WHERE name = ?";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, treatment.getName());
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            try {
                audit.logAction("Got treatment id");
            } catch (IOException e){
                System.out.println("Error with audit: " + e);
            }
            return resultSet.getInt("id_treatment");
        } else {
            try {
                audit.logAction("Didn't get treatment id");
            } catch (IOException e){
                System.out.println("Error with audit: " + e);
            }
            throw new SQLException("Treatment not found in the database.");
        }
    }
    public List<Treatment> getAllTreatments() {
        List<Treatment> treatments = new ArrayList<>();

        String selectSql = "SELECT * FROM TREATMENT";

        try (Connection connection = DatabaseConfiguration.getDatabaseConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(selectSql)) {

            while (resultSet.next()) {
                String categoryString = resultSet.getString("category");
                TreatmentCategory category = TreatmentCategory.valueOf(categoryString); // Convertim String-ul la enum
                Treatment treatment = new Treatment(
                        category,
                        resultSet.getString("name"),
                        resultSet.getDouble("price")
                );
                treatments.add(treatment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return treatments;
    }
    public void addTreatment(Treatment treatment) {
        String insertTreatmentSql = "INSERT INTO TREATMENT(category, name, price) VALUES (?, ?, ?);";

        try {
            Connection connection = DatabaseConfiguration.getDatabaseConnection();
            PreparedStatement treatmentStatement = connection.prepareStatement(insertTreatmentSql, Statement.RETURN_GENERATED_KEYS);
            treatmentStatement.setString(1, treatment.getCategory().toString());
            treatmentStatement.setString(2, treatment.getName());
            treatmentStatement.setDouble(3, treatment.getPrice());

            int rowsInserted = treatmentStatement.executeUpdate();
            if (rowsInserted == 0) {
                throw new SQLException("\nInserting treatment failed, no rows affected!");
            }

            DatabaseConfiguration.closeDatabaseConnection();
            System.out.println("\nTreatment inserted successfully.");
            // Optional: log the action
            audit.logAction("Added new Treatment: " + treatment.getName());
        } catch (SQLException e) {
            System.out.println(e);
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public void viewTreatments() {
        String selectSql = "SELECT * FROM TREATMENT;";
        Connection connection = DatabaseConfiguration.getDatabaseConnection();

        try (Statement stmt = connection.createStatement()) {
            boolean empty = true;
            ResultSet resultSet = stmt.executeQuery(selectSql);
            while (resultSet.next()) {
                if (empty) {
                    System.out.println("\nList of all Treatments:");
                    System.out.println("----------------------------------------------------------------------------------------------------------------------------");
                    empty = false;
                }
                StringBuilder treatmentInfo = new StringBuilder();
                treatmentInfo.append("Treatment ID: ").append(resultSet.getInt("id_treatment")).append(" | ")
                                .append("Category: ").append(resultSet.getString("category")).append(" | ")
                                .append("Name: ").append(resultSet.getString("name")).append(" | ")
                                .append("Price: ").append(resultSet.getString("price"));
                System.out.println(treatmentInfo);
                System.out.println("----------------------------------------------------------------------------------------------------------------------------");

            }
            audit.logAction("View All Treatments");
            DatabaseConfiguration.closeDatabaseConnection();
            if (empty) {
                System.out.println("\nNo existing Treatments!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public void searchTreatmentsByCategory(String category){
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        String selectSql = "SELECT * FROM TREATMENT  " +
                "WHERE category LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, "%" + category + "%");

            ResultSet resultSet = stmt.executeQuery();

            boolean empty = true;

            while (resultSet.next()) {
                if (empty) {
                    System.out.println("\nTreatments from Category '" + category.toUpperCase() + "':");
                    System.out.println("----------------------------------------------------------------------------------------------------------------------------");
                    empty = false;
                }

                StringBuilder treatmentInfo = new StringBuilder();
                treatmentInfo.append("Treatment ID: ").append(resultSet.getInt("id_treatment")).append(" | ")
                        .append("Category: ").append(resultSet.getString("category")).append(" | ")
                        .append("Name: ").append(resultSet.getString("name")).append(" | ")
                        .append("Price: ").append(resultSet.getString("price"));
                System.out.println(treatmentInfo);
                System.out.println("----------------------------------------------------------------------------------------------------------------------------");
            }

            if (empty) {
                System.out.println("\nNo treatments found from Category '" + category + "'.");
            }
            audit.logAction("Searched Treatments from category "+category.toUpperCase());
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }

    }
    public void searchTreatmentsByName(String name){
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        String selectSql = "SELECT * FROM TREATMENT  " +
                "WHERE name LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, "%" + name + "%");

            ResultSet resultSet = stmt.executeQuery();

            boolean empty = true;

            while (resultSet.next()) {
                if (empty) {
                    System.out.println("\nSearch results for treatments with names containing '" + name.toUpperCase() + "':");
                    System.out.println("----------------------------------------------------------------------------------------------------------------------------");
                    empty = false;
                }

                StringBuilder treatmentInfo = new StringBuilder();
                treatmentInfo.append("Treatment ID: ").append(resultSet.getInt("id_treatment")).append(" | ")
                        .append("Category: ").append(resultSet.getString("category")).append(" | ")
                        .append("Name: ").append(resultSet.getString("name")).append(" | ")
                        .append("Price: ").append(resultSet.getString("price"));
                System.out.println(treatmentInfo);
                System.out.println("----------------------------------------------------------------------------------------------------------------------------");
            }

            if (empty) {
                System.out.println("\nNo treatments found with the name '" + name.toUpperCase() + "'.");
            }
            audit.logAction("Searched treatments by name: " + name.toUpperCase());
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("Error with audit: " + e);
        }
    }
    public void updateTreatment(int id, Treatment treatment) {
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        try {
            // Retrieve the treatment details
            String selectTreatmentSql = "SELECT * FROM TREATMENT WHERE id_treatment = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectTreatmentSql);
            selectStatement.setInt(1, id);
            ResultSet resultSet = selectStatement.executeQuery();

            boolean empty = true;
            if (resultSet.next()) {
                empty = false;

                // Update the treatment details
                String updateTreatmentSql = "UPDATE TREATMENT SET category = ?, name = ?, price = ? WHERE id_treatment = ?";
                PreparedStatement treatmentStatement = connection.prepareStatement(updateTreatmentSql);
                treatmentStatement.setString(1, treatment.getCategory().toString());
                treatmentStatement.setString(2, treatment.getName());
                treatmentStatement.setDouble(3, treatment.getPrice());
                treatmentStatement.setInt(4, id);

                treatmentStatement.executeUpdate();
                System.out.println("\nThe treatment was updated.");
                audit.logAction("Updated treatment with id "+id);
            }

            DatabaseConfiguration.closeDatabaseConnection();
            if (empty) {
                System.out.println("\nNo existing treatment with this ID!");
                audit.logAction("No treatment found for update with the given ID "+id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
         catch (IOException e){
             System.out.println("Error with audit: " + e);
         }
    }
    public Treatment getTreatmentById(int id) {
        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        Treatment treatment = null;
        String selectSql = "SELECT * FROM TREATMENT WHERE id_treatment = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                TreatmentCategory category = TreatmentCategory.valueOf(resultSet.getString("category"));
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");

                treatment = new Treatment(category, name, price);
            }
            audit.logAction("Got treatment with id "+id);
            DatabaseConfiguration.closeDatabaseConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
         catch (IOException e){
             System.out.println("Error with audit: " + e);
         }
        return treatment;
    }
    public boolean treatmentExists(int id) {
        String checkTreatmentSql = "SELECT 1 FROM TREATMENT WHERE id_treatment = ?";
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
    public void deleteTreatmentById(int id) {
        String deleteTreatmentSql = "DELETE FROM TREATMENT WHERE id_treatment = ?";

        Connection connection = DatabaseConfiguration.getDatabaseConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(deleteTreatmentSql);
            statement.setInt(1, id);

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("\nTreatment deleted successfully.");
                audit.logAction("Deleted treatment with ID: " + id);
            } else {
                System.out.println("\nNo treatment found with ID: " + id);
                audit.logAction("No treatment found for deletion with the given ID: " + id);
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
