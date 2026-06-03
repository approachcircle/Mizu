package net.approachcircle.plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class NotificationClient {
    private static final Logger logger = PluginLogger.getPluginLogger();

    public static void send(Object message) {
        if (!Mizu.getInstance().getConfig().getBoolean("notify-chunk")) {
            return;
        }
        URL url;
        try {
            url = new URL("https://discord.com/api/webhooks/1288578553021202442/_BU3bOaREmctGsZUVMXEkzXwWmYRLUiGy7br2l5BsCYT9oogvjlJOiFs83T65U58yTRd");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            logger.severe("notification server URL is malformed.");
            return;
        }
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("failed to open connection to notification server");
            return;
        }
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
            logger.severe("notification server protocol is malformed.");
            return;
        }
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.addRequestProperty("Content-Type", "application/json");
        logger.info("send message called");
        byte[] buffer = ("{\"content\": \"" + message.toString() + "\"}").getBytes(StandardCharsets.UTF_8);
        logger.info(new String(buffer, StandardCharsets.UTF_8));
        connection.setFixedLengthStreamingMode(buffer.length);
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("failed to establish connection to notification server");
            return;
        }
        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("failed to write to notification server");
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("failed to read from notification server");
        }
        connection.disconnect();
    }
}
