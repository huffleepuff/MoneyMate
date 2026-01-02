package com.example.moneymate.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymate.R;
import com.example.moneymate.model.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GrafikActivity extends AppCompatActivity {

    // ================= VIEW =================
    private BarChart barChart;

    // ================= FIREBASE =================
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String uid;

    // ================= DATA =================
    private double totalIncome = 0;
    private double totalExpense = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        // Init view
        barChart = findViewById(R.id.barChart);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Cek session
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Sesi berakhir", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        uid = mAuth.getCurrentUser().getUid();

        // Load data grafik
        loadChartData();
    }

    /**
     * ================= LOAD DATA DARI FIRESTORE =================
     */
    private void loadChartData() {
        firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    // Reset nilai lama
                    totalIncome = 0;
                    totalExpense = 0;

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Transaction transaction = document.toObject(Transaction.class);

                        if (transaction != null) {
                            double amount = transaction.getAmount();
                            if (amount > 0) {
                                totalIncome += amount;
                            } else {
                                totalExpense += Math.abs(amount);
                            }
                        }
                    }

                    setupBarChart();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Gagal memuat data grafik: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    /**
     * ================= SETUP CHART =================
     */
    private void setupBarChart() {

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) totalIncome));
        entries.add(new BarEntry(1f, (float) totalExpense));

        BarDataSet dataSet = new BarDataSet(entries, "Grafik Keuangan");
        dataSet.setDrawValues(true);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        setupXAxis();
        setupYAxis();

        barChart.invalidate(); // refresh chart
    }

    /**
     * ================= X AXIS =================
     */
    private void setupXAxis() {
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(
                new IndexAxisValueFormatter(
                        new String[]{"Pemasukan", "Pengeluaran"}
                )
        );
    }

    /**
     * ================= Y AXIS =================
     */
    private void setupYAxis() {
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setGranularity(1f);
    }
}
