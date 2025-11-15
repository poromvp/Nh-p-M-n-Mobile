package com.example.bai2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.view.View; // <-- Import
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout; // <-- Import
import android.widget.TextView; // <-- Import
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bai2.database.DatabaseHelper;
import com.example.bai2.model.Customer;

public class FindCustomerActivity extends AppCompatActivity {
    EditText edtPhone;
    Button btnSearch;
    DatabaseHelper db;

    // --- Thêm các view cho phần kết quả ---
    LinearLayout layoutResult;
    TextView tvResultName, tvResultPhone, tvResultPoints;
    Button btnGoToUpdate;

    // Lưu khách hàng đang tìm thấy
    Customer foundCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_customer);

        // Kích hoạt Toolbar
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }

        db = new DatabaseHelper(this);

        // Ánh xạ các view tìm kiếm
        edtPhone = findViewById(R.id.edtPhone);
        btnSearch = findViewById(R.id.btnSearch);

        // Ánh xạ các view kết quả
        layoutResult = findViewById(R.id.layoutResult);
        tvResultName = findViewById(R.id.tvResultName);
        tvResultPhone = findViewById(R.id.tvResultPhone);
        tvResultPoints = findViewById(R.id.tvResultPoints);
        btnGoToUpdate = findViewById(R.id.btnGoToUpdate);

        // Ban đầu ẩn kết quả đi
        layoutResult.setVisibility(View.GONE);

        //Sửa sự kiện cho nút "Tìm"
        btnSearch.setOnClickListener(v -> searchCustomer());

        //Thêm sự kiện cho nút "Cập nhật điểm"
        btnGoToUpdate.setOnClickListener(v -> {
            if (foundCustomer != null) {
                // Mở Activity Cập nhật và gửi SĐT qua
                Intent intent = new Intent(FindCustomerActivity.this, UpdatePointsActivity.class);
                intent.putExtra("CUSTOMER_PHONE", foundCustomer.getPhone());
                startActivity(intent);
            }
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish(); // Đóng Activity
        });
    }

    /**
     * Hàm này được gọi khi nhấn nút "Tìm khách hàng"
     */
    @SuppressLint("SetTextI18n")
    private void searchCustomer() {
        String query = edtPhone.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập SĐT hoặc Tên", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi hàm tìm kiếm(findCustomer)
        foundCustomer = db.findCustomer(query);

        if (foundCustomer == null) {
            // Không tìm thấy
            Toast.makeText(this, "Không tìm thấy khách hàng này!", Toast.LENGTH_LONG).show();
            // Ẩn kết quả đi
            layoutResult.setVisibility(View.GONE);
        } else {
            // Tìm thấy! Hiển thị kết quả
            tvResultName.setText("Tên: " + foundCustomer.getName());
            tvResultPhone.setText("SĐT: " + foundCustomer.getPhone());
            tvResultPoints.setText("Điểm hiện tại: " + foundCustomer.getPoints());
            // Hiển thị kết quả
            layoutResult.setVisibility(View.VISIBLE);
        }
    }

    // Xử lý nút back trên Toolbar
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}