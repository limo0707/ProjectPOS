package main;

import util.DBConnection;
import view.PosFrame;

public class Main {
    public static void main(String[] args) {
        // DB 연결 및 초기화
        DBConnection.initDatabase();

        // 메인 화면 객체 생성 및 띄우기
        PosFrame frame = new PosFrame();
        frame.setVisible(true);
    }
}