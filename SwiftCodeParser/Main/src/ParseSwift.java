import java.io.*;
import java.sql.*;
import java.util.*;

public class ParseSwift {
    public static void main(String[] args) {
        ConfigReader config = new ConfigReader("config.properties");
        String filePath = config.getProperty("csv.filepath");
        String dbUrl = config.getProperty("db.url");
        String dbUser = config.getProperty("db.user");
        String dbPassword = config.getProperty("db.password");

        LoggerUtil.setupLogger(config.getProperty("log.filepath"));

        List<SwiftCode> swiftCodes = readSwiftCodes(filePath);

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            createTableIfNotExists(conn);
            DatabaseIndexer.addIndexesToDatabase(conn);
            insertIntoDatabase(conn, swiftCodes);
        } catch (SQLException e) {
            LoggerUtil.logError("Database connection error: " + e.getMessage());
        }
    }

    private static List<SwiftCode> readSwiftCodes(String filePath) {
        List<SwiftCode> swiftCodes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = splitCSVLine(line);

                if (parts.length < 5) continue;

                String countryISO2 = parts[0];
                String swiftCode = parts[1];
                String bankName = parts[3];
                String address = parts[4];
                String countryName = parts[5];

                boolean isHeadquarter = swiftCode.endsWith("XXX");

                SwiftCode swiftCodeObj = new SwiftCode(countryISO2, swiftCode, bankName, address, countryName, isHeadquarter);
                swiftCodes.add(swiftCodeObj);
            }
        } catch (IOException e) {
            LoggerUtil.logError("Error reading file: " + e.getMessage());
        }
        return swiftCodes;
    }

    private static String[] splitCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);

            if (currentChar == '"') {
                insideQuotes = !insideQuotes;
            } else if (currentChar == ',' && !insideQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(currentChar);
            }
        }

        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    private static void insertIntoDatabase(Connection conn, List<SwiftCode> swiftCodes) {
        String sql = "INSERT INTO swift_codes (swift_code, iso2_code, bank_name, address, country_name, is_headquarter) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE bank_name = VALUES(bank_name), is_headquarter = VALUES(is_headquarter), address = VALUES(address), country_name = VALUES(country_name)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (SwiftCode code : swiftCodes) {
                stmt.setString(1, code.getSwiftCode());
                stmt.setString(2, code.getCountryISO2());
                stmt.setString(3, code.getBankName());
                stmt.setString(4, code.getAddress());
                stmt.setString(5, code.getCountryName());
                stmt.setBoolean(6, code.isHeadquarter());
                stmt.executeUpdate();
            }
            LoggerUtil.logInfo("Data successfully inserted into the database.");
        } catch (SQLException e) {
            LoggerUtil.logError("Error inserting data into database: " + e.getMessage());
        }
    }

    private static void createTableIfNotExists(Connection conn) {
        String checkTableQuery = "SHOW TABLES LIKE 'swift_codes'";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkTableQuery);
             ResultSet rs = checkStmt.executeQuery()) {

            if (!rs.next()) {
                String createTableQuery = "CREATE TABLE swift_codes (" +
                                          "swift_code VARCHAR(11) PRIMARY KEY, " +
                                          "iso2_code CHAR(2), " +
                                          "bank_name VARCHAR(255), " +
                                          "address TEXT, " +
                                          "country_name VARCHAR(255), " +
                                          "is_headquarter BOOLEAN)";
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(createTableQuery);
                    LoggerUtil.logInfo("Table 'swift_codes' has been created.");
                }
            } else {
                LoggerUtil.logInfo("Table 'swift_codes' already exists.");
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error checking/creating table: " + e.getMessage());
        }
    }

    public static SwiftCode getSwiftCodeDetails(String swiftCode) {
        String query = "SELECT * FROM swift_codes WHERE swift_code = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/your_database", "username", "password");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, swiftCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String countryISO2 = rs.getString("iso2_code");
                    String bankName = rs.getString("bank_name");
                    String address = rs.getString("address");
                    String countryName = rs.getString("country_name");
                    boolean isHeadquarter = rs.getBoolean("is_headquarter");

                    return new SwiftCode(countryISO2, swiftCode, bankName, address, countryName, isHeadquarter);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error retrieving SWIFT code details: " + e.getMessage());
        }
        return null;
    }

    public static List<SwiftCode> getBranchDetails(String swiftCode) {
        List<SwiftCode> branches = new ArrayList<>();
        String query = "SELECT * FROM swift_codes WHERE swift_code != ? AND country_name = (SELECT country_name FROM swift_codes WHERE swift_code = ?)";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/your_database", "username", "password");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, swiftCode);
            stmt.setString(2, swiftCode);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String countryISO2 = rs.getString("iso2_code");
                    String bankName = rs.getString("bank_name");
                    String address = rs.getString("address");
                    String countryName = rs.getString("country_name");
                    boolean isHeadquarter = rs.getBoolean("is_headquarter");

                    SwiftCode branch = new SwiftCode(countryISO2, swiftCode, bankName, address, countryName, isHeadquarter);
                    branches.add(branch);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error retrieving branch details: " + e.getMessage());
        }
        return branches;
    }
}
