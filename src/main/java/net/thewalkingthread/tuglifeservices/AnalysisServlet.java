package net.thewalkingthread.tuglifeservices;

import com.google.appengine.labs.repackaged.org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;

public class AnalysisServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String lv = "501446w15";
        String url = "https://www.math.tugraz.at/onlinekreuze/onlinekreuze.phtml";

        HttpURLConnection conn = (HttpURLConnection) new URL(url + "?lv=" + lv).openConnection();

        String siteresponse = getStringResonse(conn);
        String session = getHiddenFieldData("session", siteresponse);
        String matnum = request.getParameter("text").split(" ")[0];
        String password = request.getParameter("text").split(" ")[1];

        conn = (HttpURLConnection) new URL(url + "?lv=" + lv).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        String body = "username_plain=" + URLEncoder.encode(matnum, "UTF-8") +
                        "&password_plain=" + URLEncoder.encode(password, "UTF-8") +
                        "&session=" + URLEncoder.encode(session, "UTF-8") +
                        "&iv=&lv=" + URLEncoder.encode(lv, "UTF-8") +
                        "&admin=false&inactive=off&matrikelnummer=&submit=Einloggen";

        try {
            sendRequest(conn, body);
        } catch (Exception e) {
            e.printStackTrace();
        }

        siteresponse = getStringResonse(conn);
        String username = getHiddenFieldData("username", siteresponse);
        password = getHiddenFieldData("password", siteresponse);
        String iv = getHiddenFieldData("iv", siteresponse);

        conn = (HttpURLConnection) new URL(url + "?lv=" + lv).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        body = "action=showstand&username=" + URLEncoder.encode(username, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&session=" + URLEncoder.encode(session, "UTF-8") +
                "&iv=" + URLEncoder.encode(iv, "UTF-8") +
                "&lv=" + URLEncoder.encode(lv, "UTF-8") +
                "&admin=false&inactive=off&matrikelnummer=" + matnum +
                "&submit=Stand+anzeigen";

        try {
            sendRequest(conn, body);
        } catch (Exception e) {
            e.printStackTrace();
        }

        siteresponse = getStringResonse(conn);

        //prepare response
        response.setContentType("application/json");
        JSONObject retval = new JSONObject();
        try {
            retval.put("points", getFromTable("P", siteresponse));
            retval.put("kreuze", getFromTable("Kreuze", siteresponse));
            retval.put("kreuze_max", getFromTable("Max Kreuze", siteresponse));
            retval.put("note", getFromTable("DerzeitigeNote", siteresponse));
            response.getWriter().print(retval.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning("Exception thrown during sending response. " + e.getClass().getName() + ": " + e.getMessage());
            response.getWriter().println("{\"status\":-1}");
        }
    }

    private static void sendRequest(HttpURLConnection connection, String body) throws Exception{
        OutputStream stream = new DataOutputStream(connection.getOutputStream());
        stream.write(body.getBytes());
        stream.flush();
        stream.close();

    }

    private static String getStringResonse(HttpURLConnection connection) throws IOException{
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

    private static String getHiddenFieldData(String name, String content){
        Document site = Jsoup.parse(content);
        Elements inputs = site.getElementsByTag("input");
        for (Element tag : inputs){
            if (tag.attr("name").equals(name)){
                return tag.attr("value");
            }
        }
        return "NF";
    }

    private static String getFromTable(String data, String content){
        Document site = Jsoup.parse(content);
        Elements rows = site.getElementsByTag("tr");
        for (Element row: rows){
            Element header = row.getElementsByTag("th").first();
            if (header != null && header.text().equals(data)){
                return row.getElementsByTag("td").first().text();
            }
        }
        return "Not found";
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Use POST!!!!");
    }
}
