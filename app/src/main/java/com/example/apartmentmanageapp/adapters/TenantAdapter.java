package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Tenant;

import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.ViewHolder> {
    private List<Tenant> tenantList;
    private Context context;

    // Constructor
    public TenantAdapter(Context context, List<Tenant> tenantList) {
        this.context = context;
        this.tenantList = tenantList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the tenant item layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_tenant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the current tenant
        Tenant tenant = tenantList.get(position);

        // Bind tenant data to views
        holder.tenantName.setText(tenant.getName());
        holder.unitNumber.setText("Unit " + tenant.getUnitNumber());
        holder.rentStatus.setText(tenant.getRentStatus() != null ? tenant.getRentStatus() : "Unknown");

        // Set rent status color
        if ("Paid".equalsIgnoreCase(tenant.getRentStatus())) {
            holder.rentStatus.setTextColor(context.getResources().getColor(R.color.green));
        } else if ("Unpaid".equalsIgnoreCase(tenant.getRentStatus())) {
            holder.rentStatus.setTextColor(context.getResources().getColor(R.color.red));
        } else {
            holder.rentStatus.setTextColor(context.getResources().getColor(R.color.gray));
        }

        // Call button click event
        holder.callButton.setOnClickListener(v -> {
            if (tenant.getPhoneNumber() != null && !tenant.getPhoneNumber().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + tenant.getPhoneNumber()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Text button click event
        holder.textButton.setOnClickListener(v -> {
            if (tenant.getPhoneNumber() != null && !tenant.getPhoneNumber().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms:" + tenant.getPhoneNumber()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    // ViewHolder class to hold item views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tenantName, unitNumber, rentStatus;
        Button callButton, textButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tenantName = itemView.findViewById(R.id.tenant_name);
            unitNumber = itemView.findViewById(R.id.unit_number);
            rentStatus = itemView.findViewById(R.id.rent_status);
            callButton = itemView.findViewById(R.id.call_button);
            textButton = itemView.findViewById(R.id.message_button);
        }
    }
}