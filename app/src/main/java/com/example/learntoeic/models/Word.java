package com.example.learntoeic.models;

public class Word {
    public String en, vi, id;
    public WordType type;
    public Boolean flag;
    public Boolean selectStatus;

    public Word(String id, String en, String vi, WordType type, Boolean flag, Boolean selectStatus) {
        this.id = id;
        this.en = en;
        this.vi = vi;
        this.type = type;
        this.flag = flag;
        this.selectStatus = selectStatus;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public String getTypeString()
    {
        return type.name();
    }

    public enum WordType{
        Noun, Verb, Adj, Adv, Prep
    }
}
