package com.example.moneymate.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.moneymate.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchTheme;
    private SharedPreferences sharedPreferences;
    private TextView tvHapusData;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        switchTheme = findViewById(R.id.switch_theme_settings);
        tvHapusData = findViewById(R.id.tv_hapus_data);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Sesi berakhir", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        uid = mAuth.getCurrentUser().getUid();

        // Theme
        setupTheme();

        // Hapus data
        setupHapusData();
    }

    /**
     * ================= HAPUS DATA USER =================
     */
    private void setupHapusData() {
        tvHapusData.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Hapus Semua Data")
                    .setMessage("Apakah Anda yakin ingin menghapus semua data transaksi? Tindakan ini tidak dapat dibatalkan.")
                    .setPositiveButton("Ya, Hapus", (dialog, which) -> {

                        firestore.collection("users")
                                .document(uid)
                                .collection("transactions")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (var doc : querySnapshot.getDocuments()) {
                                        doc.getReference().delete();
                                    }
                                    Toast.makeText(
                                            SettingsActivity.this,
                                            "Semua data berhasil dihapus",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(
                                                SettingsActivity.this,
                                                "Gagal menghapus data: " + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show()
                                );

                    })
                    .setNegativeButton("Batal", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });
    }

    /**
     * ================= THEME =================
     */
    private void setupTheme() {
        sharedPreferences = getSharedPreferences("theme_pref", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("is_dark_mode", false);
        switchTheme.setChecked(isDarkMode);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("is_dark_mode", true);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("is_dark_mode", false);
            }
            editor.apply();
        });
    }
}
