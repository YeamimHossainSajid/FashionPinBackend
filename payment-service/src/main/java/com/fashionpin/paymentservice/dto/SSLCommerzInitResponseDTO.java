package com.fashionpin.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SSLCommerzInitResponseDTO {

    private String status;

    private String failedreason;

    private String sessionkey;

    @JsonProperty("GatewayPageURL")
    private String gatewayPageURL;

    @JsonProperty("redirectGatewayURL")
    private String redirectGatewayURL;

    @JsonProperty("storeBanner")
    private String storeBanner;

    @JsonProperty("storeLogo")
    private String storeLogo;

    @JsonProperty("desc")
    private String desc;
}
