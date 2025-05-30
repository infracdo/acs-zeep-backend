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
    @Query(value = "SELECT COUNT(DISTINCT a1.username) " +
        "FROM accounting a1 " +
        "WHERE a1.acctstatustype = 'Alive' " +
        "AND NOT EXISTS (" +
        "   SELECT 1 FROM accounting a2 " +
        "   WHERE a2.acctstatustype = 'Stop' " +
        "   AND a2.username = a1.username " +
        "   AND a2.time_stamp >= a1.time_stamp " +
        ")",
        nativeQuery = true)
    long countCurrentlyConnectedUsers();
    
    // Query to get the number of currently connected access points
    // NOTE: change this to nasidentifier if the column is already implemented (since the nasidentifier accurately identifies distinct access points)
    @Query(value = "SELECT COUNT(DISTINCT a1.called_station_id) " +
        "FROM accounting a1 " +
        "WHERE a1.acctstatustype = 'Alive' " +
        "AND NOT EXISTS (" +
        "   SELECT 1 FROM accounting a2 " +
        "   WHERE a2.acctstatustype = 'Stop' " +
        "   AND a2.called_station_id = a1.called_station_id " +
        "   AND a2.time_stamp >= a1.time_stamp " +
        ")",
        nativeQuery = true)
    long countCurrentlyConnectedAPs();

    // Query to get the total user connections for today
    @Query(value = "SELECT COUNT(DISTINCT username) " +
        "FROM accounting " +
        "WHERE time_stamp >= :startOfDay " +
        "AND time_stamp < :endOfDay",
        nativeQuery = true)
    long countTotalUserConnectionsToday(
        @Param("startOfDay") long startOfDay,
        @Param("endOfDay") long endOfDay
    );

    // Query to get the total bandwidth consumption for today
    @Query(value = "SELECT COALESCE(SUM(acctinputoctets + acctoutputoctets), 0) " +
        "FROM accounting " +
        "WHERE time_stamp >= :startOfDay " +
        "AND time_stamp < :endOfDay",
        nativeQuery = true)
    long totalBandwidthConsumptionToday(
        @Param("startOfDay") long startOfDay,
        @Param("endOfDay") long endOfDay
    );

    // Query to get average connection time
    @Query(value = "SELECT AVG(acctsessiontime) " +
        "FROM accounting " +
        "WHERE acctstatustype = 'Stop' " +
        "AND acctsessiontime > 0",
    nativeQuery = true)
    Double findAverageConnectionTime();

    // Query to get the average bandwidth per connection
    @Query(value = "SELECT AVG((a.acctinputoctets + a.acctoutputoctets) / a.acctsessiontime) " +
        "FROM accounting a " +
        "WHERE a.acctstatustype = 'Stop' " +
        "AND a.acctsessiontime > 0",
    nativeQuery = true)
    Double findAverageBandwidthPerConnection();

    // Query to get the list of access points
    // NOTE: change this to nasidentifier if the column is already implemented (since the nasidentifier accurately identifies distinct access points)
    @Query(value = "SELECT DISTINCT called_station_id " +
        "FROM accounting ",
        nativeQuery = true)
    List<String> findAllAccessPoints();

    // Query to get number of currently connected users per access point
    // NOTE: change this to nasidentifier if the column is already implemented (since the nasidentifier accurately identifies distinct access points)
    @Query(value = "SELECT a1.called_station_id, COUNT(DISTINCT a1.username) as user_count " +
        "FROM accounting a1 " +
        "WHERE a1.acctstatustype = 'Alive' " +
        "AND NOT EXISTS (" +
        "   SELECT 1 FROM accounting a2 " +
        "   WHERE a2.acctstatustype = 'Stop' " +
        "   AND a2.username = a1.username " +
        "   AND a2.time_stamp >= a1.time_stamp " +
        ")" +
        "GROUP BY a1.called_station_id",
        nativeQuery = true)
    List<Object[]> countCurrentlyConnectedUsersPerAP();

    // Query to get list of currently connected users per access point
    @Query(value = "SELECT a1.called_station_id, a1.username, a1.acctinputoctets, a1.acctoutputoctets, a1.nasport, a1.calling_station_id, a1.time_stamp " +
        "FROM accounting a1 " +
        "INNER JOIN ( " +
        "   SELECT username, MAX(time_stamp) AS latest_time " +
        "   FROM accounting " +
        "   WHERE acctstatustype = 'Alive' " +
        "   GROUP BY username " +
        ") latest_alive " +
        // "ON a1.calling_station_id = latest_alive.calling_station_id " +
        // "AND a1.username = latest_alive.username " +
        "ON a1.username = latest_alive.username " +
        "AND a1.time_stamp = latest_alive.latest_time " +
        "WHERE a1.acctstatustype = 'Alive' " +
        "AND NOT EXISTS ( " +
        "   SELECT 1 FROM accounting a2 " +
        "   WHERE a2.acctstatustype = 'Stop' " +
        // "   AND a2.calling_station_id = a1.calling_station_id " +
        "   AND a2.username = a1.username " +
        "   AND a2.time_stamp >= a1.time_stamp " +
        ") " +
        // "ORDER BY a1.called_station_id, a1.time_stamp DESC",
        "ORDER BY a1.username, a1.time_stamp DESC",
        nativeQuery = true)
    List<Object[]> findCurrentlyConnectedUsersPerAP();
}
