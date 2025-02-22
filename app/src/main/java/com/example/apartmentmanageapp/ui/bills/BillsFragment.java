package com.example.apartmentmanageapp.ui.bills;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BillsFragment extends Fragment {

    private static final String TAG = "BillsFragment";
    private static final int CREATE_FILE_REQUEST_CODE = 1001;

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

    // Global PDF document reference to write once the user picks a location.
    private PdfDocument mPdfDocument;

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

        billsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        billsList = new ArrayList<>();
        adapter = new BillsAdapter(getContext(), billsList, this::openBillDetail);
        billsRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        setupFilterSpinner();
        loadBillsData(selectedFilter);

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

        Query query = db.collection("bills").whereEqualTo("ownerId", userId);
        if (!"All".equals(filter)) {
            query = query.whereEqualTo("billStatus", filter);
        }
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                billsList.clear();
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null && !snapshot.isEmpty()) {
                    for (QueryDocumentSnapshot document : snapshot) {
                        Bill bill = document.toObject(Bill.class);
                        bill.setId(document.getId());
                        if ("All".equals(filter) || bill.getBillStatus().equalsIgnoreCase(filter)) {
                            billsList.add(bill);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                updateBillsSummary();
            } else {
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
        String summaryText = String.format("Total Bills: %d | Paid: ฿%.2f | Overdue: ฿%.2f",
                totalBills, paidTotal, overdueTotal);
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

    /**
     * Generate the PDF document with extra details and then launch an intent to let the user save it.
     * This implementation uses a landscape page for extra horizontal space.
     */
    private void exportBillsToPdf() {
        Toast.makeText(getContext(), "Exporting bills to PDF...", Toast.LENGTH_SHORT).show();

        // Create a new PdfDocument
        PdfDocument pdfDocument = new PdfDocument();
        // Use a landscape page: width = 842, height = 595
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(842, 595, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw report title
        paint.setTextSize(14);
        int marginLeft = 10;
        int y = 30;
        String title = "Bills Report";
        canvas.drawText(title, marginLeft, y, paint);
        y += 20;

        // Set smaller font for table headers and rows
        paint.setTextSize(10);

        // Define column positions (x coordinates)
        int col1 = 10;   // Property
        int col2 = 110;  // Billing Period
        int col3 = 210;  // Room(s)
        int col4 = 310;  // Total Rent
        int col5 = 390;  // Total Elec
        int col6 = 470;  // Total Water
        int col7 = 550;  // Total Additional
        int col8 = 630;  // Grand Total
        int col9 = 710;  // Status

        // Draw table header row
        canvas.drawText("Property", col1, y, paint);
        canvas.drawText("Period", col2, y, paint);
        canvas.drawText("Room(s)", col3, y, paint);
        canvas.drawText("Rent", col4, y, paint);
        canvas.drawText("Elec", col5, y, paint);
        canvas.drawText("Water", col6, y, paint);
        canvas.drawText("Addtl", col7, y, paint);
        canvas.drawText("Grand", col8, y, paint);
        canvas.drawText("Status", col9, y, paint);
        y += 12;
        // Draw a line below header
        canvas.drawLine(10, y, pageInfo.getPageWidth() - 10, y, paint);
        y += 10;

        // Loop through bills and add a row for each
        for (Bill bill : billsList) {
            String property = bill.getPropertyName();
            String period = bill.getBillingPeriod();
            String rooms = getRoomInfo(bill);
            String rent = String.format("฿%.2f", bill.getTotalRent());
            String elec = String.format("฿%.2f", bill.getTotalElectric());
            String water = String.format("฿%.2f", bill.getTotalWater());
            String additional = String.format("฿%.2f", bill.getTotalAdditional());
            String grand = String.format("฿%.2f", bill.getGrandTotal());
            String status = bill.getBillStatus();

            canvas.drawText(property, col1, y, paint);
            canvas.drawText(period, col2, y, paint);
            canvas.drawText(rooms, col3, y, paint);
            canvas.drawText(rent, col4, y, paint);
            canvas.drawText(elec, col5, y, paint);
            canvas.drawText(water, col6, y, paint);
            canvas.drawText(additional, col7, y, paint);
            canvas.drawText(grand, col8, y, paint);
            canvas.drawText(status, col9, y, paint);
            y += 12;

            // If the y-coordinate exceeds page height, finish current page and start a new one.
            if (y > pageInfo.getPageHeight() - 20) {
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(842, 595, pdfDocument.getPages().size() + 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 30;
            }
        }
        // Finish the last page.
        pdfDocument.finishPage(page);

        // Save the document via the Storage Access Framework.
        mPdfDocument = pdfDocument;
        createFile();
    }

    /**
     * Helper method to extract room information from the bill.
     * It assumes Bill.getUnitBills() returns a list of UnitBill objects (or similar)
     * where each UnitBill has a getUnitId() method.
     */
    private String getRoomInfo(Bill bill) {
        if (bill.getUnitBills() != null && !bill.getUnitBills().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bill.getUnitBills().size(); i++) {
                Object unit = bill.getUnitBills().get(i);
                // Adjust this depending on your UnitBill implementation.
                if (unit instanceof com.example.apartmentmanageapp.model.UnitBill) {
                    sb.append(((com.example.apartmentmanageapp.model.UnitBill) unit).getUnit());
                } else {
                    sb.append(unit.toString());
                }
                if (i < bill.getUnitBills().size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * Launches an intent to create a new document using the Storage Access Framework.
     */
    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "bills_report.pdf");
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null && mPdfDocument != null) {
                Uri uri = data.getData();
                try (OutputStream outputStream = requireActivity().getContentResolver().openOutputStream(uri)) {
                    mPdfDocument.writeTo(outputStream);
                    mPdfDocument.close();
                    mPdfDocument = null;
                    Toast.makeText(getContext(), "PDF saved successfully", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
