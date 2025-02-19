package com.example.apartmentmanageapp.ui.bills;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apartmentmanageapp.R;
import com.example.apartmentmanageapp.model.Bill;
import com.example.apartmentmanageapp.ui.bills.CreateBillActivity;
import com.google.android.material.button.MaterialButton;
import com.example.apartmentmanageapp.adapters.BillsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class BillsFragment extends Fragment {

    private static final String TAG = "BillsFragment";

    private TextView billsSummary;
    private Spinner filterSpinner;
    private RecyclerView billsRecyclerView;
    private MaterialButton exportPdfButton;
    private FloatingActionButton addBillButton;
    private ImageButton refreshButton;

    // Adapter for RecyclerView (assume you already have BillsAdapter defined)
    private BillsAdapter adapter;

    private List<Bill> billsList;
    private DatabaseReference billsRef;
    private ValueEventListener billsListener;

    public BillsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bills_payments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        initViews(view);

        // Initialize Firebase Database reference for bills
        billsRef = FirebaseDatabase.getInstance().getReference("bills");

        // Initialize RecyclerView
        initRecyclerView();

        // Setup Filter Spinner
        setupFilterSpinner();

        // Setup Click Listeners
        setupClickListeners();

        // Load Bills Data from Firebase
        loadBillsData();
    }

    private void initViews(View view) {
        billsSummary = view.findViewById(R.id.bills_summary);
        filterSpinner = view.findViewById(R.id.filter_spinner);
        billsRecyclerView = view.findViewById(R.id.bills_recycler_view);
        exportPdfButton = view.findViewById(R.id.export_pdf);
        addBillButton = view.findViewById(R.id.add_bill_button);
        refreshButton = view.findViewById(R.id.refresh_button);
    }

    private void initRecyclerView() {
        billsList = new ArrayList<>();
        adapter = new BillsAdapter(billsList);
        billsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        billsRecyclerView.setAdapter(adapter);
    }

    private void setupFilterSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"All", "Paid", "Unpaid", "Overdue"}
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);
    }

    private void setupClickListeners() {
        exportPdfButton.setOnClickListener(view -> exportBillsToPdf());
        addBillButton.setOnClickListener(view -> openCreateNewBillActivity());
        refreshButton.setOnClickListener(view -> refreshBillsList());
    }

    private void loadBillsData() {
        billsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                billsList.clear();
                for (DataSnapshot billSnapshot : snapshot.getChildren()) {
                    Bill bill = billSnapshot.getValue(Bill.class);
                    if (bill != null) {
                        billsList.add(bill);
                    }
                }
                adapter.notifyDataSetChanged();
                updateBillsSummary();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load bills: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load bills.", Toast.LENGTH_SHORT).show();
            }
        };
        // Attach the Firebase listener
        billsRef.addValueEventListener(billsListener);
    }

    private void updateBillsSummary() {
        int totalBills = billsList.size();
        double paidTotal = 0;
        double overdueTotal = 0;
        for (Bill bill : billsList) {
            if ("Paid".equalsIgnoreCase(bill.getStatus())) {
                paidTotal += bill.getTotalAmount();
            } else if ("Overdue".equalsIgnoreCase(bill.getStatus())) {
                overdueTotal += bill.getTotalAmount();
            }
        }
        String summaryText = String.format(
                "Total Bills: %d | Paid: ฿%.2f | Overdue: ฿%.2f",
                totalBills, paidTotal, overdueTotal
        );
        billsSummary.setText(summaryText);
    }

    private void exportBillsToPdf() {
        Toast.makeText(getContext(), "Exporting bills to PDF...", Toast.LENGTH_SHORT).show();
        // TODO: Implement PDF export logic
    }

    /**
     * 1) Checks if user is logged in (optional).
     * 2) Opens the CreateBillActivity directly (no dialog).
     */
    private void openCreateNewBillActivity() {
        // Optionally check for logged-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "No user is logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Just open CreateBillActivity (without any property selection dialog)
        Intent intent = new Intent(getContext(), CreateBillActivity.class);
        // If needed, pass user info or any other data
        // intent.putExtra("OWNER_ID", currentUser.getUid());
        startActivity(intent);
    }

    private void refreshBillsList() {
        Toast.makeText(getContext(), "Refreshing bills list...", Toast.LENGTH_SHORT).show();
        if (billsRef != null) {
            billsRef.removeEventListener(billsListener);
        }
        loadBillsData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (billsRef != null && billsListener != null) {
            billsRef.removeEventListener(billsListener);
        }
    }
}
