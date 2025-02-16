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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TenantsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TenantAdapter adapter;
    private List<Tenant> tenantList;
    private FirebaseFirestore db;
    private Spinner propertySelector;
    private TextView emptyStateText;
    private String selectedPropertyId;
    private List<String> propertyIds;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

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
        ImageButton removeTenantButton = view.findViewById(R.id.remove_tenant_button);

        db = FirebaseFirestore.getInstance();
        tenantList = new ArrayList<>();
        propertyIds = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        adapter = new TenantAdapter(requireContext(), tenantList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Ensure user is logged in before proceeding
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Load properties owned by the logged-in user
        loadProperties();

        addTenantButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTenantActivity.class);
            startActivityForResult(intent, 100);
        });

        return view;
    }

    private void loadProperties() {
        String userId = currentUser.getUid();

        db.collection("properties")
                .whereEqualTo("ownerId", userId) // Filter properties by the logged-in user
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error loading properties", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        List<String> propertyNames = new ArrayList<>();
                        propertyIds.clear();

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

    private void loadTenants() {
        if (selectedPropertyId == null) {
            tenantList.clear();
            adapter.notifyDataSetChanged();
            emptyStateText.setVisibility(View.VISIBLE);
            Log.d("Firestore", "No property selected, clearing tenant list.");
            return;
        }

        Log.d("Firestore", "Fetching tenants for property ID: " + selectedPropertyId);

        db.collection("tenants")
                .whereEqualTo("property", selectedPropertyId)  // Ensure this matches the Firestore field
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error loading tenants", error);
                        return;
                    }

                    tenantList.clear();
                    if (value == null || value.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        Log.d("Firestore", "No tenants found for property ID: " + selectedPropertyId);
                    } else {
                        emptyStateText.setVisibility(View.GONE);
                        for (DocumentSnapshot tenantDoc : value.getDocuments()) {
                            Log.d("Firestore", "Tenant found: " + tenantDoc.getData());

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
                            tenant.setId(tenantDoc.getId());
                            tenant.setPropertyId(tenantDoc.getString("property")); // Ensure this matches the property ID

                            tenantList.add(tenant);
                        }
                        Log.d("Firestore", "Total tenants loaded: " + tenantList.size());
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
