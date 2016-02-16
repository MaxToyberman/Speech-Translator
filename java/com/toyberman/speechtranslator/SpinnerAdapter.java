package com.toyberman.speechtranslator;


import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Toyberman Maxim on 01-Aug-15.
 */
public class SpinnerAdapter extends ArrayAdapter<String> {

    private Context ctx;
    private String[] contentArray;

    private static List<String> supportedForSpeech;
    private TextToSpeech tts;


    public static List<String> getSupportedForSpeech() {
        return supportedForSpeech;
    }



    public SpinnerAdapter(Context context, int resource, String[] languages) {
        super(context, R.layout.row, R.id.spinnerTextView, languages);
        this.ctx = context;
        this.contentArray = languages;

        supportedForSpeech = new ArrayList<String>();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                Locale loc;


                if (status != TextToSpeech.ERROR) {
                    tts.speak("", TextToSpeech.QUEUE_FLUSH, null);

                    Locale[] locales = Locale.getAvailableLocales();

                    for (Locale locale : locales) {
                        try {
                            if (tts.isLanguageAvailable(locale) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                                supportedForSpeech.add(locale.getDisplayLanguage());

                            }
                        }
                        catch (Exception e){

                        }
                        
                    }

                }
            }
        });



    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        //Log.d(Constants.Tag,String.valueOf(supportedLanguages.size()));
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.row, parent, false);

        TextView textView = (TextView) row.findViewById(R.id.spinnerTextView);
        textView.setText(contentArray[position]);

        ImageView imageView = (ImageView) row.findViewById(R.id.spinnerSoundImage);


        if (supportedForSpeech.contains(contentArray[position]) || contentArray[position].equals("Chinese (Simplified)")) {

            imageView.setImageResource(R.drawable.ic_sound);
        }

        return row;

    }
}