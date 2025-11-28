package com.octal.fsm.entities;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "multi_user_device_details")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class MultiUserDeviceDetails extends AbstractPersistable {

    private String deviceType;
    private String appVersion;
    private String deviceToken;
    private String deviceId;
}
