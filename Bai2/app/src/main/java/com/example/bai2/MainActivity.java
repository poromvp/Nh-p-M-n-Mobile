package com.example.bai2;

import android.content.*;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        ArrayList<Customer> list = db.getAll();
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerAdapter(this, list);
        rv.setAdapter(adapter);
    }
}
