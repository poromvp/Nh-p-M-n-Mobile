package com.example.bai2.adapter;

import android.content.*;
import android.view.*;
import android.widget.*;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bai2.R;
import com.example.bai2.model.Customer;
import java.util.*;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
    private Context context;
    private List<Customer> customers;

    public CustomerAdapter(Context context, List<Customer> customers) {
        this.context = context;
        this.customers = customers;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvPoints;
        CardView card;
        public ViewHolder(View v) {
            super(v);
            card = v.findViewById(R.id.cardCustomer);
            tvName = v.findViewById(R.id.tvName);
            tvPhone = v.findViewById(R.id.tvPhone);
            tvPoints = v.findViewById(R.id.tvPoints);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int pos) {
        Customer c = customers.get(pos);
        h.tvName.setText(c.getName());
        h.tvPhone.setText("📞 " + c.getPhone());
        h.tvPoints.setText(c.getPoints() + " điểm");
        h.card.setOnClickListener(v ->
                Toast.makeText(context, "Khách " + c.getName(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() { return customers.size(); }
}
