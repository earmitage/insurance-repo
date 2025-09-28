package com.earmitage.core.security.payments;

import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class PayFastValidator {

    public static boolean isRequestFromValidPayFastIP(final String referrer, final List<String> domains) {
        try {
            // Resolve all IPs from valid hosts
            Set<String> validIps = new HashSet<>();
            for (String host : domains) {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                for (InetAddress addr : addresses) {
                    validIps.add(addr.getHostAddress());
                }
            }

            // Extract referrer domain

            if (referrer == null || referrer.isEmpty()) {
                return false; // No referer to check
            }

            URI uri = new URI(referrer);
            String referrerHost = uri.getHost();
            if (referrerHost == null || referrerHost.isEmpty()) {
                return false;
            }

            InetAddress referrerAddress = InetAddress.getByName(referrerHost);
            String referrerIp = referrerAddress.getHostAddress();

            return validIps.contains(referrerIp);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isValidSignatureFromForm(MultiValueMap<String, String> formData, String passphrase) {
        Map<String, String> flatMap = new HashMap<>();
        formData.forEach((key, values) -> {
            if (!values.isEmpty()) {
                flatMap.put(key, values.get(0));
            }
        });

        SortedMap<String, String> sortedData = new TreeMap<>(flatMap);
        StringBuilder paramString = new StringBuilder();

        for (Map.Entry<String, String> entry : sortedData.entrySet()) {
            if (!entry.getKey().equals("signature")) {
                paramString.append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
            }
        }

        if (passphrase != null && !passphrase.isEmpty()) {
            paramString.append("passphrase=").append(URLEncoder.encode(passphrase, StandardCharsets.UTF_8));
        } else {
            paramString.setLength(paramString.length() - 1); // remove trailing '&'
        }

        String computedSignature = md5(paramString.toString());
        return computedSignature.equalsIgnoreCase(flatMap.get("signature"));
    }
    
    public static boolean isValidServerConfirmation(final String pfParamString, final String url) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(pfParamString, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return "VALID".equalsIgnoreCase(response.getBody());
        } catch (Exception e) {
            e.printStackTrace(); // You may want to log this
            return false;
        }
    }
    
    public static String buildPfParamString(MultiValueMap<String, String> formData, String passphrase) {
        SortedMap<String, String> sortedData = new TreeMap<>();
        formData.forEach((key, values) -> {
            if (!values.isEmpty() && !key.equals("signature")) {
                sortedData.put(key, values.get(0));
            }
        });

        StringBuilder paramString = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedData.entrySet()) {
            paramString.append(entry.getKey())
                       .append("=")
                       .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                       .append("&");
        }

        if (passphrase != null && !passphrase.isEmpty()) {
            paramString.append("passphrase=").append(URLEncoder.encode(passphrase, StandardCharsets.UTF_8));
        } else if (paramString.length() > 0) {
            paramString.setLength(paramString.length() - 1); // Remove trailing '&'
        }

        return paramString.toString();
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 hashing failed", e);
        }
    }
}
