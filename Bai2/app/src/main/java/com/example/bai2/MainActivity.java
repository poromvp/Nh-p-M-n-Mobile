package com.example.bai2;

import android.content.*;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import androidx.appcompat.widget.Toolbar;
import com.example.bai2.adapter.CustomerAdapter;
import com.example.bai2.database.DatabaseHelper;
import com.example.bai2.model.Customer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    RecyclerView rv;
    FloatingActionButton btnAdd;
    DatabaseHelper db;
    CustomerAdapter adapter;

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

        FloatingActionButton btnFind = findViewById(R.id.btnFind);
        btnFind.setOnClickListener(v -> {
            startActivity(new Intent(this, FindCustomerActivity.class));
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList(); // Tự động cập nhật sau khi Import
    }

    private void refreshList() {
        ArrayList<Customer> list = db.getAll();
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerAdapter(this, list, db);
        rv.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Xử lý khi nhấn nút menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_export) {
            // Chỉ cần mở ExportActivity
            startActivity(new Intent(this, ExportActivity.class));
            return true;
        } else if (id == R.id.menu_import) {
            // Chỉ cần mở ImportActivity
            startActivity(new Intent(this, ImportActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}