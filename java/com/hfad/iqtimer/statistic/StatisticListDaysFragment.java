package com.hfad.iqtimer.statistic;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.hfad.iqtimer.R;
import com.hfad.iqtimer.database.App;
import com.hfad.iqtimer.database.AppDatabase;
import com.hfad.iqtimer.database.SessionDao;
import com.hfad.iqtimer.database.SessionDatabaseHelper;

public class StatisticListDaysFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MYLOGS";

    SimpleCursorAdapter listAdapter;
    static Cursor sCursor;
    AppDatabase db;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stat_list_days, null);

        // создаем лоадер для чтения данных (работает только с фрагментами)
        LoaderManager.getInstance(this).initLoader(0, null, this);

        // формируем столбцы сопоставления
        String[] from = new String[]{"date_full", "count"};
        int[] to = new int[]{R.id.stat_date, R.id.stat_count};

        listAdapter = new SimpleCursorAdapter(getContext(),
                R.layout.stat_view_item,//Как должны выводиться данные.
                null,//в варианте с лоадером - курсор передает лоадре
                from,//Вывести содержимое столбца в надписях внутри компонента ListView
                to,//куда сопоставить и вывести
                0);
        //получаем ссылку на списковое представление
        ListView listStat = (ListView) v.findViewById(R.id.list_stat);
        //текст при путом списке
        TextView emptyText = (TextView)v.findViewById(R.id.empty_text_list);
        emptyText.setVisibility(View.VISIBLE);
        listStat.setEmptyView(emptyText);
        listStat.setAdapter(listAdapter);

        return v;
    }

    @NonNull
    @Override
    //создаем Loader и даем ему на вход объект для работы с БД
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(TAG, "StatisticListDaysFragment: onCreateLoader");
        //получаем ссылку на БД
        db = App.getInstance().getDatabase();
        return new MyCursorLoader(getContext(), db);
    }

    @Override
    //мы получаем результат работы лоадера – новый курсор с данными. Этот курсор мы отдаем адаптеру методом swapCursor.
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "StatisticListDaysFragment: onLoadFinished");
        //передаем адаптеру для отображения в листе
        listAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "StatisticListDaysFragment: onLoadReset");

    }

    //наш лоадер, наследник класса CursorLoader. У него мы переопределяем метод loadInBackground, в котором просто получаем курсор с данными БД
    static class MyCursorLoader extends CursorLoader {
        AppDatabase db;

        public MyCursorLoader(Context context, AppDatabase db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(TAG, "StatisticListDaysFragment: loadInBackground");
            //получаем Dao для операций с БД
            SessionDao sessionDao = db.sessionDao();
            //Курсор возвращает значения "id", "DATE","SESSION_COUNT" каждой записи в таблице SESSIONS
            sCursor = sessionDao.getListCursor();
            return sCursor;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sCursor.close();
        if(db!=null){db.close();}
    }
}
