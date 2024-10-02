package lv.id.arseniuss.linguae.tasks.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.db.tasks.CasingTask;
import lv.id.arseniuss.linguae.tasks.AbstractTaskAnswerViewModel;
import lv.id.arseniuss.linguae.tasks.AbstractTaskViewModel;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;

public class CasingViewModel extends AbstractTaskViewModel {

    private final MutableLiveData<String> _sentence = new MutableLiveData<>("");
    private final MutableLiveData<String> _meaning = new MutableLiveData<>("");
    private final MutableLiveData<List<WordViewModel>> _words = new MutableLiveData<>(new ArrayList<>());

    public CasingViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> Sentence() { return _sentence; }

    public MutableLiveData<String> Meaning() { return _meaning; }

    public MutableLiveData<List<WordViewModel>> Words() { return _words; }

    private CasingTask casingTask() { return (CasingTask) _taskResult.Task.Data; }

    @Override
    public void Load(SessionTaskData task) {
        super.Load(task);

        _sentence.setValue(StripAccents(casingTask().Sentence));
        _meaning.setValue(casingTask().Meaning);

        List<String> options = Arrays.stream(casingTask().Options.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        List<String> words = Arrays.stream(casingTask().Sentence.split(" "))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        List<String> answers =
                Arrays.stream(casingTask().Answers.split(",", -1)).map(String::trim).collect(Collectors.toList());

        assert words.size() == answers.size();

        List<CasingViewModel.WordViewModel> models = new ArrayList<>();

        for (int i = 0; i < words.size(); i++) {
            WordViewModel model = new WordViewModel(StripAccents(words.get(i)), options, answers.get(i));

            models.add(model);
        }

        _words.setValue(models);
    }

    @Override
    public boolean Validate() {
        boolean isValid = true;
        int amount = 0;
        int points = 0;

        for (int i = 0; i < Words().getValue().size(); i++) {
            WordViewModel model = Words().getValue().get(i);

            isValid &= model.Validate();
            if (!model.Answer.isEmpty()) {
                amount += 1;
                points += model.Validate() ? 1 : 0;
            }
        }

        _taskResult.Result.Points = points;
        _taskResult.Result.Amount = amount;
        _isValidated.setValue(true);

        return isValid;
    }

    public static class WordViewModel extends AbstractTaskAnswerViewModel {
        private final MutableLiveData<Integer> _selectedPosition = new MutableLiveData<>(0);
        public String Word;
        public String Answer;
        public List<String> Options;

        public WordViewModel(String word, List<String> options, String answer) {
            Word = word;
            Answer = answer;
            Options = options;
        }

        public Boolean CanSelect() { return !Answer.isEmpty(); }

        public MutableLiveData<Integer> SelectedPosition() { return _selectedPosition; }

        @Override
        public boolean Validate() {
            boolean isValid = Answer.isEmpty() || Objects.equals(Options.get(_selectedPosition.getValue()), Answer);

            _checked.setValue(true);
            _valid.setValue(isValid);

            return isValid;
        }
    }
}