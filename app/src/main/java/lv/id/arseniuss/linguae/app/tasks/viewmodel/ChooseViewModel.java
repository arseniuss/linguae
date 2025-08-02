package lv.id.arseniuss.linguae.app.tasks.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.Utilities;
import lv.id.arseniuss.linguae.app.db.entities.TaskError;
import lv.id.arseniuss.linguae.app.tasks.AbstractTaskAnswerViewModel;
import lv.id.arseniuss.linguae.app.tasks.AbstractTaskViewModel;
import lv.id.arseniuss.linguae.app.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.enumerators.TaskType;
import lv.id.arseniuss.linguae.tasks.ChooseTask;

public class ChooseViewModel extends AbstractTaskViewModel {
    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());

    private final int _chooseOptionCount =
            _sharedPreferences.getInt(getApplication().getString(R.string.ChooseOptionCountKey), 6);

    private final MutableLiveData<List<OptionViewModel>> _options = new MutableLiveData<>();
    private int _selected = -1;
    private String _word = "";

    public ChooseViewModel(@NonNull Application application) {
        super(application);
    }

    private ChooseTask chooseTask() {
        return (ChooseTask) _taskResult.Task.Data;
    }

    public String Description() {
        return chooseTask().Description;
    }

    public String Word() {
        return _word;
    }

    public String Answer() {
        return StripAccents(chooseTask().Answer);
    }

    public MutableLiveData<List<OptionViewModel>> Options() {
        return _options;
    }

    @Override
    public boolean Validate() {
        boolean isValid;

        OptionViewModel selectedViewModel =
                Objects.requireNonNull(Objects.requireNonNull(Options().getValue())).get(_selected);
        Optional<OptionViewModel> first = Objects.requireNonNull(Options().getValue())
                .stream()
                .filter(o -> Objects.equals(o.Option, Answer()))
                .findFirst();

        assert first.isPresent();

        OptionViewModel correctViewModel = first.get();

        for (OptionViewModel optionViewModel : Objects.requireNonNull(Options().getValue())) {
            optionViewModel.IsValid().setValue(false);
        }

        if (Objects.equals(selectedViewModel.Option, Answer())) {
            isValid = true;
            selectedViewModel.IsValid().setValue(true);
        } else {
            selectedViewModel.IsValid().setValue(false);
            isValid = false;
            correctViewModel.IsValid().setValue(true);

            _taskResult.Result.Errors.add(
                    new TaskError(TaskType.ChooseTask, selectedViewModel.Option,
                            correctViewModel.Option));
        }

        // Make everything stay
        for (OptionViewModel optionViewModel : Objects.requireNonNull(Options().getValue())) {
            optionViewModel.IsEditable().setValue(false);
            optionViewModel.State()
                    .setValue(AbstractTaskAnswerViewModel.TaskState.STATE_VALIDATED.ordinal());
            optionViewModel.IsChecked().setValue(true);
            optionViewModel.notifyChange();
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

        _word = Utilities.ExtractLinkTitles(chooseTask().Word);
        _word = StripAccents(_word);

        List<String> strings = Arrays.stream(chooseTask().Additionals.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        strings.removeIf(s -> Objects.equals(s, chooseTask().Answer));

        int[] indexes = new SecureRandom().ints(0, strings.size())
                .distinct()
                .limit(_chooseOptionCount - 1)
                .toArray();

        List<String> collected1 =
                Arrays.stream(indexes).mapToObj(strings::get).collect(Collectors.toList());

        List<OptionViewModel> collected2 = collected1.stream()
                .map(o -> new OptionViewModel(StripAccents(o)))
                .collect(Collectors.toList());

        collected2.add(new OptionViewModel(StripAccents(chooseTask().Answer)));

        Collections.shuffle(collected2);

        _options.setValue(collected2);
    }

    public static class OptionViewModel extends AbstractTaskAnswerViewModel {
        protected final MutableLiveData<Boolean> _editable = new MutableLiveData<>(true);

        public String Option;

        public OptionViewModel(String option) {
            Option = option;
        }

        public MutableLiveData<Boolean> IsEditable() {
            return _editable;
        }

    }
}