package com.acs_tr069.test_tr069.radius.repository;


import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acs_tr069.test_tr069.radius.entity.Routers;

public interface RoutersRepository extends JpaRepository<Routers, String> {

    Optional<Routers> findByRouterId(String routerId);

    Optional<Routers> findBySerialNo(String serialNo);
    
    Optional<Routers> findFirstByMacAddress(String mac);

    Optional<Routers> findFirstBySerialNoAndMacAddress(String serialNo, String mac);

    List<Routers> findAllBySerialNoInAndMacAddressIn(Set<String> serials, Set<String> macs);
}
