package com.acs_tr069.test_tr069.logging;

import ch.qos.logback.core.AppenderBase;
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
    private String job = "acs-zeep";
    private String labels = "{}";  // e.g. {app="acs-zeep"}

    private PatternLayout layout;

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

    @Override
    protected void append(ILoggingEvent event) {
        System.out.println("Sending log to Loki: " + event.getFormattedMessage());
        if (lokiUrl == null || lokiUrl.isEmpty()) {
            addError("No Loki URL set for LokiHttpAppender");
            return;
        }

        try {
            // Format log message using pattern layout
            String message = (layout != null) ? layout.doLayout(event) : event.getFormattedMessage();

            // Timestamp in nanoseconds (Loki expects timestamps as strings representing nanoseconds)
            long timestampNanos = event.getTimeStamp() * 1000000L;

            // Build JSON payload for a single log entry stream
            // Example payload:
            /*
            {
              "streams": [
                {
                  "labels": "{job=\"java-app\",app=\"acs-zeep\"}",
                  "entries": [
                    {
                      "ts": "2020-11-02T20:30:12.123Z",
                      "line": "log message here"
                    }
                  ]
                }
              ]
            }
            */

            String jsonPayload = "{\"streams\":[{\"labels\":\"" + escapeJson(labels) + "\",\"entries\":[" +
                    "{\"ts\":\"" + formatTimestamp(event.getTimeStamp()) + "\",\"line\":\"" + escapeJson(message) + "\"}" +
                    "]}]}";

            sendToLoki(jsonPayload);

        } catch (Exception e) {
            addError("Failed to send log to Loki", e);
        }
    }

    private void sendToLoki(String jsonPayload) throws Exception {
        URL url = new URL(lokiUrl);
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
        if (responseCode < 200 || responseCode >= 300) {
            addError("Loki HTTP response code: " + responseCode);
        }
        con.disconnect();
    }

    private String formatTimestamp(long epochMilli) {
        // Format to RFC3339Nano for Loki timestamps (ISO 8601 with nanoseconds)
        // Java 8 has no built-in formatter for nanoseconds, approximate with milliseconds + zeros

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formatted = sdf.format(new Date(epochMilli));
        // Append zeros to simulate nanoseconds (Loki accepts this)
        return formatted.replace("Z", "000000000Z");
    }

    private String escapeJson(String s) {
        // Simple JSON string escape (quotes and backslashes)
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
