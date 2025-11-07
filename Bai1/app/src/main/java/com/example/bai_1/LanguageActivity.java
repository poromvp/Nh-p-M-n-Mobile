package com.example.bai_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import androidx.appcompat.app.AppCompatActivity;

public class LanguageActivity extends AppCompatActivity {

    private RadioButton rbEnglish, rbVietnamese;
    private LanguageManager langManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Áp dụng ngôn ngữ đã lưu TRƯỚC khi load layout
        langManager = new LanguageManager(this);
        langManager.setLocale(langManager.getLanguage());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.language_activity);

        rbEnglish = findViewById(R.id.eng);
        rbVietnamese = findViewById(R.id.vi);

        // Đọc ngôn ngữ đã lưu trước đó
        String lang = langManager.getLanguage();
        if (lang.equals("vi")) rbVietnamese.setChecked(true);
        else rbEnglish.setChecked(true);

        // Khi chọn tiếng Anh
        rbEnglish.setOnClickListener(v -> {
            rbVietnamese.setChecked(false);
            langManager.updateLanguage("en");
            Intent intent = new Intent(LanguageActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // đóng LanguageActivity để quay lại Main
        });


        // Khi chọn tiếng Việt
        rbVietnamese.setOnClickListener(v -> {
            langManager.updateLanguage("vi");
            rbEnglish.setChecked(false);
            Intent intent = new Intent(LanguageActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // đóng LanguageActivity để quay lại Main
        });
    }
}
