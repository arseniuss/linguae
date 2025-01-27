package lv.id.arseniuss.linguae.tasks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import lv.id.arseniuss.linguae.R;
import lv.id.arseniuss.linguae.tasks.AbstractTaskFragment;
import lv.id.arseniuss.linguae.tasks.entities.SessionTaskData;
import lv.id.arseniuss.linguae.tasks.viewmodel.NumberViewModel;

public class NumberFragment extends AbstractTaskFragment<NumberViewModel> {

    public NumberFragment(SessionTaskData current) {
        super(current);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_number, container, false);
    }

}