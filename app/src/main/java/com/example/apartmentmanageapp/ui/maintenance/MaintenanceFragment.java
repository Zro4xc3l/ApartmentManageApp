package com.example.apartmentmanageapp.ui.maintenance;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.MaintenanceAdapter;
import com.example.apartmentmanageapp.model.Maintenance;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaintenanceFragment extends Fragment {

    private RecyclerView recyclerView;
    private MaintenanceAdapter adapter;
    private List<Maintenance> maintenanceList;
    private Spinner filterSpinner;
    private ImageButton removeButton, refreshButton;
    private FloatingActionButton addRequestButton;
    private FirebaseFirestore db;
    private ArrayAdapter<String> filterAdapter;
    private final List<String> statusOptions = new ArrayList<>();

    public MaintenanceFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maintenance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        removeButton = view.findViewById(R.id.remove_button);
        refreshButton = view.findViewById(R.id.refresh_button);
        filterSpinner = view.findViewById(R.id.filter_spinner);
        recyclerView = view.findViewById(R.id.maintenance_recycler_view);
        addRequestButton = view.findViewById(R.id.add_request_button);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        maintenanceList = new ArrayList<>();
        adapter = new MaintenanceAdapter(maintenanceList, this::expandItemDetails);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Initialize Spinner with three options: All, Pending, Complete
        setupFilterSpinner();

        // Load initial data
        loadMaintenanceRequests();

        // Set event listeners
        refreshButton.setOnClickListener(v -> loadMaintenanceRequests());

        addRequestButton.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), RequestMaintenance.class));
        });

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterByStatus(statusOptions.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        removeButton.setOnClickListener(v -> removeSelectedItems());
    }

    /**
     * Fetches maintenance requests from Firestore and populates the RecyclerView.
     */
    private void loadMaintenanceRequests() {
        db.collection("maintenanceRequests").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                maintenanceList.clear();
                // Use a set to collect unique statuses.
                Set<String> uniqueStatuses = new HashSet<>();
                // We'll update the spinner options later. Our base options:
                uniqueStatuses.add("Pending");
                uniqueStatuses.add("Complete");
                // "All" is handled separately in filtering.
                uniqueStatuses.add("All");

                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null && !snapshot.isEmpty()) {
                    int totalDocuments = snapshot.size();
                    final int[] processedCount = {0};

                    for (QueryDocumentSnapshot document : snapshot) {
                        Maintenance maintenance = document.toObject(Maintenance.class);
                        maintenance.setId(document.getId());

                        // Set the property field from Firestore (using key "property")
                        String propertyValue = document.getString("property");
                        maintenance.setProperty(propertyValue);

                        uniqueStatuses.add(maintenance.getStatus());

                        // For each maintenance request, fetch the property details.
                        if (propertyValue != null && !propertyValue.isEmpty()) {
                            db.collection("properties").document(propertyValue)
                                    .get()
                                    .addOnSuccessListener(propertyDoc -> {
                                        if (propertyDoc.exists()) {
                                            String propertyName = propertyDoc.getString("name");
                                            maintenance.setPropertyName(propertyName);
                                        }
                                        // For now, we simply store the unit value.
                                        maintenance.setUnitName(maintenance.getUnit());
                                        maintenanceList.add(maintenance);
                                        processedCount[0]++;

                                        // If all documents have been processed, update the UI.
                                        if (processedCount[0] == totalDocuments) {
                                            updateFilterOptions(new ArrayList<>(uniqueStatuses));
                                            filterSpinner.setSelection(0); // ensure "All" is selected
                                            filterByStatus("All");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("MaintenanceFragment", "Error fetching property: " + e.getMessage());
                                        maintenanceList.add(maintenance);
                                        processedCount[0]++;
                                        if (processedCount[0] == totalDocuments) {
                                            updateFilterOptions(new ArrayList<>(uniqueStatuses));
                                            filterSpinner.setSelection(0);
                                            filterByStatus("All");
                                        }
                                    });
                        } else {
                            // If property is missing, add the maintenance item anyway.
                            maintenanceList.add(maintenance);
                            processedCount[0]++;
                            if (processedCount[0] == totalDocuments) {
                                updateFilterOptions(new ArrayList<>(uniqueStatuses));
                                filterSpinner.setSelection(0);
                                filterByStatus("All");
                            }
                        }
                    }
                } else {
                    // No documents found—update adapter with an empty list.
                    adapter.updateData(maintenanceList);
                }
            } else {
                Log.w("MaintenanceFragment", "Error getting documents.", task.getException());
                Toast.makeText(getContext(), "Failed to load maintenance requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilterSpinner() {
        // Define the options explicitly: All, Pending, and Complete.
        statusOptions.clear();
        statusOptions.add("All");
        statusOptions.add("Pending");
        statusOptions.add("Complete");
        filterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, statusOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
    }

    private void updateFilterOptions(List<String> statuses) {
        // We want to ensure our spinner always has "All", "Pending", and "Complete"
        // regardless of what Firestore returns.
        statusOptions.clear();
        statusOptions.add("All");
        statusOptions.add("Pending");
        statusOptions.add("Complete");
        filterAdapter.notifyDataSetChanged();
    }

    private void filterByStatus(String status) {
        List<Maintenance> filteredList = new ArrayList<>();
        for (Maintenance m : maintenanceList) {
            if (status.equals("All")) {
                // When filter is "All", show only items that are NOT marked as "Complete".
                if (!m.getStatus().equalsIgnoreCase("Complete")) {
                    filteredList.add(m);
                }
            } else if (m.getStatus().equalsIgnoreCase(status)) {
                filteredList.add(m);
            }
        }
        adapter.updateData(filteredList);
    }

    private void removeSelectedItems() {
        Set<String> selectedIds = adapter.getSelectedItems();
        if (selectedIds.isEmpty()) {
            Toast.makeText(getContext(), "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String id : selectedIds) {
            db.collection("maintenanceRequests").document(id).delete()
                    .addOnSuccessListener(aVoid -> Log.d("MaintenanceFragment", "Deleted " + id))
                    .addOnFailureListener(e -> Log.w("MaintenanceFragment", "Error deleting " + id, e));
        }

        maintenanceList.removeIf(m -> selectedIds.contains(m.getId()));
        adapter.updateData(maintenanceList);
        Toast.makeText(getContext(), "Selected items removed", Toast.LENGTH_SHORT).show();
    }

    private void expandItemDetails(Maintenance maintenance) {
        adapter.toggleExpandedState(maintenance.getId());
    }
}
