package com.acs_tr069.test_tr069.radius.repository;

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
    @Query(value = "SELECT COUNT(calling_station_id) FROM accounting WHERE acctstatustype = 'Start' AND time_stamp >= :startOfDay", nativeQuery = true)
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
        "WITH stop_sessions AS (" +
        "    SELECT calling_station_id, called_station_id, acctsessionid, MAX(time_stamp) AS stop_time " +
        "    FROM accounting " +
        "    WHERE acctstatustype = 'Stop' " +
        "    GROUP BY calling_station_id, called_station_id, acctsessionid " +
        "), " +
        "active_sessions AS (" +
        "    SELECT a.username, " +
        "           a.acctsessionid, " +
        "           MIN(TO_TIMESTAMP(a.time_stamp)) AS start_time, " +
        "           MAX(TO_TIMESTAMP(a.time_stamp)) AS latest_time, " +
        "           MAX(a.acctinputoctets) AS latest_input, " +
        "           MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    LEFT JOIN stop_sessions s " +
        "           ON a.calling_station_id = s.calling_station_id " +
        "           AND a.called_station_id = s.called_station_id " +
        "           AND a.acctsessionid = s.acctsessionid " +
        "    WHERE a.acctstatustype IN ('Start','Alive') " +
        "      AND s.acctsessionid IS NULL " +
        "    GROUP BY a.username, a.acctsessionid " +
        ") " +
        "SELECT username, " +
        "       COUNT(acctsessionid) AS active_session_count, " +
        "       SUM(EXTRACT(EPOCH FROM (NOW() - start_time))) AS total_session_duration_seconds, " +
        "       SUM(latest_input + latest_output) AS total_bandwidth " +
        "FROM active_sessions " +
        "WHERE latest_time >= NOW() - INTERVAL '30 minutes' " +
        "GROUP BY username " +
        "ORDER BY username ",
        nativeQuery = true)
    List<Object[]> findAllCurrentOnlineUsers();

    @Query(value =
        "WITH stop_sessions AS (" +
        "    SELECT calling_station_id, called_station_id, acctsessionid, MAX(time_stamp) AS stop_time " +
        "    FROM accounting " +
        "    WHERE acctstatustype = 'Stop' " +
        "    GROUP BY calling_station_id, called_station_id, acctsessionid " +
        "), " +
        "active_sessions AS (" +
        "    SELECT a.username, " +
        "           a.acctsessionid, " +
        "           MIN(TO_TIMESTAMP(a.time_stamp)) AS start_time, " +
        "           MAX(TO_TIMESTAMP(a.time_stamp)) AS latest_time, " +
        "           MAX(a.acctinputoctets) AS latest_input, " +
        "           MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    LEFT JOIN stop_sessions s " +
        "           ON a.calling_station_id = s.calling_station_id " +
        "           AND a.called_station_id = s.called_station_id " +
        "           AND a.acctsessionid = s.acctsessionid " +
        "    WHERE a.acctstatustype IN ('Start','Alive') " +
        "      AND s.acctsessionid IS NULL " +
        "    GROUP BY a.username, a.acctsessionid " +
        ") " +
        "SELECT COUNT(DISTINCT username) " +
        "FROM active_sessions " +
        "WHERE latest_time >= NOW() - INTERVAL '30 minutes'",
        nativeQuery = true)
    Long countAllCurrentOnlineUsers();
    
    @Query(value =
        "WITH stop_sessions AS ( " +
        "    SELECT calling_station_id, called_station_id, acctsessionid " +
        "    FROM accounting " +
        "    WHERE acctstatustype = 'Stop' " +
        "), " +
        "active_sessions AS ( " +
        "    SELECT a.acctsessionid, " +
        "           a.username, " +
        "           a.calling_station_id, " +
        "           a.called_station_id, " +
        "           MIN(TO_TIMESTAMP(a.time_stamp)) AS start_time, " +
        "           MAX(TO_TIMESTAMP(a.time_stamp)) AS latest_time, " +
        "           MAX(a.acctinputoctets) AS latest_input, " +
        "           MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    LEFT JOIN stop_sessions s " +
        "           ON a.calling_station_id = s.calling_station_id " +
        "           AND a.called_station_id = s.called_station_id " +
        "           AND a.acctsessionid = s.acctsessionid " +
        "    WHERE a.acctstatustype IN ('Start','Alive') " +
        "      AND s.acctsessionid IS NULL " +
        "      AND a.username = :username " +
        "    GROUP BY a.username, a.acctsessionid, a.calling_station_id, a.called_station_id " +
        ") " +
        "SELECT * " +
        "FROM active_sessions " +
        "WHERE latest_time >= NOW() - INTERVAL '30 minutes' " +
        "ORDER BY start_time",
        nativeQuery = true)
    List<Object[]> findAllSessionsByUsernameForCurrentOnlineUsers(@Param("username") String username);
    
    @Query(value =
        "WITH sessions AS (" +
        "    SELECT " +
        "        a.username, " +
        "        a.acctsessionid, " +
        "        MIN(CASE WHEN a.acctstatustype = 'Start' THEN TO_TIMESTAMP(a.time_stamp) END) AS start_time, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS end_time, " +
        "        MAX(a.acctinputoctets) AS latest_input, " +
            "        MAX(a.acctoutputoctets) AS latest_output " +
            "    FROM accounting a " +
            "    WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '7 days' " +
            "    GROUP BY a.username, a.acctsessionid " +
            "), " +
            "session_metrics AS (" +
            "    SELECT " +
            "        username, " +
            "        acctsessionid, " +
            "        EXTRACT(EPOCH FROM (end_time - start_time)) AS duration_seconds, " +
            "        (latest_input + latest_output) AS bandwidth " +
            "    FROM sessions " +
            "    WHERE start_time IS NOT NULL " +
            ") " +
            "SELECT " +
            "    username, " +
            "    COUNT(DISTINCT acctsessionid) AS session_count, " +
            "    SUM(duration_seconds) AS total_duration_seconds, " +
            "    SUM(bandwidth) AS total_bandwidth, " +
            "    (SUM(duration_seconds) / NULLIF(COUNT(DISTINCT acctsessionid), 0)) AS avg_session_length_seconds " +
            "FROM session_metrics " +
            "GROUP BY username " +
            "ORDER BY username ",
        nativeQuery = true)
    List<Object[]> findAllActiveUsersForThePast7Days();


    @Query(value =
        "WITH sessions AS (" +
        "    SELECT " +
        "        a.username, " +
        "        a.acctsessionid, " +
        "        MIN(CASE WHEN a.acctstatustype = 'Start' THEN TO_TIMESTAMP(a.time_stamp) END) AS start_time, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS end_time, " +
        "        MAX(a.acctinputoctets) AS latest_input, " +
        "        MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '7 days' " +
        "    GROUP BY a.username, a.acctsessionid " +
        "), " +
        "session_metrics AS (" +
        "    SELECT " +
        "        username, " +
        "        acctsessionid, " +
        "        EXTRACT(EPOCH FROM (end_time - start_time)) AS duration_seconds, " +
        "        (latest_input + latest_output) AS bandwidth " +
        "    FROM sessions " +
        "    WHERE start_time IS NOT NULL " +
        ") " +
        "SELECT COUNT(DISTINCT username) " +
        "FROM session_metrics",
        nativeQuery = true)
    Long countForAllActiveUsersForThePast7Days();


    @Query(value =
        "WITH sessions AS (" +
        "    SELECT " +
        "        a.username, " +
        "        a.acctsessionid, " +
        "        a.calling_station_id, " +
        "        a.called_station_id, " +
        "        MIN(CASE WHEN a.acctstatustype = 'Start' THEN TO_TIMESTAMP(a.time_stamp) END) AS start_time, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS end_time, " +
        "        MAX(a.acctinputoctets) AS latest_input, " +
        "        MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    WHERE TO_TIMESTAMP(a.time_stamp) >= NOW() - INTERVAL '7 days' " +
        "      AND a.username = :username " +
        "    GROUP BY a.username, a.acctsessionid, a.calling_station_id, a.called_station_id " +
        ") " +
        "SELECT " +
        "    username, " +
        "    acctsessionid, " +
        "    calling_station_id, " +
        "    called_station_id, " +
        "    start_time, " +
        "    end_time, " +
        "    EXTRACT(EPOCH FROM (end_time - start_time)) AS duration_seconds, " +
        "    (latest_input + latest_output) AS bandwidth " +
        "FROM sessions " +
        "WHERE start_time IS NOT NULL " +
        "ORDER BY end_time DESC ",
        nativeQuery = true)
    List<Object[]> findAllSessionsByUsernameForThePast7Days(@Param("username") String username);

    @Query(value =
        "WITH sessions AS (" +
        "    SELECT " +
        "        a.username, " +
        "        a.acctsessionid, " +
        "        MIN(CASE WHEN a.acctstatustype = 'Start' THEN TO_TIMESTAMP(a.time_stamp) END) AS start_time, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS end_time, " +
        "        MAX(a.acctinputoctets) AS latest_input, " +
        "        MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    GROUP BY a.username, a.acctsessionid " +
        "), " +
        "session_metrics AS (" +
        "    SELECT " +
        "        username, " +
        "        acctsessionid, " +
        "        EXTRACT(EPOCH FROM (end_time - start_time)) AS duration_seconds, " +
        "        (latest_input + latest_output) AS bandwidth " +
        "    FROM sessions " +
        "    WHERE start_time IS NOT NULL " +
        ") " +
        "SELECT " +
        "    username, " +
        "    COUNT(DISTINCT acctsessionid) AS session_count, " +
        "    SUM(duration_seconds) AS total_duration_seconds, " +
        "    SUM(bandwidth) AS total_bandwidth, " +
        "    (SUM(duration_seconds) / NULLIF(COUNT(DISTINCT acctsessionid), 0)) AS avg_session_length_seconds " +
        "FROM session_metrics " +
        "GROUP BY username " +
        "ORDER BY username ",
        nativeQuery = true)
    List<Object[]> findAllRegisteredUsersWithSessions();

    @Query(value =
        "SELECT COUNT(DISTINCT username) FROM accounting;", nativeQuery = true)
    Long countAllRegisteredUsersWithSessions();


    @Query(value =
        "WITH sessions AS (" +
        "    SELECT " +
        "        a.username, " +
        "        a.acctsessionid, " +
        "        a.calling_station_id, " +
        "        a.called_station_id, " +
        "        MIN(CASE WHEN a.acctstatustype = 'Start' THEN TO_TIMESTAMP(a.time_stamp) END) AS start_time, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS end_time, " +
        "        MAX(a.acctinputoctets) AS latest_input, " +
        "        MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    WHERE a.username = :username " +
        "    GROUP BY a.username, a.acctsessionid, a.calling_station_id, a.called_station_id " +
        ") " +
        "SELECT " +
        "    username, " +
        "    acctsessionid, " +
        "    calling_station_id, " +
        "    called_station_id, " +
        "    start_time, " +
        "    end_time, " +
        "    EXTRACT(EPOCH FROM (end_time - start_time)) AS duration_seconds, " +
        "    (latest_input + latest_output) AS bandwidth " +
        "FROM sessions " +
        "WHERE start_time IS NOT NULL " +
        "ORDER BY end_time DESC ",
        nativeQuery = true)
    List<Object[]> findAllSessionsByUsername(@Param("username") String username);

    @Query(value =
        "WITH alive_sessions AS ( " +
        "    SELECT a.called_station_id AS ap_id, a.acctsessionid " +
        "    FROM accounting a " +
        "    GROUP BY a.called_station_id, a.acctsessionid " +
        "    HAVING BOOL_OR(a.acctstatustype = 'Stop') = FALSE " +
        "       AND MAX(TO_TIMESTAMP(a.time_stamp)) >= NOW() - interval '30 minutes' " +
        ") " +
        "SELECT COUNT(DISTINCT ap_id) AS total_online_aps " +
        "FROM alive_sessions",
        nativeQuery = true)
    Long countAllCurrentOnlineApForThePast30Mins();

    @Query(value =
        "WITH alive_sessions AS (" +
        "    SELECT " +
        "        a.called_station_id AS ap_id, " +
        "        a.acctsessionid, " +
        "        MIN(CASE WHEN a.acctstatustype = 'Start' THEN TO_TIMESTAMP(a.time_stamp) END) AS start_time, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_update, " +
        "        MAX(a.acctinputoctets) AS latest_input, " +
        "        MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    GROUP BY a.called_station_id, a.acctsessionid " +
        "    HAVING BOOL_OR(a.acctstatustype = 'Stop') = FALSE " +
        "       AND MAX(TO_TIMESTAMP(a.time_stamp)) >= NOW() - interval '30 minutes' " +
        "), " +
        "durations AS ( " +
        "    SELECT " +
        "        ap_id, " +
        "        acctsessionid, " +
        "        EXTRACT(EPOCH FROM (last_update - start_time)) AS duration_seconds, " +
        "        (latest_input + latest_output) AS bandwidth, " +
        "        DATE_TRUNC('hour', start_time) AS start_hour " +
        "    FROM alive_sessions " +
        "    WHERE start_time IS NOT NULL " +
        "), " +
        "agg_per_hour AS ( " +
        "    SELECT " +
        "        ap_id, " +
        "        start_hour, " +
        "        COUNT(*) AS session_count " +
        "    FROM durations " +
        "    GROUP BY ap_id, start_hour " +
        "), " +
        "peak_per_ap AS ( " +
        "    SELECT ap_id, start_hour AS peak_hour " +
        "    FROM ( " +
        "        SELECT " +
        "            ap_id, " +
        "            start_hour, " +
        "            session_count, " +
        "            ROW_NUMBER() OVER (PARTITION BY ap_id ORDER BY session_count DESC, start_hour) AS rn " +
        "        FROM agg_per_hour " +
        "    ) ranked " +
        "    WHERE rn = 1 " +
        ") " +
        "SELECT " +
        "    d.ap_id, " +
        "    COUNT(DISTINCT d.acctsessionid) AS total_sessions, " +
        "    SUM(d.bandwidth) AS total_bandwidth, " +
        "    AVG(d.duration_seconds) AS avg_session_duration_seconds, " +
        "    p.peak_hour " +
        "FROM durations d " +
        "JOIN peak_per_ap p ON d.ap_id = p.ap_id " +
        "GROUP BY d.ap_id, p.peak_hour " +
        "ORDER BY total_bandwidth DESC ",
        nativeQuery = true)
    List<Object[]> findAllCurrentOnlineApForThePast30Mins();

    @Query(value =
        "WITH alive_sessions AS (" +
        "    SELECT " +
        "        a.called_station_id AS ap_id, " +
        "        a.username, " +
        "        a.acctsessionid, " +
        "        MIN(CASE WHEN a.acctstatustype = 'Start' THEN TO_TIMESTAMP(a.time_stamp) END) AS start_time, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_update, " +
        "        MAX(a.acctinputoctets) AS latest_input, " +
        "        MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    WHERE a.called_station_id = :apId " +
        "    GROUP BY a.called_station_id, a.username, a.acctsessionid " +
        "    HAVING BOOL_OR(a.acctstatustype = 'Stop') = FALSE " +
        "       AND MAX(TO_TIMESTAMP(a.time_stamp)) >= NOW() - interval '30 minutes' " +
        "), " +
        "durations AS (" +
        "    SELECT " +
        "        ap_id, " +
        "        username, " +
        "        acctsessionid, " +
        "        EXTRACT(EPOCH FROM (last_update - start_time)) AS duration_seconds, " +
        "        (latest_input + latest_output) AS bandwidth " +
        "    FROM alive_sessions " +
        "    WHERE start_time IS NOT NULL " +
        ")" +
        "SELECT " +
        "    ap_id, " +
        "    username, " +
        "    COUNT(acctsessionid) AS total_sessions, " +
        "    SUM(duration_seconds) AS total_time_seconds, " +
        "    SUM(bandwidth) AS total_bandwidth, " +
        "    AVG(duration_seconds) AS avg_session_length_seconds " +
        "FROM durations " +
        "GROUP BY ap_id, username " +
        "ORDER BY total_bandwidth DESC ",
        nativeQuery = true)
    List<Object[]> findCurrentOnlineApForThePast30MinsByApId(@Param("apId") String apId);

    @Query(value =
        "WITH stop_sessions AS ( " +
        "    SELECT calling_station_id, called_station_id, acctsessionid " +
        "    FROM accounting " +
        "    WHERE acctstatustype = 'Stop' " +
        "), " +
        "active_sessions AS ( " +
        "    SELECT a.acctsessionid, " +
        "           a.username, " +
        "           a.calling_station_id, " +
        "           a.called_station_id, " +
        "           MIN(TO_TIMESTAMP(a.time_stamp)) AS start_time, " +
        "           MAX(TO_TIMESTAMP(a.time_stamp)) AS latest_time, " +
        "           MAX(a.acctinputoctets) AS latest_input, " +
        "           MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    LEFT JOIN stop_sessions s " +
        "           ON a.calling_station_id = s.calling_station_id " +
        "           AND a.called_station_id = s.called_station_id " +
        "           AND a.acctsessionid = s.acctsessionid " +
        "    WHERE a.acctstatustype IN ('Start','Alive') " +
        "      AND s.acctsessionid IS NULL " +
        "      AND a.called_station_id = :apId " +
        "      AND a.username = :username " +
        "    GROUP BY a.username, a.acctsessionid, a.calling_station_id, a.called_station_id " +
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
        "WITH valid_sessions AS ( " +
        "    SELECT called_station_id AS ap_id, acctsessionid " +
        "    FROM accounting " +
        "    GROUP BY called_station_id, acctsessionid " +
        "    HAVING MAX(TO_TIMESTAMP(time_stamp)) > NOW() - interval '7 days' " +
        "       AND MIN(CASE WHEN acctstatustype = 'Start' THEN TO_TIMESTAMP(time_stamp) END) IS NOT NULL " +
        ") " +
        "SELECT COUNT(*) AS total_aps " +
        "FROM ( " +
        "    SELECT DISTINCT ap_id " +
        "    FROM valid_sessions " +
        ") AS aps",
        nativeQuery = true)
    Long countAllActiveApForThePast7Days();

    @Query(value =
        "WITH sessions AS (" +
        "    SELECT " +
        "        a.called_station_id AS ap_id, " +
        "        a.acctsessionid, " +
        "        MIN(CASE WHEN a.acctstatustype = 'Start' THEN TO_TIMESTAMP(a.time_stamp) END) AS start_time, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_update, " +
        "        MAX(a.acctinputoctets) AS latest_input, " +
        "        MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    GROUP BY a.called_station_id, a.acctsessionid " +
        "    HAVING MAX(TO_TIMESTAMP(a.time_stamp)) >= NOW() - interval '7 days' " +
        "), " +
        "durations AS ( " +
        "    SELECT " +
        "        ap_id, " +
        "        acctsessionid, " +
        "        EXTRACT(EPOCH FROM (last_update - start_time)) AS duration_seconds, " +
        "        (latest_input + latest_output) AS bandwidth, " +
        "        DATE_TRUNC('hour', start_time) AS start_hour " +
        "    FROM sessions " +
        "    WHERE start_time IS NOT NULL " +
        "), " +
        "agg_per_hour AS ( " +
        "    SELECT " +
        "        ap_id, " +
        "        start_hour, " +
        "        COUNT(*) AS session_count " +
        "    FROM durations " +
        "    GROUP BY ap_id, start_hour " +
        "), " +
        "peak_per_ap AS ( " +
        "    SELECT ap_id, start_hour AS peak_hour " +
        "    FROM ( " +
        "        SELECT " +
        "            ap_id, " +
        "            start_hour, " +
        "            session_count, " +
        "            ROW_NUMBER() OVER (PARTITION BY ap_id ORDER BY session_count DESC, start_hour) AS rn " +
        "        FROM agg_per_hour " +
        "    ) ranked " +
        "    WHERE rn = 1 " +
        ") " +
        "SELECT " +
        "    d.ap_id, " +
        "    COUNT(DISTINCT d.acctsessionid) AS total_sessions, " +
        "    SUM(d.bandwidth) AS total_bandwidth, " +
        "    AVG(d.duration_seconds) AS avg_session_duration_seconds, " +
        "    p.peak_hour " +
        "FROM durations d " +
        "JOIN peak_per_ap p ON d.ap_id = p.ap_id " +
        "GROUP BY d.ap_id, p.peak_hour " +
        "ORDER BY total_bandwidth DESC ",
        nativeQuery = true)
    List<Object[]> findAllActiveApForThePast7Days();
            
    @Query(value =
        "WITH sessions AS (" +
        "    SELECT " +
        "        a.called_station_id AS ap_id, " +
        "        a.username, " +
        "        a.acctsessionid, " +
        "        MIN(CASE WHEN a.acctstatustype = 'Start' THEN TO_TIMESTAMP(a.time_stamp) END) AS start_time, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_update, " +
        "        MAX(a.acctinputoctets) AS latest_input, " +
        "        MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    WHERE a.called_station_id = :apId " +
        "    GROUP BY a.called_station_id, a.username, a.acctsessionid " +
        "    HAVING MAX(TO_TIMESTAMP(a.time_stamp)) >= NOW() - interval '7 days' " +
        "), " +
        "durations AS (" +
        "    SELECT " +
        "        ap_id, " +
        "        username, " +
        "        acctsessionid, " +
        "        EXTRACT(EPOCH FROM (last_update - start_time)) AS duration_seconds, " +
        "        (latest_input + latest_output) AS bandwidth " +
        "    FROM sessions " +
        "    WHERE start_time IS NOT NULL " +
        ")" +
        "SELECT " +
        "    ap_id, " +
        "    username, " +
        "    COUNT(acctsessionid) AS total_sessions, " +
        "    SUM(duration_seconds) AS total_time_seconds, " +
        "    SUM(bandwidth) AS total_bandwidth, " +
        "    AVG(duration_seconds) AS avg_session_length_seconds " +
        "FROM durations " +
        "GROUP BY ap_id, username " +
        "ORDER BY total_bandwidth DESC ",
        nativeQuery = true)
    List<Object[]> findAllActiveApForThePast7DaysByApId(@Param("apId") String apId);

    @Query(value =
        "WITH valid_sessions AS ( " +
        "    SELECT called_station_id AS ap_id, acctsessionid " +
        "    FROM accounting " +
        "    GROUP BY called_station_id, acctsessionid " +
        "    HAVING MAX(TO_TIMESTAMP(time_stamp)) <= NOW() - interval '7 days' " +
        "       AND MIN(CASE WHEN acctstatustype = 'Start' THEN TO_TIMESTAMP(time_stamp) END) IS NOT NULL " +
        ") " +
        "SELECT COUNT(*) AS total_aps " +
        "FROM ( " +
        "    SELECT DISTINCT ap_id " +
        "    FROM valid_sessions " +
        ") AS aps",
        nativeQuery = true)
    Long countAllInActiveApForMoreThan7Days();
        
    @Query(value =
        "WITH sessions AS (" +
        "    SELECT " +
        "        a.called_station_id AS ap_id, " +
        "        a.acctsessionid, " +
        "        MIN(CASE WHEN a.acctstatustype = 'Start' THEN TO_TIMESTAMP(a.time_stamp) END) AS start_time, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_update, " +
        "        MAX(a.acctinputoctets) AS latest_input, " +
        "        MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    GROUP BY a.called_station_id, a.acctsessionid " +
        "    HAVING MAX(TO_TIMESTAMP(a.time_stamp)) < NOW() - interval '7 days' " +
        "), " +
        "durations AS ( " +
        "    SELECT " +
        "        ap_id, " +
        "        acctsessionid, " +
        "        EXTRACT(EPOCH FROM (last_update - start_time)) AS duration_seconds, " +
        "        (latest_input + latest_output) AS bandwidth, " +
        "        DATE_TRUNC('hour', start_time) AS start_hour " +
        "    FROM sessions " +
        "    WHERE start_time IS NOT NULL " +
        "), " +
        "agg_per_hour AS ( " +
        "    SELECT " +
        "        ap_id, " +
        "        start_hour, " +
        "        COUNT(*) AS session_count " +
        "    FROM durations " +
        "    GROUP BY ap_id, start_hour " +
        "), " +
        "peak_per_ap AS ( " +
        "    SELECT ap_id, start_hour AS peak_hour " +
        "    FROM ( " +
        "        SELECT " +
        "            ap_id, " +
        "            start_hour, " +
        "            session_count, " +
        "            ROW_NUMBER() OVER (PARTITION BY ap_id ORDER BY session_count DESC, start_hour) AS rn " +
        "        FROM agg_per_hour " +
        "    ) ranked " +
        "    WHERE rn = 1 " +
        ") " +
        "SELECT " +
        "    d.ap_id, " +
        "    COUNT(DISTINCT d.acctsessionid) AS total_sessions, " +
        "    SUM(d.bandwidth) AS total_bandwidth, " +
        "    AVG(d.duration_seconds) AS avg_session_duration_seconds, " +
        "    p.peak_hour " +
        "FROM durations d " +
        "JOIN peak_per_ap p ON d.ap_id = p.ap_id " +
        "GROUP BY d.ap_id, p.peak_hour " +
        "ORDER BY total_bandwidth DESC",
        nativeQuery = true)
    List<Object[]> findAllInActiveApForMoreThan7Days();

    @Query(value =
        "WITH sessions AS (" +
        "    SELECT " +
        "        a.called_station_id AS ap_id, " +
        "        a.username, " +
        "        a.acctsessionid, " +
        "        MIN(CASE WHEN a.acctstatustype = 'Start' THEN TO_TIMESTAMP(a.time_stamp) END) AS start_time, " +
        "        MAX(TO_TIMESTAMP(a.time_stamp)) AS last_update, " +
        "        MAX(a.acctinputoctets) AS latest_input, " +
        "        MAX(a.acctoutputoctets) AS latest_output " +
        "    FROM accounting a " +
        "    WHERE a.called_station_id = :apId " +
        "    GROUP BY a.called_station_id, a.username, a.acctsessionid " +
        "    HAVING MAX(TO_TIMESTAMP(a.time_stamp)) < NOW() - interval '7 days' " +
        "), " +
        "durations AS (" +
        "    SELECT " +
        "        ap_id, " +
        "        username, " +
        "        acctsessionid, " +
        "        EXTRACT(EPOCH FROM (last_update - start_time)) AS duration_seconds, " +
        "        (latest_input + latest_output) AS bandwidth " +
        "    FROM sessions " +
        "    WHERE start_time IS NOT NULL " +
        ")" +
        "SELECT " +
        "    ap_id, " +
        "    username, " +
        "    COUNT(acctsessionid) AS total_sessions, " +
        "    SUM(duration_seconds) AS total_time_seconds, " +
        "    SUM(bandwidth) AS total_bandwidth, " +
        "    AVG(duration_seconds) AS avg_session_length_seconds " +
        "FROM durations " +
        "GROUP BY ap_id, username " +
        "ORDER BY total_bandwidth DESC ",
        nativeQuery = true)
    List<Object[]> findAllInActiveApForThePast7DaysByApId(@Param("apId") String apId);
}

