package org.example.badminton;

import java.time.LocalDate;

// abstract class: ต้นแบบของอุปกรณ์ ทั้งของให้เช่าและของขาย
public abstract class Item {
    private String code;
    private String name;
    private double price;

    public Item(String code, String name, double price) {
        this.code = code;
        this.name = name;
        this.price = price;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double calculatePrice(int quantity) {
        return price * quantity;
    }

    // คลาสลูกต้องกำหนดเอง เพราะของเช่ากับของขายทำงานต่างกัน
    // ของเช่า: นับจำนวนที่ว่างแยกตามวัน (คืนภายในวัน) / ของขาย: สต็อกลดลงถาวร ไม่ขึ้นกับวัน
    public abstract String getUnit();
    public abstract String getCategory();
    public abstract boolean isAvailable(int quantity, LocalDate date);
    public abstract void provide(int quantity, LocalDate date);
    public abstract String getStockInfo(LocalDate date);

    @Override
    public String toString() {
        return code + " " + name + " " + String.format("%,.2f", price) + " บาท/" + getUnit();
    }
}
