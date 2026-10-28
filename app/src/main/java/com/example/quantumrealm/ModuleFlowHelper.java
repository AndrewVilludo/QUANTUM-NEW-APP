package com.example.quantumrealm;

import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

public final class ModuleFlowHelper {

    public static final String EXTRA_MODULE_ID = "module_id";
    public static final String EXTRA_MODULE_NAME = "module_name";
    public static final String EXTRA_SUBJECT_ID = "subject_id";
    public static final String EXTRA_LESSON = "lesson_json";
    public static final String EXTRA_LESSON_INDEX = "lesson_index";

    private ModuleFlowHelper() {}

    public static Intent slideshowIntent(Context context, JSONObject lesson, int lessonIndex,
                                         int moduleId, String moduleName, int subjectId) {
        Intent intent = new Intent(context, LessonActivity.class);
        intent.putExtra(EXTRA_LESSON, lesson.toString());
        intent.putExtra(EXTRA_LESSON_INDEX, lessonIndex);
        intent.putExtra(EXTRA_MODULE_ID, moduleId);
        intent.putExtra(EXTRA_MODULE_NAME, moduleName);
        intent.putExtra(EXTRA_SUBJECT_ID, subjectId);
        return intent;
    }

    public static Intent activityIntent(Context context, JSONObject lesson, int lessonIndex,
                                        int moduleId, String moduleName, int subjectId) {
        Intent intent = new Intent(context, LessonActivityActivity.class);
        intent.putExtra(EXTRA_LESSON, lesson.toString());
        intent.putExtra(EXTRA_LESSON_INDEX, lessonIndex);
        intent.putExtra(EXTRA_MODULE_ID, moduleId);
        intent.putExtra(EXTRA_MODULE_NAME, moduleName);
        intent.putExtra(EXTRA_SUBJECT_ID, subjectId);
        return intent;
    }

    public static void openNextAfterCompletion(Context context, int moduleId, String moduleName,
                                                 int subjectId, JSONArray lessons, int completedIndex) {
        int nextIndex = completedIndex + 1;
        try {
            if (nextIndex < lessons.length()) {
                JSONObject nextLesson = lessons.getJSONObject(nextIndex);
                Intent intent = slideshowIntent(context, nextLesson, nextIndex, moduleId, moduleName, subjectId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } else {
                Intent intent = questContinueIntent(context, moduleId, moduleName, subjectId);
                intent.putExtra(QuestContinueActivity.EXTRA_MODE, QuestContinueActivity.MODE_QUIZ_GATE);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        } catch (Exception ignored) {
            backToCourse(context, subjectId);
        }
    }

    public static Intent questContinueIntent(Context context, int moduleId, String moduleName, int subjectId) {
        Intent intent = new Intent(context, QuestContinueActivity.class);
        intent.putExtra(EXTRA_MODULE_ID, moduleId);
        intent.putExtra(EXTRA_MODULE_NAME, moduleName);
        intent.putExtra(EXTRA_SUBJECT_ID, subjectId);
        return intent;
    }

    public static Intent moduleQuizIntent(Context context, int moduleId, String moduleName, int subjectId) {
        Intent intent = new Intent(context, ModuleQuizActivity.class);
        intent.putExtra(EXTRA_MODULE_ID, moduleId);
        intent.putExtra(EXTRA_MODULE_NAME, moduleName);
        intent.putExtra(EXTRA_SUBJECT_ID, subjectId);
        return intent;
    }

    public static void restartModuleFromFirstLesson(Context context, int moduleId, String moduleName, int subjectId) {
        Intent intent = new Intent(context, ModuleActivity.class);
        intent.putExtra("course_id", moduleId);
        intent.putExtra("course_name", moduleName);
        intent.putExtra("subject_id", subjectId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void backToCourse(Context context, int subjectId) {
        Intent intent = new Intent(context, CourseActivity.class);
        intent.putExtra("course_id", subjectId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void continueQuestFromDashboard(Context context, JSONObject quest) {
        if (quest == null) {
            context.startActivity(new Intent(context, Journey.class));
            return;
        }
        String type = quest.optString("type", "");
        int subjectId = quest.optInt("subject_id", -1);
        int moduleId = quest.optInt("module_id", -1);
        String moduleName = quest.optString("module_name", "Module");
        int yearId = quest.optInt("year_id", 0);

        if ("module_quiz".equals(type) && moduleId > 0 && subjectId > 0) {
            context.startActivity(moduleQuizIntent(context, moduleId, moduleName, subjectId));
            return;
        }

        if ("lesson".equals(type) && moduleId > 0 && subjectId > 0) {
            Intent intent = new Intent(context, ModuleActivity.class);
            intent.putExtra("course_id", moduleId);
            intent.putExtra("course_name", moduleName);
            intent.putExtra("subject_id", subjectId);
            intent.putExtra("year_id", yearId);
            intent.putExtra("auto_continue", true);
            context.startActivity(intent);
            return;
        }

        context.startActivity(new Intent(context, Journey.class));
    }
}
