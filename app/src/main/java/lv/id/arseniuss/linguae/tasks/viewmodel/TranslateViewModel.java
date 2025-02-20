package lv.id.arseniuss.linguae.tasks.viewmodel;

import static java.lang.Integer.max;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.TaskError;
import lv.id.arseniuss.linguae.db.tasks.TranslateTask;
import lv.id.arseniuss.linguae.tasks.AbstractTaskViewModel;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;

public class TranslateViewModel extends AbstractTaskViewModel {
    private final MutableLiveData<List<WordViewModel>> _answers =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<WordViewModel>> _options =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> _answer = new MutableLiveData<>("");

    private final MutableLiveData<Boolean> _isEditMode = new MutableLiveData<>(false);

    public TranslateViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Boolean> IsEditMode() {
        return _isEditMode;
    }

    public MutableLiveData<String> Answer() {
        return _answer;
    }

    public String Sentence() {
        return translateTask().Text;
    }

    @Override
    public boolean Validate() {
        boolean isValid = true;
        List<WordViewModel> responses = Responses().getValue();
        List<List<String>> answers = Arrays.stream(translateTask().Answer)
                .map(a -> Arrays.stream(a.split("/")).collect(Collectors.toList()))
                .collect(Collectors.toList());

        assert responses != null;

        final int iterations = max(responses.size(), answers.size());

        for (int i = 0; i < iterations; i++) {
            boolean hasError = true;
            String response = null;
            WordViewModel model = null;
            String correctOptions = "";

            if (i < responses.size()) {
                response = responses.get(i).Option;
                model = responses.get(i);
            }

            if (i < answers.size()) {
                for (String answer : answers.get(i)) {
                    if (response != null && Objects.equals(response, answer)) {
                        hasError = false;
                        break;
                    }
                }
                correctOptions = translateTask().Answer[i];
            }

            if (i < responses.size())
                model.SetHasError(hasError);

            if (hasError) {
                _taskResult.Result.Errors.add(
                        new TaskError(TaskType.TranslateTask, response, correctOptions));
                isValid = false;
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

    private TranslateTask translateTask() {
        return (TranslateTask) _taskResult.Task.Data;
    }

    public MutableLiveData<List<WordViewModel>> Responses() {
        return _answers;
    }

    public MutableLiveData<List<WordViewModel>> Options() {
        return _options;
    }

    @Override
    public void Load(SessionTaskData task) {
        super.Load(task);

        _isEditMode.setValue(
                !_sharedPreferences.getBoolean(Constants.PreferenceNoKeyboardKey, false));

        List<String> answers = Arrays.stream(translateTask().Answer)
                .map(a -> a.contains("/") ? a.split("/")[0] : a)
                .collect(Collectors.toList());

        List<String> additional = Arrays.stream(translateTask().Additional.split(","))
                .limit((long) (answers.size() * 2.5))
                .collect(Collectors.toList());

        Collections.shuffle(additional);

        List<WordViewModel> options = Stream.concat(additional.stream(), answers.stream())
                .map(s -> new WordViewModel(StripAccents(s)))
                .collect(Collectors.toList());

        Collections.shuffle(options);

        Options().setValue(options);
    }

    public static class WordViewModel extends BaseObservable {

        public final String Option;
        private final MutableLiveData<Boolean> _hasError = new MutableLiveData<Boolean>(false);

        public WordViewModel(String option) {
            Option = option;
        }

        public MutableLiveData<Boolean> HasError() {
            return _hasError;
        }

        public void SetHasError(boolean hasError) {
            _hasError.setValue(hasError);
        }
    }
}