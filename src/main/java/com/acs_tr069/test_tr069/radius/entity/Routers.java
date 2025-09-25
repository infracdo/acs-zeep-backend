package com.acs_tr069.test_tr069.radius.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "\"Routers\"", schema = "public")
public class Routers {

    @Id
    @Column(name = "router_id")
    private String routerId;

	@Column(name = "serial_no")
    private String serialNo;

    @Column(name = "mac_address")
    private String macAddress;

	@Column(name = "long")
	private Double longitude;

	@Column(name = "lat")
    private Double latitude;

}
