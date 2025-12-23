package com.example.moneymate.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.R;
import com.example.moneymate.adapter.TransactionAdapter;
import com.example.moneymate.model.Transaction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// PASTIKAN NAMA KELAS INI SAMA DENGAN NAMA FILE
public class RekapActivity extends AppCompatActivity {

    private TextView tvTotalPemasukan, tvTotalPengeluaran;
    private RecyclerView recyclerViewRekap;
    private Spinner spinnerBulan, spinnerTahun;

    private TransactionAdapter adapter;
    private List<Transaction> fullTransactionList; // Menyimpan SEMUA data
    private List<Transaction> filteredTransactionList; // Untuk ditampilkan
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekap);

        Toolbar toolbar = findViewById(R.id.toolbar_rekap);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvTotalPemasukan = findViewById(R.id.tv_total_pemasukan);
        tvTotalPengeluaran = findViewById(R.id.tv_total_pengeluaran);
        recyclerViewRekap = findViewById(R.id.recycler_view_rekap);
        spinnerBulan = findViewById(R.id.spinner_bulan);
        spinnerTahun = findViewById(R.id.spinner_tahun);

        databaseReference = FirebaseDatabase.getInstance().getReference("transactions");
        fullTransactionList = new ArrayList<>();
        filteredTransactionList = new ArrayList<>();

        setupRecyclerView();
        setupSpinners();
        readDataFromFirebase();
    }

    private void setupRecyclerView() {
        recyclerViewRekap.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, filteredTransactionList, new TransactionAdapter.OnItemClickListener() {
            @Override public void onEditClick(Transaction transaction) { /* biarkan kosong */ }
            @Override public void onDeleteClick(Transaction transaction) { /* biarkan kosong */ }
        });
        recyclerViewRekap.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Listener akan memanggil filterAndDisplayData setiap kali pilihan berubah
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplayData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };
        spinnerBulan.setOnItemSelectedListener(listener);
        spinnerTahun.setOnItemSelectedListener(listener);
    }

    private void readDataFromFirebase() {
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
                // Urutkan data utama sekali saja
                fullTransactionList.sort((t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                // Setelah data dimuat, panggil filter
                filterAndDisplayData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RekapActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndDisplayData() {
        // Ambil pilihan dari spinner
        int selectedMonth = spinnerBulan.getSelectedItemPosition(); // 0 = Semua, 1 = Jan, dst.
        String selectedTahunStr = spinnerTahun.getSelectedItem().toString();

        filteredTransactionList.clear();
        double totalPemasukan = 0;
        double totalPengeluaran = 0;

        Calendar cal = Calendar.getInstance();

        for (Transaction transaction : fullTransactionList) {
            cal.setTimeInMillis(transaction.getTimestamp());
            int transactionMonth = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH itu 0-11
            int transactionTahun = cal.get(Calendar.YEAR);

            boolean monthMatch = (selectedMonth == 0) || (transactionMonth == selectedMonth);
            boolean tahunMatch = (selectedTahunStr.equals("Semua Tahun")) || (String.valueOf(transactionTahun).equals(selectedTahunStr));

            // Jika cocok dengan filter, tambahkan ke list dan hitung
            if (monthMatch && tahunMatch) {
                filteredTransactionList.add(transaction);

                if (transaction.getAmount() > 0) {
                    totalPemasukan += transaction.getAmount();
                } else {
                    totalPengeluaran += transaction.getAmount();
                }
            }
        }

        // Update UI
        adapter.notifyDataSetChanged();

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        tvTotalPemasukan.setText(formatRupiah.format(totalPemasukan));
        tvTotalPengeluaran.setText(formatRupiah.format(totalPengeluaran));
    }
}