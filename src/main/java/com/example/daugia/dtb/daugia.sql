-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Oct 29, 2025 at 05:44 PM
-- Server version: 9.1.0
-- PHP Version: 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `daugia`
--

-- --------------------------------------------------------

--
-- Table structure for table `baocao`
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

--
-- Dumping data for table `baocao`
--

INSERT INTO `baocao` (`mabc`, `maqtv`, `noidung`, `thoigian`) VALUES
('1', '1', 'abc', '2025-10-25 20:20:31'),
('BC06830611', '1', 'ádgfvb', '2025-10-29 16:56:27'),
('BC58554323', '1', 'Đã sửa nội dung', '2025-10-28 16:39:17'),
('BC69808116', '1', 'ádgfvb', '2025-10-29 16:39:05'),
('BC85500499', '1', 'ádgfvb', '2025-10-29 16:25:47');

-- --------------------------------------------------------

--
-- Table structure for table `danhmuc`
--

DROP TABLE IF EXISTS `danhmuc`;
CREATE TABLE IF NOT EXISTS `danhmuc` (
  `madm` char(10) NOT NULL,
  `tendm` varchar(50) NOT NULL,
  PRIMARY KEY (`madm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `danhmuc`
--

INSERT INTO `danhmuc` (`madm`, `tendm`) VALUES
('1', 'Gỗ');

-- --------------------------------------------------------

--
-- Table structure for table `hinhanh`
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
-- Dumping data for table `hinhanh`
--

INSERT INTO `hinhanh` (`maanh`, `masp`, `tenanh`) VALUES
('1', '1', 'anh'),
('HA07023226', 'SP12427644', 'asus_t123.jpg'),
('HA10455718', 'SP93801173', 'asus_t123.jpg'),
('HA20621650', 'SP12427644', 'thien_loc.jpg'),
('HA21457969', 'SP93801173', 'thien_loc.jpg'),
('HA55068063', 'SP12427644', 'asus_tk4565.jpg'),
('HA84565875', 'SP93801173', 'asus_tk4565.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `phiendaugia`
--

DROP TABLE IF EXISTS `phiendaugia`;
CREATE TABLE IF NOT EXISTS `phiendaugia` (
  `maphiendg` char(10) NOT NULL,
  `masp` char(10) NOT NULL,
  `maqtv` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
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
-- Dumping data for table `phiendaugia`
--

INSERT INTO `phiendaugia` (`maphiendg`, `masp`, `maqtv`, `makh`, `trangthai`, `thoigianbd`, `thoigiankt`, `thoigianbddk`, `thoigianktdk`, `giakhoidiem`, `giatran`, `buocgia`, `giacaonhatdatduoc`, `tiencoc`, `ketquaphien`, `slnguoithamgia`) VALUES
('1', '1', '1', '1', 0, '2025-10-25 18:43:07', '2025-10-25 18:43:07', '2025-10-25 18:43:07', '2025-10-25 18:43:07', 10000, 100000, 10000, 30000, 10000, 0, 1);

-- --------------------------------------------------------

--
-- Table structure for table `phientragia`
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

--
-- Dumping data for table `phientragia`
--

INSERT INTO `phientragia` (`maphientg`, `makh`, `maphiendg`, `sotien`, `solan`, `thoigian`, `thoigiancho`) VALUES
('TG30762969', 'KH66115668', '1', 30000, 1, '2025-10-29 17:25:00', '2025-10-29 17:25:20'),
('TG60931241', 'KH18401112', '1', 20000, 1, '2025-10-29 17:24:45', '2025-10-29 17:25:05');

-- --------------------------------------------------------

--
-- Table structure for table `phieuthanhtoan`
--

DROP TABLE IF EXISTS `phieuthanhtoan`;
CREATE TABLE IF NOT EXISTS `phieuthanhtoan` (
  `matt` char(10) NOT NULL,
  `maphiendg` char(10) NOT NULL,
  `makh` char(10) NOT NULL,
  `thoigianthanhtoan` timestamp NOT NULL,
  `trangthai` tinyint NOT NULL,
  PRIMARY KEY (`matt`),
  KEY `maphiendg` (`maphiendg`),
  KEY `makh` (`makh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `phieuthanhtoantiencoc`
--

DROP TABLE IF EXISTS `phieuthanhtoantiencoc`;
CREATE TABLE IF NOT EXISTS `phieuthanhtoantiencoc` (
  `matc` char(10) NOT NULL,
  `maphiendg` char(10) NOT NULL,
  `makh` char(10) NOT NULL,
  `thoigianthanhtoan` timestamp NOT NULL,
  `trangthai` tinyint NOT NULL,
  PRIMARY KEY (`matc`),
  KEY `maphiendg` (`maphiendg`),
  KEY `makh` (`makh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `sanpham`
--

DROP TABLE IF EXISTS `sanpham`;
CREATE TABLE IF NOT EXISTS `sanpham` (
  `masp` char(10) NOT NULL,
  `madm` char(10) NOT NULL,
  `maqtv` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `makh` char(10) NOT NULL,
  `tinhtrangsp` varchar(50) NOT NULL,
  `tensp` varchar(50) NOT NULL,
  `trangthai` tinyint NOT NULL,
  PRIMARY KEY (`masp`),
  KEY `madm` (`madm`),
  KEY `maqtv` (`maqtv`),
  KEY `makh` (`makh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `sanpham`
--

INSERT INTO `sanpham` (`masp`, `madm`, `maqtv`, `makh`, `tinhtrangsp`, `tensp`, `trangthai`) VALUES
('1', '1', '1', '1', 'Còn tốt', 'Gỗ xưa', 0),
('SP12427644', '1', NULL, 'KH18401112', '1000 tuổi', 'Thiên lôi trúcccccc', 0),
('SP93801173', '1', NULL, 'KH18401112', '1000 tuổi', 'Thiên lôi trúcccccc', 0);

-- --------------------------------------------------------

--
-- Table structure for table `taikhoan`
--

DROP TABLE IF EXISTS `taikhoan`;
CREATE TABLE IF NOT EXISTS `taikhoan` (
  `matk` char(10) NOT NULL,
  `ho` varchar(10) NOT NULL,
  `tenlot` varchar(10) NOT NULL,
  `ten` varchar(10) NOT NULL,
  `email` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `diachi` varchar(50) NOT NULL,
  `diachigiaohang` varchar(50) NOT NULL,
  `sdt` char(11) NOT NULL,
  `matkhau` char(100) NOT NULL,
  `trangthaidangnhap` tinyint NOT NULL,
  PRIMARY KEY (`matk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `taikhoan`
--

INSERT INTO `taikhoan` (`matk`, `ho`, `tenlot`, `ten`, `email`, `diachi`, `diachigiaohang`, `sdt`, `matkhau`, `trangthaidangnhap`) VALUES
('1', 'nguyen', 'cong', 'tan', 'congtan@gmail.com', 'Ton Dan', 'Ton Dan', '0937461234', '123456', 1),
('KH18401112', 'nguyen', 'chi', 'thien', 'chithien@gmail.com', 'Quan 7', 'Quan 4', '0979286060', '$2a$10$1Z8p.X7qMSyw/MH46nnhWe9spra6w1/QgUg3DU4wV1geraInPkHla', 0),
('KH66115668', 'nguyen', 'chi', 'thien', 'chithien1@gmail.com', 'Quan 8', 'Quan 9', '093746123', '$2a$10$3TnqLwWJb4qPJ9Bo3WHWA.iuJO6Mbop4xZFISQP.SniR2muOpCKKm', 0);

-- --------------------------------------------------------

--
-- Table structure for table `taikhoanquantri`
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
-- Dumping data for table `taikhoanquantri`
--

INSERT INTO `taikhoanquantri` (`matk`, `ho`, `tenlot`, `ten`, `email`, `sdt`, `matkhau`) VALUES
('1', 'nguyen', 'cong', 'tan', 'congtan123@gmail.com', '0937465678', '$2a$10$reRYZ3jbnO1qhH.Fb0/QU.kDCAT1OG.dP7aNNcoYvKMpsqhR6CkzS');

-- --------------------------------------------------------

--
-- Table structure for table `thongbao`
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
-- Constraints for dumped tables
--

--
-- Constraints for table `baocao`
--
ALTER TABLE `baocao`
  ADD CONSTRAINT `baocao_ibfk_1` FOREIGN KEY (`maqtv`) REFERENCES `taikhoanquantri` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Constraints for table `hinhanh`
--
ALTER TABLE `hinhanh`
  ADD CONSTRAINT `hinhanh_ibfk_1` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Constraints for table `phiendaugia`
--
ALTER TABLE `phiendaugia`
  ADD CONSTRAINT `phiendaugia_ibfk_1` FOREIGN KEY (`masp`) REFERENCES `sanpham` (`masp`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `phiendaugia_ibfk_2` FOREIGN KEY (`maqtv`) REFERENCES `taikhoanquantri` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `phiendaugia_ibfk_3` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Constraints for table `phientragia`
--
ALTER TABLE `phientragia`
  ADD CONSTRAINT `phientragia_ibfk_1` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `phientragia_ibfk_2` FOREIGN KEY (`maphiendg`) REFERENCES `phiendaugia` (`maphiendg`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Constraints for table `phieuthanhtoan`
--
ALTER TABLE `phieuthanhtoan`
  ADD CONSTRAINT `phieuthanhtoan_ibfk_1` FOREIGN KEY (`maphiendg`) REFERENCES `phiendaugia` (`maphiendg`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `phieuthanhtoan_ibfk_2` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Constraints for table `phieuthanhtoantiencoc`
--
ALTER TABLE `phieuthanhtoantiencoc`
  ADD CONSTRAINT `phieuthanhtoantiencoc_ibfk_1` FOREIGN KEY (`maphiendg`) REFERENCES `phiendaugia` (`maphiendg`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `phieuthanhtoantiencoc_ibfk_2` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Constraints for table `sanpham`
--
ALTER TABLE `sanpham`
  ADD CONSTRAINT `sanpham_ibfk_1` FOREIGN KEY (`madm`) REFERENCES `danhmuc` (`madm`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `sanpham_ibfk_2` FOREIGN KEY (`maqtv`) REFERENCES `taikhoanquantri` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `sanpham_ibfk_3` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;

--
-- Constraints for table `thongbao`
--
ALTER TABLE `thongbao`
  ADD CONSTRAINT `thongbao_ibfk_1` FOREIGN KEY (`makh`) REFERENCES `taikhoan` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  ADD CONSTRAINT `thongbao_ibfk_2` FOREIGN KEY (`maqtv`) REFERENCES `taikhoanquantri` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
