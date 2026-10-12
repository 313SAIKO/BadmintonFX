package org.example.badminton;

import java.util.ArrayList;

public class Member extends Person {
    public static final int DISCOUNT_EVERY = 20;    // ชั่วโมงที่ 20, 40, 60, ...
    public static final int DISCOUNT_PERCENT = 30;  // คิดค่าสนามเพียง 30%

    private int paidHours;                // ชั่วโมงที่จ่ายราคาปกติสะสม (ไม่นับชั่วโมงแพ็กเกจ)
    private HourPackage hourPackage;      // Association 0..1
    private ArrayList<Booking> bookings;  // Association 1 -- 0..* (ประวัติการจอง)

    public Member(String id, String name, String phone) {
        super(id, name, phone);
        paidHours = 0;
        hourPackage = null;
        bookings = new ArrayList<Booking>();
    }

    public int getPaidHours() { return paidHours; }
    public HourPackage getHourPackage() { return hourPackage; }
    public void setHourPackage(HourPackage hourPackage) { this.hourPackage = hourPackage; }
    public ArrayList<Booking> getBookings() { return bookings; }

    public void addBooking(Booking b) {
        bookings.add(b);
    }

    public int getPackageHoursLeft() {
        if (hourPackage == null)
            return 0;
        return hourPackage.getRemainingHours();
    }

    // จำนวนชั่วโมงที่จะหักจากแพ็กเกจ ถ้าจอง hours ชั่วโมง
    public int countPackageHours(int hours) {
        return Math.min(hours, getPackageHoursLeft());
    }

    // จำนวนชั่วโมงที่จะได้ส่วนลด (ชั่วโมงสะสมที่ 20, 40, 60, ...)
    public int countDiscountHours(int hours) {
        return countDiscountHours(paidHours, hours - countPackageHours(hours));
    }

    // ถ้าสะสมไว้แล้ว paidBefore ชั่วโมง แล้วจ่ายราคาปกติเพิ่ม normalHours ชั่วโมง จะได้ส่วนลดกี่ชั่วโมง
    private int countDiscountHours(int paidBefore, int normalHours) {
        int count = 0;
        for (int i = 1; i <= normalHours; i++) {
            if ((paidBefore + i) % DISCOUNT_EVERY == 0)
                count++;
        }
        return count;
    }

    private double courtFee(int normalHours, int discountHours, double pricePerHour) {
        return (normalHours - discountHours) * pricePerHour
                + discountHours * pricePerHour * DISCOUNT_PERCENT / 100;
    }

    // คำนวณค่าสนาม (ยังไม่หักชั่วโมงจริง ใช้แสดงยอดก่อนยืนยัน)
    public double calculateCourtFee(int hours, double pricePerHour) {
        int normalHours = hours - countPackageHours(hours);
        return courtFee(normalHours, countDiscountHours(paidHours, normalHours), pricePerHour);
    }

    // คิดราคาการจองหลายรายการต่อกันตามลำดับ (ยังไม่หักชั่วโมงจริง ใช้แสดงยอดก่อนยืนยัน)
    // รายการหลังใช้ชั่วโมงแพ็กเกจที่เหลือ และนับชั่วโมงสะสมต่อจากรายการก่อนหน้า
    public double priceItems(ArrayList<BookingItem> items, double pricePerHour) {
        int packageLeft = getPackageHoursLeft();
        int paid = paidHours;
        double total = 0;
        for (BookingItem item : items) {
            int fromPackage = Math.min(item.getHours(), packageLeft);
            int normalHours = item.getHours() - fromPackage;
            int discountHours = countDiscountHours(paid, normalHours);
            double fee = courtFee(normalHours, discountHours, pricePerHour);
            item.setPrice(fromPackage, discountHours, fee);
            packageLeft -= fromPackage;
            paid += normalHours;
            total += fee;
        }
        return total;
    }

    // หักชั่วโมงแพ็กเกจและสะสมชั่วโมง หลังจองสำเร็จ
    public void useHours(int hours) {
        int fromPackage = countPackageHours(hours);
        if (fromPackage > 0)
            hourPackage.useHours(fromPackage);
        paidHours += hours - fromPackage;
    }

    public int getTotalBookedHours() {
        int total = 0;
        for (Booking b : bookings)
            total += b.getHours();
        return total;
    }

    @Override
    public String getRole() {
        return "สมาชิก";
    }

    @Override
    public String toString() {
        return super.toString() + " | ชั่วโมงสะสม " + paidHours
                + " ชม. | แพ็กเกจเหลือ " + getPackageHoursLeft() + " ชม.";
    }
}
