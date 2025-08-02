package lv.id.arseniuss.linguae.enumerators;

import lv.id.arseniuss.linguae.interfaces.IntValueSerializable;
import lv.id.arseniuss.linguae.interfaces.NameValueSerializable;

public enum TaskType implements IntValueSerializable, NameValueSerializable {
    UnknownTask(0, "unknown"),

    SelectTask(1, "select"),

    ChooseTask(2, "choose"),

    ConjugateTask(3, "conjugate"),

    DeclineTask(4, "decline"),

    TranslateTask(5, "translate");

    private final int _value;
    private final String _name;

    TaskType(int value, String name) {
        _value = value;
        _name = name;
    }

    public static TaskType FromValue(int value) {
        return IntValueSerializable.FromValue(TaskType.class, value);
    }

    public static TaskType FromName(String name) {
        return NameValueSerializable.FromName(TaskType.class, name);
    }

    @Override
    public int GetValue() {
        return _value;
    }

    @Override
    public String GetName() {
        return _name;
    }
}
