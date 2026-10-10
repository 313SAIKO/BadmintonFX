module org.example.badminton {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.badminton to javafx.fxml;
    exports org.example.badminton;
}
