package lv.id.arseniuss.linguae.app.tasks.viewmodel;

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

import lv.id.arseniuss.linguae.app.Constants;
import lv.id.arseniuss.linguae.app.db.entities.TaskError;
import lv.id.arseniuss.linguae.app.tasks.AbstractTaskViewModel;
import lv.id.arseniuss.linguae.app.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.tasks.TranslateTask;
import lv.id.arseniuss.linguae.types.TaskType;

public class TranslateViewModel extends AbstractTaskViewModel {
    private final MutableLiveData<List<WordViewModel>> _respoonses =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<WordViewModel>> _options =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> _answer = new MutableLiveData<>("");

    private final MutableLiveData<Boolean> _isClickMode = new MutableLiveData<>(false);

    public TranslateViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Boolean> IsClickMode() {
        return _isClickMode;
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
        int points = 0;
        boolean isClickMode = Boolean.TRUE.equals(IsClickMode().getValue());
        List<List<String>> answers = Arrays.stream(translateTask().Answer)
                .map(a -> Arrays.stream(a.split("/")).collect(Collectors.toList()))
                .collect(Collectors.toList());

        if (isClickMode) {
            List<WordViewModel> clickedResponses = Responses().getValue();

            assert clickedResponses != null;

            final int iterations = max(clickedResponses.size(), answers.size());

            for (int i = 0; i < iterations; i++) {
                boolean hasError = true;
                String response = null;
                WordViewModel model = null;
                String correctOptions = "";

                if (i < clickedResponses.size()) {
                    response = clickedResponses.get(i).Option;
                    model = clickedResponses.get(i);
                }

                if (i < answers.size()) {
                    for (String answer : answers.get(i)) {
                        if (response != null && Objects.equals(response, answer)) {
                            hasError = false;
                            points++;
                            break;
                        }
                    }
                    correctOptions = translateTask().Answer[i];
                }

                if (model != null)
                    model.SetHasError(hasError);

                if (hasError) {
                    _taskResult.Result.Errors.add(
                            new TaskError(TaskType.TranslateTask, response, correctOptions));
                    isValid = false;
                }
            }
        } else {
            int answerIndex = 0;
            int responseIndex = 0;
            String answer = Answer().getValue();

            assert answer != null;

            if (answer.charAt(answer.length() - 1) == '.')
                answer = answer.substring(0, answer.length() - 1);

            String[] writtenResponses = answer.split(" ");

            while (responseIndex < writtenResponses.length && answerIndex < answers.size()) {
                List<String> answerOptions = answers.get(answerIndex);
                boolean isOneWord = answerOptions.stream()
                        .map(a -> Arrays.stream(a.split(" ")).count())
                        .reduce(Long::max)
                        .get() == 1;
                String correctOption = answerOptions.get(0);

                WordViewModel model = new WordViewModel();

                if (isOneWord) {
                    boolean isCorrect = false;

                    for (String option : answerOptions) {
                        if (Objects.equals(writtenResponses[responseIndex], option)) {
                            isCorrect = true;
                            points++;
                            model.Option = option;
                            break;
                        }
                    }

                    if (!isCorrect) {
                        _taskResult.Result.Errors.add(
                                new TaskError(TaskType.TranslateTask,
                                        writtenResponses[responseIndex],
                                        correctOption));
                        model.Option = writtenResponses[responseIndex];
                        isValid = false;
                    }

                    model.SetHasError(!isCorrect);
                } else {
                    if (responseIndex + 1 < writtenResponses.length) {
                        boolean isCorrect = false;
                        String combinedResponse = writtenResponses[responseIndex] + " " +
                                writtenResponses[responseIndex + 1];

                        for (String option : answerOptions) {
                            if (combinedResponse.equals(option)) {
                                isCorrect = true;
                                points++;
                                break;
                            }
                        }

                        if (!isCorrect) {
                            _taskResult.Result.Errors.add(
                                    new TaskError(TaskType.TranslateTask, combinedResponse,
                                            correctOption));
                            model.Option = combinedResponse;
                            isValid = false;
                        }

                        model.SetHasError(!isCorrect);
                        responseIndex += 1;
                    } else {
                        _taskResult.Result.Errors.add(
                                new TaskError(TaskType.TranslateTask,
                                        writtenResponses[responseIndex],
                                        correctOption));
                        isValid = false;
                        model.Option = writtenResponses[responseIndex];
                        model.SetHasError(true);
                    }
                }

                Objects.requireNonNull(Responses().getValue()).add(model);
                responseIndex += 1;
                answerIndex += 1;
            }


            for (; responseIndex < writtenResponses.length; responseIndex++) {
                WordViewModel model = new WordViewModel(writtenResponses[responseIndex]);

                model.SetHasError(true);

                Objects.requireNonNull(Responses().getValue()).add(model);
            }


            Responses().setValue(Responses().getValue());
        }

        _taskResult.Result.Points = isValid ? translateTask().Answer.length : 0;
        _taskResult.Result.Amount = translateTask().Answer.length;
        _isValidated.setValue(true);

        return isValid;
    }

    public void SwitchInputMode() {
        if (Boolean.FALSE.equals(_isValidated.getValue())) {
            _isClickMode.setValue(Boolean.FALSE.equals(_isClickMode.getValue()));
        }
    }

    private TranslateTask translateTask() {
        return (TranslateTask) _taskResult.Task.Data;
    }

    public MutableLiveData<List<WordViewModel>> Responses() {
        return _respoonses;
    }

    public MutableLiveData<List<WordViewModel>> Options() {
        return _options;
    }

    @Override
    public void Load(SessionTaskData task) {
        super.Load(task);

        _isClickMode.setValue(
                _sharedPreferences.getBoolean(Constants.PreferenceNoKeyboardKey, false));

        List<String> answers = Arrays.stream(translateTask().Answer)
                .map(a -> a.contains("/") ? a.split("/")[0] : a)
                .collect(Collectors.toList());

        List<String> additional = Arrays.stream(translateTask().Additional.split(","))
                .limit((long) (answers.size() * 0.75))
                .collect(Collectors.toList());

        Collections.shuffle(additional);

        List<WordViewModel> options = Stream.concat(additional.stream(), answers.stream())
                .map(s -> new WordViewModel(StripAccents(s)))
                .collect(Collectors.toList());

        Collections.shuffle(options);

        Options().setValue(options);
    }

    public static class WordViewModel extends BaseObservable {

        private final MutableLiveData<Boolean> _hasError = new MutableLiveData<Boolean>(false);
        public String Option;

        public WordViewModel(String option) {
            Option = option;
        }

        public WordViewModel() {

        }

        public MutableLiveData<Boolean> HasError() {
            return _hasError;
        }

        public void SetHasError(boolean hasError) {
            _hasError.setValue(hasError);
        }
    }
}