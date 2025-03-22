package lv.id.arseniuss.linguae.types;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public enum SettingType {
    Unknown("Unknown setting type"),

    Boolean("Boolean setting type");

    private static final Map<String, SettingType> _map = new TreeMap<>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
        }
    });

    static {
        for (SettingType st : SettingType.values()) {
            _map.put(st.toString(), st);
        }
    }

    private final String _name;

    SettingType(String name) {
        _name = name;
    }

    public static SettingType ValueOf(String setting) {
        return _map.get(setting);
    }

    public String GetName() {
        return _name;
    }
}

