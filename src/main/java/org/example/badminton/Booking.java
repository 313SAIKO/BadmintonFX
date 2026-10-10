package org.example.badminton;

import java.time.LocalDate;

// การจองสนาม 1 ครั้ง
public class Booking implements Chargeable {
    private String bookingId;
    private Member member;      // Association: Booking --> Member
    private Court court;        // Association: Booking --> Court
    private Cashier cashier;    // Association: แคชเชียร์ผู้บันทึกการจอง
    private LocalDate date;
    private int startHour;
    private int hours;
    private int packageHours;   // ชั่วโมงที่หักจากแพ็กเกจ
    private int discountHours;  // ชั่วโมงที่ได้ส่วนลดเหลือ 30%
    private double courtFee;
    private PaymentSlip slip;   // Composition: สร้างภายใน constructor

    public Booking(String bookingId, Member member, Court court, Cashier cashier,
                   LocalDate date, int startHour, int hours, String slipNo, String slipFile) {
        this.bookingId = bookingId;
        this.member = member;
        this.court = court;
        this.cashier = cashier;
        this.date = date;
        this.startHour = startHour;
        this.hours = hours;
        packageHours = member.countPackageHours(hours);
        discountHours = member.countDiscountHours(hours);
        courtFee = member.calculateCourtFee(hours, court.getPricePerHour());
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
