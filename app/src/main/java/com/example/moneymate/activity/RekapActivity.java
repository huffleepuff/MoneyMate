package com.example.moneymate.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.R;
import com.example.moneymate.adapter.TransactionAdapter;
import com.example.moneymate.model.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RekapActivity extends AppCompatActivity {

    private TextView tvTotalPemasukan, tvTotalPengeluaran;
    private RecyclerView recyclerViewRekap;
    private Spinner spinnerBulan, spinnerTahun;

    private TransactionAdapter adapter;
    private List<Transaction> fullTransactionList;
    private List<Transaction> filteredTransactionList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String uid;

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

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Sesi berakhir, silakan login ulang", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        uid = mAuth.getCurrentUser().getUid();

        fullTransactionList = new ArrayList<>();
        filteredTransactionList = new ArrayList<>();

        setupRecyclerView();
        setupSpinners();
        readDataFromFirestore();
    }

    private void setupRecyclerView() {
        recyclerViewRekap.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(
                this,
                filteredTransactionList,
                new TransactionAdapter.OnItemClickListener() {
                    @Override public void onEditClick(Transaction transaction) {}
                    @Override public void onDeleteClick(Transaction transaction) {}
                }
        );
        recyclerViewRekap.setAdapter(adapter);
    }

    private void setupSpinners() {
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplayData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerBulan.setOnItemSelectedListener(listener);
        spinnerTahun.setOnItemSelectedListener(listener);
    }

    /**
     * ================= FIRESTORE =================
     */
    private void readDataFromFirestore() {
        firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    fullTransactionList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Transaction transaction = doc.toObject(Transaction.class);
                        if (transaction != null) {
                            fullTransactionList.add(transaction);
                        }
                    }

                    fullTransactionList.sort(
                            (t1, t2) -> Long.compare(
                                    t2.getTimestamp(),
                                    t1.getTimestamp()
                            )
                    );

                    filterAndDisplayData();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Gagal memuat data rekap: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void filterAndDisplayData() {
        int selectedMonth = spinnerBulan.getSelectedItemPosition();
        String selectedTahunStr = spinnerTahun.getSelectedItem().toString();

        filteredTransactionList.clear();

        double totalPemasukan = 0;
        double totalPengeluaran = 0;

        Calendar cal = Calendar.getInstance();

        for (Transaction transaction : fullTransactionList) {
            cal.setTimeInMillis(transaction.getTimestamp());

            int transactionMonth = cal.get(Calendar.MONTH) + 1;
            int transactionTahun = cal.get(Calendar.YEAR);

            boolean monthMatch =
                    (selectedMonth == 0) || (transactionMonth == selectedMonth);

            boolean tahunMatch =
                    selectedTahunStr.equals("Semua Tahun") ||
                            String.valueOf(transactionTahun).equals(selectedTahunStr);

            if (monthMatch && tahunMatch) {
                filteredTransactionList.add(transaction);

                if (transaction.getAmount() > 0) {
                    totalPemasukan += transaction.getAmount();
                } else {
                    totalPengeluaran += Math.abs(transaction.getAmount());
                }
            }
        }

        adapter.notifyDataSetChanged();

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        tvTotalPemasukan.setText(formatRupiah.format(totalPemasukan));
        tvTotalPengeluaran.setText(formatRupiah.format(totalPengeluaran));
    }
}
