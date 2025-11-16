package com.example.bai2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bai2.R;
import com.example.bai2.UpdatePointsActivity;
import com.example.bai2.database.DatabaseHelper;
import com.example.bai2.model.Customer;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
    private final Context context;
    private final List<Customer> customers;

    private final DatabaseHelper db;

    public CustomerAdapter(Context context, List<Customer> customers, DatabaseHelper db) {
        this.context = context;
        this.customers = customers;
        this.db = db; //
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvPoints, tvAvatar;
        TextView tvCreatedAt, tvUpdatedAt;
        CardView card;

        public ViewHolder(View v) {
            super(v);
            card = v.findViewById(R.id.cardCustomer);
            tvName = v.findViewById(R.id.tvName);
            tvPhone = v.findViewById(R.id.tvPhone);
            tvPoints = v.findViewById(R.id.tvPoints);
            tvAvatar = v.findViewById(R.id.tvAvatar);
            tvCreatedAt = v.findViewById(R.id.tvCreatedAt);
            tvUpdatedAt = v.findViewById(R.id.tvUpdatedAt);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder h, int pos) {
        Customer c = customers.get(pos);

        String customerName = c.getName();
        if (customerName != null && !customerName.isEmpty()) {
            h.tvAvatar.setText(customerName.substring(0, 1).toUpperCase());
        } else {
            h.tvAvatar.setText("#");
        }
        h.tvName.setText(customerName);
        h.tvPhone.setText("📞 " + c.getPhone());
        h.tvPoints.setText(c.getPoints() + " điểm");
        h.tvCreatedAt.setText("Tạo: " + (c.getCreatedAt() != null ? c.getCreatedAt() : "N/A"));
        h.tvUpdatedAt.setText("Cập nhật: " + (c.getUpdatedAt() != null ? c.getUpdatedAt() : "N/A"));

        // Code nhấn để sửa
        h.card.setOnClickListener(v -> {
            Intent intent = new Intent(context, UpdatePointsActivity.class);
            intent.putExtra("CUSTOMER_PHONE", c.getPhone());
            context.startActivity(intent);
        });

        //THÊM LOGIC NHẤN GIỮ (LONG PRESS) ĐỂ XÓA
        h.card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Lấy vị trí item hiện tại
                int currentPosition = h.getAdapterPosition();

                // Hiển thị hộp thoại xác nhận
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa khách hàng: " + c.getName() + "?")
                        .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Gọi hàm xóa khỏi Database
                                db.deleteCustomer(c.getPhone());

                                // Xóa khỏi danh sách (List) trong Adapter
                                customers.remove(currentPosition);

                                // Báo cho RecyclerView biết item đã bị xóa
                                notifyItemRemoved(currentPosition);

                                Toast.makeText(context, "Đã xóa khách hàng: " + c.getName(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Hủy", null) // Nút hủy không làm gì cả
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true; // Đã xử lý sự kiện
            }
        });
    }

    @Override
    public int getItemCount() { return customers.size(); }
}