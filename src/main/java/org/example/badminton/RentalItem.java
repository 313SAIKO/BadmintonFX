package org.example.badminton;

// ของให้เช่า (รองเท้า, ไม้แบดมินตัน) ราคาคู่ละ 50 บาท/ครั้ง/วัน
public class RentalItem extends Item {
    public static final double RENTAL_PRICE = 50.0;

    private int totalPairs;  // จำนวนคู่ที่ศูนย์มีให้เช่า

    public RentalItem(String code, String name, int totalPairs) {
        super(code, name, RENTAL_PRICE);
        this.totalPairs = totalPairs;
    }

    public int getTotalPairs() { return totalPairs; }

    @Override
    public String getUnit() {
        return "คู่/วัน";
    }

    @Override
    public String getCategory() {
        return Chargeable.RENTAL;
    }

    @Override
    public boolean isAvailable(int quantity) {
        return quantity > 0 && quantity <= totalPairs;
    }

    @Override
    public void provide(int quantity) {
        // ของเช่าคืนภายในวัน จึงไม่ต้องตัดจำนวน
    }

    @Override
    public String getStockInfo() {
        return "มีให้เช่า " + totalPairs + " คู่";
    }
}
