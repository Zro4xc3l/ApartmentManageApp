package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Tenant;
import com.example.apartmentmanageapp.ui.tenants.TenantDetailActivity; // Ensure you have this Activity
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.ViewHolder> {
    private List<Tenant> tenantList;
    private Context context;
    // Flag to control selection mode.
    private boolean selectionMode = false;
    // Track selected positions.
    private Set<Integer> selectedPositions = new HashSet<>();
    // Track expanded state per item (only used in non-selection mode).
    private SparseBooleanArray expandedPositions = new SparseBooleanArray();

    // Constructor
    public TenantAdapter(Context context, List<Tenant> tenantList) {
        this.context = context;
        this.tenantList = tenantList;
    }

    // Toggle selection mode. When disabled, clear any selections.
    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        if (!selectionMode) {
            selectedPositions.clear();
        }
        notifyDataSetChanged();
    }

    // Return a list of selected tenants.
    public List<Tenant> getSelectedTenants() {
        List<Tenant> selected = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            selected.add(tenantList.get(pos));
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the tenant item layout.
        View view = LayoutInflater.from(context).inflate(R.layout.item_tenant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tenant tenant = tenantList.get(position);

        // Set tenant details (always visible header info).
        holder.tenantName.setText(tenant.getFullName());
        holder.unitNumber.setText("Unit " + tenant.getUnit());

        // Set extra details in the expandable container.
        holder.tenantPhone.setText("Phone: " + (tenant.getPhoneNumber() != null ? tenant.getPhoneNumber() : "N/A"));
        holder.tenantEmail.setText("Email: " + (tenant.getEmail() != null ? tenant.getEmail() : "N/A"));
        holder.leasePeriod.setText("Lease: " + tenant.getLeaseStartDate() + " to " + tenant.getLeaseEndDate());
        holder.emergencyContact.setText("Emergency: " + tenant.getEmergencyContactName() +
                " (" + tenant.getEmergencyContactPhone() + ")");

        // Set click listeners for call and text buttons.
        holder.callButton.setOnClickListener(v -> {
            if (tenant.getPhoneNumber() != null && !tenant.getPhoneNumber().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + tenant.getPhoneNumber()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        holder.textButton.setOnClickListener(v -> {
            if (tenant.getPhoneNumber() != null && !tenant.getPhoneNumber().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms:" + tenant.getPhoneNumber()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        // If in selection mode, show and handle checkbox clicks.
        if (selectionMode) {
            holder.selectionCheckbox.setVisibility(View.VISIBLE);
            holder.selectionCheckbox.setChecked(selectedPositions.contains(position));
            holder.itemView.setOnClickListener(v -> {
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position);
                    holder.selectionCheckbox.setChecked(false);
                } else {
                    selectedPositions.add(position);
                    holder.selectionCheckbox.setChecked(true);
                }
            });
            // Optionally, add a separate click listener for the checkbox.
            holder.selectionCheckbox.setOnClickListener(v -> {
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position);
                } else {
                    selectedPositions.add(position);
                }
                notifyItemChanged(position);
            });
        } else {
            // Not in selection mode:
            holder.selectionCheckbox.setVisibility(View.GONE);
            // Handle expandable/collapsible behavior.
            boolean isExpanded = expandedPositions.get(position, false);
            holder.detailsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            holder.itemView.setOnClickListener(v -> {
                boolean currentlyExpanded = expandedPositions.get(position, false);
                if (!currentlyExpanded) {
                    // Expand the card.
                    expandedPositions.put(position, true);
                    holder.detailsContainer.setVisibility(View.VISIBLE);
                } else {
                    // If already expanded, navigate to tenant detail screen.
                    Intent intent = new Intent(context, TenantDetailActivity.class);
                    // Pass necessary tenant data (for example, tenant ID).
                    intent.putExtra("tenantId", tenant.getId());
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    // ViewHolder class to hold item views.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tenantName, unitNumber, tenantPhone, tenantEmail, leasePeriod, emergencyContact;
        MaterialButton callButton, textButton;
        CheckBox selectionCheckbox;
        View detailsContainer;  // Container for extra details.

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tenantName = itemView.findViewById(R.id.tenant_name);
            unitNumber = itemView.findViewById(R.id.unit_number);
            tenantPhone = itemView.findViewById(R.id.tenant_phone);
            tenantEmail = itemView.findViewById(R.id.tenant_email);
            leasePeriod = itemView.findViewById(R.id.lease_period);
            emergencyContact = itemView.findViewById(R.id.emergency_contact);
            callButton = itemView.findViewById(R.id.call_button);
            textButton = itemView.findViewById(R.id.message_button);
            selectionCheckbox = itemView.findViewById(R.id.selection_checkbox);
            detailsContainer = itemView.findViewById(R.id.details_container);
        }
    }
}
