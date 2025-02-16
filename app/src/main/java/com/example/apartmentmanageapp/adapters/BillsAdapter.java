package com.example.apartmentmanageapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Bill;

import java.util.List;

public class BillsAdapter extends RecyclerView.Adapter<BillsAdapter.BillViewHolder> {

    private List<Bill> bills;

    public BillsAdapter(List<Bill> bills) {
        this.bills = bills;
    }

    @Override
    public BillViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BillViewHolder holder, int position) {
        Bill bill = bills.get(position);
        holder.bind(bill);
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    public class BillViewHolder extends RecyclerView.ViewHolder {
        TextView tenantName;
        TextView amount;
        TextView dueDate;
        TextView status;

        public BillViewHolder(View itemView) {
            super(itemView);
            tenantName = itemView.findViewById(R.id.tenant_name);
            amount = itemView.findViewById(R.id.amount);
            dueDate = itemView.findViewById(R.id.due_date);
            status = itemView.findViewById(R.id.status);
        }

        public void bind(Bill bill) {
            tenantName.setText(bill.getTenantName());
            amount.setText("$" + bill.getAmount());
            dueDate.setText(bill.getDueDate());
            status.setText(bill.getStatus());
        }
    }
}
