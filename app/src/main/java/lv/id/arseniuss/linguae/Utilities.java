package lv.id.arseniuss.linguae;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Base64;
import android.util.TypedValue;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

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

                    if (idx != -1) {
                        return replacement.codePointAt(idx);
                    } else {
                        return c;
                    }
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

    public static float GetThemeTextSize(Context context, int attrId) {
        TypedValue value = new TypedValue();

        if (context.getTheme().resolveAttribute(attrId, value, true)) {
            return value.getDimension(context.getResources().getDisplayMetrics());
        }

        return -1;
    }

    public static float GetDimension(Context context, int attrId) {
        TypedValue value = new TypedValue();
        int[] attrs = {attrId};

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

    public static void PrintToastError(Context context, Throwable error) {
        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public static InputStream GetInputStream(Context context, String filename) throws Exception {
        return GetInputStream(context, filename, 5000);
    }

    public static InputStream GetInputStream(Context context, String filename, int timeout) throws Exception {
        Uri filenameUri = Uri.parse(filename);
        String scheme = filenameUri.getScheme();

        assert scheme != null;

        switch (scheme) {
            case ContentResolver.SCHEME_CONTENT:
                // Handle content URI
                ContentResolver resolver = context.getContentResolver();
                Uri documentUri = getDocument(context, filenameUri);

                if (documentUri == null) throw new Exception("Could not find " + filename);

                return resolver.openInputStream(documentUri);
            case ContentResolver.SCHEME_FILE:
                return new FileInputStream(new File(filename));
            case "http":
            case "https":
                // I REALLY hate this
                URI uri = new URI(filename);
                uri = uri.normalize(); // This is important
                URL url = uri.toURL();

                URLConnection connection;

                if (scheme.equals("http"))
                    connection = (HttpURLConnection) url.openConnection();
                else
                    connection = (HttpsURLConnection) url.openConnection();

                connection.setConnectTimeout(timeout);

                return connection.getInputStream();
            default:
                throw new IllegalArgumentException("Unsupported URI scheme: " + scheme);
        }
    }

    private static Uri getDocument(Context context, Uri filenameUri) {
        ContentResolver resolver = context.getContentResolver();
        Uri result = new Uri.Builder().scheme(filenameUri.getScheme()).authority(filenameUri.getAuthority()).build();

        List<String> pathSegments = filenameUri.getPathSegments();
        int start = 0;

        for (start = 0; start < pathSegments.size(); start++) {
            try {
                result = DocumentsContract.buildChildDocumentsUriUsingTree(result,
                        DocumentsContract.getTreeDocumentId(result));

                break;
            } catch (IllegalArgumentException e) {
                result = Uri.parse(result.toString() + "/" + pathSegments.get(start));
            }
        }

        for (int i = start; i < pathSegments.size(); i++) {
            String pathSegment = pathSegments.get(i);

            Cursor cursor = resolver.query(result, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
            }, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String documentId = cursor.getString(0);
                    String displayName = cursor.getString(1);
                    String mimeType = cursor.getString(2);

                    if (displayName.equals(pathSegment)) {
                        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType)) {
                            result = DocumentsContract.buildChildDocumentsUriUsingTree(result, documentId);
                        } else {
                            result = DocumentsContract.buildDocumentUriUsingTree(result, documentId);
                        }
                        break;
                    }
                }
            }
        }

        return result;
    }
}
