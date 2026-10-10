package org.example.badminton;

import java.time.LocalDate;
import java.util.ArrayList;

// ผู้ดูแลระบบ: ดูประวัติการจองของสมาชิก และดูสรุปรายได้รายวัน/รายเดือน
// รับ BadmintonCenter ผ่าน parameter เท่านั้น ไม่เก็บไว้ (Dependency)
public class Admin extends Person {
    public static final String[] CATEGORIES = { Chargeable.COURT_FEE, Chargeable.PACKAGE,
                                                Chargeable.RENTAL, Chargeable.SALE };

    public Admin(String id, String name, String phone) {
        super(id, name, phone);
    }

    @Override
    public String getRole() {
        return "ผู้ดูแลระบบ";
    }

    // คืนค่า null ถ้าไม่พบสมาชิก
    public ArrayList<Booking> viewBookingHistory(BadmintonCenter center, String memberId) {
        Member m = center.findMember(memberId);
        if (m == null)
            return null;
        return m.getBookings();
    }

    // Overloading: ดูรายการรายได้ของวัน หรือของเดือน
    public ArrayList<Chargeable> viewTransactions(BadmintonCenter center, LocalDate date) {
        return center.getTransactions(date);
    }

    public ArrayList<Chargeable> viewTransactions(BadmintonCenter center, int year, int month) {
        return center.getTransactions(year, month);
    }

    public double getDailyRevenue(BadmintonCenter center, LocalDate date) {
        return sumTotal(center.getTransactions(date));
    }

    public double getMonthlyRevenue(BadmintonCenter center, int year, int month) {
        return sumTotal(center.getTransactions(year, month));
    }

    // Polymorphism: เรียก getTotal() ผ่าน interface Chargeable
    // แต่ละ object (Booking, HourPackage, ItemOrder) คิดยอดเงินของตัวเอง
    public double sumTotal(ArrayList<Chargeable> list) {
        double total = 0;
        for (Chargeable c : list)
            total += c.getTotal();
        return total;
    }

    public double sumCategory(ArrayList<Chargeable> list, String category) {
        double total = 0;
        for (Chargeable c : list) {
            if (c.getCategory().equals(category))
                total += c.getTotal();
        }
        return total;
    }

    public void printDailyReport(BadmintonCenter center, LocalDate date) {
        ArrayList<Chargeable> list = viewTransactions(center, date);
        System.out.println("===== สรุปรายได้ประจำวัน " + center.getOpeningHours().describe(date) + " =====");
        printSummary(list);
        if (list.size() > 0) {
            System.out.println("รายการทั้งหมด:");
            for (Chargeable c : list)
                System.out.println("  " + c);
        }
    }

    public void printMonthlyReport(BadmintonCenter center, int year, int month) {
        ArrayList<Chargeable> list = viewTransactions(center, year, month);
        System.out.println("===== สรุปรายได้ประจำเดือน " + month + "/" + year + " =====");
        printSummary(list);
        if (list.size() > 0) {
            System.out.println("รายได้แยกตามวัน:");
            int days = LocalDate.of(year, month, 1).lengthOfMonth();
            for (int day = 1; day <= days; day++) {
                LocalDate date = LocalDate.of(year, month, day);
                double total = getDailyRevenue(center, date);
                if (total > 0)
                    System.out.println("  " + date + " : " + String.format("%,.2f", total) + " บาท");
            }
        }
    }

    private void printSummary(ArrayList<Chargeable> list) {
        for (int i = 0; i < CATEGORIES.length; i++)
            System.out.println("  - " + CATEGORIES[i] + " : " + String.format("%,.2f", sumCategory(list, CATEGORIES[i])) + " บาท");
        System.out.println("  รวมทั้งสิ้น " + String.format("%,.2f", sumTotal(list)) + " บาท (" + list.size() + " รายการ)");
    }
}
