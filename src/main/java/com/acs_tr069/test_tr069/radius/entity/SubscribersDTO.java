package com.acs_tr069.test_tr069.radius.entity;

import java.time.LocalDate;
import lombok.Data;

@Data
public class SubscribersDTO {

    private Long id;
    private String username;
    private String password;
    private Integer sessionLimit;
    private Integer remainingSessionTime;
    private Double bytesLimit;
    private Double remainingBytes;
    private String lname;
    private String fname;
    private String mname;
    private String ename;
    private String address;
    private String phoneNo;
    private LocalDate birthdate;
    private String gender;
    private Integer status;
    private Double maxUprate;
    private Double maxDownrate;
}