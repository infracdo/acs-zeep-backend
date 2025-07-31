package com.acs_tr069.test_tr069.radius.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.acs_tr069.test_tr069.radius.entity.Accounting;
import com.acs_tr069.test_tr069.radius.repository.AccountingRepository;

@Service
public class RadiusService {

    private final AccountingRepository accountingRepository;

    public RadiusService(AccountingRepository accountingRepository) {
        this.accountingRepository = accountingRepository;
    }


    // Return number of currently connected users
    public Long getCountCurrentlyConnectedUsers() {
        // -- Uncomment if you need to get the currently connected users for today
        // long startOfDay = LocalDate.now()
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);

        // NOTE: for testing purposes
        // long startOfDay = LocalDate.of(2025, 5, 19)
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);

        // long endOfDay = startOfDay + 86400;

        // return accountingRepository.countCurrentlyConnectedUsers(startOfDay, endOfDay);
        // Uncomment if you need to get the currently connected users for today --

        return accountingRepository.countCurrentlyConnectedUsers();
    }

    // Return number of currently connected access points
    public Long getCountCurrentlyConnectedAPs() {
        // -- Uncomment if you need to get the currently connected users for today
        // long startOfDay = LocalDate.now()
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);

        // NOTE: for testing purposes
        // long startOfDay = LocalDate.of(2025, 5, 16)
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);

        // long endOfDay = startOfDay + 86400;

        // return accountingRepository.countCurrentlyConnectedAPs(startOfDay, endOfDay);
        // Uncomment if you need to get the currently connected users for today --
        return accountingRepository.countCurrentlyConnectedAPs();
    }

    // Return the total number of user connections for today
    public Long getCountTotalUserConnectionsToday() {
        long startOfDay = LocalDate.now()
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC);

        // NOTE: for testing purposes
        // long startOfDay = LocalDate.of(2025, 5, 16)
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);

        long endOfDay = startOfDay + 86400;

        return accountingRepository.countTotalUserConnectionsToday(startOfDay, endOfDay);
    }

    // Return total bandwidth consumption for today
    public String getTotalBandwidthConsumptionToday() {
        long startOfDay = LocalDate.now()
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC);

        // NOTE: for testing purposes
        // long startOfDay = LocalDate.of(2025, 5, 16)
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);

        long endOfDay = startOfDay + 86400;

        long totalRawBytes = accountingRepository.totalBandwidthConsumptionToday(startOfDay, endOfDay);

        return formatBytes(totalRawBytes);
    }

    // Return average connection time
    public String getAverageConnectionTime() {
        Double avgSeconds = accountingRepository.findAverageConnectionTime();
        return formatDuration(avgSeconds);
    }

    // Return average bandwidth per connection
    public String getAverageBandwidthPerConnection() {
        // -- Uncomment if you need to get the currently connected users for today
        // long startOfDay = LocalDate.now()
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);

        // NOTE: for testing purposes
        // long startOfDay = LocalDate.of(2025, 5, 16)
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);

        // long endOfDay = startOfDay + 86400;
        // Uncomment if you need to get the currently connected users for today --

        Double avgBytesPerSec = accountingRepository.findAverageBandwidthPerConnection();

        if (avgBytesPerSec == null || avgBytesPerSec <= 0) return "0 B/s";

        return formatBandwidth(avgBytesPerSec);
    }

    // Return list of access points
    public List<String> getAllAccessPoints() {
        return accountingRepository.findAllAccessPoints();
    }
    
    // Return list of access points
    public List<Accounting> getAllAPInfo() {
        return accountingRepository.findAllAccessPointsInfo();
    }

    // Return currently connected users per access point
    public Map<String, Long> getCountCurrentlyConnectedUsersPerAP() {
        // -- Uncomment if you need to get the currently connected users for today
        // long startOfDay = LocalDate.now()
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);

        // NOTE: for testing purposes
        // long startOfDay = LocalDate.of(2025, 5, 16)
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);

        // long endOfDay = startOfDay + 86400;

        // List<Object[]> currentlyConnectedUsers = accountingRepository.countCurrentlyConnectedUsersPerAP(startOfDay, endOfDay);
        // Uncomment if you need to get the currently connected users for today --

        List<Object[]> currentlyConnectedUsers = accountingRepository.countCurrentlyConnectedUsersPerAP();
        Map<String, Long> response = new HashMap<>();

        for (Object[] row : currentlyConnectedUsers) {
            String calledStationId = (String) row[0];
            Number count = (Number) row[1];
            response.put(calledStationId, count.longValue());
        }

        return response;
    }

    // Return list of currently connected users per access point
    public Map<String, List<Map<String, Object>>> getCurrentlyConnectedUsersPerAP() {
        // -- Uncomment if you need to get the currently connected users for today
        // long startOfDay = LocalDate.now()
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);
        
        // NOTE: for testing purposes
        // long startOfDay = LocalDate.of(2025, 5, 16)
        //     .atStartOfDay()
        //     .toEpochSecond(ZoneOffset.UTC);

        // long endOfDay = startOfDay + 86400;

        // List<Object[]> currentlyConnectedUsers = accountingRepository.findCurrentlyConnectedUsersPerAP(startOfDay, endOfDay);
        // Uncomment if you need to get the currently connected users for today --
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
    }


    // HELPER METHODS //

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

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), unit);
    }

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
