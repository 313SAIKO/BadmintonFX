package org.example.badminton;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // Overloading: ทำรายการตอนนี้ (ใช้จากหน้าจอ) หรือระบุเวลาที่ทำรายการเอง (ใช้สร้างข้อมูลประวัติย้อนหลัง)
    // slipAmount = ยอดเงินที่อ่านจากสลิป ต้องตรงกับค่าสนาม
    public Booking bookCourt(BadmintonCenter center, String memberId, int courtNo, LocalDate date, int startHour,
                             int hours, String slipNo, String slipFile, double slipAmount) {
        return bookCourt(center, memberId, courtNo, date, startHour, hours, slipNo, slipFile, slipAmount,
                LocalDateTime.now());
    }

    public Booking bookCourt(BadmintonCenter center, String memberId, int courtNo, LocalDate date, int startHour,
                             int hours, String slipNo, String slipFile, double slipAmount, LocalDateTime bookedAt) {
        return center.createBooking(this, memberId, courtNo, date, startHour, hours, slipNo, slipFile, slipAmount,
                bookedAt);
    }

    // จองหลายสนาม/หลายวันพร้อมกันในครั้งเดียว ชำระด้วยสลิปใบเดียว
    public ArrayList<Booking> bookCourts(BadmintonCenter center, String memberId, ArrayList<BookingItem> requests,
                                         String slipNo, String slipFile, double slipAmount) {
        return center.createBookings(this, memberId, requests, slipNo, slipFile, slipAmount, LocalDateTime.now());
    }

    public HourPackage sellPackage(BadmintonCenter center, String memberId) {
        return sellPackage(center, memberId, LocalDateTime.now());
    }

    public HourPackage sellPackage(BadmintonCenter center, String memberId, LocalDateTime soldAt) {
        return center.createPackage(memberId, soldAt);
    }

    public ItemOrder sellItem(BadmintonCenter center, String itemCode, int quantity,
                              LocalDate date, String customerName) {
        return sellItem(center, itemCode, quantity, date, customerName, LocalDateTime.now());
    }

    public ItemOrder sellItem(BadmintonCenter center, String itemCode, int quantity,
                              LocalDate date, String customerName, LocalDateTime soldAt) {
        return center.createItemOrder(itemCode, quantity, date, customerName, soldAt);
    }

    // เช่า/ซื้อหลายรายการจากตะกร้าพร้อมกันในครั้งเดียว
    public ArrayList<ItemOrder> sellItems(BadmintonCenter center, ArrayList<CartItem> cart,
                                          LocalDate date, String customerName) {
        return center.createItemOrders(cart, date, customerName, LocalDateTime.now());
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
