package org.example.badminton;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

public class CashierController {

    @FXML private Label lblUser;
    @FXML private TabPane tabPane;
    @FXML private Tab tabBooking;
    @FXML private Tab tabItems;

    // ----- แท็บจองสนาม -----
    @FXML private TextField txtMemberSearch;
    @FXML private ListView<Member> lstMembers;
    @FXML private Label lblMemberInfo;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Integer> cboCourt;
    @FXML private ComboBox<Integer> cboStartHour;
    @FXML private Spinner<Integer> spnHours;
    @FXML private Label lblPrice;
    @FXML private VBox boxSlip;
    @FXML private TextField txtSlipNo;
    @FXML private TextField txtSlipFile;
    @FXML private Button btnBook;
    @FXML private Label lblBookResult;
    @FXML private Button btnRentForMember;
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
    @FXML private TableView<CartItem> tblCart;
    @FXML private ComboBox<String> cboCustomer;
    @FXML private DatePicker dpItemDate;
    @FXML private Label lblCartTotal;
    @FXML private Button btnCheckout;
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
    private FilteredList<Member> filteredMembers = new FilteredList<Member>(members);
    private ObservableList<CartItem> cart = FXCollections.observableArrayList();

    // ตารางสนาม: เก็บช่องไว้เพื่อเปลี่ยนสีตอนเลือก โดยไม่ต้องสร้างตารางใหม่ (ลากเมาส์ได้ไม่สะดุด)
    private Label[][] slotCells;    // [แถวชั่วโมง][หมายเลขสนาม]
    private int scheduleFirstHour;  // ชั่วโมงของแถวแรก (ปกติคือเวลาเปิด)
    private int scheduleOpenHour;
    private int scheduleCloseHour;
    private boolean selecting;      // กำลังตั้งค่าสนาม/เวลาจากตาราง ให้คำนวณราคาครั้งเดียวตอนตั้งค่าครบ
    private int dragCourt;
    private int dragStartHour;

    private Member bookingMember;   // สมาชิกที่กำลังจองให้ ใช้ตรวจว่าเปลี่ยนคนแล้วหรือยัง
    private boolean filtering;      // กำลังกรองรายชื่อ รายการที่เลือกอาจหายแล้วกลับมา ยังไม่ถือว่าเปลี่ยนคน
    private Member lastBookedMember;
    private LocalDate lastBookedDate;

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

        lstMembers.setItems(filteredMembers);
        lstMembers.setCellFactory(TextFieldListCell.forListView(FxHelper.memberConverter()));
        lstMembers.setPlaceholder(new Label("ไม่พบสมาชิก"));
        cboPackageMember.setItems(members);
        cboPackageMember.setConverter(FxHelper.memberConverter());

        // คอลัมน์เวลาแคบ คอลัมน์สนามขยายเต็มพื้นที่ ช่องจะใหญ่และคลิกง่าย
        gridSchedule.getColumnConstraints().add(new ColumnConstraints(56));
        for (int c = 1; c <= BadmintonCenter.COURT_COUNT; c++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setFillWidth(true);
            gridSchedule.getColumnConstraints().add(column);
        }

        FxHelper.addColumn(tblMembers, "รหัส", 70, m -> m.getId());
        FxHelper.addColumn(tblMembers, "ชื่อ-นามสกุล", 200, m -> m.getName());
        FxHelper.addColumn(tblMembers, "เบอร์โทร", 130, m -> m.getPhone());
        FxHelper.addColumn(tblMembers, "จองแล้ว (ครั้ง)", 110, m -> String.valueOf(m.getBookings().size()));
        FxHelper.addColumn(tblMembers, "ชั่วโมงสะสม", 110, m -> String.valueOf(m.getPaidHours()));
        FxHelper.addColumn(tblMembers, "แพ็กเกจคงเหลือ (ชม.)", 160, m -> String.valueOf(m.getPackageHoursLeft()));
        tblMembers.setItems(members);
        tblMembers.setRowFactory(table -> {
            TableRow<Member> row = new TableRow<Member>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    selectMemberForBooking(row.getItem());
            });
            return row;
        });

        FxHelper.addColumn(tblItems, "รหัส", 60, item -> item.getCode());
        FxHelper.addColumn(tblItems, "ชื่ออุปกรณ์", 200, item -> item.getName());
        FxHelper.addColumn(tblItems, "ประเภท", 120, item -> item.getCategory());
        FxHelper.addColumn(tblItems, "ราคา (บาท)", 90, item -> FxHelper.money(item.getPrice()));
        FxHelper.addColumn(tblItems, "หน่วย", 70, item -> item.getUnit());
        FxHelper.addColumn(tblItems, "จำนวน", 130, item -> item.getStockInfo());
        tblItems.setRowFactory(table -> {
            TableRow<Item> row = new TableRow<Item>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    addToCart(row.getItem(), spnQuantity.getValue());
            });
            return row;
        });

        FxHelper.addColumn(tblCart, "รายการ", 170, line -> line.getItem().getName());
        addCartQuantityColumn();
        FxHelper.addColumn(tblCart, "รวม (บาท)", 90, line -> FxHelper.money(line.getSubtotal()));
        tblCart.setPlaceholder(new Label("ตะกร้าว่าง\nดับเบิลคลิกอุปกรณ์ด้านซ้ายเพื่อเพิ่ม"));
        tblCart.setItems(cart);

        FxHelper.setupBookingTable(tblSearch);

        // เมื่อข้อมูลในฟอร์มเปลี่ยน ให้คำนวณราคาและระบายสีตารางสนามใหม่
        txtMemberSearch.textProperty().addListener((obs, oldValue, newValue) -> filterMembers(newValue));
        lstMembers.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (!filtering)
                onMemberSelected();
        });
        dpDate.valueProperty().addListener((obs, oldValue, newValue) -> {
            drawSchedule();
            updateStartHours();
            updateBookingForm();
        });
        cboCourt.valueProperty().addListener((obs, oldValue, newValue) -> onSlotFieldChanged());
        cboStartHour.valueProperty().addListener((obs, oldValue, newValue) -> onSlotFieldChanged());
        spnHours.valueProperty().addListener((obs, oldValue, newValue) -> onSlotFieldChanged());
        cboPackageMember.valueProperty().addListener((obs, oldValue, newValue) -> updatePackageInfo());
        tblItems.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateSelectedItem());
    }

    public void setData(BadmintonApplication app, Cashier cashier) {
        this.app = app;
        this.center = app.getCenter();
        this.cashier = cashier;
        lblUser.setText(cashier.getRole() + ": " + cashier.getId() + " " + cashier.getName());
        members.setAll(center.getMembers());
        updateCustomerNames();
        tblItems.setItems(FXCollections.observableArrayList(center.getItems()));
        drawSchedule();
        updateStartHours();
        updateBookingForm();
        updatePackageInfo();
        updateSelectedItem();
        updateCartTotal();
    }

    @FXML
    protected void onLogoutButtonClick() {
        app.showLogin();
    }

    // ==================== จองสนาม: เลือกสมาชิก ====================
    private Member getSelectedMember() {
        return lstMembers.getSelectionModel().getSelectedItem();
    }

    // ค้นจากรหัส ชื่อ หรือเบอร์โทร ถ้าเหลือคนเดียวให้เลือกให้อัตโนมัติ
    // ถ้าคนที่เลือกไว้ไม่อยู่ในผลค้นหา ให้ยกเลิกการเลือก (ไม่ให้ไปเลือกคนอื่นเอง)
    private void filterMembers(String text) {
        Member selected = getSelectedMember();
        String key = text.trim().toLowerCase();
        String phoneKey = key.replace("-", "");
        filtering = true;
        filteredMembers.setPredicate(m -> key.isEmpty()
                || m.getId().toLowerCase().contains(key)
                || m.getName().toLowerCase().contains(key)
                || (!phoneKey.isEmpty() && m.getPhone().replace("-", "").contains(phoneKey)));
        if (filteredMembers.size() == 1)
            lstMembers.getSelectionModel().select(filteredMembers.get(0));
        else if (selected != null && filteredMembers.contains(selected))
            lstMembers.getSelectionModel().select(selected);
        else
            lstMembers.getSelectionModel().clearSelection();
        filtering = false;
        onMemberSelected();
    }

    // เมื่อเปลี่ยนเป็นสมาชิกคนอื่น ล้างข้อมูลของคนก่อน (สลิป ผลการจอง ปุ่มเช่าอุปกรณ์)
    // ไม่ให้ข้อมูลของคนก่อนติดมากับการจองของคนล่าสุด
    private void onMemberSelected() {
        Member m = getSelectedMember();
        if (m != bookingMember) {
            bookingMember = m;
            txtSlipNo.clear();
            txtSlipFile.clear();
            lblBookResult.setText("");
            btnRentForMember.setVisible(false);
            btnRentForMember.setManaged(false);
        }
        updateBookingForm();
    }

    @FXML
    protected void onMemberSearchEnter() {
        if (getSelectedMember() == null && !filteredMembers.isEmpty())
            lstMembers.getSelectionModel().selectFirst();
    }

    private void selectMemberForBooking(Member m) {
        txtMemberSearch.clear();
        lstMembers.getSelectionModel().select(m);
        lstMembers.scrollTo(m);
        tabPane.getSelectionModel().select(tabBooking);
    }

    // ==================== จองสนาม: วันที่ ====================
    @FXML
    protected void onPrevDayButtonClick() {
        if (dpDate.getValue() != null)
            dpDate.setValue(dpDate.getValue().minusDays(1));
    }

    @FXML
    protected void onNextDayButtonClick() {
        if (dpDate.getValue() != null)
            dpDate.setValue(dpDate.getValue().plusDays(1));
    }

    @FXML
    protected void onTodayButtonClick() {
        dpDate.setValue(LocalDate.now());
    }

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

    // ==================== จองสนาม: ฟอร์มและราคา ====================
    private void onSlotFieldChanged() {
        if (!selecting)
            updateBookingForm();
    }

    private void updateBookingForm() {
        if (center == null)
            return;
        Member m = getSelectedMember();
        if (m == null) {
            lblMemberInfo.getStyleClass().setAll("label", "hint");
            lblMemberInfo.setText("ยังไม่ได้เลือกสมาชิก (ต้องเป็นสมาชิกจึงจะจองได้)");
        } else {
            lblMemberInfo.getStyleClass().setAll("label", "member-box");
            lblMemberInfo.setText("จองให้: " + m.getId() + " " + m.getName() + " (โทร " + m.getPhone() + ")\nชั่วโมงสะสม "
                    + m.getPaidHours() + " ชม. | แพ็กเกจเหลือ " + m.getPackageHoursLeft() + " ชม.");
        }
        updatePrice();
        paintSchedule();
    }

    private void updatePrice() {
        Member m = getSelectedMember();
        LocalDate date = dpDate.getValue();
        Integer courtNo = cboCourt.getValue();
        Integer startHour = cboStartHour.getValue();
        int hours = spnHours.getValue();
        lblPrice.getStyleClass().setAll("label", "price-box");
        btnBook.setText("ยืนยันการจอง");
        btnBook.setDisable(true);
        showSlipBox(true);

        if (m == null) {
            lblPrice.setText("ขั้นที่ 1: เลือกสมาชิกจากรายชื่อด้านบน");
            return;
        }
        if (date == null || courtNo == null || startHour == null) {
            lblPrice.setText("ขั้นที่ 2: คลิกช่องสีเขียวในตารางสนามด้านขวา เพื่อเลือกสนามและเวลา");
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
            text += " (ใช้แพ็กเกจทั้งหมด ไม่ต้องแนบสลิป)";
        lblPrice.setText(text);

        btnBook.setText("ยืนยันการจอง  " + FxHelper.money(fee) + " บาท");
        btnBook.setDisable(false);
        showSlipBox(fee > 0);
    }

    private void showSlipBox(boolean show) {
        boxSlip.setVisible(show);
        boxSlip.setManaged(show);
    }

    // ==================== จองสนาม: ตารางสนาม ====================
    // สร้างตารางใหม่ เมื่อเปลี่ยนวันหรือมีการจองเพิ่ม (แถว = ชั่วโมง, คอลัมน์ = สนาม 1-12)
    private void drawSchedule() {
        gridSchedule.getChildren().clear();
        slotCells = null;
        LocalDate date = dpDate.getValue();
        if (center == null || date == null)
            return;
        OpeningHours oh = center.getOpeningHours();
        scheduleOpenHour = oh.getOpenHour(date);
        scheduleCloseHour = oh.getCloseHour(date);

        // การจองที่อยู่นอกเวลาทำการ (เช่น ประกาศวันหยุดทีหลัง) ต้องยังเห็นในตาราง จึงขยายแถวให้ครอบคลุม
        scheduleFirstHour = scheduleOpenHour;
        int lastHour = scheduleCloseHour;
        ArrayList<Booking> outside = center.findBookingsOutsideHours(date);
        for (Booking b : outside) {
            scheduleFirstHour = Math.min(scheduleFirstHour, b.getStartHour());
            lastHour = Math.max(lastHour, b.getEndHour());
        }
        String title = oh.describe(date);
        if (!outside.isEmpty())
            title += "\nมีการจอง " + outside.size() + " รายการอยู่นอกเวลาทำการ (แถวสีเทา) กรุณาติดต่อสมาชิกเพื่อเลื่อนเวลา";
        lblScheduleTitle.setText(title);

        int rows = lastHour - scheduleFirstHour;
        slotCells = new Label[rows][BadmintonCenter.COURT_COUNT + 1];

        gridSchedule.add(createHeader("เวลา"), 0, 0);
        for (int c = 1; c <= BadmintonCenter.COURT_COUNT; c++)
            gridSchedule.add(createHeader("สนาม " + c), c, 0);

        for (int r = 0; r < rows; r++) {
            int hour = scheduleFirstHour + r;
            gridSchedule.add(createHeader(String.format("%02d:00", hour)), 0, r + 1);
            for (int c = 1; c <= BadmintonCenter.COURT_COUNT; c++) {
                Label cell = new Label();
                cell.setMaxWidth(Double.MAX_VALUE);
                Booking booking = findBookingAt(c, date, hour);
                if (booking != null) {
                    cell.setText(booking.getMember().getId());
                    cell.setTooltip(new Tooltip(booking.toString()));
                    cell.setUserData(booking);
                } else if (isClosedHour(hour)) {
                    cell.setTooltip(new Tooltip("นอกเวลาทำการ"));
                } else {
                    addSlotHandlers(cell, c, hour);
                }
                slotCells[r][c] = cell;
                gridSchedule.add(cell, c, r + 1);
            }
        }
        paintSchedule();
    }

    // เปลี่ยนสีช่องตามช่วงที่เลือกอยู่ (ไม่สร้างช่องใหม่)
    private void paintSchedule() {
        if (slotCells == null)
            return;
        Integer selectedCourt = cboCourt.getValue();
        Integer selectedStart = cboStartHour.getValue();
        int selectedHours = spnHours.getValue();
        for (int r = 0; r < slotCells.length; r++) {
            int hour = scheduleFirstHour + r;
            for (int c = 1; c <= BadmintonCenter.COURT_COUNT; c++) {
                Label cell = slotCells[r][c];
                boolean booked = cell.getUserData() != null;
                boolean selected = selectedCourt != null && selectedStart != null && c == selectedCourt
                        && hour >= selectedStart && hour < selectedStart + selectedHours;
                if (booked && selected)
                    cell.getStyleClass().setAll("label", "slot-conflict");
                else if (booked)
                    cell.getStyleClass().setAll("label", "slot-booked");
                else if (isClosedHour(hour))
                    cell.getStyleClass().setAll("label", "slot-closed");
                else if (selected)
                    cell.getStyleClass().setAll("label", "slot-selected");
                else
                    cell.getStyleClass().setAll("label", "slot-free");
                if (!booked) {
                    if (selected && !isClosedHour(hour))
                        cell.setText("✓");
                    else
                        cell.setText("");
                }
            }
        }
    }

    private boolean isClosedHour(int hour) {
        return hour < scheduleOpenHour || hour >= scheduleCloseHour;
    }

    private Label createHeader(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
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

    // คลิกเพื่อเลือก/ยกเลิกทีละช่อง หรือกดค้างแล้วลากขึ้นลงเพื่อเลือกหลายชั่วโมง
    private void addSlotHandlers(Label cell, int courtNo, int hour) {
        cell.setOnMousePressed(e -> {
            dragCourt = courtNo;
            dragStartHour = hour;
        });
        cell.setOnDragDetected(e -> {
            cell.startFullDrag();
            selectSlots(courtNo, hour, hour);
        });
        cell.setOnMouseDragEntered(e -> onSlotDragged(courtNo, hour));
        cell.setOnMouseClicked(e -> {
            if (e.isStillSincePress())
                onSlotClicked(courtNo, hour);
        });
    }

    private void onSlotClicked(int courtNo, int hour) {
        Integer court = cboCourt.getValue();
        Integer start = cboStartHour.getValue();
        if (court == null || start == null || court != courtNo) {
            selectSlots(courtNo, hour, hour);
            return;
        }
        int end = start + spnHours.getValue() - 1;  // ชั่วโมงสุดท้ายที่เลือกอยู่
        if (hour >= start && hour <= end) {
            // คลิกช่องที่เลือกไว้แล้ว = ยกเลิกช่องนั้น (ช่วงที่เลือกต้องติดกันเสมอ)
            if (start == end)
                cboCourt.setValue(null);
            else if (hour == start)
                selectSlots(courtNo, start + 1, end);
            else
                selectSlots(courtNo, start, hour - 1);
        } else {
            // คลิกช่องอื่นในสนามเดียวกัน = ขยายช่วงไปถึงช่องนั้น ถ้าระหว่างทางว่างทั้งหมด
            int from = Math.min(start, hour);
            int to = Math.max(end, hour);
            if (isSlotFree(courtNo, from, to))
                selectSlots(courtNo, from, to);
            else
                selectSlots(courtNo, hour, hour);
        }
    }

    private void onSlotDragged(int courtNo, int hour) {
        if (courtNo != dragCourt)
            return;
        int from = Math.min(dragStartHour, hour);
        int to = Math.max(dragStartHour, hour);
        if (isSlotFree(courtNo, from, to))
            selectSlots(courtNo, from, to);
    }

    private boolean isSlotFree(int courtNo, int fromHour, int toHour) {
        return center.isCourtAvailable(courtNo, dpDate.getValue(), fromHour, toHour - fromHour + 1);
    }

    private void selectSlots(int courtNo, int fromHour, int toHour) {
        selecting = true;
        cboCourt.setValue(courtNo);
        cboStartHour.setValue(fromHour);
        spnHours.getValueFactory().setValue(toHour - fromHour + 1);
        selecting = false;
        updateBookingForm();
    }

    // ==================== จองสนาม: ยืนยัน ====================
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
        Member m = getSelectedMember();
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
        lastBookedMember = m;
        lastBookedDate = date;
        btnRentForMember.setText("เช่า/ซื้ออุปกรณ์ให้ " + m.getName() + " ต่อ");
        btnRentForMember.setVisible(true);
        btnRentForMember.setManaged(true);

        txtSlipNo.clear();
        txtSlipFile.clear();
        cboCourt.setValue(null);
        drawSchedule();
        tblMembers.refresh();
        lstMembers.refresh();
        updateBookingForm();
        updatePackageInfo();
    }

    // ไปแท็บอุปกรณ์ โดยกรอกชื่อลูกค้าและวันที่ของการจองล่าสุดไว้ให้
    @FXML
    protected void onRentForMemberButtonClick() {
        if (lastBookedMember == null)
            return;
        cboCustomer.setValue(lastBookedMember.getName());
        dpItemDate.setValue(lastBookedDate);
        tabPane.getSelectionModel().select(tabItems);
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
        updateCustomerNames();
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

    // ==================== เช่า/ซื้ออุปกรณ์ (ตะกร้า) ====================
    private void updateCustomerNames() {
        cboCustomer.getItems().clear();
        for (Member m : members)
            cboCustomer.getItems().add(m.getName());
    }

    private void updateSelectedItem() {
        Item item = tblItems.getSelectionModel().getSelectedItem();
        if (item == null)
            lblSelectedItem.setText("ยังไม่ได้เลือกอุปกรณ์");
        else
            lblSelectedItem.setText("เลือก: " + item.getCode() + " " + item.getName() + " "
                    + FxHelper.money(item.getPrice()) + " บาท/" + item.getUnit());
    }

    // คอลัมน์จำนวนมีปุ่ม − / + แก้จำนวนได้ทันที (กด − ตอนเหลือ 1 = ลบออกจากตะกร้า)
    private void addCartQuantityColumn() {
        TableColumn<CartItem, CartItem> column = new TableColumn<CartItem, CartItem>("จำนวน");
        column.setCellValueFactory(cell -> new SimpleObjectProperty<CartItem>(cell.getValue()));
        column.setCellFactory(col -> new TableCell<CartItem, CartItem>() {
            private final Button btnMinus = new Button("−");
            private final Label lblQuantity = new Label();
            private final Button btnPlus = new Button("+");
            private final HBox box = new HBox(6.0, btnMinus, lblQuantity, btnPlus);

            {
                box.setAlignment(Pos.CENTER);
                btnMinus.getStyleClass().add("small-button");
                btnPlus.getStyleClass().add("small-button");
                btnMinus.setOnAction(e -> changeCartQuantity(getItem(), -1));
                btnPlus.setOnAction(e -> changeCartQuantity(getItem(), 1));
            }

            @Override
            protected void updateItem(CartItem line, boolean empty) {
                super.updateItem(line, empty);
                if (empty || line == null) {
                    setGraphic(null);
                    return;
                }
                lblQuantity.setText(String.valueOf(line.getQuantity()));
                if (line.getQuantity() == 1)
                    btnMinus.setText("ลบ");
                else
                    btnMinus.setText("−");
                setGraphic(box);
            }
        });
        column.setPrefWidth(130);
        column.setSortable(false);
        tblCart.getColumns().add(column);
    }

    // คืนค่า null ถ้ายังไม่มีอุปกรณ์นี้ในตะกร้า
    private CartItem findCartItem(Item item) {
        for (CartItem line : cart) {
            if (line.getItem() == item)
                return line;
        }
        return null;
    }

    private void addToCart(Item item, int quantity) {
        if (item == null) {
            FxHelper.showMessage(lblItemResult, "กรุณาเลือกอุปกรณ์จากตาราง", false);
            return;
        }
        CartItem line = findCartItem(item);
        int total = quantity;
        if (line != null)
            total += line.getQuantity();
        if (!item.isAvailable(total)) {
            FxHelper.showMessage(lblItemResult, "มีของไม่พอ: " + item, false);
            return;
        }
        if (line == null)
            cart.add(new CartItem(item, quantity));
        else
            line.setQuantity(total);
        spnQuantity.getValueFactory().setValue(1);
        tblCart.refresh();
        updateCartTotal();
        FxHelper.showMessage(lblItemResult, "เพิ่ม " + item.getName() + " x " + quantity + " ลงตะกร้าแล้ว", true);
    }

    private void changeCartQuantity(CartItem line, int change) {
        if (line == null)
            return;
        int quantity = line.getQuantity() + change;
        if (quantity <= 0) {
            cart.remove(line);
        } else if (!line.getItem().isAvailable(quantity)) {
            FxHelper.showMessage(lblItemResult, "มีของไม่พอ: " + line.getItem(), false);
            return;
        } else {
            line.setQuantity(quantity);
        }
        tblCart.refresh();
        updateCartTotal();
    }

    private void updateCartTotal() {
        double total = 0;
        for (CartItem line : cart)
            total += line.getSubtotal();
        lblCartTotal.setText(cart.size() + " รายการ | ยอดรวม " + FxHelper.money(total) + " บาท");
        if (cart.isEmpty()) {
            btnCheckout.setText("ยืนยันรายการ");
            btnCheckout.setDisable(true);
        } else {
            btnCheckout.setText("ยืนยันรายการ  " + FxHelper.money(total) + " บาท");
            btnCheckout.setDisable(false);
        }
    }

    @FXML
    protected void onAddToCartButtonClick() {
        addToCart(tblItems.getSelectionModel().getSelectedItem(), spnQuantity.getValue());
    }

    @FXML
    protected void onClearCartButtonClick() {
        cart.clear();
        updateCartTotal();
        lblItemResult.setText("");
    }

    @FXML
    protected void onCheckoutButtonClick() {
        String customer = cboCustomer.getEditor().getText().trim();
        LocalDate date = dpItemDate.getValue();
        if (customer.isEmpty() || date == null) {
            FxHelper.showMessage(lblItemResult, "กรุณากรอกชื่อลูกค้าและวันที่", false);
            return;
        }
        ArrayList<CartItem> lines = new ArrayList<CartItem>(cart);
        String error = center.checkItemOrders(lines);
        if (error != null) {
            FxHelper.showMessage(lblItemResult, error, false);
            return;
        }
        String detail = "";
        double total = 0;
        for (CartItem line : lines) {
            detail += line + "\n";
            total += line.getSubtotal();
        }
        detail += "ยอดรวม " + FxHelper.money(total) + " บาท\nลูกค้า: " + customer + " | วันที่ " + date;
        if (!FxHelper.confirm("ยืนยันรายการเช่า/ซื้อ", detail))
            return;

        ArrayList<ItemOrder> orders = cashier.sellItems(center, lines, date, customer);
        if (orders == null) {
            FxHelper.showMessage(lblItemResult, "บันทึกไม่สำเร็จ", false);
            return;
        }
        String ids = orders.get(0).getOrderId();
        if (orders.size() > 1)
            ids += " - " + orders.get(orders.size() - 1).getOrderId();
        FxHelper.showMessage(lblItemResult, "บันทึกสำเร็จ " + orders.size() + " รายการ (" + ids + ") | ยอดรวม "
                + FxHelper.money(total) + " บาท | ลูกค้า " + customer, true);
        cart.clear();
        cboCustomer.setValue(null);
        cboCustomer.getEditor().clear();
        tblItems.refresh();
        updateCartTotal();
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
