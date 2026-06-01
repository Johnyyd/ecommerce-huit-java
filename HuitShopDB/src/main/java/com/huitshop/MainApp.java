package com.huitshop;

import com.huitshop.dto.AuthDtos.AuthResponseDto;
import com.huitshop.ui.AdminPortal;
import com.huitshop.ui.CustomerPortal;
import com.huitshop.ui.LoginRegisterView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    private StackPane rootLayout;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("HuitShopDB E-Commerce Management System");

        rootLayout = new StackPane();
        
        // Show login register view initially
        showLoginView();

        Scene scene = new Scene(rootLayout, 1024, 700);
        // Load global CSS stylesheet
        try {
            scene.getStylesheets().add(getClass().getResource("/com/huitshop/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Style sheet load failed: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showLoginView() {
        LoginRegisterView loginView = new LoginRegisterView(this::handleLoginSuccess);
        rootLayout.getChildren().clear();
        rootLayout.getChildren().add(loginView);
    }

    private void handleLoginSuccess(AuthResponseDto response) {
        rootLayout.getChildren().clear();
        
        if ("ADMIN".equals(response.getRole()) || "STAFF".equals(response.getRole()) || "WAREHOUSE".equals(response.getRole())) {
            // Swap to Admin view
            AdminPortal adminView = new AdminPortal(response, this::showLoginView);
            rootLayout.getChildren().add(adminView);
            primaryStage.setTitle("HuitShopDB - Admin Panel (" + response.getFullName() + ")");
        } else {
            // Swap to Customer view
            CustomerPortal customerView = new CustomerPortal(response, this::showLoginView);
            rootLayout.getChildren().add(customerView);
            primaryStage.setTitle("HuitShopDB E-Store - " + response.getFullName());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
