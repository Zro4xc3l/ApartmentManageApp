package com.example.apartmentmanageapp.ui.tenants;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.TenantAdapter;
import com.example.apartmentmanageapp.model.Tenant;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TenantsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TenantAdapter adapter;
    private List<Tenant> tenantList;
    private FirebaseFirestore db;
    private Spinner propertySelector;
    private TextView emptyStateText; // Empty state message
    private String selectedPropertyId;
    private static final int ADD_TENANT_REQUEST = 100;
    private List<String> propertyIds;

    // Remove button and selection mode flag
    private ImageButton removeTenantButton;
    private boolean isSelectionMode = false;

    public TenantsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenants, container, false);

        // Initialize UI components
        recyclerView = view.findViewById(R.id.tenant_recycler_view);
        propertySelector = view.findViewById(R.id.property_selector);
        emptyStateText = view.findViewById(R.id.empty_state_text);
        FloatingActionButton addTenantButton = view.findViewById(R.id.add_tenant_button);
        removeTenantButton = view.findViewById(R.id.remove_tenant_button);

        db = FirebaseFirestore.getInstance();
        tenantList = new ArrayList<>();
        propertyIds = new ArrayList<>();

        // Setup RecyclerView with the TenantAdapter that supports selection mode.
        adapter = new TenantAdapter(requireContext(), tenantList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Load properties into spinner
        loadProperties();

        // FAB click: Open AddTenantActivity to add a new tenant.
        addTenantButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTenantActivity.class);
            startActivityForResult(intent, ADD_TENANT_REQUEST);
        });

        // Remove button click: toggle selection mode or confirm removal.
        removeTenantButton.setOnClickListener(v -> {
            if (!isSelectionMode) {
                // Enter selection mode.
                isSelectionMode = true;
                adapter.setSelectionMode(true);
                // Change icon to a "confirm" icon (assumes you have ic_confirm vector)
                removeTenantButton.setImageResource(R.drawable.ic_confirm);
            } else {
                // In selection mode, get the selected tenants.
                List<Tenant> selectedTenants = adapter.getSelectedTenants();
                if (selectedTenants.isEmpty()) {
                    Toast.makeText(getContext(), "No tenant selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Show confirmation dialog.
                new AlertDialog.Builder(requireContext())
                        .setTitle("Remove Tenants")
                        .setMessage("Are you sure you want to remove the selected tenant(s)?\nTheir units will be marked as available.")
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeSelectedTenants(selectedTenants);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        return view;
    }

    private void loadProperties() {
        db.collection("properties").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error loading properties", error);
                Toast.makeText(getContext(), "Error loading properties", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                List<String> propertyNames = new ArrayList<>();
                propertyIds.clear();

                // Default option.
                propertyNames.add("Select Property");
                propertyIds.add(null);

                for (DocumentSnapshot document : value.getDocuments()) {
                    String propertyName = document.getString("name");
                    String propertyId = document.getId();
                    propertyNames.add(propertyName);
                    propertyIds.add(propertyId);
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, propertyNames);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                propertySelector.setAdapter(spinnerAdapter);

                propertySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedPropertyId = propertyIds.get(position);

                        // When "Select Property" is chosen, clear tenant list.
                        if (selectedPropertyId == null) {
                            tenantList.clear();
                            adapter.notifyDataSetChanged();
                            emptyStateText.setVisibility(View.VISIBLE);
                            return;
                        }
                        loadTenants();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedPropertyId = null;
                        tenantList.clear();
                        adapter.notifyDataSetChanged();
                        emptyStateText.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TENANT_REQUEST && resultCode == getActivity().RESULT_OK) {
            loadTenants();
        }
    }

    private void loadTenants() {
        if (selectedPropertyId == null) {
            tenantList.clear();
            adapter.notifyDataSetChanged();
            emptyStateText.setVisibility(View.VISIBLE);
            return;
        }

        // Query tenants by property document ID directly.
        db.collection("tenants").whereEqualTo("property", selectedPropertyId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error loading tenants", error);
                        return;
                    }
                    tenantList.clear();
                    if (value == null || value.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        emptyStateText.setVisibility(View.GONE);
                        for (DocumentSnapshot tenantDoc : value.getDocuments()) {
                            Tenant tenant = new Tenant(
                                    tenantDoc.getString("first_name"),
                                    tenantDoc.getString("last_name"),
                                    tenantDoc.getString("unit"),
                                    tenantDoc.getString("phone_number"),
                                    tenantDoc.getString("email"),
                                    tenantDoc.getString("lease_start_date"),
                                    tenantDoc.getString("lease_end_date"),
                                    tenantDoc.getString("emergency_contact_name"),
                                    tenantDoc.getString("emergency_contact_phone")
                            );
                            // Set the tenant's document ID (for removal)
                            tenant.setId(tenantDoc.getId());
                            // If you store a separate property ID field, you can set it here too.
                            tenant.setPropertyId(tenantDoc.getString("property_id"));
                            tenantList.add(tenant);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }


    private void removeSelectedTenants(List<Tenant> selectedTenants) {
        // Create a copy of the selected tenants so we don't modify the list we're iterating over.
        List<Tenant> selectedCopy = new ArrayList<>(selectedTenants);

        for (Tenant tenant : selectedCopy) {
            db.collection("tenants").document(tenant.getId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        // Update the associated unit.
                        updateUnitForRemoval(tenant);
                        // Remove the tenant from the main list.
                        tenantList.remove(tenant);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to remove tenant " + tenant.getFirstName(), Toast.LENGTH_SHORT).show());
        }
        // Exit selection mode.
        isSelectionMode = false;
        adapter.setSelectionMode(false);
        removeTenantButton.setImageResource(R.drawable.ic_remove);
    }


    private void updateUnitForRemoval(Tenant tenant) {
        // Use the tenant's property ID if available; otherwise, use the selected property ID.
        String propertyId = tenant.getPropertyId();
        if (propertyId == null || propertyId.isEmpty()) {
            propertyId = selectedPropertyId;  // Fallback to the property currently selected.
        }

        String unitId = tenant.getUnit(); // Ensure this value matches the unit document ID.
        if (propertyId == null || unitId == null || unitId.isEmpty()) {
            Log.e("UpdateUnit", "Missing propertyId or unitId. propertyId: " + propertyId + ", unitId: " + unitId);
            return;
        }

        DocumentReference unitRef = db.collection("properties")
                .document(propertyId)
                .collection("units")
                .document(unitId);

        unitRef.update("occupied", false, "tenantName", "No Tenant")
                .addOnSuccessListener(aVoid -> Log.d("UpdateUnit", "Unit updated for removal"))
                .addOnFailureListener(e -> Log.e("UpdateUnit", "Failed to update unit", e));
    }

}
