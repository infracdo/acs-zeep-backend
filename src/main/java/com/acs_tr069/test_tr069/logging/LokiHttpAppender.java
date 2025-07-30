package com.acs_tr069.test_tr069.logging;

import ch.qos.logback.core.AppenderBase;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.PatternLayout;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LokiHttpAppender extends AppenderBase<ILoggingEvent> {

    private String lokiUrl;
    private String job = "api_access_log";
    private Map<String, String> labels = new HashMap<String, String>() {{
        put("job", "api_access_log");
        put("host", "acs-zeep-logs");
    }};

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
            addError("No Loki URL configured.");
            return;
        }

        try {
            // Serialize the log event into JSON
            String jsonLine;
            if (jsonEncoder != null) {
                byte[] jsonBytes = jsonEncoder.encode(event);
                jsonLine = new String(jsonBytes, StandardCharsets.UTF_8).trim();
            } else {
                jsonLine = event.getFormattedMessage();
            }

            String timestamp = formatTimestamp(event.getTimeStamp());

            // Construct Loki payload using Jackson
            Map<String, Object> entry = new HashMap<>();
            entry.put("ts", timestamp);
            entry.put("line", jsonLine);

            Map<String, Object> stream = new HashMap<>();
            stream.put("labels", labels);
            stream.put("entries", Collections.singletonList(entry));

            Map<String, Object> payload = new HashMap<>();
            payload.put("streams", Collections.singletonList(stream));

            ObjectMapper mapper = new ObjectMapper();
            String payloadJson = mapper.writeValueAsString(payload);

            System.out.println("[LokiHttpAppender] Sending payload: " + payloadJson);
            sendToLoki(payloadJson);
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

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public void setPattern(String pattern) {
        layout = new PatternLayout();
        layout.setPattern(pattern);
        layout.setContext(getContext());
        layout.start();
    }

    private void sendToLoki(String jsonPayload) throws Exception {
        int attempts = 3;
        int delayMs = 1000;

        for (int i = 0; i < attempts; i++) {
            try {
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
                if (responseCode >= 200 && responseCode < 300) {
                    System.out.println("[LokiHttpAppender] Successfully sent log to Loki");
                    con.disconnect();
                    return;
                } else {
                    addError("Loki HTTP response code: " + responseCode);
                }

                con.disconnect();
            } catch (Exception e) {
                addError("Retry " + (i + 1) + " failed: " + e.getMessage(), e);
            }

            Thread.sleep(delayMs); // wait before retrying
        }

        addError("All Loki send attempts failed.");
    }


    private String formatTimestamp(long epochMilli) {
        long nanoTime = epochMilli * 1_000_000L; // convert milliseconds to nanoseconds
        return Long.toString(nanoTime);
    }
}
