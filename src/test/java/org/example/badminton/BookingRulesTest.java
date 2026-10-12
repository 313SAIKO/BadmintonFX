package org.example.badminton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.example.badminton.TestHelper.DAY;
import static org.example.badminton.TestHelper.NOON;
import static org.example.badminton.TestHelper.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("กฎการจองสนาม")
class BookingRulesTest {
    private static final LocalDate SATURDAY = LocalDate.of(2026, 10, 17);

    private BadmintonCenter center;
    private Cashier cashier;
    private Member member;

    @BeforeEach
    void setUp() {
        center = new BadmintonCenter("ทดสอบ");
        cashier = new Cashier("C01", "แคชเชียร์ทดสอบ", "080-000-0000");
        member = cashier.registerMember(center, "สมาชิกทดสอบ", "081-111-1111");
    }

    private String check(int courtNo, LocalDate date, int startHour, int hours) {
        return center.checkBooking(member.getId(), courtNo, date, startHour, hours, NOON.minusDays(7));
    }

    @Test
    @DisplayName("วันธรรมดาเปิด 13:00-21:00")
    void weekdayHours() {
        assertNull(check(1, DAY, 13, 1));
        assertNull(check(1, DAY, 20, 1));
        assertNotNull(check(1, DAY, 12, 1));
        assertNotNull(check(1, DAY, 21, 1));
        assertNotNull(check(1, DAY, 20, 2));
    }

    @Test
    @DisplayName("เสาร์-อาทิตย์เปิด 10:00-20:00")
    void weekendHours() {
        assertNull(check(1, SATURDAY, 10, 1));
        assertNull(check(1, SATURDAY, 19, 1));
        assertNotNull(check(1, SATURDAY, 9, 1));
        assertNotNull(check(1, SATURDAY, 20, 1));
    }

    @Test
    @DisplayName("วันหยุดนักขัตฤกษ์เปิด 10:00-20:00 เหมือนเสาร์-อาทิตย์")
    void publicHolidayHours() {
        center.getOpeningHours().addPublicHoliday(DAY);
        assertNull(check(1, DAY, 10, 1));
        assertNotNull(check(1, DAY, 20, 1));
    }

    @Test
    @DisplayName("ต้องเป็นสมาชิกจึงจะจองได้ และต้องเป็นสนาม 1-12")
    void memberAndCourtRequired() {
        assertNotNull(center.checkBooking("M999", 1, DAY, 18, 1, NOON));
        assertNotNull(check(0, DAY, 18, 1));
        assertNotNull(check(13, DAY, 18, 1));
        assertNull(check(12, DAY, 18, 1));
    }

    @Test
    @DisplayName("จองย้อนหลังไม่ได้ (วันที่ผ่านมาแล้ว และชั่วโมงที่ผ่านไปแล้วของวันนี้)")
    void cannotBookPast() {
        assertNotNull(center.checkBooking(member.getId(), 1, DAY.minusDays(1), 18, 1, NOON));
        assertNotNull(center.checkBooking(member.getId(), 1, DAY, 16, 1, DAY.atTime(17, 10)));
        assertNull(center.checkBooking(member.getId(), 1, DAY, 18, 1, DAY.atTime(17, 10)));
    }

    @ParameterizedTest(name = "ชั่วโมง 17:00 เริ่มไปแล้ว {0} นาที จองไม่ได้")
    @ValueSource(ints = { 31, 45, 59 })
    void currentHourTooLate(int minute) {
        String error = center.checkBooking(member.getId(), 1, DAY, 17, 1, DAY.atTime(17, minute));
        assertNotNull(error);
        assertTrue(error.contains("เกิน 30 นาที"));
    }

    @Test
    @DisplayName("จองทับเวลาที่มีคนจองแล้วไม่ได้ แต่สนามอื่นหรือเวลาต่อกันได้")
    void cannotOverlapExistingBooking() {
        assertNotNull(center.createBookings(cashier, member.getId(), request(1, DAY, 18, 2), "S1", "s.jpg", 360, NOON));
        assertNotNull(check(1, DAY, 19, 2));
        assertNotNull(check(1, DAY, 17, 2));
        assertNull(check(2, DAY, 18, 2));
        assertNull(check(1, DAY, 20, 1));
        assertNull(check(1, DAY, 16, 2));
    }

    @Test
    @DisplayName("รายการในการจองครั้งเดียวกันห้ามทับกันเอง")
    void requestsCannotOverlapEachOther() {
        ArrayList<BookingItem> requests = new ArrayList<BookingItem>();
        requests.add(new BookingItem(2, DAY, 15, 2));
        requests.add(new BookingItem(2, DAY, 16, 1));
        String error = center.checkBookings(member.getId(), requests, NOON);
        assertNotNull(error);
        assertTrue(error.contains("ทับกับรายการอื่น"));
    }

    @Test
    @DisplayName("จองหลายรายการ ถ้ามีรายการไหนไม่ผ่าน จะไม่บันทึกเลยทั้งชุด")
    void allOrNothing() {
        ArrayList<BookingItem> requests = new ArrayList<BookingItem>();
        requests.add(new BookingItem(2, DAY, 15, 1));
        requests.add(new BookingItem(2, DAY.minusDays(2), 15, 1));  // ย้อนหลัง
        assertNull(center.createBookings(cashier, member.getId(), requests, "S1", "s.jpg", 360, NOON));
        assertEquals(0, member.getBookings().size());
        assertTrue(center.isCourtAvailable(2, DAY, 15, 1));
    }

    @Test
    @DisplayName("ประกาศวันหยุดทีหลัง การจอง 20:00-21:00 ที่มีอยู่จะอยู่นอกเวลาทำการ")
    void holidayMakesBookingOutsideHours() {
        assertNotNull(center.createBookings(cashier, member.getId(), request(1, DAY, 20, 1), "S1", "s.jpg", 180, NOON));
        assertEquals(1, center.findBookingsOutsideHours(DAY, true).size());
        assertEquals(0, center.findBookingsOutsideHours(DAY).size());
        center.getOpeningHours().addPublicHoliday(DAY);
        assertEquals(1, center.findBookingsOutsideHours(DAY).size());
    }
}
