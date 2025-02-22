package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.UnitBill;
import java.util.List;

public class BillUnitDetailsAdapter extends RecyclerView.Adapter<BillUnitDetailsAdapter.UnitBillViewHolder> {

    private Context context;
    private List<UnitBill> unitBillList;

    public BillUnitDetailsAdapter(Context context, List<UnitBill> unitBillList) {
        this.context = context;
        this.unitBillList = unitBillList;
    }

    @NonNull
    @Override
    public UnitBillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_unit_bill_detail, parent, false);
        return new UnitBillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitBillViewHolder holder, int position) {
        UnitBill unitBill = unitBillList.get(position);

        // Header: Unit and Tenant
        holder.tvUnitHeader.setText("Unit " + unitBill.getUnit() + " - " + unitBill.getTenantName());

        // Electric price calculation
        double currentElectric = (unitBill.getCurrentElectricReading() != null ? unitBill.getCurrentElectricReading() : 0);
        double electricUsage = Math.max(0, currentElectric - unitBill.getPrevElectricReading());
        double electricPrice = Math.max(unitBill.getMinElectricPrice(), electricUsage * unitBill.getElectricityRate());
        holder.tvElectricPrice.setText("Price: ฿" + String.format("%.2f", electricPrice));

        // Water price calculation
        double currentWater = (unitBill.getCurrentWaterReading() != null ? unitBill.getCurrentWaterReading() : 0);
        double waterUsage = Math.max(0, currentWater - unitBill.getPrevWaterReading());
        double waterPrice = Math.max(unitBill.getMinWaterPrice(), waterUsage * unitBill.getWaterRate());
        holder.tvWaterPrice.setText("Price: ฿" + String.format("%.2f", waterPrice));

        // Additional Fee and Description
        double additionalFee = (unitBill.getAdditionalFee() != null ? unitBill.getAdditionalFee() : 0);
        holder.tvAdditionalFee.setText("฿" + String.format("%.2f", additionalFee));
        holder.tvAdditionalDesc.setText("Description: " + (unitBill.getAdditionalFeeDescription() != null ? unitBill.getAdditionalFeeDescription() : "None"));

        // Total Price Calculation
        double totalPrice = unitBill.getRentAmount() + electricPrice + waterPrice + additionalFee;
        holder.tvTotalPrice.setText("Total Price: ฿" + String.format("%.2f", totalPrice));
    }


    @Override
    public int getItemCount() {
        return unitBillList != null ? unitBillList.size() : 0;
    }

    public static class UnitBillViewHolder extends RecyclerView.ViewHolder {

        TextView tvUnitHeader;
        TextView tvElectricPrice;
        TextView tvWaterPrice;
        TextView tvAdditionalFee;
        TextView tvAdditionalDesc;
        TextView tvTotalPrice;

        public UnitBillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUnitHeader = itemView.findViewById(R.id.tv_unit_header);
            tvElectricPrice = itemView.findViewById(R.id.tv_electric_price);
            tvWaterPrice = itemView.findViewById(R.id.tv_water_price);
            tvAdditionalFee = itemView.findViewById(R.id.tv_additional_fee);
            tvAdditionalDesc = itemView.findViewById(R.id.tv_additional_desc);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
        }
    }
}
