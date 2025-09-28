package com.earmitage.core.security.payments;

import java.math.BigDecimal;

import org.springframework.util.MultiValueMap;

public class PayFastMapper {
    public static ITNPayload mapToPayload(MultiValueMap<String, String> formData) {
        ITNPayload payload = new ITNPayload();

        payload.setMPaymentId(formData.getFirst("m_payment_id"));
        payload.setPfPaymentId(formData.getFirst("pf_payment_id"));
        payload.setPaymentStatus(formData.getFirst("payment_status"));
        payload.setItemName(formData.getFirst("item_name"));
        payload.setItemDescription(formData.getFirst("item_description"));
        payload.setAmountGross(new BigDecimal(formData.getFirst("amount_gross")));
        payload.setAmountFee(new BigDecimal(formData.getFirst("amount_fee")));
        payload.setAmountNet(new BigDecimal(formData.getFirst("amount_net")));
        payload.setCustomStr1(formData.getFirst("custom_str1"));
        payload.setCustomStr2(formData.getFirst("custom_str2"));
        payload.setCustomStr3(formData.getFirst("custom_str3"));
        payload.setCustomStr4(formData.getFirst("custom_str4"));
        payload.setCustomStr5(formData.getFirst("custom_str5"));
        payload.setCustomInt1(formData.getFirst("custom_int1"));
        payload.setCustomInt2(formData.getFirst("custom_int2"));
        payload.setCustomInt3(formData.getFirst("custom_int3"));
        payload.setCustomInt4(formData.getFirst("custom_int4"));
        payload.setCustomInt5(formData.getFirst("custom_int5"));
        payload.setNameFirst(formData.getFirst("name_first"));
        payload.setNameLast(formData.getFirst("name_last"));
        payload.setEmailAddress(formData.getFirst("email_address"));
        payload.setMerchantId(formData.getFirst("merchant_id"));
        payload.setSignature(formData.getFirst("signature"));

        return payload;
    }
}
