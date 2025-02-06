package lv.id.arseniuss.linguae.tasks.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.db.tasks.SelectTask;
import lv.id.arseniuss.linguae.tasks.AbstractTaskAnswerViewModel;
import lv.id.arseniuss.linguae.tasks.AbstractTaskFragment;
import lv.id.arseniuss.linguae.tasks.AbstractTaskViewModel;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;

public class SelectViewModel extends AbstractTaskViewModel {

    private final MutableLiveData<String> _sentence = new MutableLiveData<>("");
    private final MutableLiveData<String> _meaning = new MutableLiveData<>("");
    private final MutableLiveData<List<WordViewModel>> _words = new MutableLiveData<>(new ArrayList<>());

    public SelectViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> Sentence() {
        return _sentence;
    }

    public MutableLiveData<String> Meaning() {
        return _meaning;
    }

    public MutableLiveData<List<WordViewModel>> Words() {
        return _words;
    }

    private SelectTask selectTask() {
        return (SelectTask) _taskResult.Task.Data;
    }

    public void Load(SessionTaskData task, AbstractTaskFragment.TaskChangeListener listener) {
        super.Load(task);

        String sentence = Arrays.stream(selectTask().Sentence)
                .map(this::StripBrackets)
                .collect(Collectors.joining(" "));

        _sentence.setValue(StripAccents(sentence));
        _meaning.setValue(selectTask().Meaning);

        List<String> options = Arrays.stream(selectTask().Options).collect(Collectors.toList());

        int answerCount = 0;
        List<SelectViewModel.WordViewModel> models = new ArrayList<>();

        for (String word : selectTask().Sentence) {
            String answer = null;

            if (word.startsWith("<") && word.endsWith(">"))
                answer = selectTask().Answers[answerCount];

            WordViewModel model = new WordViewModel(StripAccents(StripBrackets(word)), answer,
                    options);

            if (word.startsWith("<") && word.endsWith(">"))
                answerCount++;

            model.SelectedWord().observeForever((s) -> {
                CheckIfCanConfirm(listener);
            });

            models.add(model);
        }

        _words.setValue(models);
    }

    @Override
    public boolean Validate() {
        boolean isValid = true;
        int amount = 0;
        int points = 0;

        for (int i = 0; i < Objects.requireNonNull(Words().getValue()).size(); i++) {
            WordViewModel model = Objects.requireNonNull(Words().getValue()).get(i);

            isValid &= model.Validate();
            if (model.Answer != null && !model.Answer.isEmpty()) {
                amount += 1;
                points += model.Validate() ? 1 : 0;
            }
        }

        _taskResult.Result.Points = points;
        _taskResult.Result.Amount = amount;
        _isValidated.setValue(true);

        return isValid;
    }

    public void CheckIfCanConfirm(AbstractTaskFragment.TaskChangeListener listener) {
        boolean canCheck = true;

        for (WordViewModel word : Objects.requireNonNull(Words().getValue())) {
            if (word.Answer != null)
                canCheck &= !Objects.requireNonNull(word.SelectedWord().getValue()).isEmpty();
        }

        if (listener != null) listener.OnCanCheckChanged(canCheck);
    }

    public static class WordViewModel extends AbstractTaskAnswerViewModel {

        public enum WordState {
            STATE_TEXT,
            STATE_BUTTON,
            STATE_VALIDATED
        }

        private final MutableLiveData<String> _selectedWord = new MutableLiveData<>("");
        private final MutableLiveData<String> _result = new MutableLiveData<>("");
        private final MutableLiveData<Integer> _wordType =
                new MutableLiveData<>(WordState.STATE_TEXT.ordinal());

        public String Word;
        public String Answer;
        public List<String> Options;

        public WordViewModel(String word, String answer, List<String> options) {
            Word = word;
            Answer = answer;
            Options = answer == null ? new ArrayList<>() : options;
            _wordType.setValue(answer == null ? WordState.STATE_TEXT.ordinal() :
                    WordState.STATE_BUTTON.ordinal());
        }

        public MutableLiveData<String> SelectedWord() {
            return _selectedWord;
        }

        public MutableLiveData<Integer> WordType() {
            return _wordType;
        }

        public MutableLiveData<String> Result() {
            return _result;
        }

        @Override
        public boolean Validate() {
            boolean isValid;

            if (Answer == null) {
                isValid = true;
            } else {
                isValid = Objects.equals(_selectedWord.getValue(), Answer);
                _wordType.setValue(WordState.STATE_VALIDATED.ordinal());
                _result.setValue(Word + " (" + _selectedWord.getValue() + ")");
            }

            _state.setValue(TaskState.STATE_VALIDATED.ordinal());
            _valid.setValue(isValid);

            return isValid;
        }
    }
}