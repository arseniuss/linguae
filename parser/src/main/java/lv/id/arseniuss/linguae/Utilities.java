package lv.id.arseniuss.linguae;

import java.text.Normalizer;
import java.util.stream.Collectors;

public class Utilities {
    public static String StripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    public static String AddAccents(String s) {
        String vowels = "aeiuoAEIOU";
        String replacement = "āēīūōĀĒĪŌŪ";

        return s.chars()
                .map(c -> {
                    int idx = vowels.indexOf(c);

                    if (idx != -1) {
                        return replacement.codePointAt(idx);
                    } else {
                        return c;
                    }
                })
                .mapToObj(codePoint -> new String(
                        Character.toChars(codePoint))) // Convert code points to characters
                .collect(Collectors.joining());
    }
}
