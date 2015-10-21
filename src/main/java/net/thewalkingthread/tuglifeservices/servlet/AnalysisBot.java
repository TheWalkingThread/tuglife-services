package net.thewalkingthread.tuglifeservices.servlet;

import net.thewalkingthread.tuglifeservices.DataStoreHandler;
import net.thewalkingthread.tuglifeservices.HTTPHandler;
import net.thewalkingthread.tuglifeservices.math.DataExtractor;
import net.thewalkingthread.tuglifeservices.payload.MathUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;


public class AnalysisBot extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String lv = "501446w15";
        String usertype = "analysisuser";
        String urlstring = "https://www.math.tugraz.at/onlinekreuze/onlinekreuze.phtml";

        String slackname = request.getParameter("user_name");
        if (slackname == null || slackname.equals("")){
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning("Non-slack client access");
            response.setStatus(401);
            response.getWriter().print("This service is only usable from slack");
            return;
        }
        MathUser user;
        String siteresponse, session, password, matnum;
        String slackparams = request.getParameter("text");


        if ((user = DataStoreHandler.getMathUser(slackname, usertype)) == null){
            //user doesn't exist
            //param[0] = matnum, param[1] = password
            if (slackparams == null || slackparams.equals("")){
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info("No data for "+slackname+" found");
                response.getWriter().print("No password found");
                return;
            }
            String[] param = slackparams.split(" ");
            if (param[0] == null || param[1] == null || param[0].equals("") || param[1].equals("")){
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info("No data for "+slackname+" found");
                response.getWriter().print("No password found");
                return;
            }
            user = new MathUser(slackname, param[0], param[1]);
            DataStoreHandler.createMathUser(user, usertype);
        } else if (slackparams != null && !slackparams.equals("")){
            //user in datastore, new password provided
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info("Data changed for "+slackname);
            String[] param = slackparams.split(" ");
            //param[0] = matnum, param[1] = password
            if (param[0] != null && param[1] != null && !param[0].equals("") && !param[1].equals("")){
                user.matnum = param[0];
                user.pw = param[1];
                DataStoreHandler.updateMathUser(user, usertype);
            }
        }

        password = user.pw;
        matnum = user.matnum;

        URL url = new URL(urlstring + "?lv=" + lv);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        siteresponse = HTTPHandler.getStringResonse(conn);
        session = DataExtractor.getHiddenFieldData("session", siteresponse);


        conn = HTTPHandler.getPOSTConnection(url);

        String body = "username_plain=" + URLEncoder.encode(matnum, "UTF-8") +
                        "&password_plain=" + URLEncoder.encode(password, "UTF-8") +
                        "&session=" + URLEncoder.encode(session, "UTF-8") +
                        "&iv=&lv=" + URLEncoder.encode(lv, "UTF-8") +
                        "&admin=false&inactive=off&matrikelnummer=&submit=Einloggen";

        HTTPHandler.sendRequest(conn, body);

        siteresponse = HTTPHandler.getStringResonse(conn);
        String username = DataExtractor.getHiddenFieldData("username", siteresponse);
        password = DataExtractor.getHiddenFieldData("password", siteresponse);
        String iv = DataExtractor.getHiddenFieldData("iv", siteresponse);

        conn = HTTPHandler.getPOSTConnection(url);

        body = "action=showstand&username=" + URLEncoder.encode(username, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&session=" + URLEncoder.encode(session, "UTF-8") +
                "&iv=" + URLEncoder.encode(iv, "UTF-8") +
                "&lv=" + URLEncoder.encode(lv, "UTF-8") +
                "&admin=false&inactive=off&matrikelnummer=" + matnum +
                "&submit=Stand+anzeigen";

        HTTPHandler.sendRequest(conn, body);

        siteresponse = HTTPHandler.getStringResonse(conn);

        //prepare response
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info("User " +slackname+ " got analysis points");
        response.getWriter().print("You have "+DataExtractor.getFromTable("P", siteresponse)+" points");
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.setStatus(405);
        resp.getWriter().println("Use POST!!!!");
    }
}
