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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

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
    @FXML private Button btnClearSlots;
    @FXML private Label lblPrice;
    @FXML private VBox boxSlip;
    @FXML private TextField txtSlipNo;
    @FXML private TextField txtSlipAmount;
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
    @FXML private Label lblPackageDate;
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
    private LocalDateTime scheduleNow;  // เวลาตอนวาดตาราง ใช้ปิดช่องที่ผ่านไปแล้ว

    // ช่องที่เลือกไว้ ช่องละ 1 ชั่วโมง เลือกได้หลายสนามและหลายวัน ตอนจองจะรวมช่องที่ติดกันเป็นรายการเดียว
    private ArrayList<BookingItem> selectedSlots = new ArrayList<BookingItem>();
    private ArrayList<BookingItem> dragBase;  // ช่องที่เลือกไว้ก่อนเริ่มลาก
    private boolean dragAdding;               // ลากเพื่อเลือกเพิ่ม (true) หรือยกเลิก (false)
    private int dragCourt;
    private int dragStartHour;
    private BookingItem lastClickedSlot;      // ใช้กับ Shift+คลิก

    private Member bookingMember;   // สมาชิกที่กำลังจองให้ ใช้ตรวจว่าเปลี่ยนคนแล้วหรือยัง
    private boolean filtering;      // กำลังกรองรายชื่อ รายการที่เลือกอาจหายแล้วกลับมา ยังไม่ถือว่าเปลี่ยนคน
    private Member lastBookedMember;
    private LocalDate lastBookedDate;

    // ทำงานอัตโนมัติหลังโหลด FXML (ยังไม่มีข้อมูลระบบ)
    @FXML
    public void initialize() {
        for (int i = 1; i <= BadmintonCenter.COURT_COUNT; i++)
            cboSearchCourt.getItems().add(i);
        cboSearchCourt.getSelectionModel().selectFirst();

        FxHelper.setupDatePicker(dpDate);
        FxHelper.setupDatePicker(dpItemDate);
        FxHelper.disablePastDates(dpItemDate);
        FxHelper.setupDatePicker(dpSearchDate);
        LocalDate today = LocalDate.now();
        dpDate.setValue(today);
        lblPackageDate.setText(today + " (วันนี้ เปลี่ยนไม่ได้)");
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
        FxHelper.addColumn(tblItems, "ว่าง/คงเหลือ (ตามวันที่)", 170, item -> item.getStockInfo(getItemDate()));
        // แถวของอุปกรณ์ที่เต็มหรือหมดแล้วในวันที่เลือก แสดงเป็นสีแดง
        tblItems.setRowFactory(table -> {
            TableRow<Item> row = new TableRow<Item>() {
                @Override
                protected void updateItem(Item item, boolean empty) {
                    super.updateItem(item, empty);
                    getStyleClass().remove("row-full");
                    if (!empty && item != null && !item.isAvailable(1, getItemDate()))
                        getStyleClass().add("row-full");
                }
            };
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
            updateBookingForm();
        });
        cboPackageMember.valueProperty().addListener((obs, oldValue, newValue) -> updatePackageInfo());
        tblItems.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateSelectedItem());
        // เปลี่ยนวันที่เช่า: จำนวนว่างของของเช่าเปลี่ยนตามวัน ต้องตรวจตะกร้าใหม่
        dpItemDate.valueProperty().addListener((obs, oldValue, newValue) -> {
            tblItems.refresh();
            updateSelectedItem();
            updateCartTotal();
        });
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
            txtSlipAmount.clear();
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

    // ==================== จองสนาม: ฟอร์มและราคา ====================
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

    // รวมช่องที่เลือกซึ่งติดกัน (วันเดียวกัน สนามเดียวกัน ชั่วโมงต่อกัน) เป็นคำขอจอง 1 รายการ
    // แล้วเรียงตามวันและเวลา เพื่อคิดชั่วโมงสะสมและแพ็กเกจตามลำดับเวลาที่เล่น
    private ArrayList<BookingItem> buildBookingItems() {
        ArrayList<BookingItem> slots = new ArrayList<BookingItem>(selectedSlots);
        slots.sort(Comparator.comparing(BookingItem::getDate)
                .thenComparing(BookingItem::getCourtNo)
                .thenComparing(BookingItem::getStartHour));
        ArrayList<BookingItem> items = new ArrayList<BookingItem>();
        int i = 0;
        while (i < slots.size()) {
            BookingItem first = slots.get(i);
            int hours = 1;
            while (i + hours < slots.size() && isNextSlot(slots.get(i + hours - 1), slots.get(i + hours)))
                hours++;
            items.add(new BookingItem(first.getCourtNo(), first.getDate(), first.getStartHour(), hours));
            i += hours;
        }
        items.sort(Comparator.comparing(BookingItem::getDate)
                .thenComparing(BookingItem::getStartHour)
                .thenComparing(BookingItem::getCourtNo));
        return items;
    }

    private boolean isNextSlot(BookingItem a, BookingItem b) {
        return a.getDate().equals(b.getDate()) && a.getCourtNo() == b.getCourtNo()
                && a.getEndHour() == b.getStartHour();
    }

    private void updatePrice() {
        Member m = getSelectedMember();
        ArrayList<BookingItem> items = buildBookingItems();
        lblPrice.getStyleClass().setAll("label", "price-box");
        btnBook.setText("ยืนยันการจอง");
        btnBook.setDisable(true);
        btnClearSlots.setDisable(items.isEmpty());
        showSlipBox(false);  // แสดงช่องสลิปเมื่อรู้ยอดที่ต้องชำระแล้วเท่านั้น

        if (m == null) {
            lblPrice.setText("ขั้นที่ 1: เลือกสมาชิกจากรายชื่อด้านบน");
            return;
        }
        if (items.isEmpty()) {
            lblPrice.setText("ขั้นที่ 2: คลิกช่องสีเขียวในตารางสนามด้านขวา เลือกได้หลายสนาม และกด « » เพื่อเลือกวันอื่นเพิ่ม");
            return;
        }
        String error = center.checkBookings(m.getId(), items, LocalDateTime.now());
        if (error != null) {
            lblPrice.getStyleClass().setAll("label", "price-box-error");
            lblPrice.setText("จองไม่ได้: " + error);
            return;
        }

        double total = center.priceBookings(m, items, LocalDateTime.now());
        int totalHours = 0;
        int packageHours = 0;
        String text = "รายการจอง " + items.size() + " รายการ";
        for (int i = 0; i < items.size(); i++) {
            BookingItem item = items.get(i);
            text += "\n" + (i + 1) + ") " + item + " = " + FxHelper.money(item.getFee());
            if (item.isLateStart())
                text += " (ชม.แรกเริ่มไปแล้ว คิด " + Member.LATE_START_PERCENT + "% ไม่นับสะสม)";
            if (item.getPackageHours() > 0)
                text += " (แพ็กเกจ " + item.getPackageHours() + " ชม.)";
            if (item.getDiscountHours() > 0)
                text += " (ชั่วโมงที่ " + Member.DISCOUNT_EVERY + " ลดเหลือ " + Member.DISCOUNT_PERCENT + "%)";
            totalHours += item.getHours();
            packageHours += item.getPackageHours();
        }
        text += "\nรวม " + totalHours + " ชม.";
        if (packageHours > 0)
            text += " | แพ็กเกจเหลือหลังจอง " + (m.getPackageHoursLeft() - packageHours) + " ชม.";
        text += "\nยอดชำระ " + FxHelper.money(total) + " บาท";
        if (total == 0)
            text += " (ใช้แพ็กเกจทั้งหมด ไม่ต้องแนบสลิป)";
        lblPrice.setText(text);

        btnBook.setText("ยืนยันการจอง " + items.size() + " รายการ  " + FxHelper.money(total) + " บาท");
        btnBook.setDisable(false);
        showSlipBox(total > 0);
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
        scheduleNow = LocalDateTime.now();

        // การจองที่อยู่นอกเวลาทำการ (เช่น ประกาศวันหยุดทีหลัง) ต้องยังเห็นในตาราง จึงขยายแถวให้ครอบคลุม
        scheduleFirstHour = scheduleOpenHour;
        int lastHour = scheduleCloseHour;
        ArrayList<Booking> outside = center.findBookingsOutsideHours(date);
        for (Booking b : outside) {
            scheduleFirstHour = Math.min(scheduleFirstHour, b.getStartHour());
            lastHour = Math.max(lastHour, b.getEndHour());
        }
        String title = oh.describe(date);
        if (date.isBefore(scheduleNow.toLocalDate()))
            title += " (วันที่ผ่านมาแล้ว ดูได้อย่างเดียว จองย้อนหลังไม่ได้)";
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
                } else if (isPastHour(hour)) {
                    cell.setTooltip(new Tooltip("เลยเวลาแล้ว จองย้อนหลังไม่ได้"));
                } else {
                    if (center.isLateStart(date, hour, scheduleNow))
                        cell.setTooltip(new Tooltip("ชั่วโมงนี้เริ่มไปแล้ว " + scheduleNow.getMinute()
                                + " นาที จองได้ครึ่งราคา (ไม่นับชั่วโมงสะสม) ถึง "
                                + String.format("%02d:%02d", hour, BadmintonCenter.LATE_LIMIT_MINUTES)));
                    addSlotHandlers(cell, c, hour);
                }
                slotCells[r][c] = cell;
                gridSchedule.add(cell, c, r + 1);
            }
        }
        paintSchedule();
    }

    // เปลี่ยนสีช่องตามช่องที่เลือกไว้ (ไม่สร้างช่องใหม่)
    private void paintSchedule() {
        if (slotCells == null)
            return;
        LocalDate date = dpDate.getValue();
        for (int r = 0; r < slotCells.length; r++) {
            int hour = scheduleFirstHour + r;
            for (int c = 1; c <= BadmintonCenter.COURT_COUNT; c++) {
                Label cell = slotCells[r][c];
                boolean booked = cell.getUserData() != null;
                boolean unavailable = isClosedHour(hour) || isPastHour(hour);
                boolean selected = findSlot(c, date, hour) >= 0;
                if (selected && (booked || unavailable))
                    cell.getStyleClass().setAll("label", "slot-conflict");
                else if (booked)
                    cell.getStyleClass().setAll("label", "slot-booked");
                else if (unavailable)
                    cell.getStyleClass().setAll("label", "slot-closed");
                else if (selected)
                    cell.getStyleClass().setAll("label", "slot-selected");
                else
                    cell.getStyleClass().setAll("label", "slot-free");
                if (!booked) {
                    boolean late = !unavailable && center.isLateStart(date, hour, scheduleNow);
                    if (selected && !unavailable)
                        cell.setText("✓");
                    else if (late)
                        cell.setText("½ ราคา");  // ชั่วโมงที่เริ่มไปแล้ว ยังจองได้ครึ่งราคา
                    else
                        cell.setText("");
                }
            }
        }
    }

    private boolean isClosedHour(int hour) {
        return hour < scheduleOpenHour || hour >= scheduleCloseHour;
    }

    private boolean isPastHour(int hour) {
        return center.isPastTime(dpDate.getValue(), hour, scheduleNow);
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

    // คลิก = เลือก/ยกเลิกทีละช่อง, กดค้างแล้วลาก = เลือกหลายชั่วโมงในสนามเดียวกัน,
    // Shift+คลิก = เลือกต่อเนื่องจากช่องที่คลิกก่อนหน้า
    private void addSlotHandlers(Label cell, int courtNo, int hour) {
        cell.setOnMousePressed(e -> {
            dragCourt = courtNo;
            dragStartHour = hour;
            dragBase = null;
        });
        cell.setOnDragDetected(e -> {
            dragAdding = findSlot(courtNo, dpDate.getValue(), hour) < 0;
            dragBase = new ArrayList<BookingItem>(selectedSlots);
            setSlotRange(courtNo, hour, hour);
            cell.startFullDrag();
        });
        cell.setOnMouseDragEntered(e -> {
            if (dragBase != null && courtNo == dragCourt)
                setSlotRange(courtNo, dragStartHour, hour);
        });
        cell.setOnMouseClicked(e -> {
            if (!e.isStillSincePress())
                return;
            LocalDate date = dpDate.getValue();
            if (e.isShiftDown() && lastClickedSlot != null && lastClickedSlot.getCourtNo() == courtNo
                    && lastClickedSlot.getDate().equals(date)) {
                dragAdding = true;
                dragBase = new ArrayList<BookingItem>(selectedSlots);
                setSlotRange(courtNo, lastClickedSlot.getStartHour(), hour);
                dragBase = null;
            } else {
                toggleSlot(courtNo, hour);
            }
            lastClickedSlot = new BookingItem(courtNo, date, hour, 1);
        });
    }

    // คืนตำแหน่งในรายการที่เลือก หรือ -1 ถ้ายังไม่ได้เลือกช่องนี้
    private int findSlot(int courtNo, LocalDate date, int hour) {
        for (int i = 0; i < selectedSlots.size(); i++) {
            BookingItem s = selectedSlots.get(i);
            if (s.getCourtNo() == courtNo && s.getDate().equals(date) && s.getStartHour() == hour)
                return i;
        }
        return -1;
    }

    private void toggleSlot(int courtNo, int hour) {
        LocalDate date = dpDate.getValue();
        int index = findSlot(courtNo, date, hour);
        if (index >= 0)
            selectedSlots.remove(index);
        else
            selectedSlots.add(new BookingItem(courtNo, date, hour, 1));
        updateBookingForm();
    }

    // ตั้งช่วง fromHour-toHour ของสนามเดียวกัน โดยเริ่มจากช่องที่เลือกไว้ก่อนลาก (dragBase)
    // เริ่มลากจากช่องว่าง = เลือกเพิ่ม, เริ่มจากช่องที่เลือกไว้ = ยกเลิก
    private void setSlotRange(int courtNo, int fromHour, int toHour) {
        LocalDate date = dpDate.getValue();
        selectedSlots = new ArrayList<BookingItem>(dragBase);
        for (int h = Math.min(fromHour, toHour); h <= Math.max(fromHour, toHour); h++) {
            int index = findSlot(courtNo, date, h);
            if (dragAdding && index < 0 && isFreeSlot(courtNo, h))
                selectedSlots.add(new BookingItem(courtNo, date, h, 1));
            else if (!dragAdding && index >= 0)
                selectedSlots.remove(index);
        }
        updateBookingForm();
    }

    private boolean isFreeSlot(int courtNo, int hour) {
        return !isClosedHour(hour) && !isPastHour(hour)
                && center.isCourtAvailable(courtNo, dpDate.getValue(), hour, 1);
    }

    @FXML
    protected void onClearSlotsButtonClick() {
        selectedSlots.clear();
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
        ArrayList<BookingItem> items = buildBookingItems();
        if (m == null || items.isEmpty()) {
            FxHelper.showMessage(lblBookResult, "กรุณาเลือกสมาชิก และเลือกสนามกับเวลาจากตารางให้ครบ", false);
            return;
        }
        String error = center.checkBookings(m.getId(), items, LocalDateTime.now());
        if (error != null) {
            FxHelper.showMessage(lblBookResult, "จองไม่ได้: " + error, false);
            return;
        }

        double total = center.priceBookings(m, items, LocalDateTime.now());
        String slipNo = txtSlipNo.getText().trim();
        String slipFile = txtSlipFile.getText().trim();
        double slipAmount = 0;
        if (total > 0) {
            try {
                slipAmount = Double.parseDouble(txtSlipAmount.getText().trim().replace(",", ""));
            } catch (NumberFormatException e) {
                FxHelper.showMessage(lblBookResult, "กรุณากรอกยอดเงินในสลิปเป็นตัวเลข (ยอดที่ต้องชำระ "
                        + FxHelper.money(total) + " บาท)", false);
                return;
            }
        }
        error = center.checkSlip(slipNo, slipFile, slipAmount, total);
        if (error != null) {
            FxHelper.showMessage(lblBookResult, "สลิปไม่ถูกต้อง: " + error, false);
            return;
        }
        String confirmText = m.getId() + " " + m.getName() + "\n" + lblPrice.getText();
        if (total > 0)
            confirmText += "\nสลิป " + slipNo + " (" + slipFile + ") ยอด " + FxHelper.money(slipAmount) + " บาท";
        if (!FxHelper.confirm("ยืนยันการจอง", confirmText))
            return;

        ArrayList<Booking> booked = cashier.bookCourts(center, m.getId(), items, slipNo, slipFile, slipAmount);
        if (booked == null) {
            FxHelper.showMessage(lblBookResult, "จองไม่สำเร็จ", false);
            return;
        }
        String ids = booked.get(0).getBookingId();
        if (booked.size() > 1)
            ids += " - " + booked.get(booked.size() - 1).getBookingId();
        FxHelper.showMessage(lblBookResult, "จองสำเร็จ " + booked.size() + " รายการ (" + ids + ") | ค่าสนามรวม "
                + FxHelper.money(total) + " บาท", true);
        lastBookedMember = m;
        lastBookedDate = booked.get(0).getDate();
        btnRentForMember.setText("เช่า/ซื้ออุปกรณ์ให้ " + m.getName() + " ต่อ");
        btnRentForMember.setVisible(true);
        btnRentForMember.setManaged(true);

        txtSlipNo.clear();
        txtSlipAmount.clear();
        txtSlipFile.clear();
        selectedSlots.clear();
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
        if (m == null) {
            FxHelper.showMessage(lblPackageResult, "กรุณาเลือกสมาชิก", false);
            return;
        }
        String error = center.checkPackage(m.getId());
        if (error != null) {
            FxHelper.showMessage(lblPackageResult, error, false);
            return;
        }
        if (!FxHelper.confirm("ยืนยันการขายแพ็กเกจ", lblPackageInfo.getText() + "\nวันที่ขาย " + LocalDate.now()))
            return;
        // วันที่ขายคือวันนี้เสมอ ลงรายได้ย้อนหลังไม่ได้
        HourPackage p = cashier.sellPackage(center, m.getId());
        FxHelper.showMessage(lblPackageResult, "ขายแพ็กเกจสำเร็จ " + p.getPackageId() + " ให้ "
                + m.getId() + " " + m.getName() + " วันที่ " + p.getDate(), true);
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

    // วันที่เช่า/ซื้อ ใช้ดูจำนวนของเช่าที่ว่างในวันนั้น
    private LocalDate getItemDate() {
        if (dpItemDate.getValue() == null)
            return LocalDate.now();
        return dpItemDate.getValue();
    }

    private void updateSelectedItem() {
        Item item = tblItems.getSelectionModel().getSelectedItem();
        if (item == null)
            lblSelectedItem.setText("ยังไม่ได้เลือกอุปกรณ์");
        else
            lblSelectedItem.setText("เลือก: " + item.getCode() + " " + item.getName() + " "
                    + FxHelper.money(item.getPrice()) + " บาท/" + item.getUnit()
                    + " | " + getItemDate() + ": " + item.getStockInfo(getItemDate()));
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
        if (!item.isAvailable(total, getItemDate())) {
            FxHelper.showMessage(lblItemResult, "มีของไม่พอ: " + item.getName() + " วันที่ " + getItemDate()
                    + " " + item.getStockInfo(getItemDate()), false);
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
        } else if (!line.getItem().isAvailable(quantity, getItemDate())) {
            FxHelper.showMessage(lblItemResult, "มีของไม่พอ: " + line.getItem().getName() + " วันที่ " + getItemDate()
                    + " " + line.getItem().getStockInfo(getItemDate()), false);
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
        lblCartTotal.getStyleClass().setAll("label", "price-box");
        lblCartTotal.setText(cart.size() + " รายการ | ยอดรวม " + FxHelper.money(total) + " บาท");
        if (cart.isEmpty()) {
            btnCheckout.setText("ยืนยันรายการ");
            btnCheckout.setDisable(true);
            return;
        }
        // เปลี่ยนวันที่แล้วของเช่าวันนั้นอาจเต็ม ต้องตรวจตะกร้าใหม่
        String error = center.checkItemOrders(new ArrayList<CartItem>(cart), getItemDate(), LocalDateTime.now());
        if (error != null) {
            lblCartTotal.getStyleClass().setAll("label", "price-box-error");
            lblCartTotal.setText("ทำรายการไม่ได้: " + error);
            btnCheckout.setText("ยืนยันรายการ");
            btnCheckout.setDisable(true);
            return;
        }
        btnCheckout.setText("ยืนยันรายการ  " + FxHelper.money(total) + " บาท");
        btnCheckout.setDisable(false);
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
        String error = center.checkItemOrders(lines, date, LocalDateTime.now());
        if (error != null) {
            FxHelper.showMessage(lblItemResult, error, false);
            return;
        }
        String detail = "";
        double total = 0;
        boolean hasSaleItem = false;
        for (CartItem line : lines) {
            detail += line + "\n";
            total += line.getSubtotal();
            if (line.getItem() instanceof SaleItem)
                hasSaleItem = true;
        }
        detail += "ยอดรวม " + FxHelper.money(total) + " บาท\nลูกค้า: " + customer + " | วันที่เช่า/ใช้ " + date;
        if (hasSaleItem && !date.equals(LocalDate.now()))
            detail += "\n(ของขายบันทึกเป็นวันที่ขาย " + LocalDate.now() + ")";
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
