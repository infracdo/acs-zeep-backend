package com.acs_tr069.test_tr069.radius.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acs_tr069.test_tr069.Entity.device;
import com.acs_tr069.test_tr069.Repo.device_frontendRepository;
import com.acs_tr069.test_tr069.radius.service.RadiusService;

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

        return ResponseEntity.ok(response);
    }

    // returns number of active users
    @GetMapping("count-active-users")
    public ResponseEntity<Map<String, Object>> getCountActiveUsers(@RequestParam(required = false, defaultValue = "week") String timeframe) {
        long activeUsers = radiusService.getCountActiveUsers(timeframe);

        Map<String, Object> response = new HashMap<>();
        response.put("countActiveUsers", activeUsers);

        return ResponseEntity.ok(response);
    }

    // Get number of active users
    @GetMapping("count-inactive-users")
    public ResponseEntity<Map<String, Object>> getCountInactiveUsers(@RequestParam(required = false, defaultValue = "week") String timeframe) {
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
    public ResponseEntity<Map<String, Object>> getCountActiveAPs(@RequestParam(required = false, defaultValue = "week") String timeframe) {
        long currentlyActiveAPs = radiusService.getCountActiveAPs(timeframe);

        Map<String, Object> response = new HashMap<>();
        response.put("countActiveAPs", currentlyActiveAPs);

        return ResponseEntity.ok(response);
    }
    
    // Get number of current inactive access points
    @GetMapping("count-inactive-aps")
    public ResponseEntity<Map<String, Object>> getCountInactiveAPs(@RequestParam(required = false, defaultValue = "week") String timeframe) {
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
        return ResponseEntity.ok(deviceRepo.getDevicesByParentAndStatus("zeep", "online"));
    }

    // Get list of offline access points info
    @GetMapping("access-points-offline")
    public ResponseEntity<List<device>> getAllOfflineAP() {
        return ResponseEntity.ok(deviceRepo.getDevicesByParentAndStatus("zeep", "offline"));
    }

    // Get list of access points info
    @GetMapping("access-points-zeep")
    public ResponseEntity<List<device>> getAllAP() {
        return ResponseEntity.ok(deviceRepo.getAllDevicesByParent("zeep"));
    }

    // Get list of access points info
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
}
