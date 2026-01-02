package com.example.moneymate.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.example.moneymate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    private CardView cardCatatan, cardRekap, cardGrafik, cardPengaturan, cardTentangSaya;
    private Button btnLogout;

    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // 🔐 CEK SESSION
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            redirectToLogin();
            return;
        }

        loadTheme();
        setContentView(R.layout.activity_dashboard);

        initViews();

        // 🔍 VALIDASI DATA USER DI FIRESTORE
        validateUserData(user.getUid());

        // === NAVIGATION ===
        cardCatatan.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        cardRekap.setOnClickListener(v ->
                startActivity(new Intent(this, RekapActivity.class)));

        cardGrafik.setOnClickListener(v ->
                startActivity(new Intent(this, GrafikActivity.class)));

        cardPengaturan.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        cardTentangSaya.setOnClickListener(v ->
                startActivity(new Intent(this, TentangSayaActivity.class)));

        // === LOGOUT ===
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Berhasil Logout", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });
    }

    private void initViews() {
        cardCatatan = findViewById(R.id.card_catatan);
        cardRekap = findViewById(R.id.card_rekap);
        cardGrafik = findViewById(R.id.card_grafik);
        cardPengaturan = findViewById(R.id.card_pengaturan);
        cardTentangSaya = findViewById(R.id.card_tentang_saya);
        btnLogout = findViewById(R.id.btn_tutup);

        btnLogout.setText("Logout");
    }

    private void validateUserData(String uid) {
        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // ⚠️ AUTH ADA, DATA TIDAK ADA → INCONSISTENT STATE
                        Toast.makeText(
                                this,
                                "Data akun tidak valid. Silakan login ulang.",
                                Toast.LENGTH_LONG
                        ).show();

                        mAuth.signOut();
                        redirectToLogin();
                    }
                    // kalau ada → lanjut normal
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Gagal memverifikasi data user: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
