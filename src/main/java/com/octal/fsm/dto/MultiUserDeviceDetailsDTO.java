package com.octal.fsm.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class MultiUserDeviceDetailsDTO {

    private String deviceType;
    private String appVersion;
    private String deviceToken;
    private String deviceData;
    private String userId;
    private Boolean pushEnabled;
}
