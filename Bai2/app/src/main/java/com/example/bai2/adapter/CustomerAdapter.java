package com.example.bai2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent; // <-- THÊM IMPORT NÀY
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bai2.R;
import com.example.bai2.UpdatePointsActivity; // <-- THÊM IMPORT NÀY
import com.example.bai2.model.Customer;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
    private final Context context;
    private final List<Customer> customers;

    public CustomerAdapter(Context context, List<Customer> customers) {
        this.context = context;
        this.customers = customers;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvPoints;
        CardView card;

        //THÊM 2 BIẾN TEXTVIEW MỚI
        TextView tvCreatedAt, tvUpdatedAt;

        public ViewHolder(View v) {
            super(v);
            card = v.findViewById(R.id.cardCustomer);
            tvName = v.findViewById(R.id.tvName);
            tvPhone = v.findViewById(R.id.tvPhone);
            tvPoints = v.findViewById(R.id.tvPoints);

            //ÁNH XẠ 2 TEXTVIEW MỚI (PHẢI TRÙNG ID TRONG XML)
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

        //GÁN DỮ LIỆU CŨ
        h.tvName.setText(c.getName());
        h.tvPhone.setText("📞 " + c.getPhone());
        h.tvPoints.setText(c.getPoints() + " điểm");

        //GÁN DỮ LIỆU NGÀY THÁNG
        if (c.getCreatedAt() != null) {
            h.tvCreatedAt.setText("Tạo: " + c.getCreatedAt());
        } else {
            h.tvCreatedAt.setText("Tạo: (N/A)");
        }

        if (c.getUpdatedAt() != null) {
            h.tvUpdatedAt.setText("Cập nhật: " + c.getUpdatedAt());
        } else {
            h.tvUpdatedAt.setText("Cập nhật: (N/A)");
        }

        //SỬA HÀM ONCLICK ĐỂ MỞ ACTIVITY MỚI
        h.card.setOnClickListener(v -> {
            // Toast.makeText(context, "Khách " + c.getName(), Toast.LENGTH_SHORT).show(); // BỎ DÒNG CŨ NÀY

            // THÊM LOGIC MỚI:
            Intent intent = new Intent(context, UpdatePointsActivity.class);
            // Gửi SĐT của khách hàng này qua màn hình Update
            intent.putExtra("CUSTOMER_PHONE", c.getPhone());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return customers.size(); }
}