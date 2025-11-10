package com.example.bai2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bai2.database.DatabaseHelper;
import com.example.bai2.model.Customer;
import com.google.android.material.snackbar.Snackbar;

public class UpdatePointsActivity extends AppCompatActivity {
    EditText edtPhone, edtChange;
    TextView tvName, tvPoints;
    Button btnAddPoints, btnSubtractPoints;
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
        btnAddPoints = findViewById(R.id.btnAddPoints);
        btnSubtractPoints = findViewById(R.id.btnSubtractPoints);
        layoutInfo = findViewById(R.id.layoutInfo);

        layoutInfo.setVisibility(View.GONE);

        // Thêm TextWatcher để tự động tìm
        edtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchCustomer(s.toString());
            }
        });

        // Tách riêng logic cộng/trừ
        btnAddPoints.setOnClickListener(v -> performAddPoints());
        btnSubtractPoints.setOnClickListener(v -> performUsePoints());

        // === THÊM CODE MỚI: TỰ ĐỘNG NHẬN SĐT KHI MỞ ===
        checkIntentForPhone();
        // ============================================
    }

    // === THÊM HÀM MỚI NÀY VÀO ===
    /**
     * Kiểm tra xem Activity có được mở kèm SĐT từ MainActivity không.
     * Nếu có, tự động điền SĐT và tìm khách hàng.
     */
    private void checkIntentForPhone() {
        // Kiểm tra xem có SĐT nào được gửi qua với key "CUSTOMER_PHONE" không
        if (getIntent().hasExtra("CUSTOMER_PHONE")) {
            String phone = getIntent().getStringExtra("CUSTOMER_PHONE");
            if (phone != null && !phone.isEmpty()) {
                // 1. Tự điền SĐT vào ô EditText
                edtPhone.setText(phone);

                // 2. Tự động "Tìm kiếm"
                // (Hàm searchCustomer đã được gọi bởi TextWatcher ở trên)
                // Nếu bạn muốn chắc chắn, có thể gọi lại:
                // searchCustomer(phone);
            }
        }
    }
    // =============================

    // Hàm tìm khách hàng (tối ưu, dùng getCustomerByPhone)
    private void searchCustomer(String phone) {
        if (phone.trim().isEmpty()) {
            layoutInfo.setVisibility(View.GONE);
            currentCustomer = null;
            return;
        }

        // Tối ưu: Chỉ tìm 1 khách hàng
        currentCustomer = db.getCustomerByPhone(phone.trim());

        if (currentCustomer == null) {
            layoutInfo.setVisibility(View.GONE);
        } else {
            layoutInfo.setVisibility(View.VISIBLE);
            tvName.setText("Tên: " + currentCustomer.getName());
            tvPoints.setText("Điểm hiện tại: " + currentCustomer.getPoints());
        }
    }

    // Hàm cộng điểm
    private void performAddPoints() {
        if (currentCustomer == null) {
            showSnack("Không tìm thấy khách hàng với SĐT này");
            return;
        }
        String val = edtChange.getText().toString().trim();
        if (val.isEmpty()) {
            showSnack("Nhập số điểm cần cộng");
            return;
        }

        int pointsToAdd = Integer.parseInt(val);
        db.updatePoints(currentCustomer.getPhone(), pointsToAdd);

        showSnack("Đã cộng điểm thành công 🎉");
        edtChange.setText("");
        searchCustomer(currentCustomer.getPhone());
    }

    // Hàm trừ điểm
    private void performUsePoints() {
        if (currentCustomer == null) {
            showSnack("Không tìm thấy khách hàng với SĐT này");
            return;
        }
        String val = edtChange.getText().toString().trim();
        if (val.isEmpty()) {
            showSnack("Nhập số điểm cần trừ");
            return;
        }

        int pointsToUse = Integer.parseInt(val);

        if (currentCustomer.getPoints() < pointsToUse) {
            showSnack("Không đủ điểm để trừ!");
            return;
        }

        db.updatePoints(currentCustomer.getPhone(), -pointsToUse);

        showSnack("Đã trừ điểm thành công");
        edtChange.setText("");
        searchCustomer(currentCustomer.getPhone());
    }


    private void showSnack(String msg) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
    }
}