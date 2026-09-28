package com.fashionpin.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SSLCommerzValidatorResponseDTO {

    private String status;

    private String tran_id;

    private String val_id;

    private String amount;

    private String currency;

    private String bank_tran_id;

    private String card_type;

    private String card_no;

    private String card_issuer;

    private String card_brand;

    private String card_sub_brand;

    private String card_issuer_country;

    private String card_issuer_country_code;

    private String store_id;

    private String verify_sign;

    private String verify_key;

    private String error;

    private String risk_level;

    private String risk_title;
}
