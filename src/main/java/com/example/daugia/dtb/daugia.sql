-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1:3306
-- Thời gian đã tạo: Th10 09, 2025 lúc 07:56 PM
-- Phiên bản máy phục vụ: 9.1.0
-- Phiên bản PHP: 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `daugia`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `baocao`
--

DROP TABLE IF EXISTS `baocao`;
CREATE TABLE IF NOT EXISTS `baocao` (
  `mabc` char(10) NOT NULL,
  `maqtv` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `noidung` varchar(100) NOT NULL,
  `thoigian` timestamp NOT NULL,
  PRIMARY KEY (`mabc`),
  KEY `maqt` (`maqtv`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `danhmuc`
--

DROP TABLE IF EXISTS `danhmuc`;
CREATE TABLE IF NOT EXISTS `danhmuc` (
  `madm` char(10) NOT NULL,
  `tendm` varchar(50) NOT NULL,
  PRIMARY KEY (`madm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Đang đổ dữ liệu cho bảng `danhmuc`
--

INSERT INTO `danhmuc` (`madm`, `tendm`) VALUES
('DM00000001', 'Gỗ'),
('DM00000002', 'Bất Động Sản'),
('DM00000003', 'Điện Tử');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `hinhanh`
--

DROP TABLE IF EXISTS `hinhanh`;
CREATE TABLE IF NOT EXISTS `hinhanh` (
  `maanh` char(10) NOT NULL,
  `masp` char(10) NOT NULL,
  `tenanh` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`maanh`),
  KEY `masp` (`masp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Đang đổ dữ liệu cho bảng `hinhanh`
--

INSERT INTO `hinhanh` (`maanh`, `masp`, `tenanh`) VALUES
('HA00000001', 'SP98071333', 'nokia-chiec-la-2.png'),
('HA00000002', 'SP15966963', 'dien-thoai-ban-co-2.png'),
('HA00000003', 'SP25282533', 'ban-phim-co-ma-vang-2.png'),
('HA00000004', 'SP25282533', 'ban-phim-co-ma-vang-3.png'),
('HA11338300', 'SP21771289', 'anh-chup-man-hinh.png'),
('HA13749156', 'SP21771289', 'anh-chup-man-hinh-3.png'),
('HA14037273', 'SP68242302', 'logo-google.png'),
('HA44531072', 'SP15966963', 'dien-thoai-ban-co-1.png'),
('HA51042429', 'SP59276013', 'logo.png'),
('HA54546375', 'SP21771289', 'erd.png'),
('HA63276403', 'SP24455817', 'erd.png'),
('HA65003993', 'SP26332002', '2.png'),
('HA67849721', 'SP25282533', 'ban-phim-co-ma-vang-1.png'),
('HA78994585', 'SP98071333', 'nokia-chiec-la-1.png'),
('HA82736448', 'SP17304242', 'pastel-sunset-lake.jpg'),
('HA83666486', 'SP59276013', 'logo-google.png'),
('HA94131029', 'SP24455817', '2.png');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `phiendaugia`
--

DROP TABLE IF EXISTS `phiendaugia`;
CREATE TABLE IF NOT EXISTS `phiendaugia` (
  `maphiendg` char(10) NOT NULL,
  `masp` char(10) NOT NULL,
  `maqtv` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `makh` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `trangthai` tinyint NOT NULL,
  `thoigianbd` timestamp NOT NULL,
  `thoigiankt` timestamp NOT NULL,
  `thoigianbddk` timestamp NOT NULL,
  `thoigianktdk` timestamp NOT NULL,
  `giakhoidiem` decimal(10,0) NOT NULL,
  `giatran` decimal(10,0) NOT NULL,
  `buocgia` decimal(10,0) NOT NULL,
  `giacaonhatdatduoc` decimal(10,0) NOT NULL,
  `tiencoc` decimal(10,0) NOT NULL,
  `ketquaphien` tinyint NOT NULL,
  `slnguoithamgia` int NOT NULL,
  PRIMARY KEY (`maphiendg`),
  KEY `masp` (`masp`),
  KEY `makh` (`makh`),
  KEY `maqtv` (`maqtv`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Đang đổ dữ liệu cho bảng `phiendaugia`
--

INSERT INTO `phiendaugia` (`maphiendg`, `masp`, `maqtv`, `makh`, `trangthai`, `thoigianbd`, `thoigiankt`, `thoigianbddk`, `thoigianktdk`, `giakhoidiem`, `giatran`, `buocgia`, `giacaonhatdatduoc`, `tiencoc`, `ketquaphien`, `slnguoithamgia`) VALUES
('DG00000001', 'SP01466166', 'QT00000001', 'KH08560518', 1, '2025-11-05 18:41:43', '2025-11-05 18:41:43', '2025-11-05 18:41:43', '2025-11-05 18:41:43', 10000, 100000, 10000, 90000, 10000, 0, 1),
('DG01609968', 'SP98071333', NULL, 'KH29009892', 1, '2025-11-23 19:41:00', '2025-11-24 19:41:00', '2025-11-21 19:40:00', '2025-11-22 19:41:00', 10000, 100000, 1000, 0, 20000, 0, 0),
('DG18523456', 'SP15966963', NULL, 'KH29009892', 1, '2025-11-08 17:38:03', '2025-11-08 17:38:03', '2025-11-08 17:38:03', '2025-11-08 17:38:03', 1000000, 1000, 10000, 0, 10, 0, 0),
('DG24091114', 'SP25282533', NULL, 'KH29009892', 1, '2025-11-05 20:01:00', '2025-11-05 20:10:00', '2025-11-05 19:59:00', '2025-11-05 20:00:00', 1000000, 100000, 10000, 0, 100000, 0, 0),
('DG29423628', 'SP59276013', NULL, 'KH29009892', 1, '2025-12-03 02:22:00', '2025-12-04 02:22:00', '2025-12-01 02:22:00', '2025-12-02 02:22:00', 10, 50, 50, 0, 50, 0, 0),
('DG69813006', 'SP21771289', NULL, 'KH13658066', 1, '2025-11-09 00:25:00', '2025-11-10 00:25:00', '2025-11-07 00:25:00', '2025-11-08 00:25:00', 2, 2, 2, 0, 2, 0, 0),
('DG84211597', 'SP26332002', NULL, 'KH13658066', 1, '2025-11-21 00:25:00', '2025-11-22 00:25:00', '2025-11-19 00:25:00', '2025-11-20 00:25:00', 3, 3, 3, 0, 3, 0, 0),
('DG96730332', 'SP24455817', NULL, 'KH29009892', 1, '2025-11-24 00:28:00', '2025-11-25 00:29:00', '2025-11-22 00:28:00', '2025-11-23 00:28:00', 1, 1, 1, 0, 1, 0, 0),
('DG99509736', 'SP68242302', NULL, 'KH29009892', 1, '2025-12-10 02:30:00', '2025-12-11 02:30:00', '2025-12-08 02:29:00', '2025-12-09 02:30:00', 1, 1, 1, 0, 1, 0, 0);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `phientragia`
--

DROP TABLE IF EXISTS `phientragia`;
CREATE TABLE IF NOT EXISTS `phientragia` (
  `maphientg` char(10) NOT NULL,
  `makh` char(10) NOT NULL,
  `maphiendg` char(10) NOT NULL,
  `sotien` decimal(10,0) NOT NULL,
  `solan` int NOT NULL,
  `thoigian` timestamp NOT NULL,
  `thoigiancho` timestamp NOT NULL,
  PRIMARY KEY (`maphientg`),
  KEY `makh` (`makh`),
  KEY `maphiendg` (`maphiendg`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `phieuthanhtoan`
--

DROP TABLE IF EXISTS `phieuthanhtoan`;
CREATE TABLE IF NOT EXISTS `phieuthanhtoan` (
  `matt` char(10) NOT NULL,
  `maphiendg` char(10) NOT NULL,
  `makh` char(10) NOT NULL,
  `thoigianthanhtoan` timestamp NOT NULL,
  `trangthai` tinyint NOT NULL,
  `vnptransactionno` char(20) DEFAULT NULL,
  `bankcode` char(10) DEFAULT NULL,
  `raw` char(100) DEFAULT NULL,
  PRIMARY KEY (`matt`),
  KEY `maphiendg` (`maphiendg`),
  KEY `makh` (`makh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `phieuthanhtoantiencoc`
--

DROP TABLE IF EXISTS `phieuthanhtoantiencoc`;
CREATE TABLE IF NOT EXISTS `phieuthanhtoantiencoc` (
  `matc` char(10) NOT NULL,
  `maphiendg` char(10) NOT NULL,
  `makh` char(10) NOT NULL,
  `thoigianthanhtoan` timestamp NOT NULL,
  `trangthai` tinyint NOT NULL,
  `vnptransactionno` char(20) DEFAULT NULL,
  `bankcode` char(10) DEFAULT NULL,
  `raw` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  PRIMARY KEY (`matc`),
  KEY `maphiendg` (`maphiendg`),
  KEY `makh` (`makh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Đang đổ dữ liệu cho bảng `phieuthanhtoantiencoc`
--

INSERT INTO `phieuthanhtoantiencoc` (`matc`, `maphiendg`, `makh`, `thoigianthanhtoan`, `trangthai`, `vnptransactionno`, `bankcode`, `raw`) VALUES
('TC69790685', 'DG00000001', 'KH08560518', '2025-11-16 19:07:20', 1, '15249074', 'NCB', '{\"vnp_BankCode\":\"NCB\",\"vnp_PayDate\":\"20251110023704\",\"vnp_TransactionNo\":\"15249074\",\"vnp_TmnCode\":\"XU5Y6Y7X\",\"vnp_OrderInfo\":\"Thanh+toan+tien+coc+matc%3DTC69790685\",\"vnp_TxnRef\":\"45996364\",\"vnp_Amount\":\"1000000\",\"vnp_CardType\":\"ATM\",\"vnp_TransactionStatus\":\"00\",\"vnp_BankTranNo\":\"VNP15249074\",\"vnp_ResponseCode\":\"00\"}');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `sanpham`
--

DROP TABLE IF EXISTS `sanpham`;
CREATE TABLE IF NOT EXISTS `sanpham` (
  `masp` char(10) NOT NULL,
  `madm` char(10) NOT NULL,
  `maqtv` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `makh` char(10) NOT NULL,
  `matp` char(10) NOT NULL,
  `tinhtrangsp` varchar(50) NOT NULL,
  `tensp` varchar(50) NOT NULL,
  `trangthai` tinyint NOT NULL,
  PRIMARY KEY (`masp`),
  KEY `madm` (`madm`),
  KEY `maqtv` (`maqtv`),
  KEY `makh` (`makh`),
  KEY `sanpham_ibfk_4` (`matp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Đang đổ dữ liệu cho bảng `sanpham`
--

INSERT INTO `sanpham` (`masp`, `madm`, `maqtv`, `makh`, `matp`, `tinhtrangsp`, `tensp`, `trangthai`) VALUES
('SP01466166', 'DM00000001', NULL, 'KH08560518', 'TP00000001', 'Còn tốt hơi hơi', 'Gỗ xưa 2', 0),
('SP06976566', 'DM00000001', 'QT00000001', 'KH08560518', 'TP00000001', 'Còn tốt ác', 'Gỗ xưa', 1),
('SP15966963', 'DM00000003', NULL, 'KH29009892', 'TP00000002', 'Hàng sản xuất 1975', 'Điện thoại bàn cổ', 2),
('SP17304242', 'DM00000002', NULL, 'KH29009892', 'TP00000001', 'Nhà mới', 'Nhà B', 0),
('SP21771289', 'DM00000002', NULL, 'KH13658066', 'TP00000001', 'Mới xây chưa vào ở', 'Nhà A', 2),
('SP24455817', 'DM00000001', NULL, 'KH29009892', 'TP00000002', '10000 tuổi', 'Gỗ xưa', 2),
('SP25282533', 'DM00000003', NULL, 'KH29009892', 'TP00000001', 'New', 'Bàn phím cơ mạ vàng', 2),
('SP26332002', 'DM00000002', NULL, 'KH13658066', 'TP00000002', 'aaa', 'Nhà AA', 2),
('SP59276013', 'DM00000003', NULL, 'KH29009892', 'TP00000001', 'Hàng Lướt', 'Realme Q3 Pro', 2),
('SP68242302', 'DM00000003', NULL, 'KH29009892', 'TP00000002', 'aaaa', 'Điện thoại Samsung ', 2),
('SP98071333', 'DM00000003', NULL, 'KH29009892', 'TP00000001', 'Hàng new seal', 'Điện thoại Nokia', 2);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `taikhoan`
--

DROP TABLE IF EXISTS `taikhoan`;
CREATE TABLE IF NOT EXISTS `taikhoan` (
  `matk` char(10) NOT NULL,
  `matp` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `ho` varchar(10) NOT NULL,
  `tenlot` varchar(10) NOT NULL,
  `ten` varchar(10) NOT NULL,
  `email` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `diachi` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `diachigiaohang` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `sdt` char(11) NOT NULL,
  `matkhau` char(100) NOT NULL,
  `trangthaidangnhap` tinyint NOT NULL,
  PRIMARY KEY (`matk`),
  KEY `fktaikhoanthanhpho` (`matp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Đang đổ dữ liệu cho bảng `taikhoan`
--

INSERT INTO `taikhoan` (`matk`, `matp`, `ho`, `tenlot`, `ten`, `email`, `diachi`, `diachigiaohang`, `sdt`, `matkhau`, `trangthaidangnhap`) VALUES
('KH08560518', 'TP00000001', 'nguyen', 'chi', 'thien', 'chithien@gmail.com', 'Quan 8', 'Quan 9', '093746123', '$2a$10$pVJ90I.jSzYlJE1qSwmw3.8pMITV79xLqP6zzi4HTk2QHrfm.Xq.a', 0),
('KH13658066', NULL, 'Nguyễn', 'Văn', 'A', 'vana@gmail.com', 'Cao Lỗ, phường 4, quận 8, thành phố Hồ Chí Minh', NULL, '0912345678', '$2a$10$MXM32hQbURtxepyh.xh1Ee273BzAA1JZJova/4hm8nC9aOdx8wnzS', 0),
('KH29009892', 'TP00000001', 'Phan', 'Nhựt', 'Tân', 'nhuttan@gmail.com', 'Cao Lỗ, phường 4, quận 8, thành phố Hồ Chí Minh', 'Quận 4', '0912345678', '$2a$10$RA9OI6knJFNbyYt/d9FnV.wNY4.pTAk80Sjb4koqmuH1FrbVV2Xre', 0),
('KH32784375', 'TP00000002', 'nguyen', 'cong', 'tan', 'congtan@gmail.com', 'Quan Cầu Giấy', 'Quan 9', '093746123', '$2a$10$QpA8c0LxaWZQdMUHWcUWoOyWI2p7E1yyPkVr24SwS2jtCDsQhlXEK', 1),
('KH99329061', NULL, 'nguyen', 'cong', 'tan', 'congtan2@gmail.com', NULL, NULL, '093746123', '$2a$10$yILrkkyfLh/nAW9r8yBxmeCBzd.k4KrRJvreijq8bUDy2Q9XawWGG', 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `taikhoanquantri`
--

DROP TABLE IF EXISTS `taikhoanquantri`;
CREATE TABLE IF NOT EXISTS `taikhoanquantri` (
  `matk` char(10) NOT NULL,
  `ho` varchar(10) NOT NULL,
  `tenlot` varchar(10) NOT NULL,
  `ten` varchar(10) NOT NULL,
  `email` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sdt` char(11) NOT NULL,
  `matkhau` char(100) NOT NULL,
  PRIMARY KEY (`matk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Đang đổ dữ liệu cho bảng `taikhoanquantri`
--

INSERT INTO `taikhoanquantri` (`matk`, `ho`, `tenlot`, `ten`, `email`, `sdt`, `matkhau`) VALUES
('QT00000001', 'nguyen', 'cong', 'tan', 'congtan123@gmail.com', '0937465678', '$2a$10$reRYZ3jbnO1qhH.Fb0/QU.kDCAT1OG.dP7aNNcoYvKMpsqhR6CkzS');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `thanhpho`
--

DROP TABLE IF EXISTS `thanhpho`;
CREATE TABLE IF NOT EXISTS `thanhpho` (
  `matp` char(10) NOT NULL,
  `tentp` varchar(50) NOT NULL,
  PRIMARY KEY (`matp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Đang đổ dữ liệu cho bảng `thanhpho`
--

INSERT INTO `thanhpho` (`matp`, `tentp`) VALUES
('TP00000001', 'Hồ Chí Minh'),
('TP00000002', 'Hà Nội');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `thongbao`
--

DROP TABLE IF EXISTS `thongbao`;
CREATE TABLE IF NOT EXISTS `thongbao` (
  `matb` char(10) NOT NULL,
  `makh` char(10) NOT NULL,
  `maqtv` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `noidung` varchar(100) NOT NULL,
  `thoigian` timestamp NOT NULL,
  `trangthai` tinyint NOT NULL,
  PRIMARY KEY (`matb`),
  KEY `makh` (`makh`),
  KEY `maqt` (`maqtv`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `baocao`
--
ALTER TABLE `baocao`
  ADD CONSTRAINT `baocao_ibfk_1` FOREIGN KEY (`maqtv`) REFERENCES `taikhoanquantri` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Các ràng buộc cho bảng `hinhanh`
--
ALTER TABLE `hinhanh`
  ADD CONSTRAINT `hinhanh_ibfk_1` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Các ràng buộc cho bảng `phiendaugia`
--
ALTER TABLE `phiendaugia`
  ADD CONSTRAINT `phiendaugia_ibfk_1` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `phiendaugia_ibfk_2` FOREIGN KEY (`maqtv`) REFERENCES `taikhoanquantri` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `phiendaugia_ibfk_3` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Các ràng buộc cho bảng `phientragia`
--
ALTER TABLE `phientragia`
  ADD CONSTRAINT `phientragia_ibfk_1` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `phientragia_ibfk_2` FOREIGN KEY (`maphiendg`) REFERENCES `phiendaugia` (`maphiendg`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Các ràng buộc cho bảng `phieuthanhtoan`
--
ALTER TABLE `phieuthanhtoan`
  ADD CONSTRAINT `phieuthanhtoan_ibfk_1` FOREIGN KEY (`maphiendg`) REFERENCES `phiendaugia` (`maphiendg`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `phieuthanhtoan_ibfk_2` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Các ràng buộc cho bảng `phieuthanhtoantiencoc`
--
ALTER TABLE `phieuthanhtoantiencoc`
  ADD CONSTRAINT `phieuthanhtoantiencoc_ibfk_1` FOREIGN KEY (`maphiendg`) REFERENCES `phiendaugia` (`maphiendg`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `phieuthanhtoantiencoc_ibfk_2` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Các ràng buộc cho bảng `sanpham`
--
ALTER TABLE `sanpham`
  ADD CONSTRAINT `sanpham_ibfk_1` FOREIGN KEY (`madm`) REFERENCES `danhmuc` (`madm`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `sanpham_ibfk_2` FOREIGN KEY (`maqtv`) REFERENCES `taikhoanquantri` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `sanpham_ibfk_3` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `sanpham_ibfk_4` FOREIGN KEY (`matp`) REFERENCES `thanhpho` (`matp`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Các ràng buộc cho bảng `taikhoan`
--
ALTER TABLE `taikhoan`
  ADD CONSTRAINT `fktaikhoanthanhpho` FOREIGN KEY (`matp`) REFERENCES `thanhpho` (`matp`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Các ràng buộc cho bảng `thongbao`
--
ALTER TABLE `thongbao`
  ADD CONSTRAINT `thongbao_ibfk_1` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `thongbao_ibfk_2` FOREIGN KEY (`maqtv`) REFERENCES `taikhoanquantri` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
