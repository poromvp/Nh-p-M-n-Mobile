package com.example.bai2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bai2.database.DatabaseHelper;
import com.example.bai2.model.Customer;
import com.example.bai2.utils.XmlUtils;
import java.io.FileInputStream;
import java.util.List;

public class ImportActivity extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST_CODE = 101;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);

        // Logic của hàm importCustomers() cũ được dời vào đây
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/xml"); // Chỉ cho phép chọn file .xml

        // Mở trình chọn tệp và chờ kết quả
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

    // Logic của hàm onActivityResult() cũ được dời vào đây
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri fileUri = data.getData();
                try {
                    FileInputStream fis = (FileInputStream) getContentResolver().openInputStream(fileUri);
                    List<Customer> importedCustomers = XmlUtils.parseCustomersXml(fis);

                    db.upsertCustomers(importedCustomers); // Lưu vào DB

                    Toast.makeText(this, "Đã import thành công " + importedCustomers.size() + " khách hàng", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi khi đọc file Import", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Người dùng hủy chọn file
                Toast.makeText(this, "Đã hủy import", Toast.LENGTH_SHORT).show();
            }

            finish(); // Tự động đóng Activity này sau khi xử lý xong (hoặc hủy)
        }
    }
}