package com.toyberman.speechtranslator;


import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import java.util.List;


public class HistoryActivity extends ActionBarActivity {

    private ListView lv_history;
    private List<String> values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        //Banner ad
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView mAdView = (AdView) findViewById(R.id.adViewHistory);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);

            }
        });
        mAdView.loadAd(adRequest);

        lv_history = (ListView) findViewById(R.id.lv_history);
        //getting the file names in the directory
        values = IOUtils.getFilesNameInCurrentDirectory(getApplicationContext());
        //adapter for the files name,to fit in the listview
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        // Assign adapter to ListView
        lv_history.setAdapter(adapter);
        initEvents(adapter);
    }

    private void initEvents(final ArrayAdapter<String> adapter) {
        lv_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent translation_intent = new Intent(getApplicationContext(), Translation.class);
                String filename = values.get(position);

                translation_intent.putExtra("fileName", filename);
                startActivity(translation_intent);
            }
        });
        lv_history.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                values.remove(position);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.action_removeAll:
                IOUtils.deleteAllFiles(this);
                lv_history.setAdapter(null);
                break;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
