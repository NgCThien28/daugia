package com.example.daugia.repository;

import com.example.daugia.entity.Thongbao;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongbaoRepository extends JpaRepository<Thongbao, String> {
    List<Thongbao> findByTaiKhoan_Matk(String matk);
    Page<Thongbao> findByTaiKhoan_Matk(String matk, Pageable pageable);

    // Xóa tất cả thông báo của user
    @Modifying
    @Query("DELETE FROM Thongbao tb WHERE tb.taiKhoan.matk = :matk")
    void deleteByTaiKhoan_Matk(@Param("matk") String matk);

    // Đánh dấu tất cả thông báo của user là đã đọc
    @Modifying
    @Query("UPDATE Thongbao tb SET tb.trangthai = com.example.daugia.core.enums.TrangThaiThongBao.VIEWED WHERE tb.taiKhoan.matk = :matk AND tb.trangthai = com.example.daugia.core.enums.TrangThaiThongBao.NOT_VIEWED")
    int markAllAsReadByTaiKhoan_Matk(@Param("matk") String matk);
}
