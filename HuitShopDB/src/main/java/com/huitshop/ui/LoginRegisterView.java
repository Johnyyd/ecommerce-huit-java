package com.huitshop.ui;

import com.huitshop.dto.AuthDtos.*;
import com.huitshop.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

public class LoginRegisterView extends StackPane {
    private final AuthService authService = new AuthService();
    private final Consumer<AuthResponseDto> onLoginSuccess;

    private VBox loginCard;
    private VBox registerCard;

    public LoginRegisterView(Consumer<AuthResponseDto> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        this.getStyleClass().add("root");
        this.setPadding(new Insets(20));

        // Create cards
        createLoginCard();
        createRegisterCard();

        // Load Login Card initially
        this.getChildren().add(loginCard);
    }

    private void createLoginCard() {
        loginCard = new VBox(15);
        loginCard.setMaxSize(400, 480);
        loginCard.getStyleClass().add("card-panel");
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(35));

        // Logo / Title
        Label titleLabel = new Label("E-COMMERCE HUIT");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        
        Label subLabel = new Label("Đăng nhập hệ thống quản lý & mua sắm");
        subLabel.getStyleClass().add("label-muted");

        // Fields
        VBox emailBox = new VBox(5);
        Label emailLabel = new Label("Email");
        TextField emailField = new TextField();
        emailField.setPromptText("example@gmail.com");
        emailBox.getChildren().addAll(emailLabel, emailField);

        VBox passBox = new VBox(5);
        Label passLabel = new Label("Mật khẩu");
        PasswordField passField = new PasswordField();
        passField.setPromptText("••••••••");
        passBox.getChildren().addAll(passLabel, passField);

        // Feedback Label
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#ef4444")); // Red-500
        errorLabel.setWrapText(true);
        errorLabel.setFont(Font.font("Segoe UI", 12));

        // Buttons
        Button loginBtn = new Button("ĐĂNG NHẬP");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.getStyleClass().add("button-primary");

        Hyperlink goToRegister = new Hyperlink("Chưa có tài khoản? Đăng ký ngay");
        goToRegister.setTextFill(Color.web("#38bdf8"));

        // Action Handlers
        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passField.getText().trim();
            
            if (email.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Vui lòng nhập đầy đủ email và mật khẩu.");
                return;
            }

            LoginDto dto = new LoginDto();
            dto.setEmail(email);
            dto.setPassword(password);

            try {
                AuthResponseDto response = authService.login(dto);
                if (response != null) {
                    onLoginSuccess.accept(response);
                } else {
                    errorLabel.setText("Email hoặc mật khẩu không chính xác.");
                }
            } catch (Exception ex) {
                errorLabel.setText("Có lỗi kết nối CSDL: " + ex.getMessage());
            }
        });

        goToRegister.setOnAction(e -> {
            this.getChildren().clear();
            this.getChildren().add(registerCard);
            errorLabel.setText("");
        });

        loginCard.getChildren().addAll(titleLabel, subLabel, new Separator(), emailBox, passBox, errorLabel, loginBtn, goToRegister);
    }

    private void createRegisterCard() {
        registerCard = new VBox(12);
        registerCard.setMaxSize(400, 560);
        registerCard.getStyleClass().add("card-panel");
        registerCard.setAlignment(Pos.CENTER);
        registerCard.setPadding(new Insets(30));

        Label titleLabel = new Label("TẠO TÀI KHOẢN");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.WHITE);

        Label subLabel = new Label("Đăng ký thành viên mua sắm HuitShop");
        subLabel.getStyleClass().add("label-muted");

        // Fields
        VBox nameBox = new VBox(4);
        Label nameLabel = new Label("Họ và tên");
        TextField nameField = new TextField();
        nameField.setPromptText("Nguyễn Văn A");
        nameBox.getChildren().addAll(nameLabel, nameField);

        VBox emailBox = new VBox(4);
        Label emailLabel = new Label("Email");
        TextField emailField = new TextField();
        emailField.setPromptText("example@gmail.com");
        emailBox.getChildren().addAll(emailLabel, emailField);

        VBox phoneBox = new VBox(4);
        Label phoneLabel = new Label("Số điện thoại");
        TextField phoneField = new TextField();
        phoneField.setPromptText("0987654321");
        phoneBox.getChildren().addAll(phoneLabel, phoneField);

        VBox passBox = new VBox(4);
        Label passLabel = new Label("Mật khẩu");
        PasswordField passField = new PasswordField();
        passField.setPromptText("••••••••");
        passBox.getChildren().addAll(passLabel, passField);

        // Feedback Label
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#ef4444"));
        errorLabel.setWrapText(true);
        errorLabel.setFont(Font.font("Segoe UI", 12));

        // Buttons
        Button registerBtn = new Button("ĐĂNG KÝ");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.getStyleClass().add("button-accent");

        Hyperlink goToLogin = new Hyperlink("Đã có tài khoản? Đăng nhập");
        goToLogin.setTextFill(Color.web("#38bdf8"));

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = passField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Họ tên, email và mật khẩu không được bỏ trống.");
                return;
            }

            RegisterDto dto = new RegisterDto();
            dto.setFullName(name);
            dto.setEmail(email);
            dto.setPhone(phone);
            dto.setPassword(password);

            try {
                AuthResponseDto response = authService.register(dto);
                if (response != null) {
                    onLoginSuccess.accept(response);
                } else {
                    errorLabel.setText("Đăng ký thất bại.");
                }
            } catch (Exception ex) {
                errorLabel.setText("Lỗi: " + ex.getMessage());
            }
        });

        goToLogin.setOnAction(e -> {
            this.getChildren().clear();
            this.getChildren().add(loginCard);
            errorLabel.setText("");
        });

        registerCard.getChildren().addAll(titleLabel, subLabel, new Separator(), nameBox, emailBox, phoneBox, passBox, errorLabel, registerBtn, goToLogin);
    }
}
