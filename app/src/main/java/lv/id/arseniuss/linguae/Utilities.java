package lv.id.arseniuss.linguae;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.TypedValue;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public static String BitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] bytes = stream.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap GetImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void printToastError(Context context, Throwable error) {
        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
