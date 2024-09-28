package lv.id.arseniuss.linguae.ui;

import android.view.View;
import android.widget.ViewSwitcher;

import androidx.databinding.BindingAdapter;

public class BindingAdapters {
    @BindingAdapter("android:switchIndex")
    public static void SetSwitchIndex(ViewSwitcher viewSwitcher, int index) {
        if (index < viewSwitcher.getChildCount()) {
            viewSwitcher.setDisplayedChild(index);
        }
    }

    @BindingAdapter("android:selected")
    public static void SetSelected(View linearLayout, boolean selected) {
        linearLayout.setSelected(selected);
    }
}
