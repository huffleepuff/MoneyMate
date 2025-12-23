package com.example.moneymate.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.moneymate.R;
import com.example.moneymate.model.Transaction;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend; // <-- IMPORT BARU
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GrafikActivity extends AppCompatActivity {

    private PieChart pieChart;
    private Spinner spinnerBulan, spinnerTahun;
    private DatabaseReference databaseReference;
    private List<Transaction> fullTransactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        Toolbar toolbar = findViewById(R.id.toolbar_grafik);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        pieChart = findViewById(R.id.pie_chart);
        spinnerBulan = findViewById(R.id.spinner_bulan_grafik);
        spinnerTahun = findViewById(R.id.spinner_tahun_grafik);

        databaseReference = FirebaseDatabase.getInstance().getReference("transactions");
        fullTransactionList = new ArrayList<>();

        setupSpinners();
        loadDataFromFirebase();
    }

    private void setupSpinners() {
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplayChart();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };
        spinnerBulan.setOnItemSelectedListener(listener);
        spinnerTahun.setOnItemSelectedListener(listener);
    }

    private void loadDataFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullTransactionList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Transaction transaction = dataSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        fullTransactionList.add(transaction);
                    }
                }
                // Data dimuat, panggil filter
                filterAndDisplayChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GrafikActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndDisplayChart() {
        int selectedMonth = spinnerBulan.getSelectedItemPosition();
        String selectedTahunStr = spinnerTahun.getSelectedItem().toString();

        double totalPemasukan = 0;
        double totalPengeluaran = 0;

        Calendar cal = Calendar.getInstance();

        for (Transaction transaction : fullTransactionList) {
            cal.setTimeInMillis(transaction.getTimestamp());
            int transactionMonth = cal.get(Calendar.MONTH) + 1;
            int transactionTahun = cal.get(Calendar.YEAR);

            boolean monthMatch = (selectedMonth == 0) || (transactionMonth == selectedMonth);
            boolean tahunMatch = (selectedTahunStr.equals("Semua Tahun")) || (String.valueOf(transactionTahun).equals(selectedTahunStr));

            if (monthMatch && tahunMatch) {
                if (transaction.getAmount() > 0) {
                    totalPemasukan += transaction.getAmount();
                } else {
                    totalPengeluaran += Math.abs(transaction.getAmount());
                }
            }
        }

        // Update Pie Chart
        updatePieChart(totalPemasukan, totalPengeluaran);
    }

    // --- FUNGSI INI YANG DIPERBARUI ---
    private void updatePieChart(double totalPemasukan, double totalPengeluaran) {
        if (totalPemasukan == 0 && totalPengeluaran == 0) {
            pieChart.clear();
            pieChart.setCenterText("Tidak Ada Data");
            pieChart.invalidate();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) totalPemasukan, "Pemasukan"));
        entries.add(new PieEntry((float) totalPengeluaran, "Pengeluaran"));

        PieDataSet dataSet = new PieDataSet(entries, "Ringkasan Keuangan");
        dataSet.setColors(Color.rgb(76, 175, 80), Color.rgb(244, 67, 54)); // Hijau & Merah
        dataSet.setValueTextColor(Color.BLACK); // Warna angka di dalam chart
        dataSet.setValueTextSize(16f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Ringkasan");

        // --- INI DIA PERBAIKANNYA ---
        // Mengubah warna teks keterangan (legend)
        Legend legend = pieChart.getLegend();
        legend.setTextColor(Color.parseColor("#2196F3")); // Warna biru logo
        legend.setTextSize(12f);
        // --- AKHIR PERBAIKAN ---

        pieChart.animateY(1000);
        pieChart.invalidate(); // Refresh chart
    }
}