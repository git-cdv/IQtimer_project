package com.chkan.iqtimer.settings;



import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.chkan.iqtimer.R;
import com.chkan.iqtimer.dialogs.DialogLicense;
import com.chkan.iqtimer.dialogs.DialogOnLock;
import com.chkan.iqtimer.progress.DialogFragmentGoal;

public class AboutActivity extends AppCompatActivity {

    ListView lvAbout;
    DialogLicense dlgLicense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        lvAbout = (ListView) findViewById(R.id.list_about);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.about, android.R.layout.simple_list_item_1);
        lvAbout.setAdapter(adapter);

        lvAbout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
               if (position==1){
                   dlgLicense = new DialogLicense();
                   dlgLicense.show(getSupportFragmentManager(), "dlgLicense");
               }
            }
        });

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

}
