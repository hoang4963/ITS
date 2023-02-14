/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.its.econtract.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author quangdt
 */
@Getter
@Setter
@ToString
public class FaceAntiSpoofStatus {
    @JsonProperty("fake_score")
    double fakeScore;

    @JsonProperty("fake_type")
    String fakeType;

    String status;

    @JsonProperty("fake_code")
    String fakeCode;
}

