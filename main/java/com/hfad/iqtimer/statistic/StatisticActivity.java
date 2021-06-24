 package com.hfad.iqtimer.statistic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.hfad.iqtimer.R;


public class StatisticActivity extends AppCompatActivity  {
    private static final String TAG = "MYLOGS";

    StatisticFragment fragStat;
    FragmentTransaction ft;
    StatisticListDaysFragment fragStatList;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);
        Log.d(TAG, "StatisticActivity: onCreate");

        if (savedInstanceState == null) {
            Log.d(TAG, "StatisticActivity: SupportFragmentManager");
            //добавляем фрагмент для отображения в контейнере
            fragStat = new StatisticFragment();

            ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.stat_container, fragStat);//1 - в каком контейнере, 2 - какой фрагмент добавить
            ft.commit();
        }

        //создаем стрелку НАЗАД
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.stat_actionbar_title);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "StatisticActivity: onSaveInstanceState");
        savedInstanceState.putBoolean("fragStat",true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
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
            if(fragStatList==null){
            fragStatList = new StatisticListDaysFragment();}

            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.stat_container, fragStatList);//1 - в каком контейнере, 2 - какой фрагмент добавить
            ft.commit();

            //скрываем кнопку Список и показываем График
            hideOption(R.id.stat_list);
            showOption(R.id.stat_graph);
        }

        //обработка кнопки График
        if (id == R.id.stat_graph) {
            if(fragStat==null){
                fragStat = new StatisticFragment();}

                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.stat_container, fragStat);//1 - в каком контейнере, 2 - какой фрагмент добавить
                ft.commit();

            //скрываем кнопку График и показываем Список
            hideOption(R.id.stat_graph);
            showOption(R.id.stat_list);
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideOption(int id)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }

   }

