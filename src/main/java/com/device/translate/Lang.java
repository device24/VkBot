package com.device.translate;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


public class Lang {
    private int statusCode;
    private String line;
    private HttpClient httpClient;
    private HttpGet get;
    private HttpResponse response;
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

    public String hintLang(String text) {
        String content = "";
        String hintLang = null;
        httpClient = HttpClients.createDefault();

        try {
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("translate.yandex.net")
                    .setPath("/api/v1.5/tr.json/detect")
                    .setParameter("key", key)
                    .setParameter("text", text)
                    .build();

            get = new HttpGet(uri);
            response = httpClient.execute(get);

            statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                while ((line = reader.readLine())!=null){
                    content+=line;
                }

                JSONParser parser = new JSONParser();

                Object obj = parser.parse(content);
                JSONObject jsonObj = (JSONObject) obj;
                hintLang = (String) jsonObj.get("lang");


            }

        }catch(IOException | URISyntaxException | ParseException e){
                e.printStackTrace();
        }
        return hintLang;

    }

    public String getLang(String lang){
        String content = "";
        String hintLang = null;
        httpClient = HttpClients.createDefault();
        try {
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("translate.yandex.net")
                    .setPath("/api/v1.5/tr.json/getLangs")
                    .setParameter("key", key)
                    .setParameter("ui", "ru")
                    .build();

            get = new HttpGet(uri);
            response = httpClient.execute(get);

            statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                while ((line = reader.readLine())!=null){
                    content+=line;
                }

                JSONParser parser = new JSONParser();

                Object obj = parser.parse(content);
                JSONObject jsonObj = (JSONObject) obj;
                JSONObject object = (JSONObject) jsonObj.get("langs");
                Set<Map.Entry<String,String>> entrySet=object.entrySet();


                for (Map.Entry<String,String> pair : entrySet) {
                    if (lang.startsWith(pair.getValue().toLowerCase().substring(0, pair.getValue().toLowerCase().length()-2))) {
                        hintLang = pair.getKey();
                    }
                }



            }

        }catch(IOException | URISyntaxException | ParseException e){
            e.printStackTrace();
        }


        return hintLang;
    }
}
