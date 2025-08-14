# Universal language learning mobile application

_Sīc locūtus_ is a language-learning Android application inspired by exercises from various
textbooks. It was created to help drill vocabulary and grammar during commutes.

The project consists of three parts:

 - mobile application,
 - language content parser,
 - content repositories.

The app is language-agnostic, meaning that language content is separate from the app itself.
Teachers can create content for their subject according to specification and students may download
and parse content for usage locally. Features for various languages may be added over time.

The specification how content should be arranged can be found on another repository:
[linguae-data](https://github.com/arseniuss/linguae-data/tree/master/Specification).

The main idea is that students can complete various exercises, such as:

 - choose correct meaning of a word (choose task);
 - conjugate a verb (conjugate task);
 - decline a noun (decline task);
 - select correct name of an inflection (select task);
 - translate a sentence (translate task).

These exercises are grouped together in various ways. In lessons, exercises are compiled around
specific themes, "First Declension Nouns" or "Adjective Inflections", but in trainings these
exercises are combined for various drills like "Vocabulary" - only choose tasks.

Other extras can be found in feature section.

## Features

**Important.** Until the first "big" release all features can change and can be dropped.

### Content separation from app

Most important feature (and the main reason for building the app) is content separation from the app.
I wanted to have content written in declarative way in text files which could be accessed from
various places. That way teachers could write their lecture content and keep copyrights safe
while students may access it.

Also I wanted for the app to be free and open source so other could suggest not only improvements
in language lessons but also in the app.

### Feature list

- free and open source;
- no ads, ever;
- content is open - teachers or students can create their own repositories for languages they're
teaching and assist in learning process;
- locally stored content - you can use the app anywhere;
- highly configurable preferences like
    - task count per session;
    - usage of on-screen keyboard;
    - option count in choose tasks;
- statistics of your achievements.

Features relative to content:

Lessons:

 - tasks are grouped into lessons;
 - lessons are grouped into sections;
 - each lesson can have multiple theory sections;
 - vocabulary is automatically listed for each lesson.

Trainings:

- tasks are grouped into trainings;
- custom sessions can be started by selecting category and subcategory of tasks (like decline only
first declension masuline singular nouns);
- vocabulary is automatically listed for each training.

Theory:

- editor can provide multiple chapters of theory.

### Plans

For the first stable release I'm planning:
 - bug report system for content and app;
 - theme section which will be linked with lessons for themed tasks like "Household objects";
 - process statistics;
 - progressing strategy (currently, I only have tasks and results);
 - in session tooltip notes;
 - management of multiple languages;

Out of scope for the first stable release:
 - tasks with images;
 - tasks with sound;
 - in-app content editing system;

## Naming

Name of the application - _Sīc locūtus_ or in English "Thus spoke" inspired from Friedrich
Nietzsche book title _Thus Spoke Zarathustra_. As a joke, I call it _Sick locust_.

## Pull requests

Not accepted until the "big" release.

## Feature requests

Maybe? I cannot promise anything since this is my free time project.

## Bugs/issues

For now, I suggest periodically checking the Issues page for new bug reports. A bug-reporting
system is planned for future releases.

## Author

Armands Arseniuss Skolmeisters