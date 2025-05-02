package com.perpustakaan.perpusapi.resource;


import com.perpustakaan.perpusapi.model.Member;
import com.perpustakaan.perpusapi.service.MemberService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@Path("/profile")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfileResoruce {

    private final MemberService memberService = new MemberService();

    @GET
    @Path("/{accountId}")
    public Response getProfile(@PathParam("accountId") int accountId) {
        Optional<Member> member = memberService.getProfile(accountId);
        if (member.isPresent()) {
            return Response.ok(member.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Profil tidak ditemukan\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{accountId}")
    public Response updateProfile(@PathParam("accountId") int accountId, Member updatedMember) {
        updatedMember.setAccount_id_fk(accountId);
        boolean updated = memberService.updateProfile(updatedMember);

        if (updated) {
            return Response.ok("{\"message\": \"Profil berhasil diperbarui\"}").build();
        }else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Gagal memperbarui profil\"}")
                    .build();
        }
    }
}
