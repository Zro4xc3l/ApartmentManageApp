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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UnitBillAdapter extends RecyclerView.Adapter<UnitBillAdapter.UnitBillViewHolder> {

    private static final String TAG = "UnitBillAdapter";
    private final Context context;
    private final List<UnitBill> unitBillList;
    private boolean showValidationErrors = false;

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

        holder.unitTenantTextView.setText(unitBill.getUnit() + " - " + unitBill.getTenantName());
        holder.prevElectricReadingTextView.setText(String.valueOf(unitBill.getPrevElectricReading()));
        holder.prevWaterReadingTextView.setText(String.valueOf(unitBill.getPrevWaterReading()));
        holder.rentPriceTextView.setText("Rent: ฿" + String.format("%.2f", unitBill.getRentAmount()));

        // ✅ Fetch Service Fee from Firestore (Read-Only)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (unitBill.getPropertyId() != null) {
            db.collection("properties")
                    .document(unitBill.getPropertyId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("serviceFee")) {
                            double serviceFee = documentSnapshot.getDouble("serviceFee");
                            unitBill.setServiceFee(serviceFee);
                            holder.serviceFeeEditText.setText(String.format("%.2f", serviceFee));
                        } else {
                            holder.serviceFeeEditText.setText(""); // ✅ Leave Blank if Not Found
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch service fee", e);
                        holder.serviceFeeEditText.setText(""); // ✅ Leave Blank
                    });
        } else {
            holder.serviceFeeEditText.setText(""); // ✅ Leave Blank
        }

        // ✅ Current input fields are blank (User must enter values)
        holder.currentElectricReadingEditText.setText("");
        holder.currentWaterReadingEditText.setText("");

        // Set Service Fee as **Read-Only**
        holder.serviceFeeEditText.setFocusable(false);
        holder.serviceFeeEditText.setFocusableInTouchMode(false);
        holder.serviceFeeEditText.setClickable(false);

        // Calculate the total price initially
        updateTotalPrice(unitBill, holder);

        // Attach TextWatcher
        BillTextWatcher watcher = new BillTextWatcher(unitBill, holder);
        holder.currentElectricReadingEditText.addTextChangedListener(watcher);
        holder.currentWaterReadingEditText.addTextChangedListener(watcher);
        holder.billTextWatcher = watcher;
    }

    @Override
    public int getItemCount() {
        return unitBillList != null ? unitBillList.size() : 0;
    }

    /**
     * ✅ Validates that ONLY Current Electric & Water Readings Are Required.
     * Service Fee & Additional Fee Can Be Empty.
     */
    public boolean validateAllFields() {
        boolean valid = true;
        for (UnitBill unitBill : unitBillList) {
            if (isEmpty(unitBill.getCurrentElectricReading())) {
                valid = false;
                break;
            }
            if (isEmpty(unitBill.getCurrentWaterReading())) {
                valid = false;
                break;
            }
        }
        showValidationErrors = !valid;
        notifyDataSetChanged();
        return valid;
    }


    private void updateTotalPrice(UnitBill unitBill, UnitBillViewHolder holder) {
        double currentElectric = parseOrDefault(holder.currentElectricReadingEditText, 0.0); // ✅ Defaults to 0 if empty
        double currentWater = parseOrDefault(holder.currentWaterReadingEditText, 0.0); // ✅ Defaults to 0 if empty

        double electricUsage = Math.max(0, currentElectric - unitBill.getPrevElectricReading());
        double electricCost = Math.max(electricUsage * unitBill.getElectricityRate(), unitBill.getMinElectricPrice());

        double waterUsage = Math.max(0, currentWater - unitBill.getPrevWaterReading());
        double waterCost = Math.max(waterUsage * unitBill.getWaterRate(), unitBill.getMinWaterPrice());

        double serviceFee = parseOrDefault(holder.serviceFeeEditText, 0.0); // ✅ Defaults to 0 if empty

        double total = unitBill.getRentAmount() + electricCost + waterCost + serviceFee;

        holder.electricPriceTextView.setText("฿" + String.format("%.2f", electricCost));
        holder.waterPriceTextView.setText("฿" + String.format("%.2f", waterCost));
        holder.finalTotalPriceTextView.setText("฿" + String.format("%.2f", total));
    }

    private double parseOrDefault(EditText editText, double defaultValue) {
        String s = editText.getText().toString().trim();
        if (s.isEmpty()) return defaultValue; // ✅ If empty, use default value (0.00 for service fee)
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean isEmpty(Double value) {
        return value == null || value == 0.0;
    }

    private class BillTextWatcher implements TextWatcher {
        private final UnitBill unitBill;
        private final UnitBillViewHolder holder;

        public BillTextWatcher(UnitBill unitBill, UnitBillViewHolder holder) {
            this.unitBill = unitBill;
            this.holder = holder;
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateTotalPrice(unitBill, holder);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }
    }

    public static class UnitBillViewHolder extends RecyclerView.ViewHolder {
        TextView unitTenantTextView, prevElectricReadingTextView, prevWaterReadingTextView,
                electricPriceTextView, waterPriceTextView, finalTotalPriceTextView, rentPriceTextView;
        EditText currentElectricReadingEditText, currentWaterReadingEditText, serviceFeeEditText;
        BillTextWatcher billTextWatcher;

        public UnitBillViewHolder(@NonNull View itemView) {
            super(itemView);
            unitTenantTextView = itemView.findViewById(R.id.unit_tenant_text_view);
            prevElectricReadingTextView = itemView.findViewById(R.id.prev_electric_reading_text_view);
            currentElectricReadingEditText = itemView.findViewById(R.id.current_electric_reading_edit_text);
            prevWaterReadingTextView = itemView.findViewById(R.id.prev_water_reading_text_view);
            currentWaterReadingEditText = itemView.findViewById(R.id.current_water_reading_edit_text);
            electricPriceTextView = itemView.findViewById(R.id.electric_price_text_view);
            waterPriceTextView = itemView.findViewById(R.id.water_price_text_view);
            finalTotalPriceTextView = itemView.findViewById(R.id.final_total_price_text_view);
            rentPriceTextView = itemView.findViewById(R.id.rent_price_text_view);
            serviceFeeEditText = itemView.findViewById(R.id.service_fee_edit_text);
        }
    }
}
