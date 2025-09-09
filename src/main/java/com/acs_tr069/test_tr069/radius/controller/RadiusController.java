package com.acs_tr069.test_tr069.radius.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acs_tr069.test_tr069.Entity.device;
import com.acs_tr069.test_tr069.Repo.device_frontendRepository;
import com.acs_tr069.test_tr069.radius.service.RadiusService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/radius/")
@CrossOrigin("*")
public class RadiusController {

    @Autowired
    private RadiusService radiusService;
    
    @Autowired
    private device_frontendRepository deviceRepo;

    // returns number of users currently connected to APs
    @GetMapping("count-online-users")
    public ResponseEntity<Map<String, Object>> getCountCurrentOnlineUsers() {
        long currentOnlineUsers = radiusService.getCountOnlineUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("countOnlineUsers", currentOnlineUsers);

        log.info("Current Online Users: " + currentOnlineUsers);
        return ResponseEntity.ok(response);
    }

    // returns number of active users
    @GetMapping("count-active-users")
    public ResponseEntity<Map<String, Object>> getCountActiveUsers(@RequestParam(required = false, defaultValue = "7d") String timeframe) {
        long activeUsers = radiusService.getCountActiveUsers(timeframe);

        Map<String, Object> response = new HashMap<>();
        response.put("countActiveUsers", activeUsers);

        return ResponseEntity.ok(response);
    }

    // Get number of active users
    @GetMapping("count-inactive-users")
    public ResponseEntity<Map<String, Object>> getCountInactiveUsers(@RequestParam(required = false, defaultValue = "7d") String timeframe) {
        long inactiveUsers = radiusService.getCountInactiveUsers(timeframe);

        Map<String, Object> response = new HashMap<>();
        response.put("countInactiveUsers", inactiveUsers);

        return ResponseEntity.ok(response);
    }

    // Get number of registered users
    @GetMapping("count-registered-users")
    public ResponseEntity<Map<String, Object>> getCountRegisteredUsers() {
        long registeredUsers = radiusService.getCountRegisteredUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("countRegisteredUsers", registeredUsers);

        return ResponseEntity.ok(response);
    }
    
    // Get number of total aps
    @GetMapping("count-total-aps")
    public ResponseEntity<Map<String, Object>> getCountTotalAPs() {
        long totalAPs = radiusService.getCountTotalAPs();

        Map<String, Object> response = new HashMap<>();
        response.put("countTotalAPs", totalAPs);

        return ResponseEntity.ok(response);
    }

    // Get number of current online access points
    // @PreAuthorize("hasRole('ROLE_API_ACCESS')")
    @GetMapping("count-online-aps")
    public ResponseEntity<Map<String, Object>> getCountOnlineAPs() {
        long currentlyOnlineAPs = radiusService.getCountOnlineAPs();

        Map<String, Object> response = new HashMap<>();
        response.put("countOnlineAPs", currentlyOnlineAPs);

        return ResponseEntity.ok(response);
    }
    
    // Get number of current active access points
    @GetMapping("count-active-aps")
    public ResponseEntity<Map<String, Object>> getCountActiveAPs(@RequestParam(required = false, defaultValue = "7d") String timeframe) {
        long currentlyActiveAPs = radiusService.getCountActiveAPs(timeframe);

        Map<String, Object> response = new HashMap<>();
        response.put("countActiveAPs", currentlyActiveAPs);

        return ResponseEntity.ok(response);
    }
    
    // Get number of current inactive access points
    @GetMapping("count-inactive-aps")
    public ResponseEntity<Map<String, Object>> getCountInactiveAPs(@RequestParam(required = false, defaultValue = "7d") String timeframe) {
        long currentlyInactiveAPs = radiusService.getCountInactiveAPs(timeframe);

        Map<String, Object> response = new HashMap<>();
        response.put("countInactiveAPs", currentlyInactiveAPs);

        return ResponseEntity.ok(response);
    }

    // Get total number of user connections for today
    @GetMapping("total-user-connections-today")
    public ResponseEntity<Map<String, Object>> getTotalUserConnectionsToday() {
        long totalUserConnections = radiusService.getCountTotalUserConnectionsToday();

        Map<String, Object> response = new HashMap<>();
        response.put("totalUserConnectionsToday", totalUserConnections);

        return ResponseEntity.ok(response);
    }

    // Get total number of user sessions for today
    @GetMapping("total-user-sessions-today")
    public ResponseEntity<Map<String, Object>> getTotalUserSessionsToday() {
        long totalUserSessions = radiusService.getCountTotalSessionsToday();

        Map<String, Object> response = new HashMap<>();
        response.put("totalUserSessionsToday", totalUserSessions);

        return ResponseEntity.ok(response);
    }

    // Get total bandwidth consumption for today
    @GetMapping("total-bandwidth-consumption-today")
    public ResponseEntity<Map<String, Object>> getTotalBandwidthConsumptionToday() {
        String formattedTotalBandwidth = radiusService.getTotalBandwidthConsumptionToday();
        Map<String, Object> response = new HashMap<>();
        response.put("totalBandwidthConsumptionToday", formattedTotalBandwidth);

        return ResponseEntity.ok(response);
    }

    
    // Get total session time for today
    @GetMapping("total-session-time-today")
    public ResponseEntity<Map<String, Object>> getTotalSessionTimeToday() {
        String formattedTotalTime = radiusService.getTotalSessionTimeToday();

        Map<String, Object> response = new HashMap<>();
        response.put("totalSessionTimeToday", formattedTotalTime);

        return ResponseEntity.ok(response);
    }

    // Get average connection time
    @GetMapping("average-connection-time")
    public ResponseEntity<Map<String, String>> getAvgConnectionTime() {
        String formattedTime = radiusService.getAverageConnectionTime();
        return ResponseEntity.ok(Collections.singletonMap("averageConnectionTime", formattedTime));
    }

    // Get average connection time for the month
    @GetMapping("average-connection-time-for-month")
    public ResponseEntity<Map<String, Object>> getAverageConnectionTimeForMonth() {
        String formattedTime = radiusService.getAverageConnectionTimeForMonth();
        return ResponseEntity.ok(Collections.singletonMap("averageConnectionTimeForMonth", formattedTime));
    }

    // Get average bandwidth per connection
    @GetMapping("average-bandwidth-per-connection")
    public ResponseEntity<Map<String, Object>> getAverageBandwidthPerConnection() {
        String avgBandwidth = radiusService.getAverageBandwidthPerConnection();

        Map<String, Object> response = new HashMap<>();
        response.put("averageBandwidthPerConnection", avgBandwidth);

        return ResponseEntity.ok(response);
    }

    // Get average bandwidth per connection for the month
    @GetMapping("average-bandwidth-for-month")
    public ResponseEntity<Map<String, Object>> getAverageBandwidthForMonth() {
        String avgBandwidth = radiusService.getAverageBandwidthForMonth();

        Map<String, Object> response = new HashMap<>();
        response.put("averageBandwidthForMonth", avgBandwidth);

        return ResponseEntity.ok(response);
    }

    // Get access point device name
    @GetMapping("ap-device-name")
    public ResponseEntity<String> getAPDeviceName(@RequestParam String mac) {
        String deviceName = deviceRepo.getDeviceNameByMac(mac);
        return ResponseEntity.ok(deviceName);
    }

    // Get list of access points
    @GetMapping("access-points")
    public ResponseEntity<Map<String, Object>> getAllAccessPoints() {
        List<String> accessPoints = radiusService.getAllAccessPoints();

        Map<String, Object> response = new HashMap<>();
        response.put("accessPoints", accessPoints);
        return ResponseEntity.ok(response);
    }

    // Get list of online access points info
    @GetMapping("access-points-online")
    public ResponseEntity<List<device>> getAllOnlineAP() {
        return ResponseEntity.ok(deviceRepo.getDevicesByParentAndStatus("", "online"));
    }

    // Get list of offline access points info
    @GetMapping("access-points-offline")
    public ResponseEntity<List<device>> getAllOfflineAP() {
        return ResponseEntity.ok(deviceRepo.getDevicesByParentAndStatus("", "offline"));
    }

    // Get list of zeep access points info
    @GetMapping("access-points-zeep")
    public ResponseEntity<List<device>> getAllZeepAP() {
        return ResponseEntity.ok(deviceRepo.getAllDevicesByParent("zeep"));
    }

    // Get list of registered access points info
    @GetMapping("access-points-registered")
    public ResponseEntity<List<device>> getAllRegisteredAP() {
        return ResponseEntity.ok(deviceRepo.getAllOnlineRegisteredDevices());
    }

    // Get list of rogue access points info
    @GetMapping("access-points-rogue")
    public ResponseEntity<List<device>> getAllRogueAP() {
        return ResponseEntity.ok(deviceRepo.getAllDevicesByParent("unassigned"));
    }

    // Get number of currently connected users per access point
    @GetMapping("count-currently-connected-users-per-ap")
    public ResponseEntity<List<Map<String, Object>>> getCountCurrentlyConnectedUsersPerAP() {
        Map<String, Long> currentlyConnectedUsers = radiusService.getCountCurrentlyConnectedUsersPerAP();

        List<Map<String, Object>> response = currentlyConnectedUsers.entrySet().stream()
            .map(entry -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("calledStationId", entry.getKey());
                map.put("currentlyConnectedUsers", entry.getValue());
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Get list of currently connected users per access point
    @GetMapping("currently-connected-users-per-ap")
    public ResponseEntity<List<Map<String, Object>>> getCurrentlyConnectedUsersPerAP() {
        List<Map<String, Object>> usersPerAP = radiusService.getCurrentlyConnectedUsersPerAP()
            .entrySet()
            .stream()
            .map(entry -> {
                Map<String, Object> apEntry = new LinkedHashMap<>();
                apEntry.put("called_station_id", entry.getKey());
                apEntry.put("currently_connected_users", entry.getValue());
                return apEntry;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(usersPerAP);
    }

    // Get currently connected Users who are active for the past 30mins
    @GetMapping("online-users")
    public ResponseEntity<Map<String, Object>> getAllCurrentOnlineUsers(@RequestParam int limit, @RequestParam int offset) {
        // log.info("Fetching current online user details with limit: {} and offset: {}", limit, offset);
        List<Map<String, Object>> currentOnlineUsers = radiusService.getAllCurrentOnlineUsers(limit, offset);
        
        Map<String, Object> response = new HashMap<>();
        response.put("currentOnlineUsers", currentOnlineUsers);
        
        return ResponseEntity.ok(response);
    }
    
    // Get All of the User's Sessions Details for the past 30mins
    @GetMapping("online-users/userSession")
    public ResponseEntity<Map<String, Object>> getUserSessions(@RequestParam String username) {
        // log.info("Fetching user session details for username: {}", username);
        List<Map<String, Object>> userSessions = radiusService.getUserSessions(username);

        Map<String, Object> response = new HashMap<>();
        response.put("userSessions", userSessions);
        
        return ResponseEntity.ok(response);
    }
    
    // count all current online users for the past 30mins
    @GetMapping("online-users/countAll")
    public ResponseEntity<Map<String, Object>> getCountForAllCurrentOnlineUsers() {
        long totalCount = radiusService.getCountForAllCurrentOnlineUsers();
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", totalCount);
        return ResponseEntity.ok(response);
    }
    
    // Get All Active Users with a session for the past 7 days
    @GetMapping("active-users")
    public ResponseEntity<Map<String, Object>> getAllActiveUsersForThePast7Days(@RequestParam int limit, @RequestParam int offset) {
        List<Map<String, Object>> activeUsersForThePast7days = radiusService.getAllActiveUsersForThePast7Days(limit, offset);
        Map<String, Object> response = new HashMap<>();
        response.put("activeUsersForThePast7days", activeUsersForThePast7days);
        
        return ResponseEntity.ok(response);
    }
    
    // count All Active Users with a session for the past 7 days
    @GetMapping("active-users/countAll")
    public ResponseEntity<Map<String, Object>> getCountForAllActiveUsersForThePast7Days() {
        long totalCount = radiusService.getCountForAllActiveUsersForThePast7Days();
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", totalCount);
        return ResponseEntity.ok(response);
    }
    
    // gett All of the Users Session Details for the past 7 days  - the user must have a session for the past 7days
    @GetMapping("active-users/userSessions")
    public ResponseEntity<Map<String, Object>> getAllSessionsByUsernameForLast7Days(@RequestParam String username, @RequestParam int limit, @RequestParam int offset) {
        List<Map<String, Object>> userSessions = radiusService.getAllSessionsByUsernameForLast7Days(username, limit, offset);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userSessions", userSessions);
        
        return ResponseEntity.ok(response);
    }
    
    // Get All Registered Users with a session
    @GetMapping("all-users-with-sessions")
    public ResponseEntity<Map<String, Object>> getAllRegisteredUsersWithSessions(@RequestParam int limit, @RequestParam int offset) {
        List<Map<String, Object>> allRegisteredUsersWithSessions = radiusService.getAllRegisteredUsersWithSessions(limit, offset);
        Map<String, Object> response = new HashMap<>();
        response.put("allRegisteredUsersWithSessions", allRegisteredUsersWithSessions);
        
        return ResponseEntity.ok(response);
    }
    
    // count All Registered Users with a session
    @GetMapping("all-users-with-sessions/countAll")
    public ResponseEntity<Map<String, Object>> getCountForAllRegisteredUsersWithSessions() {
        long totalCount = radiusService.getCountForAllRegisteredUsersWithSessions();
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", totalCount);
        return ResponseEntity.ok(response);
    }
    
    // get the Registered Users Session Details
    @GetMapping("all-users-with-sessions/userSessions")
    public ResponseEntity<Map<String, Object>> getAllSessionsByUsername(@RequestParam String username, @RequestParam int limit, @RequestParam int offset) {
        List<Map<String, Object>> userSessions = radiusService.getAllSessionsByUsername(username, limit, offset);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userSessions", userSessions);
        
        return ResponseEntity.ok(response);
    }
}
