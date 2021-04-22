package com.hfad.iqtimer.statistic;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.hfad.iqtimer.R;

public class StatisticListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_statistic);

    //создаем стрелку НАЗАД
    ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.stat_actionbar_title);
    }

}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stat_menu, menu);
        return true;

    }

    @Override
    //обрабатываем стрелку НАЗАД
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //обработка кнопки назад
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        //обработка кнопки Списка
        if (id == R.id.stat_list) {
            Intent i = new Intent(this,StatisticActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }
}
