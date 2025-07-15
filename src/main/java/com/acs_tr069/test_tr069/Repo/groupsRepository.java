package com.acs_tr069.test_tr069.Repo;

import java.util.List;
import java.util.Optional;

import com.acs_tr069.test_tr069.Entity.groups;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface groupsRepository extends CrudRepository<groups, Long> {
    List<groups> findByParent(String parent);

    @Query(value = "SELECT g.location FROM `groups` g JOIN (SELECT parent, group_name FROM `groups` WHERE parent =?1 AND group_name =?2 GROUP BY parent, group_name HAVING COUNT(*) > 1 ) dup ON g.parent = dup.parent AND g.group_name = dup.group_name ORDER BY g.id LIMIT 1", nativeQuery = true)
    Optional<groups> findByParentAndGroup(String parent, String groupName);

}
