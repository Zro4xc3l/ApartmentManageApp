package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.UnitBill;
import java.util.List;

public class UnitBillAdapter extends RecyclerView.Adapter<UnitBillAdapter.UnitBillViewHolder> {

    private static final String TAG = "UnitBillAdapter";
    private Context context;
    private List<UnitBill> unitBillList;

    public UnitBillAdapter(Context context, List<UnitBill> unitBillList) {
        this.context = context;
        this.unitBillList = unitBillList;
    }

    @NonNull
    @Override
    public UnitBillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill_card, parent, false);
        return new UnitBillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitBillViewHolder holder, int position) {
        UnitBill unitBill = unitBillList.get(position);

        // Set Unit/Tenant information
        holder.unitTenantTextView.setText(unitBill.getUnit() + " - " + unitBill.getTenantName());

        // Set previous readings loaded from DB
        holder.prevElectricReadingTextView.setText(String.valueOf(unitBill.getPrevElectricReading()));
        holder.prevWaterReadingTextView.setText(String.valueOf(unitBill.getPrevWaterReading()));

        // Remove any existing TextWatcher to avoid duplicate triggers
        if (holder.billTextWatcher != null) {
            holder.currentElectricReadingEditText.removeTextChangedListener(holder.billTextWatcher);
            holder.currentWaterReadingEditText.removeTextChangedListener(holder.billTextWatcher);
            holder.additionalFeeAmountEditText.removeTextChangedListener(holder.billTextWatcher);
        }

        // Set initial calculated prices
        updateTotalPrice(unitBill, holder);

        // Add a new TextWatcher so that any input change updates the calculated prices dynamically
        BillTextWatcher watcher = new BillTextWatcher(unitBill, holder);
        holder.currentElectricReadingEditText.addTextChangedListener(watcher);
        holder.currentWaterReadingEditText.addTextChangedListener(watcher);
        holder.additionalFeeAmountEditText.addTextChangedListener(watcher);
        holder.billTextWatcher = watcher;
    }

    @Override
    public int getItemCount() {
        return unitBillList.size();
    }

    // Method to calculate and update the price values with THB currency symbol.
    // If the current reading field is blank, the cost is 0.
    private void updateTotalPrice(UnitBill unitBill, UnitBillViewHolder holder) {
        String electricStr = holder.currentElectricReadingEditText.getText().toString().trim();
        String waterStr = holder.currentWaterReadingEditText.getText().toString().trim();
        String additionalFeeStr = holder.additionalFeeAmountEditText.getText().toString().trim();

        double currentElectric = electricStr.isEmpty() ? 0 : parseDouble(electricStr);
        double currentWater = waterStr.isEmpty() ? 0 : parseDouble(waterStr);
        double additionalFee = additionalFeeStr.isEmpty() ? 0 : parseDouble(additionalFeeStr);

        Log.d(TAG, "Current Electric Reading: " + currentElectric);
        Log.d(TAG, "Current Water Reading: " + currentWater);
        Log.d(TAG, "Additional Fee: " + additionalFee);

        // Calculate electric cost only if user has input something; else 0.
        double electricCost = 0;
        if (!electricStr.isEmpty()) {
            double electricUsage = currentElectric - unitBill.getPrevElectricReading();
            if (electricUsage < 0) {
                electricUsage = 0;
            }
            electricCost = electricUsage * unitBill.getElectricityRate();
            if (electricCost < unitBill.getMinElectricPrice()) {
                electricCost = unitBill.getMinElectricPrice();
            }
        }
        Log.d(TAG, "Calculated Electric Cost: " + electricCost);

        // Calculate water cost only if user has input something; else 0.
        double waterCost = 0;
        if (!waterStr.isEmpty()) {
            double waterUsage = currentWater - unitBill.getPrevWaterReading();
            if (waterUsage < 0) {
                waterUsage = 0;
            }
            waterCost = waterUsage * unitBill.getWaterRate();
            if (waterCost < unitBill.getMinWaterPrice()) {
                waterCost = unitBill.getMinWaterPrice();
            }
        }
        Log.d(TAG, "Calculated Water Cost: " + waterCost);

        // Update individual calculated price TextViews with THB currency symbol
        holder.electricPriceTextView.setText("฿" + String.format("%.2f", electricCost));
        holder.waterPriceTextView.setText("฿" + String.format("%.2f", waterCost));

        // Calculate final total: Rent + Electric + Water + Additional Fee
        double total = unitBill.getRentAmount() + electricCost + waterCost + additionalFee;
        Log.d(TAG, "Calculated Total Price: " + total);
        holder.finalTotalPriceTextView.setText("฿" + String.format("%.2f", total));
    }

    // Utility method to safely parse a double from a string
    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // TextWatcher to update calculated prices when input changes
    private class BillTextWatcher implements TextWatcher {
        private UnitBill unitBill;
        private UnitBillViewHolder holder;

        public BillTextWatcher(UnitBill unitBill, UnitBillViewHolder holder) {
            this.unitBill = unitBill;
            this.holder = holder;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            updateTotalPrice(unitBill, holder);
        }
    }

    public class UnitBillViewHolder extends RecyclerView.ViewHolder {
        TextView unitTenantTextView;
        TextView prevElectricReadingTextView;
        EditText currentElectricReadingEditText;
        TextView prevWaterReadingTextView;
        EditText currentWaterReadingEditText;
        EditText additionalFeeAmountEditText;
        EditText additionalFeeDescriptionEditText;
        // Calculated price fields
        TextView electricPriceTextView;
        TextView waterPriceTextView;
        TextView finalTotalPriceTextView;
        BillTextWatcher billTextWatcher;

        public UnitBillViewHolder(@NonNull View itemView) {
            super(itemView);
            unitTenantTextView = itemView.findViewById(R.id.unit_tenant_text_view);
            prevElectricReadingTextView = itemView.findViewById(R.id.prev_electric_reading_text_view);
            currentElectricReadingEditText = itemView.findViewById(R.id.current_electric_reading_edit_text);
            prevWaterReadingTextView = itemView.findViewById(R.id.prev_water_reading_text_view);
            currentWaterReadingEditText = itemView.findViewById(R.id.current_water_reading_edit_text);
            additionalFeeAmountEditText = itemView.findViewById(R.id.additional_fee_amount_edit_text);
            additionalFeeDescriptionEditText = itemView.findViewById(R.id.additional_fee_description_edit_text);
            // These IDs must match your updated card layout
            electricPriceTextView = itemView.findViewById(R.id.electric_price_text_view);
            waterPriceTextView = itemView.findViewById(R.id.water_price_text_view);
            finalTotalPriceTextView = itemView.findViewById(R.id.final_total_price_text_view);
        }
    }
}
