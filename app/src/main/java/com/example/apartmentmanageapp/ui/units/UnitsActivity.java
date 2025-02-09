package com.example.apartmentmanageapp.ui.units;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import java.util.List;

public class UnitsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UnitAdapter adapter;
    private List<Unit> unitList;
    private FirebaseFirestore db;
    private String propertyId;
    private static final int ADD_UNIT_REQUEST = 102;

    private ImageButton btnBack, btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

        recyclerView = findViewById(R.id.unit_recycler_view);
        FloatingActionButton addUnitButton = findViewById(R.id.add_unit_button);
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
        db = FirebaseFirestore.getInstance();

        // Get propertyId from Intent
        propertyId = getIntent().getStringExtra("propertyId");
        unitList = new ArrayList<>();
        loadUnits();

        adapter = new UnitAdapter(this, unitList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addUnitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UnitsActivity.this, AddUnitActivity.class);
                intent.putExtra("propertyId", propertyId);
                startActivityForResult(intent, ADD_UNIT_REQUEST);
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUnits();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_UNIT_REQUEST && resultCode == RESULT_OK) {
            loadUnits(); // Reload the unit list
        }
    }

    private void loadUnits() {
        db.collection("units").whereEqualTo("propertyId", propertyId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        unitList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Unit unit = document.toObject(Unit.class);
                            unitList.add(unit);

                            Log.d("UnitsActivity", "Loaded Unit: " + unit.getUnitNumber());
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load units", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}