package com.example.bai_1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LanguageManager {
    private static final String LANG_KEY = "My_Lang";
    private final SharedPreferences prefs;
    private final Context context;

    public LanguageManager(Context ctx) {
        context = ctx;
        prefs = ctx.getSharedPreferences("Settings", Context.MODE_PRIVATE);
    }

    // Cập nhật ngôn ngữ và lưu lại
    public void updateLanguage(String langCode) {
        setLocale(langCode);
        saveLanguage(langCode);
    }

    // Thiết lập ngôn ngữ
    public void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    // Lưu ngôn ngữ vào SharedPreferences
    private void saveLanguage(String langCode) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LANG_KEY, langCode);
        editor.apply();
    }

    // Lấy ngôn ngữ đã lưu
    public String getLanguage() {
        return prefs.getString(LANG_KEY, "en"); // mặc định là tiếng Anh
    }
}

