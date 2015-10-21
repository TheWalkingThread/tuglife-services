package net.thewalkingthread.tuglifeservices.math;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DataExtractor {

    public static String getHiddenFieldData(String name, String content){
        Document site = Jsoup.parse(content);
        Elements inputs = site.getElementsByTag("input");
        for (Element tag : inputs){
            if (tag.attr("name").equals(name)){
                return tag.attr("value");
            }
        }
        return "NF";
    }

    public static String getFromTable(String data, String content){
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
}
