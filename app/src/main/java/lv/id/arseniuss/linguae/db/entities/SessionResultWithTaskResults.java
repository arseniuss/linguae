package lv.id.arseniuss.linguae.db.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class SessionResultWithTaskResults {
    @Embedded
    public SessionResult SessionResult = new SessionResult();

    @Relation(parentColumn = "id", entityColumn = "session_id")
    public List<TaskResult> TaskResults = new ArrayList<>();
}
