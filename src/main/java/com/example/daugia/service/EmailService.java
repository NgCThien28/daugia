package com.example.daugia.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String link) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Xác thực tài khoản - Đấu Giá STU");

        // Đọc file template HTML
        String templatePath = "templates/verification-email.html";
        ClassPathResource resource = new ClassPathResource(templatePath);
        String html = Files.readString(resource.getFile().toPath());

        // Thay {{link}} bằng link thực tế
        html = html.replace("{{link}}", link);

        helper.setText(html, true); // true = HTML

        mailSender.send(message);
        System.out.println("📧 Đã gửi email xác thực tới: " + to);
    }

//    public void send
}

