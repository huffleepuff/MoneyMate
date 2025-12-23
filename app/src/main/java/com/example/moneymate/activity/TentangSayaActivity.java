package com.example.moneymate.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.moneymate.R;

public class TentangSayaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tentang_saya);

        Toolbar toolbar = findViewById(R.id.toolbar_tentang_saya);
        setSupportActionBar(toolbar);
        // Aktifkan tombol back (panah kiri) di Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set listener untuk tombol back
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}