package com.acs_tr069.test_tr069.radius.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.acs_tr069.test_tr069.radius.entity.Subscribers;

public interface SubscriberRepository extends CrudRepository<Subscribers, Long> {

  @Query(value = "SELECT * from subscribers", nativeQuery = true)
  List<Subscribers> findAll();

  Optional<Subscribers> findByUsername(String username);
}