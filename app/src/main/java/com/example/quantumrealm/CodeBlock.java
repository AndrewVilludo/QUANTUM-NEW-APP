package com.example.quantumrealm;

class CodeBlock {
    int id;
    String text;

    public CodeBlock(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;  // This is the getter for 'text'
    }
}
