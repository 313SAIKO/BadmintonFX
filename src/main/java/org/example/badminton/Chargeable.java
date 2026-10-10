package org.example.badminton;

import java.time.LocalDate;

// interface ของทุกรายการที่ทำให้ศูนย์มีรายได้
// Booking, HourPackage และ ItemOrder ต่าง implements แล้วคิดยอดเงินในแบบของตัวเอง
public interface Chargeable {
    // ค่าคงที่ใน interface (public static final) ใช้แยกหมวดรายได้
    public static final String COURT_FEE = "ค่าเช่าสนาม";
    public static final String PACKAGE = "แพ็กเกจเหมาจ่าย";
    public static final String RENTAL = "ค่าเช่าอุปกรณ์";
    public static final String SALE = "ขายอุปกรณ์";

    public double getTotal();
    public LocalDate getDate();
    public String getCategory();
}
