package com.example.quantumrealm;

public class Answer {
    private int quizId;
    private String selectedAnswerKey;
    private String selectedAnswerText;

    // Constructor
    public Answer(int quizId, String selectedAnswerKey, String selectedAnswerText) {
        if (quizId <= 0 || selectedAnswerKey == null || selectedAnswerKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid quiz ID or answer key");
        }
        this.quizId = quizId;
        this.selectedAnswerKey = selectedAnswerKey;
        this.selectedAnswerText = selectedAnswerText;
    }

    public int getQuizId() {
        return quizId;
    }

    public String getSelectedAnswerKey() {
        return selectedAnswerKey;
    }

    public String getSelectedAnswerText() {
        return selectedAnswerText;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "quizId=" + quizId +
                ", selectedAnswerKey='" + selectedAnswerKey + '\'' +
                ", selectedAnswerText='" + selectedAnswerText + '\'' +
                '}';
    }
}
