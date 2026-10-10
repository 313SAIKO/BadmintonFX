package org.example.badminton;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField txtStaffId;

    @FXML
    private Label lblError;

    private BadmintonApplication app;

    public void setApp(BadmintonApplication app) {
        this.app = app;
    }

    @FXML
    protected void onLoginButtonClick() {
        String id = txtStaffId.getText().trim();
        if (id.isEmpty()) {
            lblError.setText("กรุณากรอกรหัสพนักงาน");
            return;
        }

        Cashier cashier = app.findCashier(id);
        if (cashier != null) {
            app.showCashier(cashier);
            return;
        }

        Admin admin = app.findAdmin(id);
        if (admin != null) {
            app.showAdmin(admin);
            return;
        }

        lblError.setText("ไม่พบรหัสพนักงาน " + id);
    }
}
