package repository;

import model.Product;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// 데이터베이스에 저장된 상품 정보들을 객체로 변환하는 클래스
public class ProductRepository {

    // DB에 있는 모든 상품을 가져와서 리스트로 반환
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM product"; // SQL 쿼리 준비

        //DB 연결 및 실행
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                String category = rs.getString("category"); // 카테고리 정보 추출. 포스기 화면에서 탭 분류 위함
                products.add(new Product(name, price, category)); // 데이터에서 추출된 정보들을 모아 하나의 객체로 만들어 리스트에 추가
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 완성된 상품 리스트 반환
        return products;
    }
}