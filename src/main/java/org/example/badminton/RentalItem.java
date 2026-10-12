package org.example.badminton;

import java.time.LocalDate;
import java.util.HashMap;

// ของให้เช่า (รองเท้า, ไม้แบดมินตัน) ราคาคู่ละ 50 บาท/ครั้ง/วัน
// เช่าแล้วคืนภายในวัน จึงนับจำนวนที่ถูกเช่าแยกตามวัน วันใหม่ก็กลับมาว่างครบ
public class RentalItem extends Item {
    public static final double RENTAL_PRICE = 50.0;

    private int totalPairs;                           // จำนวนคู่ที่ศูนย์มีให้เช่า
    private HashMap<LocalDate, Integer> rentedPairs;  // วันที่ -> จำนวนคู่ที่ถูกเช่าไปแล้วในวันนั้น

    public RentalItem(String code, String name, int totalPairs) {
        super(code, name, RENTAL_PRICE);
        this.totalPairs = totalPairs;
        rentedPairs = new HashMap<LocalDate, Integer>();
    }

    public int getTotalPairs() { return totalPairs; }

    public int getRentedPairs(LocalDate date) {
        return rentedPairs.getOrDefault(date, 0);
    }

    public int getAvailablePairs(LocalDate date) {
        return totalPairs - getRentedPairs(date);
    }

    @Override
    public String getUnit() {
        return "คู่/วัน";
    }

    @Override
    public String getCategory() {
        return Chargeable.RENTAL;
    }

    @Override
    public boolean isAvailable(int quantity, LocalDate date) {
        return quantity > 0 && quantity <= getAvailablePairs(date);
    }

    @Override
    public void provide(int quantity, LocalDate date) {
        rentedPairs.put(date, getRentedPairs(date) + quantity);
    }

    @Override
    public void addStock(int quantity) {
        if (quantity > 0)
            totalPairs += quantity;
    }

    @Override
    public String getStockInfo(LocalDate date) {
        if (getAvailablePairs(date) <= 0)
            return "เต็มแล้ว (เช่าครบ " + totalPairs + " คู่)";
        return "ว่าง " + getAvailablePairs(date) + "/" + totalPairs + " คู่";
    }
}
