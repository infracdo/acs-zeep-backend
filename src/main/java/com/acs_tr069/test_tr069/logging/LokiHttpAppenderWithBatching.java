package com.acs_tr069.test_tr069.logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LokiHttpAppenderWithBatching extends AppenderBase<ILoggingEvent> {

    private String lokiUrl;
    private int batchSize = 20;
    private long flushIntervalMs = 2000;

    private LoggingEventCompositeJsonEncoder jsonEncoder;

    private final BlockingQueue<ILoggingEvent> eventQueue = new LinkedBlockingQueue<>(10000);
    private final List<String[]> batchBuffer = new ArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean running = true;

    private Thread workerThread;

    private Map<String, String> labels = new HashMap<String, String>() {{
        put("job", "zeep-sbx-api");
        put("host", "zeep-sbx-log");
    }};

    public void setLokiUrl(String lokiUrl) {
        this.lokiUrl = lokiUrl;
    }

    public void setJsonEncoder(LoggingEventCompositeJsonEncoder encoder) {
        this.jsonEncoder = encoder;
        this.jsonEncoder.setContext(getContext());
        this.jsonEncoder.start();
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setFlushIntervalMs(long flushIntervalMs) {
        this.flushIntervalMs = flushIntervalMs;
    }

    @Override
    public void start() {
        super.start();
        running = true;

        workerThread = new Thread(this::batchProcessor, "Loki-Batch-Worker");
        workerThread.setDaemon(true);
        workerThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(this::flushAndStop));
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!running || lokiUrl == null || lokiUrl.isEmpty()) {
            return;
        }
        event.prepareForDeferredProcessing(); // capture MDC snapshot here

        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc == null || mdc.isEmpty()) {
            addWarn("MDC is empty for event: " + event.getFormattedMessage());
        }

        if (!eventQueue.offer(event)) {
            addWarn("Loki queue full, dropping log event");
        }
    }

    private void batchProcessor() {
        long lastFlushTime = System.currentTimeMillis();

        while (running) {
            try {
                ILoggingEvent event = eventQueue.poll(2000, TimeUnit.MILLISECONDS);
                if (event != null) {
                    String timestamp = formatTimestamp(event.getTimeStamp());

                    // Use configured encoder to produce JSON string with MDC etc
                    byte[] encoded = jsonEncoder.encode(event);
                    String jsonLine = new String(encoded, StandardCharsets.UTF_8);
                    // System.out.println("logging jsonLine " + jsonLine);

                    synchronized (batchBuffer) {
                        batchBuffer.add(new String[]{timestamp, jsonLine});
                    }
                }

                long now = System.currentTimeMillis();
                boolean shouldFlush = false;

                synchronized (batchBuffer) {
                    shouldFlush = batchBuffer.size() >= batchSize || (now - lastFlushTime) >= flushIntervalMs;
                }

                if (shouldFlush) {
                    flushBuffer();
                    lastFlushTime = now;
                }
            } catch (Exception e) {
                addError("Error in Loki batch processor", e);
            }
        }

        // final flush
        flushBuffer();
    }

    private void flushBuffer() {
        List<String[]> batchToSend;
        synchronized (batchBuffer) {
            if (batchBuffer.isEmpty()) return;
            batchToSend = new ArrayList<>(batchBuffer);
            batchBuffer.clear();
        }

        Map<String, Object> stream = new HashMap<>();
        stream.put("stream", labels);
        stream.put("values", batchToSend);

        Map<String, Object> payload = new HashMap<>();
        payload.put("streams", Collections.singletonList(stream));

        try {
            String json = objectMapper.writeValueAsString(payload);
            sendToLoki(json);
        } catch (Exception e) {
            addError("Failed to send batched logs to Loki", e);
        }
    }

    private void sendToLoki(String payloadJson) throws Exception {
        URL url = new URL(lokiUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(1000);
        con.setReadTimeout(2000);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        byte[] payloadBytes = payloadJson.getBytes(StandardCharsets.UTF_8);
        con.setFixedLengthStreamingMode(payloadBytes.length);

        try (OutputStream os = con.getOutputStream()) {
            os.write(payloadBytes);
        }

        int responseCode = con.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            Scanner s = new Scanner(con.getErrorStream()).useDelimiter("\\A");
            String errorBody = s.hasNext() ? s.next() : "(empty)";
            addError("Loki rejected batch with HTTP " + responseCode + ": " + errorBody);
        }

        con.disconnect();
    }

    private String formatTimestamp(long epochMilli) {
        long nanos = epochMilli * 1_000_000L;
        return Long.toString(nanos);
    }

    private void flushAndStop() {
        running = false;
        try {
            if (workerThread != null) {
                workerThread.join(3000);
            }
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void stop() {
        flushAndStop();
        super.stop();
    }
}
