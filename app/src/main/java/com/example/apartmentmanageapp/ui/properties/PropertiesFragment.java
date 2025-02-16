package com.example.apartmentmanageapp.ui.properties;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.PropertyAdapter;
import com.example.apartmentmanageapp.model.Property;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PropertiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private PropertyAdapter adapter;
    private List<Property> propertyList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private FirebaseAuth mAuth;

    public PropertiesFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_properties, container, false);
        setHasOptionsMenu(true); // Enable refresh button in menu

        recyclerView = view.findViewById(R.id.property_recycler_view);
        FloatingActionButton addPropertyButton = view.findViewById(R.id.add_property_button);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        propertyList = new ArrayList<>();
        adapter = new PropertyAdapter(requireActivity(), propertyList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Load properties on startup
        fetchProperties();

        // Add Property Button
        addPropertyButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddPropertyActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.property_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            fetchProperties(); // Refresh property list
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Fetch properties only for the logged-in owner
    private void fetchProperties() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        progressBar.setVisibility(View.VISIBLE);  // Show progress bar while loading

        db.collection("properties")
                .whereEqualTo("ownerId", userId) // Fetch only properties owned by logged-in user
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        progressBar.setVisibility(View.GONE);  // Hide progress bar when data is loaded

                        if (error != null) {
                            Log.e("FirestoreError", "Error fetching properties", error);
                            Toast.makeText(getContext(), "Failed to load properties", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshots == null || snapshots.isEmpty()) {
                            Log.d("Firestore", "No properties found.");
                            emptyStateText.setVisibility(View.VISIBLE);  // Show empty state
                            recyclerView.setVisibility(View.GONE);  // Hide RecyclerView
                            return;
                        }

                        propertyList.clear();  // Clear existing data
                        for (QueryDocumentSnapshot document : snapshots) {
                            Property property = document.toObject(Property.class);
                            property.setId(document.getId()); // Ensure Firestore ID is set
                            Log.d("Firestore", "Loaded Property: " + property.getName() + " (ID: " + property.getId() + ")");
                            propertyList.add(property);  // Add the new property to the list
                        }

                        // Update RecyclerView with new data
                        adapter.notifyDataSetChanged();

                        // Show RecyclerView and hide empty state if data exists
                        if (!propertyList.isEmpty()) {
                            emptyStateText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}
