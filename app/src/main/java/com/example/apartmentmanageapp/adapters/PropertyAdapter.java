package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Property;
import com.example.apartmentmanageapp.ui.units.UnitsActivity;

import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.ViewHolder> {
    private final List<Property> propertyList;
    private final Context context;

    public PropertyAdapter(Context context, List<Property> propertyList) {
        this.context = context;
        this.propertyList = propertyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_property, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = propertyList.get(position);

        // Set property name and address
        holder.propertyName.setText(property.getName());
        holder.propertyAddress.setText(property.getAddress());

        // Display unit count (Fallback if data is missing)
        String unitText = (property.getUnitCount() > 0) ? property.getUnitCount() + " Units" : "No Units";
        holder.unitCount.setText(unitText);

        // Format utility rates
        String electricityRate = String.format("%.2f THB", property.getElectricityRate());
        String waterRate = String.format("%.2f THB", property.getWaterRate());

        holder.electricityRateText.setText("Electricity Rate: " + electricityRate);
        holder.waterRateText.setText("Water Rate: " + waterRate);

        // Handle property selection click event
        holder.itemView.setOnClickListener(v -> {
            String propertyId = property.getId();
            if (propertyId == null || propertyId.isEmpty()) {
                Toast.makeText(context, "Error: Property ID is missing!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Navigate to UnitsActivity with the selected property ID
            Intent intent = new Intent(context, UnitsActivity.class);
            intent.putExtra("propertyId", propertyId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return (propertyList != null) ? propertyList.size() : 0;
    }

    /**
     * Update the adapter list dynamically when Firestore data changes.
     */
    public void updateList(List<Property> newList) {
        if (newList != null) {
            propertyList.clear();
            propertyList.addAll(newList);
            notifyDataSetChanged();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView propertyName, propertyAddress, unitCount;
        TextView electricityRateText, waterRateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            propertyName = itemView.findViewById(R.id.property_name);
            propertyAddress = itemView.findViewById(R.id.property_address);
            unitCount = itemView.findViewById(R.id.property_units);
            electricityRateText = itemView.findViewById(R.id.property_electricity_rate);
            waterRateText = itemView.findViewById(R.id.property_water_rate);
        }
    }
}
