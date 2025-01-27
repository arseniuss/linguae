package lv.id.arseniuss.linguae.tasks;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.MutableLiveData;

public abstract class AbstractTaskAnswerViewModel extends BaseObservable {
    protected final MutableLiveData<Boolean> _checked = new MutableLiveData<>(false);

    protected final MutableLiveData<Boolean> _valid = new MutableLiveData<>(true);

    public boolean Validate() {
        _checked.setValue(true);

        return true;
    }

    public MutableLiveData<Boolean> IsChecked() {
        return _checked;
    }

    public MutableLiveData<Boolean> IsValid() {
        return _valid;
    }

}
