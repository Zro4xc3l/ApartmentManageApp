package com.example.apartmentmanageapp.ui.tenants;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.TenantAdapter;
import com.example.apartmentmanageapp.model.Tenant;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TenantsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TenantAdapter adapter;
    private List<Tenant> tenantList;
    private FirebaseFirestore db;
    private Spinner propertySelector;
    private String selectedPropertyId;
    private static final int ADD_TENANT_REQUEST = 100;

    public TenantsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenants, container, false);

        // Initialize the RecyclerView, Spinner, and FAB from the inflated layout
        recyclerView = view.findViewById(R.id.unit_recycler_view);
        propertySelector = view.findViewById(R.id.property_selector);
        FloatingActionButton addTenantButton = view.findViewById(R.id.add_unit_button);

        db = FirebaseFirestore.getInstance();
        tenantList = new ArrayList<>();

        // Load properties to populate the selector
        loadProperties();

        adapter = new TenantAdapter(requireContext(), tenantList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Set FAB click listener to open AddTenantActivity
        addTenantButton.setOnClickListener(v -> {
            if (selectedPropertyId != null) {
                Intent intent = new Intent(getActivity(), AddTenantActivity.class);
                intent.putExtra("propertyId", selectedPropertyId);
                startActivityForResult(intent, ADD_TENANT_REQUEST);
            } else {
                Toast.makeText(getContext(), "Please select a property first", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadProperties() {
        CollectionReference propertiesRef = db.collection("properties");
        propertiesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> propertyNames = new ArrayList<>();
                List<String> propertyIds = new ArrayList<>();

                propertyNames.add("Select Property"); // Default option
                propertyIds.add(null);

                for (DocumentSnapshot document : task.getResult()) {
                    String propertyName = document.getString("name");
                    String propertyId = document.getId();
                    propertyNames.add(propertyName);
                    propertyIds.add(propertyId);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, propertyNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                propertySelector.setAdapter(adapter);

                propertySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedPropertyId = propertyIds.get(position);
                        loadTenants();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedPropertyId = null;
                        tenantList.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Failed to load properties", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TENANT_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            loadTenants();
        }
    }

    private void loadTenants() {
        if (selectedPropertyId == null) {
            tenantList.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        CollectionReference tenantsRef = db.collection("tenants");
        tenantsRef.whereEqualTo("propertyId", selectedPropertyId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tenantList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    Tenant tenant = new Tenant(
                            document.getString("firstName"),
                            document.getString("lastName"),
                            document.getString("roomNumber"),
                            document.getString("rentStatus"),
                            document.getString("phoneNumber")
                    );
                    tenantList.add(tenant);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Failed to load tenants", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
