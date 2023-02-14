/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.its.econtract.signature.cloudca.ca;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CertBO {

    private String status;
    private List<String> certificates;
    private String issuerDN;
    private String serialNumber;
    private String subjectDN;
    private String validFrom;
    private String validTo;

}
