package com.perpustakaan.perpusapi.resource;

import com.perpustakaan.perpusapi.model.ChatRequest;
import com.perpustakaan.perpusapi.model.ChatResponse;
import com.perpustakaan.perpusapi.service.ChatbotService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/chat")
public class ChatbotResource {

    // Hapus anotasi @Inject
    private final ChatbotService chatbotService;

    // Buat constructor untuk inisialisasi service secara manual
    public ChatbotResource() {
        this.chatbotService = new ChatbotService();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleChat(ChatRequest request) {
        try {
            String reply = chatbotService.getChatReply(request.getMessage());
            return Response.ok(new ChatResponse(reply)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity(new ChatResponse("Maaf, terjadi kesalahan pada server chatbot."))
                    .build();
        }
    }
}