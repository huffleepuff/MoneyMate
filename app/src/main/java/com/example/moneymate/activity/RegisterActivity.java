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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnRegister;
    private TextView tvToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.et_email_reg);
        etPassword = findViewById(R.id.et_password_reg);
        btnRegister = findViewById(R.id.btn_register);
        tvToLogin = findViewById(R.id.tv_to_login);

        btnRegister.setOnClickListener(v -> registerUser());
        tvToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = etEmail.getText() != null
                ? etEmail.getText().toString().trim()
                : "";

        String password = etPassword.getText() != null
                ? etPassword.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(email) || password.length() < 6) {
            Toast.makeText(
                    this,
                    "Email kosong atau password kurang dari 6 karakter!",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnRegister.setEnabled(true);

                    if (!task.isSuccessful()) {
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registrasi gagal";

                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✅ USER AUTH BERHASIL
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "User tidak valid", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = user.getUid();

                    // ✅ DATA USER UNTUK FIRESTORE
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", email);
                    userData.put("createdAt", Timestamp.now());

                    // 🔥 SIMPAN KE FIRESTORE
                    firestore.collection("users")
                            .document(uid)
                            .set(userData)
                            .addOnSuccessListener(aVoid -> {

                                // ⛔ LOGOUT PAKSA
                                mAuth.signOut();

                                Toast.makeText(
                                        this,
                                        "Registrasi berhasil. Silakan login.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                );
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(
                                        this,
                                        "Gagal menyimpan data user: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                });
    }
}
