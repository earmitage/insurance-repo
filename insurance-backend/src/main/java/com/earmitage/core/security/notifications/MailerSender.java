package com.earmitage.core.security.notifications;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Attachment;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailerSender {

    @Autowired
    private Configuration freemarkerConfig;

    @Autowired
    private AppProperties appProperties;

    public void sendSubscriptionConfirmation(String toEmail, Map<String, Object> model) throws Exception {
        Template template = freemarkerConfig.getTemplate("subscription-confirmation.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(model, stringWriter);
        String htmlContent = stringWriter.toString();

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(pdfStream);
        builder.run();

        sendEmail(toEmail, "Subscription Confirmation", "Please see attached subscription confirmation.", pdfStream.toByteArray());
    }

    public void sendEmail(final String toEmail, final String subject, final String fileName, byte[] attachment) {

       

        Email email = new Email();

        email.setFrom(appProperties.getNotifications().getNotificationsAppName(),
                appProperties.getNotifications().getFromEmail());
        email.addRecipient(toEmail, toEmail);

        email.setSubject(subject);

        email.setPlain("Please see attached subscription confirmation.");
        email.setHtml("Please see attached subscription confirmation.");
        Attachment file = new Attachment();
        file.content = Base64.getEncoder().encodeToString(attachment);
        file.filename = fileName;
        email.attachments.add(file);

        MailerSend ms = new MailerSend();

        ms.setToken(appProperties.getNotifications().getMailerSendApi());

        try {

            MailerSendResponse response = ms.emails().send(email);
            log.info(response.messageId);

        } catch (MailerSendException e) {
            e.printStackTrace();
        }
    }
}
