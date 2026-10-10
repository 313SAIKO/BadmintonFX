package org.example.badminton;

// ของขาย (ไม้แบดมินตัน, ลูกขนไก่, เทปพันด้าม) ขายแล้วตัดสต็อก
public class SaleItem extends Item {
    private int stock;

    public SaleItem(String code, String name, double price, int stock) {
        super(code, name, price);
        this.stock = stock;
    }

    public int getStock() { return stock; }

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

    @Override
    public boolean isAvailable(int quantity) {
        return quantity > 0 && quantity <= stock;
    }

    @Override
    public void provide(int quantity) {
        stock -= quantity;
    }

    @Override
    public String getStockInfo() {
        return "คงเหลือ " + stock + " ชิ้น";
    }
}
