package org.seen.controller;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.quarkus.security.Authenticated;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.Body;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.seen.dto.BaseResponse;
import org.seen.dto.LoginRequest;
import org.seen.dto.LoginResponse;
import org.seen.security.JwtService;
import org.seen.service.ProducerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Path("/base")
@ApplicationScoped
public class BaseController {

    @Inject
    JwtService jwtService;

    @Inject
    ProducerService producerService;

    @GET
    @Path("/ping-public")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public String helloPublic() {
        return "Hello RESTEasy Public";
    }

    @GET
    @Path("/ping-private")
    @RolesAllowed("**")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloPrivate() {
        return "Hello RESTEasy Private";
    }

    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        if (request.getUsername().equalsIgnoreCase("admin") && request.getPassword().equalsIgnoreCase("admin")) {
            return Response.status(Response.Status.OK).entity(LoginResponse.builder().token(jwtService.generateToken(request.getUsername())).build()).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity(BaseResponse.builder().message("Username Password Incorrect").build()).build();
        }
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadCSV(@MultipartForm MultipartFormDataInput input) {
        try {
            InputStream inputStream = input.getFormDataPart("file", InputStream.class, null);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            if (bufferedReader.ready()) {
                bufferedReader.mark(1);
                if (bufferedReader.read() != 0xFEFF) {
                    bufferedReader.reset();
                }
            }

            CSVReader csvReader = new CSVReader(bufferedReader);
            List<String[]> records = csvReader.readAll();

            StringBuilder response = new StringBuilder("Uploaded CSV File Content:\n");
            StringBuilder data = new StringBuilder();
            for (String[] record : records) {
                response.append(String.join(", ", record)).append("\n");
                data.append("[").append(String.join(", ", record)).append("]");
            }
            producerService.sendMessage(data.toString());
            return Response.ok(response.toString()).build();
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error reading CSV file").build();
        }
    }


}
