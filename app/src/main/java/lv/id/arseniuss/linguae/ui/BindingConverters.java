package lv.id.arseniuss.linguae.ui;

import android.view.View;

import androidx.databinding.BindingConversion;

public class BindingConverters {
    @BindingConversion
    public static int BooleanToVisibility(Boolean isVisible) {
        return isVisible ? View.VISIBLE : View.GONE;
    }

    public static int InverseBooleanToVisibility(Boolean isVisible) {
        return isVisible ? View.GONE : View.VISIBLE;
    }
}
