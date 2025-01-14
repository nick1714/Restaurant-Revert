package RTDRestaurant.Controller.Service;

import RTDRestaurant.Controller.Connection.DatabaseConnection;
import RTDRestaurant.Model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import javax.swing.ImageIcon;

public class ServiceAdmin {

    private final Connection con;

    //Connect tới DataBase       
    public ServiceAdmin() {
        con = DatabaseConnection.getInstance().getConnection();
    }

    //Lấy toàn bộ danh sách nhân viên
    public ArrayList<ModelNhanVien> getListNV() throws SQLException {
        ArrayList<ModelNhanVien> list = new ArrayList();
        String sql = "SELECT ID_NV, TenNV, DATE_FORMAT(NgayVL, '%d-%m-%Y') AS Ngay, SDT, Chucvu, Tinhtrang FROM NhanVien";
        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id_NV = r.getInt(1);
            String tenNV = r.getString(2);
            String ngayVL = r.getString(3);
            String sdt = r.getString(4);
            String chucvu = r.getString(5);
            String tinhtrang = r.getString(6);
            ModelNhanVien data = new ModelNhanVien(id_NV, tenNV, ngayVL, sdt, chucvu, tinhtrang);
            list.add(data);
        }
        p.close();
        r.close();
        return list;
    }

    //Lấy thông tin nhân viên từ ID
    public ModelNhanVien getNV(int idNV) throws SQLException {
        ModelNhanVien data = null;
        String sql = "SELECT ID_NV, TenNV, DATE_FORMAT(NgayVL, '%d-%m-%Y') AS Ngay, SDT, ChucVu, TinhTrang FROM NhanVien WHERE ID_NV=?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setInt(1, idNV);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id_NV = r.getInt(1);
            String tenNV = r.getString(2);
            String ngayVL = r.getString(3);
            String sdt = r.getString(4);
            String chucvu = r.getString(5);
            String tinhTrang = r.getString(6);
            data = new ModelNhanVien(id_NV, tenNV, ngayVL, sdt, chucvu, tinhTrang);
        }
        p.close();
        r.close();
        return data;
    }

    //Lấy Mã nhân viên tiếp theo được thêm
    public int getNextID_NV() throws SQLException {
        int id = 0;
        String sql = "SELECT MIN(ID_NV) + 1 FROM NhanVien WHERE ID_NV + 1 NOT IN (SELECT ID_NV FROM NhanVien)";

        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            id = r.getInt(1);
        }
        return id;
    }

    //Thêm mới một nhân viên
    public void insertNV(ModelNhanVien data) throws SQLException {
        String sql = "INSERT INTO NhanVien(ID_NV, TenNV, NgayVL, SDT, Chucvu, TinhTrang) VALUES (?, ?, ?, ?, ?, 'Dang lam viec')";

        // Chuyển đổi ngày sang định dạng yyyy-MM-dd
        String ngayVL = data.getNgayVL();
        String ngayVLFormatted = convertDateFormat(ngayVL);

        if (ngayVLFormatted == null) {
            System.out.println("Định dạng ngày không hợp lệ: " + ngayVL);
            return; // Dừng lại nếu ngày không hợp lệ
        }

        PreparedStatement p = con.prepareStatement(sql);
        p.setInt(1, data.getId_NV());
        p.setString(2, data.getTenNV());
        p.setString(3, ngayVLFormatted);
        p.setString(4, data.getSdt());
        p.setString(5, data.getChucvu());
        p.execute();
        p.close();
    }

    private String convertDateFormat(String dateStr) {
        try {
            // Định dạng đầu vào là dd-MM-yyyy
            SimpleDateFormat fromUser = new SimpleDateFormat("dd-MM-yyyy");
            Date date = fromUser.parse(dateStr);

            // Định dạng đầu ra là yyyy-MM-dd
            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
            return myFormat.format(date);

        } catch (ParseException e) {
            System.err.println("Lỗi chuyển đổi định dạng ngày: " + e.getMessage());
            return null; // Trả về null nếu có lỗi
        }
    }
    
    

    //Sa thải một nhân viên, cập nhận tình trạng thành 'Da nghi viec'
    public void FireStaff(int idNV) throws SQLException {
        String sql = "UPDATE NhanVien SET TinhTrang = 'Da nghi viec' WHERE ID_NV = ?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setInt(1, idNV);
        p.execute();
        p.close();
    }

    //Cập nhật thông tin của một nhân viên
    public void UpdateNV(ModelNhanVien data) throws SQLException {
        String sql = "UPDATE NhanVien SET TenNV=?, SDT=?, Chucvu=? WHERE ID_NV=?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, data.getTenNV());
        p.setString(2, data.getSdt());
        p.setString(3, data.getChucvu());
        p.setInt(4, data.getId_NV());
        p.execute();
        p.close();
    }

    //Lấy toàn bộ danh sách hóa đơn trong Tất cả/ngày/tháng/năm
    public ArrayList<ModelHoaDon> getListHDIn(String txt) throws SQLException {
        ArrayList<ModelHoaDon> list = new ArrayList();
        String sql = "SELECT ID_HoaDon, ID_KH, ID_Ban, DATE_FORMAT(NgayHD, '%d-%m-%Y') AS Ngay, Tienmonan, Tiengiam, Tongtien FROM HoaDon";

        if (txt.equals("Tất cả")) {
            sql = "SELECT ID_HoaDon, ID_KH, ID_Ban, DATE_FORMAT(NgayHD, '%d-%m-%Y') AS Ngay, Tienmonan, Tiengiam, Tongtien FROM HoaDon";
        } else if (txt.equals("Hôm nay")) {
            sql = "SELECT ID_HoaDon, ID_KH, ID_Ban, DATE_FORMAT(NgayHD, '%d-%m-%Y') AS Ngay, Tienmonan, Tiengiam, Tongtien FROM HoaDon "
                    + "WHERE DATE(NgayHD) = CURDATE()";
        } else if (txt.equals("Tháng này")) {
            sql = "SELECT ID_HoaDon, ID_KH, ID_Ban, DATE_FORMAT(NgayHD, '%d-%m-%Y') AS Ngay, Tienmonan, Tiengiam, Tongtien FROM HoaDon "
                    + "WHERE MONTH(NgayHD) = MONTH(CURDATE()) AND YEAR(NgayHD) = YEAR(CURDATE())";
        } else if (txt.equals("Năm này")) {
            sql = "SELECT ID_HoaDon, ID_KH, ID_Ban, DATE_FORMAT(NgayHD, '%d-%m-%Y') AS Ngay, Tienmonan, Tiengiam, Tongtien FROM HoaDon "
                    + "WHERE YEAR(NgayHD) = YEAR(CURDATE())";
        }
        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int idHoaDon = r.getInt(1);
            int idKH = r.getInt(2);
            int idBan = r.getInt(3);
            String ngayHD = r.getString(4);
            int tienMonAn = r.getInt(5);
            int tienGiam = r.getInt(6);
            int tongtien = r.getInt(7);
            ModelHoaDon data = new ModelHoaDon(idHoaDon, idKH, idBan, ngayHD, tienMonAn, tienGiam, tongtien);
            list.add(data);
        }
        p.close();
        r.close();
        return list;
    }

    //Lấy tổng doanh thu Hóa Đơn trong ngày/tháng/năm
    public int getRevenueHD(String filter) throws SQLException {
        int revenue = 0;

        String sql = "SELECT SUM(Tongtien) FROM HoaDon WHERE DATE(NgayHD) = CURDATE()";

        if (filter.equals("Hôm nay")) {
            sql = "SELECT SUM(Tongtien) FROM HoaDon WHERE DATE(NgayHD) = CURDATE()";
        } else if (filter.equals("Tháng này")) {
            sql = "SELECT SUM(Tongtien) FROM HoaDon WHERE MONTH(NgayHD) = MONTH(CURDATE()) AND YEAR(NgayHD) = YEAR(CURDATE())";
        } else if (filter.equals("Năm này")) {
            sql = "SELECT SUM(Tongtien) FROM HoaDon WHERE YEAR(NgayHD) = YEAR(CURDATE())";
        }

        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            revenue = r.getInt(1);
        }
        p.close();
        r.close();
        return revenue;
    }

    //Lấy tổng doanh thu Hóa Đơn của tháng trước
    public int getPreMonthRevenueHD() throws SQLException {
        int Pre_revenue = 0;
        String sql = "SELECT SUM(Tongtien) FROM HoaDon WHERE MONTH(NgayHD) = (MONTH(CURDATE()) - 1) "
                + "AND YEAR(NgayHD) = YEAR(CURDATE())";

        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            Pre_revenue = r.getInt(1);
        }
        p.close();
        r.close();
        return Pre_revenue;
    }

    //Lấy toàn bộ danh sách Phiếu Nhập Kho trong Tất cả/ngày/tháng/năm
    public ArrayList<ModelPNK> getListPNKIn(String txt) throws SQLException {
        ArrayList<ModelPNK> list = new ArrayList();
        String sql = "SELECT ID_NK, ID_NV, DATE_FORMAT(NgayNK, '%d-%m-%Y') AS Ngay, Tongtien FROM PhieuNK ORDER BY ID_NK";
        if (txt.equals("Tất cả")) {
            sql = "SELECT ID_NK, ID_NV, DATE_FORMAT(NgayNK, '%d-%m-%Y') AS Ngay, Tongtien FROM PhieuNK ORDER BY ID_NK";
        } else if (txt.equals("Hôm nay")) {
            sql = "SELECT ID_NK, ID_NV, DATE_FORMAT(NgayNK, '%d-%m-%Y') AS Ngay, Tongtien FROM PhieuNK "
                    + "WHERE DATE(NgayNK) = CURDATE() ORDER BY ID_NK";
        } else if (txt.equals("Tháng này")) {
            sql = "SELECT ID_NK, ID_NV, DATE_FORMAT(NgayNK, '%d-%m-%Y') AS Ngay, Tongtien FROM PhieuNK "
                    + "WHERE MONTH(NgayNK) = MONTH(CURDATE()) AND YEAR(NgayNK) = YEAR(CURDATE()) ORDER BY ID_NK";
        } else if (txt.equals("Năm này")) {
            sql = "SELECT ID_NK, ID_NV, DATE_FORMAT(NgayNK, '%d-%m-%Y') AS Ngay, Tongtien FROM PhieuNK "
                    + "WHERE YEAR(NgayNK) = YEAR(CURDATE()) ORDER BY ID_NK";
        }

        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int idNK = r.getInt(1);
            int idNV = r.getInt(2);
            String ngayNK = r.getString(3);
            int tongTien = r.getInt(4);
            ModelPNK data = new ModelPNK(idNK, idNV, ngayNK, tongTien);
            list.add(data);
        }
        p.close();
        r.close();
        return list;
    }

    //Lấy tổng chi phí Nhập kho trong ngày/tháng/năm
    public int getCostNK(String filter) throws SQLException {
        int revenue = 0;

        String sql = "SELECT SUM(Tongtien) FROM PhieuNK WHERE DATE(NgayNK) = CURDATE()";
        if (filter.equals("Hôm nay")) {
            sql = "SELECT SUM(Tongtien) FROM PhieuNK WHERE DATE(NgayNK) = CURDATE()";
        } else if (filter.equals("Tháng này")) {
            sql = "SELECT SUM(Tongtien) FROM PhieuNK WHERE MONTH(NgayNK) = MONTH(CURDATE()) "
                    + "AND YEAR(NgayNK) = YEAR(CURDATE())";
        } else if (filter.equals("Năm này")) {
            sql = "SELECT SUM(Tongtien) FROM PhieuNK WHERE YEAR(NgayNK) = YEAR(CURDATE())";
        }

        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            revenue = r.getInt(1);
        }
        p.close();
        r.close();
        return revenue;
    }

    //Lấy tổng chi phí Nhập Kho của tháng trước
    public int getPreMonthCostNK() throws SQLException {
        int Pre_Cost = 0;
        String sql = "SELECT SUM(Tongtien) FROM PhieuNK WHERE MONTH(NgayNK) = (MONTH(CURDATE()) - 1) "
                + "AND YEAR(NgayNK) = YEAR(CURDATE())";

        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            Pre_Cost = r.getInt(1);
        }
        p.close();
        r.close();
        return Pre_Cost;
    }

    //Lấy toàn bộ doanh thu, chi phí, lợi nhuận của từng tháng trong năm
    public ArrayList<ModelChart> getRevenueCostProfit_byMonth() throws SQLException {
        ArrayList<ModelChart> list = new ArrayList<>();
        String sql_Revenue = "SELECT MONTH(NgayHD) as Thang, SUM(TONGTIEN) FROM HoaDon WHERE YEAR(NgayHD) = YEAR(CURDATE()) "
                + "GROUP BY MONTH(NgayHD) ORDER BY Thang";

        String sql_Cost = "SELECT MONTH(NgayNK) as Thang, SUM(TONGTIEN) FROM PhieuNK WHERE YEAR(NgayNK) = YEAR(CURDATE()) "
                + "GROUP BY MONTH(NgayNK) ORDER BY Thang";

        PreparedStatement p_R = con.prepareStatement(sql_Revenue);
        PreparedStatement p_C = con.prepareStatement(sql_Cost);
        ResultSet r_R = p_R.executeQuery();
        ResultSet r_C = p_C.executeQuery();
        while (r_R.next() && r_C.next()) {
            int revenue = r_R.getInt(2);
            int expenses = r_C.getInt(2);
            int profit = revenue - expenses;
            ModelChart data = new ModelChart("Tháng " + r_R.getInt(1), new double[]{revenue, expenses, profit});
            list.add(data);
        }
        return list;
    }

    //Lấy toàn bộ danh sách Món ăn theo loại Món Ăn
    public ArrayList<ModelMonAn> getMenuFood() throws SQLException {
        ArrayList<ModelMonAn> list = new ArrayList<>();
        String sql = "SELECT ID_MonAn, TenMon, DonGia, Loai, TrangThai FROM MonAn";
        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id = r.getInt("ID_MonAn");
            String name = r.getString("TenMon");
            int value = r.getInt("DonGia");
            String type = r.getString("Loai");
            String state = r.getString("TrangThai");
            ModelMonAn data;
            if (id < 90) {
                data = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/" + type + "/" + id + ".jpg")), id, name, value, type, state);
            } else {
                data = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")), id, name, value, type, state);
            }
            list.add(data);
        }
        r.close();
        p.close();
        return list;
    }

    //Lấy số lượng món ăn đang kinh doanh
    public int getNumberFood_inBusiness() throws SQLException {
        int number = 0;
        String sql = "SELECT COUNT(*) FROM MonAn WHERE TrangThai = 'Dang kinh doanh'";
        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            number = r.getInt(1);
        }
        return number;
    }

    //Thêm mới một Món Ăn
    public void insertMA(ModelMonAn data) throws SQLException {
        String sql = "INSERT INTO MonAn(ID_MonAn, TenMon, Dongia, Loai, TrangThai) VALUES (?, ?, ?, ?, 'Dang kinh doanh')";

        PreparedStatement p = con.prepareStatement(sql);
        p.setInt(1, data.getId());
        p.setString(2, data.getTitle());
        p.setInt(3, data.getValue());
        p.setString(4, data.getType());
        p.execute();
        p.close();
    }

    //Cập nhật thông tin của một Món ăn
    public void UpdateMonAn(ModelMonAn data) throws SQLException {
        String sql = "UPDATE MonAn SET TenMon=?, Dongia=?, Loai=? WHERE ID_MonAn=?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, data.getTitle());
        p.setInt(2, data.getValue());
        p.setString(3, data.getType());
        p.setInt(4, data.getId());
        p.execute();
        p.close();
    }

    //Ngưng kinh doanh một món ăn (Cập nhật TrangThai='Ngung kinh doanh')
    public void StopSell(int idMA) throws SQLException {
        String sql = "UPDATE MonAn SET TrangThai = 'Ngung kinh doanh' WHERE ID_MonAn=?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setInt(1, idMA);
        p.execute();
        p.close();
    }

    //Kinh doanh trở lại một món ăn (Cập nhật TrangThai='Dang kinh doanh')
    public void BackSell(int idMA) throws SQLException {
        String sql = "UPDATE MonAn SET TrangThai = 'Dang kinh doanh' WHERE ID_MonAn=?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setInt(1, idMA);
        p.execute();
        p.close();
    }

    public int getNextID_MA() throws SQLException {
        int id = 0;
        String sql = "SELECT IFNULL(MIN(ID_MonAn) + 1, 1) FROM MonAn WHERE ID_MonAn + 1 NOT IN (SELECT ID_MonAn FROM MonAn)";
        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            id = r.getInt(1);
        }
        return id;
    }
    //Lấy toàn bộ danh sách Khách Hàng
    public ArrayList<ModelKhachHang> MenuKH() throws SQLException {
        ArrayList<ModelKhachHang> list = new ArrayList<>();
        String sql = "SELECT ID_KH, TenKH, DATE_FORMAT(Ngaythamgia, '%d-%m-%Y') AS Ngay, Doanhso, Diemtichluy FROM KhachHang";

        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int ID_KH = r.getInt(1);
            String name = r.getString(2);
            String dateJoin = r.getString(3);
            int sales = r.getInt(4);
            int points = r.getInt(5);
            ModelKhachHang data = new ModelKhachHang(ID_KH, name, dateJoin, sales, points);
            list.add(data);
        }
        r.close();
        p.close();
        return list;
    }

    //Lấy tên khách hàng từ Mã KH
    public String getTenKH(int idKH) throws SQLException {
        String name = "";
        String sql = "SELECT TenKH From KhachHang WHERE ID_KH=?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setInt(1, idKH);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            name = r.getString(1);
        }
        p.close();
        r.close();
        return name;
    }
    
    //Lấy tổng tiền nhập trong ngày hiện tại
    public int getTongtienNK() throws SQLException {
        int tongtien = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYYY");
        String sql = "SELECT SUM(Tongtien) FROM PhieuNK WHERE NgayNK = STR_TO_DATE(?, '%d-%m-%Y')";

        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, simpleDateFormat.format(new Date()));
        ResultSet r = p.executeQuery();
        while (r.next()) {
            tongtien = r.getInt(1);
        }
        r.close();
        p.close();
        return tongtien;
    }
    
}
