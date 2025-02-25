package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Bill;
import com.example.apartmentmanageapp.ui.bills.BillsDetailActivity;

import java.util.List;

public class BillsAdapter extends RecyclerView.Adapter<BillsAdapter.BillViewHolder> {

    private final List<Bill> billList;
    private final Context context;
    private final OnBillClickListener onBillClickListener; // ✅ Click Listener

    // Click Listener interface
    public interface OnBillClickListener {
        void onBillClick(Bill bill);
    }

    // Constructor: take the context, list of bills, and the listener to handle clicks
    public BillsAdapter(Context context, List<Bill> billList, OnBillClickListener listener) {
        this.context = context;
        this.billList = billList;
        this.onBillClickListener = listener; // Assign the listener to the class variable
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill_property, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = billList.get(position);

        // Populate UI fields
        holder.tvPropertyName.setText(bill.getPropertyName());
        holder.tvBillingPeriod.setText("Billing Period: " + bill.getBillingPeriod());
        holder.tvRent.setText("Total Rent: ฿" + formatCurrency(bill.getTotalRent()));
        holder.tvElectric.setText("Total Electric: ฿" + formatCurrency(bill.getTotalElectric()));
        holder.tvWater.setText("Total Water: ฿" + formatCurrency(bill.getTotalWater()));
        holder.tvServiceFee.setText("Service Fee: ฿" + formatCurrency(bill.getServiceFee()));
        holder.tvAdditional.setText("Total Additional: ฿" + formatCurrency(bill.getTotalAdditional()));
        holder.tvGrandTotal.setText("Grand Total: ฿" + formatCurrency(bill.getGrandTotal()));
        holder.tvStatus.setText("Status: " + bill.getBillStatus());

        // Set click event to open Bill Detail Page
        holder.itemView.setOnClickListener(v -> {
            // Trigger the onBillClick when an item is clicked
            onBillClickListener.onBillClick(bill);
        });
    }

    @Override
    public int getItemCount() {
        return billList != null ? billList.size() : 0;
    }

    public static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView tvPropertyName, tvBillingPeriod, tvRent, tvElectric, tvWater, tvServiceFee,
                tvAdditional, tvGrandTotal, tvStatus;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPropertyName = itemView.findViewById(R.id.tv_bill_property);
            tvBillingPeriod = itemView.findViewById(R.id.tv_bill_billing_period);
            tvRent = itemView.findViewById(R.id.tv_bill_rent);
            tvElectric = itemView.findViewById(R.id.tv_bill_electric);
            tvWater = itemView.findViewById(R.id.tv_bill_water);
            tvServiceFee = itemView.findViewById(R.id.tv_bill_service_fee);
            tvAdditional = itemView.findViewById(R.id.tv_bill_additional);
            tvGrandTotal = itemView.findViewById(R.id.tv_bill_grand_total);
            tvStatus = itemView.findViewById(R.id.tv_bill_status);
        }
    }

    // Helper method to format currency values to two decimal places
    private String formatCurrency(double value) {
        return String.format("%.2f", value);
    }

    // Helper method to format unit values, ensuring no decimal places for unit counts
    private String formatUnits(double value) {
        return String.format("%.0f", value);
    }
}
