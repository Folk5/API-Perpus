package com.perpustakaan.perpusapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class Book {

    @JsonProperty("buku_id")
    private int bukuId;

    @JsonProperty("nama_buku")
    private String namaBuku;

    @JsonProperty("tipe_buku")
    private String tipeBuku;

    @JsonProperty("jenis_buku")
    private String jenisBuku;

    @JsonProperty("tgl_terbit")
    private String tglTerbit;

    @JsonProperty("author")
    private String author;

    @JsonProperty("rakbuku_id_fk")
    private int rakbukuIdFk;

    @JsonProperty("status_booking")
    private boolean statusBooking;

    @JsonProperty("jumlah")
    private int jumlah;

    @JsonProperty("jml_tersedia")
    private int jmlTersedia;

    @JsonProperty("booking_date")
    private Date bookingDate;

    @JsonProperty("expired_date")
    private Date expiredDate;

    public Book() {
    }

    // Constructor lengkap
    public Book(int bukuId, String namaBuku, String tipeBuku, String jenisBuku,
                String tglTerbit, String author, int rakbukuIdFk,
                boolean statusBooking, int jumlah, int jmlTersedia,
                Date bookingDate, Date expiredDate) {
        this.bukuId = bukuId;
        this.namaBuku = namaBuku;
        this.tipeBuku = tipeBuku;
        this.jenisBuku = jenisBuku;
        this.tglTerbit = tglTerbit;
        this.author = author;
        this.rakbukuIdFk = rakbukuIdFk;
        this.statusBooking = statusBooking;
        this.jumlah = jumlah;
        this.jmlTersedia = jmlTersedia;
        this.bookingDate = bookingDate;
        this.expiredDate = expiredDate;
    }

    // Getter dan Setter untuk bookingDate dan expiredDate
    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Date getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(Date expiredDate) {
        this.expiredDate = expiredDate;
    }

    // Getter dan Setter untuk atribut lainnya
    public int getBukuId() {
        return bukuId;
    }

    public void setBukuId(int bukuId) {
        this.bukuId = bukuId;
    }

    public String getNamaBuku() {
        return namaBuku;
    }

    public void setNamaBuku(String namaBuku) {
        this.namaBuku = namaBuku;
    }

    public String getTipeBuku() {
        return tipeBuku;
    }

    public void setTipeBuku(String tipeBuku) {
        this.tipeBuku = tipeBuku;
    }

    public String getJenisBuku() {
        return jenisBuku;
    }

    public void setJenisBuku(String jenisBuku) {
        this.jenisBuku = jenisBuku;
    }

    public String getTglTerbit() {
        return tglTerbit;
    }

    public void setTglTerbit(String tglTerbit) {
        this.tglTerbit = tglTerbit;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getRakbukuIdFk() {
        return rakbukuIdFk;
    }

    public void setRakbukuIdFk(int rakbukuIdFk) {
        this.rakbukuIdFk = rakbukuIdFk;
    }

    public boolean isStatusBooking() {
        return statusBooking;
    }

    public void setStatusBooking(boolean statusBooking) {
        this.statusBooking = statusBooking;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public int getJmlTersedia() {
        return jmlTersedia;
    }

    public void setJmlTersedia(int jmlTersedia) {
        this.jmlTersedia = jmlTersedia;
    }
}
