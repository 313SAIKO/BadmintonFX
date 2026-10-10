package org.example.badminton;

import java.time.LocalDate;

// รายการเช่าหรือซื้ออุปกรณ์ 1 รายการ
public class ItemOrder implements Chargeable {
    private String orderId;
    private Item item;          // Association: ItemOrder --> Item
    private int quantity;
    private LocalDate date;
    private String customerName;
    private double total;

    public ItemOrder(String orderId, Item item, int quantity, LocalDate date, String customerName) {
        this.orderId = orderId;
        this.item = item;
        this.quantity = quantity;
        this.date = date;
        this.customerName = customerName;
        total = item.calculatePrice(quantity);  // เก็บยอด ณ วันที่ซื้อ
    }

    public String getOrderId() { return orderId; }
    public Item getItem() { return item; }
    public int getQuantity() { return quantity; }
    public String getCustomerName() { return customerName; }

    @Override
    public double getTotal() {
        return total;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    @Override
    public String getCategory() {
        return item.getCategory();
    }

    @Override
    public String toString() {
        return "[" + orderId + "] " + date + " " + getCategory() + ": " + item.getName()
                + " x " + quantity + " " + item.getUnit() + " = " + String.format("%,.2f", total)
                + " บาท (ลูกค้า " + customerName + ")";
    }
}
