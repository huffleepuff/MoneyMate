package com.example.moneymate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymate.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvToRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.et_email_login);
        etPassword = findViewById(R.id.et_password_login);
        btnLogin = findViewById(R.id.btn_login);
        tvToRegister = findViewById(R.id.tv_to_register);

        btnLogin.setOnClickListener(v -> loginUser());

        tvToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void loginUser() {
        String email = etEmail.getText() != null
                ? etEmail.getText().toString().trim()
                : "";

        String password = etPassword.getText() != null
                ? etPassword.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Isi semua bidang!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        btnLogin.setEnabled(true);

                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Login gagal";

                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✅ AUTH BERHASIL
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        btnLogin.setEnabled(true);
                        Toast.makeText(this, "User tidak valid", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = user.getUid();

                    // 🔍 CEK DATA USER DI FIRESTORE
                    firestore.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                btnLogin.setEnabled(true);

                                if (!documentSnapshot.exists()) {
                                    // ⚠️ DATA USER HILANG / TIDAK KONSISTEN
                                    Toast.makeText(
                                            this,
                                            "Data akun tidak ditemukan. Silakan daftar ulang.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    mAuth.signOut();
                                    return;
                                }

                                // ✅ DATA USER VALID → LANJUT DASHBOARD
                                startActivity(
                                        new Intent(LoginActivity.this, DashboardActivity.class)
                                );
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnLogin.setEnabled(true);

                                Toast.makeText(
                                        this,
                                        "Gagal mengambil data user: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                });
    }
}
