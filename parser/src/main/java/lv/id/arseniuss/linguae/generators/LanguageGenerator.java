package lv.id.arseniuss.linguae.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lv.id.arseniuss.linguae.Utilities;
import lv.id.arseniuss.linguae.tasks.Task;

public class LanguageGenerator {

    public static String[] Generate(Description description, Task task, String base,
                                    String[] options) throws GeneratorException {
        List<String> result = new ArrayList<>();

        Matcher match = description.Pattern.matcher(base);

        if (!match.find()) {
            throw new GeneratorException("The base " + base + " does not match pattern " +
                    description.Pattern.pattern());
        }

        if (match.groupCount() != description.Groups) {
            throw new GeneratorException(
                    "The pattern " + description.Pattern.pattern() + " did not generate " +
                            description.Groups + " groups: " +
                            IntStream.range(0, match.groupCount())
                                    .mapToObj(match::group)
                                    .collect(Collectors.joining(", ")));
        }

        for (int option_index = 0; option_index < options.length; option_index++) {
            String option = options[option_index];

            OptionalInt optionalInt = IntStream.range(0, description.List.length)
                    .filter(j -> Objects.equals(description.List[j], option))
                    .findAny();

            if (!optionalInt.isPresent()) {
                int finalI = option_index;

                optionalInt = IntStream.range(0, description.List.length)
                        .filter(j -> j == finalI)
                        .findAny();
            }

            if (!optionalInt.isPresent()) {
                throw new GeneratorException(task.Type.name() + " " + description.Category + " " +
                        description.Description + " generator cannot generate for " + option);
            }

            String rule = description.Rules[optionalInt.getAsInt()];
            StringBuilder str = new StringBuilder();

            for (int i = 0; i < rule.length(); i++) {
                char c = rule.charAt(i);

                if (Character.isDigit(c)) {
                    String part = match.group(Character.digit(c, 10));

                    assert part != null;

                    if (i + 1 < rule.length() && Character.isAlphabetic(rule.charAt(i + 1))) {
                        switch (rule.charAt(i + 1)) {
                            case 'l':
                                part = Utilities.AddAccents(part);
                                break;
                            case 's':
                                part = Utilities.StripAccents(part);
                                break;
                            default:
                                throw new GeneratorException(
                                        "Unknown transformation: " + rule.charAt(i + 1));
                        }

                        i++;
                    }

                    str.append(part);
                } else if (c == '+') {
                    continue;
                } else {
                    str.append(c);
                }
            }

            result.add(str.toString());
        }

        return result.toArray(new String[0]);
    }

    public static class Description {
        public lv.id.arseniuss.linguae.enumerators.TaskType TaskType;
        public String Category;
        public String Description;
        public Pattern Pattern;
        public int Groups;
        public String[] List;
        public String[] Rules;
    }

    public static class GeneratorException extends Exception {
        public GeneratorException(String msg) {
            super(msg);
        }
    }
}
