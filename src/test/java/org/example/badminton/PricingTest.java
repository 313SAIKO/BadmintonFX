package org.example.badminton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.example.badminton.TestHelper.DAY;
import static org.example.badminton.TestHelper.NOON;
import static org.example.badminton.TestHelper.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("กฎคิดค่าสนาม")
class PricingTest {
    private BadmintonCenter center;
    private Cashier cashier;
    private Member member;

    @BeforeEach
    void setUp() {
        center = new BadmintonCenter("ทดสอบ");
        cashier = new Cashier("C01", "แคชเชียร์ทดสอบ", "080-000-0000");
        member = cashier.registerMember(center, "สมาชิกทดสอบ", "081-111-1111");
    }

    private double price(ArrayList<BookingItem> requests, LocalDateTime now) {
        return center.priceBookings(member, requests, now);
    }

    @Test
    @DisplayName("ราคาปกติชั่วโมงละ 180 บาท")
    void normalPrice() {
        assertEquals(180.0, price(request(1, DAY, 18, 1), NOON), 0.001);
        assertEquals(540.0, price(request(1, DAY, 18, 3), NOON), 0.001);
    }

    @Test
    @DisplayName("สะสม 18 ชม. จองอีก 3 ชม. ชั่วโมงที่ 20 คิด 30% รวม 414 บาท (180 + 54 + 180)")
    void twentiethHourDiscount() {
        TestHelper.addPaidHours(center, cashier, member, 18);
        assertEquals(18, member.getPaidHours());
        ArrayList<BookingItem> requests = request(1, DAY, 18, 3);
        assertEquals(414.0, price(requests, NOON), 0.001);
        assertEquals(1, requests.get(0).getDiscountHours());
    }

    @Test
    @DisplayName("ส่วนลดวนทุก 20 ชม. สะสม 39 ชม. แล้วชั่วโมงที่ 40 คิด 54 บาท")
    void discountRepeatsEveryTwentyHours() {
        TestHelper.addPaidHours(center, cashier, member, 39);
        assertEquals(54.0, price(request(1, DAY, 18, 1), NOON), 0.001);
    }

    @Test
    @DisplayName("จองแล้วชั่วโมงสะสมเพิ่มขึ้นตามจำนวนชั่วโมงที่จ่ายเงิน")
    void paidHoursAccumulate() {
        ArrayList<BookingItem> requests = request(1, DAY, 18, 3);
        double fee = price(requests, NOON);
        assertNotNull(center.createBookings(cashier, member.getId(), requests, "S1", "s1.jpg", fee, NOON));
        assertEquals(3, member.getPaidHours());
    }

    @Test
    @DisplayName("แพ็กเกจครอบคลุมทั้งหมด ไม่ต้องจ่าย หักแพ็กเกจ และไม่นับชั่วโมงสะสม")
    void packageCoversAll() {
        center.createPackage(member.getId(), NOON.minusDays(1));
        ArrayList<BookingItem> requests = request(1, DAY, 18, 2);
        assertEquals(0.0, price(requests, NOON), 0.001);
        assertNotNull(center.createBookings(cashier, member.getId(), requests, "", "", 0, NOON));
        assertEquals(13, member.getPackageHoursLeft());
        assertEquals(0, member.getPaidHours());
    }

    @Test
    @DisplayName("แพ็กเกจเหลือ 1 ชม. จอง 3 ชม. หักแพ็กเกจ 1 ชม. อีก 2 ชม. จ่าย 360 บาท")
    void packagePartlyCovers() {
        center.createPackage(member.getId(), NOON.minusDays(1));
        ArrayList<BookingItem> usePackage = new ArrayList<BookingItem>();
        for (int court = 1; court <= 7; court++)
            usePackage.add(new BookingItem(court, DAY, 13, 2));  // ใช้แพ็กเกจไป 14 ชม.
        assertEquals(0.0, price(usePackage, NOON), 0.001);
        assertNotNull(center.createBookings(cashier, member.getId(), usePackage, "", "", 0, NOON));
        assertEquals(1, member.getPackageHoursLeft());

        ArrayList<BookingItem> requests = request(8, DAY, 18, 3);
        assertEquals(360.0, price(requests, NOON), 0.001);
        assertEquals(1, requests.get(0).getPackageHours());
    }

    @Test
    @DisplayName("จองหลายสนามหลายวันในครั้งเดียว ชั่วโมงสะสมคิดต่อกันตามลำดับ รวม 774 บาท")
    void multipleBookingsContinueCounting() {
        TestHelper.addPaidHours(center, cashier, member, 18);
        ArrayList<BookingItem> requests = new ArrayList<BookingItem>();
        requests.add(new BookingItem(3, DAY, 18, 2));               // ชั่วโมงที่ 19, 20
        requests.add(new BookingItem(4, DAY, 18, 1));               // ชั่วโมงที่ 21
        requests.add(new BookingItem(1, DAY.plusDays(1), 13, 2));   // ชั่วโมงที่ 22, 23
        assertEquals(774.0, price(requests, NOON), 0.001);
        assertEquals(234.0, requests.get(0).getFee(), 0.001);
        assertEquals(180.0, requests.get(1).getFee(), 0.001);
        assertEquals(360.0, requests.get(2).getFee(), 0.001);

        assertNotNull(center.createBookings(cashier, member.getId(), requests, "MULTI", "m.jpg", 774, NOON));
        assertEquals(23, member.getPaidHours());
    }

    @Test
    @DisplayName("มาถึง 17:00 พอดี จองชั่วโมง 17:00 คิดเต็มราคา")
    void onTimeFullPrice() {
        ArrayList<BookingItem> requests = request(1, DAY, 17, 1);
        assertEquals(180.0, price(requests, DAY.atTime(17, 0)), 0.001);
        assertFalse(requests.get(0).isLateStart());
    }

    @ParameterizedTest(name = "ชั่วโมง 17:00 เริ่มไปแล้ว {0} นาที จองได้ครึ่งราคา 90 บาท")
    @ValueSource(ints = { 1, 25, 30 })
    void lateStartHalfPrice(int minute) {
        ArrayList<BookingItem> requests = request(1, DAY, 17, 1);
        assertEquals(90.0, price(requests, DAY.atTime(17, minute)), 0.001);
        assertTrue(requests.get(0).isLateStart());
    }

    @Test
    @DisplayName("มาสาย จอง 17:00-19:00 ชั่วโมงแรกครึ่งราคาและไม่นับสะสม (90 + 180)")
    void lateStartNotCounted() {
        LocalDateTime at1725 = DAY.atTime(17, 25);
        ArrayList<BookingItem> requests = request(1, DAY, 17, 2);
        assertEquals(270.0, price(requests, at1725), 0.001);
        assertNotNull(center.createBookings(cashier, member.getId(), requests, "LATE", "l.jpg", 270, at1725));
        assertEquals(1, member.getPaidHours());
        assertTrue(member.getBookings().get(0).isLateStart());
    }

    @Test
    @DisplayName("สะสม 18 ชม. มาสาย 17:25 จอง 3 ชม. = 90 + 180 + 54 = 324 บาท")
    void lateStartWithTwentiethHour() {
        TestHelper.addPaidHours(center, cashier, member, 18);
        assertEquals(324.0, price(request(3, DAY, 17, 3), DAY.atTime(17, 25)), 0.001);
    }

    @Test
    @DisplayName("ชั่วโมงครึ่งราคาไม่หักแพ็กเกจ (แพ็กเกจนับเป็นชั่วโมงเต็ม)")
    void lateStartDoesNotUsePackage() {
        center.createPackage(member.getId(), NOON.minusDays(1));
        LocalDateTime at1725 = DAY.atTime(17, 25);
        ArrayList<BookingItem> requests = request(1, DAY, 17, 2);
        assertEquals(90.0, price(requests, at1725), 0.001);
        assertEquals(1, requests.get(0).getPackageHours());
        assertNotNull(center.createBookings(cashier, member.getId(), requests, "LATE-P", "p.jpg", 90, at1725));
        assertEquals(14, member.getPackageHoursLeft());
    }
}
