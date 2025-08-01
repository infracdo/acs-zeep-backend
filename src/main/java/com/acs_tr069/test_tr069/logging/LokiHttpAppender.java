package com.acs_tr069.test_tr069.logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LokiHttpAppender extends AppenderBase<ILoggingEvent> {

    private String lokiUrl;
    private String job = "api_access_log";

    private Map<String, String> labels = new HashMap<String, String>() {{
        put("job", job);
        put("host", "acs-zeep-logs");
    }};

    private LoggingEventCompositeJsonEncoder jsonEncoder;

    public void setJsonEncoder(LoggingEventCompositeJsonEncoder encoder) {
        this.jsonEncoder = encoder;
        this.jsonEncoder.setContext(getContext());
        this.jsonEncoder.start();
    }

    public void setLokiUrl(String lokiUrl) {
        this.lokiUrl = lokiUrl;
    }

    public void setJob(String job) {
        this.job = job;
        labels.put("job", job); // update the label if job changes
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (lokiUrl == null || lokiUrl.isEmpty()) {
            addError("Loki URL is not set.");
            return;
        }

        try {
            // Convert the log event to JSON
            String logLine = (jsonEncoder != null)
                    ? new String(jsonEncoder.encode(event), StandardCharsets.UTF_8).trim()
                    : event.getFormattedMessage();

            String timestamp = formatTimestamp(event.getTimeStamp());

            // Build Loki payload
            Map<String, Object> stream = new HashMap<>();
            stream.put("stream", labels);
            stream.put("values", Collections.singletonList(new String[]{timestamp, logLine}));

            Map<String, Object> payload = new HashMap<>();
            payload.put("streams", Collections.singletonList(stream));

            String jsonPayload = new ObjectMapper().writeValueAsString(payload);

            // Send to Loki
            sendToLoki(jsonPayload);

        } catch (Exception e) {
            addError("Failed to send log to Loki", e);
        }
    }

    private void sendToLoki(String jsonPayload) throws Exception {
        URL url = new URL(lokiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);  // avoid hanging forever
        connection.setReadTimeout(5000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        byte[] bytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(bytes.length);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(bytes);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            addError("Loki response code: " + responseCode);
        }

        connection.disconnect();
    }

    private String formatTimestamp(long epochMilli) {
        long nanos = epochMilli * 1_000_000L;
        return Long.toString(nanos);
    }

    @Override
    public void stop() {
        super.stop();
        if (jsonEncoder != null) {
            jsonEncoder.stop();
        }
    }
}
