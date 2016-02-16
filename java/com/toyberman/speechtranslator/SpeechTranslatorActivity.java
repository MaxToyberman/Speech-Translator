package com.toyberman.speechtranslator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class SpeechTranslatorActivity extends ActionBarActivity {


    private Button pushToTalk, btnChangeLanguage;
    private TextView tv_text;
    private String sourceLang, targetLang;
    private AlertDialog ratingDialog;
    private List<String> languageList, languageCode;
    private Spinner sp_source, sp_target;
    private Locale current = null;
    private Animation anim;
    private SharedPreferences pref;
    private String total = "";
    private TextToSpeech tts;
    private HashMap<String, String> map;
    private List<String> supportedForSpeech;
    private InterstitialAd mInterstitialAd;
    private ProgressDialog pd;

    private class connection extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            InputStream inputStream = null;
            URLConnection con = null;


            //get languages list
            try {
                inputStream = HttpHandler.getUrlData("https://www.googleapis.com/language/translate/v2/languages?key=" + Constants.key + "&target=" + current.toString().substring(0, 2));
            } catch (Exception e) {
                //English is default
                inputStream = HttpHandler.getUrlData("https://www.googleapis.com/language/translate/v2/languages?key=" + Constants.key + "&target=en");
            }


            try {
                total = HttpHandler.getTextFromStream(inputStream);
            } catch (IOException e) {
                Log.d(Constants.Tag, e.getMessage());
            }
            IOUtils.close(inputStream);

            //Creating the list of language names and codes
            setLanguageNamesAndCodes(total);

            return null;
        }
        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(SpeechTranslatorActivity.this);
            pd.setTitle("Processing...");
            pd.setMessage("Please wait.");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();

        }

        @Override
        protected void onPostExecute(Void result) {

            //fill spinners when list is ready
            fillSpinner(R.id.sp_source);
            fillSpinner(R.id.sp_target);
            //getting the list of supported languages to speech
            supportedForSpeech = SpinnerAdapter.getSupportedForSpeech();
            String sLang = loadData("source_language");
            String tLang = loadData("target_language");
            //checking if need to update the spinner according to saved preferences
            if (!sLang.equals("0")) setSpinner(R.id.sp_source, sLang);
            if (!tLang.equals("0")) setSpinner(R.id.sp_target, tLang);

            initTextToSpeech();

            if (pd!=null) {
                pd.dismiss();
            }
            super.onPostExecute(result);
        }
    }

    private void setSpinner(int spinnerId, String text) {

        Spinner spinner = (Spinner) findViewById(spinnerId);
        //Setting the local language of the system in the spinner
        int index = languageCode.indexOf(text);
        spinner.setSelection(index);

    }

    private void setLanguageNamesAndCodes(String total) {
        String langName = "";
        String langCode = "";
        try {
            JSONObject main = new JSONObject(total);
            JSONObject jObject = main.getJSONObject("data");
            JSONArray jArray = jObject.getJSONArray("languages");

            for (int i = 0; i < jArray.length(); i++) {
                //add language Name
                langName = jArray.getJSONObject(i).getString("name");
                languageList.add(langName);
                //add language Code
                langCode = jArray.getJSONObject(i).getString("language");
                languageCode.add(langCode);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_translator);

        //shared preferences for translator
        pref = getSharedPreferences("Translator", Context.MODE_PRIVATE);
        //for InterstitialAd
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1492252415765319/9247653583");
        //requesting ad
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
        //Banner ad
        AdView mAdView = (AdView) findViewById(R.id.adViewHistory);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);

            }
        });
        mAdView.loadAd(adRequest);

        //checking data
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, Constants.MY_DATA_CHECK_CODE);
        //initializing layout
        initLayout();
        //initializing events
        initEvents();

        new connection().execute();

    }

    private void saveData(String key, String data) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, data);
        editor.commit();
    }

    private String loadData(String key) {
        String data = pref.getString(key, "0");
        return data;
    }

    private void initEvents() {

        pushToTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //source language in spinner
                int source_ind = sp_source.getSelectedItemPosition();
                sourceLang = languageCode.get(source_ind);

                //target language in spinner
                int target_ind = sp_target.getSelectedItemPosition();

                targetLang = languageCode.get(target_ind);
                tts.setLanguage(new Locale(languageCode.get(target_ind)));
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                //added substring for languages that require more than 2 char letters for example zh and zh-CHT
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLang);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));

                startActivityForResult(intent, Constants.CODE_SPEECH_INPUT);

            }
        });

        btnChangeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage();

            }
        });

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                showHistory();
            }
        });
    }

    private void requestNewInterstitial() {

        AdRequest adRequest = new AdRequest.Builder()
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    private void initLayout() {
        map = new HashMap<String, String>();
        //find textView
        tv_text = (TextView) findViewById(R.id.tv_text);
        //find the change language button (arrows)
        btnChangeLanguage = (Button) findViewById(R.id.btn_changeLanguage);
        //find the microphone button
        pushToTalk = (Button) findViewById(R.id.btn_pushToTalk);
        //find source language spinner
        sp_source = (Spinner) findViewById(R.id.sp_source);
        //find target language spinner
        sp_target = (Spinner) findViewById(R.id.sp_target);
        //current System locale
        current = getResources().getConfiguration().locale;
        languageList = new ArrayList<>();
        languageCode = new ArrayList<>();
        anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
    }

    public void fillSpinner(int sp_id) {
        //find the appropriate spinner
        Spinner spinner = (Spinner) findViewById(sp_id);
        //cutsom adapter
        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.row, languageList.toArray(new String[languageList.size()]));
        //setting the adapter
        spinner.setAdapter(adapter);
        //Setting the local language of the system in the spinner
        int defaultLanguagePosition = languageCode.indexOf(current.toString().substring(0, 2));
        spinner.setSelection(defaultLanguagePosition);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && data != null) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String txt = (result.get(0));
                    tv_text.setText(txt);

                    class Translator extends AsyncTask<String, Void, Void> {

                        String translatedText = "";

                        @Override
                        protected Void doInBackground(String... params) {
                            String text_to_translate = "";
                            try {
                                text_to_translate = params[0];
                                String translated = "";
                                if (!sourceLang.equals(targetLang)) {
                                    translated = GoogleTranslate.getTranslatedText(text_to_translate, Constants.key, sourceLang, targetLang);
                                } else {
                                    translated = text_to_translate;
                                }
                                translatedText = translated;

                            } catch (Exception e) {
                                Log.d(Constants.Tag,e.getMessage());
                                translatedText = e.getMessage();

                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            ((TextView) findViewById(R.id.tv_text)).setText(translatedText);

                            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
                            while (tts.isSpeaking());

                                if (supportedForSpeech.contains(new Locale(targetLang).getDisplayName())) {
                                    tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, map);

                                }
                                else {
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.language_not_supported, Toast.LENGTH_LONG);
                                    toast.show();
                                    //here
                                }

                            IOUtils.writeHistoryToInternalStorage(translatedText, getApplicationContext());
                            super.onPostExecute(result);
                        }

                    }
                    new Translator().execute(txt);
                }
                break;

            case Constants.MY_DATA_CHECK_CODE:
                if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    // missing data, install it
                    Intent installIntent = new Intent();
                    installIntent.setAction(
                            TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                }
                break;

        }
    }

    private void changeLanguage() {
        /*
         * This function is called when user clicks on the change language button.
         *
         * */
        int source, target, temp;

        source = sp_source.getSelectedItemPosition();
        ;
        target = sp_target.getSelectedItemPosition();

        temp = source;
        source = target;
        target = temp;

        //changing languages
        sp_source.setSelection(source);
        sp_target.setSelection(target);

        try {
            btnChangeLanguage.startAnimation(anim);
        } catch (Exception e) {
            Log.d(Constants.Tag, e.getMessage());
        }
        targetLang = languageCode.get(target);
        tts.setLanguage(new Locale(targetLang));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_speech_translator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_rate:
                displayRatingDialog();
                break;

            case R.id.action_history:
                //check if ad os loaded
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    showHistory();
                }
                break;
            case R.id.action_play:
                if (!tts.isSpeaking()) {
                    tts.speak(tv_text.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);

                }
                break;
        }
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showHistory() {
        Intent history_intent = new Intent(this, HistoryActivity.class);
        startActivity(history_intent);
    }

    private void displayRatingDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View ratingView = getLayoutInflater().inflate(R.layout.rating, null, false);
        initDialogEvents(ratingView);
        builder.setView(ratingView);
        builder.setCancelable(false);
        ratingDialog = builder.show();
    }

    private void email(String remarks, float rating) {

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setType(HTTP.PLAIN_TEXT_TYPE);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"maxtoyberman@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.rating));
        emailIntent.putExtra(Intent.EXTRA_TEXT, remarks + " " + rating);
        startActivity(Intent.createChooser(emailIntent,getString(R.string.send_feedback)));
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (pd!=null) {
            pd.dismiss();
        }
        super.onDestroy();

        saveData("source_language", languageCode.get(sp_source.getSelectedItemPosition()));
        saveData("target_language", languageCode.get(sp_target.getSelectedItemPosition()));
    }

    public void initTextToSpeech() {
        try {
            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {


                    if (status != TextToSpeech.ERROR) {
                        int target_ind = sp_target.getSelectedItemPosition();
                        targetLang = languageCode.get(target_ind).toString();

                        tts.setLanguage(new Locale(targetLang));
                        tts.speak("", TextToSpeech.QUEUE_FLUSH, null);

                    }
                }
            });
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {

                }

                @Override
                public void onDone(String utteranceId) {

                }

                @Override
                public void onError(String utteranceId) {
                    Toast.makeText(getApplicationContext(), "an error occured,maybe the language is not supported yet!", Toast.LENGTH_LONG).show();

                }
            });
        } catch (Exception e) {
            Log.d(Constants.Tag, e.getMessage());
        }

    }

    private void initDialogEvents(final View ratingView) {
        final EditText etRemarks = (EditText) ratingView.findViewById(R.id.editText);

        RatingBar ratingBar = (RatingBar) ratingView.findViewById(R.id.rb_rating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                String remarks = etRemarks.getText().toString();

                ratingDialog.dismiss();
                email(remarks, rating);

            }
        });

    }


}
