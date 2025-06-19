// LOKASI: src/main/java/com/perpustakaan/perpusapi/service/ChatbotService.java
package com.perpustakaan.perpusapi.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.perpustakaan.perpusapi.model.Book;
import com.perpustakaan.perpusapi.repo.BookRepo;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotService {

    @Inject
    private BookRepo bookRepo;

    // Knowledge Base (FAQ)
    private static final Map<String, String> knowledgeBase = new HashMap<>();
    static {
        knowledgeBase.put("jam buka", "Perpustakaan kami buka dari jam 9 pagi hingga 5 sore setiap hari kerja.");
        knowledgeBase.put("lama pinjam", "Durasi peminjaman buku maksimal adalah 7 hari.");
    }

    // Constructor untuk memastikan bookRepo tidak null
    public ChatbotService() {
        // Ini adalah fallback jika Dependency Injection (@Inject) tidak bekerja
        if (this.bookRepo == null) {
            System.out.println("[DEBUG] BookRepo tidak ter-inject, membuat instance baru secara manual.");
            this.bookRepo = new BookRepo();
        }
    }

    public String getChatReply(String userMessage) throws Exception {
        // --- LOGGING UNTUK DEBUGGING ---
        System.out.println("================ CHATBOT SERVICE START ================");
        System.out.println("[DEBUG] Pesan Diterima: " + userMessage);

        String lowerCaseMessage = userMessage.toLowerCase();
        String context = "";
        String finalPrompt;

        // 1. Cek Knowledge Base (FAQ)
        System.out.println("[DEBUG] Memeriksa Knowledge Base (FAQ)...");
        for (Map.Entry<String, String> entry : knowledgeBase.entrySet()) {
            System.out.println("[DEBUG] -> Mencocokkan dengan keyword: '" + entry.getKey() + "'");
            if (lowerCaseMessage.contains(entry.getKey())) {
                context = "Jawaban untuk ini adalah: \"" + entry.getValue() + "\".";
                System.out.println("[DEBUG] ==> FAQ DITEMUKAN! Konteks diatur.");
                break;
            }
        }

        // 2. Cek Perintah Database
        if (context.isEmpty()) {
            System.out.println("[DEBUG] FAQ tidak ditemukan. Memeriksa perintah database...");
            if (lowerCaseMessage.contains("buku tersedia") || lowerCaseMessage.contains("daftar buku")) {
                System.out.println("[DEBUG] ==> Keyword database DITEMUKAN! Mengambil data...");

                // Pengecekan penting untuk memastikan bookRepo tidak null
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
                context = bookListString.toString();
                System.out.println("[DEBUG] Konteks dari database diatur.");
            }
        }

        // 3. Buat Prompt akhir untuk AI
        if (!context.isEmpty()) {
            finalPrompt = String.format("Anda Pustakawan AI ramah. Jawab pertanyaan pengguna berdasarkan info ini.\n\n[Info]:\n%s\n\n[Pertanyaan]:\n%s", context, userMessage);
        } else {
            System.out.println("[DEBUG] Tidak ada konteks ditemukan. Menggunakan prompt umum.");
            finalPrompt = String.format("Anda Pustakawan AI ramah. Jawab pertanyaan pengguna ini: \"%s\"", userMessage);
        }

        System.out.println("[DEBUG] Prompt Final yang Dikirim ke AI:\n" + finalPrompt);

        // Panggil API Gemini
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("[ERROR] GEMINI_API_KEY tidak ditemukan di environment variables.");
            return "Kunci API AI belum dikonfigurasi di server.";
        }

        Client client = Client.builder().apiKey(apiKey).build();
        GenerateContentResponse response = client.models.generateContent("gemini-1.5-flash", finalPrompt, null);

        String reply = response.text();
        System.out.println("[DEBUG] Balasan dari AI: " + reply);
        System.out.println("================ CHATBOT SERVICE END ==================");

        return reply;
    }
}