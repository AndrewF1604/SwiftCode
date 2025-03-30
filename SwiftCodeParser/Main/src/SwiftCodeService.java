import java.sql.*;
import java.util.Optional;

public class SwiftCodeService {

    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;

    public static void loadConfig() {
        ConfigReader configReader = new ConfigReader("config.properties");

        DB_URL = configReader.getProperty("db.url");
        DB_USER = configReader.getProperty("db.user");
        DB_PASSWORD = configReader.getProperty("db.password");

        System.out.println("Loaded DB config: URL=" + DB_URL + ", User=" + DB_USER);
    }

    public static Optional<String> getSwiftCodeData(String swiftCode) {
        loadConfig();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM swift_codes WHERE swift_code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, swiftCode);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(formatSwiftCodeJson(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<String> getSwiftCodesByCountry(String countryISO2) {
        loadConfig();
        StringBuilder jsonResponse = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM swift_codes WHERE iso2_code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, countryISO2);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("No data found for country: " + countryISO2);
                        return Optional.empty();
                    }

                    jsonResponse.append("{\"countryISO2\": \"")
                                .append(countryISO2)
                                .append("\", \"swiftCodes\": [");

                    boolean first = true;
                    do {
                        if (!first) {
                            jsonResponse.append(", ");
                        }
                        jsonResponse.append(formatSwiftCodeJson(rs));
                        first = false;
                    } while (rs.next());

                    jsonResponse.append("]}");

                    return Optional.of(jsonResponse.toString());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static boolean addSwiftCode(String swiftCode, String address, String bankName, String countryISO2, String countryName, boolean isHeadquarter) {
        loadConfig();

        String query = "INSERT INTO swift_codes (swift_code, address, bank_name, iso2_code, country_name, is_headquarter) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, swiftCode);
            stmt.setString(2, address);
            stmt.setString(3, bankName);
            stmt.setString(4, countryISO2);
            stmt.setString(5, countryName);
            stmt.setBoolean(6, isHeadquarter);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteSwiftCode(String swiftCode) {
        loadConfig();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "DELETE FROM swift_codes WHERE swift_code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, swiftCode);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String formatSwiftCodeJson(ResultSet rs) throws SQLException {
        return String.format(
            "{\"address\": \"%s\", \"bankName\": \"%s\", \"countryISO2\": \"%s\", \"countryName\": \"%s\", \"isHeadquarter\": %b, \"swiftCode\": \"%s\"}",
            rs.getString("address"),
            rs.getString("bank_name"),
            rs.getString("iso2_code"),
            rs.getString("country_name"),
            rs.getBoolean("is_headquarter"),
            rs.getString("swift_code")
        );
    }
}
