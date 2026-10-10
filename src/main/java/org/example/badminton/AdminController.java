package org.example.badminton;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

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
    @FXML private ListView<String> lstHolidays;
    @FXML private DatePicker dpHoliday;
    @FXML private Label lblHolidayResult;

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
        dpDaily.setValue(today);
        dpHoliday.setValue(today);

        for (int y = today.getYear() - 1; y <= today.getYear() + 1; y++)
            cboYear.getItems().add(y);
        for (int m = 1; m <= 12; m++)
            cboMonth.getItems().add(m);
        cboYear.setValue(today.getYear());
        cboMonth.setValue(today.getMonthValue());

        cboHistoryMember.valueProperty().addListener((obs, oldValue, newValue) -> showHistory());
        dpDaily.valueProperty().addListener((obs, oldValue, newValue) -> showDaily());
        cboYear.valueProperty().addListener((obs, oldValue, newValue) -> showMonthly());
        cboMonth.valueProperty().addListener((obs, oldValue, newValue) -> showMonthly());
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
        lstHolidays.getItems().clear();
        for (LocalDate d : center.getOpeningHours().getPublicHolidays())
            lstHolidays.getItems().add(center.getOpeningHours().describe(d));
    }

    @FXML
    protected void onAddHolidayButtonClick() {
        LocalDate date = dpHoliday.getValue();
        if (date == null) {
            FxHelper.showMessage(lblHolidayResult, "กรุณาเลือกวันที่", false);
            return;
        }
        if (center.getOpeningHours().isPublicHoliday(date)) {
            FxHelper.showMessage(lblHolidayResult, date + " เป็นวันหยุดอยู่แล้ว", false);
            return;
        }
        center.getOpeningHours().addPublicHoliday(date);
        showHolidays();
        FxHelper.showMessage(lblHolidayResult, "เพิ่มวันหยุด " + date + " แล้ว (เปิด 10:00 - 20:00 น.)", true);
    }
}
