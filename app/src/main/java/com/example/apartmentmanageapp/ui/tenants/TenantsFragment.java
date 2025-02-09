package com.example.apartmentmanageapp.ui.tenants;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.TenantAdapter;
import com.example.apartmentmanageapp.model.Tenant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TenantsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TenantAdapter adapter;
    private List<Tenant> tenantList;
    private List<Tenant> filteredList;
    private EditText searchBar;
    private FirebaseFirestore db;
    private static final int ADD_TENANT_REQUEST = 100;

    public TenantsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenants, container, false);

        recyclerView = view.findViewById(R.id.tenant_recycler_view);
        searchBar = view.findViewById(R.id.search_bar);
        FloatingActionButton addTenantButton = view.findViewById(R.id.add_tenant_button);

        db = FirebaseFirestore.getInstance();
        tenantList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Load tenants from Firestore
        loadTenants();

        adapter = new TenantAdapter(requireContext(), filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTenants(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Open AddTenantActivity when FAB is clicked
        addTenantButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTenantActivity.class);
            startActivityForResult(intent, ADD_TENANT_REQUEST);
        });

        return view;
    }

    private void loadTenants() {
        CollectionReference tenantsRef = db.collection("tenants");
        tenantsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tenantList.clear();
                filteredList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    Tenant tenant = new Tenant(
                            document.getString("name"),
                            document.getString("unit"),
                            document.getString("status"),
                            document.getString("phone")
                    );
                    tenantList.add(tenant);
                }
                filteredList.addAll(tenantList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_TENANT_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            String name = data.getStringExtra("name");
            String unit = data.getStringExtra("unit");
            String status = data.getStringExtra("status");
            String phone = data.getStringExtra("phone");

            // Save to Firestore
            saveTenantToFirestore(name, unit, status, phone);
        }
    }

    private void saveTenantToFirestore(String name, String unit, String status, String phone) {
        Map<String, Object> tenant = new HashMap<>();
        tenant.put("name", name);
        tenant.put("unit", unit);
        tenant.put("status", status);
        tenant.put("phone", phone);

        db.collection("tenants").add(tenant)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Tenant added", Toast.LENGTH_SHORT).show();
                    loadTenants(); // Refresh tenant list
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add tenant", Toast.LENGTH_SHORT).show());
    }

    private void filterTenants(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(tenantList);
        } else {
            for (Tenant tenant : tenantList) {
                if (tenant.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(tenant);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
