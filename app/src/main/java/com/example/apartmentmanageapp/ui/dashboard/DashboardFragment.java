package com.example.apartmentmanageapp.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.apartmentmanageapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class DashboardFragment extends Fragment {

    private FirebaseFirestore db;
    private TextView unpaidUnitsValue, occupancyRateValue, vacancyRateValue, totalUnitsValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Link TextViews with XML IDs
        unpaidUnitsValue = view.findViewById(R.id.tv_unpaid_units_value);
        occupancyRateValue = view.findViewById(R.id.tv_occupancy_rate_value);
        vacancyRateValue = view.findViewById(R.id.tv_vacancy_rate_value);
        totalUnitsValue = view.findViewById(R.id.tv_total_units_value);

        // Load data from Firestore
        loadDashboardData();

        return view;
    }

    private void loadDashboardData() {
        db.collection("units").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot unitsSnapshot = task.getResult();
                int totalUnits = unitsSnapshot.size();
                totalUnitsValue.setText(String.valueOf(totalUnits));

                int unpaidUnits = 0;
                for (var document : unitsSnapshot.getDocuments()) {
                    String paymentStatus = document.getString("paymentStatus");
                    if ("Unpaid".equalsIgnoreCase(paymentStatus)) {
                        unpaidUnits++;
                    }
                }
                unpaidUnitsValue.setText(String.valueOf(unpaidUnits));

                int occupiedUnits = totalUnits - unpaidUnits;
                int occupancyRate = totalUnits > 0 ? (occupiedUnits * 100) / totalUnits : 0;
                int vacancyRate = 100 - occupancyRate;

                occupancyRateValue.setText(occupancyRate + "%");
                vacancyRateValue.setText(vacancyRate + "%");
            }
        }).addOnFailureListener(e -> {
            unpaidUnitsValue.setText("Error");
            occupancyRateValue.setText("Error");
            vacancyRateValue.setText("Error");
            totalUnitsValue.setText("Error");
        });
    }
}