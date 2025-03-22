package lv.id.arseniuss.linguae.app.ui.preference;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;

public class EditIntegerPreference extends EditTextPreference
        implements EditTextPreference.OnBindEditTextListener {
    private String _text;

    public EditIntegerPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                 int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOnBindEditTextListener(this);
    }

    public EditIntegerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnBindEditTextListener(this);
    }

    public EditIntegerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnBindEditTextListener(this);
    }

    public EditIntegerPreference(Context context) {
        super(context);
        setOnBindEditTextListener(this);
    }

    /**
     * Gets the text from the current data storage.
     *
     * @return The current preference value
     */
    public String getText() {
        return _text;
    }

    /**
     * Saves the text to the current data storage.
     *
     * @param text The text to save
     */
    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();

        _text = text;

        int value = Integer.parseInt(text);

        persistInt(value);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }

        notifyChanged();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        int value;
        if (defaultValue != null) {
            String strDefaultValue = (String) defaultValue;

            int defaultIntValue = Integer.parseInt(strDefaultValue);
            value = getPersistedInt(defaultIntValue);
        } else {
            value = getPersistedInt(0);
        }

        setText(Integer.toString(value));
    }

    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(_text) || super.shouldDisableDependents();
    }

    @Override
    public void onBindEditText(@NonNull EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
    }
}
