package com.example.bai_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final int EDIT = 1;

    Uri img;
    protected ImageView avt;
    protected TextView name;
    protected TextView email;
    protected LinearLayout dark_mode;
    protected LinearLayout language;
    protected LinearLayout change;


    @Override
    protected void onCreate(Bundle sis){
        LanguageManager langManager = new LanguageManager(this);
        langManager.setLocale(langManager.getLanguage()); // áp dụng ngôn ngữ đã lưu

        super.onCreate(sis);
        setContentView(R.layout.activity_main);

        avt=findViewById(R.id.imgAvatar);
        name=findViewById(R.id.tvName);
        email=findViewById(R.id.tvEmail);

        SharedPreferences userPrefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String savedName = userPrefs.getString("name", null);
        String savedEmail = userPrefs.getString("email", null);
        String savedAvatar = userPrefs.getString("avatar", null);

        if (savedName != null) name.setText(savedName);
        if (savedEmail != null) email.setText(savedEmail);
        if (savedAvatar != null) {
            img = Uri.parse(savedAvatar);
            avt.setImageURI(img);
        }



        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        dark_mode=findViewById(R.id.dark_mode);
        ImageView img_1=dark_mode.findViewById(R.id.icon);
        img_1.setImageResource(R.drawable.icon_dark_mode);
        TextView t_1=dark_mode.findViewById(R.id.tvTitle);
        t_1.setText(R.string.dark_mode);

        dark_mode.setOnClickListener(v->{
            startActivity(new Intent(MainActivity.this, ThemeActivity.class));
        });

        language=findViewById(R.id.language);
        TextView t_2=language.findViewById(R.id.tvTitle);
        t_2.setText(R.string.language);

        language.setOnClickListener(v->{
            startActivity(new Intent(MainActivity.this, LanguageActivity.class));
        });

        change=findViewById(R.id.change);
        ImageView img_3=change.findViewById(R.id.icon);
        img_3.setImageResource(R.drawable.icon_change);
        TextView t_3=change.findViewById(R.id.tvTitle);
        t_3.setText(R.string.change);

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String newName = data.getStringExtra("name");
                        String newEmail = data.getStringExtra("email");
                        String uriString = data.getStringExtra("img");

                        name.setText(newName);
                        email.setText(newEmail);

                        if (uriString != null) {
                            img = Uri.parse(uriString);
                            avt.setImageURI(img);
                        }

                        // Lưu vào SharedPreferences
                        getSharedPreferences("UserData", MODE_PRIVATE)
                                .edit()
                                .putString("name", newName)
                                .putString("email", newEmail)
                                .putString("avatar", uriString)
                                .apply();
                    }
                }
        );


        change.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditActivity.class);
            intent.putExtra("name", name.getText().toString());
            intent.putExtra("email", email.getText().toString());
            if(img != null) intent.putExtra("img", img.toString());


            launcher.launch(intent);
        });




    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        LanguageManager langManager = new LanguageManager(this);
//        String savedLang = langManager.getLanguage();
//
//        // Kiểm tra nếu ngôn ngữ hệ thống hiện tại khác với ngôn ngữ đã lưu
//        if (!savedLang.equals(getResources().getConfiguration().locale.getLanguage())) {
//            langManager.setLocale(savedLang);
//            recreate(); // tự khởi động lại MainActivity để cập nhật ngôn ngữ
//        }
//    }



}
