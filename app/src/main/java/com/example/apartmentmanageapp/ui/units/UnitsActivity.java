package com.example.apartmentmanageapp.ui.units;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.UnitAdapter;
import com.example.apartmentmanageapp.model.Unit;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnitsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UnitAdapter adapter;
    private List<Unit> unitList;
    private FirebaseFirestore db;
    private String propertyId;

    private ImageButton btnBack, btnRefresh;
    private FloatingActionButton addUnitButton;

    // Activity result launcher for adding a new unit
    private final ActivityResultLauncher<Intent> addUnitLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            loadUnits(); // Reload units when a new one is added
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

        recyclerView = findViewById(R.id.unit_recycler_view);
        addUnitButton = findViewById(R.id.add_unit_button);
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
        db = FirebaseFirestore.getInstance();

        // Get propertyId from Intent
        propertyId = getIntent().getStringExtra("propertyId");

        if (propertyId == null || propertyId.isEmpty()) {
            Toast.makeText(this, "Error: Property ID is missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        unitList = new ArrayList<>();
        adapter = new UnitAdapter(this, unitList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load existing units from Firestore
        loadUnits();

        // Add Unit Button Click Event
        addUnitButton.setOnClickListener(v -> {
            Intent intent = new Intent(UnitsActivity.this, AddUnitActivity.class);
            intent.putExtra("propertyId", propertyId);
            addUnitLauncher.launch(intent);
        });

        // Back Button
        btnBack.setOnClickListener(v -> finish());

        // Refresh Button
        btnRefresh.setOnClickListener(v -> loadUnits());
    }

    private void loadUnits() {
        if (propertyId == null) return;

        db.collection("properties").document(propertyId)
                .collection("units") // Fetch units inside the selected property
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Unit> newUnitList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Unit unit = document.toObject(Unit.class);
                            newUnitList.add(unit);
                        }

                        if (newUnitList.isEmpty()) {
                            Toast.makeText(this, "No units found for this property.", Toast.LENGTH_SHORT).show();
                        }

                        unitList.clear();
                        unitList.addAll(newUnitList);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load units", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
