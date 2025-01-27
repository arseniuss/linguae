package lv.id.arseniuss.linguae.tasks;

import androidx.fragment.app.Fragment;

import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;

public abstract class AbstractTaskFragment<TViewModel extends AbstractTaskViewModel> extends Fragment {
    protected final SessionTaskData _task;
    protected Boolean _validated = false;
    protected TViewModel _model;

    public AbstractTaskFragment(SessionTaskData current) {
        _task = current;
    }

    public Boolean IsValidated() {
        return _validated;
    }

    public final Boolean Validate() {
        Boolean isValid = _model.Validate();

        _validated = true;

        return isValid;
    }

}
