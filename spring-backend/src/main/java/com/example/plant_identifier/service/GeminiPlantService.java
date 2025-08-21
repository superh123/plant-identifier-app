package com.example.plant_identifier.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiPlantService {

    @Value("${gemini.api-key}")
    private String API_KEY;

    private Client client;

    public GeminiPlantService() {
    }

    public Content constructContent(MultipartFile file) throws IOException {

        byte[] imageBytes = file.getBytes();

        return Content.fromParts(
                Part.fromText("You're an expert botanist. Identify this plant, respond with the scientific" +
                        "and common name, and then a 1 sentence description in the exact format listed after 'FORMAT:', " +
                        "IMPORTANT: Use absolutely NO special characters like asterisks (*), quotes ('), or any other formatting. Output plain text only " +
                        "FOR EXAMPLE: WRONG: Scientific: *Rosa* 'Crimson Cascade', CORRECT: Scientific: Rosa Crimson Cascade" +
                        "FORMAT: Scientific: [name], Common: [name], Description: [description]"),
                Part.fromBytes(imageBytes, "image/jpg")
        );
    }

    public List<String> generateContent(MultipartFile file) throws IOException {

        //create client
        this.client = Client.builder().apiKey(API_KEY).build();
        String commonName = null;
        String scientificName = null;
        String description = null;

        try {
            //create prompt
            GenerateContentResponse contentResponse = client.models.generateContent("gemini-1.5-flash", constructContent(file), null);

            String result = contentResponse.text().trim();
            System.out.println("AI RESPONSE: " + result);

            // Parse the response to extract names
            scientificName = "";
            commonName = "";
            description = "";


            if (result.contains("Scientific:") && result.contains("Common:")) {
                // Extract scientific name
                int scientificStart = result.indexOf("Scientific:") + 11;
                int scientificEnd = result.indexOf(",", scientificStart);
                if (scientificEnd > scientificStart) {
                    scientificName = result.substring(scientificStart, scientificEnd).trim();
                }

                // Extract common name
                int commonStart = result.indexOf("Common:") + 7;
                int commonEnd = result.indexOf(",", commonStart);
                commonName = result.substring(commonStart, commonEnd).trim();

                //Extract description
                int descriptionStart = result.indexOf("Description:") + 12;
                description = result.substring(descriptionStart).trim();

            }

            return new ArrayList<>(List.of(scientificName, commonName, description));
//            System.out.println("Scientific name: " + scientificName);
//            System.out.println("Common Name:" + commonName);
//            System.out.println("Description" + description);



        } catch (Exception e) {
            System.err.println("Error identifying plant" + e.getMessage());

            return new ArrayList<>(List.of(scientificName, commonName, description));

        }


    }
















}
