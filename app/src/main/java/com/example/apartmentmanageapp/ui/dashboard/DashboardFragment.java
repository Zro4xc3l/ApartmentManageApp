package com.example.apartmentmanageapp.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.ui.properties.AddPropertyActivity;
import com.example.apartmentmanageapp.ui.tenants.AddTenantActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DashboardFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView managerNameText, unpaidUnitsValue, occupancyRateValue, vacancyRateValue, totalUnitsValue;
    private Button btnAddTenant, btnAddProperty, btnRequestMaintenance, btnSignOut;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize Firestore & FirebaseAuth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Link UI Elements
        managerNameText = view.findViewById(R.id.tv_apartment_manager);
        unpaidUnitsValue = view.findViewById(R.id.tv_unpaid_units_value);
        occupancyRateValue = view.findViewById(R.id.tv_occupancy_rate_value);
        vacancyRateValue = view.findViewById(R.id.tv_vacancy_rate_value);
        totalUnitsValue = view.findViewById(R.id.tv_total_units_value);

        btnAddTenant = view.findViewById(R.id.btn_add_tenant);
        btnAddProperty = view.findViewById(R.id.btn_add_property);
        btnRequestMaintenance = view.findViewById(R.id.btn_request_maintenance);
        btnSignOut = view.findViewById(R.id.btn_sign_out);

        // Set up button listeners
        btnAddTenant.setOnClickListener(v -> openAddTenant());
        btnAddProperty.setOnClickListener(v -> openAddProperty());

        // Disable "Request Maintenance" for now
        btnRequestMaintenance.setEnabled(false);
        btnRequestMaintenance.setAlpha(0.5f); // Visually indicate it's disabled

        // Sign Out Button
        btnSignOut.setOnClickListener(v -> signOutUser());

        // Load Data
        loadUserName();
        loadDashboardData();

        return view;
    }

    /**
     * Fetches and Displays the Logged-in User's Name
     */
    private void loadUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            managerNameText.setText("Unknown Manager");
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("fullName");
                        managerNameText.setText(userName != null ? userName : "Unknown Manager");
                    } else {
                        managerNameText.setText("Unknown Manager");
                    }
                })
                .addOnFailureListener(e -> managerNameText.setText("Unknown Manager"));
    }

    /**
     * Opens the Add Tenant Activity
     */
    private void openAddTenant() {
        Intent intent = new Intent(getActivity(), AddTenantActivity.class);
        startActivity(intent);
    }

    /**
     * Opens the Add Property Activity
     */
    private void openAddProperty() {
        Intent intent = new Intent(getActivity(), AddPropertyActivity.class);
        startActivity(intent);
    }

    /**
     * Loads Dashboard Data from Firestore
     */
    private void loadDashboardData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        AtomicInteger totalUnits = new AtomicInteger(0);      // Total units count
        AtomicInteger unpaidUnits = new AtomicInteger(0);     // Count of units with unpaid rent
        AtomicInteger occupiedUnits = new AtomicInteger(0);   // Count of occupied units

        // Step 1: Fetch all properties owned by the user.
        db.collection("properties")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(propertiesSnapshot -> {
                    if (propertiesSnapshot.isEmpty()) {
                        showErrorState();
                        return;
                    }

                    // Build a list of property IDs and sum up unit counts.
                    List<String> propertyIdList = new ArrayList<>();
                    for (QueryDocumentSnapshot propertyDoc : propertiesSnapshot) {
                        Long unitCount = propertyDoc.getLong("unitCount");
                        if (unitCount != null) {
                            totalUnits.addAndGet(unitCount.intValue());
                        }
                        propertyIdList.add(propertyDoc.getId());
                    }

                    if (propertyIdList.isEmpty()) {
                        showErrorState();
                        return;
                    }

                    // Step 2: Fetch all units for these properties using whereIn.
                    // (whereIn supports up to 10 values.)
                    db.collectionGroup("units")
                            .whereIn("propertyId", propertyIdList)
                            .get()
                            .addOnSuccessListener(unitsSnapshot -> {
                                for (QueryDocumentSnapshot unitDoc : unitsSnapshot) {
                                    String rentStatus = unitDoc.getString("rentStatus");
                                    Boolean occupied = unitDoc.getBoolean("occupied");

                                    // Count unpaid units.
                                    if ("Unpaid".equalsIgnoreCase(rentStatus)) {
                                        unpaidUnits.incrementAndGet();
                                    }
                                    // Count occupied units.
                                    if (occupied != null && occupied) {
                                        occupiedUnits.incrementAndGet();
                                    }
                                }
                                // Step 3: Update UI with the fetched data.
                                updateDashboardUI(totalUnits.get(), occupiedUnits.get(), unpaidUnits.get());
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Dashboard", "Error fetching units: ", e);
                                showErrorState();
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e("Dashboard", "Error fetching properties: ", e);
                    showErrorState();
                });
    }


    /**
     * Updates the UI with dashboard data
     */
    private void updateDashboardUI(int totalUnits, int occupiedUnits, int unpaidUnits) {
        totalUnitsValue.setText(String.valueOf(totalUnits));
        unpaidUnitsValue.setText(String.valueOf(unpaidUnits));

        // Calculate occupancy and vacancy rates
        int occupancyRate = (totalUnits > 0) ? (occupiedUnits * 100) / totalUnits : 0;
        int vacancyRate = 100 - occupancyRate;

        occupancyRateValue.setText(occupancyRate + "%");
        vacancyRateValue.setText(vacancyRate + "%");
    }

    /**
     * Handles user sign-out
     */
    private void signOutUser() {
        mAuth.signOut();
        Toast.makeText(getActivity(), "Signed out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), com.example.apartmentmanageapp.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Displays an error state if data fails to load
     */
    private void showErrorState() {
        totalUnitsValue.setText("N/A");
        unpaidUnitsValue.setText("N/A");
        occupancyRateValue.setText("N/A");
        vacancyRateValue.setText("N/A");

        Toast.makeText(getActivity(), "Failed to load dashboard data", Toast.LENGTH_SHORT).show();
    }
}
