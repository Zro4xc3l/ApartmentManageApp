package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.example.apartmentmanageapp.ui.units.RoomDetailActivity;

import java.util.List;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.ViewHolder> {
    private List<Unit> unitList;
    private Context context;

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
        holder.unitNumber.setText("Unit " + (unit.getUnitNumber() != null ? unit.getUnitNumber() : "N/A"));
        holder.tenantName.setText(unit.isOccupied() ? unit.getTenantName() : "No Tenant");
        holder.rentStatus.setText(unit.isOccupied() ? unit.getRentStatus() : "N/A");

        // Change color based on rent status
        if ("Paid".equalsIgnoreCase(unit.getRentStatus())) {
            holder.rentStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else {
            holder.rentStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
        }

        // Set OnClickListener directly on CardView to open RoomDetailActivity
        holder.cardView.setOnClickListener(v -> {
            Log.d("UnitAdapter", "Unit clicked: " + unit.getUnitNumber());
            Intent intent = new Intent(context, RoomDetailActivity.class);
            intent.putExtra("unitId", unit.getId());
            intent.putExtra("unitNumber", unit.getUnitNumber() != null ? unit.getUnitNumber() : "N/A");
            intent.putExtra("tenantName", unit.isOccupied() ? unit.getTenantName() : "No Tenant");
            intent.putExtra("rentStatus", unit.isOccupied() ? unit.getRentStatus() : "N/A");
            intent.putExtra("roomSize", unit.getRoomSize() != null ? unit.getRoomSize() : "Unknown");
            intent.putExtra("floorLevel", unit.getFloorLevel() != null ? unit.getFloorLevel() : "Unknown");
            intent.putExtra("rentAmount", unit.getRentAmount());
            intent.putExtra("availabilityStatus", unit.isOccupied() ? "Occupied" : "Vacant");
            intent.putExtra("amenities", unit.getAmenities() != null ? unit.getAmenities() : "None");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return unitList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView unitNumber, tenantName, rentStatus;
        View cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            unitNumber = itemView.findViewById(R.id.unit_number);
            tenantName = itemView.findViewById(R.id.tenant_name);
            rentStatus = itemView.findViewById(R.id.rent_status);
            cardView = itemView;  // Reference CardView for click listener
        }
    }
}