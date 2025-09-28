package com.earmitage.core.security.payments;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class ITNPayload {

    @JsonProperty("m_payment_id")
    private String mPaymentId;

    @JsonProperty("pf_payment_id")
    private String pfPaymentId;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("item_description")
    private String itemDescription;

    @JsonProperty("amount_gross")
    private BigDecimal amountGross;

    @JsonProperty("amount_fee")
    private BigDecimal amountFee;

    @JsonProperty("amount_net")
    private BigDecimal amountNet;

    @JsonProperty("custom_str1")
    private String customStr1;

    @JsonProperty("custom_str2")
    private String customStr2;

    @JsonProperty("custom_str3")
    private String customStr3;

    @JsonProperty("custom_str4")
    private String customStr4;

    @JsonProperty("custom_str5")
    private String customStr5;

    @JsonProperty("custom_int1")
    private String customInt1;

    @JsonProperty("custom_int2")
    private String customInt2;

    @JsonProperty("custom_int3")
    private String customInt3;

    @JsonProperty("custom_int4")
    private String customInt4;

    @JsonProperty("custom_int5")
    private String customInt5;

    @JsonProperty("name_first")
    private String nameFirst;

    @JsonProperty("name_last")
    private String nameLast;

    @JsonProperty("email_address")
    private String emailAddress;

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("signature")
    private String signature;
}
