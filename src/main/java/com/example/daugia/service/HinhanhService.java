package com.example.daugia.service;

import com.example.daugia.dto.request.ImageOperation;
import com.example.daugia.dto.request.ImageUpsertRequest;
import com.example.daugia.entity.Hinhanh;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.StorageException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.HinhanhRepository;
import com.example.daugia.repository.SanphamRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HinhanhService {

    private static final int MAX_IMAGES = 3;

    @Autowired
    private HinhanhRepository hinhanhRepository;
    @Autowired
    private SanphamRepository sanphamRepository;

    public List<Hinhanh> findAll() {
        return hinhanhRepository.findAll();
    }

    /* ================== INIT ================== */
    @Transactional
    public List<Hinhanh> createInitial(String masp, List<MultipartFile> files) {
        validateFilesNotEmpty(files);
        if (files.size() > MAX_IMAGES) {
            throw new ValidationException("Tối đa " + MAX_IMAGES + " ảnh");
        }
        Sanpham sp = loadSanpham(masp);
        ensureImageCollection(sp);

        if (!sp.getHinhAnh().isEmpty()) {
            throw new ValidationException("Sản phẩm đã có ảnh, dùng replace-all hoặc upsert");
        }

        List<Hinhanh> added = internalAppend(sp, files, MAX_IMAGES);
        // Không set list mới, chỉ add vào list hiện có
        sp.getHinhAnh().addAll(added);
        sanphamRepository.save(sp);
        return sp.getHinhAnh();
    }

    /* ================== APPEND ================== */
    @Transactional
    public List<Hinhanh> append(String masp, List<MultipartFile> files) {
        validateFilesNotEmpty(files);
        Sanpham sp = loadSanpham(masp);
        ensureImageCollection(sp);
        int remaining = MAX_IMAGES - sp.getHinhAnh().size();
        if (remaining <= 0) {
            throw new ValidationException("Đã đủ " + MAX_IMAGES + " ảnh");
        }

        List<Hinhanh> added = internalAppend(sp, files, remaining);
        sp.getHinhAnh().addAll(added);
        sanphamRepository.save(sp);
        return sp.getHinhAnh();
    }

    /* ================== REPLACE PARTIAL ================== */
    @Transactional
    public List<Hinhanh> replaceIndices(String masp, Map<Integer, MultipartFile> replaceMap) {
        if (replaceMap == null || replaceMap.isEmpty()) {
            throw new ValidationException("Không có chỉ số ảnh để thay thế");
        }
        Sanpham sp = loadSanpham(masp);
        ensureImageCollection(sp);

        Path dir = ensureImageDir();
        List<Hinhanh> current = sp.getHinhAnh();

        for (Map.Entry<Integer, MultipartFile> entry : replaceMap.entrySet()) {
            Integer idx = entry.getKey();
            MultipartFile file = entry.getValue();
            if (file == null || file.isEmpty()) continue;
            if (idx < 0 || idx >= current.size()) {
                throw new ValidationException("Index không hợp lệ: " + idx);
            }
            Hinhanh old = current.get(idx);
            try {
                Files.deleteIfExists(dir.resolve(old.getTenanh()));
            } catch (IOException ignore) {}

            String original = file.getOriginalFilename();
            if (original == null || original.isBlank()) {
                throw new ValidationException("Tên file không hợp lệ ở index " + idx);
            }
            String uniqueName = resolveUniqueFilename(dir, sanitizeFilename(original));
            try {
                file.transferTo(dir.resolve(uniqueName).toFile());
            } catch (IOException e) {
                throw new StorageException("Không thể lưu file: " + original, e);
            }
            old.setTenanh(uniqueName);
            hinhanhRepository.save(old);
        }
        sanphamRepository.save(sp);
        return current;
    }

    /* ================== REPLACE ALL ================== */
    @Transactional
    public List<Hinhanh> replaceAll(String masp, List<MultipartFile> files) {
        validateFilesNotEmpty(files);
        if (files.size() > MAX_IMAGES) {
            throw new ValidationException("Tối đa " + MAX_IMAGES + " ảnh");
        }
        Sanpham sp = loadSanpham(masp);
        ensureImageCollection(sp);
        Path dir = ensureImageDir();

        // Xóa file + entity cũ
        List<Hinhanh> current = sp.getHinhAnh();
        for (Hinhanh h : new ArrayList<>(current)) {
            try {
                Files.deleteIfExists(dir.resolve(h.getTenanh()));
            } catch (IOException ignore) {}
            hinhanhRepository.delete(h);
        }
        // Không set list mới — chỉ clear() rồi addAll()
        current.clear();

        List<Hinhanh> added = internalAppend(sp, files, MAX_IMAGES);
        current.addAll(added);

        sanphamRepository.save(sp);
        return current;
    }

    /* ================== REMOVE ================== */
    @Transactional
    public List<Hinhanh> removeByIndices(String masp, List<Integer> indices) {
        if (indices == null || indices.isEmpty()) {
            throw new ValidationException("Không có index nào cần xóa");
        }
        Sanpham sp = loadSanpham(masp);
        ensureImageCollection(sp);
        Path dir = ensureImageDir();
        List<Hinhanh> current = sp.getHinhAnh();

        List<Integer> sorted = indices.stream()
                .distinct()
                .filter(i -> i >= 0 && i < current.size())
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        for (Integer idx : sorted) {
            Hinhanh h = current.get(idx);
            try {
                Files.deleteIfExists(dir.resolve(h.getTenanh()));
            } catch (IOException ignore) {}
            hinhanhRepository.delete(h);
            current.remove((int) idx);
        }
        sanphamRepository.save(sp);
        return current;
    }

    @Transactional
    public List<Hinhanh> removeByIds(String masp, List<String> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            throw new ValidationException("Không có id ảnh nào");
        }
        Sanpham sp = loadSanpham(masp);
        ensureImageCollection(sp);
        Path dir = ensureImageDir();

        List<Hinhanh> current = sp.getHinhAnh();
        Iterator<Hinhanh> it = current.iterator();
        while (it.hasNext()) {
            Hinhanh h = it.next();
            if (imageIds.contains(h.getMaanh())) {
                try {
                    Files.deleteIfExists(dir.resolve(h.getTenanh()));
                } catch (IOException ignore) {}
                hinhanhRepository.delete(h);
                it.remove();
            }
        }
        sanphamRepository.save(sp);
        return current;
    }

    /* ================== REORDER ================== */
    @Transactional
    public List<Hinhanh> reorder(String masp, List<Integer> newOrder) {
        if (newOrder == null || newOrder.isEmpty()) {
            throw new ValidationException("Danh sách reorder rỗng");
        }
        Sanpham sp = loadSanpham(masp);
        ensureImageCollection(sp);
        List<Hinhanh> current = sp.getHinhAnh();

        if (newOrder.size() != current.size()) {
            throw new ValidationException("Số lượng index reorder không khớp số ảnh hiện có");
        }
        Set<Integer> set = new HashSet<>(newOrder);
        if (set.size() != current.size()) {
            throw new ValidationException("Index reorder bị trùng");
        }
        for (Integer i : newOrder) {
            if (i < 0 || i >= current.size()) {
                throw new ValidationException("Index reorder ngoài phạm vi: " + i);
            }
        }

        List<Hinhanh> reordered = new ArrayList<>(current.size());
        for (Integer i : newOrder) {
            reordered.add(current.get(i));
        }
        // Không set list mới — clear + addAll
        current.clear();
        current.addAll(reordered);

        sanphamRepository.save(sp);
        return current;
    }

    /* ================== UPSERT HỖN HỢP ================== */
    @Transactional
    public List<Hinhanh> upsertMixed(ImageUpsertRequest meta, List<MultipartFile> files) {
        if (meta == null || meta.getMasp() == null) {
            throw new ValidationException("Thiếu thông tin sản phẩm");
        }
        Sanpham sp = loadSanpham(meta.getMasp());
        ensureImageCollection(sp);
        Path dir = ensureImageDir();

        List<Hinhanh> current = sp.getHinhAnh();
        List<ImageOperation> ops = meta.getOperations();
        if (ops == null || ops.isEmpty()) {
            throw new ValidationException("Không có thao tác nào");
        }

        Queue<MultipartFile> fileQueue = new LinkedList<>();
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) fileQueue.add(f);
        }

        long reorderCount = ops.stream().filter(o -> o.getType() == ImageOperation.OpType.REORDER).count();
        if (reorderCount > 0 && ops.size() > 1) {
            throw new ValidationException("REORDER phải là thao tác duy nhất trong danh sách");
        }
        if (reorderCount == 1) {
            return reorder(sp.getMasp(), meta.getReorderIndices());
        }

        for (ImageOperation op : ops) {
            switch (op.getType()) {
                case REMOVE -> {
                    Integer idx = op.getIndex();
                    if (idx == null) throw new ValidationException("Thiếu index cho REMOVE");
                    if (idx < 0 || idx >= current.size()) throw new ValidationException("Index REMOVE không hợp lệ: " + idx);
                    Hinhanh h = current.get(idx);
                    try {
                        Files.deleteIfExists(dir.resolve(h.getTenanh()));
                    } catch (IOException ignore) {}
                    hinhanhRepository.delete(h);
                    current.remove((int) idx);
                }
                case REPLACE -> {
                    Integer idx = op.getIndex();
                    if (idx == null) throw new ValidationException("Thiếu index cho REPLACE");
                    if (idx < 0 || idx >= current.size()) throw new ValidationException("Index REPLACE không hợp lệ: " + idx);
                    MultipartFile mf = fileQueue.poll();
                    if (mf == null) throw new ValidationException("Thiếu file cho REPLACE index " + idx);
                    String original = mf.getOriginalFilename();
                    if (original == null || original.isBlank()) throw new ValidationException("Tên file rỗng ở REPLACE index " + idx);

                    Hinhanh old = current.get(idx);
                    try {
                        Files.deleteIfExists(dir.resolve(old.getTenanh()));
                    } catch (IOException ignore) {}
                    String unique = resolveUniqueFilename(dir, sanitizeFilename(original));
                    try {
                        mf.transferTo(dir.resolve(unique).toFile());
                    } catch (IOException e) {
                        throw new StorageException("Không thể lưu file REPLACE: " + original, e);
                    }
                    old.setTenanh(unique);
                    hinhanhRepository.save(old);
                }
                case APPEND -> {
                    if (current.size() >= MAX_IMAGES) {
                        throw new ValidationException("Đã đủ " + MAX_IMAGES + " ảnh, không thể APPEND thêm");
                    }
                    MultipartFile mf = fileQueue.poll();
                    if (mf == null) throw new ValidationException("Thiếu file cho APPEND");
                    String original = mf.getOriginalFilename();
                    if (original == null || original.isBlank()) throw new ValidationException("Tên file rỗng ở APPEND");

                    String unique = resolveUniqueFilename(dir, sanitizeFilename(original));
                    try {
                        mf.transferTo(dir.resolve(unique).toFile());
                    } catch (IOException e) {
                        throw new StorageException("Không thể lưu file APPEND: " + original, e);
                    }
                    Hinhanh newImg = new Hinhanh();
                    newImg.setTenanh(unique);
                    newImg.setSanPham(sp);
                    hinhanhRepository.save(newImg);
                    current.add(newImg);
                }
                default -> throw new ValidationException("Loại thao tác không hỗ trợ: " + op.getType());
            }
        }

        if (current.size() > MAX_IMAGES) {
            throw new ValidationException("Sau thao tác vượt quá " + MAX_IMAGES + " ảnh");
        }
        // Không cần set lại collection
        sanphamRepository.save(sp);
        return current;
    }

    /* ================= Helpers ================= */

    private Sanpham loadSanpham(String masp) {
        return sanphamRepository.findById(masp)
                .orElseThrow(() -> new NotFoundException("Sản phẩm không tồn tại"));
    }

    private void ensureImageCollection(Sanpham sp) {
        // Chỉ set list mới nếu hiện tại là null; nếu đã có PersistentCollection thì không đụng tới tham chiếu
        if (sp.getHinhAnh() == null) {
            sp.setHinhAnh(new ArrayList<>());
        }
    }

    private void validateFilesNotEmpty(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ValidationException("Không có file nào được gửi");
        }
    }

    private List<Hinhanh> internalAppend(Sanpham sp, List<MultipartFile> files, int limit) {
        Path dir = ensureImageDir();
        List<Hinhanh> added = new ArrayList<>();
        int addedCount = 0;
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            if (addedCount >= limit) break;
            String original = f.getOriginalFilename();
            if (original == null || original.isBlank()) continue;
            String unique = resolveUniqueFilename(dir, sanitizeFilename(original));
            try {
                f.transferTo(dir.resolve(unique).toFile());
            } catch (IOException e) {
                throw new StorageException("Không thể lưu file: " + original, e);
            }
            Hinhanh h = new Hinhanh();
            h.setTenanh(unique);
            h.setSanPham(sp);
            hinhanhRepository.save(h);
            added.add(h);
            addedCount++;
        }
        if (added.isEmpty()) {
            throw new ValidationException("Không có file hợp lệ để thêm");
        }
        return added;
    }

    private Path ensureImageDir() {
        String imgDir = System.getProperty("user.dir") + "/imgs";
        Path dirPath = Paths.get(imgDir);
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (IOException e) {
            throw new StorageException("Không thể tạo thư mục lưu ảnh: " + imgDir, e);
        }
        return dirPath;
    }

    private String sanitizeFilename(String filename) {
        String name = filename;
        String ext = "";
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx >= 0) {
            ext = name.substring(dotIdx).toLowerCase();
            name = name.substring(0, dotIdx);
        }
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^a-zA-Z0-9\\-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "")
                .toLowerCase();
        if (base.isBlank()) base = "image";
        return base + ext;
    }

    private String resolveUniqueFilename(Path dir, String filename) {
        Path p = dir.resolve(filename);
        if (!Files.exists(p)) return filename;

        String name = filename;
        String ext = "";
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx >= 0) {
            ext = name.substring(dotIdx);
            name = name.substring(0, dotIdx);
        }
        int i = 1;
        while (true) {
            String next = name + "-" + i + ext;
            if (!Files.exists(dir.resolve(next))) return next;
            i++;
        }
    }
}