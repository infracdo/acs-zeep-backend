package com.acs_tr069.test_tr069.radius.service;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.acs_tr069.test_tr069.radius.entity.Accounting;
import com.acs_tr069.test_tr069.radius.repository.AccountingRepository;
import com.acs_tr069.test_tr069.radius.repository.SubscriberRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RadiusService {

    private final SubscriberRepository subscriberRepository;

    private final AccountingRepository accountingRepository;

    public RadiusService(AccountingRepository accountingRepository, SubscriberRepository subscriberRepository) {
        this.accountingRepository = accountingRepository;
        this.subscriberRepository = subscriberRepository;
    }


    // Return number of currently connected users
    public Long getCountOnlineUsers() {
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

        return accountingRepository.countOnlineUsers();
    }

    // Return number of active users
    public Long getCountActiveUsers(String timeframe) {
        long startTime = getTimestampsForTimeframe(timeframe);
        return accountingRepository.countActiveUsers(startTime);
    }

    // Return number of inactive users
    public Long getCountInactiveUsers(String timeframe) {
        long startTime = getTimestampsForTimeframe(timeframe);
        return accountingRepository.countInactiveUsers(startTime);
    }

    // Return number of registered users
    public Long getCountRegisteredUsers() {
        return subscriberRepository.countRegisteredUsers();
    }

    // Return number of total users
    public Long getCountTotalAPs() {
        return accountingRepository.countTotalAPs();
    }

    // Return number of current access points in use
    public Long getCountOnlineAPs() {
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
        return accountingRepository.countOnlineAPs();
    }

    // Return number of currently active access points in the past x days
    public Long getCountActiveAPs(String timeframe) {
        long startTime = getTimestampsForTimeframe(timeframe);
        return accountingRepository.countActiveAPs(startTime);
    }

    // Return number of currently inactive access points
    public Long getCountInactiveAPs(String timeframe) {
        long startTime = getTimestampsForTimeframe(timeframe);
        return accountingRepository.countInactiveAPs(startTime);
    }

    // Return the total number of user connections for today
    public Long getCountTotalUserConnectionsToday() {
        long startOfDay = getTimestampsForTimeframe("today");
        return accountingRepository.countTotalUserConnectionsToday(startOfDay);
    }

    // Return the total number of user connections for today
    public Long getCountTotalSessionsToday() {
        long startOfDay = getTimestampsForTimeframe("today");
        return accountingRepository.countTotalSessionsToday(startOfDay);
    }

    // Return total bandwidth consumption for today
    public String getTotalBandwidthConsumptionToday() {
        long startOfDay = getTimestampsForTimeframe("today");
        long totalRawBytes = accountingRepository.totalBandwidthConsumptionToday(startOfDay);
        return formatBytes(totalRawBytes);
    }

    // Return total session time for today
    public String getTotalSessionTimeToday() {
        long startOfDay = getTimestampsForTimeframe("today");
        Double totalSeconds = accountingRepository.totalSessionTimeToday(startOfDay);
        return formatDuration(totalSeconds);
    }

    // Return average connection time
    public String getAverageConnectionTime() {
        Double avgSeconds = accountingRepository.findAverageConnectionTime();
        return formatDuration(avgSeconds);
    }

    // Return average connection time for the month
    public String getAverageConnectionTimeForMonth() {
        long startOfMonth = getTimestampsForTimeframe("month");
        Double avgSeconds = accountingRepository.findAverageConnectionTime(startOfMonth);
        
        if (avgSeconds == null) {
            return "-mins, -s";
        }
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

    // Return average bandwidth per connection for the month
    public String getAverageBandwidthForMonth() {
        long startOfMonth = getTimestampsForTimeframe("month");
        Double avgBytesPerSec = accountingRepository.findAverageBandwidthPerConnection(startOfMonth);

        if (avgBytesPerSec == null) return "- B/s";

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
            userDetails.put("acctinputoctets", row[2] != null ? row[2] : 0);
            userDetails.put("acctoutputoctets", row[3] != null ? row[3] : 0);
            userDetails.put("nasport", row[4]);
            userDetails.put("calling_station_id", row[5]);
            userDetails.put("timestamp", row[6]);
            response.computeIfAbsent(called_station_id, k -> new java.util.ArrayList<>()).add(userDetails);
        }

        return response;
    }

    public List<Map<String, Object>> getAllCurrentOnlineUsers() {
        List<Object[]> results = accountingRepository.findAllCurrentOnlineUsers();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            user.put("username", row[0]);

            user.put("total_active_session_count", row[1] != null ? ((Number) row[1]).intValue() : 0);

            Long totalSessionSeconds = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            user.put("total_session_duration", formatDuration(totalSessionSeconds.doubleValue()));

            Long bandwidthUsage = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            user.put("totalBandwidthUsage", formatBandwidth(bandwidthUsage));

            response.add(user);
        }
        
        // log.info("getAllCurrentOnlineUsers: {}", response);
        return response;
    }
    
    public long getCountForAllCurrentOnlineUsers() {
        return accountingRepository.countAllCurrentOnlineUsers();
    }
    
    public List<Map<String, Object>> getAllSessionsByUsernameForCurrentOnlineUsers(String username) {
        List<Object[]> results = accountingRepository.findAllSessionsByUsernameForCurrentOnlineUsers(username);
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            user.put("acctSessionId", row[0]);
            user.put("username", row[1]);
            user.put("callingStationId", row[2]);
            user.put("calledStationId", row[3]);
            
            Timestamp startTime = (Timestamp) row[4];
            if (startTime != null) {
                long startSeconds = startTime.getTime() / 1000;
                user.put("startTime", formatRawTimeStamp(startSeconds));
                
                long now = System.currentTimeMillis() / 1000;
                long diffSeconds = now - startSeconds;
                user.put("duration", formatDuration((double) diffSeconds));
            } else {
                user.put("startTime", "-");
                user.put("duration", "-");
            }

            Long inputOctets = row[6] != null ? ((Number) row[6]).longValue() : 0;
            Long outputOctets = row[7] != null ? ((Number) row[7]).longValue() : 0;
            Long bandwidthUsage = inputOctets + outputOctets;
            user.put("bandwidthUsage", formatBandwidth(bandwidthUsage));
            
            response.add(user);
        }
        
        // log.info("getAllSessionsByUsernameForCurrentOnlineUsers: {}", response);
        return response;
    }

    public List<Map<String, Object>> getAllActiveUsersForThePast7Days() {
        List<Object[]> results = accountingRepository.findAllActiveUsersForThePast7Days();
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            user.put("username", row[0]);
            
            user.put("sessionCount", row[1] != null ? ((Number) row[1]).intValue() : 0);
            
            Long totalDurationInSeconds = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            user.put("totalTime", formatDuration(totalDurationInSeconds.doubleValue()));
            
            Long bandwidthUsage = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            user.put("totalBandwidthUsage", formatBandwidth(bandwidthUsage));
            
            Long avgSessionLengthInSeconds = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            user.put("avgSessionLength", formatDuration(avgSessionLengthInSeconds.doubleValue()));
            
            response.add(user);
        }
        
        // log.info("getAllActiveUsersForThePast7Days: {}", response);
        return response;
    }
    
    public long getCountForAllActiveUsersForThePast7Days() {
        return accountingRepository.countForAllActiveUsersForThePast7Days();
    }
    
    public List<Map<String, Object>> getAllSessionsByUsernameForThePast7Days(String username) {
        List<Object[]> results = accountingRepository.findAllSessionsByUsernameForThePast7Days(username);
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            // user.put("username", row[0]);
            
            user.put("acctSessionId", row[1]);
            user.put("callingStationId", row[2]);
            user.put("calledStationId", row[3]);
            

            Timestamp startTime = (Timestamp) row[4];
            if (startTime != null) {
                String formattedStartTime = formatRawTimeStamp(startTime.getTime() / 1000);
                user.put("startTime", formattedStartTime);
                
                Long totalDurationInSeconds = row[6] != null ? ((Number) row[6]).longValue() : 0L;

                user.put("duration", formatDuration((double) totalDurationInSeconds));
            } else {
                user.put("startTime", "-");
                user.put("duration", "-");
            }
            Long bandwidth = row[7] != null ? ((Number) row[7]).longValue() : 0L;
            user.put("bandwidthUsage", formatBandwidth(bandwidth));
            
            response.add(user);
        }
        
        // log.info("getAllSessionsByUsernameForThePast7Days: {}", response);
        return response;
    }
    
    public List<Map<String, Object>> getAllRegisteredUsersWithSessions() {
        List<Object[]> results = accountingRepository.findAllRegisteredUsersWithSessions();
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            user.put("username", row[0]);
            
            user.put("sessionCount", row[1] != null ? ((Number) row[1]).intValue() : 0);
            
            Long totalDurationInSeconds = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            user.put("totalTime", formatDuration(totalDurationInSeconds.doubleValue()));
            
            Long bandwidthUsage = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            user.put("totalBandwidthUsage", formatBandwidth(bandwidthUsage));
            
            Long avgSessionLengthInSeconds = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            user.put("avgSessionLength", formatDuration(avgSessionLengthInSeconds.doubleValue()));
            
            response.add(user);
        }
        
        // log.info("getAllRegisteredUsersWithSessions: {}", response);
        return response;
    }
    
    public long getCountForAllRegisteredUsersWithSessions() {
        return accountingRepository.countAllRegisteredUsersWithSessions();
    }
    
    public List<Map<String, Object>> getAllSessionsByUsername(String username) {
        List<Object[]> results = accountingRepository.findAllSessionsByUsername(username);
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            // user.put("username", row[0]);
            
            user.put("acctSessionId", row[1]);
            user.put("callingStationId", row[2]);
            user.put("calledStationId", row[3]);
            

            Timestamp startTime = (Timestamp) row[4];
            if (startTime != null) {
                String formattedStartTime = formatRawTimeStamp(startTime.getTime() / 1000);
                user.put("startTime", formattedStartTime);
                Long totalDurationInSeconds = row[6] != null ? ((Number) row[6]).longValue() : 0L;

                user.put("duration", formatDuration((double) totalDurationInSeconds));
            } else {
                user.put("startTime", "-");
                user.put("duration", "-");
            }
            Long bandwidth = row[7] != null ? ((Number) row[7]).longValue() : 0L;
            user.put("bandwidthUsage", formatBandwidth(bandwidth));
            
            response.add(user);
        }
        
        // log.info("getAllSessionsByUsername: {}", response);
        return response;
    }

    public long getCountForAllCurrentOnlineApForThePast30Mins() {
        return accountingRepository.countAllCurrentOnlineApForThePast30Mins();
    }

    public List<Map<String, Object>> getAllCurrentOnlineApForThePast30Mins() {
        List<Object[]> results = accountingRepository.findAllCurrentOnlineApForThePast30Mins();
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            
            user.put("calledStationId", row[0]);
            user.put("totalSessions", row[1]);
            Long bandwidth = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            user.put("totalBandwidth", formatBandwidth(bandwidth));
            
            Long avgSessionDurationInSeconds = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            user.put("avgSessionDuration", formatDuration((double) avgSessionDurationInSeconds));
                        
            Timestamp ts = (Timestamp) row[4];
            LocalDateTime local = LocalDateTime.ofInstant(ts.toInstant(), ZoneId.of("Asia/Manila"));
            String start = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime plusOne = local.plusHours(1);
            String end = plusOne.format(DateTimeFormatter.ofPattern("HH:mm"));
            String formatted = start + " - " + end;
            user.put("peakHour", formatted);
            
            response.add(user);
        }
        
        // log.info("getAllCurrentOnlineApForTheLast30Mins: {}", response);
        return response;
    }

    public List<Map<String, Object>> getCurrentOnlineApForThePast30MinsByApId(String apId) {
        List<Object[]> results = accountingRepository.findCurrentOnlineApForThePast30MinsByApId(apId);
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            
            // user.put("calledStationId", row[0]);
            user.put("userName", row[1]);
            user.put("totalSessions", row[2]);

            Long totalTimeInSeconds = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            user.put("totalTime", formatDuration((double) totalTimeInSeconds));

            Long bandwidth = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            user.put("totalBandwidth", formatBandwidth(bandwidth));
            
            Long avgSessionLengthInSeconds = row[5] != null ? ((Number) row[5]).longValue() : 0L;
            user.put("avgSessionLength", formatDuration((double) avgSessionLengthInSeconds));
            
            response.add(user);
        }
        
        // log.info("getAllCurrentOnlineApForTheLast30Mins: {}", response);
        return response;
    }

    public List<Map<String, Object>> getSessionForCurrentOnlineUsersByUsernameAndApId(String apId, String username) {
        List<Object[]> results = accountingRepository.findSessionForCurrentOnlineUsersByUsernameAndApId(apId, username);
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            // user.put("username", row[0]);
            
            user.put("acctSessionId", row[1]);
            user.put("callingStationId", row[2]);
            user.put("calledStationId", row[3]);
            

            Timestamp startTime = (Timestamp) row[4];
            if (startTime != null) {
                String formattedStartTime = formatRawTimeStamp(startTime.getTime() / 1000);
                user.put("startTime", formattedStartTime);
                Long totalDurationInSeconds = row[6] != null ? ((Number) row[6]).longValue() : 0L;

                user.put("duration", formatDuration((double) totalDurationInSeconds));
            } else {
                user.put("startTime", "-");
                user.put("duration", "-");
            }
            Long bandwidth = row[7] != null ? ((Number) row[7]).longValue() : 0L;
            user.put("bandwidthUsage", formatBandwidth(bandwidth));
            
            response.add(user);
        }
        
        // log.info("getSessionForCurrentOnlineUsersByUsernameAndApId: {}", response);
        return response;
    }

    public long getCountForAllActiveApForThePast7Days() {
        return accountingRepository.countAllActiveApForThePast7Days();
    }

    public List<Map<String, Object>> getAllActiveApForThePast7Days() {
        List<Object[]> results = accountingRepository.findAllActiveApForThePast7Days();
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            
            user.put("calledStationId", row[0]);
            user.put("totalSessions", row[1]);
            Long bandwidth = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            user.put("totalBandwidth", formatBandwidth(bandwidth));
            
            Long avgSessionDurationInSeconds = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            user.put("avgSessionDuration", formatDuration((double) avgSessionDurationInSeconds));

            Timestamp ts = (Timestamp) row[4];
            LocalDateTime local = LocalDateTime.ofInstant(ts.toInstant(), ZoneId.of("Asia/Manila"));
            String start = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime plusOne = local.plusHours(1);
            String end = plusOne.format(DateTimeFormatter.ofPattern("HH:mm"));
            String formatted = start + " - " + end;
            user.put("peakHour", formatted);

            response.add(user);
        }
        
        // log.info("getAllActiveApForTheLast7Days: {}", response);
        return response;
    }
    
    public List<Map<String, Object>> getAllActiveApForThePast7DaysByApId(String apId) {
        List<Object[]> results = accountingRepository.findAllActiveApForThePast7DaysByApId(apId);
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            
            // user.put("calledStationId", row[0]);
            user.put("userName", row[1]);
            user.put("totalSessions", row[2]);
            
            Long totalTimeInSeconds = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            user.put("totalTime", formatDuration((double) totalTimeInSeconds));

            Long bandwidth = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            user.put("totalBandwidth", formatBandwidth(bandwidth));
            
            Long avgSessionLengthInSeconds = row[5] != null ? ((Number) row[5]).longValue() : 0L;
            user.put("avgSessionLength", formatDuration((double) avgSessionLengthInSeconds));

            response.add(user);
        }
        
        // log.info("getAllActiveApForThePast7DaysByApId: {}", response);
        return response;
    }
    
    public long getCountForAllInActiveApForMoreThan7Days() {
        return accountingRepository.countAllInActiveApForMoreThan7Days();
    }

    public List<Map<String, Object>> getAllInActiveApForMoreThan7Days() {
        List<Object[]> results = accountingRepository.findAllInActiveApForMoreThan7Days();
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            
            user.put("calledStationId", row[0]);
            user.put("totalSessions", row[1]);
            Long bandwidth = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            user.put("totalBandwidth", formatBandwidth(bandwidth));
            
            Long avgSessionDurationInSeconds = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            user.put("avgSessionDuration", formatDuration((double) avgSessionDurationInSeconds));

            Timestamp ts = (Timestamp) row[4];
            LocalDateTime local = LocalDateTime.ofInstant(ts.toInstant(), ZoneId.of("Asia/Manila"));
            String start = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime plusOne = local.plusHours(1);
            String end = plusOne.format(DateTimeFormatter.ofPattern("HH:mm"));
            String formatted = start + " - " + end;
            user.put("peakHour", formatted);

            response.add(user);
        }
        
        // log.info("getAllInactiveApForMoreThan7Days: {}", response);
        return response;
    }

    public List<Map<String, Object>> getAllInActiveApForThePast7DaysByApId(String apId) {
        List<Object[]> results = accountingRepository.findAllInActiveApForThePast7DaysByApId(apId);
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            
            // user.put("calledStationId", row[0]);
            user.put("userName", row[1]);
            user.put("totalSessions", row[2]);
            
            Long totalTimeInSeconds = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            user.put("totalTime", formatDuration((double) totalTimeInSeconds));

            Long bandwidth = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            user.put("totalBandwidth", formatBandwidth(bandwidth));
            
            Long avgSessionLengthInSeconds = row[5] != null ? ((Number) row[5]).longValue() : 0L;
            user.put("avgSessionLength", formatDuration((double) avgSessionLengthInSeconds));

            response.add(user);
        }
        
        // log.info("getAllInActiveApForTheLast7DaysByApId: {}", response);
        return response;
    }
    
    // HELPER METHODS //
    private long getTimestampsForTimeframe(String timeframe) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC); // System time assumed to be UTC
        System.out.println("Current system time (UTC): " + now);
        LocalDateTime start;

        switch (timeframe.toLowerCase()) {
            case "today":
                start = now.toLocalDate().atStartOfDay();
                break;
            case "yesterday":
                start = now.minusDays(1).toLocalDate().atStartOfDay();
                break;
            case "week": // ISO standard: week starts on Monday
                start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
                break;
            case "month":
                start = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
                break;
            case "7d":
                start = now.minusDays(6).toLocalDate().atStartOfDay();
                break;
            case "30d":
                start = now.minusDays(29).toLocalDate().atStartOfDay();
                break;
            default:
                return now.minusHours(24).toEpochSecond(ZoneOffset.UTC);
        }

        long startEpoch = start.toEpochSecond(ZoneOffset.UTC);
        System.out.println("Generated UTC epoch for \"" + timeframe + "\": " + startEpoch);
        return startEpoch;
    }

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

    private String formatRawTimeStamp(long epochSeconds){
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
