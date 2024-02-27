package com.example.learntoeic.listeners;

import com.example.learntoeic.models.Word;

public interface IWordListener {
    void onLongClickListener(Word word);
    void onWordCheckboxClick(Boolean isChecked, Word word);
}
