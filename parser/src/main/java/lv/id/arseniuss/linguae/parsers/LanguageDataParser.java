package lv.id.arseniuss.linguae.parsers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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
import lv.id.arseniuss.linguae.entities.Chapter;
import lv.id.arseniuss.linguae.entities.Language;
import lv.id.arseniuss.linguae.entities.Lesson;
import lv.id.arseniuss.linguae.entities.License;
import lv.id.arseniuss.linguae.entities.Repository;
import lv.id.arseniuss.linguae.entities.Setting;
import lv.id.arseniuss.linguae.entities.Theory;
import lv.id.arseniuss.linguae.entities.Training;
import lv.id.arseniuss.linguae.entities.TrainingCategory;
import lv.id.arseniuss.linguae.enumerators.SettingType;
import lv.id.arseniuss.linguae.enumerators.TaskType;
import lv.id.arseniuss.linguae.generators.LanguageGenerator;
import lv.id.arseniuss.linguae.tasks.ChooseTask;
import lv.id.arseniuss.linguae.tasks.ConjugateTask;
import lv.id.arseniuss.linguae.tasks.DeclineTask;
import lv.id.arseniuss.linguae.tasks.SelectTask;
import lv.id.arseniuss.linguae.tasks.Task;
import lv.id.arseniuss.linguae.tasks.TranslateTask;

public class LanguageDataParser {
    final String _gen_prefix = "@gen";
    final String _eol_prefix = "EOL";

    private final ParserData _data = new ParserData();
    private final ParserInterface _parserInterface;
    private final Map<String, String> _references = new HashMap<>();
    private final List<LanguageGenerator.Description> _generators = new ArrayList<>();
    private final Pattern _referencePattern = Pattern.compile("&([a-zA-Z0-9_-]+)");
    private final String[] _languageCodes;
    private boolean _saveImages = false;
    private boolean _throwError = false;
    private int _line = 0;
    private List<String> _filenames = new ArrayList<>();
    private boolean _hasError = false;
    private boolean _languageFileParsed = false;

    public LanguageDataParser(ParserInterface parserInterface, String[] languageCodes) {
        _parserInterface = parserInterface;
        _languageCodes = languageCodes;
    }

    public LanguageDataParser(ParserInterface parserInterface, String[] languageCodes,
                              boolean saveImages) {
        _parserInterface = parserInterface;
        _saveImages = saveImages;
        _languageCodes = languageCodes;
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

    private InputStream getFile(String filename) throws Exception {
        return _parserInterface.GetFile(filename);
    }

    private void logError(String error) throws ParserException {
        String filename = _filenames.get(_filenames.size() - 1);

        error = "[E] " + error;

        if (_throwError) {
            throw new ParserException(filename, _line, error);
        } else {
            log(Log.ERROR, filename + ":" + _line + ": " + error);
            _hasError = true;
        }
    }

    private String[] performGeneration(String baseText, String generatorText, String[] options,
                                       Task task,
                                       List<LanguageGenerator.Description> generators) throws
            ParserException {
        List<LanguageGenerator.Description> descriptions = generators.stream()
                .filter(d -> d.TaskType == task.Type && Objects.equals(d.Category, task.Category) &&
                        (Objects.equals(d.Description, task.Description) ||
                                d.Description.isEmpty()))
                .collect(Collectors.toList());

        if (descriptions.isEmpty()) {
            descriptions = _generators.stream()
                    .filter(d -> d.TaskType == task.Type &&
                            Objects.equals(d.Category, task.Category) &&
                            Objects.equals(d.Description, task.Description))
                    .collect(Collectors.toList());
        }

        if (descriptions.isEmpty()) {
            logError(
                    "There is no generator for " + task.Type.GetName() + " " + task.Category + " " +
                            task.Description);
            return null;
        }

        String[] generated = null;
        String error = "";

        if (generatorText.contains(":")) {
            baseText = generatorText.substring(_gen_prefix.length() + 1); // +1 for ':'
        } else {
            baseText = Utilities.ExtractLinkTitles(baseText);
        }

        for (LanguageGenerator.Description description : descriptions) {
            try {
                generated = LanguageGenerator.Generate(description, task, baseText, options);
                break;
            } catch (LanguageGenerator.GeneratorException e) {
                error = e.getMessage();
            }
        }

        if (generated == null) {
            logError("Could not generate answers for " + baseText + ": " + error);
        }

        return generated;
    }

    private boolean parseTask(Task task, String line, String[] words,
                              Map<String, String> references,
                              List<LanguageGenerator.Description> generators, String edit) throws
            ParserException {
        if (words.length < 3) {
            logError("Expecting format: task <id> <task-type> <task-data>");
            logError("Got: " + line);
            return false;
        }

        task.Id = _filenames.get(0) + "#" + words[1];
        if (!edit.isEmpty()) task.Id += "-" + edit;

        String taskType = words[2].trim().toLowerCase();

        final String formatPrefix = "task <id> " + taskType;

        switch (taskType) {
            case "select":
                if (words.length != 3 + 4) {
                    logError("Expected format: " + formatPrefix +
                            " <meaning> <sentence> <answers> <options>");
                    logError("Got: " + line);
                    return false;
                }

                SelectTask selectTask = new SelectTask();

                task.Type = TaskType.SelectTask;

                task.Category = null;
                task.Description = null;

                selectTask.Meaning = resolveReferences(words[3], references);

                String sentence = resolveReferences(words[4], references);

                selectTask.Sentence = Arrays.stream(sentence.split(" "))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new);

                long selectedCount = Arrays.stream(selectTask.Sentence)
                        .filter(w -> w.startsWith("<") && w.endsWith(">"))
                        .count();

                String answers = resolveReferences(words[5], references);

                selectTask.Answers = answers.split(",");

                long answerCount = Arrays.stream(selectTask.Answers).count();

                if (selectedCount != answerCount) {
                    logError("Answer count doesn't match number of words to be selected " +
                            selectedCount + "/" + answerCount);
                    return false;
                }

                String options = resolveReferences(words[6], references);

                selectTask.Options = Arrays.stream(options.split(","))
                        .map(String::trim)
                        .filter(o -> !o.isEmpty())
                        .toArray(String[]::new);


                task.Amount = selectTask.Answers.length;
                task.Data = selectTask;
                break;
            case "decline":
                if (words.length != 3 + 6) {
                    logError("Expecting format: " + formatPrefix +
                            " <type> <description> <word> <meaning> <options> " + "<answers>");
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
                    String[] generated =
                            performGeneration(declineTask.Word, references2, declineTask.Cases,
                                    task, generators);

                    if (generated == null) return false;

                    declineTask.Answers = generated;
                } else {
                    declineTask.Answers = references2.split(",", -1);

                    if (declineTask.Cases.length != declineTask.Answers.length) {
                        logError("Decline task case count is not the same as answers: " +
                                declineTask.Cases.length + "/" + declineTask.Answers.length);
                        return false;
                    }
                }

                task.Amount = declineTask.Answers.length;
                task.Data = declineTask;
                break;
            case "conjugate":
                if (words.length != 3 + 6) {
                    logError("Expecting format: " + formatPrefix +
                            " <conjugation> \"<mood> <tense> <voice>\" <word> " +
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
                    String[] generated = performGeneration(conjugateTask.Verb, references4,
                            conjugateTask.Persons, task, generators);

                    if (generated == null) return false;

                    conjugateTask.Answers = generated;
                } else {
                    conjugateTask.Answers = references4.split(",", -1);
                }

                if (conjugateTask.Persons.length != conjugateTask.Answers.length) {
                    logError("Conjugate task person count is not the same as answers: " +
                            conjugateTask.Persons.length + "/" + conjugateTask.Answers.length);
                    return false;
                }

                task.Amount = conjugateTask.Answers.length;
                task.Data = conjugateTask;
                break;
            case "choose":
                if (words.length != 3 + 6) {
                    logError("Expecting format: " + formatPrefix +
                            " <category> <subcategory> <description> <word> <answer> <additionals>");
                    logError("Got: " + line);
                    return false;
                }

                ChooseTask chooseTask = new ChooseTask();

                task.Type = TaskType.ChooseTask;
                task.Category = resolveReferences(words[3], references);
                task.Description = resolveReferences(words[4], references);

                chooseTask.Description = resolveReferences(words[5], references);
                chooseTask.Word = resolveReferences(words[6], references);
                chooseTask.Answer = resolveReferences(words[7], references);
                chooseTask.Additionals = resolveReferences(words[8], references);

                task.Amount = 1;
                task.Data = chooseTask;
                break;
            case "translate":
                if (words.length != 6) {
                    logError("Expecting format: " + formatPrefix + " <text> <answer> <additional>");
                    logError("Got: " + line);
                    return false;
                }

                TranslateTask translateTask = new TranslateTask();

                task.Type = TaskType.TranslateTask;
                task.Category = null;
                task.Description = null;

                translateTask.Text = resolveReferences(words[3], references);

                translateTask.Answer =
                        Arrays.stream(resolveReferences(words[4], references).split(" "))
                                .map(a -> a.replaceAll("[,.]", ""))
                                .toArray(String[]::new);
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

    private String lookForReference(String ref, Map<String, String> references) {
        String replacement = null;

        if (references.containsKey(ref)) {
            replacement = references.get(ref);
        } else {
            if (_references.containsKey(ref)) {
                replacement = _references.get(ref);
            }
        }

        return replacement;
    }

    private String resolveReferences(String word, Map<String, String> references) throws
            ParserException {

        if (!word.isEmpty()) {
            Matcher matcher = _referencePattern.matcher(word);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String ref = matcher.group(1);
                String replacement = lookForReference(ref, references);

                if (replacement == null) {
                    for (String languageCode : _languageCodes) {
                        replacement = lookForReference(ref + "-" + languageCode, references);
                        if (replacement != null) break;
                    }
                }

                if (replacement == null) {
                    logError("Cannot find reference: '" + word + "'");
                    return word;
                }

                matcher.appendReplacement(result, replacement);
            }
            matcher.appendTail(result);

            return result.toString();
        }

        return word;
    }

    private String[] getWords(String line, LineNumberReader r) throws Exception {
        int commentStart = line.contains("#") ? line.indexOf("#") : line.length();

        line = line.substring(0, commentStart);

        while (line.endsWith("\\")) {
            String nextLine = r.readLine().trim();

            commentStart = nextLine.contains("#") ? nextLine.indexOf("#") : nextLine.length();
            line = line.substring(0, line.length() - 1);
            line += nextLine.substring(0, commentStart);
        }

        String[] words = LineParser.Split(line);

        words = takeWhile(words, w -> !w.startsWith("#")).toArray(new String[0]);

        if (words.length != 0) {
            String last = words[words.length - 1];

            if (last.equals("<" + _eol_prefix)) {
                String l = r.readLine();
                List<String> lines = new ArrayList<>();

                while (!l.startsWith(_eol_prefix)) {
                    if (l.startsWith("\t")) l = l.substring(1);
                    else if (l.startsWith("    ")) l = l.substring(4);

                    lines.add(l);
                    l = r.readLine();
                }

                words[words.length - 1] = String.join("\n", lines);

                String[] next = getWords(l.substring(_eol_prefix.length()), r);

                words = Stream.concat(Arrays.stream(words), Arrays.stream(next))
                        .toArray(String[]::new);
            }
        }

        return words;
    }

    private void parseIncludeInTrainingFile(int includes, String base, String includeFile,
                                            String trainingId, Map<String, Task> tasks,
                                            Map<String, String> references,
                                            List<LanguageGenerator.Description> generators) throws
            Exception {
        log(Log.INFO, "Parsing include file: " + includeFile);

        InputStream lessonFileStream = getFile(base + "/" + includeFile);
        LineNumberReader r =
                new LineNumberReader(new BufferedReader(new InputStreamReader(lessonFileStream)));

        String line;

        while ((line = r.readLine()) != null) {
            _line = r.getLineNumber();
            String[] words = getWords(line, r);

            if (words.length == 0) continue;

            String keyword = words[0].trim().toLowerCase();

            if (keyword.equals("stop") && words.length == 1) break;
            if (keyword.equals("skip")) continue;

            switch (keyword) {
                case "gen":
                    if (!parseGeneratorDecl(words, references, generators)) continue;
                    break;
                case "ref":
                    if (words.length != 3) {
                        logError("Expected format: ref <name> <text>");
                        continue;
                    }

                    String resolved = resolveReferences(words[2], references);

                    references.put(words[1], resolved);
                    break;
                case "task":
                    Task task = new Task();

                    if (!parseTask(task, line, words, references, generators, "i" + includes))
                        continue;

                    if (tasks.containsKey(task.Id)) {
                        logError("Task " + task.Id + " already exists!");
                        continue;
                    }

                    tasks.put(task.Id, task);
                    break;
                case "category":
                    if (words.length != 4) {
                        logError("Expected format: category <task type> <category> <description>");
                        continue;
                    }

                    TrainingCategory trainingCategory = new TrainingCategory();

                    if ((trainingCategory.Task = TaskType.FromName(words[1])) == null) {
                        logError("Unrecognized training category: " + words[1]);
                        continue;
                    }

                    trainingCategory.TrainingId = trainingId;
                    trainingCategory.Category = resolveReferences(words[2], references);
                    trainingCategory.Description = resolveReferences(words[3], references);

                    _data.TrainingCategories.add(trainingCategory);
                    break;
                default:
                    logError("Unrecognized keyword in " + includeFile + ": " + keyword);
                    break;
            }
        }
    }

    private List<Task> parseTrainingFile(String base, Training t) throws Exception {
        log(Log.INFO, "Parsing training file: " + t.Id);

        InputStream trainingFileStream = getFile(base + "/" + t.Id);
        LineNumberReader r =
                new LineNumberReader(new BufferedReader(new InputStreamReader(trainingFileStream)));

        Map<String, Task> tasks = new HashMap<>();
        Map<String, String> references = new HashMap<>();
        List<LanguageGenerator.Description> generators = new ArrayList<>();
        int includes = 0;

        String line;

        while ((line = r.readLine()) != null) {
            _line = r.getLineNumber();
            String[] words = getWords(line, r);

            if (words.length == 0) continue;

            String keyword = words[0].trim().toLowerCase();

            if (keyword.equals("stop") && words.length == 1) break;
            if (keyword.equals("skip")) continue;

            switch (keyword) {
                case "gen":
                    if (!parseGeneratorDecl(words, references, generators)) continue;
                    break;
                case "name":
                    if (words.length != 2) {
                        logError("Expected format: name <name>");
                        continue;
                    }

                    if (!t.Name.isEmpty()) {
                        logError("Training name is already set");
                        continue;
                    }

                    t.Name = resolveReferences(words[1], references);
                    break;
                case "description":
                    if (words.length != 2) {
                        logError("Expected format: description <description>");
                        continue;
                    }

                    if (!t.Description.isEmpty()) {
                        logError("Training description is already set");
                        continue;
                    }

                    t.Description = resolveReferences(words[1], references);
                    break;
                case "ref":
                    if (words.length != 3) {
                        logError("Expected format: ref <name> <text>");
                        continue;
                    }

                    String resolved = resolveReferences(words[2], references);

                    references.put(words[1], resolved);
                    break;
                case "task":
                    Task task = new Task();

                    if (!parseTask(task, line, words, references, generators, "")) continue;

                    if (tasks.containsKey(task.Id)) {
                        logError("Task " + task.Id + " already exists!");
                        continue;
                    }

                    tasks.put(task.Id, task);
                    break;
                case "category":
                    if (words.length != 4) {
                        logError("Expected format: category <task type> <category> <description>");
                        continue;
                    }

                    TrainingCategory trainingCategory = new TrainingCategory();

                    if ((trainingCategory.Task = TaskType.FromName(words[1])) == null) {
                        logError("Unrecognized training category: " + words[1]);
                        continue;
                    }

                    trainingCategory.TrainingId = t.Id;
                    trainingCategory.Category = resolveReferences(words[2], references);
                    trainingCategory.Description = resolveReferences(words[3], references);

                    _data.TrainingCategories.add(trainingCategory);
                    break;
                case "include":
                    if (words.length != 2) {
                        logError("Expecting format: include <filename>");
                        continue;
                    }

                    includes += 1;
                    _filenames.add(words[1]);
                    parseIncludeInTrainingFile(includes, base, words[1], t.Id, tasks, references,
                            generators);
                    _filenames.remove(_filenames.size() - 1);
                    break;
                default:
                    logError("Unrecognized keyword in " + t.Id + ": " + keyword);
                    break;
            }
        }

        for (TrainingCategory trainingCategory : _data.TrainingCategories) {
            boolean found = false;

            if (!Objects.equals(trainingCategory.TrainingId, t.Id))
                break;

            for (Task task : tasks.values()) {
                if (Objects.equals(task.Category, trainingCategory.Category) &&
                        Objects.equals(task.Description, trainingCategory.Description) &&
                        task.Type == trainingCategory.Task) {
                    found = true;
                    break;
                }
            }

            if (!found) logError("Category for " + trainingCategory.Task.GetName() + " task (" +
                    trainingCategory.Category + ", " + trainingCategory.Description +
                    ") has no tasks!");

        }

        log(Log.INFO, "Training file " + t.Id + " parsed");

        return new ArrayList<>(tasks.values());
    }

    private boolean parseGeneratorDecl(String[] words, Map<String, String> references,
                                       List<LanguageGenerator.Description> generators) throws
            Exception {
        if (words.length != 7) {
            logError(
                    "Expected format: gen <task type> <task category> <task description> <list> <gen " +
                            "rules>");
            return false;
        }

        LanguageGenerator.Description description = new LanguageGenerator.Description();

        if ((description.TaskType = TaskType.FromName(words[1])) == null) {
            logError("Generator task type is not set");
            return false;
        }

        description.Category = resolveReferences(words[2], references);
        description.Description = resolveReferences(words[3], references);
        String[] pattern = resolveReferences(words[4], references).split("-", -1);
        description.List = resolveReferences(words[5], references).split(",", -1);
        description.Rules = resolveReferences(words[6], references).split(",", -1);

        if (description.List.length != description.Rules.length) {
            logError("Generator list is not the same as rules: " + description.List.length + "/" +
                    description.Rules.length);
            return false;
        }

        String regex = Arrays.stream(pattern)
                .map(p -> p.isEmpty() ? "(.*?)" : "(" + p + ")")
                .collect(Collectors.joining());

        description.Pattern = Pattern.compile("^" + regex + "$");
        description.Groups = pattern.length;

        generators.add(description);

        return true;
    }

    private void parseIncludeInLessonFile(String base, String includeFile, Map<String, Task> tasks,
                                          Map<String, String> references,
                                          List<LanguageGenerator.Description> generators) throws
            Exception {
        log(Log.INFO, "Parsing include file: " + includeFile);

        InputStream lessonFileStream = getFile(base + "/" + includeFile);
        LineNumberReader r =
                new LineNumberReader(new BufferedReader(new InputStreamReader(lessonFileStream)));

        String line;

        while ((line = r.readLine()) != null) {
            _line = r.getLineNumber();
            String[] words = getWords(line, r);

            if (words.length == 0) continue;

            String keyword = words[0].trim().toLowerCase();

            if (keyword.equals("stop") && words.length == 1) break;
            if (keyword.equals("skip")) continue;

            switch (keyword) {
                case "gen":
                    if (!parseGeneratorDecl(words, references, generators)) continue;
                    break;
                case "ref":
                    if (words.length != 3) {
                        logError("Expected format: ref <name> <text>");
                        continue;
                    }

                    String resolved = resolveReferences(words[2], references);

                    references.put(words[1], resolved);
                    break;
                case "task":
                    Task task = new Task();

                    if (!parseTask(task, line, words, references, generators, "")) continue;

                    if (tasks.containsKey(task.Id)) {
                        logError("Task " + task.Id + " already exists!");
                        continue;
                    }

                    tasks.put(task.Id, task);
                    break;
                default:
                    logError("Unrecognised keyword in include file: " + keyword);
            }
        }
    }

    private Collection<Task> parseLessonFile(String base, Lesson l) throws Exception {
        log(Log.INFO, "Parsing lesson file " + l.Id);

        InputStream lessonFileStream = getFile(base + "/" + l.Id);
        LineNumberReader r =
                new LineNumberReader(new BufferedReader(new InputStreamReader(lessonFileStream)));
        Map<String, Task> lessonTasks = new HashMap<>();
        Map<String, String> references = new HashMap<>();
        List<LanguageGenerator.Description> generators = new ArrayList<>();

        String line;

        while ((line = r.readLine()) != null) {
            _line = r.getLineNumber();
            String[] words = getWords(line, r);

            if (words.length == 0) continue;

            String keyword = words[0].trim().toLowerCase();

            if (keyword.equals("stop") && words.length == 1) break;
            if (keyword.equals("skip")) continue;

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

                    String resolved = resolveReferences(words[2], references);

                    references.put(words[1], resolved);
                    break;
                case "name":
                    if (words.length != 2) {
                        logError("Expected format: name <name>  ");
                        continue;
                    }

                    if (!l.Name.isEmpty()) {
                        logError("Lesson name is already set");
                        continue;
                    }

                    l.Name = resolveReferences(words[1], references);
                    break;
                case "section":
                    if (words.length != 2) {
                        logError("Expected format: section <name>");
                        continue;
                    }

                    if (!l.Section.isEmpty()) {
                        logError("Lesson's section is already set");
                        continue;
                    }

                    l.Section = resolveReferences(words[1], references);
                    break;
                case "description":
                    if (!l.Description.isEmpty()) {
                        logError("Lesson description is already set");
                        continue;
                    }

                    l.Description = resolveReferences(words[1], references);
                    break;
                case "task":
                    Task task = new Task();

                    if (!parseTask(task, line, words, references, generators, "")) continue;

                    if (lessonTasks.containsKey(task.Id)) {
                        logError("Task " + task.Id + " already exists!");
                        continue;
                    }

                    lessonTasks.put(task.Id, task);
                    break;
                case "chapter":
                    if (words.length != 3) {
                        logError("Expected format: chapter <id> <text>");
                        continue;
                    }

                    Chapter chapter = new Chapter();

                    chapter.Id = _filenames.get(0) + "#" + words[1];
                    chapter.Text = resolveReferences(words[2], references);

                    l.Chapters.put(chapter.Id, chapter);
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
                case "include":
                    if (words.length != 2) {
                        logError("Expecting format: include <filename>");
                        continue;
                    }

                    _filenames.add(words[1]);
                    parseIncludeInLessonFile(base, words[1], lessonTasks, references, generators);
                    _filenames.remove(_filenames.size() - 1);
                    break;
                case "gen":
                    if (!parseGeneratorDecl(words, references, generators)) continue;
                    break;
                default:
                    logError("Unrecognised keyword in lesson file: " + keyword);
            }
        }

        log(Log.INFO, "Lesson file " + l.Id + " parsed");

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

    private void parseIncludeInLanguageFile(String base, LineNumberReader r,
                                            int recursion) throws Exception {
        String line;

        while ((line = r.readLine()) != null) {
            _line = r.getLineNumber();
            String[] words = getWords(line, r);

            if (words.length == 0) continue;

            String keyword = words[0].trim().toLowerCase();

            if (keyword.equals("stop") && words.length == 1) break;
            if (keyword.equals("skip")) continue;

            switch (keyword) {
                case "include":
                    if (words.length != 2) {
                        logError("Expecting format: include <filename>");
                        continue;
                    }

                    if (recursion > 5) {
                        logError("Too deep recursion");
                        continue;
                    }

                    String filename = words[1];
                    InputStream languageFileStream = getFile(base + "/" + filename);
                    LineNumberReader r1 = new LineNumberReader(
                            new BufferedReader(new InputStreamReader(languageFileStream)));

                    _filenames.add(filename);
                    parseIncludeInLanguageFile(base, r1, recursion + 1);
                    _filenames.remove(_filenames.size() - 1);
                    break;
                case "gen":
                    if (!parseGeneratorDecl(words, _references, _generators)) continue;
                    break;
                case "ref":
                    if (words.length != 3) {
                        logError("Expected format: ref <name> <text>");
                        continue;
                    }

                    String resolved = resolveReferences(words[2], _references);

                    _references.put(words[1], resolved);
                    break;
                case "name":
                    if (!_data.LanguageName.isEmpty()) {
                        logError("Language name repeats");
                        continue;
                    }
                    if (words.length < 2) {
                        logError("Language name is expected");
                        continue;
                    }

                    String name = getLine("name", line);

                    if (name == null) {
                        logError("Language is empty");
                        continue;
                    }

                    name = resolveReferences(name, _references);

                    _data.LanguageName = name;
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
                        InputStream inputStream = getFile(imageUrl);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                        if (bitmap == null) {
                            logError("Cannot get image: " + imageUrl);
                            continue;
                        }

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] bytes = stream.toByteArray();

                        _data.Config.put("image", Base64.encodeToString(bytes, Base64.DEFAULT));
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

                    Lesson l = new Lesson();

                    l.Id = words[1];
                    l.Index = _data.Lessons.size();

                    _data.Lessons.put(words[1], l);
                    break;
                case "theory":
                    if (words.length != 2) {
                        logError("Expecting format: theory <filename>");
                        continue;
                    }

                    Theory theory = new Theory();

                    theory.Id = words[1];
                    theory.Index = _data.Theory.size();

                    _data.Theory.put(words[1], theory);
                    break;
                case "training":
                    if (words.length != 2) {
                        logError("Expecting format: training <filename>");
                        continue;
                    }

                    Training t = new Training();

                    t.Id = words[1];
                    t.Index = _data.Trainings.size();

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
                /* general configuration */
                case "author":
                case "code":
                case "translation":
                case "bugs":
                    if (words.length != 2) {
                        logError("Expecting format: " + keyword + " <text>");
                        continue;
                    }

                    if (_data.Config.containsKey(keyword)) {
                        logError("Config " + keyword + " already exists!");
                        continue;
                    }

                    if (keyword.equals("code")) {
                        _data.LanguageCode = words[1];
                    }

                    _data.Config.put(keyword, words[1]);
                    break;
                case "setting":
                    if (words.length != 5) {
                        logError(
                                "Expecting format: setting <key> <description> <type> <default value>");
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
                    if (words.length < 3) {
                        logError("Expecting format: licence <id> <text>");
                        continue;
                    }

                    License license = new License();

                    license.Id = words[1];
                    license.Text = resolveReferences(words[2], _references);

                    _data.Licences.add(license);
                    break;
                default:
                    logError("Unrecognised keyword in language file: " + keyword);
                    break;
            }
        }
    }

    private void parseLanguageFile(String base) throws Exception {
        final String LanguageFile = "Language.txt";

        _filenames.add(LanguageFile);

        log(Log.INFO, "Parsing language file: " + base + "/" + LanguageFile);

        _data.LanguageUrl = base;

        InputStream languageFileStream = getFile(base + "/" + LanguageFile);
        LineNumberReader r =
                new LineNumberReader(new BufferedReader(new InputStreamReader(languageFileStream)));

        parseIncludeInLanguageFile(base, r, 0);

        if (_data.LanguageName.isEmpty()) {
            logError("Language name is not set");
        }

        log(Log.INFO, "Language file parsed");

        _filenames.remove(_filenames.size() - 1);
        _languageFileParsed = true;
    }

    public boolean ParseLanguageFile(String base, Boolean throwException) {
        _throwError = throwException;

        try {
            parseLanguageFile(base);

            return _throwError || !_hasError;
        } catch (FileNotFoundException e) {
            log(Log.INFO, _filenames.get(_filenames.size() - 1) + ":" + _line + "file not found: " +
                    e.getMessage());
        } catch (ParserException e) {
            log(Log.INFO, _filenames.get(_filenames.size() - 1) + ":" + _line + "parsing error: " +
                    e.getMessage());
        } catch (Exception e) {
            Log.e("TAG", "Error", e);
            log(Log.INFO, _filenames.get(_filenames.size() - 1) + ":" + _line + ": error:" + e);
        }

        return false;
    }

    public boolean ParseRepository(String base, Boolean throwExceptions) {
        _throwError = throwExceptions;

        try {
            if (!_languageFileParsed) parseLanguageFile(base);

            for (Theory t : _data.Theory.values()) {
                if (!t.Id.isEmpty()) {
                    _filenames.add(t.Id);
                    t.Chapters.addAll(parseTheoryFile(base, t));
                    _filenames.remove(_filenames.size() - 1);
                }
            }

            for (Training t : _data.Trainings.values()) {
                if (!t.Id.isEmpty()) {
                    _filenames.add(t.Id);
                    t.Tasks.addAll(parseTrainingFile(base, t));
                    _filenames.remove(_filenames.size() - 1);
                }
            }

            for (Lesson l : _data.Lessons.values()) {
                _filenames.add(l.Id);
                l.Tasks.addAll(parseLessonFile(base, l));
                _filenames.remove(_filenames.size() - 1);

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
            log(Log.INFO, _filenames.get(_filenames.size() - 1) + ":" + _line + ": error:" + e);
        }

        return false;
    }

    private Collection<Chapter> parseTheoryFile(String base, Theory theory) throws Exception {
        log(Log.INFO, "Parsing theory file: " + theory.Id);

        InputStream languageFileStream = getFile(base + "/" + theory.Id);
        LineNumberReader r =
                new LineNumberReader(new BufferedReader(new InputStreamReader(languageFileStream)));
        Map<String, Chapter> theoryChapters = new HashMap<>();
        Map<String, String> references = new HashMap<>();

        String line;
        while ((line = r.readLine()) != null) {
            _line = r.getLineNumber();
            String[] words = getWords(line, r);

            if (words.length == 0) continue;

            String keyword = words[0].trim().toLowerCase();

            if (keyword.equals("stop") && words.length == 1) break;
            if (keyword.equals("skip")) continue;

            switch (keyword) {
                case "ref":
                    if (words.length != 3) {
                        logError("Expected format: ref <name> <text>");
                        continue;
                    }

                    String resolved = resolveReferences(words[2], references);

                    references.put(words[1], resolved);
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
                    if (words.length != 3) {
                        logError("Expected format: chapter <id> <text>");
                        continue;
                    }

                    Chapter chapter = new Chapter();

                    chapter.Id = words[1];
                    chapter.Text = resolveReferences(words[2], references);

                    theoryChapters.put(chapter.Id, chapter);
                    break;
                default:
                    logError("Unrecognised keyword in theory file: " + keyword);
            }

        }

        log(Log.INFO, "Theory file " + theory.Id + " parsed");

        return theoryChapters.values();
    }

    public ParserData GetData() {
        return _data;
    }

    public Repository ParseRepository(String location) {
        Repository repository = new Repository();

        repository.Location = location;

        try {
            InputStream languageFileStream = getFile(location + "/Languages.txt");
            LineNumberReader r = new LineNumberReader(
                    new BufferedReader(new InputStreamReader(languageFileStream)));

            String line;
            while ((line = r.readLine()) != null) {
                _line = r.getLineNumber();
                String[] words = getWords(line, r);

                if (words.length == 0) continue;

                String keyword = words[0].trim().toLowerCase();

                if (keyword.equals("stop") && words.length == 1) break;
                if (keyword.equals("skip")) continue;

                switch (keyword) {
                    case "name":
                        if (words.length != 2) {
                            logError("Expected format: name <name>");
                            continue;
                        }

                        repository.Name = words[1];
                        break;
                    case "language":
                        if (words.length != 5) {
                            logError("Expected format: language <name> <code> <directory> <image>");
                            continue;
                        }

                        Language language = new Language();

                        language.Name = words[1];
                        language.Code = words[2];
                        language.Location = words[3];
                        language.Image = words[4];

                        if (!language.Location.startsWith(location)) {
                            language.Location = location + "/" + language.Location;
                        }
                        if (!language.Image.startsWith(location)) {
                            language.Image = location + "/" + language.Image;
                        }

                        repository.Languages.add(language);
                        break;
                    default:
                        logError("Unrecognised keyword in repository file: " + keyword);
                }
            }
        } catch (Exception e) {
            repository.Error = e.getLocalizedMessage();
        }

        return repository;
    }

    public interface ParserInterface {
        InputStream GetFile(String filename) throws Exception;

        void Inform(int type, String message);
    }

    public static class ParserException extends Exception {
        public ParserException(String file, int line, String message) {
            super(file + ":" + line + ":" + message);
        }
    }

    public static class ParserData {
        public String LanguageName = "";
        public String LanguageCode = "";
        public String LanguageVersion = "";
        public String LanguageUrl = "";

        public Map<String, Setting> Settings = new HashMap<>();
        public Map<String, Training> Trainings = new HashMap<>();
        public Map<String, Lesson> Lessons = new HashMap<>();
        public Map<String, Theory> Theory = new HashMap<>();
        public List<License> Licences = new ArrayList<>();

        public Map<String, String> Config = new HashMap<>();
        public List<TrainingCategory> TrainingCategories = new ArrayList<>();
    }
}
