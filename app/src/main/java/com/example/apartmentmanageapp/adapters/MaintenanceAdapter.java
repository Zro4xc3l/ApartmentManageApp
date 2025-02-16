package com.example.apartmentmanageapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Maintenance;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.ViewHolder> {

    private List<Maintenance> maintenanceList;
    private final OnItemClickListener listener;
    private final Set<String> expandedItems = new HashSet<>();
    private final Set<String> selectedItems = new HashSet<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnItemClickListener {
        void onItemClick(Maintenance maintenance);
    }

    public MaintenanceAdapter(List<Maintenance> maintenanceList, OnItemClickListener listener) {
        this.maintenanceList = maintenanceList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus, tvDate, tvPropertyName, tvUnit;
        ImageView imagePreview;
        ImageButton expandButton;
        CheckBox checkboxSelect;
        Button buttonMarkComplete;
        View expandedLayout;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_request_title);
            tvDescription = view.findViewById(R.id.tv_request_description);
            tvStatus = view.findViewById(R.id.tv_request_status);
            tvDate = view.findViewById(R.id.tv_request_date);
            tvPropertyName = view.findViewById(R.id.tv_property_name);
            tvUnit = view.findViewById(R.id.tv_unit_id);
            imagePreview = view.findViewById(R.id.image_preview);
            expandButton = view.findViewById(R.id.expand_button);
            checkboxSelect = view.findViewById(R.id.checkbox_select);
            buttonMarkComplete = view.findViewById(R.id.button_mark_complete);
            expandedLayout = view.findViewById(R.id.expanded_layout);
        }
    }

    @NonNull
    @Override
    public MaintenanceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maintenance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Maintenance maintenance = maintenanceList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(maintenance.getTitle());
        holder.tvStatus.setText("Status: " + maintenance.getStatus());
        holder.tvDate.setText("Created: " + maintenance.getExpectedDate());

        // Set status text color dynamically
        if (maintenance.getStatus().equalsIgnoreCase("Complete")) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorComplete));
        } else if (maintenance.getStatus().equalsIgnoreCase("Pending")) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPending));
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorDefault));
        }

        // Use the correct field for property id (now "property")
        String propertyId = maintenance.getProperty();
        String unitField = maintenance.getUnit();

        // Query Firestore for the property details if available.
        if (propertyId != null && !propertyId.isEmpty()) {
            db.collection("properties").document(propertyId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String propertyName = documentSnapshot.getString("name");
                            if (propertyName != null) {
                                holder.tvPropertyName.setText("Property: " + propertyName);
                            } else {
                                holder.tvPropertyName.setText("Property: Unknown");
                            }
                        } else {
                            holder.tvPropertyName.setText("Property: Unknown");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching property: " + e.getMessage());
                        holder.tvPropertyName.setText("Property: Unknown");
                    });
        } else {
            holder.tvPropertyName.setText("Property: Unknown");
        }

        // Format and display the unit as "Unit: Unit xxx"
        if (unitField != null && !unitField.isEmpty()) {
            String unitNumber = unitField.trim();
            if (unitNumber.toLowerCase().startsWith("unit ")) {
                unitNumber = unitNumber.substring(5).trim();
            }
            holder.tvUnit.setText("Unit: Unit " + unitNumber);
        } else {
            holder.tvUnit.setText("Unit: Unknown");
        }

        // Handle expansion logic
        boolean isExpanded = expandedItems.contains(maintenance.getId());
        holder.expandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if (isExpanded) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(maintenance.getDescription());

            // Handle image preview
            if (maintenance.getImageUrl() != null && !maintenance.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(maintenance.getImageUrl())
                        .into(holder.imagePreview);
                holder.imagePreview.setVisibility(View.VISIBLE);
            } else {
                holder.imagePreview.setVisibility(View.GONE);
            }
        } else {
            holder.tvDescription.setVisibility(View.GONE);
            holder.imagePreview.setVisibility(View.GONE);
        }

        // Expand/collapse listener
        holder.expandButton.setOnClickListener(v -> toggleExpandedState(maintenance.getId()));

        // Handle checkbox selection
        holder.checkboxSelect.setChecked(selectedItems.contains(maintenance.getId()));
        holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedItems.add(maintenance.getId());
            } else {
                selectedItems.remove(maintenance.getId());
            }
        });

        // Mark as Complete button click listener
        holder.buttonMarkComplete.setOnClickListener(v -> {
            // Update the status field in Firestore to "Complete"
            db.collection("maintenanceRequests").document(maintenance.getId())
                    .update("status", "Complete")
                    .addOnSuccessListener(aVoid -> {
                        maintenance.setStatus("Complete");
                        notifyItemChanged(position);
                        Toast.makeText(context, "Marked as Complete", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MaintenanceAdapter", "Error updating status: " + e.getMessage());
                        Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show();
                    });
        });

        // Item click listener
        holder.itemView.setOnClickListener(v -> listener.onItemClick(maintenance));
    }

    public void toggleExpandedState(String id) {
        if (expandedItems.contains(id)) {
            expandedItems.remove(id);
        } else {
            expandedItems.add(id);
        }
        notifyDataSetChanged();
    }

    public void updateData(List<Maintenance> newData) {
        this.maintenanceList = newData;
        notifyDataSetChanged();
    }

    public Set<String> getSelectedItems() {
        return selectedItems;
    }

    @Override
    public int getItemCount() {
        return maintenanceList.size();
    }
}
