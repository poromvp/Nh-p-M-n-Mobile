package com.example.bai2;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bai2.database.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;

public class AddCustomerActivity extends AppCompatActivity {
    EditText edtPhone, edtName, edtPoints;
    Button btnSave;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_customer);

        edtPhone = findViewById(R.id.edtPhone);
        edtName = findViewById(R.id.edtName);
        edtPoints = findViewById(R.id.edtPoints);
        btnSave = findViewById(R.id.btnSave);
        db = new DatabaseHelper(this);

        btnSave.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            String name = edtName.getText().toString().trim();
            String pointsStr = edtPoints.getText().toString().trim();

            if (TextUtils.isEmpty(phone)) {
                Snackbar.make(v, "Vui lòng nhập số điện thoại!", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(name)) {
                Snackbar.make(v, "Vui lòng nhập tên khách hàng!", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // 4. Chuyển điểm từ chữ sang số (mặc định là 0 nếu không nhập)
            int points = 0;
            if (!TextUtils.isEmpty(pointsStr)) {
                points = Integer.parseInt(pointsStr);
            }

            try {
                db.addCustomer(phone, name, points);
                Snackbar.make(v, "🎉 Đã thêm khách hàng thành công!", Snackbar.LENGTH_LONG).show();

                edtPhone.setText("");
                edtName.setText("");
                edtPhone.requestFocus();
                // 6. Xoá (clear) cả ô điểm
                edtPoints.setText("");
                edtPhone.requestFocus();
            } catch (Exception e) {
                Snackbar.make(v, "❌ Số điện thoại đã tồn tại!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
