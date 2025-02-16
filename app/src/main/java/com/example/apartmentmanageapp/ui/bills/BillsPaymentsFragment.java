package com.example.apartmentmanageapp.ui.bills;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.apartmentmanageapp.adapters.BillsAdapter;
import com.example.apartmentmanageapp.model.Bill;
import com.example.apartmentmanageapp.ui.bills.CreateNewBillActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BillsPaymentsFragment extends Fragment {

    private TextView billsSummary;
    private Spinner filterSpinner;
    private RecyclerView billsRecyclerView;
    private MaterialButton exportPdfButton;
    private FloatingActionButton addBillButton;
    private ImageButton refreshButton;

    private List<Bill> billsList;
    private BillsAdapter adapter;
    private DatabaseReference billsRef;

    public BillsPaymentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout (ensure you have fragment_bills_payments.xml)
        return inflater.inflate(R.layout.fragment_bills_payments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views using the inflated view
        billsSummary = view.findViewById(R.id.bills_summary);
        filterSpinner = view.findViewById(R.id.filter_spinner);
        billsRecyclerView = view.findViewById(R.id.bills_recycler_view);
        exportPdfButton = view.findViewById(R.id.export_pdf);
        addBillButton = view.findViewById(R.id.add_bill_button);
        refreshButton = view.findViewById(R.id.refresh_button);

        // Initialize Firebase Database reference for "bills"
        billsRef = FirebaseDatabase.getInstance().getReference("bills");

        // Initialize list and adapter
        billsList = new ArrayList<>();
        adapter = new BillsAdapter(billsList);
        billsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        billsRecyclerView.setAdapter(adapter);

        // Set up the filter spinner with sample options
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"All", "Paid", "Unpaid", "Overdue"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        // Set click listener for Export PDF button
        exportPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportBillsToPdf();
            }
        });

        // Set click listener for the Floating Action Button (Create New Bill)
        addBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateNewBillActivity();
            }
        });

        // Set click listener for the Refresh button
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshBillsList();
            }
        });

        // Listen for changes in the "bills" node
        billsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                billsList.clear();
                for (DataSnapshot billSnapshot : snapshot.getChildren()) {
                    Bill bill = billSnapshot.getValue(Bill.class);
                    billsList.add(bill);
                }
                adapter.notifyDataSetChanged();
                updateBillsSummary();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load bills.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the summary TextView based on the retrieved bill data.
     */
    private void updateBillsSummary() {
        int totalBills = billsList.size();
        double paidTotal = 0;
        double overdueTotal = 0;
        for (Bill bill : billsList) {
            if ("Paid".equalsIgnoreCase(bill.getStatus())) {
                paidTotal += bill.getAmount();
            } else if ("Overdue".equalsIgnoreCase(bill.getStatus())) {
                overdueTotal += bill.getAmount();
            }
        }
        billsSummary.setText("Total Bills: " + totalBills +
                " | Paid: $" + paidTotal +
                " | Overdue: $" + overdueTotal);
    }

    /**
     * Initiates the export of bills data to a PDF.
     * (Implement your PDF generation logic here.)
     */
    private void exportBillsToPdf() {
        Toast.makeText(getContext(), "Exporting bills to PDF...", Toast.LENGTH_SHORT).show();
        // TODO: Add your PDF generation code.
    }

    /**
     * Opens an activity for creating a new bill.
     * You can also open a fragment if desired.
     */
    private void openCreateNewBillActivity() {
        // If you have an Activity for creating a new bill:
        Intent intent = new Intent(getContext(), CreateNewBillActivity.class);
        startActivity(intent);
    }

    /**
     * Refreshes the bills list.
     * (Firebase listeners automatically update the list,
     * but you can add additional refresh logic if needed.)
     */
    private void refreshBillsList() {
        Toast.makeText(getContext(), "Refreshing bills list...", Toast.LENGTH_SHORT).show();
        // Optionally force a refresh or clear cache here.
    }
}
