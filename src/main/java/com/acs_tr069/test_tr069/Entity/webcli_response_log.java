package com.acs_tr069.test_tr069.Entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class webcli_response_log {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id;
    private String device_sn;
    private byte[] CommandOutput;
    private byte[] CommandUsed;
    
    public Long get_Id(){
        return id;
    }
    public String get_device_sn(){
        return device_sn;
    }
    public byte[] get_CommandOutput(){
        return CommandOutput;
    }
    public byte[] get_CommandUsed(){
        return CommandUsed;
    }

    public void set_device_sn(String device_sn){
        this.device_sn = device_sn;
    }
    public void set_CommandOutput(byte[] CommandOutput){
        this.CommandOutput = CommandOutput;
    }
    public void set_CommandUsed(byte[] CommandUsed){
        this.CommandUsed = CommandUsed;
    }

}
