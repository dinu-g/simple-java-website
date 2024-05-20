import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.InetSocketAddress;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        // Create an HTTP server listening on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port 8000");
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Get the request URI and map it to a file
            String requestURI = exchange.getRequestURI().toString();
            String filePath = "web" + requestURI;
            if (filePath.endsWith("/")) {
                filePath += "index.html"; // Serve index.html if directory is requested
            }

            // Determine the content type based on the file extension
            String contentType = getContentType(filePath);

            // Check if the file exists
            if (Files.exists(Paths.get(filePath))) {
                // Read the file content
                byte[] response = Files.readAllBytes(Paths.get(filePath));
                // Set the appropriate headers and send the response
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            } else {
                // Send 404 response if the file is not found
                String notFoundMessage = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, notFoundMessage.length());
                OutputStream os = exchange.getResponseBody();
                os.write(notFoundMessage.getBytes());
                os.close();
            }
        }

        private String getContentType(String filePath) {
            if (filePath.endsWith(".html")) {
                return "text/html";
            } else if (filePath.endsWith(".css")) {
                return "text/css";
            }
            // Add more content types as needed
            return "application/octet-stream";
        }
    }
}
