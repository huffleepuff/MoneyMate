package com.example.moneymate.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.R;
import com.example.moneymate.adapter.TransactionAdapter;
import com.example.moneymate.model.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAdd;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String uid;

    private final Calendar selectedDate = Calendar.getInstance();
    private long selectedTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        uid = mAuth.getCurrentUser().getUid();

        fabAdd = findViewById(R.id.fab_add);
        recyclerView = findViewById(R.id.recycler_view);

        setupRecyclerView();
        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
        readData();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactionList, new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Transaction transaction) {
                showAddEditDialog(transaction);
            }

            @Override
            public void onDeleteClick(Transaction transaction) {
                deleteTransaction(transaction.getId());
            }
        });
        recyclerView.setAdapter(adapter);
    }

    // 🔥 READ DATA FIRESTORE
    private void readData() {
        firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    transactionList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Transaction transaction = doc.toObject(Transaction.class);
                        if (transaction != null) {
                            transaction.setId(doc.getId());
                            transactionList.add(transaction);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showAddEditDialog(final Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_edit, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        Spinner spinnerTipe = dialogView.findViewById(R.id.spinner_tipe_transaksi);
        EditText etTanggal = dialogView.findViewById(R.id.et_tanggal);
        Spinner spinnerMetode = dialogView.findViewById(R.id.spinner_metode);
        EditText etTag = dialogView.findViewById(R.id.et_tag);
        EditText etCatatan = dialogView.findViewById(R.id.et_catatan);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        spinnerTipe.setAdapter(ArrayAdapter.createFromResource(
                this, R.array.tipe_transaksi_array,
                android.R.layout.simple_spinner_item));

        spinnerMetode.setAdapter(ArrayAdapter.createFromResource(
                this, R.array.metode_pembayaran_array,
                android.R.layout.simple_spinner_item));

        etTanggal.setOnClickListener(v -> new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    selectedDate.set(y, m, d);
                    updateTanggalLabel(etTanggal);
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show());

        if (transaction != null) {
            etTitle.setText(transaction.getTitle());
            etAmount.setText(String.valueOf(Math.abs(transaction.getAmount())));
            etTag.setText(transaction.getTag());
            etCatatan.setText(transaction.getCatatan());
            selectedDate.setTimeInMillis(transaction.getTimestamp());
        } else {
            selectedDate.setTimeInMillis(System.currentTimeMillis());
        }

        updateTanggalLabel(etTanggal);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String amountStr = etAmount.getText().toString();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(amountStr)) {
                Toast.makeText(this, "Judul dan jumlah wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String type = spinnerTipe.getSelectedItem().toString();
            double finalAmount = type.equals("Pengeluaran") ? -amount : amount;

            Transaction newTransaction = new Transaction(
                    transaction == null ? null : transaction.getId(),
                    title,
                    finalAmount,
                    selectedDate.getTimeInMillis(),
                    type,
                    etCatatan.getText().toString(),
                    spinnerMetode.getSelectedItem().toString(),
                    etTag.getText().toString()
            );

            if (transaction == null) {
                addTransaction(newTransaction);
            } else {
                updateTransaction(newTransaction);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void addTransaction(Transaction t) {
        firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .add(t);
    }

    private void updateTransaction(Transaction t) {
        firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .document(t.getId())
                .set(t);
    }

    private void deleteTransaction(String id) {
        firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .document(id)
                .delete();
    }

    private void updateTanggalLabel(EditText etTanggal) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        etTanggal.setText(sdf.format(selectedDate.getTime()));
    }
}
