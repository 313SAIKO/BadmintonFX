# BadmintonFX

โปรเจกต์ JavaFX + Maven สำหรับโจทย์ระบบจองสนามแบดมินตัน (แบบฝึกหัด OOP 2569)

## วิธีเปิด
1. เปิด IntelliJ IDEA แล้วเลือก File > Open > `C:\Users\khump\IdeaProjects\BadmintonFX`
2. รอ Maven โหลด dependencies (JavaFX 21.0.6)
3. เปิด `src/main/java/org/example/badminton/BadmintonApplication.java`
4. Run `BadmintonApplication`

หรือใช้ Terminal ที่โฟลเดอร์โปรเจกต์:

```powershell
mvn javafx:run
```


ข้อความ `WARNING: sun.misc.Unsafe ...` ตอนรันไม่ใช่ error เกิดจาก JavaFX 21 ทำงานบน Java รุ่นใหม่ ไม่มีผลกับโปรแกรม

## เข้าสู่ระบบ
- แคชเชียร์: `C01`, `C02`
- ผู้ดูแลระบบ: `A01`

## ลองใช้งาน
- **ส่วนลดชั่วโมงที่ 20:** เข้าระบบด้วย `C01` แล้วไปที่แท็บ "จองสนาม" เลือกสมาชิก M001 (มีชั่วโมงสะสม 18 ชม.) วันที่ 2026-10-12 สนาม 3 เวลา 18:00 จำนวน 3 ชม. จะเห็นยอด 414.00 บาท (180 + 54 + 180)
- **ตารางสนาม:** คลิกช่องสีเขียวในตารางเพื่อเลือกสนามและเวลา
- **แพ็กเกจ:** สมาชิก M002 มีแพ็กเกจเหลือ 13 ชม. ถ้าจองจะหักจากแพ็กเกจและไม่ต้องแนบสลิป
- **รายงาน:** เข้าระบบด้วย `A01` แท็บ "สรุปรายได้รายเดือน" จะมีกราฟรายได้แต่ละวัน แยกสีตามหมวด

## ไฟล์สำคัญ
คลาสของระบบ (เหมือนเวอร์ชัน console):
- `Person.java` (abstract) คลาสแม่ของ `Member` / `Cashier` / `Admin`
- `Item.java` (abstract) คลาสแม่ของ `RentalItem` / `SaleItem`
- `Chargeable.java` (interface) ใช้กับ `Booking` / `HourPackage` / `ItemOrder` ทุกอย่างที่ทำรายได้
- `Court.java`, `OpeningHours.java`, `PaymentSlip.java` ส่วนประกอบของระบบ
- `BadmintonCenter.java` เก็บข้อมูลทั้งหมดของศูนย์
- `SampleData.java` ข้อมูลตัวอย่าง

หน้าจอ JavaFX:
- `BadmintonApplication.java` จุดเริ่มต้นโปรแกรม และสลับหน้าจอ
- `login-view.fxml` + `LoginController.java` หน้าเข้าสู่ระบบ
- `cashier-view.fxml` + `CashierController.java` หน้าแคชเชียร์ มีแท็บจองสนาม สมาชิก แพ็กเกจ อุปกรณ์ และค้นหา
- `admin-view.fxml` + `AdminController.java` หน้าผู้ดูแลระบบ มีแท็บประวัติการจอง รายได้รายวัน/รายเดือน และวันหยุด
- `FxHelper.java` เมธอดช่วยที่ใช้ร่วมกัน เช่น สร้างคอลัมน์ตาราง และกล่องยืนยัน
- `style.css` สีและรูปแบบของหน้าจอ

Class diagram ของคลาสระบบอยู่ที่ `OneDrive\เดสก์ท็อป\OOP\Badminton\ClassDiagram.html`
