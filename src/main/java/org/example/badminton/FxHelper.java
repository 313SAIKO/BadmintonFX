package org.example.badminton;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Function;

// เมธอดช่วยที่หน้าจอแคชเชียร์และผู้ดูแลระบบใช้ร่วมกัน
public class FxHelper {

    public static String money(double amount) {
        return String.format("%,.2f", amount);
    }

    // เพิ่มคอลัมน์ให้ TableView โดยบอกว่าจะดึงข้อความจาก object แต่ละแถวอย่างไร
    public static <T> void addColumn(TableView<T> table, String title, double width, Function<T, String> getter) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue())));
        column.setPrefWidth(width);
        table.getColumns().add(column);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    // ให้ DatePicker แสดงวันที่เป็น ค.ศ. แบบ 2026-10-12 เหมือนส่วนอื่นของโปรแกรม
    public static void setupDatePicker(DatePicker picker) {
        picker.setChronology(IsoChronology.INSTANCE);
        picker.setPromptText("ปี-เดือน-วัน");
        picker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date == null)
                    return "";
                return date.toString();
            }

            @Override
            public LocalDate fromString(String text) {
                try {
                    return LocalDate.parse(text.trim());
                } catch (DateTimeParseException e) {
                    return picker.getValue();  // พิมพ์ผิดรูปแบบ ให้คงค่าเดิมไว้
                }
            }
        });
    }

    // ตารางการจอง ใช้ทั้งหน้าค้นหาของแคชเชียร์ และหน้าประวัติของผู้ดูแลระบบ
    public static void setupBookingTable(TableView<Booking> table) {
        addColumn(table, "รหัส", 70, b -> b.getBookingId());
        addColumn(table, "วันที่", 100, b -> b.getDate().toString());
        addColumn(table, "เวลา", 105, b -> b.getTimeText());
        addColumn(table, "สนาม", 70, b -> b.getCourt().toString());
        addColumn(table, "สมาชิก", 170, b -> b.getMember().getId() + " " + b.getMember().getName());
        addColumn(table, "ชม.", 45, b -> String.valueOf(b.getHours()));
        addColumn(table, "ค่าสนาม (บาท)", 110, b -> money(b.getTotal()));
        addColumn(table, "การชำระเงิน", 300, b -> paymentText(b));
        addColumn(table, "ผู้บันทึก", 75, b -> b.getCashier().getId());
        table.setPlaceholder(new Label("ไม่มีข้อมูลการจอง"));
    }

    private static String paymentText(Booking b) {
        String text = "";
        if (b.getSlip() != null)
            text = "สลิป " + b.getSlip().getSlipNo() + " (" + b.getSlip().getFileName() + ")";
        if (b.getPackageHours() > 0) {
            if (!text.isEmpty())
                text += " + ";
            text += "แพ็กเกจ " + b.getPackageHours() + " ชม.";
        }
        if (b.getDiscountHours() > 0)
            text += " | ลด 30% " + b.getDiscountHours() + " ชม.";
        return text;
    }

    // ให้ ComboBox แสดงสมาชิกเป็น "M001 สมชาย ใจดี"
    public static StringConverter<Member> memberConverter() {
        return new StringConverter<Member>() {
            @Override
            public String toString(Member m) {
                if (m == null)
                    return "";
                return m.getId() + " " + m.getName();
            }

            @Override
            public Member fromString(String text) {
                return null;
            }
        };
    }

    // ให้ ComboBox แสดงชั่วโมงเป็น "13:00"
    public static StringConverter<Integer> hourConverter() {
        return new StringConverter<Integer>() {
            @Override
            public String toString(Integer hour) {
                if (hour == null)
                    return "";
                return String.format("%02d:00", hour);
            }

            @Override
            public Integer fromString(String text) {
                return null;
            }
        };
    }

    public static void showMessage(Label label, String text, boolean success) {
        label.setText(text);
        label.getStyleClass().removeAll("success", "error");
        if (success)
            label.getStyleClass().add("success");
        else
            label.getStyleClass().add("error");
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.getDialogPane().getStylesheets().add(FxHelper.class.getResource("style.css").toExternalForm());
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
