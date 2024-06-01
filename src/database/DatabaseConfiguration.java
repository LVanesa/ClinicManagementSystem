package database;

import services.Audit;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfiguration {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/clinicdb";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678";

    private static Audit audit = Audit.getInstance();
    private static Connection databaseConnection;

    private DatabaseConfiguration() { }

    public static Connection getDatabaseConnection() {
        try {
            if (databaseConnection == null || databaseConnection.isClosed()) {
                databaseConnection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                //audit.logAction("Open connection with database");
            }else {
                System.out.println("Connection Failed");
                //audit.logAction("Connection failed with database!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return databaseConnection;
    }

    public static void closeDatabaseConnection() {
        try {
            if (databaseConnection != null && !databaseConnection.isClosed()) {
                databaseConnection.close();
                //audit.logAction("Close connection with database");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
}
