package net.approachcircle.plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class WebhookClient {
    private static final Logger logger = PluginLogger.getPluginLogger();

    public static void send(Object message) {
        String webhookURL = System.getenv("MIZU_WEBHOOK");
        if (webhookURL == null) {
            PluginLogger.getPluginLogger().warning("the MIZU_WEBHOOK environment variable is not set!");
            return;
        }
        URL url;
        try {
            url = new URL(webhookURL);
        } catch (MalformedURLException e) {
            Utils.error(e, "the webhook URL is malformed.");
            return;
        }
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Utils.error(e, "failed to open connection to the webhook");
            return;
        }
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            Utils.error(e, "the webhook protocol is malformed.");
            return;
        }
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.addRequestProperty("Content-Type", "application/json");
        byte[] buffer = ("{\"content\": \"" + message.toString() + "\"}").getBytes(StandardCharsets.UTF_8);
        logger.info(new String(buffer, StandardCharsets.UTF_8));
        connection.setFixedLengthStreamingMode(buffer.length);
        try {
            connection.connect();
        } catch (IOException e) {
            Utils.error(e, "failed to establish connection to the webhook");
            return;
        }
        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(buffer);
        } catch (IOException e) {
            Utils.error(e, "failed to write to the webhook");
        }
        connection.disconnect();
    }
}
