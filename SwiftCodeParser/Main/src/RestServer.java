import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class RestServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/v1/swift-codes", new SwiftCodeHandler());
        server.start();
        System.out.println("Server started on port 8080");
    }
}
