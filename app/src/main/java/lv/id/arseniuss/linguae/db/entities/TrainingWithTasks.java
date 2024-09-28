package lv.id.arseniuss.linguae.db.entities;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class TrainingWithTasks {
    @Embedded
    public Training Training;

    @Relation(parentColumn = "id", entityColumn = "task_id", associateBy = @Junction(TrainingTaskCrossref.class))
    public List<Task> Tasks = new ArrayList<>();
}
