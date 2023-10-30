package com.ait.tests.okhttp;

import com.ait.dto.ContactDto;
import com.ait.dto.MessageDto;
import com.google.gson.Gson;
import okhttp3.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class DeleteContactOkhttpTests {

    String token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwic3ViIjoic3R1ZGVudEBnbWFpbC5jb20iLCJpc3MiOiJSZWd1bGFpdCIsImV4cCI6MTY5OTAwMTMxMSwiaWF0IjoxNjk4NDAxMzExfQ.qoNygjgxAF7hFXj129LmNKWClvTYQYkbjciN32N9eUQ";

    public static final MediaType JSON = MediaType.get("application/json;charset=utf-8");

    Gson gson = new Gson();
    OkHttpClient client = new OkHttpClient();

    String id;

    @BeforeMethod
    public void precondition() throws IOException {
        ContactDto contactDto = ContactDto.builder()
                .name("Samara")
                .lastName("Kratz")
                .address("Berlin")
                .email("samara@gmail.com")
                .phone("01234567891")
                .description("Friend")
                .build();

        RequestBody requestBody = RequestBody.create(gson.toJson(contactDto), JSON);
        Request request = new Request.Builder()
                .url("https://contactapp-telran-backend.herokuapp.com/v1/contacts")
                .post(requestBody)
                .addHeader("Authorization", token)
                .build();

        Response response = client.newCall(request).execute();

        MessageDto messageDto = gson.fromJson(response.body().string(), MessageDto.class);
        String message = messageDto.getMessage();
        System.out.println(message);

        //get id from "message": "Contact was added! ID: a100e140-b87a-4690-895f-8e25ff9c6363"
        String[] all = message.split(": ");
        id = all[1];

    }

    @Test
    public void deleteContactByIdPositiveTest() throws IOException {
        Request request = new Request.Builder()
                .url("https://contactapp-telran-backend.herokuapp.com/v1/contacts/" + id)
                .delete()
                .header("Authorization", token)
                .build();

        Response response = client.newCall(request).execute();
        Assert.assertEquals(response.code(),200);
        MessageDto messageDto = gson.fromJson(response.body().string(), MessageDto.class);
        System.out.println(messageDto.getMessage());
        Assert.assertEquals(messageDto.getMessage(),"Contact was deleted!  ");
    }


    @Test
    public void deleteContactByIdNegativeAnyFormatErrorTest() throws IOException {

        String wrongId = "wrong_id";

        Request request = new Request.Builder()
                .url("https://contactapp-telran-backend.herokuapp.com/v1/contacts/" + wrongId)
                .delete()
                .header("Authorization", token)
                .build();

        Response response = client.newCall(request).execute();

        Assert.assertEquals(response.code(), 400);

        MessageDto messageDto = gson.fromJson(response.body().string(), MessageDto.class);
        System.out.println(messageDto.getMessage());
        Assert.assertEquals(messageDto.getMessage(), "Contact with id: " + wrongId + " not found in your contacts!");
    }


    @Test
    public void deleteContactByIdUnauthorizedTest() throws IOException {

        String wrongToken = "WrongToken";

        Request request = new Request.Builder()
                .url("https://contactapp-telran-backend.herokuapp.com/v1/contacts/" + id)
                .delete()
                .header("Authorization", wrongToken)
                .build();

        Response response = client.newCall(request).execute();

        Assert.assertEquals(response.code(), 401);

        MessageDto messageDto = gson.fromJson(response.body().string(), MessageDto.class);
        System.out.println(messageDto.getMessage());
        Assert.assertEquals(messageDto.getMessage(), "JWT strings must contain exactly 2 period characters. Found: 0");
    }


    @Test
    public void deleteContactByIdNotFoundTest() throws IOException {

        String notExistentId = "non_existent_id";

        Request request = new Request.Builder()
                .url("https://contactapp-telran-backend.herokuapp.com/v1/contacts/" + notExistentId)
                .delete()
                .header("Authorization", token)
                .build();

        Response response = client.newCall(request).execute();

        Assert.assertEquals(response.code(), 404);

        MessageDto messageDto = gson.fromJson(response.body().string(), MessageDto.class);
        System.out.println(messageDto.getMessage());
        Assert.assertEquals(messageDto.getMessage(), "Contact with id: " + notExistentId + " not found in your contacts!");
    }

}
