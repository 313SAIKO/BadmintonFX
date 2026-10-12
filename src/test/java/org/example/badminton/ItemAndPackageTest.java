package org.example.badminton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.example.badminton.TestHelper.DAY;
import static org.example.badminton.TestHelper.NOON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("อุปกรณ์และแพ็กเกจ")
class ItemAndPackageTest {
    private BadmintonCenter center;
    private Cashier cashier;
    private Member member;
    private RentalItem shoes;
    private SaleItem racket;

    @BeforeEach
    void setUp() {
        center = new BadmintonCenter("ทดสอบ");
        cashier = new Cashier("C01", "แคชเชียร์ทดสอบ", "080-000-0000");
        member = cashier.registerMember(center, "สมาชิกทดสอบ", "081-111-1111");
        shoes = new RentalItem("R01", "รองเท้า (เช่า)", 20);
        racket = new SaleItem("S01", "ไม้แบดมินตัน", 890, 10);
        center.addItem(shoes);
        center.addItem(racket);
    }

    @Test
    @DisplayName("ของเช่านับแยกตามวัน เช่าครบ 20 คู่แล้ววันนั้นเต็ม แต่วันถัดไปว่างครบ")
    void rentalCountedPerDay() {
        assertNotNull(center.createItemOrder("R01", 20, DAY, "ลูกค้า", NOON));
        assertFalse(shoes.isAvailable(1, DAY));
        assertNotNull(center.checkItemOrder("R01", 1, DAY, NOON));
        assertTrue(shoes.getStockInfo(DAY).startsWith("เต็มแล้ว"));
        assertEquals(20, shoes.getAvailablePairs(DAY.plusDays(1)));
    }

    @Test
    @DisplayName("ค่าเช่าคู่ละ 50 บาท ของขายคิดตามราคาสินค้า")
    void itemPrices() {
        assertEquals(100.0, center.createItemOrder("R01", 2, DAY, "ลูกค้า", NOON).getTotal(), 0.001);
        assertEquals(1780.0, center.createItemOrder("S01", 2, DAY, "ลูกค้า", NOON).getTotal(), 0.001);
    }

    @Test
    @DisplayName("ของขายตัดสต็อกจริง ขายหมดแล้วขายต่อไม่ได้")
    void saleStockDecreases() {
        assertNotNull(center.createItemOrder("S01", 10, DAY, "ลูกค้า", NOON));
        assertEquals(0, racket.getStock());
        assertEquals("หมดแล้ว", racket.getStockInfo(DAY));
        assertNull(center.createItemOrder("S01", 1, DAY, "ลูกค้า", NOON));
    }

    @Test
    @DisplayName("เติมสต็อกของขาย และเพิ่มจำนวนคู่ของเช่าได้ (Polymorphism ของ addStock)")
    void restock() {
        center.createItemOrder("S01", 10, DAY, "ลูกค้า", NOON);
        assertNotNull(center.restockItem("S01", 5));
        assertEquals(5, racket.getStock());
        assertNotNull(center.restockItem("R01", 10));
        assertEquals(30, shoes.getAvailablePairs(DAY));
    }

    @Test
    @DisplayName("เติมสต็อกต้อง 1-1000 และต้องมีรหัสอุปกรณ์นั้น")
    void restockValidation() {
        assertNotNull(center.checkRestock("S01", 0));
        assertNotNull(center.checkRestock("S01", 1001));
        assertNotNull(center.checkRestock("X99", 5));
        assertNull(center.restockItem("S01", 0));
        assertEquals(10, racket.getStock());
    }

    @Test
    @DisplayName("เช่า/ซื้ออุปกรณ์ลงวันที่ย้อนหลังไม่ได้")
    void itemOrderCannotBeBackdated() {
        assertNotNull(center.checkItemOrder("R01", 1, DAY.minusDays(1), NOON));
        assertNull(center.createItemOrder("R01", 1, DAY.minusDays(1), "ลูกค้า", NOON));
    }

    @Test
    @DisplayName("ของเช่าล่วงหน้าบันทึกรายได้วันที่ใช้ ส่วนของขายบันทึกวันที่ขายจริง")
    void revenueDate() {
        assertEquals(DAY.plusDays(3), center.createItemOrder("R01", 1, DAY.plusDays(3), "ลูกค้า", NOON).getDate());
        assertEquals(DAY, center.createItemOrder("S01", 1, DAY.plusDays(3), "ลูกค้า", NOON).getDate());
    }

    @Test
    @DisplayName("ตะกร้ามีรายการที่ทำไม่ได้ จะไม่บันทึกรายการไหนเลย")
    void cartAllOrNothing() {
        ArrayList<CartItem> cart = new ArrayList<CartItem>();
        cart.add(new CartItem(shoes, 2));
        cart.add(new CartItem(racket, 99));   // มีแค่ 10 ชิ้น
        assertNull(center.createItemOrders(cart, DAY, "ลูกค้า", NOON));
        assertEquals(20, shoes.getAvailablePairs(DAY));
        assertEquals(10, racket.getStock());
    }

    @Test
    @DisplayName("แพ็กเกจ 15 ชม. ราคา 2,600 บาท วันที่ซื้อคือวันที่ทำรายการ")
    void packageSale() {
        HourPackage p = center.createPackage(member.getId(), NOON);
        assertNotNull(p);
        assertEquals(2600.0, p.getTotal(), 0.001);
        assertEquals(15, member.getPackageHoursLeft());
        assertEquals(DAY, p.getDate());
    }

    @Test
    @DisplayName("ยังมีชั่วโมงแพ็กเกจเหลือ ซื้อแพ็กเกจใหม่ไม่ได้")
    void cannotBuyPackageWhileHoursLeft() {
        center.createPackage(member.getId(), NOON);
        assertNotNull(center.checkPackage(member.getId()));
        assertNull(center.createPackage(member.getId(), NOON));
    }

    @Test
    @DisplayName("รายได้แยกตามหมวดถูกต้อง (Polymorphism ของ Chargeable.getTotal)")
    void revenueByCategory() {
        Admin admin = new Admin("A01", "แอดมิน", "089-999-9999");
        center.createPackage(member.getId(), NOON);
        center.createItemOrder("R01", 2, DAY, "ลูกค้า", NOON);
        center.createItemOrder("S01", 1, DAY, "ลูกค้า", NOON);
        ArrayList<Chargeable> list = admin.viewTransactions(center, DAY);
        assertEquals(2600.0, admin.sumCategory(list, Chargeable.PACKAGE), 0.001);
        assertEquals(100.0, admin.sumCategory(list, Chargeable.RENTAL), 0.001);
        assertEquals(890.0, admin.sumCategory(list, Chargeable.SALE), 0.001);
        assertEquals(3590.0, admin.getDailyRevenue(center, DAY), 0.001);
    }
}
