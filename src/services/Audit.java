package services;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Audit
{
    private static Audit instance;
    FileWriter writer;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Audit()
    {
        try
        {
            this.writer = new FileWriter("src/Files/audit.csv");
            String[] columns = {"Log", "Date-time"};
            for (int i = 0; i < columns.length; i++) {
                writer.append(columns[i]);
                if (i != columns.length - 1) {
                    writer.append(",");
                }
            }
            writer.append("\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    static {
        try {
            instance = new Audit();
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred in creating DBFunctions singleton instance");
        }
    }

    public static Audit getInstance() {
        if (instance == null) {
            instance = new Audit();
        }
        return instance;
    }

    public void logAction(String action) throws IOException
    {
        writer.append(action);
        writer.append(",");
        writer.append(formatter.format(LocalDateTime.now()));
        writer.append("\n");
        writer.flush();
    }
}
