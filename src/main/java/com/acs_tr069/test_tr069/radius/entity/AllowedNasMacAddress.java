package com.acs_tr069.test_tr069.radius.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "allowed_nas_mac_address", schema = "public") 
public class AllowedNasMacAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "called_station_id")
    private String calledStationId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
