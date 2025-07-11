package com.acs_tr069.test_tr069.radius.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.acs_tr069.test_tr069.radius.entity.AllowedNasMacAddress;

@Repository
public interface AllowedNasMacAddressRepository extends JpaRepository<AllowedNasMacAddress, Long> {
    @Query(value = "SELECT COUNT(DISTINCT calling_station_id) " +
            "FROM allowed_nas_mac_address", nativeQuery = true)
    long countTotalAllowedNasMacAddress();

    @Query(value = "SELECT DISTINCT called_station_id " +
        "FROM allowed_nas_mac_address",
        nativeQuery = true)
    List<String> findAllAllowedNasMacAddress();

    Optional<AllowedNasMacAddress> findByCalledStationId(String calledStationId);
}
