package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {

    private int statusCode;
    private Map<String, String> responseHeaders = new HashMap<>();
    private String responseBody;

    // Constructor - det osm kalles når vi sier new
    public HttpClient(final String hostName, int port, final String requestTarget) throws IOException {
        this(hostName, port, requestTarget, "GET", null);
    }

    // Constructor - det osm kalles når vi sier new
    public HttpClient(final String hostName, int port, final String requestTarget, final String httpMethod, String requestBody) throws IOException {
        // Connect til serveren
        Socket socket = new Socket(hostName, port);

        String contentLengthHeader = requestBody != null ? "Content-Length: " + requestBody.length() + "\r\n": "";
        // HTTP request consists of request line + 0 or more request headers
        // request line consists of "verb" (GET, POST or PUT) reqyest target ("/echo", "/echo?status=404), protocol (HTTP/1,1)
        String request = httpMethod + " " + requestTarget + " HTTP/1.1\r\n" +
                // request header consists of "name: value"
                // header host brukes for å angi hostnavnet i URL
                "Host: " + hostName + "\r\n" +
                contentLengthHeader + "\r\n";

        // send request to server
        socket.getOutputStream().write(request.getBytes());

        if (requestBody != null) {
            socket.getOutputStream().write(requestBody.getBytes());
        }


        HttpMessage response = new HttpMessage(socket);
        //The start line in the response is called status line or response line
        // response line consists of protocol ("HTTP/1.1") status code (200, 400, 404, 500 osv) and status message
        String responseLine = response.getStartLine();
        responseHeaders = response.getHeaders();
        responseBody = response.getBody();

        String[] responseLineParts = responseLine.split(" ");

        // Status code determines if it went ok (2xx) or not (4xx). (In addition 5xx: server error)
        statusCode = Integer.parseInt(responseLineParts[1]);
    }


    public static void main(String[] args) throws IOException {
        HttpClient client = new HttpClient("urlecho.appspot.com", 80, "/echo?status=200&Content-Type=text%2Fhtml&body=Hei%20Kristiania");
        System.out.println(client.getResponseBody());
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseHeader(String headerName) {
        return responseHeaders.get(headerName);
    }

    public String getResponseBody() {
        return responseBody;
    }
}