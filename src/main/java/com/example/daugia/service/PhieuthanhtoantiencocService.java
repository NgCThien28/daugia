package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.example.daugia.dto.request.PhieuthanhtoantiencocCreationRequest;
import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.DepositDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Phieuthanhtoantiencoc;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.PhieuthanhtoantiencocRepository;
import com.example.daugia.repository.TaikhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class PhieuthanhtoantiencocService {
    @Autowired
    private PhieuthanhtoantiencocRepository phieuthanhtoantiencocRepository;
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    @Autowired
    private PhiendaugiaRepository phiendaugiaRepository;

    public List<DepositDTO> findAll() {
        List<Phieuthanhtoantiencoc> phieuthanhtoantiencocList = phieuthanhtoantiencocRepository.findAll();
        return phieuthanhtoantiencocList.stream()
                .map(phieuthanhtoantiencoc -> new DepositDTO(
                        phieuthanhtoantiencoc.getMatc(),
                        new UserShortDTO(phieuthanhtoantiencoc.getTaiKhoan().getMatk()),
                        new AuctionDTO(
                                phieuthanhtoantiencoc.getPhienDauGia().getMaphiendg(),
                                phieuthanhtoantiencoc.getPhienDauGia().getGiacaonhatdatduoc()
                        ),
                        phieuthanhtoantiencoc.getThoigianthanhtoan(),
                        phieuthanhtoantiencoc.getTrangthai()
                ))
                .toList();
    }
    public DepositDTO findById(String id){
        Phieuthanhtoantiencoc phieuthanhtoantiencoc = phieuthanhtoantiencocRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Không tìm thấy"));
        DepositDTO depositDTO = new DepositDTO();
        depositDTO.setMatc(phieuthanhtoantiencoc.getMatc());
        depositDTO.setTrangthai(phieuthanhtoantiencoc.getTrangthai());
        depositDTO.setPhienDauGia(new AuctionDTO(phieuthanhtoantiencoc.getPhienDauGia().getMaphiendg()));
        depositDTO.setTaiKhoanKhachThanhToan(new UserShortDTO(phieuthanhtoantiencoc.getTaiKhoan().getMatk()));
        return depositDTO;
    }

    public DepositDTO create(PhieuthanhtoantiencocCreationRequest request, String email){
        Phieuthanhtoantiencoc phieuthanhtoantiencoc = new Phieuthanhtoantiencoc();
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("Không tìm thấy tài khoản"));
        Phiendaugia phiendaugia = phiendaugiaRepository.findById(request.getMaphien())
                .orElseThrow(()-> new IllegalArgumentException("Không tìm thấy phien dau gia"));
        phieuthanhtoantiencoc.setTaiKhoan(taikhoan);
        phieuthanhtoantiencoc.setPhienDauGia(phiendaugia);
        phieuthanhtoantiencoc.setThoigianthanhtoan(Timestamp.from(Instant.now().plusSeconds(7 * 24 * 60 * 60)));
        phieuthanhtoantiencoc.setTrangthai(TrangThaiPhieuThanhToanTienCoc.UNPAID);
        phieuthanhtoantiencocRepository.save(phieuthanhtoantiencoc);
        return findById(phieuthanhtoantiencoc.getMatc());
    }

    public DepositDTO update(PhieuthanhtoantiencocCreationRequest request) {
        Phieuthanhtoantiencoc phieu = phieuthanhtoantiencocRepository.findById(request.getMatc())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu thanh toán"));
        Timestamp thoigianThanhToanChoPhep = phieu.getThoigianthanhtoan();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if(!phieu.getTrangthai().equals(TrangThaiPhieuThanhToanTienCoc.PAID)){
            if (thoigianThanhToanChoPhep.after(now)) {
                phieu.setTrangthai(TrangThaiPhieuThanhToanTienCoc.PAID);
                Phiendaugia phien = phieu.getPhienDauGia();
                phien.setSlnguoithamgia(phien.getSlnguoithamgia() + 1);
                phiendaugiaRepository.save(phien);
                phieuthanhtoantiencocRepository.save(phieu);
            } else {
                throw new IllegalStateException("Đã quá thời hạn thanh toán");
            }
        }
        else {
            throw new IllegalStateException("Đã thanh toán, khong thanh toan nua");
        }
        return findById(phieu.getMatc());
    }
}
