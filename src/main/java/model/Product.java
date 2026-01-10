package model;

public class Product {
    private String name;
    private int price;
    private String category; // 메뉴 카테고리 분류

    public Product(String name, int price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getCategory() { return category; }
}