package org.example.badminton;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

public class CashierController {

    @FXML private Label lblUser;

    // ----- แท็บจองสนาม -----
    @FXML private ComboBox<Member> cboMember;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Integer> cboCourt;
    @FXML private ComboBox<Integer> cboStartHour;
    @FXML private Spinner<Integer> spnHours;
    @FXML private Label lblMemberInfo;
    @FXML private Label lblPrice;
    @FXML private TextField txtSlipNo;
    @FXML private TextField txtSlipFile;
    @FXML private Label lblBookResult;
    @FXML private Label lblScheduleTitle;
    @FXML private GridPane gridSchedule;

    // ----- แท็บสมาชิก -----
    @FXML private TableView<Member> tblMembers;
    @FXML private TextField txtNewName;
    @FXML private TextField txtNewPhone;
    @FXML private Label lblRegisterResult;

    // ----- แท็บแพ็กเกจ -----
    @FXML private ComboBox<Member> cboPackageMember;
    @FXML private DatePicker dpPackageDate;
    @FXML private Label lblPackageInfo;
    @FXML private Label lblPackageResult;

    // ----- แท็บอุปกรณ์ -----
    @FXML private TableView<Item> tblItems;
    @FXML private Label lblSelectedItem;
    @FXML private Spinner<Integer> spnQuantity;
    @FXML private TextField txtCustomer;
    @FXML private DatePicker dpItemDate;
    @FXML private Label lblItemTotal;
    @FXML private Label lblItemResult;

    // ----- แท็บค้นหาการจอง -----
    @FXML private RadioButton rdoKeyword;
    @FXML private RadioButton rdoDate;
    @FXML private TextField txtKeyword;
    @FXML private ComboBox<Integer> cboSearchCourt;
    @FXML private DatePicker dpSearchDate;
    @FXML private Label lblSearchResult;
    @FXML private TableView<Booking> tblSearch;

    private BadmintonApplication app;
    private BadmintonCenter center;
    private Cashier cashier;
    private ObservableList<Member> members = FXCollections.observableArrayList();

    // ทำงานอัตโนมัติหลังโหลด FXML (ยังไม่มีข้อมูลระบบ)
    @FXML
    public void initialize() {
        for (int i = 1; i <= BadmintonCenter.COURT_COUNT; i++) {
            cboCourt.getItems().add(i);
            cboSearchCourt.getItems().add(i);
        }
        cboSearchCourt.getSelectionModel().selectFirst();
        cboStartHour.setConverter(FxHelper.hourConverter());

        FxHelper.setupDatePicker(dpDate);
        FxHelper.setupDatePicker(dpPackageDate);
        FxHelper.setupDatePicker(dpItemDate);
        FxHelper.setupDatePicker(dpSearchDate);
        LocalDate today = LocalDate.now();
        dpDate.setValue(today);
        dpPackageDate.setValue(today);
        dpItemDate.setValue(today);
        dpSearchDate.setValue(today);

        cboMember.setItems(members);
        cboPackageMember.setItems(members);
        cboMember.setConverter(FxHelper.memberConverter());
        cboPackageMember.setConverter(FxHelper.memberConverter());

        FxHelper.addColumn(tblMembers, "รหัส", 70, m -> m.getId());
        FxHelper.addColumn(tblMembers, "ชื่อ-นามสกุล", 200, m -> m.getName());
        FxHelper.addColumn(tblMembers, "เบอร์โทร", 130, m -> m.getPhone());
        FxHelper.addColumn(tblMembers, "จองแล้ว (ครั้ง)", 110, m -> String.valueOf(m.getBookings().size()));
        FxHelper.addColumn(tblMembers, "ชั่วโมงสะสม", 110, m -> String.valueOf(m.getPaidHours()));
        FxHelper.addColumn(tblMembers, "แพ็กเกจคงเหลือ (ชม.)", 160, m -> String.valueOf(m.getPackageHoursLeft()));
        tblMembers.setItems(members);

        FxHelper.addColumn(tblItems, "รหัส", 60, item -> item.getCode());
        FxHelper.addColumn(tblItems, "ชื่ออุปกรณ์", 200, item -> item.getName());
        FxHelper.addColumn(tblItems, "ประเภท", 120, item -> item.getCategory());
        FxHelper.addColumn(tblItems, "ราคา (บาท)", 90, item -> FxHelper.money(item.getPrice()));
        FxHelper.addColumn(tblItems, "หน่วย", 70, item -> item.getUnit());
        FxHelper.addColumn(tblItems, "จำนวน", 130, item -> item.getStockInfo());

        FxHelper.setupBookingTable(tblSearch);

        // เมื่อข้อมูลในฟอร์มเปลี่ยน ให้คำนวณราคาและวาดตารางสนามใหม่
        cboMember.valueProperty().addListener((obs, oldValue, newValue) -> updateBookingForm());
        dpDate.valueProperty().addListener((obs, oldValue, newValue) -> {
            updateStartHours();
            updateBookingForm();
        });
        cboCourt.valueProperty().addListener((obs, oldValue, newValue) -> updateBookingForm());
        cboStartHour.valueProperty().addListener((obs, oldValue, newValue) -> updateBookingForm());
        spnHours.valueProperty().addListener((obs, oldValue, newValue) -> updateBookingForm());
        cboPackageMember.valueProperty().addListener((obs, oldValue, newValue) -> updatePackageInfo());
        tblItems.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateItemTotal());
        spnQuantity.valueProperty().addListener((obs, oldValue, newValue) -> updateItemTotal());
    }

    public void setData(BadmintonApplication app, Cashier cashier) {
        this.app = app;
        this.center = app.getCenter();
        this.cashier = cashier;
        lblUser.setText(cashier.getRole() + ": " + cashier.getId() + " " + cashier.getName());
        members.setAll(center.getMembers());
        tblItems.setItems(FXCollections.observableArrayList(center.getItems()));
        updateStartHours();
        updateBookingForm();
        updatePackageInfo();
        updateItemTotal();
    }

    @FXML
    protected void onLogoutButtonClick() {
        app.showLogin();
    }

    // ==================== จองสนาม ====================
    private void updateStartHours() {
        LocalDate date = dpDate.getValue();
        if (center == null || date == null)
            return;
        OpeningHours oh = center.getOpeningHours();
        Integer oldHour = cboStartHour.getValue();
        cboStartHour.getItems().clear();
        for (int h = oh.getOpenHour(date); h < oh.getCloseHour(date); h++)
            cboStartHour.getItems().add(h);
        if (oldHour != null && cboStartHour.getItems().contains(oldHour))
            cboStartHour.setValue(oldHour);
        else
            cboStartHour.getSelectionModel().selectFirst();
    }

    private void updateBookingForm() {
        if (center == null)
            return;
        Member m = cboMember.getValue();
        if (m == null)
            lblMemberInfo.setText("เลือกสมาชิกที่ต้องการจอง (ต้องเป็นสมาชิกจึงจะจองได้)");
        else
            lblMemberInfo.setText(m.getId() + " " + m.getName() + " | ชั่วโมงสะสม " + m.getPaidHours()
                    + " ชม. | แพ็กเกจเหลือ " + m.getPackageHoursLeft() + " ชม.");
        updatePrice();
        drawSchedule();
    }

    private void updatePrice() {
        Member m = cboMember.getValue();
        LocalDate date = dpDate.getValue();
        Integer courtNo = cboCourt.getValue();
        Integer startHour = cboStartHour.getValue();
        int hours = spnHours.getValue();
        lblPrice.getStyleClass().setAll("label", "price-box");

        if (m == null || date == null || courtNo == null || startHour == null) {
            lblPrice.setText("เลือกสมาชิก วันที่ สนาม และเวลา เพื่อดูค่าบริการ\n(คลิกช่องสีเขียวในตารางด้านขวาได้)");
            return;
        }
        String error = center.checkBooking(m.getId(), courtNo, date, startHour, hours);
        if (error != null) {
            lblPrice.getStyleClass().setAll("label", "price-box-error");
            lblPrice.setText("จองไม่ได้: " + error);
            return;
        }

        int packageHours = m.countPackageHours(hours);
        int discountHours = m.countDiscountHours(hours);
        int normalHours = hours - packageHours - discountHours;
        double discountPrice = Court.PRICE_PER_HOUR * Member.DISCOUNT_PERCENT / 100;
        double fee = m.calculateCourtFee(hours, Court.PRICE_PER_HOUR);

        String text = "สนาม " + courtNo + " | " + date + " | "
                + String.format("%02d:00-%02d:00", startHour, startHour + hours) + " (" + hours + " ชม.)";
        if (packageHours > 0)
            text += "\nใช้ชั่วโมงแพ็กเกจ " + packageHours + " ชม. (เหลือ "
                    + (m.getPackageHoursLeft() - packageHours) + " ชม.)";
        if (normalHours > 0)
            text += "\nราคาปกติ " + normalHours + " ชม. x " + FxHelper.money(Court.PRICE_PER_HOUR)
                    + " = " + FxHelper.money(normalHours * Court.PRICE_PER_HOUR) + " บาท";
        if (discountHours > 0)
            text += "\nชั่วโมงที่ " + Member.DISCOUNT_EVERY + " ลดเหลือ " + Member.DISCOUNT_PERCENT + "%: "
                    + discountHours + " ชม. x " + FxHelper.money(discountPrice)
                    + " = " + FxHelper.money(discountHours * discountPrice) + " บาท";
        text += "\nยอดชำระ " + FxHelper.money(fee) + " บาท";
        if (fee == 0)
            text += " (ไม่ต้องแนบสลิป)";
        lblPrice.setText(text);
    }

    // ตารางสนาม: แถว = ชั่วโมง, คอลัมน์ = สนาม 1-12
    private void drawSchedule() {
        gridSchedule.getChildren().clear();
        LocalDate date = dpDate.getValue();
        if (date == null)
            return;
        OpeningHours oh = center.getOpeningHours();
        lblScheduleTitle.setText("ตารางสนาม " + oh.describe(date));

        Integer selectedCourt = cboCourt.getValue();
        Integer selectedStart = cboStartHour.getValue();
        int selectedHours = spnHours.getValue();

        gridSchedule.add(createHeader("เวลา"), 0, 0);
        for (int c = 1; c <= BadmintonCenter.COURT_COUNT; c++)
            gridSchedule.add(createHeader("สนาม " + c), c, 0);

        int row = 1;
        for (int h = oh.getOpenHour(date); h < oh.getCloseHour(date); h++) {
            gridSchedule.add(createHeader(String.format("%02d:00", h)), 0, row);
            for (int c = 1; c <= BadmintonCenter.COURT_COUNT; c++) {
                Booking booking = findBookingAt(c, date, h);
                boolean selected = selectedCourt != null && selectedStart != null && c == selectedCourt
                        && h >= selectedStart && h < selectedStart + selectedHours;
                Label cell = new Label();
                if (booking != null) {
                    cell.setText(booking.getMember().getId());
                    cell.setTooltip(new Tooltip(booking.toString()));
                    if (selected)
                        cell.getStyleClass().add("slot-conflict");
                    else
                        cell.getStyleClass().add("slot-booked");
                } else {
                    if (selected)
                        cell.getStyleClass().add("slot-selected");
                    else
                        cell.getStyleClass().add("slot-free");
                    final int courtNo = c;
                    final int hour = h;
                    cell.setOnMouseClicked(e -> {
                        cboCourt.setValue(courtNo);
                        cboStartHour.setValue(hour);
                    });
                }
                gridSchedule.add(cell, c, row);
            }
            row++;
        }
    }

    private Label createHeader(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("slot-header");
        return label;
    }

    // คืนค่า null ถ้าชั่วโมงนั้นว่าง
    private Booking findBookingAt(int courtNo, LocalDate date, int hour) {
        for (Booking b : center.searchBooking(courtNo, date)) {
            if (hour >= b.getStartHour() && hour < b.getEndHour())
                return b;
        }
        return null;
    }

    @FXML
    protected void onChooseSlipButtonClick() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("เลือกไฟล์สลิปการชำระเงิน");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("รูปภาพหรือ PDF", "*.png", "*.jpg", "*.jpeg", "*.pdf"));
        File file = chooser.showOpenDialog(txtSlipFile.getScene().getWindow());
        if (file != null)
            txtSlipFile.setText(file.getName());
    }

    @FXML
    protected void onBookButtonClick() {
        Member m = cboMember.getValue();
        LocalDate date = dpDate.getValue();
        Integer courtNo = cboCourt.getValue();
        Integer startHour = cboStartHour.getValue();
        int hours = spnHours.getValue();
        if (m == null || date == null || courtNo == null || startHour == null) {
            FxHelper.showMessage(lblBookResult, "กรุณาเลือกสมาชิก วันที่ สนาม และเวลาให้ครบ", false);
            return;
        }
        String error = center.checkBooking(m.getId(), courtNo, date, startHour, hours);
        if (error != null) {
            FxHelper.showMessage(lblBookResult, "จองไม่ได้: " + error, false);
            return;
        }

        double fee = m.calculateCourtFee(hours, Court.PRICE_PER_HOUR);
        String slipNo = txtSlipNo.getText().trim();
        String slipFile = txtSlipFile.getText().trim();
        if (fee > 0 && (slipNo.isEmpty() || slipFile.isEmpty())) {
            FxHelper.showMessage(lblBookResult, "มียอดชำระ " + FxHelper.money(fee)
                    + " บาท กรุณากรอกเลขที่สลิปและแนบไฟล์สลิป", false);
            return;
        }
        if (!FxHelper.confirm("ยืนยันการจอง", m.getId() + " " + m.getName() + "\n" + lblPrice.getText()))
            return;

        Booking b = cashier.bookCourt(center, m.getId(), courtNo, date, startHour, hours, slipNo, slipFile);
        if (b == null) {
            FxHelper.showMessage(lblBookResult, "จองไม่สำเร็จ", false);
            return;
        }
        FxHelper.showMessage(lblBookResult, "จองสำเร็จ " + b.getBookingId() + " | " + b.getCourt() + " "
                + b.getDate() + " " + b.getTimeText() + " | ค่าสนาม " + FxHelper.money(b.getTotal()) + " บาท", true);
        txtSlipNo.clear();
        txtSlipFile.clear();
        cboCourt.setValue(null);
        tblMembers.refresh();
        updateBookingForm();
        updatePackageInfo();
    }

    // ==================== สมาชิก ====================
    @FXML
    protected void onRegisterButtonClick() {
        String name = txtNewName.getText().trim();
        String phone = txtNewPhone.getText().trim();
        if (name.isEmpty() || phone.isEmpty()) {
            FxHelper.showMessage(lblRegisterResult, "กรุณากรอกชื่อและเบอร์โทร", false);
            return;
        }
        Member m = cashier.registerMember(center, name, phone);
        members.add(m);
        txtNewName.clear();
        txtNewPhone.clear();
        FxHelper.showMessage(lblRegisterResult, "สมัครสมาชิกสำเร็จ: " + m.getId() + " " + m.getName(), true);
    }

    // ==================== แพ็กเกจ ====================
    private void updatePackageInfo() {
        if (center == null)
            return;
        Member m = cboPackageMember.getValue();
        lblPackageInfo.getStyleClass().setAll("label", "price-box");
        if (m == null) {
            lblPackageInfo.setText("เลือกสมาชิกที่ต้องการซื้อแพ็กเกจ");
            return;
        }
        String error = center.checkPackage(m.getId());
        if (error != null) {
            lblPackageInfo.getStyleClass().setAll("label", "price-box-error");
            lblPackageInfo.setText(error);
            return;
        }
        lblPackageInfo.setText(m.getId() + " " + m.getName() + "\nแพ็กเกจ " + HourPackage.TOTAL_HOURS
                + " ชั่วโมง ราคา " + FxHelper.money(HourPackage.PRICE) + " บาท\nใช้แทนค่าสนามได้ "
                + HourPackage.TOTAL_HOURS + " ชั่วโมง ไม่มีวันหมดอายุ");
    }

    @FXML
    protected void onSellPackageButtonClick() {
        Member m = cboPackageMember.getValue();
        LocalDate date = dpPackageDate.getValue();
        if (m == null || date == null) {
            FxHelper.showMessage(lblPackageResult, "กรุณาเลือกสมาชิกและวันที่", false);
            return;
        }
        String error = center.checkPackage(m.getId());
        if (error != null) {
            FxHelper.showMessage(lblPackageResult, error, false);
            return;
        }
        if (!FxHelper.confirm("ยืนยันการขายแพ็กเกจ", lblPackageInfo.getText()))
            return;
        HourPackage p = cashier.sellPackage(center, m.getId(), date);
        FxHelper.showMessage(lblPackageResult, "ขายแพ็กเกจสำเร็จ " + p.getPackageId() + " ให้ "
                + m.getId() + " " + m.getName(), true);
        tblMembers.refresh();
        updatePackageInfo();
        updateBookingForm();
    }

    // ==================== เช่า/ซื้ออุปกรณ์ ====================
    private void updateItemTotal() {
        Item item = tblItems.getSelectionModel().getSelectedItem();
        if (item == null) {
            lblSelectedItem.setText("เลือกอุปกรณ์จากตารางด้านซ้าย");
            lblItemTotal.setText("ยอดรวม 0.00 บาท");
            return;
        }
        int quantity = spnQuantity.getValue();
        lblSelectedItem.setText(item.getCode() + " " + item.getName() + " (" + item.getCategory() + ")");
        lblItemTotal.setText(item.getName() + " x " + quantity + " " + item.getUnit()
                + "\nยอดรวม " + FxHelper.money(item.calculatePrice(quantity)) + " บาท");
    }

    @FXML
    protected void onSellItemButtonClick() {
        Item item = tblItems.getSelectionModel().getSelectedItem();
        String customer = txtCustomer.getText().trim();
        LocalDate date = dpItemDate.getValue();
        int quantity = spnQuantity.getValue();
        if (item == null || customer.isEmpty() || date == null) {
            FxHelper.showMessage(lblItemResult, "กรุณาเลือกอุปกรณ์ กรอกชื่อลูกค้า และวันที่", false);
            return;
        }
        String error = center.checkItemOrder(item.getCode(), quantity);
        if (error != null) {
            FxHelper.showMessage(lblItemResult, error, false);
            return;
        }
        if (!FxHelper.confirm("ยืนยันรายการ", lblItemTotal.getText() + "\nลูกค้า: " + customer))
            return;
        ItemOrder order = cashier.sellItem(center, item.getCode(), quantity, date, customer);
        FxHelper.showMessage(lblItemResult, "บันทึกสำเร็จ " + order.getOrderId() + " | "
                + FxHelper.money(order.getTotal()) + " บาท", true);
        txtCustomer.clear();
        tblItems.refresh();
        updateItemTotal();
    }

    // ==================== ค้นหาการจอง ====================
    @FXML
    protected void onSearchButtonClick() {
        ArrayList<Booking> result;
        if (rdoKeyword.isSelected()) {
            if (txtKeyword.getText().trim().isEmpty()) {
                FxHelper.showMessage(lblSearchResult, "กรุณากรอกคำค้นหา", false);
                return;
            }
            result = cashier.searchBooking(center, txtKeyword.getText());
        } else {
            LocalDate date = dpSearchDate.getValue();
            if (date == null) {
                FxHelper.showMessage(lblSearchResult, "กรุณาเลือกวันที่", false);
                return;
            }
            if (rdoDate.isSelected())
                result = cashier.searchBooking(center, date);
            else
                result = cashier.searchBooking(center, cboSearchCourt.getValue(), date);
        }
        tblSearch.setItems(FXCollections.observableArrayList(result));
        FxHelper.showMessage(lblSearchResult, "พบ " + result.size() + " รายการ", true);
    }
}
