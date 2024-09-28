package lv.id.arseniuss.linguae;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;
import java.util.stream.Collectors;

public class Utilities {
    private static final Stack<Integer> _colors = new Stack<>();
    private static final Stack<Integer> _recycle = new Stack<>();

    static {
        _recycle.addAll(
                Arrays.asList(0xfff44336, 0xffe91e63, 0xff9c27b0, 0xff673ab7, 0xff3f51b5, 0xff2196f3, 0xff03a9f4,
                        0xff00bcd4, 0xff009688, 0xff4caf50, 0xff8bc34a, 0xffcddc39, 0xffffeb3b, 0xffffc107, 0xffff9800,
                        0xffff5722, 0xff795548, 0xff9e9e9e, 0xff607d8b, 0xff333333));
    }

    public static String StripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    public static String AddAccents(String s) {
        String vowels = "aeiuoAEIOU";
        String replacement = "āēīūōĀĒĪŌŪ";

        return s.chars().map(c -> {
                    int idx = vowels.indexOf(c);

                    if (idx != -1) { return replacement.codePointAt(idx); }
                    else { return c; }
                }).mapToObj(codePoint -> new String(Character.toChars(codePoint))) // Convert code points to characters
                .collect(Collectors.joining());
    }

    public static int RandomColor() {
        if (_colors.isEmpty()) {
            while (!_recycle.isEmpty()) _colors.push(_recycle.pop());
            Collections.shuffle(_colors);
        }
        Integer c = _colors.pop();

        _recycle.push(c);

        return c;
    }

    public static int GetThemeColor(Resources.Theme theme, int attrId) {
        TypedValue value = new TypedValue();

        theme.resolveAttribute(attrId, value, true);

        return value.data;
    }

    public static float GetDimension(Context context, int attrId) {
        TypedValue value = new TypedValue();
        int[] attrs = { attrId };

        TypedArray a = context.obtainStyledAttributes(value.data, attrs);

        float dim = a.getDimension(0, -1);

        a.recycle();

        return dim;
    }
}
