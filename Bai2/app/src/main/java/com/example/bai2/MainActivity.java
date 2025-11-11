package com.example.bai2;

import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.*;
import androidx.appcompat.widget.Toolbar;
import com.example.bai2.adapter.CustomerAdapter;
import com.example.bai2.database.DatabaseHelper;
import com.example.bai2.model.Customer;
import com.example.bai2.utils.XmlUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    RecyclerView rv;
    FloatingActionButton btnAdd;
    DatabaseHelper db;
    CustomerAdapter adapter;

    // Mã yêu cầu để nhận kết quả từ trình chọn tệp
    private static final int FILE_PICKER_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_main);

        rv = findViewById(R.id.rvCustomers);
        btnAdd = findViewById(R.id.btnAdd);
        db = new DatabaseHelper(this);

        refreshList();

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddCustomerActivity.class));
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        ArrayList<Customer> list = db.getAll();
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CustomerAdapter(this, list, db); // <-- DÒNG MỚI (Thêm 'db' vào)

        rv.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // 2. Xử lý khi nhấn nút menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_export) {
            exportCustomers();
            return true;
        } else if (id == R.id.menu_import) {
            importCustomers();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Logic EXPORT
    private void exportCustomers() {
        // Lấy danh sách khách hàng
        List<Customer> customers = db.getAll();
        if (customers.isEmpty()) {
            Toast.makeText(this, "Không có khách hàng nào để export", Toast.LENGTH_SHORT).show();
            return;
        }

        File xmlFile = XmlUtils.writeXmlToDownloads(this, customers);

        if (xmlFile == null) {
            Toast.makeText(this, "Lỗi khi tạo file XML", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy Uri an toàn bằng FileProvider
        Uri fileUri = FileProvider.getUriForFile(
                this,
                "com.example.bai2.provider", // (PHẢI TRÙNG VỚI "authorities" TRONG MANIFEST)
                xmlFile
        );

        // Tạo Intent để gửi Email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822"); // Kiểu để mở app email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Export danh sách khách hàng");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "File XML đính kèm chứa danh sách khách hàng.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri); // Đính kèm file
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Cấp quyền đọc file

        startActivity(Intent.createChooser(emailIntent, "Chọn ứng dụng Email:"));
    }

    //Logic IMPORT (Mở trình chọn tệp)
    private void importCustomers() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/xml"); // Chỉ cho phép chọn file .xml

        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

    //Nhận kết quả file IMPORT về
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri fileUri = data.getData();
                try {
                    // Mở luồng đọc file
                    FileInputStream fis = (FileInputStream) getContentResolver().openInputStream(fileUri);

                    // Dùng XmlUtils để phân tích file
                    List<Customer> importedCustomers = XmlUtils.parseCustomersXml(fis);

                    // Thêm khách hàng vào DB (cần hàm upsert)
                    db.upsertCustomers(importedCustomers);

                    // Cập nhật lại danh sách
                    refreshList();

                    Toast.makeText(this, "Đã import thành công " + importedCustomers.size() + " khách hàng", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi khi đọc file Import", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
