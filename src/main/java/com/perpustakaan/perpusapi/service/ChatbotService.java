// LOKASI: src/main/java/com/perpustakaan/perpusapi/service/ChatbotService.java
package com.perpustakaan.perpusapi.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.perpustakaan.perpusapi.model.Book;
import com.perpustakaan.perpusapi.repo.BookRepo;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChatbotService {

    @Inject
    private BookRepo bookRepo;

    // --- 1. Definisikan semua kemungkinan 'maksud' pengguna sebagai Enum ---
    private enum Intent {
        GET_OPENING_HOURS,
        GET_BORROWING_DURATION,
        LIST_AVAILABLE_BOOKS,
        GET_LOCATION,          // -- BARU --
        HOW_TO_BOOK,           // -- BARU --
        GENERAL_QUERY          // Pilihan default jika tidak ada intent yang cocok
    }

    // --- 2. Petakan Jawaban ke setiap Intent ---
    private static final Map<Intent, String> intentAnswers = new HashMap<>();
    static {
        intentAnswers.put(Intent.GET_OPENING_HOURS, "Perpustakaan kami buka dari jam 9 pagi hingga 5 sore setiap hari senin hingga sabtu.");
        intentAnswers.put(Intent.GET_BORROWING_DURATION, "Durasi peminjaman buku maksimal adalah 7 hari.");

        // -- JAWABAN BARU YANG LEBIH KREATIF --
        intentAnswers.put(Intent.GET_LOCATION, "Tentu! Kamu bisa menemukan surga buku kami di Jalan Fiksi No. 42, Bojongsoang. Patokannya gampang, cuma 5 menit jalan kaki dari Gerbang Belakang Universitas Telkom. Kalau lihat ada gedung dengan mural buku raksasa, nah itu dia tempatnya! Kami tunggu ya!");
        intentAnswers.put(Intent.HOW_TO_BOOK, "Tentu, booking buku di sini gampang banget! Ikuti langkah-langkah simpel ini ya:\n1. Jelajahi Koleksi: Cari buku impianmu di halaman Beranda atau melalui fitur pencarian.\n2. Klik Tombol Ajaib: Di setiap detail buku, ada tombol 'Booking Buku'. Langsung klik saja!\n3. Tentukan Tanggal Kembali: Pilih tanggal kapan kamu akan mengembalikan bukunya. Ingat, maksimal 7 hari ya!\n4. Konfirmasi & Selesai!: Tinggal klik 'Oke', dan buku itu resmi jadi milikmu sementara. Kamu akan dapat notifikasi jika sudah siap diambil. Gampang kan?");
    }

    // --- 3. Petakan berbagai Keyword (Alias) ke Intent yang sesuai ---
    private static final Map<String, Intent> keywordsToIntents = new LinkedHashMap<>();
    static {
        // Keywords untuk jam buka
        keywordsToIntents.put("jam buka", Intent.GET_OPENING_HOURS);
        keywordsToIntents.put("kapan buka", Intent.GET_OPENING_HOURS);
        keywordsToIntents.put("jam operasional", Intent.GET_OPENING_HOURS);
        keywordsToIntents.put("buka jam berapa", Intent.GET_OPENING_HOURS);
        keywordsToIntents.put("tutup jam berapa", Intent.GET_OPENING_HOURS);
        keywordsToIntents.put("jam berapa", Intent.GET_OPENING_HOURS);

        // Keywords untuk lama pinjam
        keywordsToIntents.put("lama pinjam", Intent.GET_BORROWING_DURATION);
        keywordsToIntents.put("durasi pinjam", Intent.GET_BORROWING_DURATION);
        keywordsToIntents.put("berapa hari pinjam", Intent.GET_BORROWING_DURATION);
        keywordsToIntents.put("maksimal pinjam", Intent.GET_BORROWING_DURATION);

        // Keywords untuk perintah database
        keywordsToIntents.put("buku tersedia", Intent.LIST_AVAILABLE_BOOKS);
        keywordsToIntents.put("daftar buku", Intent.LIST_AVAILABLE_BOOKS);
        keywordsToIntents.put("ada buku apa aja", Intent.LIST_AVAILABLE_BOOKS);
        keywordsToIntents.put("liat buku", Intent.LIST_AVAILABLE_BOOKS);

        // -- KEYWORDS BARU --
        // Keywords untuk Lokasi
        keywordsToIntents.put("lokasi", Intent.GET_LOCATION);
        keywordsToIntents.put("alamat", Intent.GET_LOCATION);
        keywordsToIntents.put("di mana", Intent.GET_LOCATION);
        keywordsToIntents.put("gedungnya", Intent.GET_LOCATION);
        keywordsToIntents.put("bojongsari", Intent.GET_LOCATION); // Typo umum dari Bojongsoang
        keywordsToIntents.put("dekat telkom", Intent.GET_LOCATION);
        keywordsToIntents.put("letak perpustakaan", Intent.GET_LOCATION);

        // Keywords untuk Cara Booking
        keywordsToIntents.put("booking", Intent.HOW_TO_BOOK);
        keywordsToIntents.put("pesan buku", Intent.HOW_TO_BOOK);
        keywordsToIntents.put("cara booking", Intent.HOW_TO_BOOK);
        keywordsToIntents.put("gimana cara pesan", Intent.HOW_TO_BOOK);
        keywordsToIntents.put("pinjam online", Intent.HOW_TO_BOOK);
    }

    // Constructor (tidak berubah)
    public ChatbotService() {
        if (this.bookRepo == null) {
            System.out.println("[DEBUG] BookRepo tidak ter-inject, membuat instance baru secara manual.");
            this.bookRepo = new BookRepo();
        }
    }

    // Metode utama yang menjadi koordinator (tidak berubah)
    public String getChatReply(String userMessage) throws Exception {
        System.out.println("================ CHATBOT SERVICE START ================");
        System.out.println("[DEBUG] Pesan Diterima: " + userMessage);

        Intent userIntent = determineUserIntent(userMessage);
        System.out.println("[DEBUG] Intent terdeteksi: " + userIntent);

        String context = buildContextFromIntent(userIntent);

        String finalPrompt = buildFinalPrompt(userIntent, context, userMessage);
        System.out.println("[DEBUG] Prompt Final yang Dikirim ke AI:\n" + finalPrompt);

        String reply = callGeminiApi(finalPrompt);

        System.out.println("[DEBUG] Balasan dari AI: " + reply);
        System.out.println("================ CHATBOT SERVICE END ==================");
        return reply;
    }

    // --- Helper Methods (tidak perlu diubah sama sekali) ---

    private Intent determineUserIntent(String userMessage) {
        String lowerCaseMessage = userMessage.toLowerCase();
        for (Map.Entry<String, Intent> entry : keywordsToIntents.entrySet()) {
            if (lowerCaseMessage.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return Intent.GENERAL_QUERY;
    }

    private String buildContextFromIntent(Intent intent) {
        switch (intent) {
            case GET_OPENING_HOURS:
            case GET_BORROWING_DURATION:
            case GET_LOCATION:         // -- BARU --
            case HOW_TO_BOOK:          // -- BARU --
                return "Jawaban untuk ini adalah: \"" + intentAnswers.get(intent) + "\".";

            case LIST_AVAILABLE_BOOKS:
                if (bookRepo == null) {
                    System.err.println("[ERROR] BookRepo adalah null! Tidak bisa query ke database.");
                    return "Maaf, saya tidak bisa terhubung ke database buku saat ini.";
                }
                List<Book> books = bookRepo.findAvailableBooksLimit5();
                StringBuilder bookListString = new StringBuilder("Berikut adalah beberapa buku yang tersedia dari database:\n");
                if (books.isEmpty()) {
                    bookListString.append("Saat ini tidak ada buku yang tersedia untuk dipinjam.");
                } else {
                    books.forEach(book -> bookListString.append(String.format("- %s oleh %s\n", book.getNamaBuku(), book.getAuthor())));
                }
                return bookListString.toString();

            case GENERAL_QUERY:
            default:
                return "";
        }
    }

    private String buildFinalPrompt(Intent intent, String context, String userMessage) {
        if (intent == Intent.GENERAL_QUERY || context.isEmpty()) {
            System.out.println("[DEBUG] Tidak ada konteks ditemukan. Menggunakan prompt umum.");
            return String.format("Anda Pustakawan AI ramah bernama 'Jaya'. Jawab pertanyaan pengguna ini dengan gaya percakapan yang bersahabat: \"%s\"", userMessage);
        } else {
            System.out.println("[DEBUG] Konteks ditemukan. Menggunakan prompt berbasis konteks.");
            return String.format("Anda Pustakawan AI ramah bernama 'Jaya'. Jawab pertanyaan pengguna berdasarkan info ini. Berikan jawaban yang alami seolah-olah Anda tahu informasinya, jangan hanya mengulang konteks.\n\n[Info]:\n%s\n\n[Pertanyaan]:\n%s", context, userMessage);
        }
    }

    private String callGeminiApi(String prompt) throws Exception {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("[ERROR] GEMINI_API_KEY tidak ditemukan di environment variables.");
            return "Kunci API AI belum dikonfigurasi di server.";
        }

        Client client = Client.builder().apiKey(apiKey).build();
        GenerateContentResponse response = client.models.generateContent("gemini-1.5-flash", prompt, null);
        return response.text();
    }
}