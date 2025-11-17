package com.example.daugia.service;

import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Taikhoan;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(Taikhoan tk, String link) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(tk.getEmail());
        helper.setSubject("Xác thực tài khoản - Đấu Giá STU");
        String name = String.join(" ",
                tk.getHo() == null ? "" : tk.getHo(),
                tk.getTenlot() == null ? "" : tk.getTenlot(),
                tk.getTen() == null ? "" : tk.getTen()
        ).trim();
        if (name.isEmpty()) name = "Bạn";
        // Đọc file template HTML
        String templatePath = "templates/verification-email.html";
        ClassPathResource resource = new ClassPathResource(templatePath);
        String html = Files.readString(resource.getFile().toPath());

        // Thay {{link}} bằng link thực tế
        html = html.replace("{{link}}", link);
        html= html.replace("{{name}}",name);
        helper.setText(html, true); // true = HTML
        mailSender.send(message);
    }

    public void sendAuctionBeginEmail(Taikhoan tk, Phiendaugia phiendaugia) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(tk.getEmail());
        helper.setSubject("Thông báo phiên đấu giá "+phiendaugia.getMaphiendg()+" sắp bắt đầu");
        String name = String.join(" ",
                tk.getHo() == null ? "" : tk.getHo(),
                tk.getTenlot() == null ? "" : tk.getTenlot(),
                tk.getTen() == null ? "" : tk.getTen()
        ).trim();
        if (name.isEmpty()) name = "Bạn";

        String templatePath = "templates/auction-begin-email.html";
        ClassPathResource resource = new ClassPathResource(templatePath);
        String html = Files.readString(resource.getFile().toPath());
        //format
        Locale vi = new Locale("vi", "VN");
        SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm", vi);
        NumberFormat numberFmt = NumberFormat.getNumberInstance(vi);
        String thoigianbd = phiendaugia.getThoigianbd() == null ? "-" : dtf.format(phiendaugia.getThoigianbd());
        String thoigiankt = phiendaugia.getThoigiankt() == null ? "-" : dtf.format(phiendaugia.getThoigiankt());
        String giakhoidiem = phiendaugia.getGiakhoidiem() == null
                ? "-"
                : numberFmt.format(phiendaugia.getGiakhoidiem());

        html = html.replace("{{name}}",name);
        html = html.replace("{{tensp}}",phiendaugia.getSanPham().getTensp());
        html = html.replace("{{maphien}}",phiendaugia.getMaphiendg());
        html = html.replace("{{thoigianbd}}",thoigianbd);
        html = html.replace("{{thoigiankt}}",thoigiankt);
        html = html.replace("{{giakhoidiem}}",giakhoidiem);

        helper.setText(html, true); // true = HTML
        mailSender.send(message);
    }
}

