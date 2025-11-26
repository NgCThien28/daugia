package com.example.daugia.service.storage;

import com.example.daugia.config.SupabaseStorageProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
public class SupabaseStorageService {

    private final SupabaseStorageProperties props;
    private final HttpClient http = HttpClient.newHttpClient();

    public SupabaseStorageService(SupabaseStorageProperties props) {
        this.props = props;
    }

    private void validate(MultipartFile file, int maxMb) {
        if (file.isEmpty()) throw new IllegalArgumentException("File rỗng");
        long maxBytes = (long) maxMb * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("Kích thước vượt quá " + maxMb + "MB");
        }
        String ct = Optional.ofNullable(file.getContentType()).orElse("");
        if (!(ct.equals(MediaType.IMAGE_JPEG_VALUE) ||
                ct.equals(MediaType.IMAGE_PNG_VALUE)  ||
                ct.equals("image/webp"))) {
            throw new IllegalArgumentException("Chỉ chấp nhận ảnh jpeg/png/webp");
        }
    }

    private String sanitize(String original) {
        String base = Normalizer.normalize(original, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9\\.\\-]", "-")
                .toLowerCase();
        int dot = base.lastIndexOf('.');
        String ext = "";
        if (dot > 0 && dot < base.length() - 1) {
            ext = base.substring(dot + 1);
            base = base.substring(0, dot);
        }
        return base + "-" + Instant.now().toEpochMilli() + (ext.isBlank() ? "" : "." + ext);
    }

    private String buildKey(String bucket, String objectPath) {
        return bucket + "/" + objectPath; // dạng <bucket>/<path>
    }

    public record UploadResult(String key, String url) {}

    public UploadResult uploadProductImage(String masp, MultipartFile file, int maxMb) throws Exception {
        validate(file, maxMb);
        String safeName = sanitize(Objects.requireNonNull(file.getOriginalFilename()));
        String bucket = props.getBucketProducts();
        String objectPath = "product/" + masp + "/" + safeName;

        // POST /storage/v1/object/{bucket}/{objectName}
        String api = props.storageApiBase() + "/" + bucket + "/" + objectPath;

        HttpRequest req = HttpRequest.newBuilder(URI.create(api))
                .header("Authorization", "Bearer " + props.getServiceKey()) // Service Role
                .header("Content-Type", Optional.ofNullable(file.getContentType()).orElse("application/octet-stream"))
                .header("x-upsert", "true")
                .POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            String key = buildKey(bucket, objectPath);
            String url = props.buildPublicUrl(key); // bucket Public → URL public
            return new UploadResult(key, url);
        } else {
            throw new RuntimeException("Upload Supabase thất bại: " + resp.statusCode() + " - " + resp.body());
        }
    }

    public void deleteObject(String key) throws Exception {
        String api = props.storageApiBase() + "/" + key; // DELETE /object/<bucket>/<path>
        HttpRequest req = HttpRequest.newBuilder(URI.create(api))
                .header("Authorization", "Bearer " + props.getServiceKey())
                .DELETE()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) return;
        if (resp.statusCode() != 404) {
            throw new RuntimeException("Xoá object lỗi: " + resp.statusCode() + " - " + resp.body());
        }
    }
}