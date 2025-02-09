package com.example.apartmentmanageapp.ui.properties;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.PropertyAdapter;
import com.example.apartmentmanageapp.model.Property;
import com.example.apartmentmanageapp.ui.units.UnitsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PropertiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private PropertyAdapter adapter;
    private List<Property> propertyList;
    private FirebaseFirestore db;
    private static final int ADD_PROPERTY_REQUEST = 101;

    public PropertiesFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_properties, container, false);

        recyclerView = view.findViewById(R.id.property_recycler_view);
        FloatingActionButton addPropertyButton = view.findViewById(R.id.add_property_button);

        db = FirebaseFirestore.getInstance();
        propertyList = new ArrayList<>();

        loadProperties();

        adapter = new PropertyAdapter(requireActivity(), propertyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        addPropertyButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddPropertyActivity.class);
            startActivityForResult(intent, ADD_PROPERTY_REQUEST);
        });

        return view;
    }

    // ✅ FIX: Open `UnitsActivity` instead of `UnitsFragment`
    private void openUnitsPage(Property property) {
        Intent intent = new Intent(getActivity(), UnitsActivity.class);
        intent.putExtra("propertyId", property.getId()); // Pass Property ID
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_PROPERTY_REQUEST && resultCode == getActivity().RESULT_OK) {
            loadProperties();
        }
    }

    private void loadProperties() {
        CollectionReference propertiesRef = db.collection("properties");
        propertiesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                propertyList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Property property = document.toObject(Property.class);
                    property.setId(document.getId());
                    propertyList.add(property);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
