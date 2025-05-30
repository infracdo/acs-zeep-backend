package com.acs_tr069.test_tr069.radius.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.acs_tr069.test_tr069.radius.repository.AccountingRepository;

@Service
public class RadiusService {

    private final AccountingRepository accountingRepository;

    public RadiusService(AccountingRepository accountingRepository) {
        this.accountingRepository = accountingRepository;
    }


    // Return number of currently connected users
    public Long getCountCurrentlyConnectedUsers() {
        try {
            return accountingRepository.countCurrentlyConnectedUsers();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve currently connected users count", e);
        }
    }

    // Return number of currently connected access points
    public Long getCountCurrentlyConnectedAPs() {
        try {
            return accountingRepository.countCurrentlyConnectedAPs();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve currently connected access points count", e);
        }
    }

    // Return the total number of user connections for today
    public Long getCountTotalUserConnectionsToday() {
        try {
            long startOfDay = LocalDate.now()
                .atStartOfDay()
                .toEpochSecond(ZoneOffset.UTC);

            long endOfDay = startOfDay + 86400;

            return accountingRepository.countTotalUserConnectionsToday(startOfDay, endOfDay);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve total user connections for today", e);
        }
    }

    // Return total bandwidth consumption for today
    public String getTotalBandwidthConsumptionToday() {
        try {
            long startOfDay = LocalDate.now()
                .atStartOfDay()
                .toEpochSecond(ZoneOffset.UTC);

            long endOfDay = startOfDay + 86400;

            long totalRawBytes = accountingRepository.totalBandwidthConsumptionToday(startOfDay, endOfDay);

            return formatBytes(totalRawBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve total bandwidth consumption for today", e);
        }
    }

    // Return average connection time
    public String getAverageConnectionTime() {
        try {
            Double avgSeconds = accountingRepository.findAverageConnectionTime();
            return formatDuration(avgSeconds);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve average connection time", e);
        }
    }

    // Return average bandwidth per connection
    public String getAverageBandwidthPerConnection() {
        try {
            Double avgBytesPerSec = accountingRepository.findAverageBandwidthPerConnection();

            if (avgBytesPerSec == null || avgBytesPerSec <= 0) return "0 B/s";

            return formatBandwidth(avgBytesPerSec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve average bandwidth per connection", e);
        }
    }

    // Return list of access points
    public List<String> getAllAccessPoints() {
        try {
            return accountingRepository.findAllAccessPoints();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all access points", e);
        }
    }

    // Return number of currently connected users per access point
    public Map<String, Long> getCountCurrentlyConnectedUsersPerAP() {
        try {
            List<Object[]> currentlyConnectedUsers = accountingRepository.countCurrentlyConnectedUsersPerAP();
            Map<String, Long> response = new HashMap<>();

            for (Object[] row : currentlyConnectedUsers) {
                String calledStationId = (String) row[0];
                Number count = (Number) row[1];
                response.put(calledStationId, count.longValue());
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve currently connected users per access point count", e);
        }
    }

    // Return list of currently connected users per access point
    public Map<String, List<Map<String, Object>>> getCurrentlyConnectedUsersPerAP() {
        try {
            List<Object[]> currentlyConnectedUsers = accountingRepository.findCurrentlyConnectedUsersPerAP();
            Map<String, List<Map<String, Object>>> response = new LinkedHashMap<>();

            for (Object[] row : currentlyConnectedUsers) {
                String called_station_id = (String) row[0];
                
                Map<String, Object> userDetails = new HashMap<>();
                userDetails.put("username", row[1]);
                userDetails.put("acctinputoctets", row[2]);
                userDetails.put("acctoutputoctets", row[3]);
                userDetails.put("nasport", row[4]);
                userDetails.put("calling_station_id", row[5]);
                userDetails.put("timestamp", row[6]);

                response.computeIfAbsent(called_station_id, k -> new java.util.ArrayList<>()).add(userDetails);
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve list of currently connected users per access point", e);
        }
    }


    // HELPER METHODS //

    // Format duration in seconds to human-readable format
    private String formatDuration(Double seconds) {
        if (seconds == null || seconds == 0) return "0";

        long hours = (long) (seconds / 3600);
        long minutes = (long) ((seconds % 3600) / 60);
        long secs = (long) (seconds % 60);

        StringBuilder result = new StringBuilder();

        // Only show 'hrs' if hours is greater than or equal to 1
        if (hours > 0) {
            result.append(hours).append("hrs");
        }

        // Only show 'mins' if minutes is greater than or equal to 1 or if hourse were shown
        if (minutes > 0 || (hours > 0 && minutes == 0)) {
            if (result.length() > 0) result.append(", ");
            result.append(minutes).append("mins");
        }

        // Onle show 's' if seconds is greater than 0 or if nothing else was shown
        if (secs > 0 || result.length() == 0) {
            if (result.length() > 0) result.append(", ");
            result.append(secs).append("s");
        }

        return result.toString();
    }

    // Format bytes to human-readable format
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), unit);
    }

    // Format bandwidth to human-readable format
    private String formatBandwidth(double bytesPerSec) {
        String[] units = {"B/s", "KB/s", "MB/s", "GB/s"};
        int unitIndex = 0;
        while (bytesPerSec >= 1024 && unitIndex < units.length - 1) {
            bytesPerSec /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", bytesPerSec, units[unitIndex]);
    }
    
}
