package com.example.moneymate.model;

public class Transaction {
    private String id;
    private String title;
    private double amount;
    private long timestamp; // Ini sekarang adalah tanggal pilihan pengguna
    private String type;
    private String catatan; // BARU
    private String metodeBayar; // BARU
    private String tag; // BARU

    public Transaction() {
        // Diperlukan untuk Firebase
    }

    // Perbarui Constructor
    public Transaction(String id, String title, double amount, long timestamp, String type,
                       String catatan, String metodeBayar, String tag) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.timestamp = timestamp;
        this.type = type;
        this.catatan = catatan;
        this.metodeBayar = metodeBayar;
        this.tag = tag;
    }

    // --- Getter dan Setter ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    // --- Getter/Setter BARU ---
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    public String getMetodeBayar() { return metodeBayar; }
    public void setMetodeBayar(String metodeBayar) { this.metodeBayar = metodeBayar; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
}