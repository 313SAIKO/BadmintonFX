package org.example.badminton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

// ข้อมูลตั้งต้นที่เทสต์ใช้ร่วมกัน
// ทุกเทสต์ส่งเวลาที่ทำรายการ (now) เข้าไปเอง ผลจึงไม่ขึ้นกับนาฬิกาจริงของเครื่อง
class TestHelper {
    // วันพฤหัสบดี 15 ต.ค. 2569 เป็นวันธรรมดา เปิด 13:00-21:00
    static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    // ทำรายการตอนเที่ยงของวันนั้น ทุกช่วงเวลาที่เปิดยังมาไม่ถึง
    static final LocalDateTime NOON = DAY.atTime(12, 0);

    static ArrayList<BookingItem> request(int courtNo, LocalDate date, int startHour, int hours) {
        ArrayList<BookingItem> requests = new ArrayList<BookingItem>();
        requests.add(new BookingItem(courtNo, date, startHour, hours));
        return requests;
    }

    // จองย้อนหลังให้สมาชิกจนมีชั่วโมงสะสมตามต้องการ (วันละ 1 ชม. ทำรายการก่อนวันเล่น 1 วัน)
    static void addPaidHours(BadmintonCenter center, Cashier cashier, Member m, int hours) {
        LocalDate first = LocalDate.of(2026, 1, 5);
        for (int i = 0; i < hours; i++) {
            LocalDate date = first.plusDays(i);
            LocalDateTime bookedAt = date.minusDays(1).atTime(12, 0);
            ArrayList<BookingItem> requests = request(1, date, 18, 1);
            double fee = center.priceBookings(m, requests, bookedAt);
            if (center.createBookings(cashier, m.getId(), requests, "HIS-" + m.getId() + "-" + i, "his.jpg",
                    fee, bookedAt) == null)
                throw new IllegalStateException("สร้างประวัติการจองไม่สำเร็จ วันที่ " + date);
        }
    }
}
