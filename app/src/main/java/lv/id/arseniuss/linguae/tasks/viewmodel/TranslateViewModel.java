package lv.id.arseniuss.linguae.tasks.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.db.tasks.TranslateTask;
import lv.id.arseniuss.linguae.tasks.AbstractTaskViewModel;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;

public class TranslateViewModel extends AbstractTaskViewModel {
    private final MutableLiveData<List<WordViewModel>> _answers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<WordViewModel>> _options = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> _answer = new MutableLiveData<>("");

    private final MutableLiveData<Boolean> _isEditMode = new MutableLiveData<>(false);

    public TranslateViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Boolean> IsEditMode() { return _isEditMode; }

    public MutableLiveData<String> Answer() { return _answer; }

    public String Sentence() { return translateTask().Text; }

    @Override
    public boolean Validate() {
        String answer;
        String[] correct = translateTask().Answer;

        if (Boolean.TRUE.equals(_isEditMode.getValue())) {
            Answers().setValue(Arrays.stream(Answer().getValue().split(" "))
                    .map(String::trim)
                    .filter(a -> !a.isEmpty())
                    .map(WordViewModel::new)
                    .collect(Collectors.toList()));
        }

        answer = Answers().getValue().stream().map(a -> a.Option).collect(Collectors.joining(" "));

        boolean isValid = answer.equals(String.join(" ", translateTask().Answer));

        if (!isValid) {
            for (int i = 0; i < Answers().getValue().size(); i++) {
                boolean hasError = true;
                WordViewModel model = Answers().getValue().get(i);

                if (i < correct.length) {
                    hasError = !Objects.equals(correct[i], model.Option);
                }

                model.SetHasError(hasError);
            }
        }

        _taskResult.Result.Points = isValid ? translateTask().Answer.length : 0;
        _taskResult.Result.Amount = translateTask().Answer.length;
        _isValidated.setValue(true);

        return isValid;
    }

    public void SwitchInputMode() {
        if (Boolean.FALSE.equals(_isValidated.getValue())) {
            _isEditMode.setValue(Boolean.FALSE.equals(_isEditMode.getValue()));
        }
    }

    private TranslateTask translateTask() { return (TranslateTask) _taskResult.Task.Data; }

    public MutableLiveData<List<WordViewModel>> Answers() { return _answers; }

    public MutableLiveData<List<WordViewModel>> Options() { return _options; }

    @Override
    public void Load(SessionTaskData task) {
        super.Load(task);

        _isEditMode.setValue(!_sharedPreferences.getBoolean(Constants.PreferenceNoKeyboardKey, false));

        List<String> answers = Arrays.asList(translateTask().Answer);
        List<String> additional = Arrays.stream(translateTask().Additional.split(","))
                .limit((answers.size() * 2L))
                .collect(Collectors.toList());

        Collections.shuffle(additional);

        List<WordViewModel> options = Stream.concat(additional.stream(), answers.stream())
                .map(s -> new WordViewModel(StripAccents(s)))
                .collect(Collectors.toList());

        Collections.shuffle(options);

        Options().setValue(options);
    }

    public static class WordViewModel {

        public final String Option;
        private final MutableLiveData<Boolean> _hasError = new MutableLiveData<Boolean>(false);

        public WordViewModel(String option) {
            Option = option;
        }

        public MutableLiveData<Boolean> HasError() { return _hasError; }

        public void SetHasError(boolean hasError) {
            _hasError.setValue(hasError);
        }
    }
}