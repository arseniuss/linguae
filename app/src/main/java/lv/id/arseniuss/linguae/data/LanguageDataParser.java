package lv.id.arseniuss.linguae.data;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lv.id.arseniuss.linguae.Utilities;
import lv.id.arseniuss.linguae.db.entities.Chapter;
import lv.id.arseniuss.linguae.db.entities.LessonWithAttrs;
import lv.id.arseniuss.linguae.db.entities.Setting;
import lv.id.arseniuss.linguae.db.entities.Task;
import lv.id.arseniuss.linguae.db.entities.TaskConfig;
import lv.id.arseniuss.linguae.db.entities.Theory;
import lv.id.arseniuss.linguae.db.entities.TheoryWithChapters;
import lv.id.arseniuss.linguae.db.entities.Training;
import lv.id.arseniuss.linguae.db.entities.TrainingWithTasks;
import lv.id.arseniuss.linguae.db.tasks.CasingTask;
import lv.id.arseniuss.linguae.db.tasks.ChooseTask;
import lv.id.arseniuss.linguae.db.tasks.ConjugateTask;
import lv.id.arseniuss.linguae.db.tasks.DeclineTask;
import lv.id.arseniuss.linguae.db.tasks.TranslateTask;

public class LanguageDataParser {
    final String _gen_prefix = "@gen:";
    final String _eol_prefix = "EOL";

    private final ParserData _data = new ParserData();
    private final ParserInterface _parserInterface;
    private final Map<String, String> _references = new HashMap<>();
    private final List<LanguageGenerator.Description> _generators = new ArrayList<>();
    private final Pattern _referencePattern = Pattern.compile("&([a-zA-Z0-9_]+)");
    private boolean _saveImages = false;
    private boolean _throwError = false;
    private int _line = 0;
    private String _filename = "";
    private boolean _hasError = false;
    private boolean _languageFileParsed = false;

    public LanguageDataParser(@NonNull ParserInterface parserInterface) {
        _parserInterface = parserInterface;
    }

    public LanguageDataParser(@NonNull ParserInterface parserInterface, boolean saveImages) {
        _parserInterface = parserInterface;
        _saveImages = saveImages;
    }

    public static <T> List<T> takeWhile(T[] list, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (!predicate.test(item)) {
                break;
            }
            result.add(item);
        }
        return result;
    }

    private void log(int type, String message) {
        _parserInterface.Inform(type, message);
    }

    private InputStream getFile(Uri base, String filename) throws IOException {
        return _parserInterface.GetFile(base, filename);
    }

    private void logError(String error) throws ParserException {
        error = "[E] " + error;

        if (_throwError) {
            throw new ParserException(_filename, _line, error);
        }
        else {
            log(Log.ERROR, _filename + ":" + _line + ": " + error);
            _hasError = true;
        }
    }

    private boolean parseTask(Task task, String line, String[] words, Map<String, String> references)
            throws ParserException
    {
        if (words.length < 3) {
            logError("Expecting format: task <id> <task-type> <task-data>");
            logError("Got: " + line);
            return false;
        }

        task.Id = _filename + "-" + words[1]; // filename.txt-1

        String taskType = words[2].trim().toLowerCase();

        switch (taskType) {
            case "casing":
                if (words.length != 3 + 4) {
                    logError("Expected format: <meaning> <sentence> <answers> <options>");
                    logError("Got: " + line);
                    return false;
                }

                CasingTask casingTask = new CasingTask();

                task.Type = TaskType.CasingTask;

                task.Category = null;
                task.Description = null;

                casingTask.Meaning = resolveReferences(words[3], references);
                casingTask.Sentence = resolveReferences(words[4], references);
                casingTask.Answers = resolveReferences(words[5], references);
                casingTask.Options = resolveReferences(words[6], references);

                List<String> options = Arrays.stream(casingTask.Options.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                List<String> sentence = Arrays.stream(casingTask.Sentence.split(" "))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                List<String> answers = Arrays.stream(casingTask.Answers.split(",", -1)).collect(Collectors.toList());

                boolean failed = false;
                for (String answer : answers) {
                    if (answer.isEmpty()) continue;
                    if (!options.contains(answer)) {
                        logError("Casing answer \"" + answer + "\" is not in options list: " + casingTask.Options);
                        failed = true;
                    }
                }

                if (failed) return false;

                if (sentence.size() != answers.size()) {
                    logError("Casing sentence and answer not the same: " + casingTask.Sentence + "/" +
                             casingTask.Answers);
                    return false;
                }

                task.Amount = casingTask.Answers.length();
                task.Data = casingTask;
                break;
            case "decline":
                if (words.length != 3 + 6) {
                    logError("Expecting format: type #N decline <type> <description> <word> <meaning> <options> " +
                             "<answers>");
                    logError("Got: " + line);
                    return false;
                }
                DeclineTask declineTask = new DeclineTask();

                task.Type = TaskType.DeclineTask;

                task.Category = resolveReferences(words[3], references);
                task.Description = resolveReferences(words[4], references);

                declineTask.Word = resolveReferences(words[5], references);
                declineTask.Meaning = resolveReferences(words[6], references);

                String references1 = resolveReferences(words[7], references);
                String references2 = resolveReferences(words[8], references);

                declineTask.Cases = references1.split(",", -1);

                if (references2.startsWith(_gen_prefix)) {
                    Optional<LanguageGenerator.Description> description = _generators.stream()
                            .filter(d -> d.TaskType == task.Type && Objects.equals(d.Category, task.Category) &&
                                         Objects.equals(d.Description, task.Description))
                            .findAny();

                    if (!description.isPresent()) {
                        logError("There is no generator for " + task.Type.GetName() + " " + task.Category + " " +
                                 task.Description);
                        return false;
                    }

                    try {
                        String base = references2.substring(_gen_prefix.length());

                        declineTask.Answers =
                                LanguageGenerator.Generate(description.get(), task, base, declineTask.Cases);
                    } catch (LanguageGenerator.GeneratorException ex) {
                        logError(ex.getMessage());
                        return false;
                    }
                }
                else {
                    declineTask.Answers = references2.split(",", -1);

                    if (declineTask.Cases.length != declineTask.Answers.length) {
                        logError("Decline task case count is not the same as answers: " + declineTask.Cases.length +
                                 "/" + declineTask.Answers.length);
                        return false;
                    }
                }

                task.Amount = declineTask.Answers.length;
                task.Data = declineTask;
                break;
            case "conjugate":
                if (words.length != 3 + 6) {
                    logError("Expecting format: task #n conjugate <conjugation> \"<mood> <tense> <voice>\" <word> " +
                             "<meaning> <persons> <answers>");
                    logError("Got: " + line);
                    return false;
                }
                ConjugateTask conjugateTask = new ConjugateTask();

                task.Type = TaskType.ConjugateTask;
                task.Category = resolveReferences(words[3], references);
                task.Description = resolveReferences(words[4], references);

                conjugateTask.Verb = resolveReferences(words[5], references);
                conjugateTask.Meaning = resolveReferences(words[6], references);

                String references3 = resolveReferences(words[7], references);
                String references4 = resolveReferences(words[8], references);

                conjugateTask.Persons = references3.split(",", -1);

                if (references4.startsWith(_gen_prefix)) {
                    Optional<LanguageGenerator.Description> description = _generators.stream()
                            .filter(d -> d.TaskType == task.Type && Objects.equals(d.Category, task.Category) &&
                                         Objects.equals(d.Description, task.Description))
                            .findAny();

                    if (!description.isPresent()) {
                        logError("There is no generator for " + task.Type.GetName() + " " + task.Category + " " +
                                 task.Description);
                        return false;
                    }

                    try {
                        String base = references4.substring(_gen_prefix.length());

                        conjugateTask.Answers =
                                LanguageGenerator.Generate(description.get(), task, base, conjugateTask.Persons);
                    } catch (LanguageGenerator.GeneratorException ex) {
                        logError(ex.getMessage());
                        return false;
                    }
                }
                else {
                    conjugateTask.Answers = references4.split(",", -1);
                }

                if (conjugateTask.Persons.length != conjugateTask.Answers.length) {
                    logError("Conjugate task person count is not the same as answers: " + conjugateTask.Persons.length +
                             "/" + conjugateTask.Answers.length);
                    return false;
                }

                task.Amount = conjugateTask.Answers.length;
                task.Data = conjugateTask;
                break;
            case "choose":
                if (words.length != 3 + 5) {
                    logError("Expected format: <description> <word> <meaning> <answer> <options>");
                    logError("Got: " + line);
                    return false;
                }

                ChooseTask chooseTask = new ChooseTask();

                task.Type = TaskType.ChooseTask;
                task.Category = null;
                task.Description = null;

                chooseTask.Description = resolveReferences(words[3], references);
                chooseTask.Word = resolveReferences(words[4], references);
                chooseTask.Meaning = resolveReferences(words[5], references);
                chooseTask.Answer = resolveReferences(words[6], references);
                chooseTask.Options = resolveReferences(words[7], references);

                boolean hasAnswer = Arrays.stream(chooseTask.Options.split(","))
                        .anyMatch(o -> Objects.equals(o, chooseTask.Answer));
                if (!hasAnswer) {
                    logError(
                            "Choose task answer (" + chooseTask.Answer + ") is not in the list: " + chooseTask.Options);
                    return false;
                }

                task.Amount = 1;
                task.Data = chooseTask;
                break;
            case "translate":
                if (words.length != 6) {
                    logError("Expected format: <text> <answer>");
                    logError("Got: " + line);
                    return false;
                }

                TranslateTask translateTask = new TranslateTask();

                task.Type = TaskType.TranslateTask;
                task.Category = null;
                task.Description = null;

                translateTask.Text = resolveReferences(words[3], references);
                translateTask.Answer = resolveReferences(words[4], references).split(" ");
                translateTask.Additional = resolveReferences(words[5], references);

                task.Amount = translateTask.Answer.length;
                task.Data = translateTask;
                break;
            default:
                logError("Unrecognised task: " + taskType);
                return false;
        }

        return true;
    }

    private String resolveReferences(String word, Map<String, String> references) throws ParserException {

        if (!word.isEmpty()) {
            Matcher matcher = _referencePattern.matcher(word);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String ref = matcher.group(1);
                String replacement;

                if (references.containsKey(ref)) {
                    replacement = references.get(ref);
                }
                else {
                    if (_references.containsKey(ref)) {
                        replacement = _references.get(ref);
                    }
                    else {
                        logError("Cannot find reference: " + word);
                        return word;
                    }
                }

                matcher.appendReplacement(result, replacement);
            }
            matcher.appendTail(result);

            return result.toString();
        }

        return word;
    }

    private String[] getWords(String line, LineNumberReader r) throws Exception {
        String[] words = LineParser.Split(line);

        words = takeWhile(words, w -> !w.startsWith("#")).toArray(new String[0]);

        if (words.length != 0) {
            String last = words[words.length - 1];

            if (last.endsWith("\\")) {
                last = last.substring(0, last.length() - 1);
                words[words.length - 1] = last;

                if (last.isEmpty()) words = Arrays.copyOfRange(words, 0, words.length - 1);

                String[] next = getWords(r.readLine(), r);

                if (last.isEmpty()) {
                    words = Stream.concat(Arrays.stream(words), Arrays.stream(next)).toArray(String[]::new);
                }
                else {
                    words[words.length - 1] += next[0];

                    if (next.length > 1) {
                        words = Stream.concat(Arrays.stream(words),
                                Arrays.stream(Arrays.copyOfRange(next, 1, next.length))).toArray(String[]::new);
                    }
                }
            }
            else if (last.equals("<" + _eol_prefix)) {
                String l = r.readLine();
                List<String> lines = new ArrayList<>();

                while (!l.startsWith(_eol_prefix)) {
                    lines.add(l.trim());
                    l = r.readLine();
                }

                words[words.length - 1] = String.join("\n", lines);

                String[] next = getWords(l.substring(_eol_prefix.length()), r);

                words = Stream.concat(Arrays.stream(words), Arrays.stream(next)).toArray(String[]::new);
            }
        }

        return words;
    }

    private List<Task> parseTrainingFile(Uri base, Training t) throws Exception {
        _filename = t.Filename;

        log(Log.INFO, "Parsing training file: " + t.Filename);

        InputStream trainingFileStream = getFile(base, t.Filename);
        LineNumberReader r = new LineNumberReader(new BufferedReader(new InputStreamReader(trainingFileStream)));

        Map<String, Task> tasks = new HashMap<>();
        Map<String, String> references = new HashMap<>();

        String line;

        while ((line = r.readLine()) != null) {
            _line = r.getLineNumber();
            String[] words = getWords(line, r);

            if (words.length == 0) continue;

            String keyword = words[0].trim().toLowerCase();

            switch (keyword) {
                case "ref":
                    if (words.length != 3) {
                        logError("Expected format: ref <name> <text>");
                        continue;
                    }

                    references.put(words[1], words[2]);
                    break;
                case "task":
                    Task task = new Task();

                    if (!parseTask(task, line, words, references)) continue;

                    if (tasks.containsKey(task.Id)) {
                        logError("Task " + task.Id + " already exists!");
                        continue;
                    }

                    tasks.put(task.Id, task);
                    break;
                default:
                    logError("Unrecognized keyword in " + t.Filename + ": " + keyword);
                    break;
            }
        }

        log(Log.INFO, "Training file " + t.Filename + " parsed");

        return new ArrayList<>(tasks.values());
    }

    private Collection<Task> parseLessonFile(Uri base, LessonWithAttrs l) throws Exception {
        _filename = l.Lesson.Id;

        log(Log.INFO, "Parsing lesson file " + l.Lesson.Id);

        InputStream lessonFileStream = getFile(base, l.Lesson.Id);
        LineNumberReader r = new LineNumberReader(new BufferedReader(new InputStreamReader(lessonFileStream)));
        Map<String, Task> lessonTasks = new HashMap<>();
        Map<String, String> references = new HashMap<>();

        String line;

        while ((line = r.readLine()) != null) {
            _line = r.getLineNumber();
            String[] words = getWords(line, r);

            if (words.length == 0) continue;

            String keyword = words[0].trim().toLowerCase();

            switch (keyword) {
                case "theory":
                    if (words.length != 2) {
                        logError("Expecting format: theory <filename>");
                        continue;
                    }

                    Theory t = new Theory();

                    t.Id = words[1];

                    l.Theories.add(t);
                    break;
                case "ref":
                    if (words.length != 3) {
                        logError("Expected format: ref <name> <text>");
                        continue;
                    }

                    references.put(words[1], words[2]);
                    break;
                case "name":
                    if (words.length != 2) {
                        logError("Expected format: name <name>  ");
                        continue;
                    }

                    if (!l.Lesson.Name.isEmpty()) {
                        logError("Lesson name is already set");
                        continue;
                    }

                    l.Lesson.Name = words[1];
                    break;
                case "description":
                    if (!l.Lesson.Description.isEmpty()) {
                        logError("Lesson description is already set");
                        continue;
                    }

                    l.Lesson.Description = Arrays.stream(words).skip(1).collect(Collectors.joining(" "));
                    break;
                case "task":
                    Task task = new Task();

                    if (!parseTask(task, line, words, references)) continue;

                    if (lessonTasks.containsKey(task.Id)) {
                        logError("Task " + task.Id + " already exists!");
                        continue;
                    }

                    lessonTasks.put(task.Id, task);
                    break;
                case "task-ref":
                    if (words.length != 2) {
                        logError("Expecting format: task-ref <filename-id>");
                        continue;
                    }
                    if (lessonTasks.containsKey(words[1])) {
                        logError("Task reference " + words[1] + " is already listed");
                        continue;
                    }

                    lessonTasks.put(words[1], new Task(words[1]));
                    break;
                default:
                    logError("Unrecognised keyword in lesson file: " + keyword);
            }
        }

        log(Log.INFO, "Lesson file " + l.Lesson.Id + " parsed");

        return lessonTasks.values();
    }

    private String getLine(String prefix, String line) {
        String ret = null;
        Pattern pattern = Pattern.compile("^" + prefix + "[ \\t]+(.*)", Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
            if (matcher.groupCount() == 1) {
                ret = matcher.group(1);
            }
        }

        return ret;
    }

    private void parseLanguageFile(Uri base) throws Exception {
        _filename = "/Language.txt";

        log(Log.INFO, "Parsing language file: " + base.toString() + _filename);

        InputStream languageFileStream = getFile(base, "Language.txt");
        LineNumberReader r = new LineNumberReader(new BufferedReader(new InputStreamReader(languageFileStream)));

        String languageName = "";
        boolean hasSpecialTraining = false;

        String line;
        while ((line = r.readLine()) != null) {
            _line = r.getLineNumber();
            String[] words = getWords(line, r);

            if (words.length == 0) continue;

            String keyword = words[0].trim().toLowerCase();

            switch (keyword) {
                case "gen":
                    if (words.length != 7) {
                        logError("Expected format: gen <task type> <task category> <task description> <list> <gen " +
                                 "rules>");
                        continue;
                    }

                    LanguageGenerator.Description description = new LanguageGenerator.Description();

                    if ((description.TaskType = TaskType.ValueOf(words[1])) == null) {
                        logError("Generator task type is not set");
                        continue;
                    }

                    description.Category = resolveReferences(words[2], _references);
                    description.Description = resolveReferences(words[3], _references);
                    String[] pattern = resolveReferences(words[4], _references).split("-", -1);
                    description.List = resolveReferences(words[5], _references).split(",", -1);
                    description.Rules = resolveReferences(words[6], _references).split(",", -1);

                    if (description.List.length != description.Rules.length) {
                        logError("Generator list os not the same as rules");
                        continue;
                    }

                    String regex = Arrays.stream(pattern)
                            .map(p -> p.isEmpty() ? "(.*?)" : "(" + p + ")")
                            .collect(Collectors.joining());

                    description.Pattern = Pattern.compile("^" + regex + "$");
                    description.Groups = pattern.length;

                    _generators.add(description);
                    break;
                case "ref":
                    if (words.length != 3) {
                        logError("Expected format: ref <name> <text>");
                        continue;
                    }

                    _references.put(words[1], words[2]);
                    break;
                case "task-config":
                    if (words.length != 5) {
                        logError("Expected format: task-config <task type> <task part> <value>");
                        continue;
                    }
                    TaskConfig taskConfig = new TaskConfig();

                    if ((taskConfig.Type = TaskType.ValueOf(words[1])) == null) {
                        logError("Config task type is not set");
                        continue;
                    }
                    if ((taskConfig.Part = words[2]) == null) {
                        logError("Config task part is not set");
                        continue;
                    }
                    if ((taskConfig.Value = words[3]) == null) {
                        logError("Config value is not set");
                        continue;
                    }
                    if ((taskConfig.Description = words[4]) == null) {
                        logError("Config description is not set");
                        continue;
                    }

                    _data.TaskConfig.add(taskConfig);
                    break;
                case "name":
                    if (!languageName.isEmpty()) {
                        logError("Language name repeats");
                        continue;
                    }
                    if (words.length < 2) {
                        logError("Language name is expected");
                        continue;
                    }

                    String suffix = getLine("name", line);

                    if (suffix == null) {
                        logError("Language is empty");
                        continue;
                    }

                    languageName = suffix;
                    break;
                case "image":
                    if (_data.Config.containsKey("image")) {
                        logError("Language image is already set");
                        continue;
                    }

                    if (words.length < 2) {
                        logError("Expected format: image <image url>");
                        continue;
                    }

                    String suffix1 = getLine("image", line);

                    if (suffix1 == null) {
                        logError("Image is empty");
                        continue;
                    }

                    String imageUrl = base + "/" + suffix1;

                    _data.Config.put("image-url", imageUrl);
                    if (_saveImages) {
                        Bitmap bitmap = Utilities.GetImage(imageUrl);

                        if (bitmap == null) {
                            logError("Cannot get image: " + imageUrl);
                            continue;
                        }

                        _data.Config.put("image", Utilities.BitmapToBase64(bitmap));
                    }
                    break;
                case "lesson":
                    if (words.length != 2) {
                        logError("Lesson expects filename");
                        continue;
                    }
                    if (_data.Lessons.containsKey(words[1])) {
                        logError("Duplicate lesson file: " + words[1]);
                        continue;
                    }

                    LessonWithAttrs l = new LessonWithAttrs();

                    l.Lesson.Id = words[1];

                    _data.Lessons.put(words[1], l);
                    break;
                case "theory":
                    if (words.length != 2) {
                        logError("Expecting format: theory <filename>");
                        continue;
                    }

                    TheoryWithChapters theory = new TheoryWithChapters();

                    theory.Theory.Id = words[1];
                    theory.Theory.Index = _data.Theory.size();

                    _data.Theory.put(words[1], theory);
                    break;
                case "training":
                    if (words.length != 4) {
                        logError("Expecting format: training <filename> <name> <description>");
                        continue;
                    }
                    if (words[1].isEmpty() && hasSpecialTraining) {
                        logError("Multiple trainings with no filename!");
                        continue;
                    }

                    if (words[1].isEmpty()) hasSpecialTraining = true;
                    TrainingWithTasks t = new TrainingWithTasks();

                    t.Training = new Training();
                    t.Training.Filename = words[1];
                    t.Training.Id = words[1];
                    t.Training.Name = words[2];
                    t.Training.Description = words[3];

                    _data.Trainings.put(words[1], t);
                    break;
                case "version":
                    if (words.length != 2) {
                        logError("Expecting format: version <version>");
                        continue;
                    }
                    if (!_data.LanguageVersion.isEmpty()) {
                        logError("Language version is set");
                        continue;
                    }

                    _data.LanguageVersion = words[1];
                    _data.Config.put("version", _data.LanguageVersion);
                    break;
                case "setting":
                    if (words.length != 5) {
                        logError("Expecting format: setting <key> <description> <type> <default value>");
                        continue;
                    }

                    Setting s = new Setting();

                    s.Key = resolveReferences(words[1], _references);
                    s.Description = resolveReferences(words[2], _references);

                    String type = resolveReferences(words[3], _references);

                    if ((s.Type = SettingType.ValueOf(type)) == null) {
                        logError("Unrecognised setting type: " + type);
                        continue;
                    }

                    s.Value = resolveReferences(words[4], _references);

                    _data.Settings.put(s.Key, s);
                    break;
                case "license":
                case "licence":
                    if (words.length < 2) {
                        logError("Expecting format: licence <text>");
                        continue;
                    }

                    String licenceText = Arrays.stream(words).skip(1).collect(Collectors.joining(" "));
                    _data.Licences.add(licenceText);
                    break;
                default:
                    logError("Unrecognised keyword in language file: " + keyword);
                    break;
            }
        }

        if (languageName.isEmpty()) {
            logError("Language name is not set");
        }

        if (!hasSpecialTraining) {
            log(Log.INFO, "No special *all* training"); // Only warn
        }

        log(Log.INFO, "Language file parsed");

        _languageFileParsed = true;
    }

    public boolean ParseLanguageFile(Uri base, Boolean throwException) {
        _throwError = throwException;

        try {
            parseLanguageFile(base);

            return _throwError || !_hasError;
        } catch (FileNotFoundException e) {
            log(Log.INFO, "File not found: " + e.getMessage());
        } catch (ParserException e) {
            log(Log.INFO, "Parsing error: " + e.getMessage());
        } catch (Exception e) {
            Log.e("TAG", "Error", e);
            log(Log.INFO, "Error: " + e);
        }

        return false;
    }

    public boolean ParseRepository(Uri base, Boolean throwExceptions) {
        _throwError = throwExceptions;

        try {
            if (!_languageFileParsed) parseLanguageFile(base);

            for (TheoryWithChapters t : _data.Theory.values()) {
                if (!t.Theory.Id.isEmpty()) t.Chapters.addAll(parseTheoryFile(base, t.Theory));
            }

            for (TrainingWithTasks t : _data.Trainings.values()) {
                if (!t.Training.Id.isEmpty()) t.Tasks.addAll(parseTrainingFile(base, t.Training));
            }

            for (LessonWithAttrs l : _data.Lessons.values()) {
                l.Tasks.addAll(parseLessonFile(base, l));

                for (Task task : l.Tasks) {

                    // Check task references
                    if (task.Type == TaskType.UnknownTask) {
                        Optional<Task> any = _data.Trainings.values()
                                .stream()
                                .flatMap(t -> t.Tasks.stream())
                                .filter(t -> t.Id.equals(task.Id))
                                .findAny();

                        if (!any.isPresent()) {
                            logError("Task " + task.Id + " is not defined!");
                        }
                    }
                }
            }

            return _throwError || !_hasError;
        } catch (FileNotFoundException e) {
            log(Log.INFO, "File not found: " + e.getMessage());
        } catch (ParserException e) {
            log(Log.INFO, "Parsing error: " + e.getMessage());
        } catch (Exception e) {
            Log.e("TAG", "Error", e);
            log(Log.INFO, "Error: " + e);
        }

        return false;
    }

    private Collection<Chapter> parseTheoryFile(Uri base, Theory theory) throws Exception {
        _filename = theory.Id;

        log(Log.INFO, "Parsing theory file: " + _filename);

        InputStream languageFileStream = getFile(base, _filename);
        LineNumberReader r = new LineNumberReader(new BufferedReader(new InputStreamReader(languageFileStream)));
        Map<String, Chapter> theoryChapters = new HashMap<>();
        Map<String, String> references = new HashMap<>();

        String line;
        while ((line = r.readLine()) != null) {
            _line = r.getLineNumber();
            String[] words = getWords(line, r);

            if (words.length == 0) continue;

            String keyword = words[0].trim().toLowerCase();

            switch (keyword) {
                case "ref":
                    if (words.length != 3) {
                        logError("Expected format: ref <name> <text>");
                        continue;
                    }

                    references.put(words[1], words[2]);
                    break;
                case "description":
                    if (words.length != 2) {
                        logError("Expected format: description <description>");
                        continue;
                    }

                    theory.Description = resolveReferences(words[1], references);
                    break;
                case "title":
                    if (words.length != 2) {
                        logError("Expected format: title <title>");
                        continue;
                    }

                    if (!theory.Title.isEmpty()) {
                        logError("Theory title is alrady set!");
                        continue;
                    }

                    theory.Title = resolveReferences(words[1], references);
                    break;
                case "chapter":
                    if (words.length != 4) {
                        logError("Expected format: chapter <id> <explanation> <translation>");
                        continue;
                    }

                    Chapter chapter = new Chapter();

                    chapter.Id = words[1];
                    chapter.Explanation = resolveReferences(words[2], references);
                    chapter.Translation = resolveReferences(words[3], references);

                    theoryChapters.put(chapter.Id, chapter);
                    break;
                default:
                    logError("Unrecognised keyword in theory file: " + keyword);
            }

        }

        log(Log.INFO, "Theory file " + theory.Id + " parsed");

        return theoryChapters.values();
    }

    public ParserData GetData() { return _data; }

    public List<LanguagePortal> ParsePortals(List<Pair<String, String>> portals) throws Exception {
        List<LanguagePortal> result = new ArrayList<>();

        for (Pair<String, String> portal : portals) {

            log(Log.INFO, "Parsing portal " + portal.first);

            Uri portalUri = Uri.parse(portal.second);
            InputStream languageFileStream = getFile(portalUri, "Languages.txt");
            LineNumberReader r = new LineNumberReader(new BufferedReader(new InputStreamReader(languageFileStream)));

            LanguagePortal languagePortal = new LanguagePortal();

            languagePortal.Location = portal.second;

            String line;
            while ((line = r.readLine()) != null) {
                _line = r.getLineNumber();
                String[] words = getWords(line, r);

                if (words.length == 0) continue;

                String keyword = words[0].trim().toLowerCase();

                switch (keyword) {
                    case "name":
                        if (words.length != 2) {
                            logError("Expected format: name <name>");
                            continue;
                        }

                        if (!languagePortal.Name.isEmpty()) {
                            logError("Portal name is set: " + languagePortal.Name);
                            continue;
                        }

                        languagePortal.Name = words[1];
                        break;
                    case "language":
                        if (words.length != 4) {
                            logError("Expected format: language <name> <directory> <image>");
                            continue;
                        }

                        Language language = new Language();

                        language.Name = words[1];
                        language.Location = words[2];
                        language.Image = words[3];

                        if (!language.Location.startsWith(portal.second)) {
                            language.Location = portal.second + "/" + language.Location;
                        }
                        if (!language.Image.startsWith(portal.second)) {
                            language.Image = portal.second + "/" + language.Image;
                        }

                        languagePortal.Languages.add(language);
                        break;
                    default:
                        logError("Unrecognised keyword in portal file: " + keyword);
                }
            }

            if (languagePortal.Name.isEmpty()) languagePortal.Name = portal.first;
            result.add(languagePortal);
        }

        return result;
    }

    public interface ParserInterface {
        InputStream GetFile(Uri base, String filename) throws IOException;

        void Inform(int type, String message);
    }

    public static class ParserException extends Exception {
        public ParserException(String file, int line, String message) {
            super(file + ":" + line + ":" + message);
        }
    }

    public static class ParserData {
        public Map<String, Setting> Settings = new HashMap<>();
        public Map<String, TrainingWithTasks> Trainings = new HashMap<>();
        public Map<String, LessonWithAttrs> Lessons = new HashMap<>();
        public Map<String, TheoryWithChapters> Theory = new HashMap<>();
        public List<String> Licences = new ArrayList<>();
        public String LanguageVersion = "";
        public List<TaskConfig> TaskConfig = new ArrayList<>();
        public Map<String, String> Config = new HashMap<>();
    }

    public static class LanguagePortal {
        public String Name = "";
        public String Location = "";
        public List<Language> Languages = new ArrayList<>();

        public LanguagePortal() {

        }

        public LanguagePortal(String name, String location) {
            Name = name;
            Location = location;
        }
    }

    public static class Language {
        public String Name = "";
        public String Location = "";
        public String Image = "";
    }
}
