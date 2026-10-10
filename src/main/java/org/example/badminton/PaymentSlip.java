package org.example.badminton;

// สลิปการชำระเงินที่แนบมากับการจอง (เป็นส่วนหนึ่งของ Booking แบบ Composition)
public class PaymentSlip {
    private String slipNo;
    private String fileName;
    private double amount;

    public PaymentSlip(String slipNo, String fileName, double amount) {
        this.slipNo = slipNo;
        this.fileName = fileName;
        this.amount = amount;
    }

    public String getSlipNo() { return slipNo; }
    public String getFileName() { return fileName; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return "สลิป " + slipNo + " (" + fileName + ") " + String.format("%,.2f", amount) + " บาท";
    }
}
