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
        if (nextIndex < lessons.length()) {
            try {
                JSONObject nextLesson = lessons.getJSONObject(nextIndex);
                Intent intent = slideshowIntent(context, nextLesson, nextIndex, moduleId, moduleName, subjectId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } catch (Exception ignored) {
                backToCourse(context, subjectId);
            }
        } else {
            Intent intent = moduleQuizIntent(context, moduleId, moduleName, subjectId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
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
}
