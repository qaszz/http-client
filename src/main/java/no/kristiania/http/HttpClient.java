package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {

    private int statusCode;
    private final Map<String, String> responseHeaders = new HashMap<>();
    private String responseBody;

    // Constructor - det osm kalles når vi sier new
    public HttpClient(final String hostName, int port, final String requestTarget) throws IOException {
        // Connect til serveren
        Socket socket = new Socket(hostName, port);

        // HTTP request consists of request line + 0 or more request headers
        // request line consists of "verb" (GET, POST or PUT) reqyest target ("/echo", "/echo?status=404), protocol (HTTP/1,1)
        String request = "GET " + requestTarget + " HTTP/1.1\r\n" +
                // request header consists of "name: value"
                // header host brukes for å angi hostnavnet i URL
                "Host: " + hostName + "\r\n\r\n";

        // send request to server
        socket.getOutputStream().write(request.getBytes());

        String line = readLine(socket);
        System.out.print(line);

        //The first line in the response is called status line or response line
        // response line consists of protocol ("HTTP/1.1") status code (200, 400, 404, 500 osv) and status message
        String[] responseLineParts = line.split(" ");

        // Status code determines if it went ok (2xx) or not (4xx). (In addition 5xx: server error)
        statusCode = Integer.parseInt(responseLineParts[1]);

        // After status line in the response contains 0 or more response headers
        String headerLine;
        while (!(headerLine = readLine(socket)).isEmpty()) {
            // response header consists of "name: value"
            int colonPos = headerLine.indexOf(':');
            // parse headerr
            String headerName = headerLine.substring(0, colonPos);
            String headerValue = headerLine.substring(colonPos+1).trim();

            // store headers
            responseHeaders.put(headerName, headerValue);
        }

        // Response header content-length tells how many bytes the response body is
        int contentLength = Integer.parseInt(getResponseHeader("Content-Length"));
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            // read content body based on content-length
            body.append((char)socket.getInputStream().read());
        }
        responseBody = body.toString();
    }



    public static String readLine(Socket socket) throws IOException {
        StringBuilder line = new StringBuilder();
        int c;
        while ((c = socket.getInputStream().read()) != -1) {
            // each line ends with r\n ( CRLF - carriage return, line feed)
            if (c == '\r') {
                socket.getInputStream().read(); //read and ignore the following \n
                break;
            };
            line.append((char) c);
        }
        return line.toString();
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