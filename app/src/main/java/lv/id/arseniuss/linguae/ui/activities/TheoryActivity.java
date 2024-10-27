package lv.id.arseniuss.linguae.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.ActivityTheoryBinding;
import lv.id.arseniuss.linguae.databinding.ItemChapterBinding;
import lv.id.arseniuss.linguae.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.viewmodel.TheoryViewModel;

public class TheoryActivity extends AppCompatActivity {
    public static final String TheoryExtraTag = "THEORY_ID";
    public static final String TheoryNameExtraTag = "THEORY_NAME";

    public static final String LessonExtraTag = "LESSON_ID";
    public static final String LessonNameExtraTag = "LESSON_NAME";

    private final TheoryActivity _this = this;
    private TheoryViewModel _model;
    private ActivityTheoryBinding _binding;

    @BindingAdapter("items")
    public static void BindLessonList(RecyclerView recyclerView, List<TheoryViewModel.ChapterViewModel> chapters) {
        MyRecyclerViewAdapter<TheoryViewModel.ChapterViewModel, ItemChapterBinding> adapter =
                (MyRecyclerViewAdapter<TheoryViewModel.ChapterViewModel, ItemChapterBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert chapters != null;

        adapter.Update(chapters);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Intent i = getIntent();

        super.onCreate(savedInstanceState);

        if (!i.hasExtra(TheoryExtraTag) && !i.hasExtra(LessonExtraTag)) {
            finish();
        }

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_theory);
        _model = new ViewModelProvider(this).get(TheoryViewModel.class);

        if (i.hasExtra(TheoryExtraTag)) {
            _model.LoadTheory(i.getStringExtra(TheoryExtraTag));
        }
        else if (i.hasExtra(LessonExtraTag)) {
            _model.LoadLesson(i.getStringExtra(LessonExtraTag));
        }

        _binding.setViewmodel(_model);
        _binding.setPresenter(this);
        _binding.setLifecycleOwner(this);

        RecyclerView.Adapter adapter = getMyAdapter();
        _binding.chapters.setAdapter(adapter);

        ActionBar supportActionBar = getSupportActionBar();

        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);

            if (i.hasExtra(TheoryNameExtraTag)) { supportActionBar.setTitle(i.getStringExtra(TheoryNameExtraTag)); }
            else if (i.hasExtra(LessonNameExtraTag)) supportActionBar.setTitle(i.getStringExtra(LessonNameExtraTag));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_theory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        else if (item.getItemId() == R.id.item_language) {
            _model.SwitchLanguage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private RecyclerView.Adapter getMyAdapter() {
        MyRecyclerViewAdapter<TheoryViewModel.ChapterViewModel, ItemChapterBinding> adapter =
                new MyRecyclerViewAdapter<>(this, R.layout.item_chapter);

        MyRecyclerViewAdapter<TheoryViewModel.ChapterViewModel, ItemChapterBinding>.OnBinded binded =
                adapter.new OnBinded() {

                    @Override
                    public void Binded(ItemChapterBinding binding, TheoryViewModel.ChapterViewModel item) {
                        binding.setPresenter(_this);
                    }
                };

        adapter.SetOnBinded(binded);

        return adapter;
    }
}
