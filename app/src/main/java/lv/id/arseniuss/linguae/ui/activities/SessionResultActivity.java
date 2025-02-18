package lv.id.arseniuss.linguae.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.google.android.material.R.attr;
import com.google.gson.Gson;

import java.util.List;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.Utilities;
import lv.id.arseniuss.linguae.databinding.ActivitySessionResultBinding;
import lv.id.arseniuss.linguae.db.entities.SessionResultWithTaskResults;
import lv.id.arseniuss.linguae.viewmodel.SessionResultViewModel;

public class SessionResultActivity extends AppCompatActivity {
    public static final String ResultTag = "RESULT";

    protected SessionResultViewModel _model;
    protected ActivitySessionResultBinding _binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getIntent();

        super.onCreate(savedInstanceState);

        if (!i.hasExtra(ResultTag)) {
            throw new RuntimeException("No result is provided in indent");
        }

        String result = i.getStringExtra(ResultTag);
        SessionResultWithTaskResults sessionResult = new Gson().fromJson(result,
                SessionResultWithTaskResults.class);

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_session_result);
        _model = new ViewModelProvider(this).get(SessionResultViewModel.class);
        _model.SetResult(sessionResult, this::onLoaded);

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        _binding.pieChart.setDrawCenterText(true);
        _binding.pieChart.setCenterTextColor(Utilities.GetThemeColor(getTheme(),
                attr.colorOnBackground));
        _binding.pieChart.setCenterTextSize(Utilities.GetDimension(getBaseContext(),
                android.R.attr.textSize));

        _binding.pieChart.setDrawHoleEnabled(true);
        _binding.pieChart.setDrawRoundedSlices(true);
        _binding.pieChart.setHoleColor(Utilities.GetThemeColor(getTheme(),
                android.R.attr.colorBackground));
        _binding.pieChart.setDrawEntryLabels(false);

        Legend pieChartLegend = _binding.pieChart.getLegend();

        pieChartLegend.setTextColor(Color.WHITE);
        pieChartLegend.setEnabled(true);
        pieChartLegend.setTextSize(14f);
        pieChartLegend.setWordWrapEnabled(true);

        _binding.pieChart.getDescription().setEnabled(false);
    }

    public void OnContinueClick() {
        finish();
    }

    private void onLoaded() {
        List<PieEntry> pieEntryList = _model.GetTaskResults()
                .stream()
                .collect(Collectors.groupingBy(taskResult -> taskResult.TaskType))
                .values()
                .stream()
                .map(taskResults -> new PieEntry(
                        (float) taskResults.stream().map(t -> t.Points).reduce(0, Integer::sum),
                        taskResults.get(0).TaskType.GetName(), taskResults))
                .collect(Collectors.toList());
        pieEntryList.add(new PieEntry(_model.GetAmount() - _model.GetPoints(), "Errors"));
        PieDataSet dataSet = new PieDataSet(pieEntryList, null);

        _binding.pieChart.setCenterText(_model.GetPoints() + "/" + _model.GetAmount());

        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(20f); // TODO: get from theme
        List<Integer> colors = pieEntryList.stream().map(e -> Utilities.RandomColor()).collect(Collectors.toList());
        colors.add(colors.size() - 1, Color.RED);
        dataSet.setColors(colors);
        dataSet.setValueFormatter(new DefaultValueFormatter(0));

        PieData pieData = new PieData(dataSet);

        _binding.pieChart.setData(pieData);
        _binding.pieChart.invalidate();
    }
}
