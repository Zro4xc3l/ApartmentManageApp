package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.UnitBill;
import java.util.List;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder> {

    private final Context context;
    private final List<UnitBill> unitBillList;

    public SummaryAdapter(Context context, List<UnitBill> unitBillList) {
        this.context = context;
        this.unitBillList = unitBillList;
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill_summary, parent, false);
        return new SummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        UnitBill unitBill = unitBillList.get(position);

        // Set unit and tenant name
        holder.unitTenantSummary.setText(unitBill.getUnit() + " - " + unitBill.getTenantName());

        // Rent price
        holder.rentSummary.setText("Rent: ฿" + String.format("%.2f", unitBill.getRentAmount()));

        // Retrieve values (ensuring they are valid)
        double currentElectric = unitBill.getCurrentElectricReading();
        double prevElectric = unitBill.getPrevElectricReading();
        double currentWater = unitBill.getCurrentWaterReading();
        double prevWater = unitBill.getPrevWaterReading();
        double additionalFee = unitBill.getAdditionalFee();
        double serviceFee = unitBill.getServiceFee();

        // Debugging logs (Check if values are correct)
        Log.d("SummaryAdapter", "Unit: " + unitBill.getUnit());
        Log.d("SummaryAdapter", "Current Electric: " + currentElectric + ", Prev Electric: " + prevElectric);
        Log.d("SummaryAdapter", "Current Water: " + currentWater + ", Prev Water: " + prevWater);

        // Calculate electric usage and cost
        double electricUsage = (currentElectric >= prevElectric) ? (currentElectric - prevElectric) : 0;
        double electricCost = Math.max(electricUsage * unitBill.getElectricityRate(), unitBill.getMinElectricPrice());

        // Calculate water usage and cost
        double waterUsage = (currentWater >= prevWater) ? (currentWater - prevWater) : 0;
        double waterCost = Math.max(waterUsage * unitBill.getWaterRate(), unitBill.getMinWaterPrice());

        // Bind values to views
        holder.electricSummary.setText("Electric: ฿" + String.format("%.2f", electricCost));
        holder.electricUsage.setText("Usage: " + String.format("%.2f", electricUsage) + " units");
        holder.waterSummary.setText("Water: ฿" + String.format("%.2f", waterCost));
        holder.waterUsage.setText("Usage: " + String.format("%.2f", waterUsage) + " units");
        holder.serviceFeeSummary.setText("Service Fee: ฿" + String.format("%.2f", serviceFee));
        holder.additionalSummary.setText("Additional: ฿" + String.format("%.2f", additionalFee));

        // Additional description
        String additionalDesc = unitBill.getAdditionalFeeDescription() != null ? unitBill.getAdditionalFeeDescription() : "-";
        holder.additionalDescription.setText("Description: " + additionalDesc);

        // Calculate final total (Rent + Electric + Water + Service Fee + Additional Fee)
        double finalTotal = unitBill.getRentAmount() + electricCost + waterCost + serviceFee + additionalFee;
        holder.finalTotalSummary.setText("Final Total: ฿" + String.format("%.2f", finalTotal));
    }

    @Override
    public int getItemCount() {
        return unitBillList.size();
    }

    public static class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView unitTenantSummary, rentSummary, electricSummary, electricUsage, waterSummary, waterUsage;
        TextView serviceFeeSummary, additionalSummary, additionalDescription, finalTotalSummary;

        public SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            unitTenantSummary = itemView.findViewById(R.id.unit_tenant_summary);
            rentSummary = itemView.findViewById(R.id.rent_value);
            electricSummary = itemView.findViewById(R.id.electric_value);
            waterSummary = itemView.findViewById(R.id.water_value);
            serviceFeeSummary = itemView.findViewById(R.id.service_fee_value);
            additionalSummary = itemView.findViewById(R.id.additional_value);
            additionalDescription = itemView.findViewById(R.id.additional_description);
            finalTotalSummary = itemView.findViewById(R.id.final_total_summary);
        }
    }
}
