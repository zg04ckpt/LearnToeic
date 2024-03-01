package com.example.learntoeic.models;

import android.os.Parcelable;

import com.example.learntoeic.utilities.Const;

import java.io.Serializable;

public class Word {
    public String en, vi, id;
    public WordType type;
    public Boolean flag;
    public Boolean selectStatus;

    public Word(String en, String vi, String type) {
        this.en = en;
        this.vi = vi;
        stringToWordType(type);
    }
    public Word(String id, String en, String vi, String type, Boolean flag, Boolean selectStatus) {
        this.id = id;
        this.en = en;
        this.vi = vi;
        stringToWordType(type);
        this.flag = flag;
        this.selectStatus = selectStatus;
    }

    private void stringToWordType(String s)
    {
        if(s.equals(Word.WordType.Noun.name())) type = Word.WordType.Noun;
        else if(s.equals(Word.WordType.Verb.name())) type = Word.WordType.Verb;
        else if(s.equals(Word.WordType.Adj.name())) type = Word.WordType.Adj;
        else if(s.equals(Word.WordType.Adv.name())) type = Word.WordType.Adv;
        else type = Word.WordType.Prep;
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
