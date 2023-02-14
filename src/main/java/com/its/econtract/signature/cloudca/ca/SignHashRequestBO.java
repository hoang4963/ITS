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
public class SignHashRequestBO {
    private String credentialID;
    public String SAD;
    private List<DocumentBO> documents;
    private List<String> hash;
    private String hashAlgo;
    private String signAlgo;
}
