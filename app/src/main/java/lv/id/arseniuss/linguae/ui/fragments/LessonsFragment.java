package lv.id.arseniuss.linguae.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.databinding.FragmentLessonsBinding;
import lv.id.arseniuss.linguae.databinding.ItemLessonBinding;
import lv.id.arseniuss.linguae.db.dataaccess.LessonDataAccess;
import lv.id.arseniuss.linguae.ui.MyRecyclerViewAdapter;
import lv.id.arseniuss.linguae.ui.activities.MainActivity;
import lv.id.arseniuss.linguae.ui.activities.SessionActivity;
import lv.id.arseniuss.linguae.ui.activities.TheoryActivity;
import lv.id.arseniuss.linguae.viewmodel.LessonsViewModel;
import lv.id.arseniuss.linguae.viewmodel.LessonsViewModel.EntryViewModel;

public class LessonsFragment extends Fragment {

    private FragmentLessonsBinding _binding;
    private LessonsViewModel _model;

    @BindingAdapter("items")
    public static void BindLessonList(RecyclerView recyclerView, List<LessonsViewModel.EntryViewModel> lessons) {
        MyRecyclerViewAdapter<LessonsViewModel.EntryViewModel, ItemLessonBinding> adapter =
                (MyRecyclerViewAdapter<EntryViewModel, ItemLessonBinding>) recyclerView.getAdapter();

        assert adapter != null;
        assert lessons != null;

        adapter.Update(lessons);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _model = new ViewModelProvider(this).get(LessonsViewModel.class);
        _binding = FragmentLessonsBinding.inflate(inflater, container, false);

        _binding.setViewmodel(_model);
        _binding.setLifecycleOwner(this);

        _binding.lessons.setAdapter(getAdapter());

        MainActivity mainActivity = (MainActivity) getActivity();

        assert mainActivity != null;

        mainActivity.setSupportActionBar(_binding.toolbar);

        mainActivity.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fragment_lessons, menu);
                mainActivity.SetupDrawer();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        });

        return _binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        _model.Load();
    }

    private RecyclerView.Adapter getAdapter() {
        MyRecyclerViewAdapter<EntryViewModel, ItemLessonBinding> adapter =
                new MyRecyclerViewAdapter<>(this, R.layout.item_lesson, 0, selection -> {
                    LessonDataAccess.LessonWithCount lesson = _model.GetLesson(selection);

                    if (lesson != null && !lesson.Lesson.Id.isEmpty()) {
                        Intent i = new Intent(getContext(), SessionActivity.class);

                        i.putExtra(SessionActivity.LessonExtraTag, lesson.Lesson.Id);

                        startActivity(i);
                    }
                });

        adapter.SetOnLongClickListener(selection -> {
            LessonDataAccess.LessonWithCount lesson = _model.GetLesson(selection);

            if (lesson != null && lesson.TheoryCount > 0 && !lesson.Lesson.Id.isEmpty()) {
                Intent i = new Intent(getContext(), TheoryActivity.class);

                i.putExtra(TheoryActivity.LessonExtraTag, lesson.Lesson.Id);
                i.putExtra(TheoryActivity.LessonNameExtraTag, lesson.Lesson.Name);

                startActivity(i);

                return true;
            }

            return true;
        });

        return adapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }
}