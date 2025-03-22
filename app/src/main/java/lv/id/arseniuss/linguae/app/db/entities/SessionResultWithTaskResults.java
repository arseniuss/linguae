package lv.id.arseniuss.linguae.app.db.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class SessionResultWithTaskResults {
    @Embedded
    public SessionResultEntity SessionResult = new SessionResultEntity();

    @Relation(parentColumn = "id", entityColumn = "session_id")
    public List<TaskResultEntity> TaskResults = new ArrayList<>();
}
