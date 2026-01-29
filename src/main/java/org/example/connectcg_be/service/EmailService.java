package org.example.connectcg_be.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name:Connect App}")
    private String fromName;

    public void sendHtmlMessage(String to, String subject, String htmlBody) {
        logger.info("=== SENDING EMAIL VIA WEB API ===");
        logger.info("To: {}", to);
        logger.info("From: {} <{}>", fromName, fromEmail);
        logger.info("Subject: {}", subject);

        // Validate API Key
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            logger.error("❌ SendGrid API Key is not configured!");
            throw new RuntimeException("SendGrid API Key is missing");
        }

        // Build email
        Email from = new Email(fromEmail, fromName);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, toEmail, content);

        // Send via SendGrid API
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            logger.info("Calling SendGrid API...");
            Response response = sg.api(request);

            logger.info("Response Status: {}", response.getStatusCode());
            logger.debug("Response Body: {}", response.getBody());
            logger.debug("Response Headers: {}", response.getHeaders());

            // Check response
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("✅ Email sent successfully via Web API!");
                logger.info("=== EMAIL SENT ===");
            } else {
                logger.error("❌ Failed to send email");
                logger.error("Status: {}", response.getStatusCode());
                logger.error("Body: {}", response.getBody());
                throw new RuntimeException("SendGrid error: " + response.getStatusCode());
            }

        } catch (IOException e) {
            logger.error("❌ IOException while calling SendGrid API");
            logger.error("Error: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage(), e);
        }
    }
}