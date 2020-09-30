package no.kristiania.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HttpServer {

    private File contentRoot;
    private List<String> productNames = new ArrayList<>();

    public HttpServer(int port) throws IOException{
        // Opens an entry point to our program for network clients
        ServerSocket serverSocket = new ServerSocket(port);

        // new Thread executes the code in a separate "thread", that is: In parallel
        new Thread(() ->{ // anonymous function with code that will be executed in parallel
            while (true) {
                try { //accept waits for a client to try to connect - blocks
                    handleRequest(serverSocket.accept());
                } catch (IOException e) {
                    // If something went wrong - print out exception and try again
                    e.printStackTrace();
                }
            }
        }).start(); // Start the threads, so the code inside executes without blocking the current thread

    }

    // This code will be executed for each client
    private void handleRequest(Socket clientSocket) throws IOException {
        String requestLine = HttpMessage.readLine(clientSocket);
        System.out.println(requestLine);
        // Example GET /index.html HTTP/1.1

        String requestTarget = requestLine.split(" ")[1];
        // Example GET /echo?body=hello HTTP/1.1
        String statusCode = "200";
        String body = "Hello <strong>World</strong>!";



        int questionPos = requestTarget.indexOf('?');

        String requestPath;
        if (questionPos != -1) {
            requestPath = requestTarget.substring(0, questionPos);
        } else {
            requestPath = requestTarget;
        }

        if (questionPos != -1) {
            // body = helloo
            QueryString queryString = new QueryString(requestTarget.substring(questionPos+1));
            if (queryString.getParameter("body") != null){
                body = queryString.getParameter("body");
            }
            if (queryString.getParameter("status") != null){
                statusCode = queryString.getParameter("status");
            }

        } else if (!requestPath.equals("/echo")){
            File file = new File(contentRoot, requestPath);
            if (!file.exists()) {
                body = file + " does not exist";
                String response = "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: " + body.length() + "\r\n" +
                        "\r\n" +
                        body;

                // Write the response back to the client
                clientSocket.getOutputStream().write(response.getBytes());
                return;
            }

            statusCode = "200";
            String contentType = "text/plain";
            if (file.getName().endsWith(".html")){
                contentType = "text/html";
            }
            String response = "HTTP/1.1 " + statusCode + " OK\r\n" +
                    "Content-Length: " + file.length() + "\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "\r\n";

            // Write the response back to the client
            clientSocket.getOutputStream().write(response.getBytes());

            new FileInputStream(file).transferTo(clientSocket.getOutputStream());
            return;
        }

        String response = "HTTP/1.1 " + statusCode + " OK\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                body;

        // Write the response back to the client
        clientSocket.getOutputStream().write(response.getBytes());
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer(8080);
        server.setContentRoot(new File("src/main/resources"));
    }

    public void setContentRoot(File contentRoot) {
        this.contentRoot = contentRoot;
    }

    public List<String> getProductNames() {
        return productNames;
    }
}
