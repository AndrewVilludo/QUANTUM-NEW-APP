package com.example.quantumrealm;


import android.content.Context;
import android.content.SharedPreferences;

public class ProgressManager {

    private SharedPreferences preferences;

    public ProgressManager(Context context) {
        preferences = context.getSharedPreferences("USER_PROGRESS", Context.MODE_PRIVATE);
    }

    // Check if the quiz is unlocked
    public boolean isQuizUnlocked(int quizId) {
        return preferences.getBoolean("quiz_" + quizId + "_unlocked", false);
    }

    // Unlock a quiz
    public void unlockQuiz(int quizId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("quiz_" + quizId + "_unlocked", true);
        editor.apply();

        // Unlock next lesson/module if needed
        unlockNextLesson(quizId);
    }

    // Check if a subject is unlocked
    public boolean isSubjectUnlocked(int subjectId) {
        return preferences.getBoolean("subject_" + subjectId + "_unlocked", false);
    }

    // Unlock the next subject after completing the current one
    public void unlockSubject(int subjectId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("subject_" + subjectId + "_unlocked", true);
        editor.apply();
    }

    // Check if a year is unlocked
    public boolean isYearUnlocked(int year) {
        return preferences.getBoolean("year_" + year + "_unlocked", false);
    }

    // Unlock the next year after completing all subjects
    public void unlockYear(int year) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("year_" + (year + 1) + "_unlocked", true);
        editor.apply();
    }

    // Unlock next lesson after completing a quiz
    public void unlockNextLesson(int quizId) {
        // Assuming each quiz corresponds to a lesson, update accordingly
        int lessonId = getLessonIdFromQuiz(quizId);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("lesson_" + lessonId + "_unlocked", true);
        editor.apply();
    }

    // Example method to get lessonId from quizId (you can customize this logic)
    private int getLessonIdFromQuiz(int quizId) {
        // You can define your mapping here based on quiz IDs to lesson IDs.
        // For simplicity, let's return the quizId as lessonId for now.
        return quizId;
    }
}
