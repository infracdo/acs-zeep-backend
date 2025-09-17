package com.acs_tr069.test_tr069.radius.entity;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "ap_accounting", schema = "public")
public class ApAccounting {

    @Id
    @Column(name = "called_station_id")
    private String calledStationId;

	@Column(name = "serial_um")
    private String serialNum;

	@Column(name = "totalinputoctets")
    private Long totalInputOctets;

    @Column(name = "totaloutputoctets")
    private Long totalOutputOctets;
	
    @Column(name = "totalsessiontime")
    private Long totalSessionTime;

	@Column(name = "created_on", columnDefinition = "timestamptz")
    private OffsetDateTime createdOn;

    @Column(name = "last_updated", columnDefinition = "timestamptz")
    private OffsetDateTime lastUpdated;

    @Column(name = "last_reset", columnDefinition = "timestamptz")
    private OffsetDateTime lastReset;
}
