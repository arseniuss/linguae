package lv.id.arseniuss.linguae.tasks.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.db.tasks.ChooseTask;
import lv.id.arseniuss.linguae.tasks.AbstractTaskAnswerViewModel;
import lv.id.arseniuss.linguae.tasks.AbstractTaskViewModel;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;

public class ChooseViewModel extends AbstractTaskViewModel {
    private final MutableLiveData<List<OptionViewModel>> _options = new MutableLiveData<List<OptionViewModel>>();
    private int _selected = -1;

    public ChooseViewModel(@NonNull Application application) {
        super(application);
    }

    private ChooseTask chooseTask() { return (ChooseTask) _taskResult.Task.Data; }

    public String Description() { return chooseTask().Description; }

    public String Word() { return StripAccents(chooseTask().Word); }

    public String Meaning() { return chooseTask().Meaning; }

    public String Answer() { return StripAccents(chooseTask().Answer); }

    public MutableLiveData<List<OptionViewModel>> Options() { return _options; }

    @Override
    public boolean Validate() {
        boolean isValid;

        OptionViewModel selectedViewModel = Objects.requireNonNull(Objects.requireNonNull(Options().getValue()))
                .get(_selected);
        Optional<OptionViewModel> first = Objects.requireNonNull(Options().getValue())
                .stream()
                .filter(o -> Objects.equals(o.Option, Answer()))
                .findFirst();

        OptionViewModel correctViewModel = first.get();

        if (Objects.equals(selectedViewModel.Option, Answer())) {
            isValid = true;
            selectedViewModel.IsValid().setValue(true);
        }
        else {
            selectedViewModel.IsValid().setValue(false);
            isValid = false;
            correctViewModel.IsValid().setValue(true);
        }

        // Make everything stay
        for (OptionViewModel optionViewModel : Objects.requireNonNull(Options().getValue())) {
            optionViewModel.IsChecked().setValue(true);
        }

        _taskResult.Result.Points = isValid ? 1 : 0;
        _taskResult.Result.Amount = 1;

        return isValid;
    }

    public void SetSelected(int position) {
        _selected = position;
    }

    @Override
    public void Load(SessionTaskData task) {
        super.Load(task);

        _options.setValue(Arrays.stream(chooseTask().Options.split(","))
                .map(o -> new OptionViewModel(StripAccents(o)))
                .collect(Collectors.toList()));
    }

    public static class OptionViewModel extends AbstractTaskAnswerViewModel {
        public String Option;

        public OptionViewModel(String option) {
            Option = option;
        }

    }
}