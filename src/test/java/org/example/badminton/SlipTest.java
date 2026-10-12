package org.example.badminton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.example.badminton.TestHelper.DAY;
import static org.example.badminton.TestHelper.NOON;
import static org.example.badminton.TestHelper.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("การตรวจสลิปการชำระเงิน")
class SlipTest {
    private BadmintonCenter center;
    private Cashier cashier;
    private Member member;

    @BeforeEach
    void setUp() {
        center = new BadmintonCenter("ทดสอบ");
        cashier = new Cashier("C01", "แคชเชียร์ทดสอบ", "080-000-0000");
        member = cashier.registerMember(center, "สมาชิกทดสอบ", "081-111-1111");
    }

    @Test
    @DisplayName("ต้องมีเลขที่สลิปและไฟล์สลิป")
    void slipNumberAndFileRequired() {
        assertNotNull(center.checkSlip("", "a.jpg", 180, 180));
        assertNotNull(center.checkSlip("SL1", "", 180, 180));
        assertNull(center.checkSlip("SL1", "a.jpg", 180, 180));
    }

    @Test
    @DisplayName("ยอดเงินในสลิปต้องตรงกับยอดที่ต้องชำระ")
    void amountMustMatch() {
        String error = center.checkSlip("SL1", "a.jpg", 150, 180);
        assertNotNull(error);
        assertTrue(error.contains("ไม่ตรง"));
        assertNull(center.createBookings(cashier, member.getId(), request(1, DAY, 18, 1), "SL1", "a.jpg", 150, NOON));
        assertEquals(0, member.getBookings().size());
    }

    @Test
    @DisplayName("เลขที่สลิปที่ใช้แล้วใช้ซ้ำไม่ได้ (ไม่สนตัวพิมพ์เล็ก/ใหญ่และช่องว่าง)")
    void slipNumberCannotBeReused() {
        assertNotNull(center.createBookings(cashier, member.getId(), request(1, DAY, 18, 1), "SL-001", "a.jpg", 180, NOON));
        assertNotNull(center.checkSlip("SL-001", "b.jpg", 180, 180));
        assertNotNull(center.checkSlip(" sl-001 ", "b.jpg", 180, 180));
        assertNull(center.createBookings(cashier, member.getId(), request(2, DAY, 18, 1), "sl-001", "b.jpg", 180, NOON));
    }

    @Test
    @DisplayName("จองหลายรายการพร้อมกันใช้สลิปใบเดียวได้ แต่ละการจองเก็บยอดส่วนของตัวเอง")
    void oneSlipForManyBookings() {
        ArrayList<BookingItem> requests = new ArrayList<BookingItem>();
        requests.add(new BookingItem(1, DAY, 18, 2));
        requests.add(new BookingItem(2, DAY, 18, 1));
        ArrayList<Booking> booked = center.createBookings(cashier, member.getId(), requests, "SL-ALL", "all.jpg", 540, NOON);
        assertNotNull(booked);
        assertEquals(2, booked.size());
        assertEquals(360.0, booked.get(0).getSlip().getAmount(), 0.001);
        assertEquals(180.0, booked.get(1).getSlip().getAmount(), 0.001);
        assertEquals("SL-ALL", booked.get(1).getSlip().getSlipNo());
    }

    @Test
    @DisplayName("ใช้แพ็กเกจทั้งหมด ยอด 0 บาท ไม่ต้องมีสลิป")
    void noSlipWhenNothingToPay() {
        assertNull(center.checkSlip("", "", 0, 0));
        center.createPackage(member.getId(), NOON.minusDays(1));
        ArrayList<Booking> booked = center.createBookings(cashier, member.getId(), request(1, DAY, 18, 2), "", "", 0, NOON);
        assertNotNull(booked);
        assertNull(booked.get(0).getSlip());
    }
}
