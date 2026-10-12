package org.example.badminton;

import java.time.LocalDate;
import java.util.ArrayList;

// พนักงานแคชเชียร์: สมัครสมาชิก จองสนาม ขายแพ็กเกจ เช่า/ขายอุปกรณ์ และค้นหาการจอง
// รับ BadmintonCenter ผ่าน parameter เท่านั้น ไม่เก็บไว้ (Dependency)
public class Cashier extends Person {

    public Cashier(String id, String name, String phone) {
        super(id, name, phone);
    }

    @Override
    public String getRole() {
        return "แคชเชียร์";
    }

    public Member registerMember(BadmintonCenter center, String name, String phone) {
        Member m = new Member(center.nextMemberId(), name, phone);
        center.addMember(m);
        return m;
    }

    public Booking bookCourt(BadmintonCenter center, String memberId, int courtNo, LocalDate date,
                             int startHour, int hours, String slipNo, String slipFile) {
        return center.createBooking(this, memberId, courtNo, date, startHour, hours, slipNo, slipFile);
    }

    public HourPackage sellPackage(BadmintonCenter center, String memberId, LocalDate date) {
        return center.createPackage(memberId, date);
    }

    public ItemOrder sellItem(BadmintonCenter center, String itemCode, int quantity,
                              LocalDate date, String customerName) {
        return center.createItemOrder(itemCode, quantity, date, customerName);
    }

    // เช่า/ซื้อหลายรายการจากตะกร้าพร้อมกันในครั้งเดียว
    public ArrayList<ItemOrder> sellItems(BadmintonCenter center, ArrayList<CartItem> cart,
                                          LocalDate date, String customerName) {
        return center.createItemOrders(cart, date, customerName);
    }

    // Overloading: ค้นหาการจองได้ 3 แบบ ชื่อเมธอดเดียวกันแต่ parameter ต่างกัน
    public ArrayList<Booking> searchBooking(BadmintonCenter center, String keyword) {
        return center.searchBooking(keyword);
    }

    public ArrayList<Booking> searchBooking(BadmintonCenter center, LocalDate date) {
        return center.searchBooking(date);
    }

    public ArrayList<Booking> searchBooking(BadmintonCenter center, int courtNo, LocalDate date) {
        return center.searchBooking(courtNo, date);
    }
}
