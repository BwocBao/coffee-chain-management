SET DEFINE OFF;

-- ============================================================================
-- BEGIN: seed tong hop
-- ============================================================================
-- Seed RBAC + 8 chi nhanh mau cho Oracle
-- Co the chay nhieu lan: dung MERGE va INSERT WHERE NOT EXISTS de tranh trung unique.
-- Luu y: file nay chi nap du lieu, khong tao bang.
-- Neu database moi hoan toan, hay chay setup_full_database.sql hoac chay
-- quan_ly_chuoi_cafe_phung_loc.sql truoc file nay.

-------------------------------------------------------------------------------
-- 1. Vai tro
-------------------------------------------------------------------------------
MERGE INTO VAITRO t
USING (SELECT 'ADMIN' ten_vai_tro FROM dual) s
ON (t.ten_vai_tro = s.ten_vai_tro)
WHEN NOT MATCHED THEN INSERT (ten_vai_tro) VALUES (s.ten_vai_tro);

MERGE INTO VAITRO t
USING (SELECT 'QUAN_LY_KHO' ten_vai_tro FROM dual) s
ON (t.ten_vai_tro = s.ten_vai_tro)
WHEN NOT MATCHED THEN INSERT (ten_vai_tro) VALUES (s.ten_vai_tro);

MERGE INTO VAITRO t
USING (SELECT 'QUAN_LY_CHI_NHANH' ten_vai_tro FROM dual) s
ON (t.ten_vai_tro = s.ten_vai_tro)
WHEN NOT MATCHED THEN INSERT (ten_vai_tro) VALUES (s.ten_vai_tro);

MERGE INTO VAITRO t
USING (SELECT 'THU_NGAN' ten_vai_tro FROM dual) s
ON (t.ten_vai_tro = s.ten_vai_tro)
WHEN NOT MATCHED THEN INSERT (ten_vai_tro) VALUES (s.ten_vai_tro);

-------------------------------------------------------------------------------
-- 2. Chuc nang / module
-------------------------------------------------------------------------------
MERGE INTO CHUCNANG t USING (SELECT 'USER' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'ROLE' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'BRANCH' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'PRODUCT' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'INGREDIENT' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'SUPPLIER' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'INVENTORY' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'STOCKTAKE' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'WASTAGE' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'ORDER' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'REPORT' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'UNIT' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'WAREHOUSE' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);
MERGE INTO CHUCNANG t USING (SELECT 'RECIPE' ten_chuc_nang FROM dual) s ON (t.ten_chuc_nang = s.ten_chuc_nang)
WHEN NOT MATCHED THEN INSERT (ten_chuc_nang) VALUES (s.ten_chuc_nang);

-------------------------------------------------------------------------------
-- 2.1. Cap nhat constraint hanh_dong de cho phep STOCKTAKE:MANAGE
-------------------------------------------------------------------------------
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE QUYEN DROP CONSTRAINT CK_QUYEN_HANH_DONG';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2443 THEN
            RAISE;
        END IF;
END;
/

ALTER TABLE QUYEN ADD CONSTRAINT CK_QUYEN_HANH_DONG CHECK (
    hanh_dong IN (
        'VIEW', 'CREATE', 'UPDATE', 'DELETE',
        'IMPORT', 'EXPORT', 'TRANSFER', 'ADJUST',
        'PAY', 'CANCEL', 'REFUND', 'MANAGE'
    )
);
-------------------------------------------------------------------------------
-- 3. Quyen
-------------------------------------------------------------------------------
MERGE INTO QUYEN q USING (SELECT 'USER' module, 'VIEW' action, 'Xem người dùng' name FROM dual) s
ON (q.ma_chuc_nang = (SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module) AND q.hanh_dong = s.action)
WHEN MATCHED THEN UPDATE SET q.ten_quyen = s.name
WHEN NOT MATCHED THEN INSERT (ma_chuc_nang, hanh_dong, ten_quyen) VALUES ((SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module), s.action, s.name);
MERGE INTO QUYEN q USING (SELECT 'USER' module, 'CREATE' action, 'Tạo người dùng' name FROM dual) s
ON (q.ma_chuc_nang = (SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module) AND q.hanh_dong = s.action)
WHEN MATCHED THEN UPDATE SET q.ten_quyen = s.name
WHEN NOT MATCHED THEN INSERT (ma_chuc_nang, hanh_dong, ten_quyen) VALUES ((SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module), s.action, s.name);

MERGE INTO QUYEN q USING (SELECT 'ROLE' module, 'VIEW' action, 'Xem vai trò' name FROM dual) s
ON (q.ma_chuc_nang = (SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module) AND q.hanh_dong = s.action)
WHEN MATCHED THEN UPDATE SET q.ten_quyen = s.name
WHEN NOT MATCHED THEN INSERT (ma_chuc_nang, hanh_dong, ten_quyen) VALUES ((SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module), s.action, s.name);
MERGE INTO QUYEN q USING (SELECT 'ROLE' module, 'CREATE' action, 'Tạo vai trò' name FROM dual) s
ON (q.ma_chuc_nang = (SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module) AND q.hanh_dong = s.action)
WHEN MATCHED THEN UPDATE SET q.ten_quyen = s.name
WHEN NOT MATCHED THEN INSERT (ma_chuc_nang, hanh_dong, ten_quyen) VALUES ((SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module), s.action, s.name);
MERGE INTO QUYEN q USING (SELECT 'ROLE' module, 'UPDATE' action, 'Sửa vai trò' name FROM dual) s
ON (q.ma_chuc_nang = (SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module) AND q.hanh_dong = s.action)
WHEN MATCHED THEN UPDATE SET q.ten_quyen = s.name
WHEN NOT MATCHED THEN INSERT (ma_chuc_nang, hanh_dong, ten_quyen) VALUES ((SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module), s.action, s.name);
MERGE INTO QUYEN q USING (SELECT 'ROLE' module, 'DELETE' action, 'Xóa vai trò' name FROM dual) s
ON (q.ma_chuc_nang = (SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module) AND q.hanh_dong = s.action)
WHEN MATCHED THEN UPDATE SET q.ten_quyen = s.name
WHEN NOT MATCHED THEN INSERT (ma_chuc_nang, hanh_dong, ten_quyen) VALUES ((SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module), s.action, s.name);

MERGE INTO QUYEN q USING (SELECT 'BRANCH' module, 'VIEW' action, 'Xem chi nhánh' name FROM dual) s
ON (q.ma_chuc_nang = (SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module) AND q.hanh_dong = s.action)
WHEN MATCHED THEN UPDATE SET q.ten_quyen = s.name
WHEN NOT MATCHED THEN INSERT (ma_chuc_nang, hanh_dong, ten_quyen) VALUES ((SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module), s.action, s.name);
MERGE INTO QUYEN q USING (SELECT 'BRANCH' module, 'CREATE' action, 'Tạo chi nhánh' name FROM dual) s
ON (q.ma_chuc_nang = (SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module) AND q.hanh_dong = s.action)
WHEN MATCHED THEN UPDATE SET q.ten_quyen = s.name
WHEN NOT MATCHED THEN INSERT (ma_chuc_nang, hanh_dong, ten_quyen) VALUES ((SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module), s.action, s.name);
MERGE INTO QUYEN q USING (SELECT 'BRANCH' module, 'UPDATE' action, 'Sửa chi nhánh' name FROM dual) s
ON (q.ma_chuc_nang = (SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module) AND q.hanh_dong = s.action)
WHEN MATCHED THEN UPDATE SET q.ten_quyen = s.name
WHEN NOT MATCHED THEN INSERT (ma_chuc_nang, hanh_dong, ten_quyen) VALUES ((SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module), s.action, s.name);

MERGE INTO QUYEN q USING (SELECT module, action, name FROM (
    SELECT 'PRODUCT' module, 'VIEW' action, 'Xem sản phẩm' name FROM dual UNION ALL
    SELECT 'RECIPE', 'VIEW', 'Xem công thức' FROM dual UNION ALL
    SELECT 'RECIPE', 'MANAGE', 'Quản lý công thức' FROM dual UNION ALL
    SELECT 'INGREDIENT', 'VIEW', 'Xem nguyên liệu' FROM dual UNION ALL
    SELECT 'INGREDIENT', 'CREATE', 'Tạo nguyên liệu' FROM dual UNION ALL
    SELECT 'INGREDIENT', 'UPDATE', 'Sửa nguyên liệu' FROM dual UNION ALL
    SELECT 'INGREDIENT', 'DELETE', 'Xóa nguyên liệu' FROM dual UNION ALL
    SELECT 'SUPPLIER', 'VIEW', 'Xem nhà cung cấp' FROM dual UNION ALL
    SELECT 'SUPPLIER', 'CREATE', 'Tạo nhà cung cấp' FROM dual UNION ALL
    SELECT 'SUPPLIER', 'UPDATE', 'Sửa nhà cung cấp' FROM dual UNION ALL
    SELECT 'SUPPLIER', 'DELETE', 'Xóa nhà cung cấp' FROM dual UNION ALL
    SELECT 'WAREHOUSE', 'VIEW', 'Xem kho' FROM dual UNION ALL
    SELECT 'WAREHOUSE', 'CREATE', 'Tạo kho' FROM dual UNION ALL
    SELECT 'WAREHOUSE', 'UPDATE', 'Sửa kho' FROM dual UNION ALL
    SELECT 'WAREHOUSE', 'DELETE', 'Ngưng hoạt động kho' FROM dual UNION ALL
    SELECT 'INVENTORY', 'VIEW', 'Xem tồn kho' FROM dual UNION ALL
    SELECT 'INVENTORY', 'IMPORT', 'Nhập kho' FROM dual UNION ALL
    SELECT 'INVENTORY', 'EXPORT', 'Xuất kho' FROM dual UNION ALL
    SELECT 'INVENTORY', 'TRANSFER', 'Điều chuyển kho' FROM dual UNION ALL
    SELECT 'INVENTORY', 'ADJUST', 'Điều chỉnh tồn kho' FROM dual UNION ALL
    SELECT 'STOCKTAKE', 'VIEW', 'Xem kiểm kho' FROM dual UNION ALL
    SELECT 'STOCKTAKE', 'MANAGE', 'Quản lý phiếu kiểm kho' FROM dual UNION ALL
    SELECT 'WASTAGE', 'VIEW', 'Xem hao hụt' FROM dual UNION ALL
    SELECT 'WASTAGE', 'CREATE', 'Báo hao hụt' FROM dual UNION ALL
    SELECT 'ORDER', 'VIEW', 'Xem đơn hàng' FROM dual UNION ALL
    SELECT 'ORDER', 'CREATE', 'Tạo đơn hàng' FROM dual UNION ALL
    SELECT 'ORDER', 'PAY', 'Thanh toán đơn' FROM dual UNION ALL
    SELECT 'ORDER', 'CANCEL', 'Hủy đơn hàng' FROM dual UNION ALL
    SELECT 'REPORT', 'VIEW', 'Xem bao cao thong ke' FROM dual UNION ALL
    SELECT 'UNIT', 'VIEW', 'Xem đơn vị tính' FROM dual UNION ALL
    SELECT 'UNIT', 'CREATE', 'Tạo đơn vị tính' FROM dual UNION ALL
    SELECT 'UNIT', 'UPDATE', 'Sửa đơn vị tính' FROM dual UNION ALL
    SELECT 'UNIT', 'DELETE', 'Xóa đơn vị tính' FROM dual
)) s
ON (q.ma_chuc_nang = (SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module) AND q.hanh_dong = s.action)
WHEN MATCHED THEN UPDATE SET q.ten_quyen = s.name
WHEN NOT MATCHED THEN INSERT (ma_chuc_nang, hanh_dong, ten_quyen)
VALUES ((SELECT ma_chuc_nang FROM CHUCNANG WHERE ten_chuc_nang = s.module), s.action, s.name);

-------------------------------------------------------------------------------
-- 3.1. Don quyen khong con dung
-------------------------------------------------------------------------------
DELETE FROM VAITRO_QUYEN
WHERE ma_quyen IN (
    SELECT q.ma_quyen
    FROM QUYEN q
    JOIN CHUCNANG cn ON cn.ma_chuc_nang = q.ma_chuc_nang
    WHERE (cn.ten_chuc_nang = 'ORDER' AND q.hanh_dong = 'REFUND')
       OR (cn.ten_chuc_nang = 'PRODUCT' AND q.hanh_dong IN ('CREATE','UPDATE','DELETE'))
       OR (cn.ten_chuc_nang = 'USER' AND q.hanh_dong IN ('UPDATE','DELETE'))
       OR (cn.ten_chuc_nang = 'WASTAGE' AND q.hanh_dong = 'UPDATE')
);

DELETE FROM QUYEN q
WHERE EXISTS (
    SELECT 1
    FROM CHUCNANG cn
    WHERE cn.ma_chuc_nang = q.ma_chuc_nang
      AND (
          (cn.ten_chuc_nang = 'ORDER' AND q.hanh_dong = 'REFUND')
       OR (cn.ten_chuc_nang = 'PRODUCT' AND q.hanh_dong IN ('CREATE','UPDATE','DELETE'))
       OR (cn.ten_chuc_nang = 'USER' AND q.hanh_dong IN ('UPDATE','DELETE'))
       OR (cn.ten_chuc_nang = 'WASTAGE' AND q.hanh_dong = 'UPDATE')
      )
);

-------------------------------------------------------------------------------
-- 4. Gan quyen cho vai tro
-------------------------------------------------------------------------------
-- ADMIN: toan bo quyen
INSERT INTO VAITRO_QUYEN (ma_vai_tro, ma_quyen)
SELECT vt.ma_vai_tro, q.ma_quyen
FROM VAITRO vt CROSS JOIN QUYEN q
WHERE vt.ten_vai_tro = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM VAITRO_QUYEN x
      WHERE x.ma_vai_tro = vt.ma_vai_tro AND x.ma_quyen = q.ma_quyen
  );

-- Don quyen cu khong con thuoc cac vai tro nghiep vu
DELETE FROM VAITRO_QUYEN vtq
WHERE EXISTS (
    SELECT 1
    FROM VAITRO vt
    JOIN QUYEN q ON q.ma_quyen = vtq.ma_quyen
    JOIN CHUCNANG cn ON cn.ma_chuc_nang = q.ma_chuc_nang
    WHERE vt.ma_vai_tro = vtq.ma_vai_tro
      AND (
          (vt.ten_vai_tro = 'QUAN_LY_KHO'
           AND ((cn.ten_chuc_nang = 'PRODUCT' AND q.hanh_dong = 'VIEW')
             OR (cn.ten_chuc_nang = 'ORDER' AND q.hanh_dong = 'VIEW')
             OR (cn.ten_chuc_nang = 'REPORT' AND q.hanh_dong = 'VIEW')))
       OR (vt.ten_vai_tro IN ('QUAN_LY_CHI_NHANH','THU_NGAN')
           AND cn.ten_chuc_nang = 'REPORT'
           AND q.hanh_dong = 'VIEW')
      )
);

-- QUAN_LY_KHO
INSERT INTO VAITRO_QUYEN (ma_vai_tro, ma_quyen)
SELECT vt.ma_vai_tro, q.ma_quyen
FROM VAITRO vt
JOIN QUYEN q ON 1 = 1
JOIN CHUCNANG cn ON cn.ma_chuc_nang = q.ma_chuc_nang
WHERE vt.ten_vai_tro = 'QUAN_LY_KHO'
  AND (
      (cn.ten_chuc_nang = 'INVENTORY' AND q.hanh_dong IN ('VIEW','IMPORT','EXPORT','TRANSFER','ADJUST'))
      OR (cn.ten_chuc_nang = 'STOCKTAKE' AND q.hanh_dong IN ('VIEW','MANAGE'))
      OR (cn.ten_chuc_nang = 'WASTAGE' AND q.hanh_dong IN ('VIEW','CREATE'))
      OR (cn.ten_chuc_nang = 'SUPPLIER' AND q.hanh_dong IN ('VIEW','CREATE','UPDATE','DELETE'))
      OR (cn.ten_chuc_nang = 'INGREDIENT' AND q.hanh_dong IN ('VIEW','CREATE','UPDATE','DELETE'))
      OR (cn.ten_chuc_nang = 'BRANCH' AND q.hanh_dong IN ('VIEW'))
      OR (cn.ten_chuc_nang = 'UNIT' AND q.hanh_dong IN ('VIEW','CREATE','UPDATE','DELETE'))
      OR (cn.ten_chuc_nang = 'WAREHOUSE' AND q.hanh_dong IN ('VIEW','CREATE','UPDATE','DELETE'))
  )
  AND NOT EXISTS (
      SELECT 1 FROM VAITRO_QUYEN x
      WHERE x.ma_vai_tro = vt.ma_vai_tro AND x.ma_quyen = q.ma_quyen
  );

-- QUAN_LY_CHI_NHANH
INSERT INTO VAITRO_QUYEN (ma_vai_tro, ma_quyen)
SELECT vt.ma_vai_tro, q.ma_quyen
FROM VAITRO vt
JOIN QUYEN q ON 1 = 1
JOIN CHUCNANG cn ON cn.ma_chuc_nang = q.ma_chuc_nang
WHERE vt.ten_vai_tro = 'QUAN_LY_CHI_NHANH'
  AND (
      (cn.ten_chuc_nang = 'USER' AND q.hanh_dong IN ('VIEW','CREATE'))
      OR (cn.ten_chuc_nang = 'BRANCH' AND q.hanh_dong IN ('VIEW'))
      OR (cn.ten_chuc_nang = 'INVENTORY' AND q.hanh_dong IN ('VIEW'))
      OR (cn.ten_chuc_nang = 'STOCKTAKE' AND q.hanh_dong IN ('VIEW','MANAGE'))
      OR (cn.ten_chuc_nang = 'WASTAGE' AND q.hanh_dong IN ('VIEW','CREATE'))
      OR (cn.ten_chuc_nang = 'ORDER' AND q.hanh_dong IN ('VIEW','CREATE','PAY','CANCEL'))
      OR (cn.ten_chuc_nang = 'INGREDIENT' AND q.hanh_dong IN ('VIEW'))
      OR (cn.ten_chuc_nang = 'PRODUCT' AND q.hanh_dong IN ('VIEW'))
      OR (cn.ten_chuc_nang = 'UNIT' AND q.hanh_dong IN ('VIEW'))
      OR (cn.ten_chuc_nang = 'RECIPE' AND q.hanh_dong IN ('VIEW'))
  )
  AND NOT EXISTS (
      SELECT 1 FROM VAITRO_QUYEN x
      WHERE x.ma_vai_tro = vt.ma_vai_tro AND x.ma_quyen = q.ma_quyen
  );

-- THU_NGAN
INSERT INTO VAITRO_QUYEN (ma_vai_tro, ma_quyen)
SELECT vt.ma_vai_tro, q.ma_quyen
FROM VAITRO vt
JOIN QUYEN q ON 1 = 1
JOIN CHUCNANG cn ON cn.ma_chuc_nang = q.ma_chuc_nang
WHERE vt.ten_vai_tro = 'THU_NGAN'
  AND (
      (cn.ten_chuc_nang = 'ORDER' AND q.hanh_dong IN ('VIEW','CREATE','PAY','CANCEL'))
      OR (cn.ten_chuc_nang = 'PRODUCT' AND q.hanh_dong IN ('VIEW'))
  )
  AND NOT EXISTS (
      SELECT 1 FROM VAITRO_QUYEN x
      WHERE x.ma_vai_tro = vt.ma_vai_tro AND x.ma_quyen = q.ma_quyen
  );

-------------------------------------------------------------------------------
-- 5. 8 chi nhanh mau
-------------------------------------------------------------------------------
MERGE INTO CHINHANH t
USING (SELECT 'Bến Thành' ten, '12 Nguyễn Huệ, Quận 1, TP.HCM' dia_chi, '02838220001' sdt FROM dual) s
ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_chi_nhanh = s.ten, t.dia_chi = s.dia_chi, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_chi_nhanh, dia_chi, so_dien_thoai, trang_thai) VALUES (s.ten, s.dia_chi, s.sdt, 'ACTIVE');

MERGE INTO CHINHANH t
USING (SELECT 'Phố đi bộ Nguyễn Huệ' ten, '45 Lê Lợi, Quận 1, TP.HCM' dia_chi, '02838220002' sdt FROM dual) s
ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_chi_nhanh = s.ten, t.dia_chi = s.dia_chi, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_chi_nhanh, dia_chi, so_dien_thoai, trang_thai) VALUES (s.ten, s.dia_chi, s.sdt, 'ACTIVE');

MERGE INTO CHINHANH t
USING (SELECT 'Landmark 81' ten, '210 Võ Văn Tần, Quận 3, TP.HCM' dia_chi, '02838220003' sdt FROM dual) s
ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_chi_nhanh = s.ten, t.dia_chi = s.dia_chi, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_chi_nhanh, dia_chi, so_dien_thoai, trang_thai) VALUES (s.ten, s.dia_chi, s.sdt, 'ACTIVE');

MERGE INTO CHINHANH t
USING (SELECT 'Giga Mall' ten, '88 Phan Xích Long, Phú Nhuận, TP.HCM' dia_chi, '02838220004' sdt FROM dual) s
ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_chi_nhanh = s.ten, t.dia_chi = s.dia_chi, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_chi_nhanh, dia_chi, so_dien_thoai, trang_thai) VALUES (s.ten, s.dia_chi, s.sdt, 'ACTIVE');

MERGE INTO CHINHANH t
USING (SELECT 'Thảo Điền' ten, '32 Trần Não, TP Thủ Đức, TP.HCM' dia_chi, '02838220005' sdt FROM dual) s
ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_chi_nhanh = s.ten, t.dia_chi = s.dia_chi, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_chi_nhanh, dia_chi, so_dien_thoai, trang_thai) VALUES (s.ten, s.dia_chi, s.sdt, 'ACTIVE');

MERGE INTO CHINHANH t
USING (SELECT 'Quang Trung' ten, '156 Quang Trung, Gò Vấp, TP.HCM' dia_chi, '02838220006' sdt FROM dual) s
ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_chi_nhanh = s.ten, t.dia_chi = s.dia_chi, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_chi_nhanh, dia_chi, so_dien_thoai, trang_thai) VALUES (s.ten, s.dia_chi, s.sdt, 'ACTIVE');

MERGE INTO CHINHANH t
USING (SELECT 'Aeon Mall Bình Tân' ten, '25 Tên Lửa, Bình Tân, TP.HCM' dia_chi, '02838220007' sdt FROM dual) s
ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_chi_nhanh = s.ten, t.dia_chi = s.dia_chi, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_chi_nhanh, dia_chi, so_dien_thoai, trang_thai) VALUES (s.ten, s.dia_chi, s.sdt, 'ACTIVE');

MERGE INTO CHINHANH t
USING (SELECT 'Nguyễn Trãi' ten, '301 Nguyễn Trãi, Quận 5, TP.HCM' dia_chi, '02838220008' sdt FROM dual) s
ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_chi_nhanh = s.ten, t.dia_chi = s.dia_chi, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_chi_nhanh, dia_chi, so_dien_thoai, trang_thai) VALUES (s.ten, s.dia_chi, s.sdt, 'ACTIVE');

-------------------------------------------------------------------------------
-- 6. POS cho moi chi nhanh
-------------------------------------------------------------------------------
INSERT INTO MAYPOS (ma_chi_nhanh, ma_thiet_bi, trang_thai_thiet_bi)
SELECT cn.ma_chi_nhanh,
       'POS-CN-' || cn.ma_chi_nhanh,
       'OFFLINE'
FROM CHINHANH cn
WHERE cn.ten_chi_nhanh IN (
    'Bến Thành',
    'Phố đi bộ Nguyễn Huệ',
    'Landmark 81',
    'Giga Mall',
    'Thảo Điền',
    'Quang Trung',
    'Aeon Mall Bình Tân',
    'Nguyễn Trãi'
)
AND NOT EXISTS (
    SELECT 1
    FROM MAYPOS p
    WHERE p.ma_chi_nhanh = cn.ma_chi_nhanh
);
-------------------------------------------------------------------------------
-- 7. Kho tong va kho chi nhanh
-------------------------------------------------------------------------------
MERGE INTO KHO t
USING (
    SELECT 'Kho tổng Phụng Lộc' ten_kho,
           'CENTRAL' loai_kho,
           CAST(NULL AS NUMBER(19)) ma_chi_nhanh
    FROM dual
) s
ON (t.loai_kho = s.loai_kho AND t.ma_chi_nhanh IS NULL)
WHEN MATCHED THEN UPDATE SET t.ten_kho = s.ten_kho, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN
    INSERT (ten_kho, loai_kho, ma_chi_nhanh, trang_thai)
    VALUES (s.ten_kho, s.loai_kho, s.ma_chi_nhanh, 'ACTIVE');

INSERT INTO KHO (ten_kho, loai_kho, ma_chi_nhanh, trang_thai)
SELECT 'Kho ' || cn.ten_chi_nhanh,
       'BRANCH',
       cn.ma_chi_nhanh,
       'ACTIVE'
FROM CHINHANH cn
WHERE cn.ten_chi_nhanh IN (
    'Bến Thành',
    'Phố đi bộ Nguyễn Huệ',
    'Landmark 81',
    'Giga Mall',
    'Thảo Điền',
    'Quang Trung',
    'Aeon Mall Bình Tân',
    'Nguyễn Trãi'
)
AND NOT EXISTS (
    SELECT 1
    FROM KHO k
    WHERE k.ma_chi_nhanh = cn.ma_chi_nhanh
);

UPDATE KHO k
SET ten_kho = (
        SELECT 'Kho ' || cn.ten_chi_nhanh
        FROM CHINHANH cn
        WHERE cn.ma_chi_nhanh = k.ma_chi_nhanh
    ),
    trang_thai = 'ACTIVE'
WHERE k.loai_kho = 'BRANCH'
  AND EXISTS (
      SELECT 1
      FROM CHINHANH cn
      WHERE cn.ma_chi_nhanh = k.ma_chi_nhanh
  );

COMMIT;

-------------------------------------------------------------------------------
-- 8. Nguoi dung mau
-- Mat khau demo cho cac user ben duoi: 123456
-------------------------------------------------------------------------------
MERGE INTO NGUOIDUNG t
USING (
    SELECT 'admin01' ten_dang_nhap, 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=' mat_khau, 'admin01@phungloc.local' email, 'ADMIN' ten_vai_tro, CAST(NULL AS VARCHAR2(150)) ten_chi_nhanh FROM dual UNION ALL
    SELECT 'qlkho01', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'qlkho01@phungloc.local', 'QUAN_LY_KHO', CAST(NULL AS VARCHAR2(150)) FROM dual UNION ALL
    SELECT 'qlcn_nguyenhue', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'qlcn.nguyenhue@phungloc.local', 'QUAN_LY_CHI_NHANH', 'Bến Thành' FROM dual UNION ALL
    SELECT 'thungan_nguyenhue', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'thungan.nguyenhue@phungloc.local', 'THU_NGAN', 'Bến Thành' FROM dual UNION ALL
    SELECT 'qlcn_leloi', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'qlcn.leloi@phungloc.local', 'QUAN_LY_CHI_NHANH', 'Phố đi bộ Nguyễn Huệ' FROM dual UNION ALL
    SELECT 'thungan_leloi', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'thungan.leloi@phungloc.local', 'THU_NGAN', 'Phố đi bộ Nguyễn Huệ' FROM dual UNION ALL
    SELECT 'qlcn_vovantan', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'qlcn.vovantan@phungloc.local', 'QUAN_LY_CHI_NHANH', 'Landmark 81' FROM dual UNION ALL
    SELECT 'thungan_vovantan', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'thungan.vovantan@phungloc.local', 'THU_NGAN', 'Landmark 81' FROM dual UNION ALL
    SELECT 'qlcn_phanxichlong', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'qlcn.phanxichlong@phungloc.local', 'QUAN_LY_CHI_NHANH', 'Giga Mall' FROM dual UNION ALL
    SELECT 'thungan_phanxichlong', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'thungan.phanxichlong@phungloc.local', 'THU_NGAN', 'Giga Mall' FROM dual UNION ALL
    SELECT 'qlcn_trannao', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'qlcn.trannao@phungloc.local', 'QUAN_LY_CHI_NHANH', 'Thảo Điền' FROM dual UNION ALL
    SELECT 'thungan_trannao', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'thungan.trannao@phungloc.local', 'THU_NGAN', 'Thảo Điền' FROM dual UNION ALL
    SELECT 'qlcn_quangtrung', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'qlcn.quangtrung@phungloc.local', 'QUAN_LY_CHI_NHANH', 'Quang Trung' FROM dual UNION ALL
    SELECT 'thungan_quangtrung', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'thungan.quangtrung@phungloc.local', 'THU_NGAN', 'Quang Trung' FROM dual UNION ALL
    SELECT 'qlcn_binhtan', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'qlcn.binhtan@phungloc.local', 'QUAN_LY_CHI_NHANH', 'Aeon Mall Bình Tân' FROM dual UNION ALL
    SELECT 'thungan_binhtan', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'thungan.binhtan@phungloc.local', 'THU_NGAN', 'Aeon Mall Bình Tân' FROM dual UNION ALL
    SELECT 'qlcn_nguyentrai', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'qlcn.nguyentrai@phungloc.local', 'QUAN_LY_CHI_NHANH', 'Nguyễn Trãi' FROM dual UNION ALL
    SELECT 'thungan_nguyentrai', 'pbkdf2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$RW1MdHjwRlvsM3gqxz4WMQ+c4iddFH8xtK/JW3SlD8A=', 'thungan.nguyentrai@phungloc.local', 'THU_NGAN', 'Nguyễn Trãi' FROM dual
) s
ON (t.ten_dang_nhap = s.ten_dang_nhap)
WHEN MATCHED THEN UPDATE SET
    t.email = s.email,
    t.ma_vai_tro = (SELECT ma_vai_tro FROM VAITRO WHERE ten_vai_tro = s.ten_vai_tro),
    t.ma_chi_nhanh = (SELECT ma_chi_nhanh FROM CHINHANH WHERE ten_chi_nhanh = s.ten_chi_nhanh),
    t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_dang_nhap, mat_khau, email, ma_vai_tro, ma_chi_nhanh, trang_thai)
VALUES (
    s.ten_dang_nhap,
    s.mat_khau,
    s.email,
    (SELECT ma_vai_tro FROM VAITRO WHERE ten_vai_tro = s.ten_vai_tro),
    (SELECT ma_chi_nhanh FROM CHINHANH WHERE ten_chi_nhanh = s.ten_chi_nhanh),
    'ACTIVE'
);

-------------------------------------------------------------------------------
-- 9. Don vi tinh, nguyen lieu, san pham, cong thuc
-------------------------------------------------------------------------------
MERGE INTO DONVITINH t USING (SELECT 'Gram' ten, 'g' ky_hieu FROM dual) s ON (t.ky_hieu = s.ky_hieu)
WHEN MATCHED THEN UPDATE SET t.ten_don_vi_tinh = s.ten
WHEN NOT MATCHED THEN INSERT (ten_don_vi_tinh, ky_hieu) VALUES (s.ten, s.ky_hieu);
MERGE INTO DONVITINH t USING (SELECT 'Mililit' ten, 'ml' ky_hieu FROM dual) s ON (t.ky_hieu = s.ky_hieu)
WHEN MATCHED THEN UPDATE SET t.ten_don_vi_tinh = s.ten
WHEN NOT MATCHED THEN INSERT (ten_don_vi_tinh, ky_hieu) VALUES (s.ten, s.ky_hieu);
MERGE INTO DONVITINH t USING (SELECT 'Cái' ten, 'cai' ky_hieu FROM dual) s ON (t.ky_hieu = s.ky_hieu)
WHEN MATCHED THEN UPDATE SET t.ten_don_vi_tinh = s.ten
WHEN NOT MATCHED THEN INSERT (ten_don_vi_tinh, ky_hieu) VALUES (s.ten, s.ky_hieu);
MERGE INTO DONVITINH t USING (SELECT 'Chai' ten, 'chai' ky_hieu FROM dual) s ON (t.ky_hieu = s.ky_hieu)
WHEN MATCHED THEN UPDATE SET t.ten_don_vi_tinh = s.ten
WHEN NOT MATCHED THEN INSERT (ten_don_vi_tinh, ky_hieu) VALUES (s.ten, s.ky_hieu);
MERGE INTO DONVITINH t USING (SELECT 'Gói' ten, 'goi' ky_hieu FROM dual) s ON (t.ky_hieu = s.ky_hieu)
WHEN MATCHED THEN UPDATE SET t.ten_don_vi_tinh = s.ten
WHEN NOT MATCHED THEN INSERT (ten_don_vi_tinh, ky_hieu) VALUES (s.ten, s.ky_hieu);

-- Don vi bo sung cho bao cao Power BI
MERGE INTO DONVITINH t USING (SELECT 'Kilogram' ten, 'kg' ky_hieu FROM dual) s ON (t.ky_hieu = s.ky_hieu)
WHEN MATCHED THEN UPDATE SET t.ten_don_vi_tinh = s.ten
WHEN NOT MATCHED THEN INSERT (ten_don_vi_tinh, ky_hieu) VALUES (s.ten, s.ky_hieu);
MERGE INTO DONVITINH t USING (SELECT 'Thùng' ten, 'thung' ky_hieu FROM dual) s ON (t.ky_hieu = s.ky_hieu)
WHEN MATCHED THEN UPDATE SET t.ten_don_vi_tinh = s.ten
WHEN NOT MATCHED THEN INSERT (ten_don_vi_tinh, ky_hieu) VALUES (s.ten, s.ky_hieu);

MERGE INTO NGUYENLIEU t USING (SELECT 'Cà phê hạt Arabica' ten, 'g' dvt, 5000 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Cà phê hạt Robusta' ten, 'g' dvt, 8000 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Sữa tươi' ten, 'ml' dvt, 10000 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Đường cát' ten, 'g' dvt, 4000 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Trà đen' ten, 'g' dvt, 3000 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Trân châu đen' ten, 'g' dvt, 2500 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');

-- Nguyen lieu bo sung cho bao cao Power BI
MERGE INTO NGUYENLIEU t USING (SELECT 'Bột cacao' ten, 'g' dvt, 3000 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Bột matcha' ten, 'g' dvt, 2500 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Siro caramel' ten, 'ml' dvt, 2500 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Siro vanilla' ten, 'ml' dvt, 2500 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Kem béo thực vật' ten, 'ml' dvt, 5000 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Đào ngâm' ten, 'g' dvt, 4000 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Ly giấy 16oz' ten, 'cai' dvt, 500 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Nắp nhựa 16oz' ten, 'cai' dvt, 500 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');
MERGE INTO NGUYENLIEU t USING (SELECT 'Ống hút giấy' ten, 'cai' dvt, 500 min_qty FROM dual) s ON (t.ten_nguyen_lieu = s.ten)
WHEN MATCHED THEN UPDATE SET t.ma_don_vi_tinh = (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), t.muc_ton_toi_thieu = s.min_qty, t.trang_thai = 'ACTIVE'
WHEN NOT MATCHED THEN INSERT (ten_nguyen_lieu, ma_don_vi_tinh, muc_ton_toi_thieu, trang_thai) VALUES (s.ten, (SELECT ma_don_vi_tinh FROM DONVITINH WHERE ky_hieu = s.dvt), s.min_qty, 'ACTIVE');

MERGE INTO SANPHAM t
USING (
    SELECT
        'Cà phê sữa đá' ten,
        35000 gia,
        'https://placehold.co/600x400?text=Ca+phe+sua+da' hinh
    FROM dual
) s
ON (t.ten_san_pham = s.ten)
WHEN MATCHED THEN UPDATE SET
    t.gia_ban_hien_tai = s.gia,
    t.hinh_anh = s.hinh,
    t.trang_thai = 'AVAILABLE'
WHEN NOT MATCHED THEN INSERT
    (ten_san_pham, gia_ban_hien_tai, hinh_anh, trang_thai)
    VALUES (s.ten, s.gia, s.hinh, 'AVAILABLE');

MERGE INTO SANPHAM t
USING (
    SELECT
        'Bạc xỉu' ten,
        39000 gia,
        'https://placehold.co/600x400?text=Bac+xiu' hinh
    FROM dual
) s
ON (t.ten_san_pham = s.ten)
WHEN MATCHED THEN UPDATE SET
    t.gia_ban_hien_tai = s.gia,
    t.hinh_anh = s.hinh,
    t.trang_thai = 'AVAILABLE'
WHEN NOT MATCHED THEN INSERT
    (ten_san_pham, gia_ban_hien_tai, hinh_anh, trang_thai)
    VALUES (s.ten, s.gia, s.hinh, 'AVAILABLE');

MERGE INTO SANPHAM t
USING (
    SELECT
        'Latte đá' ten,
        45000 gia,
        'https://placehold.co/600x400?text=Latte+da' hinh
    FROM dual
) s
ON (t.ten_san_pham = s.ten)
WHEN MATCHED THEN UPDATE SET
    t.gia_ban_hien_tai = s.gia,
    t.hinh_anh = s.hinh,
    t.trang_thai = 'AVAILABLE'
WHEN NOT MATCHED THEN INSERT
    (ten_san_pham, gia_ban_hien_tai, hinh_anh, trang_thai)
    VALUES (s.ten, s.gia, s.hinh, 'AVAILABLE');

MERGE INTO SANPHAM t
USING (
    SELECT
        'Trà sữa trân châu' ten,
        42000 gia,
        'https://placehold.co/600x400?text=Tra+sua+tran+chau' hinh
    FROM dual
) s
ON (t.ten_san_pham = s.ten)
WHEN MATCHED THEN UPDATE SET
    t.gia_ban_hien_tai = s.gia,
    t.hinh_anh = s.hinh,
    t.trang_thai = 'AVAILABLE'
WHEN NOT MATCHED THEN INSERT
    (ten_san_pham, gia_ban_hien_tai, hinh_anh, trang_thai)
    VALUES (s.ten, s.gia, s.hinh, 'AVAILABLE');

-- San pham bo sung cho bao cao Power BI

MERGE INTO SANPHAM t
USING (
    SELECT
        'Americano đá' ten,
        39000 gia,
        'https://placehold.co/600x400?text=Americano+da' hinh
    FROM dual
) s
ON (t.ten_san_pham = s.ten)
WHEN MATCHED THEN UPDATE SET
    t.gia_ban_hien_tai = s.gia,
    t.hinh_anh = s.hinh,
    t.trang_thai = 'AVAILABLE'
WHEN NOT MATCHED THEN INSERT
    (ten_san_pham, gia_ban_hien_tai, hinh_anh, trang_thai)
    VALUES (s.ten, s.gia, s.hinh, 'AVAILABLE');

MERGE INTO SANPHAM t
USING (
    SELECT
        'Cacao sữa đá' ten,
        45000 gia,
        'https://placehold.co/600x400?text=Cacao+sua+da' hinh
    FROM dual
) s
ON (t.ten_san_pham = s.ten)
WHEN MATCHED THEN UPDATE SET
    t.gia_ban_hien_tai = s.gia,
    t.hinh_anh = s.hinh,
    t.trang_thai = 'AVAILABLE'
WHEN NOT MATCHED THEN INSERT
    (ten_san_pham, gia_ban_hien_tai, hinh_anh, trang_thai)
    VALUES (s.ten, s.gia, s.hinh, 'AVAILABLE');

MERGE INTO SANPHAM t
USING (
    SELECT
        'Caramel macchiato' ten,
        59000 gia,
        'https://placehold.co/600x400?text=Caramel+macchiato' hinh
    FROM dual
) s
ON (t.ten_san_pham = s.ten)
WHEN MATCHED THEN UPDATE SET
    t.gia_ban_hien_tai = s.gia,
    t.hinh_anh = s.hinh,
    t.trang_thai = 'AVAILABLE'
WHEN NOT MATCHED THEN INSERT
    (ten_san_pham, gia_ban_hien_tai, hinh_anh, trang_thai)
    VALUES (s.ten, s.gia, s.hinh, 'AVAILABLE');

MERGE INTO SANPHAM t
USING (
    SELECT
        'Matcha latte' ten,
        55000 gia,
        'https://placehold.co/600x400?text=Matcha+latte' hinh
    FROM dual
) s
ON (t.ten_san_pham = s.ten)
WHEN MATCHED THEN UPDATE SET
    t.gia_ban_hien_tai = s.gia,
    t.hinh_anh = s.hinh,
    t.trang_thai = 'AVAILABLE'
WHEN NOT MATCHED THEN INSERT
    (ten_san_pham, gia_ban_hien_tai, hinh_anh, trang_thai)
    VALUES (s.ten, s.gia, s.hinh, 'AVAILABLE');

MERGE INTO SANPHAM t
USING (
    SELECT
        'Trà đào' ten,
        49000 gia,
        'https://placehold.co/600x400?text=Tra+dao' hinh
    FROM dual
) s
ON (t.ten_san_pham = s.ten)
WHEN MATCHED THEN UPDATE SET
    t.gia_ban_hien_tai = s.gia,
    t.hinh_anh = s.hinh,
    t.trang_thai = 'AVAILABLE'
WHEN NOT MATCHED THEN INSERT
    (ten_san_pham, gia_ban_hien_tai, hinh_anh, trang_thai)
    VALUES (s.ten, s.gia, s.hinh, 'AVAILABLE');

MERGE INTO CONGTHUC_SANPHAM t USING (
    SELECT 'Cà phê sữa đá' sp, 'Cà phê hạt Robusta' nl, 20 qty FROM dual UNION ALL
    SELECT 'Cà phê sữa đá', 'Sữa tươi', 40 FROM dual UNION ALL
    SELECT 'Cà phê sữa đá', 'Đường cát', 12 FROM dual UNION ALL
    SELECT 'Bạc xỉu', 'Cà phê hạt Robusta', 12 FROM dual UNION ALL
    SELECT 'Bạc xỉu', 'Sữa tươi', 90 FROM dual UNION ALL
    SELECT 'Latte đá', 'Cà phê hạt Arabica', 18 FROM dual UNION ALL
    SELECT 'Latte đá', 'Sữa tươi', 120 FROM dual UNION ALL
    SELECT 'Trà sữa trân châu', 'Trà đen', 8 FROM dual UNION ALL
    SELECT 'Trà sữa trân châu', 'Sữa tươi', 100 FROM dual UNION ALL
    SELECT 'Trà sữa trân châu', 'Trân châu đen', 45 FROM dual
) s
ON (t.ma_san_pham = (SELECT ma_san_pham FROM SANPHAM WHERE ten_san_pham = s.sp)
    AND t.ma_nguyen_lieu = (SELECT ma_nguyen_lieu FROM NGUYENLIEU WHERE ten_nguyen_lieu = s.nl))
WHEN MATCHED THEN UPDATE SET t.so_luong_can = s.qty
WHEN NOT MATCHED THEN INSERT (ma_san_pham, ma_nguyen_lieu, so_luong_can)
VALUES ((SELECT ma_san_pham FROM SANPHAM WHERE ten_san_pham = s.sp), (SELECT ma_nguyen_lieu FROM NGUYENLIEU WHERE ten_nguyen_lieu = s.nl), s.qty);

-- Cong thuc bo sung cho bao cao Power BI
MERGE INTO CONGTHUC_SANPHAM t USING (
    SELECT 'Americano đá' sp, 'Cà phê hạt Arabica' nl, 16 qty FROM dual UNION ALL
    SELECT 'Americano đá', 'Ly giấy 16oz', 1 FROM dual UNION ALL
    SELECT 'Americano đá', 'Nắp nhựa 16oz', 1 FROM dual UNION ALL
    SELECT 'Americano đá', 'Ống hút giấy', 1 FROM dual UNION ALL
    SELECT 'Cacao sữa đá', 'Bột cacao', 18 FROM dual UNION ALL
    SELECT 'Cacao sữa đá', 'Sữa tươi', 120 FROM dual UNION ALL
    SELECT 'Cacao sữa đá', 'Đường cát', 10 FROM dual UNION ALL
    SELECT 'Cacao sữa đá', 'Ly giấy 16oz', 1 FROM dual UNION ALL
    SELECT 'Cacao sữa đá', 'Nắp nhựa 16oz', 1 FROM dual UNION ALL
    SELECT 'Caramel macchiato', 'Cà phê hạt Arabica', 18 FROM dual UNION ALL
    SELECT 'Caramel macchiato', 'Sữa tươi', 120 FROM dual UNION ALL
    SELECT 'Caramel macchiato', 'Siro caramel', 20 FROM dual UNION ALL
    SELECT 'Caramel macchiato', 'Kem béo thực vật', 30 FROM dual UNION ALL
    SELECT 'Caramel macchiato', 'Ly giấy 16oz', 1 FROM dual UNION ALL
    SELECT 'Caramel macchiato', 'Nắp nhựa 16oz', 1 FROM dual UNION ALL
    SELECT 'Matcha latte', 'Bột matcha', 14 FROM dual UNION ALL
    SELECT 'Matcha latte', 'Sữa tươi', 150 FROM dual UNION ALL
    SELECT 'Matcha latte', 'Đường cát', 10 FROM dual UNION ALL
    SELECT 'Matcha latte', 'Ly giấy 16oz', 1 FROM dual UNION ALL
    SELECT 'Matcha latte', 'Nắp nhựa 16oz', 1 FROM dual UNION ALL
    SELECT 'Trà đào', 'Trà đen', 8 FROM dual UNION ALL
    SELECT 'Trà đào', 'Đào ngâm', 60 FROM dual UNION ALL
    SELECT 'Trà đào', 'Đường cát', 8 FROM dual UNION ALL
    SELECT 'Trà đào', 'Ly giấy 16oz', 1 FROM dual UNION ALL
    SELECT 'Trà đào', 'Nắp nhựa 16oz', 1 FROM dual
) s
ON (t.ma_san_pham = (SELECT ma_san_pham FROM SANPHAM WHERE ten_san_pham = s.sp)
    AND t.ma_nguyen_lieu = (SELECT ma_nguyen_lieu FROM NGUYENLIEU WHERE ten_nguyen_lieu = s.nl))
WHEN MATCHED THEN UPDATE SET t.so_luong_can = s.qty
WHEN NOT MATCHED THEN INSERT (ma_san_pham, ma_nguyen_lieu, so_luong_can)
VALUES ((SELECT ma_san_pham FROM SANPHAM WHERE ten_san_pham = s.sp), (SELECT ma_nguyen_lieu FROM NGUYENLIEU WHERE ten_nguyen_lieu = s.nl), s.qty);

-------------------------------------------------------------------------------
-- 10. Nha cung cap
-------------------------------------------------------------------------------
MERGE INTO NHACUNGCAP t USING (SELECT 'Cà Phê Cao Nguyên' ten, '0901000001' sdt, 'caphe@demo.local' email, 'Lam Dong' dia_chi FROM dual) s ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_nha_cung_cap = s.ten, t.email = s.email, t.dia_chi = s.dia_chi
WHEN NOT MATCHED THEN INSERT (ten_nha_cung_cap, so_dien_thoai, email, dia_chi) VALUES (s.ten, s.sdt, s.email, s.dia_chi);
MERGE INTO NHACUNGCAP t USING (SELECT 'Sữa Thực Phẩm Sài Gòn' ten, '0901000002' sdt, 'sua@demo.local' email, 'TP.HCM' dia_chi FROM dual) s ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_nha_cung_cap = s.ten, t.email = s.email, t.dia_chi = s.dia_chi
WHEN NOT MATCHED THEN INSERT (ten_nha_cung_cap, so_dien_thoai, email, dia_chi) VALUES (s.ten, s.sdt, s.email, s.dia_chi);
MERGE INTO NHACUNGCAP t USING (SELECT 'Nguyên Liệu Trà Sữa' ten, '0901000003' sdt, 'trasua@demo.local' email, 'Binh Duong' dia_chi FROM dual) s ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_nha_cung_cap = s.ten, t.email = s.email, t.dia_chi = s.dia_chi
WHEN NOT MATCHED THEN INSERT (ten_nha_cung_cap, so_dien_thoai, email, dia_chi) VALUES (s.ten, s.sdt, s.email, s.dia_chi);

-- Nha cung cap bo sung cho bao cao Power BI
MERGE INTO NHACUNGCAP t USING (SELECT 'Bao Bì Xanh' ten, '0901000004' sdt, 'baobi@demo.local' email, 'KCN Tân Bình, TP.HCM' dia_chi FROM dual) s ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_nha_cung_cap = s.ten, t.email = s.email, t.dia_chi = s.dia_chi
WHEN NOT MATCHED THEN INSERT (ten_nha_cung_cap, so_dien_thoai, email, dia_chi) VALUES (s.ten, s.sdt, s.email, s.dia_chi);
MERGE INTO NHACUNGCAP t USING (SELECT 'Hương Liệu Việt' ten, '0901000005' sdt, 'huonglieu@demo.local' email, 'Quận 7, TP.HCM' dia_chi FROM dual) s ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_nha_cung_cap = s.ten, t.email = s.email, t.dia_chi = s.dia_chi
WHEN NOT MATCHED THEN INSERT (ten_nha_cung_cap, so_dien_thoai, email, dia_chi) VALUES (s.ten, s.sdt, s.email, s.dia_chi);
MERGE INTO NHACUNGCAP t USING (SELECT 'Trái Cây Sài Gòn' ten, '0901000006' sdt, 'traicay@demo.local' email, 'Thủ Đức, TP.HCM' dia_chi FROM dual) s ON (t.so_dien_thoai = s.sdt)
WHEN MATCHED THEN UPDATE SET t.ten_nha_cung_cap = s.ten, t.email = s.email, t.dia_chi = s.dia_chi
WHEN NOT MATCHED THEN INSERT (ten_nha_cung_cap, so_dien_thoai, email, dia_chi) VALUES (s.ten, s.sdt, s.email, s.dia_chi);

-------------------------------------------------------------------------------
-- 11. Ton kho ban dau cho tat ca kho
-------------------------------------------------------------------------------
MERGE INTO TONKHO t
USING (
    SELECT k.ma_kho, nl.ma_nguyen_lieu,
           CASE WHEN k.loai_kho = 'CENTRAL' THEN 50000 ELSE 7000 END so_luong_ton
    FROM KHO k
    JOIN NGUYENLIEU nl ON nl.ten_nguyen_lieu IN ('Cà phê hạt Arabica','Cà phê hạt Robusta','Sữa tươi','Đường cát','Trà đen','Trân châu đen')
) s
ON (t.ma_kho = s.ma_kho AND t.ma_nguyen_lieu = s.ma_nguyen_lieu)
WHEN MATCHED THEN UPDATE SET t.so_luong_ton = s.so_luong_ton, t.lan_cap_nhat_cuoi = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ma_kho, ma_nguyen_lieu, so_luong_ton, lan_cap_nhat_cuoi)
VALUES (s.ma_kho, s.ma_nguyen_lieu, s.so_luong_ton, CURRENT_TIMESTAMP);

-------------------------------------------------------------------------------
-- 12. Phieu nhap, chi tiet phieu nhap, lo hang
-------------------------------------------------------------------------------
INSERT INTO PHIEUNHAP (ma_kho, ma_nha_cung_cap, ngay_nhap, tong_tien, nguoi_tao, ghi_chu)
SELECT (SELECT ma_kho FROM KHO WHERE ten_kho = 'Kho tổng Phụng Lộc'),
       (SELECT ma_nha_cung_cap FROM NHACUNGCAP WHERE ten_nha_cung_cap = 'Cà Phê Cao Nguyên'),
       TIMESTAMP '2026-05-10 08:00:00',
       5625000,
       (SELECT ma_nguoi_dung FROM NGUOIDUNG WHERE ten_dang_nhap = 'qlkho01'),
       'SEED_NHAP_001'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM PHIEUNHAP WHERE ghi_chu = 'SEED_NHAP_001');

UPDATE PHIEUNHAP
SET ngay_nhap = TIMESTAMP '2026-05-10 08:00:00',
    tong_tien = 5625000
WHERE ghi_chu = 'SEED_NHAP_001';

INSERT INTO CHITIETPHIEUNHAP (ma_phieu_nhap, ma_nguyen_lieu, so_luong_nhap, don_gia_nhap, so_lo, han_su_dung)
SELECT pn.ma_phieu_nhap, nl.ma_nguyen_lieu, s.qty, s.price, s.so_lo, s.hsd
FROM PHIEUNHAP pn
JOIN (
    SELECT 'Cà phê hạt Arabica' nl, 20000 qty, 120 price, 'SEED-AR-001' so_lo, DATE '2027-12-31' hsd FROM dual UNION ALL
    SELECT 'Cà phê hạt Robusta', 30000, 95000 / 1000, 'SEED-RO-001', DATE '2027-12-31' FROM dual UNION ALL
    SELECT 'Đường cát', 15000, 25000 / 1000, 'SEED-DU-001', DATE '2027-06-30' FROM dual
) s ON 1 = 1
JOIN NGUYENLIEU nl ON nl.ten_nguyen_lieu = s.nl
WHERE pn.ghi_chu = 'SEED_NHAP_001'
  AND NOT EXISTS (
      SELECT 1 FROM CHITIETPHIEUNHAP c
      WHERE c.ma_phieu_nhap = pn.ma_phieu_nhap AND c.ma_nguyen_lieu = nl.ma_nguyen_lieu AND c.so_lo = s.so_lo
  );

INSERT INTO LOHANG_NGUYENLIEU (ma_kho, ma_nguyen_lieu, ma_ct_phieu_nhap, so_luong_con_lai, trang_thai, han_su_dung)
SELECT pn.ma_kho, c.ma_nguyen_lieu, c.ma_ct_phieu_nhap, c.so_luong_nhap, 'ACTIVE', c.han_su_dung
FROM CHITIETPHIEUNHAP c
JOIN PHIEUNHAP pn ON pn.ma_phieu_nhap = c.ma_phieu_nhap
WHERE pn.ghi_chu = 'SEED_NHAP_001'
  AND NOT EXISTS (SELECT 1 FROM LOHANG_NGUYENLIEU lh WHERE lh.ma_ct_phieu_nhap = c.ma_ct_phieu_nhap);

-------------------------------------------------------------------------------
-- 13. Phieu xuat, dieu chuyen, kiem kho, hao hut
-------------------------------------------------------------------------------
INSERT INTO PHIEUXUAT (ma_kho, ngay_xuat, loai_xuat, tong_gia_tri_xuat, nguoi_tao, trang_thai, ghi_chu)
SELECT (SELECT ma_kho FROM KHO WHERE ten_kho = 'Kho tổng Phụng Lộc'),
       TIMESTAMP '2026-05-10 14:00:00',
       'TRAINING',
       120000,
       (SELECT ma_nguoi_dung FROM NGUOIDUNG WHERE ten_dang_nhap = 'qlkho01'),
       'COMPLETED',
       'SEED_XUAT_001'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM PHIEUXUAT WHERE ghi_chu = 'SEED_XUAT_001');

UPDATE PHIEUXUAT
SET ngay_xuat = TIMESTAMP '2026-05-10 14:00:00',
    trang_thai = 'COMPLETED',
    tong_gia_tri_xuat = 120000
WHERE ghi_chu = 'SEED_XUAT_001';

INSERT INTO CHITIETPHIEUXUAT (ma_phieu_xuat, ma_nguyen_lieu, so_luong_xuat, don_gia_xuat)
SELECT px.ma_phieu_xuat, nl.ma_nguyen_lieu, 1000, 120
FROM PHIEUXUAT px
JOIN NGUYENLIEU nl ON nl.ten_nguyen_lieu = 'Cà phê hạt Arabica'
WHERE px.ghi_chu = 'SEED_XUAT_001'
  AND NOT EXISTS (SELECT 1 FROM CHITIETPHIEUXUAT c WHERE c.ma_phieu_xuat = px.ma_phieu_xuat AND c.ma_nguyen_lieu = nl.ma_nguyen_lieu);

UPDATE CHITIETPHIEUXUAT c
SET ma_lo_hang = (
        SELECT lh.ma_lo_hang
        FROM LOHANG_NGUYENLIEU lh
        JOIN CHITIETPHIEUNHAP cpn ON cpn.ma_ct_phieu_nhap = lh.ma_ct_phieu_nhap
        WHERE cpn.so_lo = 'SEED-AR-001'
    )
WHERE c.ma_nguyen_lieu = (SELECT ma_nguyen_lieu FROM NGUYENLIEU WHERE ten_nguyen_lieu = 'Cà phê hạt Arabica')
  AND EXISTS (
      SELECT 1
      FROM PHIEUXUAT px
      WHERE px.ma_phieu_xuat = c.ma_phieu_xuat
        AND px.ghi_chu = 'SEED_XUAT_001'
  );

INSERT INTO PHIEUDIEUCHUYEN (ma_kho_nguon, ma_kho_dich, ngay_tao, trang_thai, nguoi_tao, ghi_chu)
SELECT (SELECT ma_kho FROM KHO WHERE ten_kho = 'Kho tổng Phụng Lộc'),
       (SELECT k.ma_kho FROM KHO k JOIN CHINHANH cn ON cn.ma_chi_nhanh = k.ma_chi_nhanh WHERE cn.ten_chi_nhanh = 'Bến Thành'),
       TIMESTAMP '2026-05-10 16:00:00',
       'COMPLETED',
       (SELECT ma_nguoi_dung FROM NGUOIDUNG WHERE ten_dang_nhap = 'qlkho01'),
       'SEED_DC_001'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM PHIEUDIEUCHUYEN WHERE ghi_chu = 'SEED_DC_001');

UPDATE PHIEUDIEUCHUYEN
SET ngay_tao = TIMESTAMP '2026-05-10 16:00:00',
    trang_thai = 'COMPLETED'
WHERE ghi_chu = 'SEED_DC_001';

INSERT INTO CHITIETPHIEUDIEUCHUYEN (ma_phieu_dieu_chuyen, ma_nguyen_lieu, so_luong_dieu_chuyen)
SELECT pdc.ma_phieu_dieu_chuyen, nl.ma_nguyen_lieu, 3000
FROM PHIEUDIEUCHUYEN pdc
JOIN NGUYENLIEU nl ON nl.ten_nguyen_lieu = 'Cà phê hạt Robusta'
WHERE pdc.ghi_chu = 'SEED_DC_001'
  AND NOT EXISTS (SELECT 1 FROM CHITIETPHIEUDIEUCHUYEN c WHERE c.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen AND c.ma_nguyen_lieu = nl.ma_nguyen_lieu);

UPDATE CHITIETPHIEUDIEUCHUYEN c
SET ma_lo_hang_nguon = (
        SELECT lh.ma_lo_hang
        FROM LOHANG_NGUYENLIEU lh
        JOIN CHITIETPHIEUNHAP cpn ON cpn.ma_ct_phieu_nhap = lh.ma_ct_phieu_nhap
        WHERE cpn.so_lo = 'SEED-RO-001'
    )
WHERE c.ma_nguyen_lieu = (SELECT ma_nguyen_lieu FROM NGUYENLIEU WHERE ten_nguyen_lieu = 'Cà phê hạt Robusta')
  AND EXISTS (
      SELECT 1
      FROM PHIEUDIEUCHUYEN pdc
      WHERE pdc.ma_phieu_dieu_chuyen = c.ma_phieu_dieu_chuyen
        AND pdc.ghi_chu = 'SEED_DC_001'
  );

INSERT INTO LOHANG_NGUYENLIEU (ma_kho, ma_nguyen_lieu, ma_ct_phieu_nhap, so_luong_con_lai, trang_thai, han_su_dung)
SELECT pdc.ma_kho_dich,
       c.ma_nguyen_lieu,
       NULL,
       c.so_luong_dieu_chuyen,
       'ACTIVE',
       lh_nguon.han_su_dung
FROM PHIEUDIEUCHUYEN pdc
JOIN CHITIETPHIEUDIEUCHUYEN c ON c.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
JOIN LOHANG_NGUYENLIEU lh_nguon ON lh_nguon.ma_lo_hang = c.ma_lo_hang_nguon
WHERE pdc.ghi_chu = 'SEED_DC_001'
  AND c.ma_lo_hang_dich IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM LOHANG_NGUYENLIEU lh
      WHERE lh.ma_kho = pdc.ma_kho_dich
        AND lh.ma_nguyen_lieu = c.ma_nguyen_lieu
        AND lh.ma_ct_phieu_nhap IS NULL
        AND (
            lh.han_su_dung = lh_nguon.han_su_dung
            OR (lh.han_su_dung IS NULL AND lh_nguon.han_su_dung IS NULL)
        )
  );

UPDATE CHITIETPHIEUDIEUCHUYEN c
SET ma_lo_hang_dich = (
        SELECT MIN(lh_dich.ma_lo_hang)
        FROM PHIEUDIEUCHUYEN pdc
        JOIN LOHANG_NGUYENLIEU lh_dich
            ON lh_dich.ma_kho = pdc.ma_kho_dich
           AND lh_dich.ma_nguyen_lieu = c.ma_nguyen_lieu
           AND lh_dich.ma_ct_phieu_nhap IS NULL
        JOIN LOHANG_NGUYENLIEU lh_nguon
            ON lh_nguon.ma_lo_hang = c.ma_lo_hang_nguon
        WHERE pdc.ma_phieu_dieu_chuyen = c.ma_phieu_dieu_chuyen
          AND pdc.ghi_chu = 'SEED_DC_001'
          AND (
              lh_dich.han_su_dung = lh_nguon.han_su_dung
              OR (lh_dich.han_su_dung IS NULL AND lh_nguon.han_su_dung IS NULL)
          )
    )
WHERE c.ma_lo_hang_nguon IS NOT NULL
  AND EXISTS (
      SELECT 1
      FROM PHIEUDIEUCHUYEN pdc
      WHERE pdc.ma_phieu_dieu_chuyen = c.ma_phieu_dieu_chuyen
        AND pdc.ghi_chu = 'SEED_DC_001'
  );

INSERT INTO PHIEUKIEMKHO (ma_kho, ngay_kiem_kho, nguoi_kiem, trang_thai, ghi_chu)
SELECT (SELECT k.ma_kho FROM KHO k JOIN CHINHANH cn ON cn.ma_chi_nhanh = k.ma_chi_nhanh WHERE cn.ten_chi_nhanh = 'Bến Thành'),
       TIMESTAMP '2026-05-11 22:00:00',
       (SELECT ma_nguoi_dung FROM NGUOIDUNG WHERE ten_dang_nhap = 'qlcn_nguyenhue'),
       'COMPLETED',
       'SEED_KIEM_001'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM PHIEUKIEMKHO WHERE ghi_chu = 'SEED_KIEM_001');

UPDATE PHIEUKIEMKHO
SET ngay_kiem_kho = TIMESTAMP '2026-05-11 22:00:00',
    trang_thai = 'COMPLETED'
WHERE ghi_chu = 'SEED_KIEM_001';

INSERT INTO CHITIETPHIEUKIEMKHO (ma_phieu_kiem_kho, ma_nguyen_lieu, so_luong_he_thong, so_luong_thuc_te, so_luong_chenh_lech, ty_le_chenh_lech, ly_do_chenh_lech, huong_xu_ly)
SELECT pkk.ma_phieu_kiem_kho, nl.ma_nguyen_lieu, 6860, 6810, -50, -0.0073, 'Lech nho sau ca lam viec', 'CREATE_WASTAGE'
FROM PHIEUKIEMKHO pkk
JOIN NGUYENLIEU nl ON nl.ten_nguyen_lieu = 'Sữa tươi'
WHERE pkk.ghi_chu = 'SEED_KIEM_001'
  AND NOT EXISTS (SELECT 1 FROM CHITIETPHIEUKIEMKHO c WHERE c.ma_phieu_kiem_kho = pkk.ma_phieu_kiem_kho AND c.ma_nguyen_lieu = nl.ma_nguyen_lieu);

UPDATE CHITIETPHIEUKIEMKHO c
SET so_luong_he_thong = 6860,
    so_luong_thuc_te = 6810,
    so_luong_chenh_lech = -50,
    ty_le_chenh_lech = -0.0073,
    huong_xu_ly = 'CREATE_WASTAGE'
WHERE c.ma_nguyen_lieu = (SELECT ma_nguyen_lieu FROM NGUYENLIEU WHERE ten_nguyen_lieu = 'Sữa tươi')
  AND EXISTS (
      SELECT 1
      FROM PHIEUKIEMKHO pkk
      WHERE pkk.ma_phieu_kiem_kho = c.ma_phieu_kiem_kho
        AND pkk.ghi_chu = 'SEED_KIEM_001'
  );

INSERT INTO PHIEUHAOHUT (ma_kho, ma_nguyen_lieu, so_luong_hao_hut, loai_hao_hut, ngay_hao_hut, ghi_chu, nguoi_bao_cao)
SELECT (SELECT k.ma_kho FROM KHO k JOIN CHINHANH cn ON cn.ma_chi_nhanh = k.ma_chi_nhanh WHERE cn.ten_chi_nhanh = 'Bến Thành'),
       (SELECT ma_nguyen_lieu FROM NGUYENLIEU WHERE ten_nguyen_lieu = 'Sữa tươi'),
       50,
       'SPILL',
       TIMESTAMP '2026-05-11 22:05:00',
       'SEED_HAOHUT_001',
       (SELECT ma_nguoi_dung FROM NGUOIDUNG WHERE ten_dang_nhap = 'qlcn_nguyenhue')
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM PHIEUHAOHUT WHERE ghi_chu = 'SEED_HAOHUT_001');

UPDATE PHIEUHAOHUT
SET ngay_hao_hut = TIMESTAMP '2026-05-11 22:05:00',
    so_luong_hao_hut = 50
WHERE ghi_chu = 'SEED_HAOHUT_001';

-------------------------------------------------------------------------------
-- 14. Hoa don POS, chi tiet hoa don, ban hang tru kho
-------------------------------------------------------------------------------
INSERT INTO HOADON (ma_chi_nhanh, ma_pos, ma_nguoi_dung, thoi_gian_tao_hoa_don, trang_thai_hoa_don, tong_thanh_toan, phuong_thuc_thanh_toan, thoi_gian_thanh_toan, trang_thai_thanh_toan)
SELECT cn.ma_chi_nhanh,
       p.ma_pos,
       (SELECT ma_nguoi_dung FROM NGUOIDUNG WHERE ten_dang_nhap = s.thu_ngan),
       s.thoi_gian_tao,
       'COMPLETED',
       77000,
       'CASH',
       s.thoi_gian_tao + INTERVAL '1' MINUTE,
       'SUCCESS'
FROM (
    SELECT 'Bến Thành' ten_chi_nhanh, 'thungan_nguyenhue' thu_ngan, TIMESTAMP '2026-05-11 08:30:00' thoi_gian_tao FROM dual UNION ALL
    SELECT 'Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-11 09:00:00' FROM dual UNION ALL
    SELECT 'Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-11 09:30:00' FROM dual UNION ALL
    SELECT 'Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-11 10:00:00' FROM dual UNION ALL
    SELECT 'Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-11 10:30:00' FROM dual UNION ALL
    SELECT 'Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-11 11:00:00' FROM dual UNION ALL
    SELECT 'Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-11 11:30:00' FROM dual UNION ALL
    SELECT 'Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-11 12:00:00' FROM dual
) s
JOIN CHINHANH cn ON cn.ten_chi_nhanh = s.ten_chi_nhanh
JOIN MAYPOS p ON p.ma_chi_nhanh = cn.ma_chi_nhanh
WHERE NOT EXISTS (
      SELECT 1 FROM HOADON h
      WHERE h.ma_pos = p.ma_pos AND h.thoi_gian_tao_hoa_don = s.thoi_gian_tao
  );

INSERT INTO CHITIETHOADON (ma_hoa_don, ma_san_pham, so_luong, don_gia_ban, thanh_tien_dong)
SELECT h.ma_hoa_don, sp.ma_san_pham, s.qty, s.price, s.qty * s.price
FROM HOADON h
JOIN (
    SELECT 'Bến Thành' ten_chi_nhanh, TIMESTAMP '2026-05-11 08:30:00' thoi_gian_tao FROM dual UNION ALL
    SELECT 'Phố đi bộ Nguyễn Huệ', TIMESTAMP '2026-05-11 09:00:00' FROM dual UNION ALL
    SELECT 'Landmark 81', TIMESTAMP '2026-05-11 09:30:00' FROM dual UNION ALL
    SELECT 'Giga Mall', TIMESTAMP '2026-05-11 10:00:00' FROM dual UNION ALL
    SELECT 'Thảo Điền', TIMESTAMP '2026-05-11 10:30:00' FROM dual UNION ALL
    SELECT 'Quang Trung', TIMESTAMP '2026-05-11 11:00:00' FROM dual UNION ALL
    SELECT 'Aeon Mall Bình Tân', TIMESTAMP '2026-05-11 11:30:00' FROM dual UNION ALL
    SELECT 'Nguyễn Trãi', TIMESTAMP '2026-05-11 12:00:00' FROM dual
) hd ON hd.thoi_gian_tao = h.thoi_gian_tao_hoa_don
JOIN CHINHANH cn ON cn.ma_chi_nhanh = h.ma_chi_nhanh AND cn.ten_chi_nhanh = hd.ten_chi_nhanh
JOIN (
    SELECT 'Cà phê sữa đá' sp, 1 qty, 35000 price FROM dual UNION ALL
    SELECT 'Trà sữa trân châu', 1, 42000 FROM dual
) s ON 1 = 1
JOIN SANPHAM sp ON sp.ten_san_pham = s.sp
WHERE NOT EXISTS (
      SELECT 1 FROM CHITIETHOADON c
      WHERE c.ma_hoa_don = h.ma_hoa_don AND c.ma_san_pham = sp.ma_san_pham
  );

INSERT INTO BANHANG_TRUKHO (ma_hoa_don, ma_ct_hoa_don, ma_kho, ma_nguyen_lieu, so_luong_nguyen_lieu_moi_sp, tong_so_luong_tru, trang_thai)
SELECT h.ma_hoa_don,
       c.ma_ct_hoa_don,
       k.ma_kho,
       nl.ma_nguyen_lieu,
       ct.so_luong_can,
       ct.so_luong_can * c.so_luong,
       'DEDUCTED'
FROM HOADON h
JOIN (
    SELECT 'Bến Thành' ten_chi_nhanh, TIMESTAMP '2026-05-11 08:30:00' thoi_gian_tao FROM dual UNION ALL
    SELECT 'Phố đi bộ Nguyễn Huệ', TIMESTAMP '2026-05-11 09:00:00' FROM dual UNION ALL
    SELECT 'Landmark 81', TIMESTAMP '2026-05-11 09:30:00' FROM dual UNION ALL
    SELECT 'Giga Mall', TIMESTAMP '2026-05-11 10:00:00' FROM dual UNION ALL
    SELECT 'Thảo Điền', TIMESTAMP '2026-05-11 10:30:00' FROM dual UNION ALL
    SELECT 'Quang Trung', TIMESTAMP '2026-05-11 11:00:00' FROM dual UNION ALL
    SELECT 'Aeon Mall Bình Tân', TIMESTAMP '2026-05-11 11:30:00' FROM dual UNION ALL
    SELECT 'Nguyễn Trãi', TIMESTAMP '2026-05-11 12:00:00' FROM dual
) hd ON hd.thoi_gian_tao = h.thoi_gian_tao_hoa_don
JOIN CHINHANH cn ON cn.ma_chi_nhanh = h.ma_chi_nhanh AND cn.ten_chi_nhanh = hd.ten_chi_nhanh
JOIN CHITIETHOADON c ON c.ma_hoa_don = h.ma_hoa_don
JOIN SANPHAM sp ON sp.ma_san_pham = c.ma_san_pham
JOIN CONGTHUC_SANPHAM ct ON ct.ma_san_pham = sp.ma_san_pham
JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = ct.ma_nguyen_lieu
JOIN KHO k ON k.ma_chi_nhanh = h.ma_chi_nhanh
WHERE NOT EXISTS (
      SELECT 1 FROM BANHANG_TRUKHO b
      WHERE b.ma_ct_hoa_don = c.ma_ct_hoa_don AND b.ma_nguyen_lieu = nl.ma_nguyen_lieu
  );

-------------------------------------------------------------------------------
-- 15. Dong bo ton kho theo chung tu seed da hoan tat
-------------------------------------------------------------------------------
MERGE INTO LOHANG_NGUYENLIEU lh
USING (
    SELECT c.ma_ct_phieu_nhap,
           c.so_luong_nhap
           - NVL((
                 SELECT SUM(cpx.so_luong_xuat)
                 FROM PHIEUXUAT px
                 JOIN CHITIETPHIEUXUAT cpx ON cpx.ma_phieu_xuat = px.ma_phieu_xuat
                 WHERE px.ghi_chu = 'SEED_XUAT_001'
                   AND px.trang_thai = 'COMPLETED'
                   AND px.ma_kho = pn.ma_kho
                   AND cpx.ma_nguyen_lieu = c.ma_nguyen_lieu
             ), 0)
           - NVL((
                 SELECT SUM(cdc.so_luong_dieu_chuyen)
                 FROM PHIEUDIEUCHUYEN pdc
                 JOIN CHITIETPHIEUDIEUCHUYEN cdc ON cdc.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
                 WHERE pdc.ghi_chu = 'SEED_DC_001'
                   AND pdc.trang_thai = 'COMPLETED'
                   AND pdc.ma_kho_nguon = pn.ma_kho
                   AND cdc.ma_nguyen_lieu = c.ma_nguyen_lieu
             ), 0) so_luong_con_lai
    FROM CHITIETPHIEUNHAP c
    JOIN PHIEUNHAP pn ON pn.ma_phieu_nhap = c.ma_phieu_nhap
    WHERE pn.ghi_chu = 'SEED_NHAP_001'
) s
ON (lh.ma_ct_phieu_nhap = s.ma_ct_phieu_nhap)
WHEN MATCHED THEN UPDATE SET
    so_luong_con_lai = s.so_luong_con_lai;

UPDATE LOHANG_NGUYENLIEU lh
SET trang_thai = CASE WHEN so_luong_con_lai <= 0 THEN 'USED_UP' ELSE 'ACTIVE' END
WHERE EXISTS (
    SELECT 1
    FROM PHIEUNHAP pn
    JOIN CHITIETPHIEUNHAP c ON c.ma_phieu_nhap = pn.ma_phieu_nhap
    WHERE pn.ghi_chu = 'SEED_NHAP_001'
      AND c.ma_ct_phieu_nhap = lh.ma_ct_phieu_nhap
);

UPDATE BANHANG_TRUKHO b
SET ma_lo_hang = (
        SELECT cdc.ma_lo_hang_dich
        FROM PHIEUDIEUCHUYEN pdc
        JOIN CHITIETPHIEUDIEUCHUYEN cdc ON cdc.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
        WHERE pdc.ghi_chu = 'SEED_DC_001'
          AND pdc.ma_kho_dich = b.ma_kho
          AND cdc.ma_nguyen_lieu = b.ma_nguyen_lieu
          AND cdc.ma_lo_hang_dich IS NOT NULL
    )
WHERE b.trang_thai = 'DEDUCTED'
  AND EXISTS (
      SELECT 1
      FROM PHIEUDIEUCHUYEN pdc
      JOIN CHITIETPHIEUDIEUCHUYEN cdc ON cdc.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
      WHERE pdc.ghi_chu = 'SEED_DC_001'
        AND pdc.ma_kho_dich = b.ma_kho
        AND cdc.ma_nguyen_lieu = b.ma_nguyen_lieu
        AND cdc.ma_lo_hang_dich IS NOT NULL
  );

UPDATE LOHANG_NGUYENLIEU lh
SET so_luong_con_lai = (
        SELECT cdc.so_luong_dieu_chuyen
               - NVL((
                     SELECT SUM(b.tong_so_luong_tru)
                     FROM BANHANG_TRUKHO b
                     JOIN HOADON h ON h.ma_hoa_don = b.ma_hoa_don
                     WHERE b.ma_lo_hang = cdc.ma_lo_hang_dich
                       AND b.trang_thai = 'DEDUCTED'
                       AND h.thoi_gian_tao_hoa_don IN (
                           TIMESTAMP '2026-05-11 08:30:00',
                           TIMESTAMP '2026-05-11 09:00:00',
                           TIMESTAMP '2026-05-11 09:30:00',
                           TIMESTAMP '2026-05-11 10:00:00',
                           TIMESTAMP '2026-05-11 10:30:00',
                           TIMESTAMP '2026-05-11 11:00:00',
                           TIMESTAMP '2026-05-11 11:30:00',
                           TIMESTAMP '2026-05-11 12:00:00'
                       )
                 ), 0)
        FROM PHIEUDIEUCHUYEN pdc
        JOIN CHITIETPHIEUDIEUCHUYEN cdc ON cdc.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
        WHERE pdc.ghi_chu = 'SEED_DC_001'
          AND cdc.ma_lo_hang_dich = lh.ma_lo_hang
    )
WHERE EXISTS (
    SELECT 1
    FROM PHIEUDIEUCHUYEN pdc
    JOIN CHITIETPHIEUDIEUCHUYEN cdc ON cdc.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
    WHERE pdc.ghi_chu = 'SEED_DC_001'
      AND cdc.ma_lo_hang_dich = lh.ma_lo_hang
);

UPDATE LOHANG_NGUYENLIEU lh
SET trang_thai = CASE WHEN so_luong_con_lai <= 0 THEN 'USED_UP' ELSE 'ACTIVE' END
WHERE EXISTS (
    SELECT 1
    FROM PHIEUDIEUCHUYEN pdc
    JOIN CHITIETPHIEUDIEUCHUYEN cdc ON cdc.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
    WHERE pdc.ghi_chu = 'SEED_DC_001'
      AND cdc.ma_lo_hang_dich = lh.ma_lo_hang
);

MERGE INTO TONKHO t
USING (
    WITH seed_hd AS (
        SELECT TIMESTAMP '2026-05-11 08:30:00' thoi_gian_tao FROM dual UNION ALL
        SELECT TIMESTAMP '2026-05-11 09:00:00' FROM dual UNION ALL
        SELECT TIMESTAMP '2026-05-11 09:30:00' FROM dual UNION ALL
        SELECT TIMESTAMP '2026-05-11 10:00:00' FROM dual UNION ALL
        SELECT TIMESTAMP '2026-05-11 10:30:00' FROM dual UNION ALL
        SELECT TIMESTAMP '2026-05-11 11:00:00' FROM dual UNION ALL
        SELECT TIMESTAMP '2026-05-11 11:30:00' FROM dual UNION ALL
        SELECT TIMESTAMP '2026-05-11 12:00:00' FROM dual
    ),
    base AS (
        SELECT k.ma_kho,
               nl.ma_nguyen_lieu,
               CASE WHEN k.loai_kho = 'CENTRAL' THEN 50000 ELSE 7000 END base_qty
        FROM KHO k
        JOIN NGUYENLIEU nl ON nl.ten_nguyen_lieu IN (
            'Cà phê hạt Arabica',
            'Cà phê hạt Robusta',
            'Sữa tươi',
            'Đường cát',
            'Trà đen',
            'Trân châu đen'
        )
        WHERE k.loai_kho = 'CENTRAL' OR k.ma_chi_nhanh IS NOT NULL
    ),
    movements AS (
        SELECT pn.ma_kho, c.ma_nguyen_lieu, c.so_luong_nhap delta_qty
        FROM PHIEUNHAP pn
        JOIN CHITIETPHIEUNHAP c ON c.ma_phieu_nhap = pn.ma_phieu_nhap
        WHERE pn.ghi_chu = 'SEED_NHAP_001'
        UNION ALL
        SELECT px.ma_kho, c.ma_nguyen_lieu, -c.so_luong_xuat
        FROM PHIEUXUAT px
        JOIN CHITIETPHIEUXUAT c ON c.ma_phieu_xuat = px.ma_phieu_xuat
        WHERE px.ghi_chu = 'SEED_XUAT_001'
          AND px.trang_thai = 'COMPLETED'
        UNION ALL
        SELECT pdc.ma_kho_nguon, c.ma_nguyen_lieu, -c.so_luong_dieu_chuyen
        FROM PHIEUDIEUCHUYEN pdc
        JOIN CHITIETPHIEUDIEUCHUYEN c ON c.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
        WHERE pdc.ghi_chu = 'SEED_DC_001'
          AND pdc.trang_thai = 'COMPLETED'
        UNION ALL
        SELECT pdc.ma_kho_dich, c.ma_nguyen_lieu, c.so_luong_dieu_chuyen
        FROM PHIEUDIEUCHUYEN pdc
        JOIN CHITIETPHIEUDIEUCHUYEN c ON c.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
        WHERE pdc.ghi_chu = 'SEED_DC_001'
          AND pdc.trang_thai = 'COMPLETED'
        UNION ALL
        SELECT b.ma_kho, b.ma_nguyen_lieu, -b.tong_so_luong_tru
        FROM BANHANG_TRUKHO b
        JOIN HOADON h ON h.ma_hoa_don = b.ma_hoa_don
        JOIN seed_hd hd ON hd.thoi_gian_tao = h.thoi_gian_tao_hoa_don
        WHERE b.trang_thai = 'DEDUCTED'
        UNION ALL
        SELECT phh.ma_kho, phh.ma_nguyen_lieu, -phh.so_luong_hao_hut
        FROM PHIEUHAOHUT phh
        WHERE phh.ghi_chu = 'SEED_HAOHUT_001'
    )
    SELECT b.ma_kho,
           b.ma_nguyen_lieu,
           b.base_qty + NVL(SUM(m.delta_qty), 0) so_luong_ton
    FROM base b
    LEFT JOIN movements m ON m.ma_kho = b.ma_kho AND m.ma_nguyen_lieu = b.ma_nguyen_lieu
    GROUP BY b.ma_kho, b.ma_nguyen_lieu, b.base_qty
) s
ON (t.ma_kho = s.ma_kho AND t.ma_nguyen_lieu = s.ma_nguyen_lieu)
WHEN MATCHED THEN UPDATE SET
    t.so_luong_ton = s.so_luong_ton,
    t.lan_cap_nhat_cuoi = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ma_kho, ma_nguyen_lieu, so_luong_ton, lan_cap_nhat_cuoi)
VALUES (s.ma_kho, s.ma_nguyen_lieu, s.so_luong_ton, CURRENT_TIMESTAMP);

-------------------------------------------------------------------------------
-- 16. Nhat ky kho mau
-------------------------------------------------------------------------------
DELETE FROM NHATKY_KHO n
WHERE (n.ten_chung_tu = 'PHIEUNHAP'
       AND EXISTS (SELECT 1 FROM PHIEUNHAP pn WHERE pn.ma_phieu_nhap = n.ma_chung_tu AND pn.ghi_chu = 'SEED_NHAP_001'))
   OR (n.ten_chung_tu = 'PHIEUXUAT'
       AND EXISTS (SELECT 1 FROM PHIEUXUAT px WHERE px.ma_phieu_xuat = n.ma_chung_tu AND px.ghi_chu = 'SEED_XUAT_001'))
   OR (n.ten_chung_tu = 'PHIEUDIEUCHUYEN'
       AND EXISTS (SELECT 1 FROM PHIEUDIEUCHUYEN pdc WHERE pdc.ma_phieu_dieu_chuyen = n.ma_chung_tu AND pdc.ghi_chu = 'SEED_DC_001'))
   OR (n.ten_chung_tu = 'PHIEUHAOHUT'
       AND EXISTS (SELECT 1 FROM PHIEUHAOHUT phh WHERE phh.ma_phieu_hao_hut = n.ma_chung_tu AND phh.ghi_chu = 'SEED_HAOHUT_001'))
   OR (n.ten_chung_tu = 'HOADON'
       AND EXISTS (
           SELECT 1
           FROM HOADON h
           WHERE h.ma_hoa_don = n.ma_chung_tu
             AND h.thoi_gian_tao_hoa_don IN (
                 TIMESTAMP '2026-05-11 08:30:00',
                 TIMESTAMP '2026-05-11 09:00:00',
                 TIMESTAMP '2026-05-11 09:30:00',
                 TIMESTAMP '2026-05-11 10:00:00',
                 TIMESTAMP '2026-05-11 10:30:00',
                 TIMESTAMP '2026-05-11 11:00:00',
                 TIMESTAMP '2026-05-11 11:30:00',
                 TIMESTAMP '2026-05-11 12:00:00'
             )
       ));

INSERT INTO NHATKY_KHO (ma_kho, ma_nguyen_lieu, loai_giao_dich, ten_chung_tu, ma_chung_tu, so_luong_thay_doi, so_luong_truoc, so_luong_sau, thoi_gian, nguoi_thao_tac)
SELECT pn.ma_kho,
       c.ma_nguyen_lieu,
       'IMPORT',
       'PHIEUNHAP',
       pn.ma_phieu_nhap,
       c.so_luong_nhap,
       CASE WHEN k.loai_kho = 'CENTRAL' THEN 50000 ELSE 7000 END,
       CASE WHEN k.loai_kho = 'CENTRAL' THEN 50000 ELSE 7000 END + c.so_luong_nhap,
       pn.ngay_nhap,
       pn.nguoi_tao
FROM PHIEUNHAP pn
JOIN CHITIETPHIEUNHAP c ON c.ma_phieu_nhap = pn.ma_phieu_nhap
JOIN KHO k ON k.ma_kho = pn.ma_kho
WHERE pn.ghi_chu = 'SEED_NHAP_001';

INSERT INTO NHATKY_KHO (ma_kho, ma_nguyen_lieu, loai_giao_dich, ten_chung_tu, ma_chung_tu, so_luong_thay_doi, so_luong_truoc, so_luong_sau, thoi_gian, nguoi_thao_tac)
SELECT s.ma_kho,
       s.ma_nguyen_lieu,
       'EXPORT',
       'PHIEUXUAT',
       s.ma_phieu_xuat,
       -s.so_luong_xuat,
       s.so_luong_truoc,
       s.so_luong_truoc - s.so_luong_xuat,
       s.ngay_xuat,
       s.nguoi_tao
FROM (
    SELECT px.ma_phieu_xuat,
           px.ma_kho,
           c.ma_nguyen_lieu,
           c.so_luong_xuat,
           px.ngay_xuat,
           px.nguoi_tao,
           CASE WHEN k.loai_kho = 'CENTRAL' THEN 50000 ELSE 7000 END
           + NVL((
                 SELECT SUM(cpn.so_luong_nhap)
                 FROM PHIEUNHAP pn
                 JOIN CHITIETPHIEUNHAP cpn ON cpn.ma_phieu_nhap = pn.ma_phieu_nhap
                 WHERE pn.ghi_chu = 'SEED_NHAP_001'
                   AND pn.ma_kho = px.ma_kho
                   AND cpn.ma_nguyen_lieu = c.ma_nguyen_lieu
             ), 0) so_luong_truoc
    FROM PHIEUXUAT px
    JOIN CHITIETPHIEUXUAT c ON c.ma_phieu_xuat = px.ma_phieu_xuat
    JOIN KHO k ON k.ma_kho = px.ma_kho
    WHERE px.ghi_chu = 'SEED_XUAT_001'
      AND px.trang_thai = 'COMPLETED'
) s;

INSERT INTO NHATKY_KHO (ma_kho, ma_nguyen_lieu, ma_lo_hang, loai_giao_dich, ten_chung_tu, ma_chung_tu, so_luong_thay_doi, so_luong_truoc, so_luong_sau, thoi_gian, nguoi_thao_tac)
SELECT s.ma_kho,
       s.ma_nguyen_lieu,
       s.ma_lo_hang_nguon,
       'TRANSFER_OUT',
       'PHIEUDIEUCHUYEN',
       s.ma_phieu_dieu_chuyen,
       -s.so_luong_dieu_chuyen,
       s.so_luong_truoc,
       s.so_luong_truoc - s.so_luong_dieu_chuyen,
       s.ngay_tao,
       s.nguoi_tao
FROM (
    SELECT pdc.ma_phieu_dieu_chuyen,
           pdc.ma_kho_nguon ma_kho,
           c.ma_nguyen_lieu,
           c.ma_lo_hang_nguon,
           c.so_luong_dieu_chuyen,
           pdc.ngay_tao,
           pdc.nguoi_tao,
           CASE WHEN k.loai_kho = 'CENTRAL' THEN 50000 ELSE 7000 END
           + NVL((
                 SELECT SUM(cpn.so_luong_nhap)
                 FROM PHIEUNHAP pn
                 JOIN CHITIETPHIEUNHAP cpn ON cpn.ma_phieu_nhap = pn.ma_phieu_nhap
                 WHERE pn.ghi_chu = 'SEED_NHAP_001'
                   AND pn.ma_kho = pdc.ma_kho_nguon
                   AND cpn.ma_nguyen_lieu = c.ma_nguyen_lieu
             ), 0)
           - NVL((
                 SELECT SUM(cpx.so_luong_xuat)
                 FROM PHIEUXUAT px
                 JOIN CHITIETPHIEUXUAT cpx ON cpx.ma_phieu_xuat = px.ma_phieu_xuat
                 WHERE px.ghi_chu = 'SEED_XUAT_001'
                   AND px.trang_thai = 'COMPLETED'
                   AND px.ma_kho = pdc.ma_kho_nguon
                   AND cpx.ma_nguyen_lieu = c.ma_nguyen_lieu
             ), 0) so_luong_truoc
    FROM PHIEUDIEUCHUYEN pdc
    JOIN CHITIETPHIEUDIEUCHUYEN c ON c.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
    JOIN KHO k ON k.ma_kho = pdc.ma_kho_nguon
    WHERE pdc.ghi_chu = 'SEED_DC_001'
      AND pdc.trang_thai = 'COMPLETED'
) s;

INSERT INTO NHATKY_KHO (ma_kho, ma_nguyen_lieu, ma_lo_hang, loai_giao_dich, ten_chung_tu, ma_chung_tu, so_luong_thay_doi, so_luong_truoc, so_luong_sau, thoi_gian, nguoi_thao_tac)
SELECT pdc.ma_kho_dich,
       c.ma_nguyen_lieu,
       c.ma_lo_hang_dich,
       'TRANSFER_IN',
       'PHIEUDIEUCHUYEN',
       pdc.ma_phieu_dieu_chuyen,
       c.so_luong_dieu_chuyen,
       CASE WHEN k.loai_kho = 'CENTRAL' THEN 50000 ELSE 7000 END,
       CASE WHEN k.loai_kho = 'CENTRAL' THEN 50000 ELSE 7000 END + c.so_luong_dieu_chuyen,
       pdc.ngay_tao,
       pdc.nguoi_tao
FROM PHIEUDIEUCHUYEN pdc
JOIN CHITIETPHIEUDIEUCHUYEN c ON c.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
JOIN KHO k ON k.ma_kho = pdc.ma_kho_dich
WHERE pdc.ghi_chu = 'SEED_DC_001'
  AND pdc.trang_thai = 'COMPLETED';

INSERT INTO NHATKY_KHO (ma_kho, ma_nguyen_lieu, ma_lo_hang, loai_giao_dich, ten_chung_tu, ma_chung_tu, so_luong_thay_doi, so_luong_truoc, so_luong_sau, thoi_gian, nguoi_thao_tac)
WITH seed_hd AS (
    SELECT TIMESTAMP '2026-05-11 08:30:00' thoi_gian_tao FROM dual UNION ALL
    SELECT TIMESTAMP '2026-05-11 09:00:00' FROM dual UNION ALL
    SELECT TIMESTAMP '2026-05-11 09:30:00' FROM dual UNION ALL
    SELECT TIMESTAMP '2026-05-11 10:00:00' FROM dual UNION ALL
    SELECT TIMESTAMP '2026-05-11 10:30:00' FROM dual UNION ALL
    SELECT TIMESTAMP '2026-05-11 11:00:00' FROM dual UNION ALL
    SELECT TIMESTAMP '2026-05-11 11:30:00' FROM dual UNION ALL
    SELECT TIMESTAMP '2026-05-11 12:00:00' FROM dual
),
sale_qty AS (
    SELECT b.ma_kho,
           b.ma_nguyen_lieu,
           b.ma_lo_hang,
           b.ma_hoa_don,
           h.thoi_gian_tao_hoa_don,
           h.ma_nguoi_dung,
           SUM(b.tong_so_luong_tru) so_luong_tru
    FROM BANHANG_TRUKHO b
    JOIN HOADON h ON h.ma_hoa_don = b.ma_hoa_don
    JOIN seed_hd hd ON hd.thoi_gian_tao = h.thoi_gian_tao_hoa_don
    WHERE b.trang_thai = 'DEDUCTED'
    GROUP BY b.ma_kho, b.ma_nguyen_lieu, b.ma_lo_hang, b.ma_hoa_don, h.thoi_gian_tao_hoa_don, h.ma_nguoi_dung
),
sale_before AS (
    SELECT sq.*,
           CASE WHEN k.loai_kho = 'CENTRAL' THEN 50000 ELSE 7000 END
           + NVL((
                 SELECT SUM(cdc.so_luong_dieu_chuyen)
                 FROM PHIEUDIEUCHUYEN pdc
                 JOIN CHITIETPHIEUDIEUCHUYEN cdc ON cdc.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
                 WHERE pdc.ghi_chu = 'SEED_DC_001'
                   AND pdc.trang_thai = 'COMPLETED'
                   AND pdc.ma_kho_dich = sq.ma_kho
                   AND cdc.ma_nguyen_lieu = sq.ma_nguyen_lieu
             ), 0) so_luong_truoc
    FROM sale_qty sq
    JOIN KHO k ON k.ma_kho = sq.ma_kho
)
SELECT ma_kho,
       ma_nguyen_lieu,
       ma_lo_hang,
       'SALE_DEDUCT',
       'HOADON',
       ma_hoa_don,
       -so_luong_tru,
       so_luong_truoc,
       so_luong_truoc - so_luong_tru,
       thoi_gian_tao_hoa_don,
       ma_nguoi_dung
FROM sale_before;

INSERT INTO NHATKY_KHO (ma_kho, ma_nguyen_lieu, loai_giao_dich, ten_chung_tu, ma_chung_tu, so_luong_thay_doi, so_luong_truoc, so_luong_sau, thoi_gian, nguoi_thao_tac)
SELECT s.ma_kho,
       s.ma_nguyen_lieu,
       'WASTAGE',
       'PHIEUHAOHUT',
       s.ma_phieu_hao_hut,
       -s.so_luong_hao_hut,
       s.so_luong_truoc,
       s.so_luong_truoc - s.so_luong_hao_hut,
       s.ngay_hao_hut,
       s.nguoi_bao_cao
FROM (
    SELECT phh.ma_phieu_hao_hut,
           phh.ma_kho,
           phh.ma_nguyen_lieu,
           phh.so_luong_hao_hut,
           phh.ngay_hao_hut,
           phh.nguoi_bao_cao,
           CASE WHEN k.loai_kho = 'CENTRAL' THEN 50000 ELSE 7000 END
           + NVL((
                 SELECT SUM(cdc.so_luong_dieu_chuyen)
                 FROM PHIEUDIEUCHUYEN pdc
                 JOIN CHITIETPHIEUDIEUCHUYEN cdc ON cdc.ma_phieu_dieu_chuyen = pdc.ma_phieu_dieu_chuyen
                 WHERE pdc.ghi_chu = 'SEED_DC_001'
                   AND pdc.trang_thai = 'COMPLETED'
                   AND pdc.ma_kho_dich = phh.ma_kho
                   AND cdc.ma_nguyen_lieu = phh.ma_nguyen_lieu
             ), 0)
           - NVL((
                 SELECT SUM(b.tong_so_luong_tru)
                 FROM BANHANG_TRUKHO b
                 JOIN HOADON h ON h.ma_hoa_don = b.ma_hoa_don
                 WHERE b.trang_thai = 'DEDUCTED'
                   AND b.ma_kho = phh.ma_kho
                   AND b.ma_nguyen_lieu = phh.ma_nguyen_lieu
                   AND h.thoi_gian_tao_hoa_don IN (
                       TIMESTAMP '2026-05-11 08:30:00',
                       TIMESTAMP '2026-05-11 09:00:00',
                       TIMESTAMP '2026-05-11 09:30:00',
                       TIMESTAMP '2026-05-11 10:00:00',
                       TIMESTAMP '2026-05-11 10:30:00',
                       TIMESTAMP '2026-05-11 11:00:00',
                       TIMESTAMP '2026-05-11 11:30:00',
                       TIMESTAMP '2026-05-11 12:00:00'
                   )
             ), 0) so_luong_truoc
    FROM PHIEUHAOHUT phh
    JOIN KHO k ON k.ma_kho = phh.ma_kho
    WHERE phh.ghi_chu = 'SEED_HAOHUT_001'
) s;

COMMIT;

-- ============================================================================
-- BEGIN: giao dich kho mo rong cho Power BI
-- ============================================================================
-- ============================================================================
-- Giao dich kho mo rong cho Power BI.
-- Master data bo sung da duoc dua len cac muc 9-10 de file chay nhu mot seed duy nhat.
-- Phan duoi chi tao phieu nhap/xuat/dieu chuyen/POS/kiem kho/hao hut va nhat ky kho.
-- ============================================================================

-- --------------------------------------------------------------------------
-- Helper functions dung rieng cho file seed Power BI.
-- Ly do: Oracle khong cho goi local function trong SQL/MERGE cua anonymous block
-- tren mot so phien ban, nen tao function cap schema de SQL co the goi duoc.
-- --------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION pbi_get_user_id(p_username VARCHAR2) RETURN NUMBER
AS
    v_id NUMBER;
BEGIN
    SELECT ma_nguoi_dung INTO v_id
    FROM NGUOIDUNG
    WHERE ten_dang_nhap = p_username;
    RETURN v_id;
END;
/

CREATE OR REPLACE FUNCTION pbi_get_supplier_id(p_name VARCHAR2) RETURN NUMBER
AS
    v_id NUMBER;
BEGIN
    SELECT ma_nha_cung_cap INTO v_id
    FROM NHACUNGCAP
    WHERE ten_nha_cung_cap = p_name;
    RETURN v_id;
END;
/

CREATE OR REPLACE FUNCTION pbi_get_unit_id(p_symbol VARCHAR2) RETURN NUMBER
AS
    v_id NUMBER;
BEGIN
    SELECT ma_don_vi_tinh INTO v_id
    FROM DONVITINH
    WHERE ky_hieu = p_symbol;
    RETURN v_id;
END;
/

CREATE OR REPLACE FUNCTION pbi_get_ingredient_id(p_name VARCHAR2) RETURN NUMBER
AS
    v_id NUMBER;
BEGIN
    SELECT ma_nguyen_lieu INTO v_id
    FROM NGUYENLIEU
    WHERE ten_nguyen_lieu = p_name;
    RETURN v_id;
END;
/

CREATE OR REPLACE FUNCTION pbi_get_product_id(p_name VARCHAR2) RETURN NUMBER
AS
    v_id NUMBER;
BEGIN
    SELECT ma_san_pham INTO v_id
    FROM SANPHAM
    WHERE ten_san_pham = p_name;
    RETURN v_id;
END;
/

CREATE OR REPLACE FUNCTION pbi_get_branch_id(p_branch_name VARCHAR2) RETURN NUMBER
AS
    v_id NUMBER;
BEGIN
    SELECT ma_chi_nhanh INTO v_id
    FROM CHINHANH
    WHERE ten_chi_nhanh = p_branch_name;
    RETURN v_id;
END;
/

CREATE OR REPLACE FUNCTION pbi_get_branch_kho(p_branch_name VARCHAR2) RETURN NUMBER
AS
    v_id NUMBER;
BEGIN
    SELECT k.ma_kho INTO v_id
    FROM KHO k
    JOIN CHINHANH cn ON cn.ma_chi_nhanh = k.ma_chi_nhanh
    WHERE cn.ten_chi_nhanh = p_branch_name;
    RETURN v_id;
END;
/

CREATE OR REPLACE FUNCTION pbi_get_pos_id(p_branch_name VARCHAR2) RETURN NUMBER
AS
    v_id NUMBER;
BEGIN
    SELECT p.ma_pos INTO v_id
    FROM MAYPOS p
    JOIN CHINHANH cn ON cn.ma_chi_nhanh = p.ma_chi_nhanh
    WHERE cn.ten_chi_nhanh = p_branch_name;
    RETURN v_id;
END;
/


DECLARE
    v_central_kho NUMBER;

    FUNCTION stock_from_lots(p_kho NUMBER, p_ingredient NUMBER) RETURN NUMBER IS
        v_qty NUMBER(18,3);
    BEGIN
        SELECT NVL(SUM(so_luong_con_lai), 0)
        INTO v_qty
        FROM LOHANG_NGUYENLIEU
        WHERE ma_kho = p_kho
          AND ma_nguyen_lieu = p_ingredient
          AND trang_thai = 'ACTIVE'
          AND so_luong_con_lai > 0
          AND (han_su_dung IS NULL OR han_su_dung >= TRUNC(SYSDATE));
        RETURN v_qty;
    END;

    FUNCTION find_available_lot(p_kho NUMBER, p_ingredient NUMBER, p_qty NUMBER) RETURN NUMBER IS
        v_lot NUMBER;
    BEGIN
        SELECT ma_lo_hang INTO v_lot
        FROM (
            SELECT ma_lo_hang
            FROM LOHANG_NGUYENLIEU
            WHERE ma_kho = p_kho
              AND ma_nguyen_lieu = p_ingredient
              AND trang_thai = 'ACTIVE'
              AND so_luong_con_lai >= p_qty
              AND (han_su_dung IS NULL OR han_su_dung >= TRUNC(SYSDATE))
            ORDER BY han_su_dung ASC NULLS LAST, ngay_tao ASC, ma_lo_hang ASC
        )
        WHERE ROWNUM = 1;
        RETURN v_lot;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20001, 'Khong du lo hang cho nguyen lieu ' || p_ingredient || ' tai kho ' || p_kho);
    END;

    PROCEDURE journal_once(
        p_kho NUMBER,
        p_ing NUMBER,
        p_lot NUMBER,
        p_type VARCHAR2,
        p_doc VARCHAR2,
        p_doc_id NUMBER,
        p_delta NUMBER,
        p_before NUMBER,
        p_after NUMBER,
        p_time TIMESTAMP,
        p_user NUMBER
    ) IS
        v_exists NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_exists
        FROM NHATKY_KHO
        WHERE ten_chung_tu = p_doc
          AND ma_chung_tu = p_doc_id
          AND loai_giao_dich = p_type
          AND ma_nguyen_lieu = p_ing
          AND NVL(ma_lo_hang, -1) = NVL(p_lot, -1)
          AND so_luong_thay_doi = p_delta;

        IF v_exists = 0 THEN
            INSERT INTO NHATKY_KHO (
                ma_kho, ma_nguyen_lieu, ma_lo_hang, loai_giao_dich,
                ten_chung_tu, ma_chung_tu, so_luong_thay_doi,
                so_luong_truoc, so_luong_sau, thoi_gian, nguoi_thao_tac
            ) VALUES (
                p_kho, p_ing, p_lot, p_type,
                p_doc, p_doc_id, p_delta,
                GREATEST(p_before, 0), GREATEST(p_after, 0), p_time, p_user
            );
        END IF;
    END;

    -- Header phiếu nhập vẫn cần helper vì nhiều dòng nhập có thể thuộc cùng một phiếu.
    FUNCTION ensure_import_header(p_code VARCHAR2, p_time TIMESTAMP, p_supplier VARCHAR2) RETURN NUMBER IS
        v_id NUMBER;
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM PHIEUNHAP
        WHERE ghi_chu = p_code;

        IF v_count = 0 THEN
            INSERT INTO PHIEUNHAP (ma_kho, ma_nha_cung_cap, ngay_nhap, tong_tien, nguoi_tao, ghi_chu)
            VALUES (v_central_kho, pbi_get_supplier_id(p_supplier), p_time, 0, pbi_get_user_id('qlkho01'), p_code)
            RETURNING ma_phieu_nhap INTO v_id;
        ELSE
            SELECT ma_phieu_nhap INTO v_id
            FROM PHIEUNHAP
            WHERE ghi_chu = p_code;
        END IF;

        RETURN v_id;
    END;

    PROCEDURE add_import_line(p_code VARCHAR2, p_time TIMESTAMP, p_supplier VARCHAR2, p_ing VARCHAR2, p_qty NUMBER, p_price NUMBER, p_lot_no VARCHAR2, p_hsd DATE) IS
        v_receipt NUMBER;
        v_ing NUMBER;
        v_detail NUMBER;
        v_lot NUMBER;
        v_exists NUMBER;
        v_before NUMBER;
    BEGIN
        v_receipt := ensure_import_header(p_code, p_time, p_supplier);
        v_ing := pbi_get_ingredient_id(p_ing);
        SELECT COUNT(*) INTO v_exists FROM CHITIETPHIEUNHAP WHERE ma_phieu_nhap = v_receipt AND so_lo = p_lot_no;
        IF v_exists = 0 THEN
            v_before := stock_from_lots(v_central_kho, v_ing);
            INSERT INTO CHITIETPHIEUNHAP (ma_phieu_nhap, ma_nguyen_lieu, so_luong_nhap, don_gia_nhap, so_lo, han_su_dung)
            VALUES (v_receipt, v_ing, p_qty, p_price, p_lot_no, p_hsd)
            RETURNING ma_ct_phieu_nhap INTO v_detail;

            INSERT INTO LOHANG_NGUYENLIEU (ma_kho, ma_nguyen_lieu, ma_ct_phieu_nhap, so_luong_con_lai, trang_thai, ngay_tao, han_su_dung)
            VALUES (v_central_kho, v_ing, v_detail, p_qty, 'ACTIVE', p_time, p_hsd)
            RETURNING ma_lo_hang INTO v_lot;

            UPDATE PHIEUNHAP
            SET tong_tien = tong_tien + p_qty * p_price
            WHERE ma_phieu_nhap = v_receipt;

            journal_once(v_central_kho, v_ing, v_lot, 'IMPORT', 'PHIEUNHAP', v_receipt, p_qty, v_before, v_before + p_qty, p_time, pbi_get_user_id('qlkho01'));
        END IF;
    END;

    FUNCTION ensure_export_header(p_code VARCHAR2, p_time TIMESTAMP, p_type VARCHAR2) RETURN NUMBER IS
        v_id NUMBER;
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM PHIEUXUAT WHERE ghi_chu = p_code;
        IF v_count = 0 THEN
            INSERT INTO PHIEUXUAT (ma_kho, ngay_xuat, loai_xuat, tong_gia_tri_xuat, nguoi_tao, trang_thai, ghi_chu)
            VALUES (v_central_kho, p_time, p_type, 0, pbi_get_user_id('qlkho01'), 'COMPLETED', p_code)
            RETURNING ma_phieu_xuat INTO v_id;
        ELSE
            SELECT ma_phieu_xuat INTO v_id FROM PHIEUXUAT WHERE ghi_chu = p_code;
        END IF;
        RETURN v_id;
    END;

    PROCEDURE add_export_line(p_code VARCHAR2, p_time TIMESTAMP, p_type VARCHAR2, p_ing VARCHAR2, p_qty NUMBER, p_price NUMBER) IS
        v_export NUMBER;
        v_ing NUMBER;
        v_lot NUMBER;
        v_exists NUMBER;
        v_before NUMBER;
    BEGIN
        v_export := ensure_export_header(p_code, p_time, p_type);
        v_ing := pbi_get_ingredient_id(p_ing);
        SELECT COUNT(*) INTO v_exists FROM CHITIETPHIEUXUAT WHERE ma_phieu_xuat = v_export AND ma_nguyen_lieu = v_ing;
        IF v_exists = 0 THEN
            v_lot := find_available_lot(v_central_kho, v_ing, p_qty);
            v_before := stock_from_lots(v_central_kho, v_ing);

            INSERT INTO CHITIETPHIEUXUAT (ma_phieu_xuat, ma_nguyen_lieu, ma_lo_hang, so_luong_xuat, don_gia_xuat)
            VALUES (v_export, v_ing, v_lot, p_qty, p_price);

            UPDATE LOHANG_NGUYENLIEU
            SET so_luong_con_lai = so_luong_con_lai - p_qty
            WHERE ma_lo_hang = v_lot;

            UPDATE PHIEUXUAT
            SET tong_gia_tri_xuat = tong_gia_tri_xuat + p_qty * p_price
            WHERE ma_phieu_xuat = v_export;

            journal_once(v_central_kho, v_ing, v_lot, 'EXPORT', 'PHIEUXUAT', v_export, -p_qty, v_before, v_before - p_qty, p_time, pbi_get_user_id('qlkho01'));
        END IF;
    END;

    FUNCTION ensure_transfer_header(p_code VARCHAR2, p_time TIMESTAMP, p_branch VARCHAR2) RETURN NUMBER IS
        v_id NUMBER;
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM PHIEUDIEUCHUYEN WHERE ghi_chu = p_code;
        IF v_count = 0 THEN
            INSERT INTO PHIEUDIEUCHUYEN (ma_kho_nguon, ma_kho_dich, ngay_tao, trang_thai, nguoi_tao, ghi_chu)
            VALUES (v_central_kho, pbi_get_branch_kho(p_branch), p_time, 'COMPLETED', pbi_get_user_id('qlkho01'), p_code)
            RETURNING ma_phieu_dieu_chuyen INTO v_id;
        ELSE
            SELECT ma_phieu_dieu_chuyen INTO v_id FROM PHIEUDIEUCHUYEN WHERE ghi_chu = p_code;
        END IF;
        RETURN v_id;
    END;

    PROCEDURE add_transfer_line(p_code VARCHAR2, p_time TIMESTAMP, p_branch VARCHAR2, p_ing VARCHAR2, p_qty NUMBER) IS
        v_transfer NUMBER;
        v_ing NUMBER;
        v_source_lot NUMBER;
        v_dest_lot NUMBER;
        v_detail NUMBER;
        v_exists NUMBER;
        v_src_before NUMBER;
        v_dest_before NUMBER;
        v_hsd DATE;
        v_dest_kho NUMBER;
    BEGIN
        v_transfer := ensure_transfer_header(p_code, p_time, p_branch);
        v_ing := pbi_get_ingredient_id(p_ing);
        v_dest_kho := pbi_get_branch_kho(p_branch);
        SELECT COUNT(*) INTO v_exists FROM CHITIETPHIEUDIEUCHUYEN WHERE ma_phieu_dieu_chuyen = v_transfer AND ma_nguyen_lieu = v_ing;
        IF v_exists = 0 THEN
            v_source_lot := find_available_lot(v_central_kho, v_ing, p_qty);
            SELECT han_su_dung INTO v_hsd FROM LOHANG_NGUYENLIEU WHERE ma_lo_hang = v_source_lot;
            v_src_before := stock_from_lots(v_central_kho, v_ing);
            v_dest_before := stock_from_lots(v_dest_kho, v_ing);

            UPDATE LOHANG_NGUYENLIEU
            SET so_luong_con_lai = so_luong_con_lai - p_qty
            WHERE ma_lo_hang = v_source_lot;

            INSERT INTO LOHANG_NGUYENLIEU (ma_kho, ma_nguyen_lieu, ma_ct_phieu_nhap, so_luong_con_lai, trang_thai, ngay_tao, han_su_dung)
            VALUES (v_dest_kho, v_ing, NULL, p_qty, 'ACTIVE', p_time, v_hsd)
            RETURNING ma_lo_hang INTO v_dest_lot;

            INSERT INTO CHITIETPHIEUDIEUCHUYEN (ma_phieu_dieu_chuyen, ma_nguyen_lieu, ma_lo_hang_nguon, ma_lo_hang_dich, so_luong_dieu_chuyen)
            VALUES (v_transfer, v_ing, v_source_lot, v_dest_lot, p_qty)
            RETURNING ma_ct_phieu_dieu_chuyen INTO v_detail;

            journal_once(v_central_kho, v_ing, v_source_lot, 'TRANSFER_OUT', 'PHIEUDIEUCHUYEN', v_transfer, -p_qty, v_src_before, v_src_before - p_qty, p_time, pbi_get_user_id('qlkho01'));
            journal_once(v_dest_kho, v_ing, v_dest_lot, 'TRANSFER_IN', 'PHIEUDIEUCHUYEN', v_transfer, p_qty, v_dest_before, v_dest_before + p_qty, p_time, pbi_get_user_id('qlkho01'));
        END IF;
    END;

    FUNCTION ensure_invoice_header(p_branch VARCHAR2, p_cashier VARCHAR2, p_time TIMESTAMP, p_method VARCHAR2) RETURN NUMBER IS
        v_id NUMBER;
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM HOADON h
        WHERE h.ma_chi_nhanh = pbi_get_branch_id(p_branch)
          AND h.thoi_gian_tao_hoa_don = p_time;

        IF v_count = 0 THEN
            INSERT INTO HOADON (ma_chi_nhanh, ma_pos, ma_nguoi_dung, thoi_gian_tao_hoa_don, trang_thai_hoa_don, tong_thanh_toan, phuong_thuc_thanh_toan, thoi_gian_thanh_toan, trang_thai_thanh_toan)
            VALUES (pbi_get_branch_id(p_branch), pbi_get_pos_id(p_branch), pbi_get_user_id(p_cashier), p_time, 'COMPLETED', 0, p_method, p_time + INTERVAL '1' MINUTE, 'SUCCESS')
            RETURNING ma_hoa_don INTO v_id;
        ELSE
            SELECT h.ma_hoa_don INTO v_id
            FROM HOADON h
            WHERE h.ma_chi_nhanh = pbi_get_branch_id(p_branch)
              AND h.thoi_gian_tao_hoa_don = p_time;
        END IF;
        RETURN v_id;
    END;

    PROCEDURE add_invoice_line(p_branch VARCHAR2, p_cashier VARCHAR2, p_time TIMESTAMP, p_method VARCHAR2, p_product VARCHAR2, p_qty NUMBER) IS
        v_invoice NUMBER;
        v_product NUMBER;
        v_price NUMBER(18,2);
        v_detail NUMBER;
        v_exists NUMBER;
        v_kho NUMBER;
        v_lot NUMBER;
        v_need NUMBER;
        v_before NUMBER;
    BEGIN
        v_invoice := ensure_invoice_header(p_branch, p_cashier, p_time, p_method);
        v_product := pbi_get_product_id(p_product);
        SELECT gia_ban_hien_tai INTO v_price FROM SANPHAM WHERE ma_san_pham = v_product;

        SELECT COUNT(*) INTO v_exists FROM CHITIETHOADON WHERE ma_hoa_don = v_invoice AND ma_san_pham = v_product;
        IF v_exists = 0 THEN
            INSERT INTO CHITIETHOADON (ma_hoa_don, ma_san_pham, so_luong, don_gia_ban, thanh_tien_dong)
            VALUES (v_invoice, v_product, p_qty, v_price, p_qty * v_price)
            RETURNING ma_ct_hoa_don INTO v_detail;

            UPDATE HOADON
            SET tong_thanh_toan = tong_thanh_toan + p_qty * v_price
            WHERE ma_hoa_don = v_invoice;

            v_kho := pbi_get_branch_kho(p_branch);

            FOR r IN (
                SELECT ma_nguyen_lieu, so_luong_can
                FROM CONGTHUC_SANPHAM
                WHERE ma_san_pham = v_product
            ) LOOP
                v_need := r.so_luong_can * p_qty;
                v_lot := find_available_lot(v_kho, r.ma_nguyen_lieu, v_need);
                v_before := stock_from_lots(v_kho, r.ma_nguyen_lieu);

                INSERT INTO BANHANG_TRUKHO (ma_hoa_don, ma_ct_hoa_don, ma_kho, ma_nguyen_lieu, ma_lo_hang, so_luong_nguyen_lieu_moi_sp, tong_so_luong_tru, trang_thai)
                VALUES (v_invoice, v_detail, v_kho, r.ma_nguyen_lieu, v_lot, r.so_luong_can, v_need, 'DEDUCTED');

                UPDATE LOHANG_NGUYENLIEU
                SET so_luong_con_lai = so_luong_con_lai - v_need
                WHERE ma_lo_hang = v_lot;

                journal_once(v_kho, r.ma_nguyen_lieu, v_lot, 'SALE_DEDUCT', 'HOADON', v_invoice, -v_need, v_before, v_before - v_need, p_time, pbi_get_user_id(p_cashier));
            END LOOP;
        END IF;
    END;

    PROCEDURE ensure_wastage(p_code VARCHAR2, p_time TIMESTAMP, p_branch VARCHAR2, p_user VARCHAR2, p_ing VARCHAR2, p_qty NUMBER, p_type VARCHAR2) IS
        v_id NUMBER;
        v_count NUMBER;
        v_kho NUMBER;
        v_ing NUMBER;
        v_lot NUMBER;
        v_before NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM PHIEUHAOHUT WHERE ghi_chu = p_code;
        IF v_count = 0 THEN
            v_kho := pbi_get_branch_kho(p_branch);
            v_ing := pbi_get_ingredient_id(p_ing);
            v_lot := find_available_lot(v_kho, v_ing, p_qty);
            v_before := stock_from_lots(v_kho, v_ing);

            INSERT INTO PHIEUHAOHUT (ma_kho, ma_nguyen_lieu, ma_lo_hang, so_luong_hao_hut, loai_hao_hut, ngay_hao_hut, ghi_chu, nguoi_bao_cao)
            VALUES (v_kho, v_ing, v_lot, p_qty, p_type, p_time, p_code, pbi_get_user_id(p_user))
            RETURNING ma_phieu_hao_hut INTO v_id;

            UPDATE LOHANG_NGUYENLIEU
            SET so_luong_con_lai = so_luong_con_lai - p_qty
            WHERE ma_lo_hang = v_lot;

            journal_once(v_kho, v_ing, v_lot, 'WASTAGE', 'PHIEUHAOHUT', v_id, -p_qty, v_before, v_before - p_qty, p_time, pbi_get_user_id(p_user));
        END IF;
    END;

    PROCEDURE ensure_stocktake(p_code VARCHAR2, p_time TIMESTAMP, p_branch VARCHAR2, p_user VARCHAR2, p_ing VARCHAR2, p_actual_offset NUMBER, p_reason VARCHAR2) IS
        v_id NUMBER;
        v_count NUMBER;
        v_kho NUMBER;
        v_ing NUMBER;
        v_system NUMBER;
        v_actual NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM PHIEUKIEMKHO WHERE ghi_chu = p_code;
        IF v_count = 0 THEN
            v_kho := pbi_get_branch_kho(p_branch);
            INSERT INTO PHIEUKIEMKHO (ma_kho, ngay_kiem_kho, nguoi_kiem, trang_thai, ghi_chu)
            VALUES (v_kho, p_time, pbi_get_user_id(p_user), 'COMPLETED', p_code)
            RETURNING ma_phieu_kiem_kho INTO v_id;
        ELSE
            SELECT ma_phieu_kiem_kho INTO v_id FROM PHIEUKIEMKHO WHERE ghi_chu = p_code;
            v_kho := pbi_get_branch_kho(p_branch);
        END IF;

        v_ing := pbi_get_ingredient_id(p_ing);
        SELECT COUNT(*) INTO v_count FROM CHITIETPHIEUKIEMKHO WHERE ma_phieu_kiem_kho = v_id AND ma_nguyen_lieu = v_ing;
        IF v_count = 0 THEN
            v_system := stock_from_lots(v_kho, v_ing);
            v_actual := GREATEST(v_system + p_actual_offset, 0);
            INSERT INTO CHITIETPHIEUKIEMKHO (ma_phieu_kiem_kho, ma_nguyen_lieu, so_luong_he_thong, so_luong_thuc_te, so_luong_chenh_lech, ty_le_chenh_lech, ly_do_chenh_lech, huong_xu_ly)
            VALUES (
                v_id,
                v_ing,
                v_system,
                v_actual,
                v_actual - v_system,
                CASE WHEN v_system = 0 THEN 0 ELSE ROUND((v_actual - v_system) / v_system, 4) END,
                p_reason,
                CASE WHEN p_actual_offset = 0 THEN 'NO_ACTION' ELSE 'CREATE_WASTAGE' END
            );
        END IF;
    END;

    PROCEDURE sync_stock_from_lots IS
    BEGIN
        MERGE INTO TONKHO tk
        USING (
            WITH pairs AS (
                SELECT ma_kho, ma_nguyen_lieu FROM TONKHO
                UNION
                SELECT ma_kho, ma_nguyen_lieu FROM LOHANG_NGUYENLIEU
            )
            SELECT p.ma_kho,
                   p.ma_nguyen_lieu,
                   NVL((
                       SELECT SUM(lh.so_luong_con_lai)
                       FROM LOHANG_NGUYENLIEU lh
                       WHERE lh.ma_kho = p.ma_kho
                         AND lh.ma_nguyen_lieu = p.ma_nguyen_lieu
                         AND lh.trang_thai = 'ACTIVE'
                         AND lh.so_luong_con_lai > 0
                         AND (lh.han_su_dung IS NULL OR lh.han_su_dung >= TRUNC(SYSDATE))
                   ), 0) so_luong_ton
            FROM pairs p
        ) src
        ON (tk.ma_kho = src.ma_kho AND tk.ma_nguyen_lieu = src.ma_nguyen_lieu)
        WHEN MATCHED THEN UPDATE SET tk.so_luong_ton = src.so_luong_ton, tk.lan_cap_nhat_cuoi = CURRENT_TIMESTAMP
        WHEN NOT MATCHED THEN INSERT (ma_kho, ma_nguyen_lieu, so_luong_ton, lan_cap_nhat_cuoi)
            VALUES (src.ma_kho, src.ma_nguyen_lieu, src.so_luong_ton, CURRENT_TIMESTAMP);
    END;

BEGIN
    SELECT ma_kho INTO v_central_kho FROM KHO WHERE ten_kho = 'Kho tổng Phụng Lộc';

    -- Master data bo sung da duoc nap o muc 9-10 ben tren.

    -- Nhập kho tong: nhieu dot nhap de bao cao Power BI co chuoi thoi gian
    add_import_line('PBI_NHAP_20260512_CAPHE', TIMESTAMP '2026-05-12 08:10:00', 'Cà Phê Cao Nguyên', 'Cà phê hạt Arabica', 180000, 118, 'PBI-AR-20260512', DATE '2027-12-31');
    add_import_line('PBI_NHAP_20260512_CAPHE', TIMESTAMP '2026-05-12 08:10:00', 'Cà Phê Cao Nguyên', 'Cà phê hạt Robusta', 260000, 94, 'PBI-RO-20260512', DATE '2027-12-31');
    add_import_line('PBI_NHAP_20260512_SUA', TIMESTAMP '2026-05-12 09:00:00', 'Sữa Thực Phẩm Sài Gòn', 'Sữa tươi', 650000, 32, 'PBI-SUA-20260512', DATE '2026-06-15');
    add_import_line('PBI_NHAP_20260512_SUA', TIMESTAMP '2026-05-12 09:00:00', 'Sữa Thực Phẩm Sài Gòn', 'Kem béo thực vật', 180000, 45, 'PBI-KB-20260512', DATE '2026-09-30');
    add_import_line('PBI_NHAP_20260513_TRASUA', TIMESTAMP '2026-05-13 08:30:00', 'Nguyên Liệu Trà Sữa', 'Trà đen', 120000, 82, 'PBI-TRA-20260513', DATE '2027-05-31');
    add_import_line('PBI_NHAP_20260513_TRASUA', TIMESTAMP '2026-05-13 08:30:00', 'Nguyên Liệu Trà Sữa', 'Trân châu đen', 140000, 38, 'PBI-TC-20260513', DATE '2026-08-31');
    add_import_line('PBI_NHAP_20260513_TRASUA', TIMESTAMP '2026-05-13 08:30:00', 'Nguyên Liệu Trà Sữa', 'Bột matcha', 60000, 155, 'PBI-MAT-20260513', DATE '2027-03-31');
    add_import_line('PBI_NHAP_20260513_TRASUA', TIMESTAMP '2026-05-13 08:30:00', 'Nguyên Liệu Trà Sữa', 'Bột cacao', 70000, 125, 'PBI-CACAO-20260513', DATE '2027-03-31');
    add_import_line('PBI_NHAP_20260514_HUONGLIEU', TIMESTAMP '2026-05-14 10:00:00', 'Hương Liệu Việt', 'Siro caramel', 90000, 72, 'PBI-CARAMEL-20260514', DATE '2027-01-31');
    add_import_line('PBI_NHAP_20260514_HUONGLIEU', TIMESTAMP '2026-05-14 10:00:00', 'Hương Liệu Việt', 'Siro vanilla', 70000, 70, 'PBI-VANILLA-20260514', DATE '2027-01-31');
    add_import_line('PBI_NHAP_20260514_HUONGLIEU', TIMESTAMP '2026-05-14 10:00:00', 'Hương Liệu Việt', 'Đào ngâm', 80000, 68, 'PBI-DAO-20260514', DATE '2026-10-31');
    add_import_line('PBI_NHAP_20260515_BAOBI', TIMESTAMP '2026-05-15 14:00:00', 'Bao Bì Xanh', 'Ly giấy 16oz', 16000, 850, 'PBI-LY-20260515', DATE '2028-12-31');
    add_import_line('PBI_NHAP_20260515_BAOBI', TIMESTAMP '2026-05-15 14:00:00', 'Bao Bì Xanh', 'Nắp nhựa 16oz', 16000, 450, 'PBI-NAP-20260515', DATE '2028-12-31');
    add_import_line('PBI_NHAP_20260515_BAOBI', TIMESTAMP '2026-05-15 14:00:00', 'Bao Bì Xanh', 'Ống hút giấy', 16000, 300, 'PBI-ONGHUT-20260515', DATE '2028-12-31');
    add_import_line('PBI_NHAP_20260522_CAPHE', TIMESTAMP '2026-05-22 08:10:00', 'Cà Phê Cao Nguyên', 'Cà phê hạt Arabica', 120000, 119, 'PBI-AR-20260522', DATE '2028-01-15');
    add_import_line('PBI_NHAP_20260522_CAPHE', TIMESTAMP '2026-05-22 08:10:00', 'Cà Phê Cao Nguyên', 'Cà phê hạt Robusta', 200000, 95, 'PBI-RO-20260522', DATE '2028-01-15');
    add_import_line('PBI_NHAP_20260525_SUA', TIMESTAMP '2026-05-25 08:20:00', 'Sữa Thực Phẩm Sài Gòn', 'Sữa tươi', 500000, 33, 'PBI-SUA-20260525', DATE '2026-06-28');
    add_import_line('PBI_NHAP_20260525_SUA', TIMESTAMP '2026-05-25 08:20:00', 'Sữa Thực Phẩm Sài Gòn', 'Kem béo thực vật', 120000, 46, 'PBI-KB-20260525', DATE '2026-10-15');
    add_import_line('PBI_NHAP_20260602_TRASUA', TIMESTAMP '2026-06-02 08:40:00', 'Nguyên Liệu Trà Sữa', 'Trà đen', 100000, 83, 'PBI-TRA-20260602', DATE '2027-06-30');
    add_import_line('PBI_NHAP_20260602_TRASUA', TIMESTAMP '2026-06-02 08:40:00', 'Nguyên Liệu Trà Sữa', 'Trân châu đen', 120000, 39, 'PBI-TC-20260602', DATE '2026-09-30');
    add_import_line('PBI_NHAP_20260602_TRASUA', TIMESTAMP '2026-06-02 08:40:00', 'Nguyên Liệu Trà Sữa', 'Bột matcha', 50000, 156, 'PBI-MAT-20260602', DATE '2027-04-30');


    -- Nhap bo sung quy mo lon de du du lieu dieu chuyen va ban hang Power BI
    add_import_line('PBI_NHAP_20260512_OPEN_CAPHE', TIMESTAMP '2026-05-12 07:30:00', 'Cà Phê Cao Nguyên', 'Cà phê hạt Arabica', 500000, 118, 'PBI-AR-OPEN-20260512', DATE '2028-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_CAPHE', TIMESTAMP '2026-05-12 07:30:00', 'Cà Phê Cao Nguyên', 'Cà phê hạt Robusta', 900000, 94, 'PBI-RO-OPEN-20260512', DATE '2028-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_SUA', TIMESTAMP '2026-05-12 07:45:00', 'Sữa Thực Phẩm Sài Gòn', 'Sữa tươi', 1800000, 32, 'PBI-SUA-OPEN-20260512', DATE '2026-07-31');
    add_import_line('PBI_NHAP_20260512_OPEN_SUA', TIMESTAMP '2026-05-12 07:45:00', 'Sữa Thực Phẩm Sài Gòn', 'Kem béo thực vật', 350000, 45, 'PBI-KB-OPEN-20260512', DATE '2026-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_TRASUA', TIMESTAMP '2026-05-12 08:00:00', 'Nguyên Liệu Trà Sữa', 'Đường cát', 600000, 25, 'PBI-DUONG-OPEN-20260512', DATE '2027-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_TRASUA', TIMESTAMP '2026-05-12 08:00:00', 'Nguyên Liệu Trà Sữa', 'Trà đen', 350000, 82, 'PBI-TRA-OPEN-20260512', DATE '2028-05-31');
    add_import_line('PBI_NHAP_20260512_OPEN_TRASUA', TIMESTAMP '2026-05-12 08:00:00', 'Nguyên Liệu Trà Sữa', 'Trân châu đen', 350000, 38, 'PBI-TC-OPEN-20260512', DATE '2026-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_TRASUA', TIMESTAMP '2026-05-12 08:00:00', 'Nguyên Liệu Trà Sữa', 'Bột cacao', 180000, 125, 'PBI-CACAO-OPEN-20260512', DATE '2027-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_TRASUA', TIMESTAMP '2026-05-12 08:00:00', 'Nguyên Liệu Trà Sữa', 'Bột matcha', 160000, 155, 'PBI-MATCHA-OPEN-20260512', DATE '2027-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_HUONGLIEU', TIMESTAMP '2026-05-12 08:15:00', 'Hương Liệu Việt', 'Siro caramel', 160000, 72, 'PBI-CARAMEL-OPEN-20260512', DATE '2027-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_HUONGLIEU', TIMESTAMP '2026-05-12 08:15:00', 'Hương Liệu Việt', 'Siro vanilla', 130000, 70, 'PBI-VANILLA-OPEN-20260512', DATE '2027-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_HUONGLIEU', TIMESTAMP '2026-05-12 08:15:00', 'Hương Liệu Việt', 'Đào ngâm', 170000, 68, 'PBI-DAO-OPEN-20260512', DATE '2026-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_BAOBI', TIMESTAMP '2026-05-12 08:30:00', 'Bao Bì Xanh', 'Ly giấy 16oz', 60000, 850, 'PBI-LY-OPEN-20260512', DATE '2028-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_BAOBI', TIMESTAMP '2026-05-12 08:30:00', 'Bao Bì Xanh', 'Nắp nhựa 16oz', 60000, 450, 'PBI-NAP-OPEN-20260512', DATE '2028-12-31');
    add_import_line('PBI_NHAP_20260512_OPEN_BAOBI', TIMESTAMP '2026-05-12 08:30:00', 'Bao Bì Xanh', 'Ống hút giấy', 60000, 300, 'PBI-ONGHUT-OPEN-20260512', DATE '2028-12-31');

    -- Xuất kho tong cho dao tao, dung noi bo va tra nha cung cap
    add_export_line('PBI_XUAT_TRAINING_20260516', TIMESTAMP '2026-05-16 15:00:00', 'TRAINING', 'Cà phê hạt Arabica', 3500, 118);
    add_export_line('PBI_XUAT_TRAINING_20260516', TIMESTAMP '2026-05-16 15:00:00', 'TRAINING', 'Sữa tươi', 12000, 32);
    add_export_line('PBI_XUAT_TRAINING_20260516', TIMESTAMP '2026-05-16 15:00:00', 'TRAINING', 'Ly giấy 16oz', 80, 850);
    add_export_line('PBI_XUAT_INTERNAL_20260518', TIMESTAMP '2026-05-18 17:00:00', 'INTERNAL_USE', 'Cà phê hạt Robusta', 2500, 94);
    add_export_line('PBI_XUAT_INTERNAL_20260518', TIMESTAMP '2026-05-18 17:00:00', 'INTERNAL_USE', 'Đường cát', 3000, 25);
    add_export_line('PBI_XUAT_RETURN_20260527', TIMESTAMP '2026-05-27 10:00:00', 'RETURN_SUPPLIER', 'Siro vanilla', 2000, 70);
    add_export_line('PBI_XUAT_TRAINING_20260605', TIMESTAMP '2026-06-05 14:30:00', 'TRAINING', 'Bột matcha', 1500, 156);
    add_export_line('PBI_XUAT_TRAINING_20260605', TIMESTAMP '2026-06-05 14:30:00', 'TRAINING', 'Sữa tươi', 8000, 33);
    add_export_line('PBI_XUAT_TRAINING_20260605', TIMESTAMP '2026-06-05 14:30:00', 'TRAINING', 'Ly giấy 16oz', 60, 850);

    -- Dieu chuyen tu kho tong ve cac kho chi nhanh
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Cà phê hạt Arabica', 10125);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Cà phê hạt Robusta', 15750);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Sữa tươi', 50625);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Đường cát', 11250);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Trà đen', 7312);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Trân châu đen', 9562);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Bột cacao', 3375);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Bột matcha', 2812);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Siro caramel', 3375);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Kem béo thực vật', 6750);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Đào ngâm', 3375);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Ly giấy 16oz', 675);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Nắp nhựa 16oz', 675);
    add_transfer_line('PBI_DC_01_01', TIMESTAMP '2026-05-16 07:30:00', 'Bến Thành', 'Ống hút giấy', 675);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Cà phê hạt Arabica', 10935);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Cà phê hạt Robusta', 17010);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Sữa tươi', 54675);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Đường cát', 12150);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Trà đen', 7897);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Trân châu đen', 10327);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Bột cacao', 3645);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Bột matcha', 3037);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Siro caramel', 3645);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Kem béo thực vật', 7290);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Đào ngâm', 3645);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Ly giấy 16oz', 729);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Nắp nhựa 16oz', 729);
    add_transfer_line('PBI_DC_01_02', TIMESTAMP '2026-05-16 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Ống hút giấy', 729);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Cà phê hạt Arabica', 12150);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Cà phê hạt Robusta', 18900);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Sữa tươi', 60750);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Đường cát', 13500);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Trà đen', 8775);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Trân châu đen', 11475);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Bột cacao', 4050);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Bột matcha', 3375);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Siro caramel', 4050);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Kem béo thực vật', 8100);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Đào ngâm', 4050);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Ly giấy 16oz', 810);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Nắp nhựa 16oz', 810);
    add_transfer_line('PBI_DC_01_03', TIMESTAMP '2026-05-16 07:30:00', 'Landmark 81', 'Ống hút giấy', 810);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Cà phê hạt Arabica', 9720);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Cà phê hạt Robusta', 15120);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Sữa tươi', 48600);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Đường cát', 10800);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Trà đen', 7020);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Trân châu đen', 9180);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Bột cacao', 3240);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Bột matcha', 2700);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Siro caramel', 3240);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Kem béo thực vật', 6480);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Đào ngâm', 3240);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Ly giấy 16oz', 648);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Nắp nhựa 16oz', 648);
    add_transfer_line('PBI_DC_01_04', TIMESTAMP '2026-05-16 07:30:00', 'Giga Mall', 'Ống hút giấy', 648);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Cà phê hạt Arabica', 8910);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Cà phê hạt Robusta', 13860);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Sữa tươi', 44550);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Đường cát', 9900);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Trà đen', 6435);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Trân châu đen', 8415);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Bột cacao', 2970);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Bột matcha', 2475);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Siro caramel', 2970);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Kem béo thực vật', 5940);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Đào ngâm', 2970);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Ly giấy 16oz', 594);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Nắp nhựa 16oz', 594);
    add_transfer_line('PBI_DC_01_05', TIMESTAMP '2026-05-16 07:30:00', 'Thảo Điền', 'Ống hút giấy', 594);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Cà phê hạt Arabica', 8100);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Cà phê hạt Robusta', 12600);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Sữa tươi', 40500);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Đường cát', 9000);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Trà đen', 5850);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Trân châu đen', 7650);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Bột cacao', 2700);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Bột matcha', 2250);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Siro caramel', 2700);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Kem béo thực vật', 5400);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Đào ngâm', 2700);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Ly giấy 16oz', 540);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Nắp nhựa 16oz', 540);
    add_transfer_line('PBI_DC_01_06', TIMESTAMP '2026-05-16 07:30:00', 'Quang Trung', 'Ống hút giấy', 540);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Cà phê hạt Arabica', 9315);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Cà phê hạt Robusta', 14489);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Sữa tươi', 46574);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Đường cát', 10350);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Trà đen', 6727);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Trân châu đen', 8797);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Bột cacao', 3104);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Bột matcha', 2587);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Siro caramel', 3104);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Kem béo thực vật', 6209);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Đào ngâm', 3104);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Ly giấy 16oz', 621);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Nắp nhựa 16oz', 621);
    add_transfer_line('PBI_DC_01_07', TIMESTAMP '2026-05-16 07:30:00', 'Aeon Mall Bình Tân', 'Ống hút giấy', 621);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Cà phê hạt Arabica', 8505);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Cà phê hạt Robusta', 13230);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Sữa tươi', 42525);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Đường cát', 9450);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Trà đen', 6142);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Trân châu đen', 8032);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Bột cacao', 2835);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Bột matcha', 2362);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Siro caramel', 2835);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Kem béo thực vật', 5670);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Đào ngâm', 2835);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Ly giấy 16oz', 567);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Nắp nhựa 16oz', 567);
    add_transfer_line('PBI_DC_01_08', TIMESTAMP '2026-05-16 07:30:00', 'Nguyễn Trãi', 'Ống hút giấy', 567);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Cà phê hạt Arabica', 10687);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Cà phê hạt Robusta', 16625);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Sữa tươi', 53437);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Đường cát', 11875);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Trà đen', 7718);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Trân châu đen', 10093);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Bột cacao', 3562);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Bột matcha', 2968);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Siro caramel', 3562);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Kem béo thực vật', 7125);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Đào ngâm', 3562);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Ly giấy 16oz', 712);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Nắp nhựa 16oz', 712);
    add_transfer_line('PBI_DC_02_01', TIMESTAMP '2026-05-23 07:30:00', 'Bến Thành', 'Ống hút giấy', 712);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Cà phê hạt Arabica', 11542);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Cà phê hạt Robusta', 17955);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Sữa tươi', 57712);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Đường cát', 12825);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Trà đen', 8336);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Trân châu đen', 10901);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Bột cacao', 3847);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Bột matcha', 3206);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Siro caramel', 3847);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Kem béo thực vật', 7695);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Đào ngâm', 3847);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Ly giấy 16oz', 769);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Nắp nhựa 16oz', 769);
    add_transfer_line('PBI_DC_02_02', TIMESTAMP '2026-05-23 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Ống hút giấy', 769);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Cà phê hạt Arabica', 12825);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Cà phê hạt Robusta', 19950);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Sữa tươi', 64125);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Đường cát', 14250);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Trà đen', 9262);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Trân châu đen', 12112);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Bột cacao', 4275);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Bột matcha', 3562);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Siro caramel', 4275);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Kem béo thực vật', 8550);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Đào ngâm', 4275);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Ly giấy 16oz', 855);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Nắp nhựa 16oz', 855);
    add_transfer_line('PBI_DC_02_03', TIMESTAMP '2026-05-23 07:30:00', 'Landmark 81', 'Ống hút giấy', 855);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Cà phê hạt Arabica', 10260);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Cà phê hạt Robusta', 15960);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Sữa tươi', 51300);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Đường cát', 11400);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Trà đen', 7410);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Trân châu đen', 9690);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Bột cacao', 3420);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Bột matcha', 2850);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Siro caramel', 3420);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Kem béo thực vật', 6840);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Đào ngâm', 3420);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Ly giấy 16oz', 684);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Nắp nhựa 16oz', 684);
    add_transfer_line('PBI_DC_02_04', TIMESTAMP '2026-05-23 07:30:00', 'Giga Mall', 'Ống hút giấy', 684);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Cà phê hạt Arabica', 9405);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Cà phê hạt Robusta', 14630);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Sữa tươi', 47025);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Đường cát', 10450);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Trà đen', 6792);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Trân châu đen', 8882);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Bột cacao', 3135);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Bột matcha', 2612);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Siro caramel', 3135);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Kem béo thực vật', 6270);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Đào ngâm', 3135);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Ly giấy 16oz', 627);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Nắp nhựa 16oz', 627);
    add_transfer_line('PBI_DC_02_05', TIMESTAMP '2026-05-23 07:30:00', 'Thảo Điền', 'Ống hút giấy', 627);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Cà phê hạt Arabica', 8550);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Cà phê hạt Robusta', 13300);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Sữa tươi', 42750);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Đường cát', 9500);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Trà đen', 6175);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Trân châu đen', 8075);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Bột cacao', 2850);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Bột matcha', 2375);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Siro caramel', 2850);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Kem béo thực vật', 5700);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Đào ngâm', 2850);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Ly giấy 16oz', 570);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Nắp nhựa 16oz', 570);
    add_transfer_line('PBI_DC_02_06', TIMESTAMP '2026-05-23 07:30:00', 'Quang Trung', 'Ống hút giấy', 570);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Cà phê hạt Arabica', 9832);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Cà phê hạt Robusta', 15294);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Sữa tươi', 49162);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Đường cát', 10925);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Trà đen', 7101);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Trân châu đen', 9286);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Bột cacao', 3277);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Bột matcha', 2731);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Siro caramel', 3277);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Kem béo thực vật', 6554);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Đào ngâm', 3277);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Ly giấy 16oz', 655);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Nắp nhựa 16oz', 655);
    add_transfer_line('PBI_DC_02_07', TIMESTAMP '2026-05-23 07:30:00', 'Aeon Mall Bình Tân', 'Ống hút giấy', 655);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Cà phê hạt Arabica', 8977);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Cà phê hạt Robusta', 13965);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Sữa tươi', 44887);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Đường cát', 9975);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Trà đen', 6483);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Trân châu đen', 8478);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Bột cacao', 2992);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Bột matcha', 2493);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Siro caramel', 2992);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Kem béo thực vật', 5985);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Đào ngâm', 2992);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Ly giấy 16oz', 598);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Nắp nhựa 16oz', 598);
    add_transfer_line('PBI_DC_02_08', TIMESTAMP '2026-05-23 07:30:00', 'Nguyễn Trãi', 'Ống hút giấy', 598);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Cà phê hạt Arabica', 11250);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Cà phê hạt Robusta', 17500);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Sữa tươi', 56250);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Đường cát', 12500);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Trà đen', 8125);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Trân châu đen', 10625);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Bột cacao', 3750);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Bột matcha', 3125);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Siro caramel', 3750);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Kem béo thực vật', 7500);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Đào ngâm', 3750);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Ly giấy 16oz', 750);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Nắp nhựa 16oz', 750);
    add_transfer_line('PBI_DC_03_01', TIMESTAMP '2026-05-30 07:30:00', 'Bến Thành', 'Ống hút giấy', 750);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Cà phê hạt Arabica', 12150);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Cà phê hạt Robusta', 18900);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Sữa tươi', 60750);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Đường cát', 13500);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Trà đen', 8775);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Trân châu đen', 11475);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Bột cacao', 4050);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Bột matcha', 3375);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Siro caramel', 4050);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Kem béo thực vật', 8100);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Đào ngâm', 4050);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Ly giấy 16oz', 810);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Nắp nhựa 16oz', 810);
    add_transfer_line('PBI_DC_03_02', TIMESTAMP '2026-05-30 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Ống hút giấy', 810);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Cà phê hạt Arabica', 13500);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Cà phê hạt Robusta', 21000);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Sữa tươi', 67500);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Đường cát', 15000);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Trà đen', 9750);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Trân châu đen', 12750);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Bột cacao', 4500);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Bột matcha', 3750);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Siro caramel', 4500);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Kem béo thực vật', 9000);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Đào ngâm', 4500);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Ly giấy 16oz', 900);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Nắp nhựa 16oz', 900);
    add_transfer_line('PBI_DC_03_03', TIMESTAMP '2026-05-30 07:30:00', 'Landmark 81', 'Ống hút giấy', 900);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Cà phê hạt Arabica', 10800);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Cà phê hạt Robusta', 16800);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Sữa tươi', 54000);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Đường cát', 12000);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Trà đen', 7800);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Trân châu đen', 10200);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Bột cacao', 3600);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Bột matcha', 3000);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Siro caramel', 3600);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Kem béo thực vật', 7200);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Đào ngâm', 3600);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Ly giấy 16oz', 720);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Nắp nhựa 16oz', 720);
    add_transfer_line('PBI_DC_03_04', TIMESTAMP '2026-05-30 07:30:00', 'Giga Mall', 'Ống hút giấy', 720);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Cà phê hạt Arabica', 9900);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Cà phê hạt Robusta', 15400);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Sữa tươi', 49500);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Đường cát', 11000);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Trà đen', 7150);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Trân châu đen', 9350);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Bột cacao', 3300);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Bột matcha', 2750);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Siro caramel', 3300);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Kem béo thực vật', 6600);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Đào ngâm', 3300);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Ly giấy 16oz', 660);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Nắp nhựa 16oz', 660);
    add_transfer_line('PBI_DC_03_05', TIMESTAMP '2026-05-30 07:30:00', 'Thảo Điền', 'Ống hút giấy', 660);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Cà phê hạt Arabica', 9000);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Cà phê hạt Robusta', 14000);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Sữa tươi', 45000);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Đường cát', 10000);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Trà đen', 6500);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Trân châu đen', 8500);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Bột cacao', 3000);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Bột matcha', 2500);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Siro caramel', 3000);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Kem béo thực vật', 6000);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Đào ngâm', 3000);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Ly giấy 16oz', 600);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Nắp nhựa 16oz', 600);
    add_transfer_line('PBI_DC_03_06', TIMESTAMP '2026-05-30 07:30:00', 'Quang Trung', 'Ống hút giấy', 600);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Cà phê hạt Arabica', 10350);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Cà phê hạt Robusta', 16099);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Sữa tươi', 51749);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Đường cát', 11500);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Trà đen', 7474);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Trân châu đen', 9775);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Bột cacao', 3449);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Bột matcha', 2875);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Siro caramel', 3449);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Kem béo thực vật', 6899);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Đào ngâm', 3449);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Ly giấy 16oz', 690);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Nắp nhựa 16oz', 690);
    add_transfer_line('PBI_DC_03_07', TIMESTAMP '2026-05-30 07:30:00', 'Aeon Mall Bình Tân', 'Ống hút giấy', 690);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Cà phê hạt Arabica', 9450);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Cà phê hạt Robusta', 14700);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Sữa tươi', 47250);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Đường cát', 10500);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Trà đen', 6825);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Trân châu đen', 8925);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Bột cacao', 3150);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Bột matcha', 2625);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Siro caramel', 3150);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Kem béo thực vật', 6300);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Đào ngâm', 3150);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Ly giấy 16oz', 630);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Nắp nhựa 16oz', 630);
    add_transfer_line('PBI_DC_03_08', TIMESTAMP '2026-05-30 07:30:00', 'Nguyễn Trãi', 'Ống hút giấy', 630);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Cà phê hạt Arabica', 11812);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Cà phê hạt Robusta', 18375);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Sữa tươi', 59062);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Đường cát', 13125);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Trà đen', 8531);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Trân châu đen', 11156);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Bột cacao', 3937);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Bột matcha', 3281);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Siro caramel', 3937);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Kem béo thực vật', 7875);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Đào ngâm', 3937);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Ly giấy 16oz', 787);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Nắp nhựa 16oz', 787);
    add_transfer_line('PBI_DC_04_01', TIMESTAMP '2026-06-06 07:30:00', 'Bến Thành', 'Ống hút giấy', 787);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Cà phê hạt Arabica', 12757);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Cà phê hạt Robusta', 19845);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Sữa tươi', 63787);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Đường cát', 14175);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Trà đen', 9213);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Trân châu đen', 12048);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Bột cacao', 4252);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Bột matcha', 3543);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Siro caramel', 4252);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Kem béo thực vật', 8505);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Đào ngâm', 4252);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Ly giấy 16oz', 850);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Nắp nhựa 16oz', 850);
    add_transfer_line('PBI_DC_04_02', TIMESTAMP '2026-06-06 07:30:00', 'Phố đi bộ Nguyễn Huệ', 'Ống hút giấy', 850);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Cà phê hạt Arabica', 14175);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Cà phê hạt Robusta', 22050);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Sữa tươi', 70875);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Đường cát', 15750);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Trà đen', 10237);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Trân châu đen', 13387);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Bột cacao', 4725);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Bột matcha', 3937);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Siro caramel', 4725);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Kem béo thực vật', 9450);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Đào ngâm', 4725);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Ly giấy 16oz', 945);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Nắp nhựa 16oz', 945);
    add_transfer_line('PBI_DC_04_03', TIMESTAMP '2026-06-06 07:30:00', 'Landmark 81', 'Ống hút giấy', 945);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Cà phê hạt Arabica', 11340);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Cà phê hạt Robusta', 17640);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Sữa tươi', 56700);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Đường cát', 12600);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Trà đen', 8190);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Trân châu đen', 10710);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Bột cacao', 3780);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Bột matcha', 3150);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Siro caramel', 3780);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Kem béo thực vật', 7560);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Đào ngâm', 3780);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Ly giấy 16oz', 756);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Nắp nhựa 16oz', 756);
    add_transfer_line('PBI_DC_04_04', TIMESTAMP '2026-06-06 07:30:00', 'Giga Mall', 'Ống hút giấy', 756);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Cà phê hạt Arabica', 10395);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Cà phê hạt Robusta', 16170);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Sữa tươi', 51975);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Đường cát', 11550);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Trà đen', 7507);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Trân châu đen', 9817);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Bột cacao', 3465);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Bột matcha', 2887);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Siro caramel', 3465);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Kem béo thực vật', 6930);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Đào ngâm', 3465);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Ly giấy 16oz', 693);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Nắp nhựa 16oz', 693);
    add_transfer_line('PBI_DC_04_05', TIMESTAMP '2026-06-06 07:30:00', 'Thảo Điền', 'Ống hút giấy', 693);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Cà phê hạt Arabica', 9450);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Cà phê hạt Robusta', 14700);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Sữa tươi', 47250);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Đường cát', 10500);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Trà đen', 6825);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Trân châu đen', 8925);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Bột cacao', 3150);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Bột matcha', 2625);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Siro caramel', 3150);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Kem béo thực vật', 6300);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Đào ngâm', 3150);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Ly giấy 16oz', 630);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Nắp nhựa 16oz', 630);
    add_transfer_line('PBI_DC_04_06', TIMESTAMP '2026-06-06 07:30:00', 'Quang Trung', 'Ống hút giấy', 630);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Cà phê hạt Arabica', 10867);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Cà phê hạt Robusta', 16905);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Sữa tươi', 54337);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Đường cát', 12075);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Trà đen', 7848);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Trân châu đen', 10263);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Bột cacao', 3622);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Bột matcha', 3018);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Siro caramel', 3622);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Kem béo thực vật', 7244);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Đào ngâm', 3622);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Ly giấy 16oz', 724);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Nắp nhựa 16oz', 724);
    add_transfer_line('PBI_DC_04_07', TIMESTAMP '2026-06-06 07:30:00', 'Aeon Mall Bình Tân', 'Ống hút giấy', 724);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Cà phê hạt Arabica', 9922);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Cà phê hạt Robusta', 15435);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Sữa tươi', 49612);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Đường cát', 11025);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Trà đen', 7166);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Trân châu đen', 9371);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Bột cacao', 3307);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Bột matcha', 2756);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Siro caramel', 3307);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Kem béo thực vật', 6615);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Đào ngâm', 3307);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Ly giấy 16oz', 661);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Nắp nhựa 16oz', 661);
    add_transfer_line('PBI_DC_04_08', TIMESTAMP '2026-06-06 07:30:00', 'Nguyễn Trãi', 'Ống hút giấy', 661);

    -- Hoa don POS va tru kho theo cong thuc mon
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-17 08:10:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-17 08:10:00', 'CASH', 'Cacao sữa đá', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-17 08:47:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-17 08:47:00', 'BANK_TRANSFER', 'Caramel macchiato', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-17 09:13:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-17 09:13:00', 'BANK_TRANSFER', 'Caramel macchiato', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-17 09:50:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-17 09:50:00', 'EWALLET', 'Matcha latte', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-17 10:16:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-17 10:16:00', 'EWALLET', 'Matcha latte', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-17 10:53:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-17 10:53:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-17 11:19:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-17 11:19:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-17 11:56:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-17 11:56:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-17 12:22:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-17 12:22:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-17 12:59:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-17 12:59:00', 'EWALLET', 'Latte đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-17 08:25:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-17 08:25:00', 'EWALLET', 'Latte đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-17 09:02:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-17 09:02:00', 'CASH', 'Cà phê sữa đá', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-17 09:28:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-17 09:28:00', 'CASH', 'Cà phê sữa đá', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-17 10:05:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-17 10:05:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-17 10:11:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-17 10:11:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-17 10:48:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-17 10:48:00', 'EWALLET', 'Americano đá', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-18 08:10:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-18 08:10:00', 'BANK_TRANSFER', 'Matcha latte', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-18 08:47:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-18 08:47:00', 'EWALLET', 'Trà đào', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-18 09:13:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-18 09:13:00', 'EWALLET', 'Trà đào', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-18 09:50:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-18 09:50:00', 'CASH', 'Bạc xỉu', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-18 10:16:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-18 10:16:00', 'CASH', 'Bạc xỉu', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-18 10:53:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-18 10:53:00', 'BANK_TRANSFER', 'Latte đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-18 11:19:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-18 11:19:00', 'BANK_TRANSFER', 'Latte đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-18 11:56:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-18 11:56:00', 'EWALLET', 'Cà phê sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-18 12:22:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-18 12:22:00', 'EWALLET', 'Cà phê sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-18 12:59:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-18 12:59:00', 'CASH', 'Trà sữa trân châu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-18 08:25:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-18 08:25:00', 'CASH', 'Trà sữa trân châu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-18 09:02:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-18 09:02:00', 'BANK_TRANSFER', 'Americano đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-18 09:28:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-18 09:28:00', 'BANK_TRANSFER', 'Americano đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-18 10:05:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-18 10:05:00', 'EWALLET', 'Cacao sữa đá', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-18 10:11:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-18 10:11:00', 'EWALLET', 'Cacao sữa đá', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-18 10:48:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-18 10:48:00', 'CASH', 'Caramel macchiato', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-19 08:10:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-19 08:10:00', 'EWALLET', 'Bạc xỉu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-19 08:47:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-19 08:47:00', 'CASH', 'Latte đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-19 09:13:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-19 09:13:00', 'CASH', 'Latte đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-19 09:50:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-19 09:50:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-19 10:16:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-19 10:16:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-19 10:53:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-19 10:53:00', 'EWALLET', 'Trà sữa trân châu', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-19 11:19:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-19 11:19:00', 'EWALLET', 'Trà sữa trân châu', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-19 11:56:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-19 11:56:00', 'CASH', 'Americano đá', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-19 12:22:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-19 12:22:00', 'CASH', 'Americano đá', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-19 12:59:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-19 12:59:00', 'BANK_TRANSFER', 'Cacao sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-19 08:25:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-19 08:25:00', 'BANK_TRANSFER', 'Cacao sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-19 09:02:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-19 09:02:00', 'EWALLET', 'Caramel macchiato', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-19 09:28:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-19 09:28:00', 'EWALLET', 'Caramel macchiato', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-19 10:05:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-19 10:05:00', 'CASH', 'Matcha latte', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-19 10:11:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-19 10:11:00', 'CASH', 'Matcha latte', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-19 10:48:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-19 10:48:00', 'BANK_TRANSFER', 'Trà đào', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-20 08:10:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-20 08:10:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-20 08:47:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-20 08:47:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-20 09:13:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-20 09:13:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-20 09:50:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-20 09:50:00', 'EWALLET', 'Americano đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-20 10:16:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-20 10:16:00', 'EWALLET', 'Americano đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-20 10:53:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-20 10:53:00', 'CASH', 'Cacao sữa đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-20 11:19:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-20 11:19:00', 'CASH', 'Cacao sữa đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-20 11:56:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-20 11:56:00', 'BANK_TRANSFER', 'Caramel macchiato', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-20 12:22:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-20 12:22:00', 'BANK_TRANSFER', 'Caramel macchiato', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-20 12:59:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-20 12:59:00', 'EWALLET', 'Matcha latte', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-20 08:25:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-20 08:25:00', 'EWALLET', 'Matcha latte', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-20 09:02:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-20 09:02:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-20 09:28:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-20 09:28:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-20 10:05:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-20 10:05:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-20 10:11:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-20 10:11:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-20 10:48:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-20 10:48:00', 'EWALLET', 'Latte đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-21 08:10:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-21 08:10:00', 'BANK_TRANSFER', 'Americano đá', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-21 08:47:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-21 08:47:00', 'EWALLET', 'Cacao sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-21 09:13:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-21 09:13:00', 'EWALLET', 'Cacao sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-21 09:50:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-21 09:50:00', 'CASH', 'Caramel macchiato', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-21 10:16:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-21 10:16:00', 'CASH', 'Caramel macchiato', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-21 10:53:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-21 10:53:00', 'BANK_TRANSFER', 'Matcha latte', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-21 11:19:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-21 11:19:00', 'BANK_TRANSFER', 'Matcha latte', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-21 11:56:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-21 11:56:00', 'EWALLET', 'Trà đào', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-21 12:22:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-21 12:22:00', 'EWALLET', 'Trà đào', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-21 12:59:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-21 12:59:00', 'CASH', 'Bạc xỉu', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-21 08:25:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-21 08:25:00', 'CASH', 'Bạc xỉu', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-21 09:02:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-21 09:02:00', 'BANK_TRANSFER', 'Latte đá', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-21 09:28:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-21 09:28:00', 'BANK_TRANSFER', 'Latte đá', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-21 10:05:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-21 10:05:00', 'EWALLET', 'Cà phê sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-21 10:11:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-21 10:11:00', 'EWALLET', 'Cà phê sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-21 10:48:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-21 10:48:00', 'CASH', 'Trà sữa trân châu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-22 08:10:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-22 08:10:00', 'EWALLET', 'Caramel macchiato', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-22 08:47:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-22 08:47:00', 'CASH', 'Matcha latte', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-22 09:13:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-22 09:13:00', 'CASH', 'Matcha latte', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-22 09:50:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-22 09:50:00', 'BANK_TRANSFER', 'Trà đào', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-22 10:16:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-22 10:16:00', 'BANK_TRANSFER', 'Trà đào', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-22 10:53:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-22 10:53:00', 'EWALLET', 'Bạc xỉu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-22 11:19:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-22 11:19:00', 'EWALLET', 'Bạc xỉu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-22 11:56:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-22 11:56:00', 'CASH', 'Latte đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-22 12:22:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-22 12:22:00', 'CASH', 'Latte đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-22 12:59:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-22 12:59:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-22 08:25:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-22 08:25:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-22 09:02:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-22 09:02:00', 'EWALLET', 'Trà sữa trân châu', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-22 09:28:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-22 09:28:00', 'EWALLET', 'Trà sữa trân châu', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-22 10:05:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-22 10:05:00', 'CASH', 'Americano đá', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-22 10:11:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-22 10:11:00', 'CASH', 'Americano đá', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-22 10:48:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-22 10:48:00', 'BANK_TRANSFER', 'Cacao sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-23 08:10:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-23 08:10:00', 'CASH', 'Trà đào', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-23 08:47:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-23 08:47:00', 'BANK_TRANSFER', 'Bạc xỉu', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-23 09:13:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-23 09:13:00', 'BANK_TRANSFER', 'Bạc xỉu', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-23 09:50:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-23 09:50:00', 'EWALLET', 'Latte đá', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-23 10:16:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-23 10:16:00', 'EWALLET', 'Latte đá', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-23 10:53:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-23 10:53:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-23 11:19:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-23 11:19:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-23 11:56:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-23 11:56:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-23 12:22:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-23 12:22:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-23 12:59:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-23 12:59:00', 'EWALLET', 'Americano đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-23 08:25:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-23 08:25:00', 'EWALLET', 'Americano đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-23 09:02:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-23 09:02:00', 'CASH', 'Cacao sữa đá', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-23 09:28:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-23 09:28:00', 'CASH', 'Cacao sữa đá', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-23 10:05:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-23 10:05:00', 'BANK_TRANSFER', 'Caramel macchiato', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-23 10:11:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-23 10:11:00', 'BANK_TRANSFER', 'Caramel macchiato', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-23 10:48:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-23 10:48:00', 'EWALLET', 'Matcha latte', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-24 08:10:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-24 08:10:00', 'BANK_TRANSFER', 'Latte đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-24 08:47:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-24 08:47:00', 'EWALLET', 'Cà phê sữa đá', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-24 09:13:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-24 09:13:00', 'EWALLET', 'Cà phê sữa đá', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-24 09:50:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-24 09:50:00', 'CASH', 'Trà sữa trân châu', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-24 10:16:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-24 10:16:00', 'CASH', 'Trà sữa trân châu', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-24 10:53:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-24 10:53:00', 'BANK_TRANSFER', 'Americano đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-24 11:19:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-24 11:19:00', 'BANK_TRANSFER', 'Americano đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-24 11:56:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-24 11:56:00', 'EWALLET', 'Cacao sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-24 12:22:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-24 12:22:00', 'EWALLET', 'Cacao sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-24 12:59:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-24 12:59:00', 'CASH', 'Caramel macchiato', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-24 08:25:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-24 08:25:00', 'CASH', 'Caramel macchiato', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-24 09:02:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-24 09:02:00', 'BANK_TRANSFER', 'Matcha latte', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-24 09:28:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-24 09:28:00', 'BANK_TRANSFER', 'Matcha latte', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-24 10:05:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-24 10:05:00', 'EWALLET', 'Trà đào', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-24 10:11:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-24 10:11:00', 'EWALLET', 'Trà đào', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-24 10:48:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-24 10:48:00', 'CASH', 'Bạc xỉu', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-25 08:10:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-25 08:10:00', 'EWALLET', 'Trà sữa trân châu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-25 08:47:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-25 08:47:00', 'CASH', 'Americano đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-25 09:13:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-25 09:13:00', 'CASH', 'Americano đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-25 09:50:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-25 09:50:00', 'BANK_TRANSFER', 'Cacao sữa đá', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-25 10:16:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-25 10:16:00', 'BANK_TRANSFER', 'Cacao sữa đá', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-25 10:53:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-25 10:53:00', 'EWALLET', 'Caramel macchiato', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-25 11:19:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-25 11:19:00', 'EWALLET', 'Caramel macchiato', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-25 11:56:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-25 11:56:00', 'CASH', 'Matcha latte', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-25 12:22:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-25 12:22:00', 'CASH', 'Matcha latte', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-25 12:59:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-25 12:59:00', 'BANK_TRANSFER', 'Trà đào', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-25 08:25:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-25 08:25:00', 'BANK_TRANSFER', 'Trà đào', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-25 09:02:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-25 09:02:00', 'EWALLET', 'Bạc xỉu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-25 09:28:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-25 09:28:00', 'EWALLET', 'Bạc xỉu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-25 10:05:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-25 10:05:00', 'CASH', 'Latte đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-25 10:11:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-25 10:11:00', 'CASH', 'Latte đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-25 10:48:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-25 10:48:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-26 08:10:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-26 08:10:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-26 08:47:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-26 08:47:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-26 09:13:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-26 09:13:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-26 09:50:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-26 09:50:00', 'EWALLET', 'Matcha latte', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-26 10:16:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-26 10:16:00', 'EWALLET', 'Matcha latte', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-26 10:53:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-26 10:53:00', 'CASH', 'Trà đào', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-26 11:19:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-26 11:19:00', 'CASH', 'Trà đào', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-26 11:56:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-26 11:56:00', 'BANK_TRANSFER', 'Bạc xỉu', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-26 12:22:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-26 12:22:00', 'BANK_TRANSFER', 'Bạc xỉu', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-26 12:59:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-26 12:59:00', 'EWALLET', 'Latte đá', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-26 08:25:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-26 08:25:00', 'EWALLET', 'Latte đá', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-26 09:02:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-26 09:02:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-26 09:28:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-26 09:28:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-26 10:05:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-26 10:05:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-26 10:11:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-26 10:11:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-26 10:48:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-26 10:48:00', 'EWALLET', 'Americano đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-27 08:10:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-27 08:10:00', 'BANK_TRANSFER', 'Matcha latte', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-27 08:47:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-27 08:47:00', 'EWALLET', 'Trà đào', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-27 09:13:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-27 09:13:00', 'EWALLET', 'Trà đào', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-27 09:50:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-27 09:50:00', 'CASH', 'Bạc xỉu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-27 10:16:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-27 10:16:00', 'CASH', 'Bạc xỉu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-27 10:53:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-27 10:53:00', 'BANK_TRANSFER', 'Latte đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-27 11:19:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-27 11:19:00', 'BANK_TRANSFER', 'Latte đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-27 11:56:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-27 11:56:00', 'EWALLET', 'Cà phê sữa đá', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-27 12:22:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-27 12:22:00', 'EWALLET', 'Cà phê sữa đá', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-27 12:59:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-27 12:59:00', 'CASH', 'Trà sữa trân châu', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-27 08:25:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-27 08:25:00', 'CASH', 'Trà sữa trân châu', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-27 09:02:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-27 09:02:00', 'BANK_TRANSFER', 'Americano đá', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-27 09:28:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-27 09:28:00', 'BANK_TRANSFER', 'Americano đá', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-27 10:05:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-27 10:05:00', 'EWALLET', 'Cacao sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-27 10:11:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-27 10:11:00', 'EWALLET', 'Cacao sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-27 10:48:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-27 10:48:00', 'CASH', 'Caramel macchiato', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-28 08:10:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-28 08:10:00', 'EWALLET', 'Bạc xỉu', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-28 08:47:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-28 08:47:00', 'CASH', 'Latte đá', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-28 09:13:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-28 09:13:00', 'CASH', 'Latte đá', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-28 09:50:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-28 09:50:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-28 10:16:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-28 10:16:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-28 10:53:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-28 10:53:00', 'EWALLET', 'Trà sữa trân châu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-28 11:19:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-28 11:19:00', 'EWALLET', 'Trà sữa trân châu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-28 11:56:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-28 11:56:00', 'CASH', 'Americano đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-28 12:22:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-28 12:22:00', 'CASH', 'Americano đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-28 12:59:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-28 12:59:00', 'BANK_TRANSFER', 'Cacao sữa đá', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-28 08:25:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-28 08:25:00', 'BANK_TRANSFER', 'Cacao sữa đá', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-28 09:02:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-28 09:02:00', 'EWALLET', 'Caramel macchiato', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-28 09:28:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-28 09:28:00', 'EWALLET', 'Caramel macchiato', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-28 10:05:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-28 10:05:00', 'CASH', 'Matcha latte', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-28 10:11:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-28 10:11:00', 'CASH', 'Matcha latte', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-28 10:48:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-28 10:48:00', 'BANK_TRANSFER', 'Trà đào', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-29 08:10:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-29 08:10:00', 'CASH', 'Cà phê sữa đá', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-29 08:47:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-29 08:47:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-29 09:13:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-29 09:13:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-29 09:50:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-29 09:50:00', 'EWALLET', 'Americano đá', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-29 10:16:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-29 10:16:00', 'EWALLET', 'Americano đá', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-29 10:53:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-29 10:53:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-29 11:19:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-29 11:19:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-29 11:56:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-29 11:56:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-29 12:22:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-29 12:22:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-29 12:59:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-29 12:59:00', 'EWALLET', 'Matcha latte', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-29 08:25:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-29 08:25:00', 'EWALLET', 'Matcha latte', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-29 09:02:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-29 09:02:00', 'CASH', 'Trà đào', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-29 09:28:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-29 09:28:00', 'CASH', 'Trà đào', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-29 10:05:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-29 10:05:00', 'BANK_TRANSFER', 'Bạc xỉu', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-29 10:11:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-29 10:11:00', 'BANK_TRANSFER', 'Bạc xỉu', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-29 10:48:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-29 10:48:00', 'EWALLET', 'Latte đá', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-30 08:10:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-30 08:10:00', 'BANK_TRANSFER', 'Americano đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-30 08:47:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-30 08:47:00', 'EWALLET', 'Cacao sữa đá', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-30 09:13:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-30 09:13:00', 'EWALLET', 'Cacao sữa đá', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-30 09:50:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-30 09:50:00', 'CASH', 'Caramel macchiato', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-30 10:16:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-30 10:16:00', 'CASH', 'Caramel macchiato', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-30 10:53:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-30 10:53:00', 'BANK_TRANSFER', 'Matcha latte', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-30 11:19:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-30 11:19:00', 'BANK_TRANSFER', 'Matcha latte', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-30 11:56:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-30 11:56:00', 'EWALLET', 'Trà đào', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-30 12:22:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-30 12:22:00', 'EWALLET', 'Trà đào', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-30 12:59:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-30 12:59:00', 'CASH', 'Bạc xỉu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-30 08:25:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-30 08:25:00', 'CASH', 'Bạc xỉu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-30 09:02:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-30 09:02:00', 'BANK_TRANSFER', 'Latte đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-30 09:28:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-30 09:28:00', 'BANK_TRANSFER', 'Latte đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-30 10:05:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-30 10:05:00', 'EWALLET', 'Cà phê sữa đá', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-30 10:11:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-30 10:11:00', 'EWALLET', 'Cà phê sữa đá', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-30 10:48:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-30 10:48:00', 'CASH', 'Trà sữa trân châu', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-31 08:10:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-31 08:10:00', 'EWALLET', 'Caramel macchiato', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-31 08:47:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-05-31 08:47:00', 'CASH', 'Matcha latte', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-31 09:13:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-31 09:13:00', 'CASH', 'Matcha latte', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-31 09:50:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-05-31 09:50:00', 'BANK_TRANSFER', 'Trà đào', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-31 10:16:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-31 10:16:00', 'BANK_TRANSFER', 'Trà đào', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-31 10:53:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-05-31 10:53:00', 'EWALLET', 'Bạc xỉu', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-31 11:19:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-31 11:19:00', 'EWALLET', 'Bạc xỉu', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-31 11:56:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-05-31 11:56:00', 'CASH', 'Latte đá', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-31 12:22:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-31 12:22:00', 'CASH', 'Latte đá', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-31 12:59:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-05-31 12:59:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-31 08:25:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-31 08:25:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-31 09:02:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-05-31 09:02:00', 'EWALLET', 'Trà sữa trân châu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-31 09:28:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-31 09:28:00', 'EWALLET', 'Trà sữa trân châu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-31 10:05:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-05-31 10:05:00', 'CASH', 'Americano đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-31 10:11:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-31 10:11:00', 'CASH', 'Americano đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-31 10:48:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-05-31 10:48:00', 'BANK_TRANSFER', 'Cacao sữa đá', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-01 08:10:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-01 08:10:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-01 08:47:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-01 08:47:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-01 09:13:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-01 09:13:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-01 09:50:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-01 09:50:00', 'EWALLET', 'Latte đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-01 10:16:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-01 10:16:00', 'EWALLET', 'Latte đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-01 10:53:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-01 10:53:00', 'CASH', 'Cà phê sữa đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-01 11:19:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-01 11:19:00', 'CASH', 'Cà phê sữa đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-01 11:56:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-01 11:56:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-01 12:22:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-01 12:22:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-01 12:59:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-01 12:59:00', 'EWALLET', 'Americano đá', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-01 08:25:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-01 08:25:00', 'EWALLET', 'Americano đá', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-01 09:02:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-01 09:02:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-01 09:28:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-01 09:28:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-01 10:05:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-01 10:05:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-01 10:11:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-01 10:11:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-01 10:48:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-01 10:48:00', 'EWALLET', 'Matcha latte', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-02 08:10:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-02 08:10:00', 'BANK_TRANSFER', 'Latte đá', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-02 08:47:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-02 08:47:00', 'EWALLET', 'Cà phê sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-02 09:13:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-02 09:13:00', 'EWALLET', 'Cà phê sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-02 09:50:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-02 09:50:00', 'CASH', 'Trà sữa trân châu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-02 10:16:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-02 10:16:00', 'CASH', 'Trà sữa trân châu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-02 10:53:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-02 10:53:00', 'BANK_TRANSFER', 'Americano đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-02 11:19:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-02 11:19:00', 'BANK_TRANSFER', 'Americano đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-02 11:56:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-02 11:56:00', 'EWALLET', 'Cacao sữa đá', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-02 12:22:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-02 12:22:00', 'EWALLET', 'Cacao sữa đá', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-02 12:59:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-02 12:59:00', 'CASH', 'Caramel macchiato', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-02 08:25:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-02 08:25:00', 'CASH', 'Caramel macchiato', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-02 09:02:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-02 09:02:00', 'BANK_TRANSFER', 'Matcha latte', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-02 09:28:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-02 09:28:00', 'BANK_TRANSFER', 'Matcha latte', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-02 10:05:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-02 10:05:00', 'EWALLET', 'Trà đào', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-02 10:11:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-02 10:11:00', 'EWALLET', 'Trà đào', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-02 10:48:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-02 10:48:00', 'CASH', 'Bạc xỉu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-03 08:10:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-03 08:10:00', 'EWALLET', 'Trà sữa trân châu', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-03 08:47:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-03 08:47:00', 'CASH', 'Americano đá', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-03 09:13:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-03 09:13:00', 'CASH', 'Americano đá', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-03 09:50:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-03 09:50:00', 'BANK_TRANSFER', 'Cacao sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-03 10:16:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-03 10:16:00', 'BANK_TRANSFER', 'Cacao sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-03 10:53:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-03 10:53:00', 'EWALLET', 'Caramel macchiato', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-03 11:19:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-03 11:19:00', 'EWALLET', 'Caramel macchiato', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-03 11:56:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-03 11:56:00', 'CASH', 'Matcha latte', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-03 12:22:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-03 12:22:00', 'CASH', 'Matcha latte', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-03 12:59:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-03 12:59:00', 'BANK_TRANSFER', 'Trà đào', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-03 08:25:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-03 08:25:00', 'BANK_TRANSFER', 'Trà đào', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-03 09:02:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-03 09:02:00', 'EWALLET', 'Bạc xỉu', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-03 09:28:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-03 09:28:00', 'EWALLET', 'Bạc xỉu', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-03 10:05:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-03 10:05:00', 'CASH', 'Latte đá', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-03 10:11:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-03 10:11:00', 'CASH', 'Latte đá', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-03 10:48:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-03 10:48:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-04 08:10:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-04 08:10:00', 'CASH', 'Cacao sữa đá', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-04 08:47:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-04 08:47:00', 'BANK_TRANSFER', 'Caramel macchiato', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-04 09:13:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-04 09:13:00', 'BANK_TRANSFER', 'Caramel macchiato', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-04 09:50:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-04 09:50:00', 'EWALLET', 'Matcha latte', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-04 10:16:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-04 10:16:00', 'EWALLET', 'Matcha latte', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-04 10:53:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-04 10:53:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-04 11:19:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-04 11:19:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-04 11:56:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-04 11:56:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-04 12:22:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-04 12:22:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-04 12:59:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-04 12:59:00', 'EWALLET', 'Latte đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-04 08:25:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-04 08:25:00', 'EWALLET', 'Latte đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-04 09:02:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-04 09:02:00', 'CASH', 'Cà phê sữa đá', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-04 09:28:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-04 09:28:00', 'CASH', 'Cà phê sữa đá', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-04 10:05:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-04 10:05:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-04 10:11:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-04 10:11:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-04 10:48:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-04 10:48:00', 'EWALLET', 'Americano đá', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-05 08:10:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-05 08:10:00', 'BANK_TRANSFER', 'Matcha latte', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-05 08:47:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-05 08:47:00', 'EWALLET', 'Trà đào', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-05 09:13:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-05 09:13:00', 'EWALLET', 'Trà đào', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-05 09:50:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-05 09:50:00', 'CASH', 'Bạc xỉu', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-05 10:16:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-05 10:16:00', 'CASH', 'Bạc xỉu', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-05 10:53:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-05 10:53:00', 'BANK_TRANSFER', 'Latte đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-05 11:19:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-05 11:19:00', 'BANK_TRANSFER', 'Latte đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-05 11:56:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-05 11:56:00', 'EWALLET', 'Cà phê sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-05 12:22:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-05 12:22:00', 'EWALLET', 'Cà phê sữa đá', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-05 12:59:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-05 12:59:00', 'CASH', 'Trà sữa trân châu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-05 08:25:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-05 08:25:00', 'CASH', 'Trà sữa trân châu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-05 09:02:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-05 09:02:00', 'BANK_TRANSFER', 'Americano đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-05 09:28:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-05 09:28:00', 'BANK_TRANSFER', 'Americano đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-05 10:05:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-05 10:05:00', 'EWALLET', 'Cacao sữa đá', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-05 10:11:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-05 10:11:00', 'EWALLET', 'Cacao sữa đá', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-05 10:48:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-05 10:48:00', 'CASH', 'Caramel macchiato', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-06 08:10:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-06 08:10:00', 'EWALLET', 'Bạc xỉu', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-06 08:47:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-06 08:47:00', 'CASH', 'Latte đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-06 09:13:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-06 09:13:00', 'CASH', 'Latte đá', 1);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-06 09:50:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-06 09:50:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-06 10:16:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-06 10:16:00', 'BANK_TRANSFER', 'Cà phê sữa đá', 2);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-06 10:53:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-06 10:53:00', 'EWALLET', 'Trà sữa trân châu', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-06 11:19:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-06 11:19:00', 'EWALLET', 'Trà sữa trân châu', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-06 11:56:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-06 11:56:00', 'CASH', 'Americano đá', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-06 12:22:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-06 12:22:00', 'CASH', 'Americano đá', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-06 12:59:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-06 12:59:00', 'BANK_TRANSFER', 'Cacao sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-06 08:25:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-06 08:25:00', 'BANK_TRANSFER', 'Cacao sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-06 09:02:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-06 09:02:00', 'EWALLET', 'Caramel macchiato', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-06 09:28:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-06 09:28:00', 'EWALLET', 'Caramel macchiato', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-06 10:05:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-06 10:05:00', 'CASH', 'Matcha latte', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-06 10:11:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-06 10:11:00', 'CASH', 'Matcha latte', 1);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-06 10:48:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-06 10:48:00', 'BANK_TRANSFER', 'Trà đào', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-07 08:10:00', 'CASH', 'Cacao sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-07 08:10:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-07 08:47:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Bến Thành', 'thungan_nguyenhue', TIMESTAMP '2026-06-07 08:47:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-07 09:13:00', 'BANK_TRANSFER', 'Caramel macchiato', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-07 09:13:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-07 09:50:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Phố đi bộ Nguyễn Huệ', 'thungan_leloi', TIMESTAMP '2026-06-07 09:50:00', 'EWALLET', 'Americano đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-07 10:16:00', 'EWALLET', 'Matcha latte', 3);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-07 10:16:00', 'EWALLET', 'Americano đá', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-07 10:53:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Landmark 81', 'thungan_vovantan', TIMESTAMP '2026-06-07 10:53:00', 'CASH', 'Cacao sữa đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-07 11:19:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-07 11:19:00', 'CASH', 'Cacao sữa đá', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-07 11:56:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Giga Mall', 'thungan_phanxichlong', TIMESTAMP '2026-06-07 11:56:00', 'BANK_TRANSFER', 'Caramel macchiato', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-07 12:22:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-07 12:22:00', 'BANK_TRANSFER', 'Caramel macchiato', 1);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-07 12:59:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Thảo Điền', 'thungan_trannao', TIMESTAMP '2026-06-07 12:59:00', 'EWALLET', 'Matcha latte', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-07 08:25:00', 'EWALLET', 'Latte đá', 3);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-07 08:25:00', 'EWALLET', 'Matcha latte', 2);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-07 09:02:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Quang Trung', 'thungan_quangtrung', TIMESTAMP '2026-06-07 09:02:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-07 09:28:00', 'CASH', 'Cà phê sữa đá', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-07 09:28:00', 'CASH', 'Trà đào', 1);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-07 10:05:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Aeon Mall Bình Tân', 'thungan_binhtan', TIMESTAMP '2026-06-07 10:05:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-07 10:11:00', 'BANK_TRANSFER', 'Trà sữa trân châu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-07 10:11:00', 'BANK_TRANSFER', 'Bạc xỉu', 2);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-07 10:48:00', 'EWALLET', 'Americano đá', 3);
    add_invoice_line('Nguyễn Trãi', 'thungan_nguyentrai', TIMESTAMP '2026-06-07 10:48:00', 'EWALLET', 'Latte đá', 1);

    -- Hao hut va kiem kho tai chi nhanh
    ensure_wastage('PBI_HAOHUT_01_01', TIMESTAMP '2026-05-23 22:06:00', 'Bến Thành', 'qlcn_nguyenhue', 'Sữa tươi', 312, 'SPILL');
    ensure_wastage('PBI_HAOHUT_01_02', TIMESTAMP '2026-05-26 22:06:00', 'Bến Thành', 'qlcn_nguyenhue', 'Trân châu đen', 225, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_01_03', TIMESTAMP '2026-05-29 22:06:00', 'Bến Thành', 'qlcn_nguyenhue', 'Ly giấy 16oz', 15, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_01_04', TIMESTAMP '2026-06-01 22:06:00', 'Bến Thành', 'qlcn_nguyenhue', 'Đào ngâm', 112, 'SPILL');
    ensure_stocktake('PBI_KIEM_01_SUA', TIMESTAMP '2026-06-09 22:01:00', 'Bến Thành', 'qlcn_nguyenhue', 'Sữa tươi', -150, 'Lech sau doi so voi ca ban hang');
    ensure_stocktake('PBI_KIEM_01_TC', TIMESTAMP '2026-06-09 22:01:00', 'Bến Thành', 'qlcn_nguyenhue', 'Trân châu đen', -100, 'Hao hut trong qua trinh bao quan');
    ensure_stocktake('PBI_KIEM_01_LY', TIMESTAMP '2026-06-09 22:01:00', 'Bến Thành', 'qlcn_nguyenhue', 'Ly giấy 16oz', -5, 'Vo hong khi kiem dem');
    ensure_wastage('PBI_HAOHUT_02_01', TIMESTAMP '2026-05-23 22:07:00', 'Phố đi bộ Nguyễn Huệ', 'qlcn_leloi', 'Sữa tươi', 337, 'SPILL');
    ensure_wastage('PBI_HAOHUT_02_02', TIMESTAMP '2026-05-26 22:07:00', 'Phố đi bộ Nguyễn Huệ', 'qlcn_leloi', 'Trân châu đen', 243, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_02_03', TIMESTAMP '2026-05-29 22:07:00', 'Phố đi bộ Nguyễn Huệ', 'qlcn_leloi', 'Ly giấy 16oz', 16, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_02_04', TIMESTAMP '2026-06-01 22:07:00', 'Phố đi bộ Nguyễn Huệ', 'qlcn_leloi', 'Đào ngâm', 121, 'SPILL');
    ensure_stocktake('PBI_KIEM_02_SUA', TIMESTAMP '2026-06-09 22:02:00', 'Phố đi bộ Nguyễn Huệ', 'qlcn_leloi', 'Sữa tươi', -162, 'Lech sau doi so voi ca ban hang');
    ensure_stocktake('PBI_KIEM_02_TC', TIMESTAMP '2026-06-09 22:02:00', 'Phố đi bộ Nguyễn Huệ', 'qlcn_leloi', 'Trân châu đen', -108, 'Hao hut trong qua trinh bao quan');
    ensure_stocktake('PBI_KIEM_02_LY', TIMESTAMP '2026-06-09 22:02:00', 'Phố đi bộ Nguyễn Huệ', 'qlcn_leloi', 'Ly giấy 16oz', -5, 'Vo hong khi kiem dem');
    ensure_wastage('PBI_HAOHUT_03_01', TIMESTAMP '2026-05-23 22:08:00', 'Landmark 81', 'qlcn_vovantan', 'Sữa tươi', 375, 'SPILL');
    ensure_wastage('PBI_HAOHUT_03_02', TIMESTAMP '2026-05-26 22:08:00', 'Landmark 81', 'qlcn_vovantan', 'Trân châu đen', 270, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_03_03', TIMESTAMP '2026-05-29 22:08:00', 'Landmark 81', 'qlcn_vovantan', 'Ly giấy 16oz', 18, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_03_04', TIMESTAMP '2026-06-01 22:08:00', 'Landmark 81', 'qlcn_vovantan', 'Đào ngâm', 135, 'SPILL');
    ensure_stocktake('PBI_KIEM_03_SUA', TIMESTAMP '2026-06-09 22:03:00', 'Landmark 81', 'qlcn_vovantan', 'Sữa tươi', -180, 'Lech sau doi so voi ca ban hang');
    ensure_stocktake('PBI_KIEM_03_TC', TIMESTAMP '2026-06-09 22:03:00', 'Landmark 81', 'qlcn_vovantan', 'Trân châu đen', -120, 'Hao hut trong qua trinh bao quan');
    ensure_stocktake('PBI_KIEM_03_LY', TIMESTAMP '2026-06-09 22:03:00', 'Landmark 81', 'qlcn_vovantan', 'Ly giấy 16oz', -5, 'Vo hong khi kiem dem');
    ensure_wastage('PBI_HAOHUT_04_01', TIMESTAMP '2026-05-23 22:09:00', 'Giga Mall', 'qlcn_phanxichlong', 'Sữa tươi', 300, 'SPILL');
    ensure_wastage('PBI_HAOHUT_04_02', TIMESTAMP '2026-05-26 22:09:00', 'Giga Mall', 'qlcn_phanxichlong', 'Trân châu đen', 216, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_04_03', TIMESTAMP '2026-05-29 22:09:00', 'Giga Mall', 'qlcn_phanxichlong', 'Ly giấy 16oz', 14, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_04_04', TIMESTAMP '2026-06-01 22:09:00', 'Giga Mall', 'qlcn_phanxichlong', 'Đào ngâm', 108, 'SPILL');
    ensure_stocktake('PBI_KIEM_04_SUA', TIMESTAMP '2026-06-09 22:04:00', 'Giga Mall', 'qlcn_phanxichlong', 'Sữa tươi', -144, 'Lech sau doi so voi ca ban hang');
    ensure_stocktake('PBI_KIEM_04_TC', TIMESTAMP '2026-06-09 22:04:00', 'Giga Mall', 'qlcn_phanxichlong', 'Trân châu đen', -96, 'Hao hut trong qua trinh bao quan');
    ensure_stocktake('PBI_KIEM_04_LY', TIMESTAMP '2026-06-09 22:04:00', 'Giga Mall', 'qlcn_phanxichlong', 'Ly giấy 16oz', -5, 'Vo hong khi kiem dem');
    ensure_wastage('PBI_HAOHUT_05_01', TIMESTAMP '2026-05-23 22:10:00', 'Thảo Điền', 'qlcn_trannao', 'Sữa tươi', 275, 'SPILL');
    ensure_wastage('PBI_HAOHUT_05_02', TIMESTAMP '2026-05-26 22:10:00', 'Thảo Điền', 'qlcn_trannao', 'Trân châu đen', 198, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_05_03', TIMESTAMP '2026-05-29 22:10:00', 'Thảo Điền', 'qlcn_trannao', 'Ly giấy 16oz', 13, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_05_04', TIMESTAMP '2026-06-01 22:10:00', 'Thảo Điền', 'qlcn_trannao', 'Đào ngâm', 99, 'SPILL');
    ensure_stocktake('PBI_KIEM_05_SUA', TIMESTAMP '2026-06-09 22:05:00', 'Thảo Điền', 'qlcn_trannao', 'Sữa tươi', -132, 'Lech sau doi so voi ca ban hang');
    ensure_stocktake('PBI_KIEM_05_TC', TIMESTAMP '2026-06-09 22:05:00', 'Thảo Điền', 'qlcn_trannao', 'Trân châu đen', -88, 'Hao hut trong qua trinh bao quan');
    ensure_stocktake('PBI_KIEM_05_LY', TIMESTAMP '2026-06-09 22:05:00', 'Thảo Điền', 'qlcn_trannao', 'Ly giấy 16oz', -5, 'Vo hong khi kiem dem');
    ensure_wastage('PBI_HAOHUT_06_01', TIMESTAMP '2026-05-23 22:11:00', 'Quang Trung', 'qlcn_quangtrung', 'Sữa tươi', 250, 'SPILL');
    ensure_wastage('PBI_HAOHUT_06_02', TIMESTAMP '2026-05-26 22:11:00', 'Quang Trung', 'qlcn_quangtrung', 'Trân châu đen', 180, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_06_03', TIMESTAMP '2026-05-29 22:11:00', 'Quang Trung', 'qlcn_quangtrung', 'Ly giấy 16oz', 12, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_06_04', TIMESTAMP '2026-06-01 22:11:00', 'Quang Trung', 'qlcn_quangtrung', 'Đào ngâm', 90, 'SPILL');
    ensure_stocktake('PBI_KIEM_06_SUA', TIMESTAMP '2026-06-09 22:06:00', 'Quang Trung', 'qlcn_quangtrung', 'Sữa tươi', -120, 'Lech sau doi so voi ca ban hang');
    ensure_stocktake('PBI_KIEM_06_TC', TIMESTAMP '2026-06-09 22:06:00', 'Quang Trung', 'qlcn_quangtrung', 'Trân châu đen', -80, 'Hao hut trong qua trinh bao quan');
    ensure_stocktake('PBI_KIEM_06_LY', TIMESTAMP '2026-06-09 22:06:00', 'Quang Trung', 'qlcn_quangtrung', 'Ly giấy 16oz', -5, 'Vo hong khi kiem dem');
    ensure_wastage('PBI_HAOHUT_07_01', TIMESTAMP '2026-05-23 22:12:00', 'Aeon Mall Bình Tân', 'qlcn_binhtan', 'Sữa tươi', 287, 'SPILL');
    ensure_wastage('PBI_HAOHUT_07_02', TIMESTAMP '2026-05-26 22:12:00', 'Aeon Mall Bình Tân', 'qlcn_binhtan', 'Trân châu đen', 206, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_07_03', TIMESTAMP '2026-05-29 22:12:00', 'Aeon Mall Bình Tân', 'qlcn_binhtan', 'Ly giấy 16oz', 13, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_07_04', TIMESTAMP '2026-06-01 22:12:00', 'Aeon Mall Bình Tân', 'qlcn_binhtan', 'Đào ngâm', 103, 'SPILL');
    ensure_stocktake('PBI_KIEM_07_SUA', TIMESTAMP '2026-06-09 22:07:00', 'Aeon Mall Bình Tân', 'qlcn_binhtan', 'Sữa tươi', -138, 'Lech sau doi so voi ca ban hang');
    ensure_stocktake('PBI_KIEM_07_TC', TIMESTAMP '2026-06-09 22:07:00', 'Aeon Mall Bình Tân', 'qlcn_binhtan', 'Trân châu đen', -92, 'Hao hut trong qua trinh bao quan');
    ensure_stocktake('PBI_KIEM_07_LY', TIMESTAMP '2026-06-09 22:07:00', 'Aeon Mall Bình Tân', 'qlcn_binhtan', 'Ly giấy 16oz', -5, 'Vo hong khi kiem dem');
    ensure_wastage('PBI_HAOHUT_08_01', TIMESTAMP '2026-05-23 22:13:00', 'Nguyễn Trãi', 'qlcn_nguyentrai', 'Sữa tươi', 262, 'SPILL');
    ensure_wastage('PBI_HAOHUT_08_02', TIMESTAMP '2026-05-26 22:13:00', 'Nguyễn Trãi', 'qlcn_nguyentrai', 'Trân châu đen', 189, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_08_03', TIMESTAMP '2026-05-29 22:13:00', 'Nguyễn Trãi', 'qlcn_nguyentrai', 'Ly giấy 16oz', 12, 'DAMAGED');
    ensure_wastage('PBI_HAOHUT_08_04', TIMESTAMP '2026-06-01 22:13:00', 'Nguyễn Trãi', 'qlcn_nguyentrai', 'Đào ngâm', 94, 'SPILL');
    ensure_stocktake('PBI_KIEM_08_SUA', TIMESTAMP '2026-06-09 22:08:00', 'Nguyễn Trãi', 'qlcn_nguyentrai', 'Sữa tươi', -126, 'Lech sau doi so voi ca ban hang');
    ensure_stocktake('PBI_KIEM_08_TC', TIMESTAMP '2026-06-09 22:08:00', 'Nguyễn Trãi', 'qlcn_nguyentrai', 'Trân châu đen', -84, 'Hao hut trong qua trinh bao quan');
    ensure_stocktake('PBI_KIEM_08_LY', TIMESTAMP '2026-06-09 22:08:00', 'Nguyễn Trãi', 'qlcn_nguyentrai', 'Ly giấy 16oz', -5, 'Vo hong khi kiem dem');

    -- Dong bo TONKHO tu LOHANG_NGUYENLIEU de TONKHO la bang tong hop, LOHANG la source of truth.
    UPDATE LOHANG_NGUYENLIEU
    SET trang_thai = CASE
        WHEN so_luong_con_lai <= 0 THEN 'USED_UP'
        WHEN han_su_dung IS NOT NULL AND han_su_dung < TRUNC(SYSDATE) THEN 'EXPIRED'
        ELSE 'ACTIVE'
    END;

    sync_stock_from_lots;

    COMMIT;
END;
/

-- ============================================================================
-- Goi y cac cau query kiem tra nhanh sau khi chay
-- ============================================================================
-- SELECT COUNT(*) FROM PHIEUNHAP WHERE ghi_chu LIKE 'PBI_%';
-- SELECT COUNT(*) FROM PHIEUXUAT WHERE ghi_chu LIKE 'PBI_%';
-- SELECT COUNT(*) FROM PHIEUDIEUCHUYEN WHERE ghi_chu LIKE 'PBI_%';
-- SELECT COUNT(*) FROM PHIEUHAOHUT WHERE ghi_chu LIKE 'PBI_%';
-- SELECT COUNT(*) FROM PHIEUKIEMKHO WHERE ghi_chu LIKE 'PBI_%';
-- SELECT COUNT(*) FROM HOADON WHERE thoi_gian_tao_hoa_don BETWEEN TIMESTAMP '2026-05-17 00:00:00' AND TIMESTAMP '2026-06-10 23:59:59';
-- SELECT COUNT(*) FROM BANHANG_TRUKHO b JOIN HOADON h ON h.ma_hoa_don = b.ma_hoa_don WHERE h.thoi_gian_tao_hoa_don BETWEEN TIMESTAMP '2026-05-17 00:00:00' AND TIMESTAMP '2026-06-10 23:59:59';
-- SELECT COUNT(*) FROM NHATKY_KHO WHERE thoi_gian BETWEEN TIMESTAMP '2026-05-12 00:00:00' AND TIMESTAMP '2026-06-10 23:59:59';
-- SELECT k.ten_kho, nl.ten_nguyen_lieu, tk.so_luong_ton FROM TONKHO tk JOIN KHO k ON k.ma_kho = tk.ma_kho JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = tk.ma_nguyen_lieu ORDER BY k.ten_kho, nl.ten_nguyen_lieu;

DROP FUNCTION pbi_get_user_id;
DROP FUNCTION pbi_get_supplier_id;
DROP FUNCTION pbi_get_unit_id;
DROP FUNCTION pbi_get_ingredient_id;
DROP FUNCTION pbi_get_product_id;
DROP FUNCTION pbi_get_branch_id;
DROP FUNCTION pbi_get_branch_kho;
DROP FUNCTION pbi_get_pos_id;


-- ============================================================================
-- END: seed tong hop
-- SEED_FULL_ONCE_CLEAN.SQL completed.
-- ============================================================================
