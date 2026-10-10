package org.example.badminton;

import java.time.LocalDate;

// ข้อมูลตัวอย่างสำหรับทดลองใช้งานระบบ
public class SampleData {

    public static void load(BadmintonCenter center, Cashier[] cashiers) {
        Cashier c1 = cashiers[0];
        Cashier c2 = cashiers[1];

        // อุปกรณ์ (Aggregation: สร้างข้างนอกแล้วส่งเข้า center)
        center.addItem(new RentalItem("R01", "รองเท้าแบดมินตัน (เช่า)", 20));
        center.addItem(new RentalItem("R02", "ไม้แบดมินตัน (เช่า)", 20));
        center.addItem(new SaleItem("S01", "ไม้แบดมินตัน", 890, 10));
        center.addItem(new SaleItem("S02", "ลูกขนไก่ (หลอด 12 ลูก)", 420, 30));
        center.addItem(new SaleItem("S03", "เทปพันด้ามไม้", 60, 50));

        // วันหยุดนักขัตฤกษ์ช่วงปลายปี 2569
        OpeningHours oh = center.getOpeningHours();
        oh.addPublicHoliday(LocalDate.of(2026, 10, 13));  // วันนวมินทรมหาราช
        oh.addPublicHoliday(LocalDate.of(2026, 10, 23));  // วันปิยมหาราช
        oh.addPublicHoliday(LocalDate.of(2026, 12, 7));   // ชดเชยวันพ่อแห่งชาติ
        oh.addPublicHoliday(LocalDate.of(2026, 12, 10));  // วันรัฐธรรมนูญ
        oh.addPublicHoliday(LocalDate.of(2026, 12, 31));  // วันสิ้นปี

        Member m1 = c1.registerMember(center, "สมชาย ใจดี", "089-123-4567");
        Member m2 = c1.registerMember(center, "สมศรี รักกีฬา", "086-765-4321");
        Member m3 = c2.registerMember(center, "John Smith", "082-222-3333");

        // M001 จองมาแล้ว 9 ครั้ง ครั้งละ 2 ชม. = 18 ชม. (ครั้งต่อไปจะได้ส่วนลดชั่วโมงที่ 20)
        for (int i = 0; i < 9; i++) {
            LocalDate date = LocalDate.of(2026, 9, 1).plusDays(i * 3);
            c1.bookCourt(center, m1.getId(), i + 1, date, 18, 2, "SL09" + (i + 1), "slip_m001_" + (i + 1) + ".jpg");
        }

        // M002 ซื้อแพ็กเกจแล้วใช้ไป 2 ชม. พร้อมเช่ารองเท้า
        c1.sellPackage(center, m2.getId(), LocalDate.of(2026, 10, 1));
        c2.bookCourt(center, m2.getId(), 5, LocalDate.of(2026, 10, 3), 10, 2, "", "");
        c2.sellItem(center, "R01", 2, LocalDate.of(2026, 10, 3), m2.getName());

        // M003 จองวันเสาร์ที่ 10 ต.ค. 2569 พร้อมเช่าไม้และซื้อลูกขนไก่
        c1.bookCourt(center, m3.getId(), 1, LocalDate.of(2026, 10, 10), 10, 2, "SL1010A", "slip_m003.jpg");
        c1.sellItem(center, "R02", 1, LocalDate.of(2026, 10, 10), m3.getName());
        c1.sellItem(center, "S02", 1, LocalDate.of(2026, 10, 10), m3.getName());
    }
}
