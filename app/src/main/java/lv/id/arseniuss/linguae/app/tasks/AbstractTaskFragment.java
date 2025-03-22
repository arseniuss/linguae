package lv.id.arseniuss.linguae.app.tasks;

import androidx.fragment.app.Fragment;

import lv.id.arseniuss.linguae.app.tasks.entities.SessionTaskData;

public abstract class AbstractTaskFragment<TViewModel extends AbstractTaskViewModel>
        extends Fragment {
    protected final SessionTaskData _task;
    protected Boolean _validated = false;
    protected TViewModel _model;
    protected TaskChangeListener _listener;

    public AbstractTaskFragment(SessionTaskData current, TaskChangeListener listener) {
        _task = current;
        _listener = listener;
    }

    public Boolean IsValidated() {
        return _validated;
    }

    public final Boolean Validate() {
        Boolean isValid = _model.Validate();

        _validated = true;

        return isValid;
    }

    public interface TaskChangeListener {
        void OnCanCheckChanged(boolean canCheck);
    }

}
