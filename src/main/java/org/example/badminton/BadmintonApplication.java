package org.example.badminton;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

// จุดเริ่มต้นโปรแกรม: สร้างข้อมูลของระบบ และสลับหน้าจอ เข้าสู่ระบบ / แคชเชียร์ / ผู้ดูแลระบบ
public class BadmintonApplication extends Application {
    private Stage stage;
    private BadmintonCenter center;
    private Cashier[] cashiers;
    private Admin[] admins;

    @Override
    public void start(Stage stage) {
        setup(stage);
        stage.show();
    }

    void setup(Stage stage) {
        this.stage = stage;
        center = new BadmintonCenter("ศูนย์แบดมินตัน");
        cashiers = new Cashier[2];
        cashiers[0] = new Cashier("C01", "สมหญิง ขยันงาน", "081-111-1111");
        cashiers[1] = new Cashier("C02", "สมปอง ตั้งใจ", "081-222-2222");
        admins = new Admin[1];
        admins[0] = new Admin("A01", "วิชัย ดูแลดี", "081-999-9999");
        SampleData.load(center, cashiers);

        stage.setTitle("ระบบจองสนามแบดมินตัน");
        showLogin();
    }

    public BadmintonCenter getCenter() {
        return center;
    }

    // คืนค่า null ถ้าไม่พบ
    public Cashier findCashier(String id) {
        for (int i = 0; i < cashiers.length; i++) {
            if (cashiers[i].getId().equalsIgnoreCase(id))
                return cashiers[i];
        }
        return null;
    }

    public Admin findAdmin(String id) {
        for (int i = 0; i < admins.length; i++) {
            if (admins[i].getId().equalsIgnoreCase(id))
                return admins[i];
        }
        return null;
    }

    public void showLogin() {
        FXMLLoader loader = changeScene("login-view.fxml", 520, 460);
        if (loader != null) {
            LoginController controller = loader.getController();
            controller.setApp(this);
        }
    }

    public void showCashier(Cashier cashier) {
        FXMLLoader loader = changeScene("cashier-view.fxml", 1240, 760);
        if (loader != null) {
            CashierController controller = loader.getController();
            controller.setData(this, cashier);
        }
    }

    public void showAdmin(Admin admin) {
        FXMLLoader loader = changeScene("admin-view.fxml", 1240, 760);
        if (loader != null) {
            AdminController controller = loader.getController();
            controller.setData(this, admin);
        }
    }

    private FXMLLoader changeScene(String fxml, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(BadmintonApplication.class.getResource(fxml));
            Scene scene = new Scene(loader.load(), width, height);
            scene.getStylesheets().add(BadmintonApplication.class.getResource("style.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
            return loader;
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "เปิดหน้าจอ " + fxml + " ไม่ได้: " + e.getMessage()).showAndWait();
            return null;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
