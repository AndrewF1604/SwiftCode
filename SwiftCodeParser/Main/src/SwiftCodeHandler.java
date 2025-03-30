import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SwiftCodeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] segments = path.split("/");

        System.out.println("Received request: " + path);
        System.out.println("Path segments: " + String.join(", ", segments));

        if ("GET".equalsIgnoreCase(method)) {
            handleGetRequest(exchange, segments);
        } else if ("POST".equalsIgnoreCase(method)) {
            handlePostRequest(exchange);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            handleDeleteRequest(exchange, segments);
        } else {
            sendResponse(exchange, "{\"error\": \"Method not allowed\"}", 405);
        }
    }

    private void handleGetRequest(HttpExchange exchange, String[] segments) throws IOException {
        if (segments.length == 4) {
            String lastSegment = segments[3];

            if (lastSegment.length() == 2) {
                handleCountryRequest(exchange, lastSegment);
            } else {
                handleSwiftCodeRequest(exchange, lastSegment);
            }
        } else {
            sendResponse(exchange, "{\"error\": \"Invalid request. Provide a country ISO2 code or SWIFT code\"}", 400);
        }
    }

    private void handleCountryRequest(HttpExchange exchange, String countryISO2) throws IOException {
        SwiftCodeService.loadConfig();
        Optional<String> jsonResponse = SwiftCodeService.getSwiftCodesByCountry(countryISO2);

        if (jsonResponse.isPresent()) {
            sendResponse(exchange, jsonResponse.get(), 200);
        } else {
            sendResponse(exchange, "{\"error\": \"No SWIFT codes found for the country\"}", 404);
        }
    }

    private void handleSwiftCodeRequest(HttpExchange exchange, String swiftCode) throws IOException {
        SwiftCodeService.loadConfig();
        Optional<String> jsonResponse = SwiftCodeService.getSwiftCodeData(swiftCode);

        if (jsonResponse.isPresent()) {
            sendResponse(exchange, jsonResponse.get(), 200);
        } else {
            sendResponse(exchange, "{\"error\": \"SWIFT code not found\"}", 404);
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        String jsonRequest = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        String swiftCode = extractJsonValue(jsonRequest, "swiftCode");
        String address = extractJsonValue(jsonRequest, "address");
        String bankName = extractJsonValue(jsonRequest, "bankName");
        String countryISO2 = extractJsonValue(jsonRequest, "countryISO2");
        String countryName = extractJsonValue(jsonRequest, "countryName");
        boolean isHeadquarter = Boolean.parseBoolean(extractJsonValue(jsonRequest, "isHeadquarter"));

        boolean success = SwiftCodeService.addSwiftCode(swiftCode, address, bankName, countryISO2, countryName, isHeadquarter);

        String response = success ? "{\"message\": \"SWIFT code successfully added\"}" : "{\"error\": \"Failed to add SWIFT code\"}";
        sendResponse(exchange, response, success ? 200 : 500);
    }

    private void handleDeleteRequest(HttpExchange exchange, String[] segments) throws IOException {
        if (segments.length == 4) {
            String swiftCode = segments[3];
            SwiftCodeService.loadConfig();
            boolean success = SwiftCodeService.deleteSwiftCode(swiftCode);

            if (success) {
                sendResponse(exchange, "{\"message\": \"SWIFT code deleted successfully\"}", 200);
            } else {
                sendResponse(exchange, "{\"error\": \"SWIFT code not found\"}", 404);
            }
        } else {
            sendResponse(exchange, "{\"error\": \"Invalid request. Provide a valid SWIFT code\"}", 400);
        }
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
