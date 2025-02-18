package lv.id.arseniuss.linguae.db.entities;

import lv.id.arseniuss.linguae.data.TaskType;

public class TaskError {

    public TaskType Type;
    public String Title = "";
    public String IncorrectAnswer = "";
    public String CorrectAnswer = "";

    public TaskError(TaskType taskType, String incorrectAnswer, String correctAnswer) {
        Type = taskType;
        IncorrectAnswer = incorrectAnswer;
        CorrectAnswer = correctAnswer;
    }

    public TaskError(TaskType taskType, String title, String incorrectAnswer, String correctAnswer) {
        Type = taskType;
        Title = title;
        IncorrectAnswer = incorrectAnswer;
        CorrectAnswer = correctAnswer;
    }
}
