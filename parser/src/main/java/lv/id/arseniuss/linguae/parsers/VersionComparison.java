package lv.id.arseniuss.linguae.parsers;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionComparison {

    private static int[] parseVersion(String version, boolean comparison) throws Exception {
        Pattern versionPattern =
                Pattern.compile((comparison ? "[><=]+" : "") + "v?([0-9]+).([0-9]+).([0-9]+)");
        Matcher matcher = versionPattern.matcher(version);

        if (matcher.matches()) {
            int[] v = new int[3];

            v[0] = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
            v[1] = Integer.parseInt(Objects.requireNonNull(matcher.group(2)));
            v[2] = Integer.parseInt(Objects.requireNonNull(matcher.group(3)));

            return v;
        }

        throw new Exception("not a version string: " + version);
    }

    public static boolean Compare(String version, String comparison) throws Exception {
        int[] version1 = parseVersion(version, false);
        int[] version2 = parseVersion(comparison, true);

        Pattern pattern = Pattern.compile("(<|=|==|>|<=|>=)v?([0-9]+).([0-9]+).([0-9]+)");
        Matcher matcher = pattern.matcher(comparison);

        if (!matcher.matches()) throw new Exception("not a comparison string: " + comparison);

        String operator = matcher.group(1);

        int c = 0;

        for (int i = 0; i < 3; i++) {
            if (version1[i] != version2[i]) {
                c = Integer.compare(version1[i], version2[i]);
                break;
            }
        }

        switch (Objects.requireNonNull(operator)) {
            case "<":
                return c < 0;
            case "<=":
                return c <= 0;
            case ">":
                return c > 0;
            case ">=":
                return c >= 0;
            case "=":
            case "==":
                return c == 0;
            case "!=":
                return c != 0;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }
}
