package com.device.translate;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class Translate {
    private Lang lang = new Lang();

    private int statusCode;
    private String line;
    private HttpClient httpClient;
    private HttpGet get;
    private HttpResponse response;
    private String hintLang = "";
    private String whatLang;
    private static String key = "";
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream("src/main/resources/config.properties"));
            key = properties.getProperty("yandex.key");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTranslateText(String text, String lang1, String lang2) {
        String translateText = "";
        httpClient = HttpClients.createDefault();
        System.out.println(lang1);

        String content = "";
        try {
            whatLang = lang.getLang(lang2.substring(0, lang2.length() - 2));
            hintLang = lang.getLang(lang1.substring(0, lang1.length() - 3));
        }catch (Exception e){
            return null;
        }

        try {
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("translate.yandex.net")
                    .setPath("/api/v1.5/tr.json/translate")
                    .setParameter("key", key)
                    .setParameter("lang", hintLang + "-" + whatLang)
                    .setParameter("text", text)
                    .build();

            get = new HttpGet(uri);
            response = httpClient.execute(get);

            statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                while ((line = reader.readLine()) != null) {
                    content += line;
                    System.out.println(content);
                }

                JSONParser parser = new JSONParser();

                Object obj = parser.parse(content);
                JSONObject jsonObj = (JSONObject) obj;
                JSONArray array = (JSONArray) jsonObj.get("text");
                for (Object txt : array) {
                    translateText += txt;
                }

            }

        } catch (IOException | URISyntaxException | ParseException e) {
            e.printStackTrace();
        }


        return translateText;

    }
}
