package com.namastemart.srv;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.BufferedReader;

@WebServlet("/chatbot")
public class ChatbotServlet extends HttpServlet 
{

    private static final String API_KEY = "AIzaSyAEqpvjNR85qYJ9Ri0037uB2JYt_GjTScM";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=" + API_KEY;
		
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Read request body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) 
		{
            String line;
            while ((line = reader.readLine()) != null) 
			{
                sb.append(line);
            }
        }

        JsonObject jsonRequest = JsonParser.parseString(sb.toString()).getAsJsonObject();
        JsonArray contentsArray = jsonRequest.getAsJsonArray("contents");
        JsonObject firstContent = contentsArray.get(0).getAsJsonObject();
        JsonArray partsArray = firstContent.getAsJsonArray("parts");
        JsonObject firstPart = partsArray.get(0).getAsJsonObject();
        String userMessage = firstPart.get("text").getAsString();

        // Prepare API request
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        // Create request body
        JsonObject jsonRequestBody = new JsonObject();
        JsonObject jsonContent = new JsonObject();
        jsonContent.addProperty("role", "user");
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", userMessage);
        parts.add(part);
        jsonContent.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(jsonContent);
        jsonRequestBody.add("contents", contents);

        try (PrintWriter out = new PrintWriter(conn.getOutputStream())) 
		{
            out.print(jsonRequestBody.toString());
            out.flush();
        }

        // Read API response
        int statusCode = conn.getResponseCode();
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) 
		{
            String line;
            while ((line = in.readLine()) != null) 
			{
                responseBuilder.append(line);
            }
        }

        // Return API response
        response.setStatus(statusCode);
        try (PrintWriter out = response.getWriter()) 
		{
            out.print(responseBuilder.toString());
        }
    }
}