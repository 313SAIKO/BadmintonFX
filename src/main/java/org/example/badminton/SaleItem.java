package org.example.badminton;

import java.time.LocalDate;

// ของขาย (ไม้แบดมินตัน, ลูกขนไก่, เทปพันด้าม) ขายแล้วตัดสต็อก
public class SaleItem extends Item {
    private int stock;

    public SaleItem(String code, String name, double price, int stock) {
        super(code, name, price);
        this.stock = stock;
    }

    public int getStock() { return stock; }

    @Override
    public void addStock(int quantity) {
        if (quantity > 0)
            stock += quantity;
    }

    @Override
    public String getUnit() {
        return "ชิ้น";
    }

    @Override
    public String getCategory() {
        return Chargeable.SALE;
    }

    // ของขายไม่ขึ้นกับวันที่ ขายแล้วสต็อกลดลงถาวร
    @Override
    public boolean isAvailable(int quantity, LocalDate date) {
        return quantity > 0 && quantity <= stock;
    }

    @Override
    public void provide(int quantity, LocalDate date) {
        stock -= quantity;
    }

    // ของขายส่งมอบทันที จึงบันทึกรายได้เป็นวันที่ขาย ถึงตะกร้าจะเลือกวันที่ล่วงหน้าไว้สำหรับของเช่า
    @Override
    public LocalDate getRevenueDate(LocalDate useDate, LocalDate today) {
        return today;
    }

    @Override
    public String getStockInfo(LocalDate date) {
        if (stock <= 0)
            return "หมดแล้ว";
        return "คงเหลือ " + stock + " ชิ้น";
    }
}
