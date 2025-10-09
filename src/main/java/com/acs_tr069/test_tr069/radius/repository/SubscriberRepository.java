package com.acs_tr069.test_tr069.radius.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.acs_tr069.test_tr069.radius.entity.Subscribers;

public interface SubscriberRepository extends CrudRepository<Subscribers, Long> {

  @Query(value = "SELECT * from subscribers", nativeQuery = true)
  List<Subscribers> findAll();

  @Query(value = "SELECT COUNT(*) FROM subscribers", nativeQuery = true)
  long countRegisteredUsers();

  @Modifying
  @Transactional
  @Query(value = "UPDATE subscribers SET remaining_bytes = remaining_bytes + ?1 WHERE username =?2", nativeQuery = true)
  void addRemainingBytes(Long additionalBytes, String username);

  @Modifying
  @Transactional
  @Query(value = "UPDATE subscribers SET remaining_session_time = remaining_session_time + ?1 WHERE username =?2", nativeQuery = true)
  void addRemainingTime(Long additionalTime, String username);

  Optional<Subscribers> findByUsername(String username);
}
