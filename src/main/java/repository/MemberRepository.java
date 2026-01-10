package repository;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 회원 테이블 DB 전담 클래스
public class MemberRepository {
    // 포인트 적립, 현재 포인트 반환
    public int addPoint(String phone, int amount) {
        // 신규 회원인지 기존 회원인지 확인. DB에 회원 정보가 없을 경우 신규 회원 등록
        if (!isMemberExist(phone)) createMember(phone);

        // 기존 포인트 + 새 포인트
        String sql = "UPDATE member SET point = point + ? WHERE phone = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, phone);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getPoint(phone);
    }

    // DB에 해당 번호의 회원이 있는지 확인
    private boolean isMemberExist(String phone) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT count(*) FROM member WHERE phone = ?")) {
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 신규 회원 생성
    private void createMember(String phone) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO member (phone, point) VALUES (?, 0)")) { // 초기 포인트 = 0
            pstmt.setString(1, phone);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 현재 포인트 조회
    private int getPoint(String phone) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT point FROM member WHERE phone = ?")) {
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("point");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}