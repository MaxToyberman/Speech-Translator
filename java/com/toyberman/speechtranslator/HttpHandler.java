package com.toyberman.speechtranslator;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Toyberman Maxim on 22-Jul-15.
 */
public class HttpHandler {
    /*
        This class handles http connections

     */

    public static InputStream getUrlData(String address){
        InputStream inputStream=null;
        try {
            //obtaining languages for spinners
            URL url=new URL(address);
            inputStream =url.openStream();

        } catch (MalformedURLException e) {
           Log.d("CHECK",e.getMessage());
        } catch (IOException e) {
            Log.d("CHECK",e.getMessage());
        }
        catch (Exception e){
            Log.d("CHECK",e.getMessage());
        }

        return inputStream;
    }

    public static  String getTextFromStream(InputStream inputstream)throws IOException{

        StringBuilder total = new StringBuilder();
        String line="";

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));

            while ((line = reader.readLine()) != null) {
                total.append(line);
            }

        return total.toString();
    }
}
