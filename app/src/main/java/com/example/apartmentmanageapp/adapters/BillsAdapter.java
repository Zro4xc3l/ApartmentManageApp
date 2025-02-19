package com.example.apartmentmanageapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Bill;

import java.util.List;

public class BillsAdapter extends RecyclerView.Adapter<BillsAdapter.BillViewHolder> {

    private List<Bill> billList;

    public BillsAdapter(List<Bill> billList) {
        this.billList = billList;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single bill item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = billList.get(position);

        // Bind data from the Bill object to the TextViews
        holder.tvPropertyName.setText(bill.getPropertyName());
        holder.tvTotalAmount.setText(String.format("฿%.2f", bill.getTotalAmount()));
        holder.tvBillStatus.setText(bill.getStatus());
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    // ViewHolder class for bill items
    public static class BillViewHolder extends RecyclerView.ViewHolder {

        TextView tvPropertyName;
        TextView tvTotalAmount;
        TextView tvBillStatus;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPropertyName = itemView.findViewById(R.id.tv_bill_property);
            tvTotalAmount = itemView.findViewById(R.id.tv_bill_amount);
            tvBillStatus = itemView.findViewById(R.id.tv_bill_status);
        }
    }
}
