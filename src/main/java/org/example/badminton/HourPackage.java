package org.example.badminton;

import java.time.LocalDate;

// แพ็กเกจเหมาจ่าย 15 ชั่วโมง ราคา 2,600 บาท
public class HourPackage implements Chargeable {
    public static final int TOTAL_HOURS = 15;
    public static final double PRICE = 2600.0;

    private String packageId;
    private Member member;
    private LocalDate purchaseDate;
    private int remainingHours;

    public HourPackage(String packageId, Member member, LocalDate purchaseDate) {
        this.packageId = packageId;
        this.member = member;
        this.purchaseDate = purchaseDate;
        remainingHours = TOTAL_HOURS;
    }

    public String getPackageId() { return packageId; }
    public Member getMember() { return member; }
    public int getRemainingHours() { return remainingHours; }

    public void useHours(int hours) {
        if (hours > 0 && hours <= remainingHours)
            remainingHours -= hours;
    }

    @Override
    public double getTotal() {
        return PRICE;
    }

    @Override
    public LocalDate getDate() {
        return purchaseDate;
    }

    @Override
    public String getCategory() {
        return Chargeable.PACKAGE;
    }

    @Override
    public String toString() {
        return "[" + packageId + "] " + purchaseDate + " แพ็กเกจ " + TOTAL_HOURS + " ชม. ของ "
                + member.getId() + " " + member.getName() + " | " + String.format("%,.2f", PRICE)
                + " บาท | เหลือ " + remainingHours + " ชม.";
    }
}
