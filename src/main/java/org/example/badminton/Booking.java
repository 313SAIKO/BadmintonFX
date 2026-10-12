package org.example.badminton;

import java.time.LocalDate;
import java.time.LocalDateTime;

// การจองสนาม 1 ครั้ง
public class Booking implements Chargeable {
    private String bookingId;
    private Member member;      // Association: Booking --> Member
    private Court court;        // Association: Booking --> Court
    private Cashier cashier;    // Association: แคชเชียร์ผู้บันทึกการจอง
    private LocalDate date;
    private int startHour;
    private int hours;
    private LocalDateTime bookedAt;  // เวลาที่ทำรายการจอง
    private boolean lateStart;  // ชั่วโมงแรกเริ่มไปแล้วตอนจอง คิดครึ่งราคา และไม่นับชั่วโมงสะสม
    private int packageHours;   // ชั่วโมงที่หักจากแพ็กเกจ
    private int discountHours;  // ชั่วโมงที่ได้ส่วนลดเหลือ 30%
    private double courtFee;
    private PaymentSlip slip;   // Composition: สร้างภายใน constructor

    // request คือคำขอจองที่คิดราคาแล้ว (BadmintonCenter.priceBookings) ราคาที่บันทึกจึงตรงกับที่แสดงก่อนยืนยัน
    public Booking(String bookingId, Member member, Court court, Cashier cashier, BookingItem request,
                   String slipNo, String slipFile, LocalDateTime bookedAt) {
        this.bookingId = bookingId;
        this.member = member;
        this.court = court;
        this.cashier = cashier;
        this.date = request.getDate();
        this.startHour = request.getStartHour();
        this.hours = request.getHours();
        this.bookedAt = bookedAt;
        lateStart = request.isLateStart();
        packageHours = request.getPackageHours();
        discountHours = request.getDiscountHours();
        courtFee = request.getFee();
        if (courtFee > 0)
            slip = new PaymentSlip(slipNo, slipFile, courtFee);
        else
            slip = null;  // ใช้ชั่วโมงแพ็กเกจทั้งหมด ไม่มียอดต้องชำระ
    }

    public String getBookingId() { return bookingId; }
    public Member getMember() { return member; }
    public Court getCourt() { return court; }
    public Cashier getCashier() { return cashier; }
    public int getStartHour() { return startHour; }
    public int getHours() { return hours; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public boolean isLateStart() { return lateStart; }
    public int getPackageHours() { return packageHours; }
    public int getDiscountHours() { return discountHours; }
    public PaymentSlip getSlip() { return slip; }

    public int getEndHour() {
        return startHour + hours;
    }

    public String getTimeText() {
        return String.format("%02d:00-%02d:00", startHour, getEndHour());
    }

    // ตรวจว่าช่วงเวลาที่ขอจองทับกับการจองนี้หรือไม่
    public boolean overlaps(int courtNo, LocalDate date, int startHour, int hours) {
        return court.getCourtNo() == courtNo && this.date.equals(date)
                && startHour < getEndHour() && this.startHour < startHour + hours;
    }

    @Override
    public double getTotal() {
        return courtFee;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    @Override
    public String getCategory() {
        return Chargeable.COURT_FEE;
    }

    @Override
    public String toString() {
        String detail = "";
        if (lateStart)
            detail += " ชม.แรกเริ่มไปแล้ว ครึ่งราคา";
        if (packageHours > 0)
            detail += " ใช้แพ็กเกจ " + packageHours + " ชม.";
        if (discountHours > 0)
            detail += " ลด 30% " + discountHours + " ชม.";
        String payment;
        if (slip == null)
            payment = "ชำระด้วยแพ็กเกจ";
        else
            payment = slip.toString();
        return "[" + bookingId + "] " + date + " " + getTimeText() + " " + court + " | " + member.getId() + " " + member.getName()
                + " | ค่าสนาม " + String.format("%,.2f", courtFee) + " บาท" + detail
                + " | " + payment + " | บันทึกโดย " + cashier.getId();
    }
}
