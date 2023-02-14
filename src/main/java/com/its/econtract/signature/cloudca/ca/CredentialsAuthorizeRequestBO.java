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
public class CredentialsAuthorizeRequestBO {
    private String description;
    private String credentialID;
    private int numSignatures;
    private List<DocumentBO> documents;
    private List<String> hash;
}
