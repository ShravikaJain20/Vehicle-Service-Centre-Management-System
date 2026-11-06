import config.DatabaseConfig;
import java.sql.Connection;

public class DatabaseTest {
    public static void main(String[] args) {
        try {
            Connection con = DatabaseConfig.getConnection();

            if (con != null) {
                System.out.println("MySQL Connected Successfully!");
                con.close();
            }

        } catch (Exception e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
        }
    }
}