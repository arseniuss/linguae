package lv.id.arseniuss.linguae.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.db.LanguageDatabase;
import lv.id.arseniuss.linguae.app.db.dataaccess.TheoryDataAccess;
import lv.id.arseniuss.linguae.app.ui.BindingAdapters;

public class TheoriesViewModel extends AndroidViewModel {
    private final TheoryDataAccess _theoryDataAccess =
            LanguageDatabase.GetInstance(getApplication()).GetTheoryDataAccess();

    private final MutableLiveData<List<SectionViewModel>> _sections =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> _hasError = new MutableLiveData<>(false);
    private final MutableLiveData<String> _error = new MutableLiveData<>("");
    private final MutableLiveData<List<EntryViewModel>> _theories;

    public TheoriesViewModel(@NonNull Application application) {
        super(application);

        _theories = new MutableLiveData<>(new ArrayList<>());

        loadData();
    }

    public LiveData<List<EntryViewModel>> Theories() {
        return _theories;
    }

    public MutableLiveData<Boolean> HasError() {
        return _hasError;
    }

    public MutableLiveData<String> GetError() {
        return _error;
    }

    public LiveData<List<SectionViewModel>> Sections() {
        return _sections;
    }

    void loadData() {
        Disposable d = _theoryDataAccess.GetTheories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::acceptTheory, this::handleError);
    }

    private void handleError(Throwable throwable) {
        _hasError.setValue(true);
        _error.setValue(throwable.getMessage());
    }

    public TheoryDataAccess.TheoryWithCount GetTheory(int selection) {
        TheoryDataAccess.TheoryWithCount res = null;
        EntryViewModel viewModel = Objects.requireNonNull(_theories.getValue()).get(selection);

        res = viewModel._theory;

        return res;
    }

    private void acceptTheory(List<TheoryDataAccess.TheoryWithCount> theoryWithCounts) {
        Map<String, List<TheoryDataAccess.TheoryWithCount>> sections =
                theoryWithCounts.stream().collect(Collectors.groupingBy(t -> t.Theory.Section));

        _sections.setValue(sections.entrySet()
                .stream()
                .map(s -> new SectionViewModel(s.getKey(), s.getValue()
                        .stream()
                        .map(EntryViewModel::new)
                        .collect(Collectors.toList())))
                .sorted(Comparator.comparing(SectionViewModel::Index))
                .collect(Collectors.toList()));
    }

    public void SetSelectedSection(String tabName) {
        Optional<SectionViewModel> section = Objects.requireNonNull(_sections.getValue())
                .stream().filter(s -> Objects.equals(s.TabName(), tabName)).findAny();

        section.ifPresent(sectionViewModel -> _theories.setValue(
                sectionViewModel.Theories()));
    }

    public static class SectionViewModel extends BaseObservable
            implements BindingAdapters.TabViewModel {
        private final String _name;
        private final List<EntryViewModel> _theories;

        private int _index;

        public SectionViewModel(String name, List<EntryViewModel> theories) {
            _name = name;
            _theories = theories;

            theories.stream()
                    .min(Comparator.comparing(t -> t._theory.Theory.Index))
                    .ifPresent(t -> _index = t._theory.Theory.Index);
        }

        @Override
        public String TabName() {
            return _name;
        }

        public int Index() {
            return _index;
        }

        public List<EntryViewModel> Theories() {
            return _theories;
        }
    }

    public static class EntryViewModel extends BaseObservable {
        private final TheoryDataAccess.TheoryWithCount _theory;

        public EntryViewModel(TheoryDataAccess.TheoryWithCount t) {
            _theory = t;
        }

        @Bindable("Title")
        public String getTitle() {
            return _theory.Theory.Title;
        }

        public String getChapterCountStr() {
            return String.valueOf(_theory.ChapterCount);
        }

        @Bindable("Description")
        public String getDescription() {
            return _theory.Theory.Description;
        }

        public String getId() {
            return _theory.Theory.Id;
        }

        public int getChapterCount() {
            return _theory.ChapterCount;
        }
    }
}
