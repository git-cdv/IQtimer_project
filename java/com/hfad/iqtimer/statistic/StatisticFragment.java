package com.hfad.iqtimer.statistic;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.color.MaterialColors;
import com.hfad.iqtimer.R;
import com.hfad.iqtimer.databinding.FragmentStatisticBinding;

import java.util.ArrayList;
import java.util.List;

public class StatisticFragment extends Fragment {

    StatisticViewModel mViewmodel;
    FragmentStatisticBinding binding;
    BarChart mBarChartDay,mBarChartMonth;
    int mColorOnPrimary;
    int mPlanDefault;

    private static final String TAG = "MYLOGS";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewmodel = new ViewModelProvider(requireActivity()).get(StatisticViewModel.class);
        //обновляет график после загрузки данных
        LiveData<String []> liveDataDays = mViewmodel.getDaysDates();
        liveDataDays.observe(this, strings -> {
            Log.d(TAG, "StatisticFragment: liveDataDays");
                setDaysChart();
        });

        LiveData <ArrayList<String>> liveDataMonth = mViewmodel.getMonthDates();
        liveDataMonth.observe(this, arrayList -> {
            Log.d(TAG, "StatisticFragment: liveDataMonth");
               setMonthChart();

        });

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "StatisticFragment: onCreateView");
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_statistic,container,false);
        View v = binding.getRoot();

        mColorOnPrimary = MaterialColors.getColor(requireContext(),R.attr.colorOnPrimary,Color.GRAY);

        mBarChartDay = (BarChart) v.findViewById(R.id.history_chart_days);
        mBarChartMonth = (BarChart) v.findViewById(R.id.history_chart_month);

        binding.setViewmodel(mViewmodel);
        binding.setLifecycleOwner(this);

        return v;
    }

    @SuppressWarnings("RedundantArrayCreation")
    void setDaysChart() {

        ArrayList<BarEntry> arrayForChartDay = mViewmodel.getBarDaysEntries().getValue();
        String[] datesForChartDay = mViewmodel.getDaysDates().getValue();
        mPlanDefault = mViewmodel.getPlanDefault().getValue();

        String stringDescription = getResources().getString(R.string.stat_chart_description);

        //создаем через свой класс, где переопределен метод вывода цвета для бара
        MyBarDataSet barDataSet1 = new MyBarDataSet(arrayForChartDay,stringDescription);
        //назначаем цвета для баров
        barDataSet1.setColors(new int[]{ContextCompat.getColor(requireContext(), R.color.brand_orange), ContextCompat.getColor(requireContext(), R.color.brand_blue_600) });
        barDataSet1.setValueTextColor(mColorOnPrimary);
        BarData barData = new BarData();
        barData.addDataSet(barDataSet1);

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                assert datesForChartDay != null;
                return datesForChartDay[(int) value];
            }
        };
        //настройка оси Х (шаг и формат подписей)
        XAxis xAxis = mBarChartDay.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        xAxis.setTextColor(mColorOnPrimary);

        //добавление "линии тренда" (план) и начала с 0
        YAxis leftAxis = mBarChartDay.getAxisLeft();
        YAxis rightAxis = mBarChartDay.getAxisRight();

        LimitLine ll = new LimitLine(mPlanDefault);
        ll.setLineColor(ContextCompat.getColor(requireContext(), R.color.brand_orange));
        //как пунктир
        ll.enableDashedLine(16f,4f,2f);
        ll.setLineWidth(1f);
        leftAxis.addLimitLine(ll);
        //чтобы начиналось с 0
        leftAxis.setAxisMinimum(0f);
        rightAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(mColorOnPrimary);
        rightAxis.setTextColor(mColorOnPrimary);

        mBarChartDay.setData(barData);
        //устанавливает количество Баров для отображение, если больше - скролится
        mBarChartDay.setVisibleXRangeMaximum(14f);
        //переводит начальный вид графиков в конец
        assert arrayForChartDay != null;
        mBarChartDay.moveViewToX(arrayForChartDay.size());
        //убираем description
        Description description = mBarChartDay.getDescription();
        description.setEnabled(false);
        mBarChartDay.setAutoScaleMinMaxEnabled(true);
        mBarChartDay.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.brand_orange));
        Legend legend = mBarChartDay.getLegend();
        legend.setTextColor(mColorOnPrimary);
        mBarChartDay.invalidate();
        mViewmodel.isDataDaysDone.set(true);
    }

    private void setMonthChart() {

        ArrayList<BarEntry> arrayForChartMonth = mViewmodel.getBarMonthEntries().getValue();
        ArrayList<String> datesForChartMonth = mViewmodel.getMonthDates().getValue();

        String stringDescription = getResources().getString(R.string.stat_chart_description_month);

        //создаем через свой класс, где переопределен метод вывода цвета для бара
        assert arrayForChartMonth != null;
        BarDataSet barDataSet1 = new BarDataSet(arrayForChartMonth,stringDescription);
        //назначаем цвета для баров
        barDataSet1.setColors(ContextCompat.getColor(requireContext(), R.color.brand_blue_600));
        barDataSet1.setValueTextColor(mColorOnPrimary);
        BarData barData = new BarData();
        barData.addDataSet(barDataSet1);

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                assert datesForChartMonth != null;
                return datesForChartMonth.get((int) value);
            }
        };

        //настройка оси Х (шаг и формат подписей)
        XAxis xAxis = mBarChartMonth.getXAxis();

        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        xAxis.setTextColor(mColorOnPrimary);

        //чтобы начиналось с 0
        YAxis leftAxisM = mBarChartMonth.getAxisLeft();
        YAxis rightAxisM = mBarChartMonth.getAxisRight();
        leftAxisM.setAxisMinimum(0f);
        rightAxisM.setAxisMinimum(0f);
        leftAxisM.setTextColor(mColorOnPrimary);
        rightAxisM.setTextColor(mColorOnPrimary);

        mBarChartMonth.setData(barData);
        //устанавливает количество Баров для отображение, если больше - скролится
        mBarChartMonth.setVisibleXRangeMaximum(12f);
        //переводит начальный вид графиков в конец
        mBarChartMonth.moveViewToX(arrayForChartMonth.size());
        //убираем description
        Description description = mBarChartMonth.getDescription();
        description.setEnabled(false);
        Legend legend = mBarChartMonth.getLegend();
        legend.setTextColor(mColorOnPrimary);
        mBarChartMonth.invalidate();
        mViewmodel.isDataMonthDone.set(true);
    }

    class MyBarDataSet extends BarDataSet {

        public MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            if(getEntryForIndex(index).getY() < mPlanDefault){
                return mColors.get(0);}
            else {
                return mColors.get(1);}

        }

    }
}
