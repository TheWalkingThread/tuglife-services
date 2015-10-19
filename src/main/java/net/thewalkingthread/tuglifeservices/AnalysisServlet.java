package net.thewalkingthread.tuglifeservices;

import com.google.appengine.labs.repackaged.org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
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
        String siteresponse = "";


        URLConnection conn = new URL(url + "?lv=" + lv).openConnection();
        InputStream stream = conn.getInputStream();

        siteresponse = getStringResonse(stream);
        String session;


        //prepare response
        response.setContentType("application/json");
        JSONObject retval = new JSONObject();
        try {
            retval.append("site", siteresponse);
            retval.append("session", getHiddenFieldData("session", siteresponse));
            response.getWriter().print(retval.toString());
        } catch (Exception e) {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning("Exception thrown during sending response. " + e.getClass().getName() + ": " + e.getMessage());
            response.getWriter().println("{\"status\":-1}");
        }
    }

    private static String getStringResonse(InputStream stream) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    private static String getHiddenFieldData(String name, String content){
        Document site = Jsoup.parse(content);
        Elements inputs = site.
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Use POST!!!!");
    }
}