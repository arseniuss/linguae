package lv.id.arseniuss.linguae.tasks.viewmodel;

import android.app.Application;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lv.id.arseniuss.linguae.data.TaskType;
import lv.id.arseniuss.linguae.db.entities.TaskError;
import lv.id.arseniuss.linguae.db.tasks.DeclineTask;
import lv.id.arseniuss.linguae.tasks.AbstractTaskAnswerViewModel;
import lv.id.arseniuss.linguae.tasks.AbstractTaskViewModel;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;

public class DeclineViewModel extends AbstractTaskViewModel {

    private final MutableLiveData<List<CaseViewModel>> _cases = new MutableLiveData<>(new ArrayList<>());

    public DeclineViewModel(@NonNull Application application) {
        super(application);
    }

    private DeclineTask declineTask() {
        return (DeclineTask) _taskResult.Task.Data;
    }

    public MutableLiveData<List<CaseViewModel>> Cases() {
        return _cases;
    }

    public String Description() {
        return StripAccents(_taskResult.Task.Description);
    }

    public String Word() {
        return StripAccents(declineTask().Word);
    }

    public String Meaning() {
        return declineTask().Meaning;
    }

    @Override
    public boolean Validate() {
        boolean allValid = true;
        int points = 0;
        int amount = 0;

        for (CaseViewModel caseViewModel : Objects.requireNonNull(_cases.getValue())) {
            boolean valid = caseViewModel.Validate();

            if (valid) points += 1;
            else {
                _taskResult.Result.Errors.add(new TaskError(TaskType.DeclineTask,
                        caseViewModel.Answer().getValue(), caseViewModel._correct));
            }
            amount += 1;

            allValid &= valid;
        }

        _taskResult.Result.Points = points;
        _taskResult.Result.Amount = amount;

        return allValid;
    }

    @Override
    public void Load(SessionTaskData task) {
        super.Load(task);

        assert declineTask().Cases.length == declineTask().Answers.length;

        List<String> randomAnswers = new Random()
                .ints(0, declineTask().Answers.length)
                .distinct()
                .limit(declineTask().Answers.length)
                .mapToObj(i -> declineTask().Answers[i])
                .collect(Collectors.toList());

        List<CaseViewModel> viewModels =
                IntStream.range(0, declineTask().Cases.length)
                        .mapToObj(idx ->
                                new CaseViewModel(
                                        StripAccents(declineTask().Cases[idx]),
                                        randomAnswers,
                                        StripAccents(declineTask().Answers[idx]),
                                        _noKeyboard
                                ))
                        .collect(Collectors.toList());

        _cases.setValue(viewModels);
    }

    public static class CaseViewModel extends AbstractTaskAnswerViewModel {
        private final String _correct;
        private final MutableLiveData<String> _answer = new MutableLiveData<>("");
        private final MutableLiveData<String> _caseName = new MutableLiveData<>("");
        private final MutableLiveData<Spanned> _result =
                new MutableLiveData<>(new SpannableString(""));
        private final MutableLiveData<List<String>> _options =
                new MutableLiveData<>(new ArrayList<>());

        public CaseViewModel(String caseName, List<String> options, String correct,
                             Boolean noKeyboard) {
            _caseName.setValue(caseName);
            _options.setValue(options);
            _correct = correct;
            if (noKeyboard) _state.setValue(TaskState.STATE_CHOOSE.ordinal());
            else _state.setValue(TaskState.STATE_EDIT.ordinal());
        }

        public MutableLiveData<String> CaseName() {
            return _caseName;
        }

        public MutableLiveData<String> Answer() {
            return _answer;
        }

        public MutableLiveData<Spanned> GetResult() {
            return _result;
        }

        public MutableLiveData<List<String>> Options() {
            return _options;
        }

        @Override
        public boolean Validate() {
            String correct = _correct;
            String answer = _answer.getValue().trim();

            boolean result = answer.equals(correct);

            if (!result) {
                _result.setValue(
                        Html.fromHtml("<strike>" + answer + "</strike>        " + correct,
                                Html.FROM_HTML_MODE_LEGACY));
            } else {
                _result.setValue(Html.fromHtml(correct, Html.FROM_HTML_MODE_LEGACY));
            }

            _checked.setValue(true);
            _valid.setValue(result);
            _state.setValue(TaskState.STATE_VALIDATED.ordinal());

            notifyChange();

            return result;
        }
    }
}