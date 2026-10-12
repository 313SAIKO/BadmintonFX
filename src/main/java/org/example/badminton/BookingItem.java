package org.example.badminton;

import java.time.LocalDate;

// คำขอจอง 1 ช่วง (สนาม + วันที่ + เวลา) ใช้จองหลายสนามหรือหลายวันในครั้งเดียว
// ยังไม่ใช่การจองจริงจนกว่าแคชเชียร์จะยืนยัน
public class BookingItem {
    private int courtNo;
    private LocalDate date;
    private int startHour;
    private int hours;
    private int packageHours;   // ผลจาก Member.priceItems() ใช้แสดงราคาก่อนยืนยัน
    private int discountHours;
    private double fee;

    public BookingItem(int courtNo, LocalDate date, int startHour, int hours) {
        this.courtNo = courtNo;
        this.date = date;
        this.startHour = startHour;
        this.hours = hours;
    }

    public int getCourtNo() { return courtNo; }
    public LocalDate getDate() { return date; }
    public int getStartHour() { return startHour; }
    public int getHours() { return hours; }
    public int getPackageHours() { return packageHours; }
    public int getDiscountHours() { return discountHours; }
    public double getFee() { return fee; }

    public int getEndHour() {
        return startHour + hours;
    }

    public String getTimeText() {
        return String.format("%02d:00-%02d:00", startHour, getEndHour());
    }

    public void setPrice(int packageHours, int discountHours, double fee) {
        this.packageHours = packageHours;
        this.discountHours = discountHours;
        this.fee = fee;
    }

    // ทับกับคำขออื่นในการจองครั้งเดียวกันหรือไม่
    public boolean overlaps(BookingItem other) {
        return courtNo == other.courtNo && date.equals(other.date)
                && startHour < other.getEndHour() && other.startHour < getEndHour();
    }

    @Override
    public String toString() {
        return date + " สนาม " + courtNo + " " + getTimeText();
    }
}
