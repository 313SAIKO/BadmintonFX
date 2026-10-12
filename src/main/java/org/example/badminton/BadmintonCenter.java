package org.example.badminton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

// ศูนย์แบดมินตัน: เก็บสนาม สมาชิก อุปกรณ์ การจอง และรายการที่ทำให้เกิดรายได้ทั้งหมด
public class BadmintonCenter {
    public static final int COURT_COUNT = 12;
    public static final int LATE_LIMIT_MINUTES = 30;  // ชั่วโมงที่กำลังเป็นอยู่ จองได้ถ้าเริ่มไปแล้วไม่เกิน 30 นาที
    public static final int MAX_RESTOCK = 1000;       // เติมสต็อกได้ครั้งละไม่เกิน 1,000 ชิ้น/คู่

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

    // จองย้อนหลังไม่ได้: ชั่วโมงที่ผ่านไปแล้ว หรือชั่วโมงที่กำลังเป็นอยู่แต่ผ่านไปเกิน 30 นาที
    public boolean isPastTime(LocalDate date, int hour, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        if (date.isBefore(today))
            return true;
        if (!date.equals(today))
            return false;
        return hour < now.getHour() || (hour == now.getHour() && now.getMinute() > LATE_LIMIT_MINUTES);
    }

    // ชั่วโมงที่กำลังเป็นอยู่ซึ่งเริ่มไปแล้วไม่เกิน 30 นาที (ลูกค้า walk-in):
    // ยังจองได้ แต่คิดครึ่งราคาและไม่นับเป็นชั่วโมงสะสม เพราะเล่นได้ไม่เต็มชั่วโมง
    public boolean isLateStart(LocalDate date, int hour, LocalDateTime now) {
        return date.equals(now.toLocalDate()) && hour == now.getHour()
                && now.getMinute() > 0 && now.getMinute() <= LATE_LIMIT_MINUTES;
    }

    // คิดราคาคำขอจองทั้งหมดตามลำดับ (ตรวจชั่วโมงที่เริ่มไปแล้วตามเวลา now ก่อน) คืนยอดรวม
    public double priceBookings(Member m, ArrayList<BookingItem> requests, LocalDateTime now) {
        for (BookingItem r : requests)
            r.setLateStart(isLateStart(r.getDate(), r.getStartHour(), now));
        return m.priceItems(requests, Court.PRICE_PER_HOUR);
    }

    // คืนข้อความบอกสาเหตุถ้าจองไม่ได้ หรือ null ถ้าจองได้ (bookedAt = เวลาที่ทำรายการจอง)
    public String checkBooking(String memberId, int courtNo, LocalDate date, int startHour, int hours,
                               LocalDateTime bookedAt) {
        if (findMember(memberId) == null)
            return "ไม่พบสมาชิกรหัส " + memberId + " (ต้องสมัครสมาชิกก่อนจึงจะจองได้)";
        if (getCourt(courtNo) == null)
            return "ไม่มีสนามหมายเลข " + courtNo + " (มีสนาม 1-" + COURT_COUNT + ")";
        if (hours < 1)
            return "ต้องจองอย่างน้อย 1 ชั่วโมง";
        if (isPastTime(date, startHour, bookedAt)) {
            if (date.equals(bookedAt.toLocalDate()) && startHour == bookedAt.getHour())
                return "ชั่วโมง " + String.format("%02d:00", startHour) + " เริ่มไปแล้วเกิน "
                        + LATE_LIMIT_MINUTES + " นาที จองไม่ได้";
            return "จองย้อนหลังไม่ได้ เวลา " + String.format("%02d:00", startHour) + " ของวันที่ " + date + " ผ่านไปแล้ว";
        }
        if (!openingHours.isOpen(date, startHour, hours))
            return "ช่วงเวลาที่จองอยู่นอกเวลาทำการ: " + openingHours.describe(date);
        if (!isCourtAvailable(courtNo, date, startHour, hours))
            return "สนาม " + courtNo + " มีผู้จองแล้วในช่วงเวลานี้";
        return null;
    }

    // ตรวจคำขอจองทุกรายการ รวมถึงรายการที่ทับกันเองในการจองครั้งเดียวกัน
    public String checkBookings(String memberId, ArrayList<BookingItem> requests, LocalDateTime bookedAt) {
        if (requests.isEmpty())
            return "ยังไม่ได้เลือกสนามและเวลา";
        for (int i = 0; i < requests.size(); i++) {
            BookingItem r = requests.get(i);
            String error = checkBooking(memberId, r.getCourtNo(), r.getDate(), r.getStartHour(), r.getHours(), bookedAt);
            if (error != null)
                return r + ": " + error;
            for (int j = 0; j < i; j++) {
                if (r.overlaps(requests.get(j)))
                    return r + ": ทับกับรายการอื่นที่เลือกไว้";
            }
        }
        return null;
    }

    // คืนการจองที่ใช้สลิปเลขนี้ไปแล้ว หรือ null ถ้ายังไม่เคยใช้
    public Booking findBookingBySlip(String slipNo) {
        for (Booking b : bookings) {
            if (b.getSlip() != null && b.getSlip().getSlipNo().trim().equalsIgnoreCase(slipNo.trim()))
                return b;
        }
        return null;
    }

    // ตรวจสลิปการชำระเงิน: ต้องมีเลขที่และไฟล์, เลขที่สลิปห้ามซ้ำกับการจองครั้งก่อน
    // และยอดเงินในสลิปต้องตรงกับยอดที่ต้องชำระ คืนข้อความบอกสาเหตุ หรือ null ถ้าผ่าน
    public String checkSlip(String slipNo, String slipFile, double slipAmount, double total) {
        if (total <= 0)
            return null;  // ใช้ชั่วโมงแพ็กเกจทั้งหมด ไม่ต้องมีสลิป
        if (slipNo == null || slipNo.trim().isEmpty())
            return "กรุณากรอกเลขที่สลิป";
        if (slipFile == null || slipFile.trim().isEmpty())
            return "กรุณาแนบไฟล์สลิป";
        Booking used = findBookingBySlip(slipNo);
        if (used != null)
            return "เลขที่สลิป " + slipNo.trim() + " ถูกใช้ไปแล้วกับการจอง " + used.getBookingId()
                    + " ของ " + used.getMember().getId() + " " + used.getMember().getName();
        if (Math.abs(slipAmount - total) > 0.005)
            return "ยอดเงินในสลิป " + String.format("%,.2f", slipAmount) + " บาท ไม่ตรงกับยอดที่ต้องชำระ "
                    + String.format("%,.2f", total) + " บาท";
        return null;
    }

    // จองหลายสนาม/หลายวันในครั้งเดียว ใช้สลิปใบเดียวกัน (แต่ละการจองเก็บยอดส่วนของตัวเองในสลิป)
    // จองตามลำดับในรายการ ชั่วโมงสะสมและแพ็กเกจจะต่อกันเหมือน Member.priceItems()
    // คืนค่า null ถ้ามีรายการใดจองไม่ได้ หรือสลิปไม่ผ่านการตรวจ (จะไม่บันทึกรายการไหนเลย)
    public ArrayList<Booking> createBookings(Cashier cashier, String memberId, ArrayList<BookingItem> requests,
                                             String slipNo, String slipFile, double slipAmount, LocalDateTime bookedAt) {
        if (checkBookings(memberId, requests, bookedAt) != null)
            return null;
        Member m = findMember(memberId);
        double total = priceBookings(m, requests, bookedAt);
        if (checkSlip(slipNo, slipFile, slipAmount, total) != null)
            return null;
        ArrayList<Booking> result = new ArrayList<Booking>();
        for (BookingItem r : requests) {
            String bookingId = String.format("B%04d", bookings.size() + 1);
            Booking b = new Booking(bookingId, m, getCourt(r.getCourtNo()), cashier, r, slipNo, slipFile, bookedAt);
            m.useHours(r);
            m.addBooking(b);
            bookings.add(b);
            transactions.add(b);
            result.add(b);
        }
        return result;
    }

    // จอง 1 รายการ คืนค่า null ถ้าจองไม่ได้
    public Booking createBooking(Cashier cashier, String memberId, int courtNo, LocalDate date, int startHour,
                                 int hours, String slipNo, String slipFile, double slipAmount, LocalDateTime bookedAt) {
        ArrayList<BookingItem> requests = new ArrayList<BookingItem>();
        requests.add(new BookingItem(courtNo, date, startHour, hours));
        ArrayList<Booking> result = createBookings(cashier, memberId, requests, slipNo, slipFile, slipAmount, bookedAt);
        if (result == null)
            return null;
        return result.get(0);
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

    // วันที่ซื้อแพ็กเกจคือวันที่ทำรายการเสมอ (soldAt) เลือกวันเองไม่ได้ จึงลงรายได้ย้อนหลังไม่ได้
    public HourPackage createPackage(String memberId, LocalDateTime soldAt) {
        if (checkPackage(memberId) != null)
            return null;
        Member m = findMember(memberId);
        packageCount++;
        HourPackage p = new HourPackage(String.format("P%03d", packageCount), m, soldAt.toLocalDate());
        m.setHourPackage(p);
        transactions.add(p);
        return p;
    }

    // ---------- เช่า/ขายอุปกรณ์ ----------
    // date = วันที่ใช้อุปกรณ์ (ของเช่าจองล่วงหน้าได้) แต่ลงย้อนหลังไม่ได้ (now = เวลาที่ทำรายการ)
    // ของเช่าดูจำนวนว่างของวันนั้น ส่วนของขายดูสต็อกคงเหลือ
    public String checkItemOrder(String itemCode, int quantity, LocalDate date, LocalDateTime now) {
        Item item = findItem(itemCode);
        if (item == null)
            return "ไม่พบอุปกรณ์รหัส " + itemCode;
        if (quantity < 1)
            return "จำนวนต้องมากกว่า 0";
        if (date.isBefore(now.toLocalDate()))
            return "ลงรายการย้อนหลังไม่ได้ วันที่ " + date + " ผ่านไปแล้ว";
        if (!item.isAvailable(quantity, date))
            return "มีของไม่พอ: " + item.getName() + " วันที่ " + date + " " + item.getStockInfo(date);
        return null;
    }

    public ItemOrder createItemOrder(String itemCode, int quantity, LocalDate date, String customerName,
                                     LocalDateTime now) {
        if (checkItemOrder(itemCode, quantity, date, now) != null)
            return null;
        Item item = findItem(itemCode);
        item.provide(quantity, date);
        orderCount++;
        ItemOrder order = new ItemOrder(String.format("O%04d", orderCount), item, quantity,
                item.getRevenueDate(date, now.toLocalDate()), customerName);
        transactions.add(order);
        return order;
    }

    // เติมสต็อก/เพิ่มจำนวนอุปกรณ์ คืนข้อความบอกสาเหตุถ้าทำไม่ได้ หรือ null ถ้าทำได้
    public String checkRestock(String itemCode, int quantity) {
        if (findItem(itemCode) == null)
            return "ไม่พบอุปกรณ์รหัส " + itemCode;
        if (quantity < 1 || quantity > MAX_RESTOCK)
            return "จำนวนที่เพิ่มต้องอยู่ระหว่าง 1-" + MAX_RESTOCK;
        return null;
    }

    // คืนค่า null ถ้าทำไม่ได้ (Polymorphism: ของขายเติมสต็อก ของเช่าเพิ่มจำนวนคู่ให้เช่า)
    public Item restockItem(String itemCode, int quantity) {
        if (checkRestock(itemCode, quantity) != null)
            return null;
        Item item = findItem(itemCode);
        item.addStock(quantity);
        return item;
    }

    // ตรวจทุกรายการในตะกร้า คืนข้อความบอกสาเหตุถ้าทำรายการไม่ได้ หรือ null ถ้าทำได้
    public String checkItemOrders(ArrayList<CartItem> cart, LocalDate date, LocalDateTime now) {
        if (cart.isEmpty())
            return "ยังไม่มีรายการในตะกร้า";
        for (CartItem c : cart) {
            String error = checkItemOrder(c.getItem().getCode(), c.getQuantity(), date, now);
            if (error != null)
                return error;
        }
        return null;
    }

    // เช่า/ซื้อหลายรายการในการซื้อครั้งเดียว ได้ ItemOrder แยกตามรายการ (เพื่อแยกหมวดรายได้เช่ากับขาย)
    // คืนค่า null ถ้ามีรายการใดทำไม่ได้ และจะไม่บันทึกรายการไหนเลย
    public ArrayList<ItemOrder> createItemOrders(ArrayList<CartItem> cart, LocalDate date, String customerName,
                                                 LocalDateTime now) {
        if (checkItemOrders(cart, date, now) != null)
            return null;
        ArrayList<ItemOrder> orders = new ArrayList<ItemOrder>();
        for (CartItem c : cart)
            orders.add(createItemOrder(c.getItem().getCode(), c.getQuantity(), date, customerName, now));
        return orders;
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

    // ---------- วันหยุด ----------
    // การจองของวันนั้นที่จะอยู่นอกเวลาทำการ ถ้าวันนั้นเป็นวันหยุด (holiday = true) หรือวันธรรมดา (false)
    // ใช้เตือนก่อนเพิ่ม/ลบวันหยุด เช่น วันธรรมดามีคนจอง 20:00-21:00 ไว้ แต่วันหยุดปิด 20:00
    public ArrayList<Booking> findBookingsOutsideHours(LocalDate date, boolean holiday) {
        int open = openingHours.getOpenHour(holiday);
        int close = openingHours.getCloseHour(holiday);
        ArrayList<Booking> result = new ArrayList<Booking>();
        for (Booking b : searchBooking(date)) {
            if (b.getStartHour() < open || b.getEndHour() > close)
                result.add(b);
        }
        return result;
    }

    // การจองที่อยู่นอกเวลาทำการตามประเภทวันในตอนนี้
    public ArrayList<Booking> findBookingsOutsideHours(LocalDate date) {
        return findBookingsOutsideHours(date, openingHours.isHoliday(date));
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
