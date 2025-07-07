package com.acs_tr069.test_tr069.radius.entity;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "dummy table") // change file name to table name for acs-provisioned aps
public class Dummy {
    
    @Id
    @Column(name = "calling_station_id")
    private String callingStationId;

    @Column(name = "username")
    private String username;

    @Column(name = "time_stamp")
    private Long timestamp;

    @Column(name = "acctstatustype")
    private String acctStatusType;

    @Column(name = "mac")
    private String mac;

    @Column(name = "created_at", columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;

}
