package com.acs_tr069.test_tr069.radius.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "subscribers", schema = "public") 
public class Subscribers {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "session_limit")
    private Integer sessionLimit;

    @Column(name = "remaining_session_time")
    private Integer remainingSessionTime;

    @Column(name = "bytes_limit")
    private Long bytesLimit;

    @Column(name = "remaining_bytes")
    private Long remainingBytes;

    @Column(name = "lname")
    private String lname;

    @Column(name = "fname")
    private String fname;

    @Column(name = "mname")
    private String mname;

    @Column(name = "ename")
    private String ename;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_no")
    private String phoneNo;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "gender")
    private String gender;

    @Column(name = "status")
    private Integer status;

    @Column(name = "registration_date")
    private String registrationDate;
}
