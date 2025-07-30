package com.acs_tr069.test_tr069.logging;

import ch.qos.logback.core.AppenderBase;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.PatternLayout;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class LokiHttpAppender extends AppenderBase<ILoggingEvent> {

    private String lokiUrl;
    private String job = "api_access_log";
    private String labels = "{job=\"api_access_log\",host=\"acs-zeep-logs\"}";  // e.g. {app="acs-zeep"}

    private PatternLayout layout;

    private LoggingEventCompositeJsonEncoder jsonEncoder;

    public void setJsonEncoder(LoggingEventCompositeJsonEncoder encoder) {
        this.jsonEncoder = encoder;
        this.jsonEncoder.setContext(getContext());
        this.jsonEncoder.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (lokiUrl == null || lokiUrl.isEmpty()) {
            addError("No Loki URL set for LokiHttpAppender");
            return;
        }
        try {
            // Encode full event to JSON string
            String jsonLine;
            if (jsonEncoder != null) {
                byte[] jsonBytes = jsonEncoder.encode(event);
                jsonLine = new String(jsonBytes, StandardCharsets.UTF_8).trim();
            } else {
                // fallback to simple message
                jsonLine = escapeJson(event.getFormattedMessage());
            }

            String jsonPayload = "{\"streams\":[{\"labels\":\"" + escapeJson(labels) + "\",\"entries\":[" +
                    "{\"ts\":\"" + formatTimestamp(event.getTimeStamp()) + "\",\"line\":\"" + escapeJson(jsonLine) + "\"}" +
                    "]}]}";

            addInfo("[LokiHttpAppender] Sending payload to Loki: " + jsonPayload);

            sendToLoki(jsonPayload);
        } catch (Exception e) {
            addError("Failed to send log to Loki", e);
        }
    }

    public void setLokiUrl(String lokiUrl) {
        this.lokiUrl = lokiUrl;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public void setPattern(String pattern) {
        layout = new PatternLayout();
        layout.setPattern(pattern);
        layout.setContext(getContext());
        layout.start();
    }

    private void sendToLoki(String jsonPayload) throws Exception {
        URL url = new URL(lokiUrl);
        System.out.println("[LokiHttpAppender] url to loki: " + url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        byte[] payloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
        con.setFixedLengthStreamingMode(payloadBytes.length);

        try (OutputStream os = con.getOutputStream()) {
            os.write(payloadBytes);
        }

        int responseCode = con.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            System.out.println("[LokiHttpAppender] Successfully sent log to Loki: " + labels);
        } else {
            System.out.println("[LokiHttpAppender] error while sending log to loki: " + labels);
            addError("Loki HTTP response code: " + responseCode);
        }
        con.disconnect();
    }

    private String formatTimestamp(long epochMilli) {
        long nanoTime = epochMilli * 1_000_000L; // convert milliseconds to nanoseconds
        return Long.toString(nanoTime);
    }

    private String escapeJson(String s) {
        // Simple JSON string escape (quotes and backslashes)
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
