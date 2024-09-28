package lv.id.arseniuss.linguae.data;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public enum TaskType {
    UnknownTask("Unknown task"),

    CasingTask("Casing task"),

    ChooseTask("Choose task"),

    ConjugateTask("Conjugate task"),

    DeclineTask("Decline task"),

    MacronTask("Macron task"),

    NumberTask("Number task"),

    TranslateTask("Translate task");

    private static final Map<String, TaskType> map = new TreeMap<>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            String prefix1 = removeTaskSuffix(o1);
            String prefix2 = removeTaskSuffix(o2);

            return String.CASE_INSENSITIVE_ORDER.compare(prefix1, prefix2);
        }

        private String removeTaskSuffix(String str) {
            if (str.toLowerCase().endsWith("task")) {
                return str.substring(0, str.length() - 4);
            }
            return str;
        }
    });

    static {
        for (TaskType taskType : TaskType.values()) {
            map.put(taskType.toString(), taskType);
        }
    }

    private final String _name;

    TaskType(String name) {
        _name = name;
    }

    public static TaskType ValueOf(String task) {
        return map.get(task);
    }

    public String GetName() {
        return _name;
    }
}
