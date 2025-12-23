package com.example.moneymate.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
// import android.widget.RadioButton; // <-- SUDAH TIDAK DIPAKAI
// import android.widget.RadioGroup;  // <-- SUDAH TIDAK DIPAKAI
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
// import com.google.android.material.textfield.TextInputLayout; // <-- Tidak terpakai, bisa dihapus
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private DatabaseReference databaseReference;

    // Variabel untuk menyimpan tanggal yang dipilih
    private final Calendar selectedDate = Calendar.getInstance();
    private long selectedTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference("transactions");
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

    private void readData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Transaction transaction = dataSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        transactionList.add(transaction);
                    }
                }
                // Urutkan berdasarkan timestamp (tanggal pilihan pengguna)
                transactionList.sort((t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==================================================================
    // == FUNGSI DI BAWAH INI ADALAH YANG SUDAH DIPERBAIKI TOTAL ==
    // ==================================================================

    private void showAddEditDialog(final Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_edit, null);
        builder.setView(dialogView);

        // Inisialisasi SEMUA UI Dialog
        final EditText etTitle = dialogView.findViewById(R.id.et_title);
        final EditText etAmount = dialogView.findViewById(R.id.et_amount);
        // INI YANG DIPERBAIKI: Menggunakan Spinner, bukan RadioGroup
        final Spinner spinnerTipeTransaksi = dialogView.findViewById(R.id.spinner_tipe_transaksi);
        final EditText etTanggal = dialogView.findViewById(R.id.et_tanggal);
        final Spinner spinnerMetode = dialogView.findViewById(R.id.spinner_metode);
        final EditText etTag = dialogView.findViewById(R.id.et_tag);
        final EditText etCatatan = dialogView.findViewById(R.id.et_catatan);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        // Setup Spinner Tipe Transaksi (Pemasukan/Pengeluaran)
        ArrayAdapter<CharSequence> tipeAdapter = ArrayAdapter.createFromResource(this,
                R.array.tipe_transaksi_array, android.R.layout.simple_spinner_item);
        tipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipeTransaksi.setAdapter(tipeAdapter);

        // Setup Spinner Metode Pembayaran
        ArrayAdapter<CharSequence> metodeAdapter = ArrayAdapter.createFromResource(this,
                R.array.metode_pembayaran_array, android.R.layout.simple_spinner_item);
        metodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMetode.setAdapter(metodeAdapter);

        // Setup Date Picker
        final DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateTanggalLabel(etTanggal);
        };

        etTanggal.setOnClickListener(v -> new DatePickerDialog(MainActivity.this, dateSetListener,
                selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)).show());


        final AlertDialog dialog = builder.create();

        if (transaction != null) {
            // MODE EDIT - Isi data yang ada
            builder.setTitle("Edit Transaksi");
            etTitle.setText(transaction.getTitle());
            etAmount.setText(String.valueOf(Math.abs(transaction.getAmount())));
            etTag.setText(transaction.getTag());
            etCatatan.setText(transaction.getCatatan());

            // INI YANG DIPERBAIKI: Mengatur Spinner Tipe, bukan RadioButton
            int tipeSpinnerPosition = tipeAdapter.getPosition(transaction.getType());
            spinnerTipeTransaksi.setSelection(tipeSpinnerPosition);

            // Atur Tanggal
            selectedTimestamp = transaction.getTimestamp();
            selectedDate.setTimeInMillis(selectedTimestamp);
            updateTanggalLabel(etTanggal);

            // Atur Spinner Metode Pembayaran
            int metodeSpinnerPosition = metodeAdapter.getPosition(transaction.getMetodeBayar());
            spinnerMetode.setSelection(metodeSpinnerPosition);

        } else {
            // MODE TAMBAH BARU - Atur default
            builder.setTitle("Tambah Transaksi");
            // Set tanggal hari ini sebagai default
            selectedTimestamp = System.currentTimeMillis();
            selectedDate.setTimeInMillis(selectedTimestamp);
            updateTanggalLabel(etTanggal);
        }

        btnSave.setOnClickListener(v -> {
            // Ambil semua data dari input
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String tag = etTag.getText().toString().trim();
            String catatan = etCatatan.getText().toString().trim();
            String metodeBayar = spinnerMetode.getSelectedItem().toString();

            // INI YANG DIPERBAIKI: Validasi menggunakan Spinner Tipe
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(amountStr)) {
                Toast.makeText(this, "Judul dan Jumlah harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            // INI YANG DIPERBAIKI: Mengambil data 'type' dari Spinner
            String type = spinnerTipeTransaksi.getSelectedItem().toString(); // "Pemasukan" atau "Pengeluaran"

            double amount = Double.parseDouble(amountStr);
            double finalAmount = (type.equals("Pengeluaran")) ? -Math.abs(amount) : Math.abs(amount);

            // Dapatkan timestamp yang dipilih (sudah di-set oleh DatePicker)
            long finalTimestamp = selectedDate.getTimeInMillis();

            if (transaction != null) {
                // UPDATE
                updateTransaction(transaction.getId(), title, finalAmount, finalTimestamp, type, catatan, metodeBayar, tag);
            } else {
                // CREATE
                addTransaction(title, finalAmount, finalTimestamp, type, catatan, metodeBayar, tag);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    // Fungsi helper untuk update label tanggal
    private void updateTanggalLabel(EditText etTanggal) {
        String format = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        etTanggal.setText(sdf.format(selectedDate.getTime()));
        selectedTimestamp = selectedDate.getTimeInMillis();
    }

    // Fungsi ini sudah benar (8 parameter)
    private void addTransaction(String title, double amount, long timestamp, String type,
                                String catatan, String metodeBayar, String tag) {
        String id = databaseReference.push().getKey();

        // Panggil constructor baru (8 parameter)
        Transaction transaction = new Transaction(id, title, amount, timestamp, type, catatan, metodeBayar, tag);

        if (id != null) {
            databaseReference.child(id).setValue(transaction)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Transaksi ditambahkan", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal menambahkan", Toast.LENGTH_SHORT).show());
        }
    }

    // Fungsi ini sudah benar (8 parameter)
    private void updateTransaction(String id, String title, double amount, long timestamp, String type,
                                   String catatan, String metodeBayar, String tag) {
        DatabaseReference transactionRef = databaseReference.child(id);

        // Buat objek baru untuk update (atau update per child)
        Transaction updatedTransaction = new Transaction(id, title, amount, timestamp, type, catatan, metodeBayar, tag);

        // Set value akan menimpa seluruh data di node 'id'
        transactionRef.setValue(updatedTransaction)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Transaksi diperbarui", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui", Toast.LENGTH_SHORT).show());
    }

    // Fungsi ini sudah benar
    private void deleteTransaction(String id) {
        databaseReference.child(id).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Transaksi dihapus", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal menghapus", Toast.LENGTH_SHORT).show());
    }
}