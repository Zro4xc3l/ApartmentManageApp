package com.example.apartmentmanageapp.ui.bills;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.adapters.BillsAdapter;
import com.example.apartmentmanageapp.model.Bill;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BillsFragment extends Fragment {

    private static final String TAG = "BillsFragment";

    private RecyclerView billsRecyclerView;
    private BillsAdapter adapter;
    private List<Bill> billsList;
    private Spinner filterSpinner;
    private FloatingActionButton addBillButton;
    private MaterialButton exportPdfButton;
    private TextView billsSummary;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ArrayAdapter<String> filterAdapter;
    private final List<String> statusOptions = new ArrayList<>();

    private String selectedFilter = "Unpaid"; // Default filter

    public BillsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bills_payments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "BillsFragment is loaded");

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Current User ID: " + currentUser.getUid());

        // Initialize UI components
        billsRecyclerView = view.findViewById(R.id.bills_recycler_view);
        filterSpinner = view.findViewById(R.id.filter_spinner);
        addBillButton = view.findViewById(R.id.add_bill_button);
        exportPdfButton = view.findViewById(R.id.export_pdf);
        billsSummary = view.findViewById(R.id.bills_summary);

        // Set up RecyclerView
        billsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        billsList = new ArrayList<>();

        // Pass the listener (this::openBillDetail) when creating the adapter
        adapter = new BillsAdapter(getContext(), billsList, this::openBillDetail); // Updated to handle clicks
        billsRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Set up Spinner
        setupFilterSpinner();

        // Load bills with default filter "Unpaid"
        loadBillsData(selectedFilter);

        // Set event listeners
        exportPdfButton.setOnClickListener(v -> exportBillsToPdf());
        addBillButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), CreateBillActivity.class)));

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = statusOptions.get(position);
                loadBillsData(selectedFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupFilterSpinner() {
        statusOptions.clear();
        statusOptions.add("All");
        statusOptions.add("Paid");
        statusOptions.add("Unpaid");
        statusOptions.add("Overdue");

        filterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, statusOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);

        // Set default filter to "Unpaid"
        filterSpinner.setSelection(2);
    }

    private void loadBillsData(String filter) {
        String userId = currentUser.getUid();
        Log.d(TAG, "Fetching bills for user ID: " + userId);

        Query query = db.collection("bills").whereEqualTo("ownerId", userId); // Filter by logged-in user

        if (!"All".equals(filter)) {
            query = query.whereEqualTo("billStatus", filter);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                billsList.clear();
                QuerySnapshot snapshot = task.getResult();

                if (snapshot != null && !snapshot.isEmpty()) {
                    for (QueryDocumentSnapshot document : snapshot) {
                        Log.d(TAG, "Raw Bill Data: " + document.getData());

                        Bill bill = document.toObject(Bill.class);
                        bill.setId(document.getId());

                        Log.d(TAG, "Bill Loaded: " + bill.getPropertyName() + " | Status: " + bill.getBillStatus());

                        // Apply filter
                        if ("All".equals(filter) || bill.getBillStatus().equalsIgnoreCase(filter)) {
                            billsList.add(bill);
                        }
                    }
                } else {
                    Log.d(TAG, "No bills found for filter: " + filter);
                }

                adapter.notifyDataSetChanged();
                updateBillsSummary();
            } else {
                Log.e(TAG, "Error getting bills", task.getException());
                Toast.makeText(getContext(), "Failed to load bills.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBillsSummary() {
        int totalBills = billsList.size();
        double paidTotal = 0;
        double overdueTotal = 0;

        for (Bill bill : billsList) {
            if ("Paid".equalsIgnoreCase(bill.getBillStatus())) {
                paidTotal += bill.getGrandTotal();
            } else if ("Overdue".equalsIgnoreCase(bill.getBillStatus())) {
                overdueTotal += bill.getGrandTotal();
            }
        }

        String summaryText = String.format(
                "Total Bills: %d | Paid: ฿%.2f | Overdue: ฿%.2f",
                totalBills, paidTotal, overdueTotal
        );
        billsSummary.setText(summaryText);
    }

    private void openBillDetail(Bill bill) {
        Intent intent = new Intent(getContext(), BillsDetailActivity.class);
        intent.putExtra("billId", bill.getId());
        intent.putExtra("propertyName", bill.getPropertyName());
        intent.putExtra("billingPeriod", bill.getBillingPeriod());
        intent.putExtra("grandTotal", bill.getGrandTotal());
        intent.putExtra("status", bill.getBillStatus());
        startActivity(intent);
    }

    private void exportBillsToPdf() {
        Toast.makeText(getContext(), "Exporting bills to PDF...", Toast.LENGTH_SHORT).show();
        // TODO: Implement PDF export logic
    }
}
