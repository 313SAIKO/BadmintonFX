package org.example.badminton;

import java.time.LocalDate;
import java.util.ArrayList;

// ศูนย์แบดมินตัน: เก็บสนาม สมาชิก อุปกรณ์ การจอง และรายการที่ทำให้เกิดรายได้ทั้งหมด
public class BadmintonCenter {
    public static final int COURT_COUNT = 12;

    private String name;
    private Court[] courts;                     // Composition: สร้างภายใน constructor
    private OpeningHours openingHours;          // Composition: สร้างภายใน constructor
    private ArrayList<Member> members;          // Aggregation: สร้างข้างนอกแล้วส่งเข้ามา
    private ArrayList<Item> items;              // Aggregation: สร้างข้างนอกแล้วส่งเข้ามา
    private ArrayList<Booking> bookings;
    private ArrayList<Chargeable> transactions; // ทุกรายการที่มีรายได้ ใช้ทำสรุปรายได้
    private int packageCount;
    private int orderCount;

    public BadmintonCenter(String name) {
        this.name = name;
        courts = new Court[COURT_COUNT];
        for (int i = 0; i < COURT_COUNT; i++)
            courts[i] = new Court(i + 1);
        openingHours = new OpeningHours();
        members = new ArrayList<Member>();
        items = new ArrayList<Item>();
        bookings = new ArrayList<Booking>();
        transactions = new ArrayList<Chargeable>();
        packageCount = 0;
        orderCount = 0;
    }

    public String getName() { return name; }
    public OpeningHours getOpeningHours() { return openingHours; }
    public ArrayList<Member> getMembers() { return members; }
    public ArrayList<Item> getItems() { return items; }

    // คืนค่า null ถ้าไม่มีสนามหมายเลขนี้
    public Court getCourt(int courtNo) {
        if (courtNo < 1 || courtNo > COURT_COUNT)
            return null;
        return courts[courtNo - 1];
    }

    // ---------- สมาชิก ----------
    public String nextMemberId() {
        return String.format("M%03d", members.size() + 1);
    }

    public void addMember(Member m) {
        members.add(m);
    }

    public Member findMember(String memberId) {
        for (Member m : members) {
            if (m.getId().equalsIgnoreCase(memberId.trim()))
                return m;
        }
        return null;
    }

    // ---------- อุปกรณ์ ----------
    public void addItem(Item item) {
        items.add(item);
    }

    public Item findItem(String code) {
        for (Item item : items) {
            if (item.getCode().equalsIgnoreCase(code.trim()))
                return item;
        }
        return null;
    }

    // ---------- การจองสนาม ----------
    public boolean isCourtAvailable(int courtNo, LocalDate date, int startHour, int hours) {
        for (Booking b : bookings) {
            if (b.overlaps(courtNo, date, startHour, hours))
                return false;
        }
        return true;
    }

    // คืนข้อความบอกสาเหตุถ้าจองไม่ได้ หรือ null ถ้าจองได้
    public String checkBooking(String memberId, int courtNo, LocalDate date, int startHour, int hours) {
        if (findMember(memberId) == null)
            return "ไม่พบสมาชิกรหัส " + memberId + " (ต้องสมัครสมาชิกก่อนจึงจะจองได้)";
        if (getCourt(courtNo) == null)
            return "ไม่มีสนามหมายเลข " + courtNo + " (มีสนาม 1-" + COURT_COUNT + ")";
        if (hours < 1)
            return "ต้องจองอย่างน้อย 1 ชั่วโมง";
        if (!openingHours.isOpen(date, startHour, hours))
            return "ช่วงเวลาที่จองอยู่นอกเวลาทำการ: " + openingHours.describe(date);
        if (!isCourtAvailable(courtNo, date, startHour, hours))
            return "สนาม " + courtNo + " มีผู้จองแล้วในช่วงเวลานี้";
        return null;
    }

    // คืนค่า null ถ้าจองไม่ได้ (ข้อมูลไม่ถูกต้อง หรือมียอดชำระแต่ไม่มีสลิป)
    public Booking createBooking(Cashier cashier, String memberId, int courtNo, LocalDate date,
                                 int startHour, int hours, String slipNo, String slipFile) {
        if (checkBooking(memberId, courtNo, date, startHour, hours) != null)
            return null;
        Member m = findMember(memberId);
        double fee = m.calculateCourtFee(hours, Court.PRICE_PER_HOUR);
        if (fee > 0 && (slipNo == null || slipNo.trim().isEmpty()))
            return null;
        String bookingId = String.format("B%04d", bookings.size() + 1);
        Booking b = new Booking(bookingId, m, getCourt(courtNo), cashier, date, startHour, hours, slipNo, slipFile);
        m.useHours(hours);
        m.addBooking(b);
        bookings.add(b);
        transactions.add(b);
        return b;
    }

    // ---------- แพ็กเกจ ----------
    public String checkPackage(String memberId) {
        Member m = findMember(memberId);
        if (m == null)
            return "ไม่พบสมาชิกรหัส " + memberId + " (ต้องสมัครสมาชิกก่อนจึงจะซื้อแพ็กเกจได้)";
        if (m.getPackageHoursLeft() > 0)
            return "สมาชิกยังมีชั่วโมงแพ็กเกจเหลือ " + m.getPackageHoursLeft() + " ชม. ใช้ให้หมดก่อนจึงซื้อใหม่ได้";
        return null;
    }

    public HourPackage createPackage(String memberId, LocalDate date) {
        if (checkPackage(memberId) != null)
            return null;
        Member m = findMember(memberId);
        packageCount++;
        HourPackage p = new HourPackage(String.format("P%03d", packageCount), m, date);
        m.setHourPackage(p);
        transactions.add(p);
        return p;
    }

    // ---------- เช่า/ขายอุปกรณ์ ----------
    public String checkItemOrder(String itemCode, int quantity) {
        Item item = findItem(itemCode);
        if (item == null)
            return "ไม่พบอุปกรณ์รหัส " + itemCode;
        if (!item.isAvailable(quantity))
            return "จำนวนไม่ถูกต้องหรือมีของไม่พอ: " + item;
        return null;
    }

    public ItemOrder createItemOrder(String itemCode, int quantity, LocalDate date, String customerName) {
        if (checkItemOrder(itemCode, quantity) != null)
            return null;
        Item item = findItem(itemCode);
        item.provide(quantity);
        orderCount++;
        ItemOrder order = new ItemOrder(String.format("O%04d", orderCount), item, quantity, date, customerName);
        transactions.add(order);
        return order;
    }

    // ---------- ค้นหาการจอง (Overloading) ----------
    // ค้นจากรหัสการจอง รหัสสมาชิก หรือบางส่วนของชื่อสมาชิก
    public ArrayList<Booking> searchBooking(String keyword) {
        ArrayList<Booking> result = new ArrayList<Booking>();
        String key = keyword.trim().toLowerCase();
        if (key.isEmpty())
            return result;
        for (Booking b : bookings) {
            if (b.getBookingId().toLowerCase().equals(key)
                    || b.getMember().getId().toLowerCase().equals(key)
                    || b.getMember().getName().toLowerCase().contains(key))
                result.add(b);
        }
        return result;
    }

    public ArrayList<Booking> searchBooking(LocalDate date) {
        ArrayList<Booking> result = new ArrayList<Booking>();
        for (Booking b : bookings) {
            if (b.getDate().equals(date))
                result.add(b);
        }
        return result;
    }

    public ArrayList<Booking> searchBooking(int courtNo, LocalDate date) {
        ArrayList<Booking> result = new ArrayList<Booking>();
        for (Booking b : bookings) {
            if (b.getCourt().getCourtNo() == courtNo && b.getDate().equals(date))
                result.add(b);
        }
        return result;
    }

    // ---------- รายได้ ----------
    public ArrayList<Chargeable> getTransactions(LocalDate date) {
        ArrayList<Chargeable> result = new ArrayList<Chargeable>();
        for (Chargeable c : transactions) {
            if (c.getDate().equals(date))
                result.add(c);
        }
        return result;
    }

    public ArrayList<Chargeable> getTransactions(int year, int month) {
        ArrayList<Chargeable> result = new ArrayList<Chargeable>();
        for (Chargeable c : transactions) {
            if (c.getDate().getYear() == year && c.getDate().getMonthValue() == month)
                result.add(c);
        }
        return result;
    }
}
