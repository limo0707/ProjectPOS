package repository;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 매출 기록을 저장하고 조회하는 클래스
public class SalesRepository {

    // 결제 완료 시 호출되어 판매 내역을 DB에 저장
    public void saveSale(String items, int totalPrice, String paymentMethod) {
        String sql = "INSERT INTO sales (items, total_price, payment_method) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, items);
            pstmt.setInt(2, totalPrice);
            pstmt.setString(3, paymentMethod);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 관리자용 상세 매출 리포트 생성
    public String getSalesHistory() {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT * FROM sales ORDER BY order_time DESC"; // 내림차순 정렬(최신 내역이 위로 오도록)
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // 한 줄씩 포맷팅하여 StringBuilder에 추가
                sb.append("[").append(rs.getTimestamp("order_time")).append("] ")
                        .append(rs.getString("payment_method")).append(" | ")
                        .append(rs.getString("items")).append(" : ")
                        .append(rs.getInt("total_price")).append("원\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (sb.length() == 0)
            return "판매 내역이 없습니다.";

        // 완성된 전체 문자열 반환
        return sb.toString();
    }

    // 총 매출 합계 계산
    public int getTotalSales() {
        String sql = "SELECT SUM(total_price) FROM sales";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}