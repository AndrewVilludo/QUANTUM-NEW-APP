package com.example.quantumrealm;

public class Question {
    private int id;
    private int moduleLessonId;
    private String question;
    private String answer1, answer2, answer3, answer4;
    private String correct;

    public Question(int id, int moduleLessonId, String question, String answer1, String answer2, String answer3, String answer4, String correct) {
        this.id = id;
        this.moduleLessonId = moduleLessonId;
        this.question = question;
        this.answer1 = answer1;
        this.answer2 = answer2;
        this.answer3 = answer3;
        this.answer4 = answer4;
        this.correct = correct;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer1() { return answer1; }
    public String getAnswer2() { return answer2; }
    public String getAnswer3() { return answer3; }
    public String getAnswer4() { return answer4; }

    // Correct answer text
    public String getCorrectAnswerText() {
        switch (correct.toLowerCase()) {  // Ensure case insensitivity
            case "answer1": return answer1;
            case "answer2": return answer2;
            case "answer3": return answer3;
            case "answer4": return answer4;
            default: return "Unknown";  // Fallback for invalid data
        }
    }
}
