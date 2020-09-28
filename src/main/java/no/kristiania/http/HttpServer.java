package no.kristiania.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    public HttpServer(int port) throws IOException{
        // Opens an entry point to our program for network clients
        ServerSocket serverSocket = new ServerSocket(port);

        // new Thread executes the code in a separate "thread", that is: In parallel
        new Thread(() ->{ // anonymous function with code that will be executed in parallell
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
        String requestLine = HttpClient.readLine(clientSocket);
        System.out.println(requestLine);
        // Example GET /index.html HTTP/1.1

        String requestTarget = requestLine.split(" ")[1];
        // Example GET /echo?body=hello HTTP/1.1
        String statusCode = "200";

        int questionPos = requestTarget.indexOf('?');
        if (questionPos != -1) {
            // body = hello
            QueryString queryString = new QueryString(requestTarget.substring(questionPos+1));

            statusCode = queryString.getParameter("status");
        }

        String response = "HTTP/1.1 " + statusCode + " OK\r\n" +
                "Content-Length: 29\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Hello <strong>World</strong>!";

        // Write the response back to the client
        clientSocket.getOutputStream().write(response.getBytes());
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(8080);
    }
}
