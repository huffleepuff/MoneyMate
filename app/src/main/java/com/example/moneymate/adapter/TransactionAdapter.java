package com.example.moneymate.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.R;
import com.example.moneymate.model.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final Context context;
    private final List<Transaction> transactionList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Transaction transaction);
        void onDeleteClick(Transaction transaction);
    }

    public TransactionAdapter(Context context, List<Transaction> transactionList, OnItemClickListener listener) {
        this.context = context;
        this.transactionList = transactionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.tvTitle.setText(transaction.getTitle());

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.tvAmount.setText(formatRupiah.format(transaction.getAmount()));

        // Atur warna berdasarkan tipe (Pemasukan/Pengeluaran)
        if (transaction.getAmount() > 0) {
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Hijau
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")); // Merah
        }

        // Tampilkan Tanggal
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(transaction.getTimestamp()));

        // Tampilkan Metode Pembayaran
        if (transaction.getMetodeBayar() != null && !transaction.getMetodeBayar().isEmpty()) {
            holder.tvMetodeBayar.setText(transaction.getMetodeBayar());
            holder.tvMetodeBayar.setVisibility(View.VISIBLE);
        } else {
            holder.tvMetodeBayar.setVisibility(View.GONE);
        }

        // Tampilkan Tag
        if (transaction.getTag() != null && !transaction.getTag().isEmpty()) {
            holder.tvTag.setText("#" + transaction.getTag()); // Tambah '#' di depan tag
            holder.tvTag.setVisibility(View.VISIBLE);
        } else {
            holder.tvTag.setVisibility(View.GONE);
        }

        // Tampilkan Catatan (jika ada)
        if (transaction.getCatatan() != null && !transaction.getCatatan().isEmpty()) {
            holder.tvCatatan.setText("Catatan: " + transaction.getCatatan());
            holder.tvCatatan.setVisibility(View.VISIBLE);
        } else {
            holder.tvCatatan.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(transaction));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(transaction));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount, tvDate, tvMetodeBayar, tvTag, tvCatatan; // Menambahkan tvDate, tvMetodeBayar, tvTag, tvCatatan
        ImageView btnEdit, btnDelete;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date); // Inisialisasi
            tvMetodeBayar = itemView.findViewById(R.id.tv_metode_bayar); // Inisialisasi
            tvTag = itemView.findViewById(R.id.tv_tag); // Inisialisasi
            tvCatatan = itemView.findViewById(R.id.tv_catatan); // Inisialisasi
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}