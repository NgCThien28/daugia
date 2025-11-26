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
import java.math.BigDecimal;
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

    public void sendAuctionEndEmail(Taikhoan tk, Phiendaugia phiendaugia, String lydo) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(tk.getEmail());
        String subject = "Thông báo kết thúc phiên đấu giá " + phiendaugia.getMaphiendg();
        helper.setSubject(subject);
        String name = String.join(" ",
                tk.getHo() == null ? "" : tk.getHo(),
                tk.getTenlot() == null ? "" : tk.getTenlot(),
                tk.getTen() == null ? "" : tk.getTen()
        ).trim();
        if (name.isEmpty()) name = "Bạn";

        String templatePath = "templates/auction-end-email.html";
        ClassPathResource resource = new ClassPathResource(templatePath);
        String html = Files.readString(resource.getFile().toPath());

        // Format dữ liệu
        Locale vi = new Locale("vi", "VN");
        SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm", vi);
        NumberFormat numberFmt = NumberFormat.getNumberInstance(vi);
        String thoigiankt = phiendaugia.getThoigiankt() == null ? "-" : dtf.format(phiendaugia.getThoigiankt());
        String giacaonhat = phiendaugia.getGiacaonhatdatduoc() == null
                ? "-"
                : numberFmt.format(phiendaugia.getGiacaonhatdatduoc());

        html = html.replace("{{name}}", name);
        html = html.replace("{{tensp}}", phiendaugia.getSanPham().getTensp());
        html = html.replace("{{maphien}}", phiendaugia.getMaphiendg());
        html = html.replace("{{thoigiankt}}", thoigiankt);
        html = html.replace("{{trangthai}}", phiendaugia.getTrangthai().getValue());
        html = html.replace("{{giacaonhat}}", giacaonhat);
        html = html.replace("{{lydo}}", lydo);

        helper.setText(html, true); // true = HTML
        mailSender.send(message);
    }

    public void sendAuctionWinEmail(Taikhoan winner, Phiendaugia phiendaugia, BigDecimal giaThang) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(winner.getEmail());
        helper.setSubject("Chúc mừng! Bạn đã thắng phiên đấu giá " + phiendaugia.getMaphiendg());

        String name = String.join(" ",
                winner.getHo() == null ? "" : winner.getHo(),
                winner.getTenlot() == null ? "" : winner.getTenlot(),
                winner.getTen() == null ? "" : winner.getTen()
        ).trim();
        if (name.isEmpty()) name = "Bạn";

        String templatePath = "templates/auction-win-email.html";
        ClassPathResource resource = new ClassPathResource(templatePath);
        String html = Files.readString(resource.getFile().toPath());

        // Format dữ liệu
        Locale vi = new Locale("vi", "VN");
        SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm", vi);
        NumberFormat numberFmt = NumberFormat.getNumberInstance(vi);

        // Thời hạn thanh toán: 7 ngày từ bây giờ
        long sevenDaysMs = 7L * 24 * 60 * 60 * 1000;
        String thoigiandue = dtf.format(new java.util.Date(System.currentTimeMillis() + sevenDaysMs));

        String giaThangFormatted = giaThang == null ? "-" : numberFmt.format(giaThang);

        html = html.replace("{{name}}", name);
        html = html.replace("{{tensp}}", phiendaugia.getSanPham().getTensp());
        html = html.replace("{{maphien}}", phiendaugia.getMaphiendg());
        html = html.replace("{{giaThang}}", giaThangFormatted);
        html = html.replace("{{thoigiandue}}", thoigiandue);

        helper.setText(html, true); // true = HTML
        mailSender.send(message);
    }

    // Thêm method gửi email cho người thắng thứ 2
    public void sendAuctionTransferEmail(Taikhoan newWinner, Phiendaugia phiendaugia, BigDecimal giaThang) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(newWinner.getEmail());
        helper.setSubject("Quyền thắng phiên đấu giá " + phiendaugia.getMaphiendg() + " đã được chuyển cho bạn");

        String name = String.join(" ",
                newWinner.getHo() == null ? "" : newWinner.getHo(),
                newWinner.getTenlot() == null ? "" : newWinner.getTenlot(),
                newWinner.getTen() == null ? "" : newWinner.getTen()
        ).trim();
        if (name.isEmpty()) name = "Bạn";

        String templatePath = "templates/auction-transfer-email.html";
        ClassPathResource resource = new ClassPathResource(templatePath);
        String html = Files.readString(resource.getFile().toPath());

        // Format dữ liệu
        Locale vi = new Locale("vi", "VN");
        SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm", vi);
        NumberFormat numberFmt = NumberFormat.getNumberInstance(vi);

        // Thời hạn thanh toán: 7 ngày từ bây giờ
        long sevenDaysMs = 7L * 24 * 60 * 60 * 1000;
        String thoigiandue = dtf.format(new java.util.Date(System.currentTimeMillis() + sevenDaysMs));

        String giaThangFormatted = giaThang == null ? "-" : numberFmt.format(giaThang);

        html = html.replace("{{name}}", name);
        html = html.replace("{{tensp}}", phiendaugia.getSanPham().getTensp());
        html = html.replace("{{maphien}}", phiendaugia.getMaphiendg());
        html = html.replace("{{giaThang}}", giaThangFormatted);
        html = html.replace("{{thoigiandue}}", thoigiandue);

        helper.setText(html, true); // true = HTML
        mailSender.send(message);
    }

    public void sendAuctionCancelWinEmail(Taikhoan oldWinner, Phiendaugia phiendaugia) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(oldWinner.getEmail());
        helper.setSubject("Quyền thắng phiên đấu giá " + phiendaugia.getMaphiendg() + " đã bị hủy");

        String name = String.join(" ",
                oldWinner.getHo() == null ? "" : oldWinner.getHo(),
                oldWinner.getTenlot() == null ? "" : oldWinner.getTenlot(),
                oldWinner.getTen() == null ? "" : oldWinner.getTen()
        ).trim();
        if (name.isEmpty()) name = "Bạn";

        String templatePath = "templates/auction-cancel-win-email.html";
        ClassPathResource resource = new ClassPathResource(templatePath);
        String html = Files.readString(resource.getFile().toPath());

        html = html.replace("{{name}}", name);
        html = html.replace("{{tensp}}", phiendaugia.getSanPham().getTensp());
        html = html.replace("{{maphien}}", phiendaugia.getMaphiendg());

        helper.setText(html, true); // true = HTML
        mailSender.send(message);
    }
}

