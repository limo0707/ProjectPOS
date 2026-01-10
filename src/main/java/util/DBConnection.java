package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    // 파일 저장
    private static final String URL = "jdbc:h2:./pos_db;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    // Connection 객체 생성 - DB와 통신
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // DB 초기화 - 프로그램이 켜질 때 한 번 실행되어 테이블을 만들고 기초 데이터를 넣음
    public static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 상품 테이블 생성
            stmt.execute("CREATE TABLE IF NOT EXISTS product (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " + // id 증가
                    "name VARCHAR(255) NOT NULL, " +
                    "price INT NOT NULL, " +
                    "category VARCHAR(50) NOT NULL)");

            // 매출 테이블 생성
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "items VARCHAR(1000), " +
                    "total_price INT, " +
                    "payment_method VARCHAR(20), " + // 결제 수단
                    "order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"); // 현재 시간

            // 회원 테이블 생성
            stmt.execute("CREATE TABLE IF NOT EXISTS member (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "phone VARCHAR(20) NOT NULL UNIQUE, " + // 중복 가입 방지
                    "point INT DEFAULT 0)");

            // 테이블이 비어있을 때만 메뉴 데이터 넣음
            if (isProductTableEmpty(stmt)) {
                addSampleData(stmt);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 상품 테이블이 비어있는지 확인
    private static boolean isProductTableEmpty(Statement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM product")) {
            if (rs.next()) return rs.getInt(1) == 0;
        }
        return true;
    }

    // 메뉴
    private static void addSampleData(Statement stmt) throws SQLException {
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('아메리카노', 2000, '커피')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('빅 아메리카노', 2500, '커피')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('에스프레소', 2500, '커피')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('콜드브루', 3000, '커피')");

        stmt.execute("INSERT INTO product (name, price, category) VALUES ('카페라떼', 3000, '라떼')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('카페라떼', 3000, '라떼')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('논커피 카페라떼', 3500, '라떼')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('바닐라라떼', 3500, '라떼')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('연유라떼', 3500, '라떼')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('딸기라떼', 3500, '라떼')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('고구마라떼', 3500, '라떼')");

        stmt.execute("INSERT INTO product (name, price, category) VALUES ('아이스티', 2500, '음료')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('레몬에이드', 3000, '음료')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('딸기스무디', 4000, '음료')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('바나나스무디', 4000, '음료')");

        stmt.execute("INSERT INTO product (name, price, category) VALUES ('치즈케이크', 5000, '디저트')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('초코케이크', 5000, '디저트')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('마카롱', 2000, '디저트')");
        stmt.execute("INSERT INTO product (name, price, category) VALUES ('두바이쫀득쿠키', 5500, '디저트')");
    }
}