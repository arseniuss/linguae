package lv.id.arseniuss.linguae.app.db;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class DatabaseConverters {
    @TypeConverter
    public static int[] StringToIntArray(String text) {
        return Arrays.stream(text.split(",")).mapToInt(Integer::parseInt).toArray();
    }

    @TypeConverter
    public static String IntArrayToString(int[] ints) {
        return Arrays.stream(ints).mapToObj(Integer::toString).collect(Collectors.joining(","));
    }

    @TypeConverter
    public static String[] StringToStringArray(String text) {
        return text.split(",");
    }

    @TypeConverter
    public static String StringArrayToString(String[] strings) {
        return String.join(",", strings);
    }

    @TypeConverter
    public static Date TimestampToDate(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long DateToTimestamp(Date value) {
        return value == null ? null : value.getTime();
    }
}
