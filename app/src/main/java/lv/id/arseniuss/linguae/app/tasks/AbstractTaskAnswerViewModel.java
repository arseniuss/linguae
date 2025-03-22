package lv.id.arseniuss.linguae.app.tasks;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.MutableLiveData;

public abstract class AbstractTaskAnswerViewModel extends BaseObservable {
    protected final MutableLiveData<Boolean> _valid = new MutableLiveData<>(true);
    protected final MutableLiveData<Integer> _state =
            new MutableLiveData<>(TaskState.STATE_EDIT.ordinal());
    protected final MutableLiveData<Boolean> _checked = new MutableLiveData<>(false);

    public boolean Validate() {
        _state.setValue(TaskState.STATE_VALIDATED.ordinal());

        return true;
    }

    public MutableLiveData<Boolean> IsValid() {
        return _valid;
    }

    public MutableLiveData<Integer> State() {
        return _state;
    }

    public MutableLiveData<Boolean> IsChecked() {
        return _checked;
    }

    public enum TaskState {
        STATE_EDIT,
        STATE_CHOOSE,
        STATE_VALIDATED
    }

}
