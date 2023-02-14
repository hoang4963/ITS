package com.its.econtract.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailTemplateDto {
    private int id;
    private int type;
    private String system_template;
    private String company_template;
    private String description;
    private int status;
}
