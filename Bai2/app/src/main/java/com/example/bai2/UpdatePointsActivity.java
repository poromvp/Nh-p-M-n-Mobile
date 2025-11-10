package com.example.bai2;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bai2.database.DatabaseHelper;
import com.example.bai2.model.Customer;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class UpdatePointsActivity extends AppCompatActivity {
    EditText edtPhone, edtChange;
    TextView tvName, tvPoints;
    Button btnSearch, btnAddPoints, btnSubtractPoints;
    LinearLayout layoutInfo;
    DatabaseHelper db;
    Customer currentCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_points);

        db = new DatabaseHelper(this);
        edtPhone = findViewById(R.id.edtPhone);
        edtChange = findViewById(R.id.edtChange);
        tvName = findViewById(R.id.tvName);
        tvPoints = findViewById(R.id.tvPoints);
        btnSearch = findViewById(R.id.btnSearch);
        btnAddPoints = findViewById(R.id.btnAddPoints);
        btnSubtractPoints = findViewById(R.id.btnSubtractPoints);
        layoutInfo = findViewById(R.id.layoutInfo);

        btnSearch.setOnClickListener(v -> searchCustomer());
        btnAddPoints.setOnClickListener(v -> updatePoints(true));
        btnSubtractPoints.setOnClickListener(v -> updatePoints(false));
    }

    private void searchCustomer() {
        String phone = edtPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            showSnack("Vui lòng nhập số điện thoại");
            return;
        }

        ArrayList<Customer> list = db.getAll();
        currentCustomer = null;
        for (Customer c : list) {
            if (c.getPhone().equals(phone)) {
                currentCustomer = c;
                break;
            }
        }

        if (currentCustomer == null) {
            layoutInfo.setVisibility(View.GONE);
            showSnack("Không tìm thấy khách hàng này!");
        } else {
            layoutInfo.setVisibility(View.VISIBLE);
            tvName.setText("Tên: " + currentCustomer.getName());
            tvPoints.setText("Điểm hiện tại: " + currentCustomer.getPoints());
        }
    }

    private void updatePoints(boolean isAdd) {
        if (currentCustomer == null) {
            showSnack("Hãy tìm khách hàng trước");
            return;
        }

        String val = edtChange.getText().toString().trim();
        if (val.isEmpty()) {
            showSnack("Nhập số điểm cần thay đổi");
            return;
        }

        int delta = Integer.parseInt(val);
        if (!isAdd) delta = -delta;

        // Kiểm tra không trừ quá số điểm
        if (!isAdd && currentCustomer.getPoints() < Math.abs(delta)) {
            showSnack("Không đủ điểm để trừ!");
            return;
        }

        db.updatePoints(currentCustomer.getPhone(), delta);
        showSnack(isAdd ? "Đã cộng điểm thành công 🎉" : "Đã trừ điểm thành công");
        searchCustomer(); // refresh hiển thị mới
    }

    private void showSnack(String msg) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
    }
}
