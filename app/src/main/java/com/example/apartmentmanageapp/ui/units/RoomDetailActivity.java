package com.example.apartmentmanageapp.ui.units;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.ui.tenants.TenantDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RoomDetailActivity extends AppCompatActivity {

    private TextView tvRoomNumber, tvRoomSize, tvFloorLevel, tvRentAmount, tvAvailabilityStatus, tvAmenitiesList;
    private TextView tvTenantName, tvTenantContact, tvLeasePeriod, tvPaymentStatus;
    private TextView tvMaintenanceHistory;
    private Button btnEditRoom, btnAssignTenant, btnEndLease, btnViewTenantDetails;

    // Firestore instance
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        Toast.makeText(this, "RoomDetailActivity Opened!", Toast.LENGTH_SHORT).show();

        initializeViews();

        // 1) Get the unitId from the Intent
        String unitId = getIntent().getStringExtra("unitId");
        if (unitId == null || unitId.isEmpty()) {
            Toast.makeText(this, "No unitId provided!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2) Fetch the room details from Firestore
        loadRoomDetailsFromFirestore(unitId);

        // 3) Setup listeners (Edit, AssignTenant, etc.)
        setupListeners();
    }

    private void initializeViews() {
        tvRoomNumber = findViewById(R.id.tvRoomNumber);
        tvRoomSize = findViewById(R.id.tvRoomSize);
        tvFloorLevel = findViewById(R.id.tvFloorLevel);
        tvRentAmount = findViewById(R.id.tvRentAmount);
        tvAvailabilityStatus = findViewById(R.id.tvAvailabilityStatus);
        tvAmenitiesList = findViewById(R.id.tvAmenitiesList);

        tvTenantName = findViewById(R.id.tvTenantName);
        tvTenantContact = findViewById(R.id.tvTenantContact);
        tvLeasePeriod = findViewById(R.id.tvLeasePeriod);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);

        tvMaintenanceHistory = findViewById(R.id.tvMaintenanceHistory);

        btnEditRoom = findViewById(R.id.btnEditRoom);
        btnAssignTenant = findViewById(R.id.btnAssignTenant);
        btnEndLease = findViewById(R.id.btnEndLease);
        btnViewTenantDetails = findViewById(R.id.btnViewTenantDetails);
    }

    /**
     * Fetches the room/unit details from Firestore using the given unitId.
     */
    private void loadRoomDetailsFromFirestore(String unitId) {
        db.collection("units")
                .document(unitId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateUIWithFirestoreData(documentSnapshot);
                    } else {
                        Toast.makeText(this, "Room does not exist!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading room details", Toast.LENGTH_SHORT).show();
                    Log.e("RoomDetailActivity", "loadRoomDetailsFromFirestore failed", e);
                    finish();
                });
    }

    /**
     * Update the TextViews and occupant logic based on Firestore document data.
     */
    private void updateUIWithFirestoreData(DocumentSnapshot doc) {
        // parse fields
        String roomNumber     = doc.getString("unitNumber");
        String roomSize       = doc.getString("roomSize");
        String floorLevel     = doc.getString("floorLevel");
        Double rentAmountVal  = doc.getDouble("rentAmount");
        String amenities      = doc.getString("amenities");
        Boolean occupied      = doc.getBoolean("occupied");
        String tenantName     = doc.getString("tenantName");
        String tenantContact  = doc.getString("tenantContact");
        String leasePeriod    = doc.getString("leasePeriod");
        String rentStatus     = doc.getString("rentStatus");

        // update UI for room basics
        tvRoomNumber.setText(roomNumber != null ? "Room " + roomNumber : "Room N/A");
        tvRoomSize.setText("Size: " + (roomSize != null ? roomSize : "Unknown"));
        tvFloorLevel.setText("Floor: " + (floorLevel != null ? floorLevel : "Unknown"));
        tvRentAmount.setText("Rent: " + (rentAmountVal != null ? String.valueOf(rentAmountVal) : "Unknown"));
        tvAmenitiesList.setText(amenities != null ? amenities : "None");

        // Occupancy logic
        if (occupied != null && occupied) {
            tvAvailabilityStatus.setText("Status: Occupied");
            tvTenantName.setText("Tenant: " + (tenantName != null ? tenantName : "Unknown Tenant"));
            tvTenantContact.setText("Contact: " + (tenantContact != null ? tenantContact : "N/A"));
            tvLeasePeriod.setText("Lease: " + (leasePeriod != null ? leasePeriod : "N/A"));
            tvPaymentStatus.setText("Payment Status: " + (rentStatus != null ? rentStatus : "N/A"));

            btnAssignTenant.setVisibility(View.GONE);
            btnEndLease.setVisibility(View.VISIBLE);
        } else {
            tvAvailabilityStatus.setText("Status: Vacant");
            tvTenantName.setText("Tenant: Vacant");
            tvTenantContact.setText("Contact: N/A");
            tvLeasePeriod.setText("Lease: N/A");
            tvPaymentStatus.setText("Payment Status: N/A");

            btnAssignTenant.setVisibility(View.VISIBLE);
            btnEndLease.setVisibility(View.GONE);
        }

        // Example maintenance history, if you want to fetch from another collection:
        loadMaintenanceHistory();
    }

    private void loadMaintenanceHistory() {
        String maintenanceHistory = "No maintenance issues reported.";
        // If you store maintenance logs in Firestore, you can query them here
        tvMaintenanceHistory.setText(maintenanceHistory);
    }

    private void setupListeners() {
        btnEditRoom.setOnClickListener(view -> {
            // We already have the unitId from getIntent()
            String unitId = getIntent().getStringExtra("unitId");
            if (unitId == null) {
                Log.e("RoomDetailActivity", "unitId is missing!");
                Toast.makeText(this, "Unit ID is missing!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Launch EditRoomActivity, passing the doc ID so we can retrieve and edit Firestore data there
            Intent intent = new Intent(RoomDetailActivity.this, EditRoomActivity.class);
            intent.putExtra("unitId", unitId);
            startActivityForResult(intent, 100);
        });

        btnAssignTenant.setOnClickListener(view -> {
            // Launch some "AssignTenantActivity" passing doc ID if needed
            String unitId = getIntent().getStringExtra("unitId");
            Intent intent = new Intent(RoomDetailActivity.this, AssignTenantActivity.class);
            intent.putExtra("unitId", unitId);
            startActivity(intent);
        });

        btnEndLease.setOnClickListener(view -> {
            // Logic to end lease, e.g. set 'occupied' = false in Firestore
            // ...
        });

        btnViewTenantDetails.setOnClickListener(view -> {
            // Possibly pass doc ID or tenant ID
            Intent intent = new Intent(RoomDetailActivity.this, TenantDetailActivity.class);
            startActivity(intent);
        });
    }
}
