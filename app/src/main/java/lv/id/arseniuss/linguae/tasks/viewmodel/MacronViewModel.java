package lv.id.arseniuss.linguae.tasks.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import lv.id.arseniuss.linguae.tasks.AbstractTaskViewModel;

public class MacronViewModel extends AbstractTaskViewModel {

    public MacronViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public boolean Validate() {
        return false;
    }
}