package com.example.bai2.database;

import android.annotation.SuppressLint;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import com.example.bai2.model.Customer;
import java.text.SimpleDateFormat;
import java.util.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "loyalty.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE customers (" +
                "phone TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "points INTEGER DEFAULT 0," +
                "createdAt TEXT, " +
                "updatedAt TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS customers");
        onCreate(db);
    }

    public void addCustomer(String phone, String name, int points) {
        SQLiteDatabase db = getWritableDatabase();
        @SuppressLint("SimpleDateFormat") String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        ContentValues v = new ContentValues();
        v.put("phone", phone);
        v.put("name", name);
        v.put("points", points);
        v.put("createdAt", now);
        v.put("updatedAt", now);
        db.insert("customers", null, v);
    }

    public ArrayList<Customer> getAll() {
        ArrayList<Customer> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM customers ORDER BY points DESC", null);
        while (c.moveToNext()) {
            list.add(new Customer(
                    c.getString(0), c.getString(1), c.getInt(2),
                    c.getString(3), c.getString(4)
            ));
        }
        c.close();
        return list;
    }

    public void updatePoints(String phone, int addPoints) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT points FROM customers WHERE phone=?", new String[]{phone});
        if (c.moveToFirst()) {
            int newPoints = c.getInt(0) + addPoints;
            @SuppressLint("SimpleDateFormat") String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            ContentValues v = new ContentValues();
            v.put("points", newPoints);
            v.put("updatedAt", now);
            db.update("customers", v, "phone=?", new String[]{phone});
        }
        c.close();
    }

    @SuppressLint("Range")
    public Customer getCustomerByPhone(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM customers WHERE phone = ? LIMIT 1", new String[]{phone});

        Customer customer = null;

        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndexOrThrow("name"));
            int points = c.getInt(c.getColumnIndexOrThrow("points"));
            String createdAt = c.getString(c.getColumnIndexOrThrow("createdAt"));
            String updatedAt = c.getString(c.getColumnIndexOrThrow("updatedAt"));

            // Giả sử constructor của Customer là (phone, name, points, createdAt, updatedAt)
            customer = new Customer(phone, name, points, createdAt, updatedAt);
        }

        c.close();
        db.close();
        return customer; // Sẽ trả về null nếu không tìm thấy
    }
}
