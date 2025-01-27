package lv.id.arseniuss.linguae.data;

import java.util.ArrayList;
import java.util.List;

public class LineParser {
    public static String[] Split(String line) throws Exception {
        List<String> words = new ArrayList<>();
        String[] a = new String[0];

        int start = 0;
        int quotes = -1;
        int i;

        for (i = 0; i < line.length(); i++) {
            switch (line.charAt(i)) {
                case ' ':
                    if (quotes < 0) {
                        String word = line.substring(start, i);

                        if (!word.isEmpty()) words.add(line.substring(start, i));
                        start = i + 1;
                    }
                    break;
                case '\"':
                    if (quotes < 0) {
                        quotes = i + 1;
                    } else {
                        words.add(line.substring(quotes, i));
                        quotes = -1;
                        while (i < line.length()) {
                            if (line.charAt(i) == ' ') break;
                            i++;

                        }
                        start = i + 1;
                    }
                    break;
                default:
                    break;
            }
        }
        if (quotes >= 0) {
            throw new Exception("Unresolved quotes");
        }
        if (start != i) words.add(line.substring(start, i));

        return words.toArray(a);
    }
}
