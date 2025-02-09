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
import com.example.apartmentmanageapp.model.Property;
import com.example.apartmentmanageapp.ui.units.UnitsActivity;

import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.ViewHolder> {
    private List<Property> propertyList;
    private Context context;

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
        holder.propertyName.setText(property.getName());
        holder.propertyAddress.setText(property.getAddress());
        holder.unitCount.setText(property.getUnitCount() + " Units");

        // ✅ Open UnitsActivity when a property is clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UnitsActivity.class);
            intent.putExtra("propertyId", property.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView propertyName, propertyAddress, unitCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            propertyName = itemView.findViewById(R.id.property_name);
            propertyAddress = itemView.findViewById(R.id.property_address);
            unitCount = itemView.findViewById(R.id.property_units);
        }
    }
}
