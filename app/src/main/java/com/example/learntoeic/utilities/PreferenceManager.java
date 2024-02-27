package com.example.learntoeic.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Const.KEY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }
}
