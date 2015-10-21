package net.thewalkingthread.tuglifeservices;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPHandler {
    public static void sendRequest(HttpURLConnection connection, String body) throws IOException {
        OutputStream stream = new DataOutputStream(connection.getOutputStream());
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    public static String getStringResonse(HttpURLConnection connection) throws IOException{
        InputStream stream = new DataInputStream(connection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        stream.close();
        return builder.toString();
    }

    public static HttpURLConnection getPOSTConnection(URL url) throws IOException{
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        return conn;
    }
}
