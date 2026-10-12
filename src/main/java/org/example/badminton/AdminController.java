package org.example.badminton;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.ArrayList;

public class AdminController {

    @FXML private Label lblUser;

    // ----- แท็บประวัติการจอง -----
    @FXML private ComboBox<Member> cboHistoryMember;
    @FXML private Label lblHistorySummary;
    @FXML private TableView<Booking> tblHistory;

    // ----- แท็บรายได้รายวัน -----
    @FXML private DatePicker dpDaily;
    @FXML private VBox boxDailySummary;
    @FXML private TableView<Chargeable> tblDaily;

    // ----- แท็บรายได้รายเดือน -----
    @FXML private ComboBox<Integer> cboYear;
    @FXML private ComboBox<Integer> cboMonth;
    @FXML private VBox boxMonthlySummary;
    @FXML private StackedBarChart<String, Number> chartMonthly;

    // ----- แท็บวันหยุด -----
    @FXML private ListView<LocalDate> lstHolidays;
    @FXML private DatePicker dpHoliday;
    @FXML private Label lblHolidayResult;

    // ----- แท็บสต็อกอุปกรณ์ -----
    @FXML private TableView<Item> tblStock;
    @FXML private Label lblStockItem;
    @FXML private Spinner<Integer> spnRestock;
    @FXML private Label lblStockResult;

    private BadmintonApplication app;
    private BadmintonCenter center;
    private Admin admin;

    @FXML
    public void initialize() {
        LocalDate today = LocalDate.now();
        cboHistoryMember.setConverter(FxHelper.memberConverter());
        FxHelper.setupBookingTable(tblHistory);

        FxHelper.addColumn(tblDaily, "ประเภท", 120, c -> c.getCategory());
        FxHelper.addColumn(tblDaily, "จำนวนเงิน (บาท)", 120, c -> FxHelper.money(c.getTotal()));
        FxHelper.addColumn(tblDaily, "รายละเอียด", 600, c -> c.toString());
        tblDaily.setPlaceholder(new Label("ไม่มีรายได้ในวันนี้"));
        FxHelper.setupDatePicker(dpDaily);
        FxHelper.setupDatePicker(dpHoliday);
        lstHolidays.setCellFactory(TextFieldListCell.forListView(holidayConverter()));
        dpDaily.setValue(today);
        dpHoliday.setValue(today);

        for (int y = today.getYear() - 1; y <= today.getYear() + 1; y++)
            cboYear.getItems().add(y);
        for (int m = 1; m <= 12; m++)
            cboMonth.getItems().add(m);
        cboYear.setValue(today.getYear());
        cboMonth.setValue(today.getMonthValue());

        FxHelper.addColumn(tblStock, "รหัส", 60, item -> item.getCode());
        FxHelper.addColumn(tblStock, "ชื่ออุปกรณ์", 200, item -> item.getName());
        FxHelper.addColumn(tblStock, "ประเภท", 120, item -> item.getCategory());
        FxHelper.addColumn(tblStock, "ราคา (บาท)", 90, item -> FxHelper.money(item.getPrice()) + "/" + item.getUnit());
        FxHelper.addColumn(tblStock, "คงเหลือ/ว่างวันนี้", 200, item -> item.getStockInfo(LocalDate.now()));
        // แถวของที่หมดหรือเช่าเต็มวันนี้ แสดงเป็นสีแดง
        tblStock.setRowFactory(table -> new TableRow<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("row-full");
                if (!empty && item != null && !item.isAvailable(1, LocalDate.now()))
                    getStyleClass().add("row-full");
            }
        });

        cboHistoryMember.valueProperty().addListener((obs, oldValue, newValue) -> showHistory());
        dpDaily.valueProperty().addListener((obs, oldValue, newValue) -> showDaily());
        cboYear.valueProperty().addListener((obs, oldValue, newValue) -> showMonthly());
        cboMonth.valueProperty().addListener((obs, oldValue, newValue) -> showMonthly());
        tblStock.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> showStockItem());
    }

    public void setData(BadmintonApplication app, Admin admin) {
        this.app = app;
        this.center = app.getCenter();
        this.admin = admin;
        lblUser.setText(admin.getRole() + ": " + admin.getId() + " " + admin.getName());
        cboHistoryMember.setItems(FXCollections.observableArrayList(center.getMembers()));
        cboHistoryMember.getSelectionModel().selectFirst();
        showHistory();
        showDaily();
        showMonthly();
        showHolidays();
        tblStock.setItems(FXCollections.observableArrayList(center.getItems()));
        showStockItem();
    }

    @FXML
    protected void onLogoutButtonClick() {
        app.showLogin();
    }

    // ==================== ประวัติการจองของสมาชิก ====================
    private void showHistory() {
        Member m = cboHistoryMember.getValue();
        if (center == null || m == null)
            return;
        ArrayList<Booking> history = admin.viewBookingHistory(center, m.getId());
        tblHistory.setItems(FXCollections.observableArrayList(history));
        double total = 0;
        int packageHours = 0;
        for (Booking b : history) {
            total += b.getTotal();
            packageHours += b.getPackageHours();
        }
        lblHistorySummary.setText("จอง " + history.size() + " ครั้ง รวม " + m.getTotalBookedHours()
                + " ชม. (ใช้แพ็กเกจ " + packageHours + " ชม.) | ค่าสนามรวม " + FxHelper.money(total)
                + " บาท | ชั่วโมงสะสม " + m.getPaidHours() + " ชม. | แพ็กเกจเหลือ " + m.getPackageHoursLeft() + " ชม.");
    }

    // ==================== รายได้รายวัน ====================
    private void showDaily() {
        LocalDate date = dpDaily.getValue();
        if (center == null || date == null)
            return;
        ArrayList<Chargeable> list = admin.viewTransactions(center, date);
        showSummary(boxDailySummary, "รายได้ " + center.getOpeningHours().describe(date), list);
        tblDaily.setItems(FXCollections.observableArrayList(list));
    }

    // ==================== รายได้รายเดือน ====================
    private void showMonthly() {
        Integer year = cboYear.getValue();
        Integer month = cboMonth.getValue();
        if (center == null || year == null || month == null)
            return;
        ArrayList<Chargeable> list = admin.viewTransactions(center, year, month);
        showSummary(boxMonthlySummary, "รายได้เดือน " + month + "/" + year, list);

        // กราฟแท่งซ้อน: แต่ละวันแยกสีตามหมวดรายได้
        chartMonthly.getData().clear();
        int days = LocalDate.of(year, month, 1).lengthOfMonth();
        for (int i = 0; i < Admin.CATEGORIES.length; i++) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(Admin.CATEGORIES[i]);
            for (int day = 1; day <= days; day++) {
                ArrayList<Chargeable> dayList = admin.viewTransactions(center, LocalDate.of(year, month, day));
                series.getData().add(new XYChart.Data<>(String.valueOf(day), admin.sumCategory(dayList, Admin.CATEGORIES[i])));
            }
            chartMonthly.getData().add(series);
        }
    }

    // สรุปยอดแยกตามหมวด (ใช้ทั้งรายวันและรายเดือน)
    private void showSummary(VBox box, String title, ArrayList<Chargeable> list) {
        box.getChildren().clear();
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("section-title");
        lblTitle.setWrapText(true);
        box.getChildren().add(lblTitle);
        for (int i = 0; i < Admin.CATEGORIES.length; i++) {
            Label line = new Label(Admin.CATEGORIES[i] + " : " + FxHelper.money(admin.sumCategory(list, Admin.CATEGORIES[i])) + " บาท");
            box.getChildren().add(line);
        }
        Label total = new Label("รวมทั้งสิ้น " + FxHelper.money(admin.sumTotal(list)) + " บาท");
        total.getStyleClass().add("summary-total");
        box.getChildren().add(total);
        box.getChildren().add(new Label(list.size() + " รายการ"));
    }

    // ==================== วันหยุดนักขัตฤกษ์ ====================
    private void showHolidays() {
        lstHolidays.setItems(FXCollections.observableArrayList(center.getOpeningHours().getPublicHolidays()));
    }

    // แสดงวันหยุดพร้อมเวลาเปิด-ปิด และบอกถ้ามีการจองที่เกินเวลาทำการ
    private StringConverter<LocalDate> holidayConverter() {
        return new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date == null)
                    return "";
                String text = center.getOpeningHours().describe(date);
                int outside = center.findBookingsOutsideHours(date).size();
                if (outside > 0)
                    text += "  [มีการจองนอกเวลาทำการ " + outside + " รายการ]";
                return text;
            }

            @Override
            public LocalDate fromString(String text) {
                return null;
            }
        };
    }

    // รายการจองที่จะเกินเวลาทำการ แสดงในกล่องยืนยัน
    private String describeBookings(ArrayList<Booking> bookings) {
        String text = "";
        for (Booking b : bookings)
            text += "\n- " + b.getBookingId() + " " + b.getCourt() + " " + b.getTimeText() + " "
                    + b.getMember().getId() + " " + b.getMember().getName() + " (โทร " + b.getMember().getPhone() + ")";
        return text;
    }

    @FXML
    protected void onAddHolidayButtonClick() {
        LocalDate date = dpHoliday.getValue();
        if (date == null) {
            FxHelper.showMessage(lblHolidayResult, "กรุณาเลือกวันที่", false);
            return;
        }
        OpeningHours oh = center.getOpeningHours();
        if (oh.isPublicHoliday(date)) {
            FxHelper.showMessage(lblHolidayResult, date + " เป็นวันหยุดอยู่แล้ว", false);
            return;
        }
        ArrayList<Booking> affected = center.findBookingsOutsideHours(date, true);
        if (!affected.isEmpty() && !FxHelper.confirm("มีการจองที่จะเกินเวลาทำการ",
                "ถ้าให้ " + date + " เป็นวันหยุด ศูนย์จะเปิด " + oh.getOpenHour(true) + ":00 - "
                        + oh.getCloseHour(true) + ":00 น.\nการจอง " + affected.size()
                        + " รายการนี้จะอยู่นอกเวลาทำการ ควรติดต่อสมาชิกเพื่อเลื่อนเวลา:"
                        + describeBookings(affected) + "\n\nยืนยันเพิ่มวันหยุดหรือไม่?"))
            return;

        admin.addPublicHoliday(center, date);
        showHolidays();
        lstHolidays.getSelectionModel().select(date);
        String message = "เพิ่มวันหยุด " + date + " แล้ว (เปิด 10:00 - 20:00 น.)";
        if (!affected.isEmpty())
            message += " | มีการจองนอกเวลาทำการ " + affected.size() + " รายการ ต้องติดต่อสมาชิก";
        FxHelper.showMessage(lblHolidayResult, message, affected.isEmpty());
    }

    @FXML
    protected void onRemoveHolidayButtonClick() {
        LocalDate date = lstHolidays.getSelectionModel().getSelectedItem();
        if (date == null) {
            FxHelper.showMessage(lblHolidayResult, "กรุณาเลือกวันหยุดจากรายการที่ต้องการลบ", false);
            return;
        }
        // ถ้าตรงกับเสาร์-อาทิตย์ ลบแล้วก็ยังเป็นวันหยุดอยู่ เวลาเปิด-ปิดไม่เปลี่ยน
        OpeningHours oh = center.getOpeningHours();
        boolean holidayAfter = oh.isWeekend(date);
        ArrayList<Booking> affected = center.findBookingsOutsideHours(date, holidayAfter);
        String message = "ลบวันหยุด " + date + " หรือไม่?\nหลังลบ วันนั้นจะเปิด " + oh.getOpenHour(holidayAfter)
                + ":00 - " + oh.getCloseHour(holidayAfter) + ":00 น.";
        if (!affected.isEmpty())
            message += "\n\nการจอง " + affected.size() + " รายการนี้จะอยู่นอกเวลาทำการ ควรติดต่อสมาชิกเพื่อเลื่อนเวลา:"
                    + describeBookings(affected);
        if (!FxHelper.confirm("ยืนยันการลบวันหยุด", message))
            return;

        admin.removePublicHoliday(center, date);
        showHolidays();
        String result = "ลบวันหยุด " + date + " แล้ว";
        if (!affected.isEmpty())
            result += " | มีการจองนอกเวลาทำการ " + affected.size() + " รายการ ต้องติดต่อสมาชิก";
        FxHelper.showMessage(lblHolidayResult, result, affected.isEmpty());
    }

    // ==================== สต็อกอุปกรณ์ ====================
    private void showStockItem() {
        Item item = tblStock.getSelectionModel().getSelectedItem();
        if (item == null) {
            lblStockItem.setText("เลือกอุปกรณ์จากตารางด้านซ้าย");
            return;
        }
        lblStockItem.setText(item.getCode() + " " + item.getName() + " (" + item.getCategory() + ")\nวันนี้: "
                + item.getStockInfo(LocalDate.now()));
    }

    @FXML
    protected void onRestockButtonClick() {
        Item item = tblStock.getSelectionModel().getSelectedItem();
        if (item == null) {
            FxHelper.showMessage(lblStockResult, "กรุณาเลือกอุปกรณ์จากตาราง", false);
            return;
        }
        int quantity = spnRestock.getValue();
        String error = center.checkRestock(item.getCode(), quantity);
        if (error != null) {
            FxHelper.showMessage(lblStockResult, error, false);
            return;
        }
        if (!FxHelper.confirm("ยืนยันการเพิ่มจำนวน", item.getCode() + " " + item.getName() + "\nเพิ่ม " + quantity
                + " " + item.getUnit().replace("/วัน", "")))
            return;
        admin.restockItem(center, item.getCode(), quantity);
        tblStock.refresh();
        showStockItem();
        FxHelper.showMessage(lblStockResult, "เพิ่ม " + item.getName() + " " + quantity + " แล้ว | วันนี้: "
                + item.getStockInfo(LocalDate.now()), true);
    }
}
