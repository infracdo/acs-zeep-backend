package com.acs_tr069.test_tr069.Repo;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//import org.springframework.data.repository.Repository;
//import org.springframework.data.repository.PagingAndSortingRepository;
//import net.bytebuddy.dynamic.DynamicType.Builder.FieldDefinition.Optional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.acs_tr069.test_tr069.Entity.device;


@Repository
public interface device_frontendRepository extends CrudRepository<device, Long>{
   @Query("SELECT d FROM device d WHERE d.serial_number=?1")
   List<device> findBySerialNum(String serial_number);
   
   @Query(value = "SELECT * FROM device WHERE serial_number IN :serialNumbers", nativeQuery = true)
   List<device> findAllBySerialNumbers(@Param("serialNumbers") Set<String> serialNumbers);

   @Query("SELECT d FROM device d WHERE d.parent=?1")
   List<device> findByGroup(String parent);

   @Query("SELECT d FROM device d WHERE d.serial_number=?1")
   device getBySerialNum(String serial_number);

   @Query("SELECT d.device_name FROM device d WHERE d.mac_address=?1")
   String getDeviceNameByMac(String mac_address);

   @Query("SELECT d FROM device d WHERE LOWER(d.parent) LIKE %?1% AND LOWER(d.status) =?2")
   List<device> getDevicesByParentAndStatus(String keyword, String status);

   @Query("SELECT d FROM device d WHERE LOWER(d.parent) LIKE %?1% ")
   List<device> getAllDevicesByParent(String keyword);

   @Query("SELECT d FROM device d WHERE LOWER(d.status) ='online' AND LOWER(d.parent) <> 'unassigned'")
   List<device> getAllOnlineRegisteredDevices();

}
