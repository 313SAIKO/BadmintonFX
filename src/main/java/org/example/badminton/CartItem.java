package org.example.badminton;

// 1 รายการในตะกร้าอุปกรณ์ (ยังไม่เป็นรายได้จนกว่าแคชเชียร์จะยืนยันรายการ)
public class CartItem {
    private Item item;      // Association: CartItem --> Item
    private int quantity;

    public CartItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem() { return item; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubtotal() {
        return item.calculatePrice(quantity);
    }

    @Override
    public String toString() {
        return item.getName() + " x " + quantity + " " + item.getUnit() + " = "
                + String.format("%,.2f", getSubtotal()) + " บาท";
    }
}
