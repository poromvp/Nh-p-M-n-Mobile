package com.example.bai2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bai2.database.DatabaseHelper;
import com.example.bai2.model.Customer;
import com.google.android.material.snackbar.Snackbar;

// (Không cần import Toolbar nữa vì layout mới không dùng)

public class UpdatePointsActivity extends AppCompatActivity {
    // Không còn edtPhone hay btnSearch
    EditText edtChange;
    TextView tvName, tvPoints;
    Button btnAddPoints, btnSubtractPoints, btnBack; // Thêm btnBack
    DatabaseHelper db;
    Customer currentCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_points);

        // --- 1. Ánh xạ các view ---
        db = new DatabaseHelper(this);
        edtChange = findViewById(R.id.edtChange);
        tvName = findViewById(R.id.tvName);
        tvPoints = findViewById(R.id.tvPoints);
        btnAddPoints = findViewById(R.id.btnAddPoints);
        btnSubtractPoints = findViewById(R.id.btnSubtractPoints);
        btnBack = findViewById(R.id.btnBack); // Ánh xạ nút Back

        // --- 2. Nhận SĐT được gửi từ FindCustomerActivity ---
        String phone = getIntent().getStringExtra("CUSTOMER_PHONE");

        // Kiểm tra SĐT (rất quan trọng)
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không nhận được SĐT khách hàng.", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không có SĐT
            return;
        }

        // --- 3. Tải thông tin khách hàng (chỉ 1 lần) ---
        loadCustomerInfo(phone);

        // --- 4. Gán sự kiện cho các nút ---
        btnAddPoints.setOnClickListener(v -> performAddPoints());
        btnSubtractPoints.setOnClickListener(v -> performUsePoints());
        btnBack.setOnClickListener(v -> {
            finish(); // Nút Back chỉ cần đóng Activity
        });
    }

    /**
     * Tải thông tin khách hàng từ DB và cập nhật UI
     */
    private void loadCustomerInfo(String phone) {
        currentCustomer = db.getCustomerByPhone(phone.trim());
        if (currentCustomer == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy khách hàng.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Hiển thị thông tin lên
            tvName.setText("Tên: " + currentCustomer.getName());
            tvPoints.setText("Điểm hiện tại: " + currentCustomer.getPoints());
        }
    }

    // (Không còn hàm searchCustomer() hay checkIntentForPhone() nữa)

    /**
     * Hàm xử lý cộng điểm
     */
    private void performAddPoints() {
        if (currentCustomer == null) return;

        String val = edtChange.getText().toString().trim();
        if (val.isEmpty()) {
            showSnack("Nhập số điểm cần cộng");
            return;
        }

        int pointsToAdd = Integer.parseInt(val);
        db.updatePoints(currentCustomer.getPhone(), pointsToAdd);

        showSnack("Đã cộng điểm thành công 🎉");
        edtChange.setText("");

        // Tải lại thông tin điểm mới nhất
        loadCustomerInfo(currentCustomer.getPhone());
    }

    /**
     * Hàm xử lý trừ điểm
     */
    private void performUsePoints() {
        if (currentCustomer == null) return;

        String val = edtChange.getText().toString().trim();
        if (val.isEmpty()) {
            showSnack("Nhập số điểm cần trừ");
            return;
        }

        int pointsToUse = Integer.parseInt(val);

        // Kiểm tra điểm
        if (currentCustomer.getPoints() < pointsToUse) {
            showSnack("Không được nhập điểm trừ cao hơn điểm hiện tại!");
            return;
        }

        db.updatePoints(currentCustomer.getPhone(), -pointsToUse);

        showSnack("Đã trừ điểm thành công");
        edtChange.setText("");

        // Tải lại thông tin điểm mới nhất
        loadCustomerInfo(currentCustomer.getPhone());
    }

    /**
     * Hàm tiện ích hiển thị Snackbar
     */
    private void showSnack(String msg) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
    }
}