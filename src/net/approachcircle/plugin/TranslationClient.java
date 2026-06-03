package net.approachcircle.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class TranslationClient {
    private static final Logger logger = PluginLogger.getPluginLogger();
    private static final String api_key = "";

    private static String request(String path, byte[] buffer) {
        URI uri;
        try {
            uri = new URI("http://127.0.0.1:5080/" + path);
        } catch (java.net.URISyntaxException e) {
            e.printStackTrace();
            logger.severe("translation API URL is malformed");
            return null;
        }
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) uri.toURL().openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("failed to open connection for translation API");
            return null;
        }
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
            logger.severe("error in underlying protocol");
            return null;
        }
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.addRequestProperty("Content-Type", "application/json");
        connection.setFixedLengthStreamingMode(buffer.length);
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("failed to connect to translation API");
            return null;
        }
        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("failed to write buffer to translation API");
        }
        StringBuilder response;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("failed to read response from translation API");
            return null;
        }
        connection.disconnect();
        return response.toString();
    }

    public static String translate(String message, String from, String to) {
        // auto-detect language to translate from, then decide what to translate to
        byte[] buffer = String.format(
                "{\"q\":\"%s\", \"source\":\"%s\", \"target\":\"%s\", \"format\":\"text\", \"alternatives\":0, \"api_key\":\"%s\"}",
                message,
                from,
                to,
                api_key
        ).getBytes(StandardCharsets.UTF_8);
        String response = request("translate", buffer);
        ObjectMapper mapper = new ObjectMapper();
        TranslationResponse jsonResponse;
        try {
            jsonResponse = mapper.readValue(response, new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.severe("failed to parse response from translation API");
            return null;
        }
        return jsonResponse.translatedText;
    }

    public static String detect(String message) {
        // auto-detect language to translate from, then decide what to translate to
        byte[] buffer = String.format(
                "{\"q\":\"%s\"}",
                message
        ).getBytes(StandardCharsets.UTF_8);
        String response = request("detect", buffer);
        ObjectMapper mapper = new ObjectMapper();
        DetectionResponse[] jsonResponse;
        try {
            jsonResponse = mapper.readValue(response, new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.severe("failed to parse response from translation API");
            return null;
        }
        DetectionResponse strongest = jsonResponse[0];
        // if the API gives us multiple languages back, we will attempt to use the language it is most likely to be
        for (DetectionResponse detection : jsonResponse) {
            logger.info(String.format("%s, confidence %d", detection.language, detection.confidence));
            if (detection.confidence > strongest.confidence) {
                strongest = detection;
            }
        }
        logger.info(String.format("chose %s", strongest.language));
        return strongest.language;
    }
}
