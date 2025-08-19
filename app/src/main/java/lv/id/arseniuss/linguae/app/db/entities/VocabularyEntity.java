package lv.id.arseniuss.linguae.app.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocabulary")
public class VocabularyEntity {
    @PrimaryKey
    @ColumnInfo(name = "word")
    @NonNull
    public String Word;

    public String Text;
}
