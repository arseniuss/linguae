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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.TaskDataAccess;
import lv.id.arseniuss.linguae.db.tasks.ConjugateTask;
import lv.id.arseniuss.linguae.tasks.AbstractTaskAnswerViewModel;
import lv.id.arseniuss.linguae.tasks.AbstractTaskViewModel;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;

public class ConjugateViewModel extends AbstractTaskViewModel {

    private final TaskDataAccess _taskDataAccess = LanguageDatabase.GetInstance(getApplication(), _language)
            .GetTaskDataAccess();
    private final MutableLiveData<List<PersonViewModel>> _persons = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> _word = new MutableLiveData<>("");

    public ConjugateViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public boolean Validate() {
        boolean allValid = true;
        int points = 0;
        int amount = 0;

        for (ConjugateViewModel.PersonViewModel viewModel : Objects.requireNonNull(_persons.getValue())) {
            if (viewModel.IsRequired()) {
                boolean valid = viewModel.Validate();

                if (valid) points += 1;

                allValid &= valid;
                amount += 1;
            }
        }

        _taskResult.Result.Points = points;
        _taskResult.Result.Amount = amount;

        return allValid;
    }

    private ConjugateTask conjugateTask() { return (ConjugateTask) _taskResult.Task.Data; }

    public MutableLiveData<List<PersonViewModel>> Persons() { return _persons; }

    public String Description() { return StripAccents(_taskResult.Task.Description); }

    public MutableLiveData<String> Word() { return _word; }

    public String Meaning() { return conjugateTask().Meaning; }

    @Override
    public void Load(SessionTaskData task) {
        super.Load(task);

        assert conjugateTask().Persons.length == conjugateTask().Answers.length;

        List<PersonViewModel> viewModels = IntStream.range(0, conjugateTask().Persons.length)
                .mapToObj(idx -> new PersonViewModel(StripAccents(conjugateTask().Persons[idx]),
                        StripAccents(conjugateTask().Answers[idx])))
                .collect(Collectors.toList());

        _persons.setValue(viewModels);

        _word.setValue(conjugateTask().Verb);
    }

    public static class PersonViewModel extends AbstractTaskAnswerViewModel {
        private final MutableLiveData<String> _answer = new MutableLiveData<>("");
        private final MutableLiveData<String> _personName = new MutableLiveData<>("");
        private final String _correct;

        private final MutableLiveData<Spanned> _result = new MutableLiveData<>(new SpannableString(""));

        public PersonViewModel(String person, String correct) {
            _personName.setValue(person);
            _correct = correct;
            _checked.setValue(_correct.isEmpty());
        }

        public MutableLiveData<String> PersonName() { return _personName; }

        public MutableLiveData<String> Answer() { return _answer; }

        public boolean IsRequired() { return !_correct.isEmpty(); }

        public MutableLiveData<Spanned> GetResult() { return _result; }

        @Override
        public boolean Validate() {
            String correct = _correct;
            String answer = Objects.requireNonNull(_answer.getValue()).trim();
            boolean hasMultipleAnswers = correct.contains("/");

            boolean result = false;

            if (correct.contains("/")) {
                String[] values = correct.split("/");

                for (String value : values) {
                    result = answer.equals(value);
                    if (result) break;
                }
            }
            else {
                result = answer.equals(correct);
            }

            if (!result) {
                _result.setValue(
                        Html.fromHtml("<strike>" + answer + "</strike>\t" + correct, Html.FROM_HTML_MODE_LEGACY));
            }
            else {
                if (hasMultipleAnswers) {
                    List<String> results = new ArrayList<>();
                    String[] values = correct.split("/");

                    for (String value : values) {
                        if (value.equals(answer)) { results.add("<u>" + value + "</u>"); }
                        else { results.add(value); }
                    }

                    _result.setValue(Html.fromHtml(results.stream().collect(Collectors.joining("/")),
                            Html.FROM_HTML_MODE_LEGACY));
                }
                else {
                    _result.setValue(Html.fromHtml(correct, Html.FROM_HTML_MODE_LEGACY));
                }
            }

            _checked.setValue(true);

            return result;
        }
    }
}