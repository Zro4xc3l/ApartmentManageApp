package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Unit;

import java.util.Arrays;
import java.util.List;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.ViewHolder> {
    private List<Unit> unitList;
    private Context context;
    private int expandedPosition = -1; // Tracks which unit is expanded

    public UnitAdapter(Context context, List<Unit> unitList) {
        this.context = context;
        this.unitList = unitList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Unit unit = unitList.get(position);

        // Use getUnitId() instead of getUnitNumber()
        String unitNumberText = (unit.getUnitId() != null) ? unit.getUnitId() : "N/A";
        holder.unitNumber.setText("Unit " + unitNumberText);

        // Use getRentAmount() instead of getRentRate()
        String rentRateText = String.format("%.2f THB", unit.getRentAmount());
        holder.rentRate.setText("Rent: " + rentRateText);

        // Display Floor Level
        String floorLevelText = (unit.getFloorLevel() != null) ? unit.getFloorLevel() : "Unknown";
        holder.floorLevel.setText("Floor: " + floorLevelText);

        // Convert `amenities` from List<String> to a comma-separated string
        String amenitiesText = (unit.getAmenities() != null && !unit.getAmenities().isEmpty())
                ? String.join(", ", unit.getAmenities())
                : "No Amenities";
        holder.amenities.setText("Amenities: " + amenitiesText);

        // Display Occupancy Status with color
        if (unit.isOccupied()) {
            holder.occupancyStatus.setText("Occupied");
            holder.occupancyStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.red_500));
            holder.tenantName.setText("Tenant: " + unit.getTenantName());
        } else {
            holder.occupancyStatus.setText("Available");
            holder.occupancyStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.green_500));
            holder.tenantName.setText("Tenant: None");
        }

        // Handle Expand/Collapse
        final boolean isExpanded = position == expandedPosition;
        holder.expandedDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            expandedPosition = isExpanded ? -1 : position; // Toggle expansion
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return unitList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView unitNumber, rentRate, floorLevel, amenities, occupancyStatus, tenantName;
        View expandedDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            unitNumber = itemView.findViewById(R.id.unit_number);
            rentRate = itemView.findViewById(R.id.unit_rent_rate);
            floorLevel = itemView.findViewById(R.id.unit_floor_level);
            amenities = itemView.findViewById(R.id.unit_amenities);
            occupancyStatus = itemView.findViewById(R.id.unit_availability_status);
            tenantName = itemView.findViewById(R.id.unit_tenant_name);
            expandedDetails = itemView.findViewById(R.id.unit_expanded_details);
        }
    }
}
