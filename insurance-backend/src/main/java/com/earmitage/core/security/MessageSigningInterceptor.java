package com.earmitage.core.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageSigningInterceptor implements ClientHttpRequestInterceptor {

    private final CertProperties certProperties;

    public MessageSigningInterceptor(final CertProperties certProperties) {
        this.certProperties = certProperties;
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
            final ClientHttpRequestExecution execution) throws IOException {
        // log.info("request.getURI().toString() {}", request.getURI().toString());

        try {
            String domain = getDomainName(request.getURI().toString());
            // log.info("domain {}", domain);
            String apiPath = request.getURI().toString().split(domain)[1];
            // log.info("split on domain {}", apiPath);

            final String[] urlParts = request.getURI().toString().split(domain);
            final String uri = urlParts[1];

            String[] paths = apiPath.split("/");
            final String context = paths[paths.length - 1];

            final HttpHeaders headers = request.getHeaders();
            // final String content = "/" + context + uri + new String(body, "UTF-8");
            final String content = uri + new String(body, "UTF-8");
            log.info("Hashing body: {}", content);
            final byte[] signedMessage = signMessage(content.getBytes(StandardCharsets.UTF_8));
            final String encodedSignature = Base64.getEncoder().encodeToString(signedMessage);
            headers.add("signature", encodedSignature);
            headers.add("client", "papss-portal-backend");

        } catch (Exception e) {
            log.error("Error adding signature", e);
        }

        return execution.execute(request, body);
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getAuthority();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    private byte[] signMessage(final byte[] messageBytes) {
        try {
            final Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(loadSenderPrivateKey());
            signature.update(messageBytes);
            return signature.sign();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

    }

    protected byte[] signMessageAlternative(final byte[] messageBytes) throws Exception {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] messageHash = md.digest(messageBytes);
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, loadSenderPrivateKey());
            return cipher.doFinal(messageHash);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private PrivateKey loadSenderPrivateKey() throws Exception {
        final KeyStore keyStore = KeyStore.getInstance(certProperties.getKeyType());
        InputStream is = MessageSigningInterceptor.class.getResourceAsStream(certProperties.getKeyStoreFile());
        keyStore.load(is, certProperties.getKeyPassword().toCharArray());
        return (PrivateKey) keyStore.getKey(certProperties.getKeyAlias(),
                certProperties.getKeyPassword().toCharArray());

    }
}
