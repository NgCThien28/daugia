-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Oct 22, 2025 at 05:06 PM
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
  `maqt` char(10) NOT NULL,
  `noidung` varchar(100) NOT NULL,
  `thoigian` timestamp NOT NULL,
  PRIMARY KEY (`mabc`),
  KEY `maqt` (`maqt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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

-- --------------------------------------------------------

--
-- Table structure for table `hinhanh`
--

DROP TABLE IF EXISTS `hinhanh`;
CREATE TABLE IF NOT EXISTS `hinhanh` (
  `maanh` char(10) NOT NULL,
  `masp` char(10) NOT NULL,
  `tenanh` char(20) NOT NULL,
  PRIMARY KEY (`maanh`),
  KEY `masp` (`masp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `phiendaugia`
--

DROP TABLE IF EXISTS `phiendaugia`;
CREATE TABLE IF NOT EXISTS `phiendaugia` (
  `maphiendg` char(10) NOT NULL,
  `masp` char(10) NOT NULL,
  `maqt` char(10) NOT NULL,
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
  KEY `maqt` (`maqt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
  `maqtv` char(10) NOT NULL,
  `makh` char(10) NOT NULL,
  `tinhtrangsp` varchar(50) NOT NULL,
  `tensp` varchar(50) NOT NULL,
  `trangthai` tinyint NOT NULL,
  PRIMARY KEY (`masp`),
  KEY `madm` (`madm`),
  KEY `maqtv` (`maqtv`),
  KEY `makh` (`makh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
  `email` char(20) NOT NULL,
  `diachi` varchar(50) NOT NULL,
  `diachigiaohang` varchar(50) NOT NULL,
  `sdt` char(11) NOT NULL,
  `matkhau` char(100) NOT NULL,
  `trangthaidangnhap` tinyint NOT NULL,
  PRIMARY KEY (`matk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
  `email` char(20) NOT NULL,
  `sdt` char(11) NOT NULL,
  `matkhau` char(100) NOT NULL,
  PRIMARY KEY (`matk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `thongbao`
--

DROP TABLE IF EXISTS `thongbao`;
CREATE TABLE IF NOT EXISTS `thongbao` (
  `matb` char(10) NOT NULL,
  `makh` char(10) NOT NULL,
  `maqt` char(10) NOT NULL,
  `noidung` varchar(100) NOT NULL,
  `thoigian` timestamp NOT NULL,
  PRIMARY KEY (`matb`),
  KEY `makh` (`makh`),
  KEY `maqt` (`maqt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `baocao`
--
ALTER TABLE `baocao`
  ADD CONSTRAINT `baocao_ibfk_1` FOREIGN KEY (`maqt`) REFERENCES `taikhoanquantri` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;

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
  ADD CONSTRAINT `phiendaugia_ibfk_2` FOREIGN KEY (`maqt`) REFERENCES `taikhoanquantri` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;

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
  ADD CONSTRAINT `thongbao_ibfk_2` FOREIGN KEY (`maqt`) REFERENCES `taikhoanquantri` (`matk`) ON DELETE RESTRICT ON UPDATE RESTRICT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
