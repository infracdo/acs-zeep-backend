package com.acs_tr069.test_tr069.Repo;

import java.util.List;
import java.util.Optional;

import com.acs_tr069.test_tr069.Entity.groups;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface groupsRepository extends CrudRepository<groups, Long> {
    List<groups> findByParent(String parent);
    
    @Query(value = "SELECT * FROM `groups` WHERE parent =?1 AND group_name =?2 ORDER BY id LIMIT 1", nativeQuery = true)
    Optional<groups> findByParentAndGroup(String parent, String groupName);
}
