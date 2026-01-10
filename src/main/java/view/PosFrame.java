package view;

import model.Product;
import repository.MemberRepository;
import repository.ProductRepository;
import repository.SalesRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PosFrame extends JFrame {

    private DefaultTableModel tableModel; //장바구니 데이터를 화면에 보여주기 위한 테이블 모델
    private JLabel totalLabel; //총 결제 금액
    private ArrayList<Product> cartList = new ArrayList<>(); //상품 객체들 담아둘 리스트

    private ProductRepository productRepository = new ProductRepository();
    private SalesRepository salesRepository = new SalesRepository();
    private MemberRepository memberRepository = new MemberRepository();

    public PosFrame() {
        setTitle("카페 키오스크 시스템");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 왼쪽 장바구니
        String[] columnNames = {"상품명", "가격", "수량"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(350, 0));
        add(scrollPane, BorderLayout.WEST);

        // 중앙 메뉴
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        // DB에서 모든 상품 가져오기
        List<Product> allProducts = productRepository.getAllProducts();

        // 탭 생성 - 커피, 라떼, 음료, 디저트
        tabbedPane.addTab("커피", createMenuPanel(allProducts, "커피"));
        tabbedPane.addTab("라떼", createMenuPanel(allProducts, "라떼"));
        tabbedPane.addTab("음료", createMenuPanel(allProducts, "음료"));
        tabbedPane.addTab("디저트", createMenuPanel(allProducts, "디저트"));

        add(tabbedPane, BorderLayout.CENTER);

        // 하단 패널
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(0, 100));

        totalLabel = new JLabel("총 결제금액: 0원", SwingConstants.CENTER);
        totalLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        bottomPanel.add(totalLabel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));

        // 매출 확인 버튼
        JButton adminButton = new JButton("매출 확인");
        adminButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        adminButton.setBackground(Color.LIGHT_GRAY);
        adminButton.addActionListener(e -> showSalesReport());
        btnPanel.add(adminButton);

        // 결제하기 버튼
        JButton payButton = new JButton("결제하기");
        payButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        payButton.setBackground(new Color(255, 100, 100));
        payButton.setForeground(Color.WHITE);
        payButton.addActionListener(e -> processPayment());
        btnPanel.add(payButton);

        bottomPanel.add(btnPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // 카테고리별 패널
    private JPanel createMenuPanel(List<Product> allProducts, String category) {
        JPanel panel = new JPanel(new GridLayout(3, 3, 10, 10));
        panel.setBackground(Color.WHITE);

        // 해당 카테고리 상품만 필터링
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.getCategory().equals(category))
                .collect(Collectors.toList());

        for (Product p : filtered) {
            String buttonText = "<html><center><font size='5'>" + p.getName() + "</font><br>"
                    + "<font color='gray'>" + p.getPrice() + "원</font></center></html>";
            //버튼 클릭 시 장바구니 담기 메서드 호출
            JButton btn = new JButton(buttonText);
            btn.setBackground(new Color(204, 255, 255));
            btn.addActionListener(e -> addToCart(p));
            panel.add(btn);
        }
        return panel;
    }

    // 장바구나 담기
    private void addToCart(Product product) {
        // 1. 계산용 리스트에 추가
        cartList.add(product);

        // 2. 중복 체크 후 화면용 테이블 업데이트
        boolean exists = false;
        // 테이블을 순회하며 이미 있는 상품인지 확인
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            // 이미 있다면 수량(2번 컬럼)만 +1 증가
            if (tableModel.getValueAt(i, 0).equals(product.getName())) {
                int currentQty = (int) tableModel.getValueAt(i, 2);
                tableModel.setValueAt(currentQty + 1, i, 2);
                exists = true;
                break;
            }
        }
        // 없다면 새로운 행 추가
        if (!exists) tableModel.addRow(new Object[]{product.getName(), product.getPrice(), 1});

        // 3. 금액 갱신
        updateTotal();
    }

    // 리스트 내 상품 가격 총합 계산
    private int calculateTotal() {
        return cartList.stream().mapToInt(Product::getPrice).sum();
    }

    private void updateTotal() {
        totalLabel.setText("총 결제금액 : " + calculateTotal() + "원");
    }

    // 결제 프로세스(결제 수단 -> 포인트 적립 -> 완료)
    private void processPayment() {
        if (cartList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "장바구니가 비어있습니다");
            return;
        }
        int totalAmount = calculateTotal();

        // 1. 결제 수단 선택
        Object[] options = {"신용카드", "현금", "간편결제(Pay)"};
        int choice = JOptionPane.showOptionDialog(null,
                "결제 수단을 선택해주세요.", "결제 수단",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == -1) return;
        String paymentMethod = (String) options[choice];

        // 2. 포인트 적립 여부 확인
        int pointChoice = JOptionPane.showConfirmDialog(null,
                "포인트를 적립하시겠습니까?", "포인트 적립", JOptionPane.YES_NO_OPTION);

        int currentPoint = 0;
        boolean savedPoint = false;
        String phone = "";

        // "예"를 눌렀을 때만 전화번호 입력 로직 실행
        if (pointChoice == JOptionPane.YES_OPTION) {

            // 숫자 입력 유효성 검사 무한 반복
            while (true) {
                phone = JOptionPane.showInputDialog("휴대폰 번호를 입력하세요(예: 01000000000)");

                // 취소 버튼을 눌렀으면 적립 포기하고 결제 진행
                if (phone == null) {
                    break;
                }

                phone = phone.trim(); // 앞뒤 공백 제거

                // 아무것도 안 적었으면 다시
                if (phone.isEmpty()) {
                    continue;
                }

                // 숫자로만 이루어져 있는지 검사
                if (phone.matches("^[0-9]+$")) {
                    int pointToAdd = (int) (totalAmount * 0.05); // 5% 적립
                    currentPoint = memberRepository.addPoint(phone, pointToAdd);
                    savedPoint = true;
                    break;
                } else {
                    JOptionPane.showMessageDialog(null, "잘못된 형식입니다. 숫자만 입력 가능합니다.");
                }
            }
        }

        // DB 저장
        StringBuilder itemsBuilder = new StringBuilder();
        for (Product p : cartList) itemsBuilder.append(p.getName()).append(", ");

        salesRepository.saveSale(itemsBuilder.toString(), totalAmount, paymentMethod);

        // 결제 결과 출력
        StringBuilder msg = new StringBuilder();
        msg.append("결제가 완료되었습니다\n");
        msg.append("결제수단: ").append(paymentMethod).append("\n");
        msg.append("총 금액: ").append(totalAmount).append("원\n");

        if (savedPoint) {
            msg.append("--------------------\n");
            msg.append("포인트 적립 완료\n");
            msg.append("전화번호: ").append(phone).append("\n");
            msg.append("누적: ").append(currentPoint).append("P");
        }

        JOptionPane.showMessageDialog(null, msg.toString());

        // 결제 완료 후 초기화
        cartList.clear();
        tableModel.setRowCount(0);
        updateTotal();
    }

    // 매출 리포트
    private void showSalesReport() {
        String history = salesRepository.getSalesHistory();
        int totalSales = salesRepository.getTotalSales();

        JTextArea textArea = new JTextArea(history);
        textArea.setEditable(false); // 수정 불가능
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(600, 300));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("상세 매출 리포트"), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(new JLabel("총 매출 합계: " + totalSales + "원", SwingConstants.CENTER), BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(null, panel, "매출 리포트", JOptionPane.PLAIN_MESSAGE);
    }
}