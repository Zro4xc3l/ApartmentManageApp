package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Unit;
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
        holder.unitNumber.setText("Unit " + unit.getUnitNumber());
        holder.tenantName.setText(unit.getTenantName());
        holder.rentStatus.setText(unit.getRentStatus());

        // Change color based on rent status
        if ("Paid".equalsIgnoreCase(unit.getRentStatus())) {
            holder.rentStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else {
            holder.rentStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return unitList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView unitNumber, tenantName, rentStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            unitNumber = itemView.findViewById(R.id.unit_number);
            tenantName = itemView.findViewById(R.id.tenant_name);
            rentStatus = itemView.findViewById(R.id.rent_status);
        }
    }
}
