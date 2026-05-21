package com.coffeechain.dto.response;

public class PermissionResponse {
    private Long maQuyen;
    private Long maChucNang;
    private String module;
    private String action;
    private String code;
    private String tenQuyen;

    public PermissionResponse() {}

    public PermissionResponse(Long maQuyen, Long maChucNang, String module, String action, String tenQuyen) {
        this.maQuyen = maQuyen;
        this.maChucNang = maChucNang;
        this.module = module;
        this.action = action;
        this.code = module + ":" + action;
        this.tenQuyen = tenQuyen;
    }

    public Long getMaQuyen() { return maQuyen; }
    public void setMaQuyen(Long maQuyen) { this.maQuyen = maQuyen; }
    public Long getMaChucNang() { return maChucNang; }
    public void setMaChucNang(Long maChucNang) { this.maChucNang = maChucNang; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; refreshCode(); }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; refreshCode(); }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTenQuyen() { return tenQuyen; }
    public void setTenQuyen(String tenQuyen) { this.tenQuyen = tenQuyen; }
    private void refreshCode() { if (module != null && action != null) this.code = module + ":" + action; }
}
