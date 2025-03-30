import java.sql.*;

public class DatabaseIndexer {
    public static void addIndexesToDatabase(Connection conn) {
        createIndexIfNotExists(conn, "idx_swift_code", "swift_codes", "swift_code");
        createIndexIfNotExists(conn, "idx_iso2_code", "swift_codes", "iso2_code");
    }

    private static void createIndexIfNotExists(Connection conn, String indexName, String tableName, String columnName) {
        String checkIndexQuery = "SHOW INDEX FROM " + tableName + " WHERE Key_name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkIndexQuery)) {
            checkStmt.setString(1, indexName);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    String createIndexQuery = "CREATE INDEX " + indexName + " ON " + tableName + "(" + columnName + ")";
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate(createIndexQuery);
                        LoggerUtil.logInfo("Index " + indexName + " has been created.");
                    }
                } else {
                    LoggerUtil.logInfo("Index " + indexName + " already exists.");
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error creating index " + indexName + ": " + e.getMessage());
        }
    }
}
