package com.example.moneymate.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
// import android.widget.TextView; // <-- SUDAH TIDAK DIPAKAI
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView; // <-- TETAP DIPAKAI

import com.example.moneymate.R;

public class DashboardActivity extends AppCompatActivity {

    // 'cardTentangSaya' menggantikan 'tvTentangSaya'
    private CardView cardCatatan, cardRekap, cardGrafik, cardPengaturan, cardTentangSaya;
    private Button btnTutup;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadTheme();

        setContentView(R.layout.activity_dashboard);

        // Inisialisasi Views
        cardCatatan = findViewById(R.id.card_catatan);
        cardRekap = findViewById(R.id.card_rekap);
        cardGrafik = findViewById(R.id.card_grafik);
        cardPengaturan = findViewById(R.id.card_pengaturan);
        btnTutup = findViewById(R.id.btn_tutup);
        cardTentangSaya = findViewById(R.id.card_tentang_saya); // <-- INISIALISASI BARU

        // --- Set OnClickListener untuk setiap tombol ---

        cardCatatan.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(intent);
        });

        cardRekap.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, RekapActivity.class);
            startActivity(intent);
        });

        cardGrafik.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, GrafikActivity.class);
            startActivity(intent);
        });

        cardPengaturan.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // --- LISTENER BARU UNTUK CARD "TENTANG SAYA" ---
        cardTentangSaya.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, TentangSayaActivity.class);
            startActivity(intent);
        });

        btnTutup.setOnClickListener(v -> {
            finishAffinity();
        });
    }

    private void loadTheme() {
        sharedPreferences = getSharedPreferences("theme_pref", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("is_dark_mode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}