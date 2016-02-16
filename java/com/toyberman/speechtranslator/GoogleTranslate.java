package com.toyberman.speechtranslator;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 * Created by Toyberman Maxim on 26-Jul-15.
 */
public class GoogleTranslate {

    public static String getTranslatedText(String text_to_translate,String key,String sourceLang,String targetLang) throws IOException {
        /**
         * get's translation from google translate api
         */
        String text = null;
        InputStream inputStream= HttpHandler.getUrlData("https://www.googleapis.com/language/translate/v2?key=" +key + "&source=" + sourceLang + "&target=" + targetLang + "&q=" + URLEncoder.encode(text_to_translate, "UTF-8"));
        try {
             text = HttpHandler.getTextFromStream(inputStream);
        }
        catch (Exception e){
            Log.d("CHECK",e.getMessage());
        }
            IOUtils.close(inputStream);

        String translated="";
        try {
            //getting translations from json
            JSONObject main=new JSONObject(text);
            JSONObject jObject=main.getJSONObject("data");
            JSONArray jArray = jObject.getJSONArray("translations");

            translated = jArray.getJSONObject(0).getString("translatedText");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return translated;
    }

}
