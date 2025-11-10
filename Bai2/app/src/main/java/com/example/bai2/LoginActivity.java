package com.example.bai2; // Thay đổi thành package của bạn

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gắn layout activity_login.xml vào Activity này
        setContentView(R.layout.activity_login);

        // Ẩn Action Bar (thanh tiêu đề) cho đẹp
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Ánh xạ (tìm) nút login từ file XML
        btnLogin = findViewById(R.id.btnLogin);

        // Thiết lập sự kiện khi nhấn nút
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo một Intent để chuyển từ LoginActivity sang MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                // Kết thúc (đóng) LoginActivity để người dùng không thể
                // nhấn "back" quay lại trang này
                finish();
            }
        });
    }
}