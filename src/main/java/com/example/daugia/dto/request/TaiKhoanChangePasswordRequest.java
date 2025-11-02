package com.example.daugia.dto.request;

public class TaiKhoanChangePasswordRequest {
    private String matkhaucu;
    private String matkhaumoi;
    private String xacnhanmatkhau;

    public String getMatkhaucu() {
        return matkhaucu;
    }

    public void setMatkhaucu(String matkhaucu) {
        this.matkhaucu = matkhaucu;
    }

    public String getMatkhaumoi() {
        return matkhaumoi;
    }

    public void setMatkhaumoi(String matkhaumoi) {
        this.matkhaumoi = matkhaumoi;
    }

    public String getXacnhanmatkhau() {
        return xacnhanmatkhau;
    }

    public void setXacnhanmatkhau(String xacnhanmatkhau) {
        this.xacnhanmatkhau = xacnhanmatkhau;
    }
}
