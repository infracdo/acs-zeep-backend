package com.acs_tr069.test_tr069.radius.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.acs_tr069.test_tr069.radius.entity.Accounting;

@Repository
public interface AccountingRepository extends JpaRepository<Accounting, String> {

    // Query to get number of currently connected users
    // @Query(value = "SELECT COUNT(DISTINCT calling_station_id) " +
    //     "FROM accounting " +
    //     "WHERE acctstatustype = 'Start' " +
    //     "AND time_stamp >= :startOfDay " +
    //     "AND time_stamp < :endOfDay",
    //     nativeQuery = true)
    // long countCurrentlyConnectedUsers(
    //     @Param("startOfDay") long startOfDay,
    //     @Param("endOfDay") long endOfDay
    // );
    // NOTE: since we couldn't just query directly the users with acctstatustype of 'Start' as the table only inserts data (no updating nor removing a data),
    // we just have to make sure that the certain 'Start' session doesn't have a corresponding 'Stop' status type
    @Query(value = "SELECT COUNT(DISTINCT a1.calling_station_id) " +
        "FROM accounting a1 " +
        "WHERE a1.acctstatustype = 'Start' " +
        // "AND a1.time_stamp >= :startOfDay " +    // uncomment if you need to get the currently connected users for today
        // "AND a1.time_stamp < :endOfDay " +       // uncomment if you need to get the currently connected users for today
        "AND NOT EXISTS (" +
        "   SELECT 1 FROM accounting a2 " +
        "   WHERE a2.acctstatustype = 'Stop' " +
        "   AND a2.calling_station_id = a1.calling_station_id " +
        "   AND a2.time_stamp >= a1.time_stamp " +
        // "   AND a2.time_stamp < :endOfDay" +     // uncomment if you need to get the currently connected users for today
        ")",
        nativeQuery = true)
    // long countCurrentlyConnectedUsers(           // uncomment if you need to get the currently connected users for today
    //     @Param("startOfDay") long startOfDay,
    //     @Param("endOfDay") long endOfDay
    // );
    long countOnlineUsers();
    
    // Query to get the number of currently connected access points
    // @Query(value = "SELECT COUNT(DISTINCT called_station_id) " +
    //     "FROM accounting " +
    //     "WHERE acctstatustype = 'Start' " +
    //     "AND time_stamp >= :startOfDay " +
    //     "AND time_stamp < :endOfDay",
    //     nativeQuery = true)
    // long countCurrentlyConnectedAPs(
    //     @Param("startOfDay") long startOfDay,
    //     @Param("endOfDay") long endOfDay
    // );
    // NOTE: since we couldn't just query directly the access points with acctstatustype of 'Start' as the table only inserts data (no updating nor removing a data),
    // we just have to make sure that the certain 'Start' session doesn't have a corresponding 'Stop' status type
    @Query(value = "SELECT COUNT(DISTINCT called_station_id) FROM accounting a1 WHERE a1.acctstatustype = 'Start'" +
        // "AND time_stamp >= :startOfDay " +
        // "AND time_stamp < :endOfDay",
        "AND NOT EXISTS (SELECT 1 FROM accounting a2 WHERE a2.acctstatustype = 'Stop' AND a2.calling_station_id = a1.calling_station_id AND a2.called_station_id = a1.called_station_id AND a2.time_stamp >= a1.time_stamp" +
        // "   AND a2.time_stamp < :endOfDay" +     // uncomment if you need to get the currently connected users for today
        ")",
        nativeQuery = true)
    long countOnlineAPs(); // APs currently in use 

    @Query(value = "SELECT COUNT(DISTINCT called_station_id) FROM accounting WHERE called_station_id IS NOT NULL AND time_stamp >= :startOfDay", nativeQuery = true)
    long countActiveAPs(@Param("startOfDay") long startOfDay); // APs that have been used for the last x number of days

    @Query(value = "SELECT COUNT(DISTINCT called_station_id) FROM accounting WHERE called_station_id IS NOT NULL AND called_station_id NOT IN (SELECT DISTINCT called_station_id FROM accounting WHERE time_stamp >= :startOfDay)", nativeQuery = true)
    long countInactiveAPs(@Param("startOfDay") long startOfDay); // APs that have not been used for the last x number of days

    @Query(value = "SELECT COUNT(DISTINCT username) FROM accounting WHERE calling_station_id IS NOT NULL AND time_stamp >= :startOfDay", nativeQuery = true)
    long countActiveUsers(@Param("startOfDay") long startOfDay); // APs that have been used for the last x number of days

    @Query(value = "SELECT COUNT(DISTINCT username) FROM accounting WHERE calling_station_id IS NOT NULL AND calling_station_id NOT IN (SELECT DISTINCT calling_station_id FROM accounting WHERE time_stamp >= :startOfDay)", nativeQuery = true)
    long countInactiveUsers(@Param("startOfDay") long startOfDay); // APs that have not been used for the last x number of days

    @Query(value = "SELECT COUNT(DISTINCT called_station_id) FROM accounting a1", nativeQuery = true)
    long countTotalAPs();

    // Query to get the total user connections for today
    @Query(value = "SELECT COUNT(DISTINCT calling_station_id) FROM accounting WHERE time_stamp >= :startOfDay", nativeQuery = true)
    long countTotalUserConnectionsToday(@Param("startOfDay") long startOfDay);

    // Query to get the total user connections for today
    @Query(value = "SELECT COUNT(DISTINCT acctsessionid) FROM accounting WHERE time_stamp >= :startOfDay", nativeQuery = true)
    long countTotalSessionsToday(@Param("startOfDay") long startOfDay);

    // Query to get the total bandwidth consumption for today
    @Query(value = "SELECT COALESCE(SUM(acctinputoctets + acctoutputoctets), 0) FROM accounting WHERE acctstatustype = 'Stop' AND time_stamp >= :startOfDay", nativeQuery = true)
    long totalBandwidthConsumptionToday(@Param("startOfDay") long startOfDay);

    // Query to get the total session time for today
    @Query(value = "SELECT COALESCE(SUM(acctsessiontime), 0) FROM accounting WHERE acctstatustype = 'Stop' AND acctsessiontime > 0 AND time_stamp >= :startOfDay", nativeQuery = true)
    Double totalSessionTimeToday(@Param("startOfDay") long startOfDay);

    // Query to get average connection time
    @Query(value = "SELECT AVG(acctsessiontime) FROM accounting WHERE acctstatustype = 'Stop' AND acctsessiontime > 0", nativeQuery = true)
    Double findAverageConnectionTime();

    // Query to get average connection time
    @Query(value = "SELECT COALESCE(AVG(acctsessiontime), 0) FROM accounting WHERE acctstatustype = 'Stop' AND time_stamp >= :startOfMonth AND acctsessiontime > 0", nativeQuery = true)
    Double findAverageConnectionTime(@Param("startOfMonth") long startOfMonth);

    // Query to get the average bandwidth per connection
    @Query(value = "SELECT AVG((a.acctinputoctets + a.acctoutputoctets) * 1.0 / a.acctsessiontime) " +
        "FROM accounting a " +
        "WHERE a.acctstatustype = 'Stop' " +
        "AND a.acctsessiontime > 0",
        // "AND a.time_stamp >= :startTime " +  // Uncomment if need to get the average bandwidth per connection for today
        // "AND a.time_stamp < :endTime",       // Uncomment if need to get the average bandwidth per connection for today
    nativeQuery = true)
    Double findAverageBandwidthPerConnection(
        // @Param("startTime") long startTime,  // Uncomment if need to get the average bandwidth per connection for today
        // @Param("endTime") long endTime       // Uncomment if need to get the average bandwidth per connection for today
    );
    
    // Query to get the average bandwidth per connection
    @Query(value = "SELECT AVG((a.acctinputoctets + a.acctoutputoctets) * 1.0 / a.acctsessiontime) FROM accounting a WHERE a.acctstatustype = 'Stop' AND time_stamp >= :startOfMonth AND a.acctsessiontime > 0", nativeQuery = true)
    Double findAverageBandwidthPerConnection(@Param("startOfMonth") long startOfMonth);

    // Query to get the list of access points
    @Query(value = "SELECT DISTINCT called_station_id " +
        "FROM accounting ",
        nativeQuery = true)
    List<String> findAllAccessPoints();
    
    // Query to get the list of access points
    @Query(value = "SELECT DISTINCT called_station_id FROM accounting ", nativeQuery = true)
    List<Accounting> findAllAccessPointsInfo();

    // Query to get number of currently connected users per access point
    // @Query(value = "SELECT called_station_id, COUNT(DISTINCT calling_station_id) as user_count " +
    //     "FROM accounting " +
    //     "WHERE acctstatustype = 'Start' " +
    //     "AND time_stamp >= :startOfDay " +
    //     "AND time_stamp < :endOfDay " +
    //     "GROUP BY called_station_id",
    //     nativeQuery = true)
    // List<Object[]> countCurrentlyConnectedUsersPerAP(
    //     @Param("startOfDay") long startOfDay,
    //     @Param("endOfDay") long endOfDay
    // );
    // NOTE: since we couldn't just query directly the users with acctstatustype of 'Start' as the table only inserts data (no updating nor removing a data),
    // we just have to make sure that the certain 'Start' session doesn't have a corresponding 'Stop' status type
    @Query(value = "SELECT a1.called_station_id, COUNT(DISTINCT a1.calling_station_id) as user_count " +
        "FROM accounting a1 " +
        "WHERE a1.acctstatustype = 'Start' " +
        // "AND a1.time_stamp >= :startOfDay " +   // Uncomment if you need to get the currently connected users for today
        // "AND a1.time_stamp < :endOfDay " +      // Uncomment if you need to get the currently connected users for today
        "AND NOT EXISTS (" +
        "   SELECT 1 FROM accounting a2 " +
        "   WHERE a2.acctstatustype = 'Stop' " +
        "   AND a2.calling_station_id = a1.calling_station_id " +
        "   AND a2.time_stamp >= a1.time_stamp " +
        // "   AND a2.time_stamp < :endOfDay " +    // Uncomment if you need to get the currently connected users for today
        ")" +
        "GROUP BY a1.called_station_id",
        nativeQuery = true)
    List<Object[]> countCurrentlyConnectedUsersPerAP();

    // Query to get list of currently connected users per access point
    // @Query(value = "SELECT called_station_id, username, acctinputoctets, acctoutputoctets, nasport, calling_station_id, time_stamp " +
    //     "FROM accounting " +
    //     "WHERE acctstatustype = 'Start' " +
    //     "AND time_stamp >= :startOfDay " +
    //     "AND time_stamp < :endOfDay " +
    //     "ORDER BY called_station_id, time_stamp DESC",
    //     nativeQuery = true)
    // List<Object[]> findCurrentlyConnectedUsersPerAP(
    //     @Param("startOfDay") long startOfDay,
    //     @Param("endOfDay") long endOfDay
    // );
    // NOTE: since we couldn't just query directly the users with acctstatustype of 'Start' as the table only inserts data (no updating nor removing a data),
    // we just have to make sure that the certain 'Start' session doesn't have a corresponding 'Stop' status type
    @Query(value = "SELECT sub.called_station_id, sub.username, sub.acctinputoctets, sub.acctoutputoctets, sub.nasport, sub.calling_station_id, sub.time_stamp FROM (SELECT a1.* FROM accounting a1 WHERE a1.acctstatustype IN ('Start', 'Alive') AND NOT EXISTS (SELECT 1 FROM accounting a2 WHERE a2.acctstatustype = 'Stop' AND a2.calling_station_id = a1.calling_station_id AND a2.time_stamp >= a1.time_stamp)) sub INNER JOIN (SELECT calling_station_id, MAX(time_stamp) AS max_time FROM accounting WHERE acctstatustype IN ('Start', 'Alive') GROUP BY calling_station_id) latest ON sub.calling_station_id = latest.calling_station_id AND sub.time_stamp = latest.max_time ORDER BY sub.called_station_id, sub.time_stamp DESC", nativeQuery = true)
    List<Object[]> findCurrentlyConnectedUsersPerAP(
        // @Param("startOfDay") long startOfDay,    // Uncomment if need to get currently connected users for today
        // @Param("endOfDay") long endOfDay         // Uncomment if need to get currently connected users for today
    );
    

    @Query(value =
        "WITH latest_records AS ( " +
        "    SELECT DISTINCT ON (a.acctsessionid) " +
        "        a.acctsessionid, " +
        "        a.username, " +
        "        a.start_time, " +
        "        a.acctsessiontime, " +
        "        a.acctinputoctets + a.acctoutputoctets AS bandwidth, " +
        "        a.acctstatustype " +
        "    FROM accounting a " +
        "    WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '30 minutes' " +
        "    ORDER BY a.acctsessionid, a.time_stamp DESC " +
        ") " +
        "SELECT " +
        "    username, " +
        "    COUNT(acctsessionid) AS active_session_count, " +
        "    COALESCE(SUM(acctsessiontime),0) AS total_session_duration_in_seconds, " +
        "    COALESCE(SUM(bandwidth),0) AS total_bandwidth " +
        "FROM latest_records " +
        "WHERE acctstatustype != 'Stop' " +
        "GROUP BY username " +
        "ORDER BY username",
        nativeQuery = true)
    List<Object[]> findAllCurrentOnlineUsers();

    @Query(value =
        "WITH latest_records AS ( " +
        "    SELECT DISTINCT ON (a.acctsessionid) " +
        "        a.acctsessionid, " +
        "        a.username, " +
        "        a.acctstatustype " +
        "    FROM accounting a " +
        "    WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '30 minutes' " +
        "    ORDER BY a.acctsessionid, a.time_stamp DESC " +
        ") " +
        "SELECT COUNT(DISTINCT username) " +
        "FROM latest_records " +
        "WHERE acctstatustype != 'Stop'",
        nativeQuery = true)
    Long countAllCurrentOnlineUsers();
    
    @Query(value =
        "SELECT DISTINCT ON (a.acctsessionid) " +
        "    a.acctsessionid, " +
        "    a.username, " +
        "    a.calling_station_id, " +
        "    a.called_station_id, " +
        "    TO_TIMESTAMP(a.start_time) AS start_time, " +
        "    a.acctsessiontime, " +
        "    a.acctinputoctets + a.acctoutputoctets AS bandwidth " +
        "FROM accounting a " +
        "WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '30 minutes' " +
        "    AND a.username = :username " +
        "    AND acctstatustype != 'Stop' " +
        "ORDER BY a.acctsessionid, a.time_stamp DESC ",
        nativeQuery = true)
    List<Object[]> findAllSessionsByUsernameForCurrentOnlineUsers(@Param("username") String username);//CHECK
    
    @Query(value =
        "WITH latest_sessions AS ( " +
        "    SELECT DISTINCT ON (a.acctsessionid) " +
        "        a.acctsessionid, " +
        "        a.username, " +
        "        a.acctstatustype, " +
        "        a.acctsessiontime, " +
        "        a.acctinputoctets + a.acctoutputoctets AS bandwidth, " +
        "        TO_TIMESTAMP(a.time_stamp) AS last_timestamp " +
        "    FROM accounting a " +
        "    WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '7 days' " +
        "    ORDER BY a.acctsessionid, a.time_stamp DESC " +
        ") " +
        "SELECT " +
        "    username, " +
        "    COUNT(acctsessionid) AS session_count, " +
        "    SUM(acctsessiontime) AS total_session_duration_in_seconds, " +
        "    SUM(bandwidth) AS total_bandwidth, " +
        "    (SUM(acctsessiontime) / NULLIF(COUNT(DISTINCT acctsessionid), 0)) AS avg_session_length_seconds " +
        "FROM latest_sessions " +
        "GROUP BY username " +
        "ORDER BY username",
        nativeQuery = true)
    List<Object[]> findAllActiveUsersForThePast7Days();

    @Query(value =
        "WITH latest_sessions AS ( " +
        "    SELECT DISTINCT ON (a.acctsessionid) " +
        "        a.username " +
        "    FROM accounting a " +
        "    WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '7 days' " +
        "    ORDER BY a.acctsessionid, a.time_stamp DESC " +
        ") " +
        "SELECT COUNT(DISTINCT username) AS user_count " +
        "FROM latest_sessions",
        nativeQuery = true)
    Long countForAllActiveUsersForThePast7Days();

    // @Query(value =
    //     "SELECT DISTINCT ON (a.acctsessionid) " +
    //     "    a.acctsessionid, " +
    //     "    a.calling_station_id, " +
    //     "    a.called_station_id, " +
    //     "    TO_TIMESTAMP(a.start_time) AS start_time, " +
    //     "    a.acctsessiontime, " +
    //     "    a.acctinputoctets + a.acctoutputoctets AS bandwidth " +
    //     "FROM accounting a " +
    //     "WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '7 days' " +
    //     "   AND a.username = :username " +
    //     "ORDER BY a.acctsessionid, a.time_stamp DESC ",
    //     nativeQuery = true)
    // List<Object[]> findAllSessionsByUsernameForThePast7Days(@Param("username") String username);
    @Query(value =
        "WITH latest_routers AS ( " +
        "    SELECT DISTINCT ON (UPPER(r.mac_address)) r.* " +
        "    FROM \"Routers\" r " +
        "    ORDER BY UPPER(r.mac_address), r.created_at DESC " +
        ") " +
        "SELECT DISTINCT ON (a.acctsessionid) " +
        "    a.acctsessionid, " +
        "    a.calling_station_id, " +
        "    a.called_station_id, " +
        "    TO_TIMESTAMP(a.start_time) AS start_time, " +
        "    a.acctsessiontime, " +
        "    a.acctinputoctets + a.acctoutputoctets AS bandwidth, " +
        "    lr.long, " +
        "    lr.lat " +
        "FROM accounting a " +
        "LEFT JOIN latest_routers lr " +
        "    ON UPPER(split_part(a.called_station_id, chr(58), 1)) = UPPER(lr.mac_address) " + //chr(58) is colon (:)
        "WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '7 days' " +
        "    AND a.username = :username " +
        "ORDER BY a.acctsessionid, a.time_stamp DESC",
        nativeQuery = true)
    List<Object[]> findAllSessionsWithLocationByUsernameForThePast7Days(@Param("username") String username); // Modified query to include location data from Routers table

    @Query(value =
        "WITH latest_records AS ( " +
        "    SELECT DISTINCT ON (a.acctsessionid) " +
        "        a.username, " +
        "        a.acctsessionid, " +
        "        a.acctsessiontime, " +
        "        a.acctinputoctets + a.acctoutputoctets AS bandwidth " +
        "    FROM accounting a " +
        "    ORDER BY a.acctsessionid, a.time_stamp DESC " +
        ") " +
        "SELECT " +
        "    username, " +
        "    COUNT(acctsessionid) AS session_count, " +
        "    SUM(acctsessiontime) AS total_duration_seconds, " +
        "    SUM(bandwidth) AS total_bandwidth, " +
        "    (SUM(acctsessiontime) / NULLIF(COUNT(acctsessionid), 0)) AS avg_session_length_seconds " +
        "FROM latest_records " +
        "GROUP BY username " +
        "ORDER BY username ",
        nativeQuery = true)
    List<Object[]> findAllRegisteredUsersWithSessions();

    @Query(value =
        "SELECT COUNT(DISTINCT username) FROM accounting;", nativeQuery = true)
    Long countAllRegisteredUsersWithSessions();


    // @Query(value =
    //     "SELECT DISTINCT ON (a.acctsessionid)" +
    //     "    a.acctsessionid, " +
    //     "    a.calling_station_id, " +
    //     "    a.called_station_id, " +
    //     "    TO_TIMESTAMP(a.start_time) AS start_time, " +
    //     "    a.acctsessiontime, " +
    //     "    (a.acctinputoctets + a.acctoutputoctets) AS bandwidth " +
    //     // "    a.location " +
    //     "FROM accounting a " +
    //     "WHERE a.username = :username " +
    //     "ORDER BY a.acctsessionid, a.time_stamp DESC ",
    //     nativeQuery = true)
    // List<Object[]> findAllSessionsByUsername(@Param("username") String username);
    @Query(value =
        "WITH latest_routers AS ( " +
        "    SELECT DISTINCT ON (UPPER(r.mac_address)) r.* " +
        "    FROM \"Routers\" r " +
        "    ORDER BY UPPER(r.mac_address), r.created_at DESC " +
        ") " +
        "SELECT DISTINCT ON (a.acctsessionid) " +
        "    a.acctsessionid, " +
        "    a.calling_station_id, " +
        "    a.called_station_id, " +
        "    TO_TIMESTAMP(a.start_time) AS start_time, " +
        "    a.acctsessiontime, " +
        "    (a.acctinputoctets + a.acctoutputoctets) AS bandwidth, " +
        "    lr.long, " +
        "    lr.lat " +
        "FROM accounting a " +
        "LEFT JOIN latest_routers lr " +
        "    ON UPPER(split_part(a.called_station_id, chr(58), 1)) = UPPER(lr.mac_address) " + //chr(58) is colon (:)
        "WHERE a.username = :username " +
        "ORDER BY a.acctsessionid, a.time_stamp DESC",
        nativeQuery = true)
    List<Object[]> findAllSessionsWithLocationByUsername(@Param("username") String username); // Modified query to include location data from Routers table

    @Query(value =
        "WITH latest_sessions AS ( " +
        "    SELECT DISTINCT ON (a.acctsessionid) " +
        "        a.called_station_id AS ap_id, " +
        "        a.acctsessionid, " +
        "        TO_TIMESTAMP(a.time_stamp) AS last_update, " +
        "        a.acctstatustype " +
        "    FROM accounting a " +
        "    WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '30 minutes' " +
        "    ORDER BY a.acctsessionid, a.time_stamp DESC " +
        ") " +
        "SELECT COUNT(DISTINCT ap_id) AS total_online_aps " +
        "FROM latest_sessions " +
        "WHERE acctstatustype != 'Stop'",
        nativeQuery = true)
    Long countAllCurrentOnlineApForThePast30Mins();

    // @Query(value =
    //     "WITH latest_sessions AS ( " +
    //     "    SELECT DISTINCT ON (a.acctsessionid) " +
    //     "        a.called_station_id AS ap_id, " +
    //     "        a.acctsessionid, " +
    //     "        TO_TIMESTAMP(a.time_stamp) AS last_update, " +
    //     "        a.acctsessiontime AS duration_seconds, " +
    //     "        a.acctinputoctets + a.acctoutputoctets AS bandwidth, " +
    //     "        a.acctstatustype " +
    //     "    FROM accounting a " +
    //     "    WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '30 minutes' " +
    //     "    ORDER BY a.acctsessionid, a.time_stamp DESC " +
    //     ") " +
    //     "SELECT " +
    //     "    ap_id, " +
    //     "    COUNT(*) AS total_sessions, " +
    //     "    SUM(bandwidth) AS total_bandwidth, " +
    //     "    AVG(duration_seconds) AS avg_session_duration_seconds " +
    //     "FROM latest_sessions " +
    //     "WHERE acctstatustype != 'Stop' " +
    //     "GROUP BY ap_id " +
    //     "ORDER BY total_bandwidth DESC",
    //     nativeQuery = true)
    // List<Object[]> findAllCurrentOnlineApForThePast30Mins();
    @Query(value =
        "WITH latest_sessions AS ( " +
        "    SELECT DISTINCT ON (a.acctsessionid) " +
        "        a.called_station_id AS ap_id, " +
        "        a.acctsessionid, " +
        "        TO_TIMESTAMP(a.time_stamp) AS last_update, " +
        "        a.acctsessiontime AS duration_seconds, " +
        "        a.acctinputoctets + a.acctoutputoctets AS bandwidth, " +
        "        a.acctstatustype " +
        "    FROM accounting a " +
        "    WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '30 minutes' " +
        "    ORDER BY a.acctsessionid, a.time_stamp DESC " +
        ") " +
        "SELECT " +
        "    ls.ap_id, " +
        "    COUNT(*) AS total_sessions, " +
        "    SUM(ls.bandwidth) AS total_bandwidth, " +
        "    AVG(ls.duration_seconds) AS avg_session_duration_seconds, " +
        "    lr.long, " +
        "    lr.lat " +
        "FROM latest_sessions ls " +
        "LEFT JOIN ( " +
        "    SELECT DISTINCT ON (UPPER(r.mac_address)) r.* " +
        "    FROM \"Routers\" r " +
        "    ORDER BY UPPER(r.mac_address), r.created_at DESC " +
        ") lr " +
        "ON UPPER(split_part(ls.ap_id, chr(58), 1)) = UPPER(lr.mac_address) " + //chr(58) is colon (:)
        "WHERE ls.acctstatustype != 'Stop' " +
        "GROUP BY ls.ap_id, lr.long, lr.lat " +
        "ORDER BY total_bandwidth DESC",
        nativeQuery = true)
    List<Object[]> findAllCurrentOnlineApWithLocationForThePast30Mins(); // Modified query to include location data from Routers table

    @Query(value =
        "WITH alive_sessions AS ( " +
        "    SELECT DISTINCT ON (a.called_station_id, a.username, a.acctsessionid) " +
        "        a.called_station_id AS ap_id, " +
        "        a.username, " +
        "        a.acctsessionid, " +
        "        TO_TIMESTAMP(a.time_stamp) AS start_time, " +
        "        a.acctsessiontime AS duration_seconds, " +
        "        (a.acctinputoctets + a.acctoutputoctets) AS bandwidth, " +
        "        TO_TIMESTAMP(a.time_stamp) AS last_update, " +
        "        a.acctstatustype " +
        "    FROM accounting a " +
        "    WHERE a.called_station_id = :apId " +
        "    ORDER BY a.called_station_id, a.username, a.acctsessionid, a.time_stamp DESC " +
        "), " +
        "filtered AS ( " +
        "    SELECT * " +
        "    FROM alive_sessions " +
        "    WHERE acctstatustype != 'Stop' " +
        "      AND last_update >= NOW() - interval '30 minutes' " +
        ") " +
        "SELECT " +
        "    ap_id, " +
        "    username, " +
        "    COUNT(acctsessionid) AS total_sessions, " +
        "    SUM(duration_seconds) AS total_time_seconds, " +
        "    SUM(bandwidth) AS total_bandwidth, " +
        "    AVG(duration_seconds) AS avg_session_length_seconds " +
        "FROM filtered " +
        "GROUP BY ap_id, username " +
        "ORDER BY total_bandwidth DESC",
        nativeQuery = true)
    List<Object[]> findCurrentOnlineApForThePast30MinsByApId(@Param("apId") String apId);

    @Query(value =
        "WITH stop_sessions AS ( " +
        "    SELECT calling_station_id, called_station_id, acctsessionid " +
        "    FROM accounting " +
        "    WHERE acctstatustype = 'Stop' " +
        "), " +
        "active_sessions AS ( " +
        "    SELECT DISTINCT ON (a.acctsessionid) " +
        "           a.username, " +
        "           a.acctsessionid, " +
        "           a.calling_station_id, " +
        "           a.called_station_id, " +
        "           TO_TIMESTAMP(a.start_time) AS start_time, " +
        "           a.acctsessiontime AS duration_seconds, " +
        "           a.acctinputoctets + a.acctoutputoctets AS bandwidth, " +
        "           TO_TIMESTAMP(a.time_stamp) AS latest_time " +
        "    FROM accounting a " +
        "    LEFT JOIN stop_sessions s " +
        "           ON a.calling_station_id = s.calling_station_id " +
        "           AND a.called_station_id = s.called_station_id " +
        "           AND a.acctsessionid = s.acctsessionid " +
        "    WHERE a.acctstatustype IN ('Start','Alive') " +
        "      AND s.acctsessionid IS NULL " +
        "      AND a.called_station_id = :apId " +
        "      AND a.username = :username " +
        "    ORDER BY a.acctsessionid, a.time_stamp DESC " +
        ") " +
        "SELECT * " +
        "FROM active_sessions " +
        "WHERE latest_time >= NOW() - INTERVAL '30 minutes' " +
        "ORDER BY start_time",
        nativeQuery = true)
    List<Object[]> findSessionForCurrentOnlineUsersByUsernameAndApId(
        @Param("apId") String apId,
        @Param("username") String username);

    @Query(value =
        "WITH ap_sessions AS ( " +
        "    SELECT " +
        "        a.called_station_id AS ap_id, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_seen " +
        "    FROM accounting a " +
        "    GROUP BY a.called_station_id " +
        ") " +
        "SELECT COUNT(*) AS active_ap_count " +
        "FROM ap_sessions " +
        "WHERE last_seen >= NOW() - interval '7 days'",
        nativeQuery = true)
    Long countAllActiveApForThePast7Days();

    // @Query(value =
    //     "WITH ap_sessions AS ( " +
    //     "    SELECT " +
    //     "        a.called_station_id AS ap_id, " +
    //     "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_seen, " +
    //     "        COUNT(DISTINCT a.acctsessionid) AS total_sessions, " +
    //     "        SUM(a.acctinputoctets + a.acctoutputoctets) AS total_bandwidth, " +
    //     "        AVG(a.acctsessiontime) AS avg_session_duration_seconds " +
    //     "    FROM accounting a " +
    //     "    GROUP BY a.called_station_id " +
    //     ") " +
    //     "SELECT " +
    //     "    ap_id, " +
    //     "    total_sessions, " +
    //     "    total_bandwidth, " +
    //     "    avg_session_duration_seconds " +
    //     "FROM ap_sessions " +
    //     "WHERE last_seen >= NOW() - interval '7 days' " +
    //     "ORDER BY total_bandwidth DESC",
    //     nativeQuery = true)
    // List<Object[]> findAllActiveApForThePast7Days();
    @Query(value =
        "WITH latest_routers AS ( " +
        "    SELECT DISTINCT ON (UPPER(r.mac_address)) r.* " +
        "    FROM \"Routers\" r " +
        "    ORDER BY UPPER(r.mac_address), r.created_at DESC " +
        "), " +
        "ap_sessions AS ( " +
        "    SELECT " +
        "        a.called_station_id AS ap_id, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_seen, " +
        "        COUNT(DISTINCT a.acctsessionid) AS total_sessions, " +
        "        SUM(a.acctinputoctets + a.acctoutputoctets) AS total_bandwidth, " +
        "        AVG(a.acctsessiontime) AS avg_session_duration_seconds " +
        "    FROM accounting a " +
        "    GROUP BY a.called_station_id " +
        ") " +
        "SELECT " +
        "    s.ap_id, " +
        "    s.total_sessions, " +
        "    s.total_bandwidth, " +
        "    s.avg_session_duration_seconds, " +
        "    lr.long, " +
        "    lr.lat " +
        "FROM ap_sessions s " +
        "LEFT JOIN latest_routers lr " +
        "    ON UPPER(split_part(s.ap_id, chr(58), 1)) = UPPER(lr.mac_address) " + //chr(58) is colon (:)
        "WHERE s.last_seen >= NOW() - interval '7 days' " +
        "ORDER BY s.total_bandwidth DESC",
        nativeQuery = true)
    List<Object[]> findAllActiveApWithLocationForThePast7Days(); // Modified query to include location data from Routers table
            
    @Query(value =
        "WITH sessions AS (" +
        "    SELECT DISTINCT ON (a.acctsessionid)" +
        "        a.called_station_id AS ap_id, " +
        "        a.username, " +
        "        a.acctsessionid, " +
        "        a.start_time AS start_time, " +
        "        TO_TIMESTAMP(a.time_stamp) AS last_update, " +
        "        (a.acctinputoctets + a.acctoutputoctets) AS bandwidth, " +
        "        a.acctsessiontime AS duration_seconds " +
        "    FROM accounting a " +
        "    WHERE a.called_station_id = :apId " +
        // "      AND TO_TIMESTAMP(a.time_stamp) >= NOW() - interval '7 days' " + //findAllActiveApForThePast7DaysByApId
        "    ORDER BY a.acctsessionid, a.time_stamp DESC " +
        ") " +
        "SELECT " +
        "    ap_id, " +
        "    username, " +
        "    COUNT(acctsessionid) AS total_sessions, " +
        "    SUM(duration_seconds) AS total_time_seconds, " +
        "    SUM(bandwidth) AS total_bandwidth, " +
        "    AVG(duration_seconds) AS avg_session_length_seconds " +
        "FROM sessions " +
        "GROUP BY ap_id, username " +
        "ORDER BY total_bandwidth DESC ",
        nativeQuery = true)
    // List<Object[]> findAllActiveApForThePast7DaysByApId(@Param("apId") String apId);
    List<Object[]> findAllActiveOrInActiveApForThePast7DaysByApId(@Param("apId") String apId);

    @Query(value =
        "WITH ap_sessions AS ( " +
        "    SELECT " +
        "        a.called_station_id AS ap_id, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_seen " +
        "    FROM accounting a " +
        "    GROUP BY a.called_station_id " +
        ") " +
        "SELECT COUNT(*) AS active_ap_count " +
        "FROM ap_sessions " +
        "WHERE last_seen < NOW() - interval '7 days'",
        nativeQuery = true)
    Long countAllInActiveApForMoreThan7Days();
        
    // @Query(value =
    //     "WITH ap_sessions AS ( " +
    //     "    SELECT " +
    //     "        a.called_station_id AS ap_id, " +
    //     "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_seen, " +
    //     "        COUNT(DISTINCT a.acctsessionid) AS total_sessions, " +
    //     "        SUM(a.acctinputoctets + a.acctoutputoctets) AS total_bandwidth, " +
    //     "        AVG(a.acctsessiontime) AS avg_session_duration_seconds " +
    //     "    FROM accounting a " +
    //     "    GROUP BY a.called_station_id " +
    //     ") " +
    //     "SELECT " +
    //     "    ap_id, " +
    //     "    total_sessions, " +
    //     "    total_bandwidth, " +
    //     "    avg_session_duration_seconds " +
    //     "FROM ap_sessions " +
    //     "WHERE last_seen < NOW() - interval '7 days' " +
    //     "ORDER BY total_bandwidth DESC",
    //     nativeQuery = true)
    // List<Object[]> findAllInActiveApForMoreThan7Days();
    @Query(value =
        "WITH latest_routers AS ( " +
        "    SELECT DISTINCT ON (UPPER(r.mac_address)) r.* " +
        "    FROM \"Routers\" r " +
        "    ORDER BY UPPER(r.mac_address), r.created_at DESC " +
        "), " +
        "ap_sessions AS ( " +
        "    SELECT " +
        "        a.called_station_id AS ap_id, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_seen, " +
        "        COUNT(DISTINCT a.acctsessionid) AS total_sessions, " +
        "        SUM(a.acctinputoctets + a.acctoutputoctets) AS total_bandwidth, " +
        "        AVG(a.acctsessiontime) AS avg_session_duration_seconds " +
        "    FROM accounting a " +
        "    GROUP BY a.called_station_id " +
        ") " +
        "SELECT " +
        "    s.ap_id, " +
        "    s.total_sessions, " +
        "    s.total_bandwidth, " +
        "    s.avg_session_duration_seconds, " +
        "    lr.long, " +
        "    lr.lat " +
        "FROM ap_sessions s " +
        "LEFT JOIN latest_routers lr " +
        "    ON UPPER(split_part(s.ap_id, chr(58), 1)) = UPPER(lr.mac_address) " + //chr(58) is colon (:)
        "WHERE s.last_seen < NOW() - interval '7 days' " +
        "ORDER BY s.total_bandwidth DESC",
        nativeQuery = true)
    List<Object[]> findAllInActiveApWithLocationForMoreThan7Days(); // Modified query to include location data from Routers table

    // @Query(value =
    //     "WITH sessions AS (" +
    //     "    SELECT DISTINCT ON (a.acctsessionid)" +
    //     "        a.called_station_id AS ap_id, " +
    //     "        a.username, " +
    //     "        a.acctsessionid, " +
    //     "        a.start_time AS start_time, " +
    //     "        TO_TIMESTAMP(a.time_stamp) AS last_update, " +
    //     "        (a.acctinputoctets + a.acctoutputoctets) AS bandwidth, " +
    //     "        a.acctsessiontime AS duration_seconds " +
    //     "    FROM accounting a " +
    //     "    WHERE a.called_station_id = :apId " +
    //     // "      AND TO_TIMESTAMP(a.time_stamp) < NOW() - interval '7 days' " +
    //     "    ORDER BY a.acctsessionid, a.time_stamp DESC " +
    //     ") " +
    //     "SELECT " +
    //     "    ap_id, " +
    //     "    username, " +
    //     "    COUNT(acctsessionid) AS total_sessions, " +
    //     "    SUM(duration_seconds) AS total_time_seconds, " +
    //     "    SUM(bandwidth) AS total_bandwidth, " +
    //     "    AVG(duration_seconds) AS avg_session_length_seconds " +
    //     "FROM sessions " +
    //     "GROUP BY ap_id, username " +
    //     "ORDER BY total_bandwidth DESC ",
    //     nativeQuery = true)
    // List<Object[]> findAllInActiveApForThePast7DaysByApId(@Param("apId") String apId);
    // This query is commented out because we are now using findAllActiveOrInActiveApForThePast7DaysByApId

    @Query(value =
        "SELECT " +
        "   a.called_station_id, " +
        "   COALESCE( " +
        "       TO_TIMESTAMP(a.start_time), " +
        "       MIN(CASE WHEN a.acctstatustype IN ('Start', 'Alive') THEN TO_TIMESTAMP(a.time_stamp) END) " +
        "   ) AS start_time, " +
        "   MAX(TO_TIMESTAMP(a.time_stamp)) AS latest_time " +
        "FROM accounting a " +
        "WHERE a.called_station_id IN (:apId) " +
        "GROUP BY a.called_station_id, a.start_time, a.time_stamp " +
        "ORDER BY a.called_station_id, a.time_stamp DESC",
        nativeQuery = true)
    List<Object[]> findAllTimestampsByApId(@Param("apId") List<String> apId);
    
    @Query(value =
        "SELECT " +
        "   TO_TIMESTAMP(a.time_stamp) AS latest_time " +
        "FROM accounting a " +
        "WHERE a.acctsessionid = :sessionId ORDER BY a.time_stamp ASC LIMIT 1",
        nativeQuery = true)
    Timestamp findTimestampStartTimeBySessionId(@Param("sessionId") String sessionId);

}

