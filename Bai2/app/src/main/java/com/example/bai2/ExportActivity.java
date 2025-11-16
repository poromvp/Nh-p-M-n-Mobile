package com.example.bai2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider; // <-- Dùng FileProvider
import com.example.bai2.database.DatabaseHelper;
import com.example.bai2.model.Customer;
import com.example.bai2.utils.XmlUtils; // <-- Dùng lại XmlUtils
import java.io.File; // <-- Dùng File
import java.util.List;

public class ExportActivity extends AppCompatActivity {

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);

        // Chạy logic export (dùng FileProvider)
        exportCustomers();
    }

    private void exportCustomers() {
        List<Customer> customers = db.getAll();
        if (customers.isEmpty()) {
            Toast.makeText(this, "Không có khách hàng nào để export", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 1. Dùng XmlUtils để lưu file vào thư mục *riêng* của app
        // (Hàm này đã có timestamp để tạo file duy nhất)
        File xmlFile = XmlUtils.writeXmlToDownloads(this, customers);

        if (xmlFile == null) {
            Toast.makeText(this, "Lỗi khi tạo file XML", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Dùng FileProvider để lấy Uri
        Uri fileUri = FileProvider.getUriForFile(
                this,
                "com.example.bai2.provider", // (Authority của bạn)
                xmlFile
        );

        // 3. Tạo Intent gửi Email (Giữ nguyên)
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Export danh sách khách hàng");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "File XML đính kèm...");
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(emailIntent, "Chọn ứng dụng Email:"));

        finish(); // Đóng activity
    }
}