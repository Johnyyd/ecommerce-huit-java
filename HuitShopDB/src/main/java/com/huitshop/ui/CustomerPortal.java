package com.huitshop.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huitshop.dto.AuthDtos.AuthResponseDto;
import com.huitshop.dto.InventoryDtos.InventoryDto;
import com.huitshop.dto.OrderDtos.*;
import com.huitshop.dto.ProductDtos.*;
import com.huitshop.dao.VoucherDao;
import com.huitshop.model.Cart;
import com.huitshop.model.CartItem;
import com.huitshop.model.Review;
import com.huitshop.model.Voucher;
import com.huitshop.service.CartService;
import com.huitshop.service.OrderService;
import com.huitshop.service.ProductService;
import com.huitshop.service.ReviewService;
import com.huitshop.service.UserService;
import com.huitshop.service.WarrantyService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerPortal extends BorderPane {
    private final AuthResponseDto user;
    private final Runnable onLogout;

    private final ProductService productService = new ProductService();
    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();
    private final ReviewService reviewService = new ReviewService();
    private final UserService userService = new UserService();
    private final WarrantyService warrantyService = new WarrantyService();
    private final VoucherDao voucherDao = new VoucherDao();

    private final Map<String, Button> navButtons = new HashMap<>();
    private StackPane contentArea;

    // Sub-panels
    private VBox shopPanel;
    private ScrollPane shopScroll;
    private VBox cartPanel;
    private VBox checkoutPanel;
    private VBox ordersPanel;
    private VBox profilePanel;
    private VBox warrantyPanel;

    // Profile controls
    private TextField profFullName;
    private TextField profEmail;
    private TextField profPhone;
    private PasswordField profNewPassword;
    private VBox addressListContainer;

    // Warranty controls
    private TextField warrantySearchField;
    private VBox warrantyResultCard;
    private VBox recentWarrantiesContainer;

    // Current State
    private Cart currentCart;

    public CustomerPortal(AuthResponseDto user, Runnable onLogout) {
        this.user = user;
        this.onLogout = onLogout;
        this.getStyleClass().add("root");

        // Build navigation sidebar
        buildSidebar();

        // Build central content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        this.setCenter(contentArea);

        // Prebuild panels
        buildShopPanel();
        buildCartPanel();
        buildOrdersPanel();
        buildProfilePanel();
        buildWarrantyPanel();

        // Show shop panel initially
        showPanel("shop");
    }

    private void buildSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        Label welcomeLabel = new Label("Xin chào,");
        welcomeLabel.getStyleClass().add("label-muted");
        Label nameLabel = new Label(user.getFullName());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);

        VBox userHeader = new VBox(2);
        userHeader.setPadding(new Insets(0, 10, 20, 10));
        userHeader.getChildren().addAll(welcomeLabel, nameLabel);

        Button shopBtn = createNavButton("Cửa hàng", "shop");
        Button cartBtn = createNavButton("Giỏ hàng", "cart");
        Button ordersBtn = createNavButton("Đơn mua", "orders");
        Button profileBtn = createNavButton("Tài khoản", "profile");
        Button warrantyBtn = createNavButton("Tra cứu bảo hành", "warranty");
        Button logoutBtn = new Button("Đăng xuất");
        logoutBtn.getStyleClass().add("nav-button");
        logoutBtn.setPrefWidth(200);
        logoutBtn.setOnAction(e -> onLogout.run());

        sidebar.getChildren().addAll(userHeader, new Separator(), shopBtn, cartBtn, ordersBtn, profileBtn, warrantyBtn, new Spacer(), logoutBtn);
        this.setLeft(sidebar);
    }

    private Button createNavButton(String text, String targetPanel) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setPrefWidth(200);
        btn.setOnAction(e -> showPanel(targetPanel));
        navButtons.put(targetPanel, btn);
        return btn;
    }

    private void showPanel(String target) {
        // Reset navigation styles
        navButtons.forEach((k, v) -> {
            v.getStyleClass().removeAll("nav-button-selected");
            v.getStyleClass().add("nav-button");
        });

        // Highlight selected nav button
        if (navButtons.containsKey(target)) {
            navButtons.get(target).getStyleClass().add("nav-button-selected");
        }

        contentArea.getChildren().clear();

        switch (target) {
            case "shop":
                refreshShopCatalog(null, null, "");
                contentArea.getChildren().add(shopPanel);
                break;
            case "cart":
                refreshCartPanel();
                contentArea.getChildren().add(cartPanel);
                break;
            case "orders":
                refreshOrdersPanel();
                contentArea.getChildren().add(ordersPanel);
                break;
            case "profile":
                refreshProfilePanel();
                contentArea.getChildren().add(profilePanel);
                break;
            case "warranty":
                refreshWarrantyPanel();
                contentArea.getChildren().add(warrantyPanel);
                break;
        }
    }

    // =========================================================================
    // 1. SHOP PANEL (PRODUCT CATALOG)
    // =========================================================================
    private FlowPane productGrid;
    private ComboBox<CategoryDto> catFilter;
    private ComboBox<BrandDto> brandFilter;
    private TextField searchField;

    private void buildShopPanel() {
        shopPanel = new VBox(15);
        shopPanel.getStyleClass().add("root");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Danh mục sản phẩm");
        title.getStyleClass().add("title-main");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Tìm kiếm sản phẩm...");
        searchField.setPrefWidth(220);

        catFilter = new ComboBox<>();
        catFilter.setPromptText("Tất cả danh mục");
        catFilter.setPrefWidth(160);
        // Load categories
        try {
            CategoryDto allCats = new CategoryDto();
            allCats.setName("Tất cả danh mục");
            catFilter.getItems().add(allCats);
            catFilter.getItems().addAll(productService.getCategories());
        } catch (Exception e) {
            e.printStackTrace();
        }

        brandFilter = new ComboBox<>();
        brandFilter.setPromptText("Tất cả thương hiệu");
        brandFilter.setPrefWidth(160);
        // Load brands
        try {
            BrandDto allBrands = new BrandDto();
            allBrands.setName("Tất cả thương hiệu");
            brandFilter.getItems().add(allBrands);
            brandFilter.getItems().addAll(productService.getBrands());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button filterBtn = new Button("Lọc");
        filterBtn.getStyleClass().add("button-primary");

        header.getChildren().addAll(title, spacer, searchField, catFilter, brandFilter, filterBtn);

        // Grid scroll pane
        productGrid = new FlowPane();
        productGrid.setHgap(20);
        productGrid.setVgap(20);
        productGrid.setPadding(new Insets(10, 0, 10, 0));

        shopScroll = new ScrollPane(productGrid);
        shopScroll.setFitToWidth(true);
        shopScroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(shopScroll, Priority.ALWAYS);

        shopPanel.getChildren().addAll(header, new Separator(), shopScroll);

        // Filter Actions
        filterBtn.setOnAction(e -> {
            CategoryDto selCat = catFilter.getValue();
            BrandDto selBrand = brandFilter.getValue();
            Integer catId = (selCat != null && selCat.getId() > 0) ? selCat.getId() : null;
            Integer brandId = (selBrand != null && selBrand.getId() > 0) ? selBrand.getId() : null;
            refreshShopCatalog(catId, brandId, searchField.getText());
        });
    }

    private void refreshShopCatalog(Integer categoryId, Integer brandId, String search) {
        productGrid.getChildren().clear();

        ProductQueryParams query = new ProductQueryParams();
        query.setPageSize(100); // load all for simple desktop scroll
        query.setCategoryId(categoryId);
        query.setBrandId(brandId);
        query.setSearch(search);

        try {
            List<ProductListDto> products = productService.getProducts(query);
            if (products.isEmpty()) {
                VBox empty = new VBox(10);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(50));
                Label emptyLabel = new Label("Không tìm thấy sản phẩm nào khớp với bộ lọc.");
                emptyLabel.getStyleClass().add("label-muted");
                emptyLabel.setFont(Font.font("Segoe UI", 16));
                empty.getChildren().add(emptyLabel);
                productGrid.getChildren().add(empty);
                return;
            }

            for (ProductListDto p : products) {
                VBox card = new VBox(8);
                card.getStyleClass().add("card-panel");
                card.setPrefWidth(220);
                card.setMaxWidth(220);
                card.setPadding(new Insets(12));

                // Image placeholder or loaded
                StackPane imgHolder = new StackPane();
                imgHolder.setPrefSize(196, 150);
                imgHolder.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8px;");

                ImageView iv = null;
                if (p.getThumbnailUrl() != null && !p.getThumbnailUrl().isEmpty()) {
                    Image resolved = getProductImage(p.getThumbnailUrl());
                    if (resolved != null) {
                        iv = new ImageView(resolved);
                    }
                }
                if (iv != null) {
                    iv.setFitWidth(196);
                    iv.setFitHeight(150);
                    iv.setPreserveRatio(true);
                    imgHolder.getChildren().add(iv);
                } else {
                    Label lbl = new Label("💻 Laptop/Mobile");
                    imgHolder.getChildren().add(lbl);
                }

                // Brand
                Label brandLbl = new Label(p.getBrand() != null ? p.getBrand().getName().toUpperCase() : "NO BRAND");
                brandLbl.setTextFill(Color.web("#38bdf8"));
                brandLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

                // Title
                Label nameLbl = new Label(p.getName());
                nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                nameLbl.setTextFill(Color.WHITE);
                nameLbl.setWrapText(true);
                nameLbl.setPrefHeight(40);

                // Price range
                String priceStr = formatMoney(p.getPriceFrom()) + "đ";
                if (p.getPriceTo() != null && p.getPriceTo().compareTo(p.getPriceFrom()) > 0) {
                    priceStr = formatMoney(p.getPriceFrom()) + " - " + formatMoney(p.getPriceTo()) + "đ";
                }
                Label priceLbl = new Label(priceStr);
                priceLbl.setTextFill(Color.web("#f43f5e")); // Rose-500
                priceLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

                Button viewBtn = new Button("Chi tiết");
                viewBtn.setMaxWidth(Double.MAX_VALUE);
                viewBtn.getStyleClass().add("button-primary");
                viewBtn.setOnAction(e -> showProductDetail(p.getId()));

                card.getChildren().addAll(imgHolder, brandLbl, nameLbl, priceLbl, viewBtn);
                productGrid.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // 2. PRODUCT DETAIL VIEW (TABS & REVIEWS)
    // =========================================================================
    private void showProductDetail(int productId) {
        ProductDetailDto detail = productService.getProductDetail(productId);
        if (detail == null) return;

        VBox detailBox = new VBox(20);
        detailBox.getStyleClass().add("root");

        // Back button row
        Button backBtn = new Button("← Quay lại cửa hàng");
        backBtn.setOnAction(e -> showPanel("shop"));

        HBox mainRow = new HBox(30);

        // Left Side: Image
        VBox left = new VBox(15);
        left.setPrefWidth(300);
        StackPane imgFrame = new StackPane();
        imgFrame.setPrefSize(300, 250);
        imgFrame.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 12px; -fx-border-color: #334155; -fx-border-radius: 12px;");
        ImageView iv = null;
        if (detail.getVariants() != null && !detail.getVariants().isEmpty() && detail.getVariants().get(0).getThumbnailUrl() != null) {
            Image resolved = getProductImage(detail.getVariants().get(0).getThumbnailUrl());
            if (resolved != null) {
                iv = new ImageView(resolved);
            }
        }
        if (iv != null) {
            iv.setFitWidth(300);
            iv.setFitHeight(250);
            iv.setPreserveRatio(true);
            imgFrame.getChildren().add(iv);
        } else {
            imgFrame.getChildren().add(new Label("💻"));
        }
        left.getChildren().add(imgFrame);

        // Right Side: Info
        VBox right = new VBox(15);
        HBox.setHgrow(right, Priority.ALWAYS);

        Label title = new Label(detail.getName());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        HBox brandCatRow = new HBox(15);
        Label brand = new Label("Thương hiệu: " + (detail.getBrand() != null ? detail.getBrand().getName() : "Không có"));
        Label cat = new Label("Danh mục: " + (detail.getCategory() != null ? detail.getCategory().getName() : "Không có"));
        brand.getStyleClass().add("label-muted");
        cat.getStyleClass().add("label-muted");
        brandCatRow.getChildren().addAll(brand, cat);

        // Description
        Label descTitle = new Label("Mô tả sản phẩm");
        descTitle.getStyleClass().add("title-sub");
        Label desc = new Label(detail.getDescription());
        desc.setWrapText(true);

        // Variant selection
        Label varTitle = new Label("Chọn phiên bản");
        varTitle.getStyleClass().add("title-sub");

        VBox varGroup = new VBox(10);
        ToggleGroup tg = new ToggleGroup();
        final ProductVariantDto[] selectedVariant = {null};

        // Price Label
        Label priceLabel = new Label();
        priceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        priceLabel.setTextFill(Color.web("#f43f5e"));

        for (ProductVariantDto v : detail.getVariants()) {
            RadioButton rb = new RadioButton(v.getVariantName() + " (Kho: " + v.getQuantityAvailable() + ")");
            rb.setToggleGroup(tg);
            rb.setTextFill(Color.WHITE);
            rb.setOnAction(e -> {
                selectedVariant[0] = v;
                priceLabel.setText(formatMoney(v.getPrice()) + " đ");
            });
            varGroup.getChildren().add(rb);
        }

        // Select first variant by default
        if (!detail.getVariants().isEmpty()) {
            tg.selectToggle((RadioButton)varGroup.getChildren().get(0));
            selectedVariant[0] = detail.getVariants().get(0);
            priceLabel.setText(formatMoney(selectedVariant[0].getPrice()) + " đ");
        }

        // Add to Cart
        Button addToCartBtn = new Button("THÊM VÀO GIỎ HÀNG");
        addToCartBtn.getStyleClass().add("button-accent");
        addToCartBtn.setOnAction(e -> {
            if (selectedVariant[0] == null) {
                alertUser("Lỗi", "Vui lòng chọn phiên bản trước.");
                return;
            }
            if (selectedVariant[0].getQuantityAvailable() <= 0) {
                alertUser("Lỗi", "Sản phẩm đã hết hàng.");
                return;
            }
            cartService.addItemToCart(user.getId(), selectedVariant[0].getId(), 1);
            showPanel("cart");
        });

        HBox buyRow = new HBox(15);
        buyRow.setAlignment(Pos.CENTER_LEFT);
        buyRow.getChildren().addAll(priceLabel, addToCartBtn);

        right.getChildren().addAll(title, brandCatRow, new Separator(), descTitle, desc, new Separator(), varTitle, varGroup, buyRow);

        mainRow.getChildren().addAll(left, right);

        // Reviews section
        VBox reviewSection = new VBox(10);
        Label reviewTitle = new Label("Đánh giá từ khách hàng");
        reviewTitle.getStyleClass().add("title-sub");
        
        VBox reviewsContainer = new VBox(10);
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        if (reviews.isEmpty()) {
            reviewsContainer.getChildren().add(new Label("Sản phẩm chưa có đánh giá nào."));
        } else {
            for (Review r : reviews) {
                VBox rBox = new VBox(5);
                rBox.getStyleClass().add("card-panel");
                rBox.setPadding(new Insets(10));
                
                HBox rHeader = new HBox(10);
                Label name = new Label(r.getUserName() != null ? r.getUserName() : "Khách hàng");
                name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                
                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < r.getRating(); i++) stars.append("⭐");
                Label starLbl = new Label(stars.toString());
                
                Label date = new Label(r.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                date.getStyleClass().add("label-muted");
                Region rSpacer = new Region();
                HBox.setHgrow(rSpacer, Priority.ALWAYS);

                rHeader.getChildren().addAll(name, starLbl, rSpacer, date);

                Label rContent = new Label(r.getContent());
                rContent.setWrapText(true);

                rBox.getChildren().addAll(rHeader, rContent);
                reviewsContainer.getChildren().add(rBox);
            }
        }

        // Add review form
        VBox addReviewBox = new VBox(8);
        addReviewBox.getStyleClass().add("card-panel");
        Label addReviewTitle = new Label("Viết đánh giá của bạn");
        addReviewTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        
        HBox ratingRow = new HBox(8);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Label rateLbl = new Label("Số sao:");
        ComboBox<Integer> starBox = new ComboBox<>();
        starBox.getItems().addAll(5, 4, 3, 2, 1);
        starBox.setValue(5);
        ratingRow.getChildren().addAll(rateLbl, starBox);

        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Nhập đánh giá chi tiết của bạn tại đây...");
        reviewArea.setPrefHeight(80);
        Button submitReviewBtn = new Button("Gửi đánh giá");
        submitReviewBtn.getStyleClass().add("button-primary");

        submitReviewBtn.setOnAction(e -> {
            String content = reviewArea.getText().trim();
            if (content.isEmpty()) {
                alertUser("Lỗi", "Vui lòng nhập nội dung đánh giá.");
                return;
            }

            Review rev = new Review();
            rev.setUserId(user.getId());
            rev.setProductId(productId);
            if (selectedVariant[0] != null) rev.setVariantId(selectedVariant[0].getId());
            rev.setRating(starBox.getValue());
            rev.setTitle("Review");
            rev.setContent(content);
            rev.setVerifiedPurchase(true);

            try {
                reviewService.addReview(rev);
                reviewArea.clear();
                alertUser("Thành công", "Đánh giá của bạn đã được ghi nhận!");
                showProductDetail(productId); // reload
            } catch (Exception ex) {
                alertUser("Lỗi", "Lỗi gửi đánh giá: " + ex.getMessage());
            }
        });

        addReviewBox.getChildren().addAll(addReviewTitle, ratingRow, reviewArea, submitReviewBtn);

        reviewSection.getChildren().addAll(reviewTitle, reviewsContainer, addReviewBox);

        detailBox.getChildren().addAll(backBtn, mainRow, new Separator(), reviewSection);

        ScrollPane detailScroll = new ScrollPane(detailBox);
        detailScroll.setFitToWidth(true);
        detailScroll.getStyleClass().add("scroll-pane");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(detailScroll);
    }

    // =========================================================================
    // 3. CART PANEL
    // =========================================================================
    private TableView<CartItem> cartTable;
    private Label subtotalLbl;
    private Label discountLbl;
    private Label shippingFeeLbl;
    private Label totalLbl;
    private TextField voucherInput;
    private Label voucherStatusLbl;

    private void buildCartPanel() {
        cartPanel = new VBox(15);
        cartPanel.getStyleClass().add("root");

        Label title = new Label("Giỏ hàng của bạn");
        title.getStyleClass().add("title-main");

        // Cart TableView
        cartTable = new TableView<>();
        cartTable.setPlaceholder(new Label("Giỏ hàng trống. Hãy thêm sản phẩm vào!"));
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        TableColumn<CartItem, String> nameCol = new TableColumn<>("Sản phẩm");
        nameCol.setPrefWidth(300);
        nameCol.setCellValueFactory(cellData -> {
            CartItem item = cellData.getValue();
            String name = item.getProductVariant().getProduct().getName() + " - " + item.getProductVariant().getVariantName();
            return new SimpleStringProperty(name);
        });

        TableColumn<CartItem, String> priceCol = new TableColumn<>("Đơn giá");
        priceCol.setPrefWidth(120);
        priceCol.setCellValueFactory(cellData -> new SimpleStringProperty(formatMoney(cellData.getValue().getProductVariant().getPrice()) + "đ"));

        TableColumn<CartItem, Integer> qtyCol = new TableColumn<>("Số lượng");
        qtyCol.setPrefWidth(100);
        qtyCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));

        TableColumn<CartItem, String> totalCol = new TableColumn<>("Thành tiền");
        totalCol.setPrefWidth(140);
        totalCol.setCellValueFactory(cellData -> {
            BigDecimal price = cellData.getValue().getProductVariant().getPrice();
            BigDecimal total = price.multiply(BigDecimal.valueOf(cellData.getValue().getQuantity()));
            return new SimpleStringProperty(formatMoney(total) + "đ");
        });

        // Add Delete column
        TableColumn<CartItem, Void> actionCol = new TableColumn<>("Thao tác");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(col -> new TableCell<CartItem, Void>() {
            private final Button delBtn = new Button("Xóa");
            {
                delBtn.getStyleClass().add("button-danger");
                delBtn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cartService.removeItemFromCart(user.getId(), item.getId());
                    refreshCartPanel();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(delBtn);
            }
        });

        cartTable.getColumns().addAll(nameCol, priceCol, qtyCol, totalCol, actionCol);

        // Lower Layout: Vouchers and Invoice Recap
        HBox bottom = new HBox(30);
        bottom.setPadding(new Insets(15, 0, 0, 0));

        // Voucher Box
        VBox voucherBox = new VBox(10);
        voucherBox.setPrefWidth(300);
        voucherBox.getStyleClass().add("card-panel");
        Label vTitle = new Label("Mã giảm giá (Voucher)");
        vTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        voucherInput = new TextField();
        voucherInput.setPromptText("Mã voucher...");
        Button applyVoucherBtn = new Button("Áp dụng");
        applyVoucherBtn.getStyleClass().add("button-primary");
        voucherStatusLbl = new Label();
        voucherStatusLbl.setFont(Font.font("Segoe UI", 11));

        applyVoucherBtn.setOnAction(e -> {
            String code = voucherInput.getText().trim();
            try {
                cartService.applyVoucher(user.getId(), code);
                voucherStatusLbl.setTextFill(Color.web("#22c55e")); // Green-500
                voucherStatusLbl.setText("Áp dụng voucher thành công!");
                refreshCartPanel();
            } catch (Exception ex) {
                voucherStatusLbl.setTextFill(Color.web("#ef4444")); // Red-500
                voucherStatusLbl.setText(ex.getMessage());
            }
        });

        voucherBox.getChildren().addAll(vTitle, voucherInput, applyVoucherBtn, voucherStatusLbl);

        // Summary Box
        VBox summaryBox = new VBox(10);
        summaryBox.getStyleClass().add("card-panel");
        HBox.setHgrow(summaryBox, Priority.ALWAYS);

        subtotalLbl = new Label("Tạm tính: 0 đ");
        discountLbl = new Label("Khuyến mãi: 0 đ");
        shippingFeeLbl = new Label("Phí vận chuyển: 0 đ");
        totalLbl = new Label("Tổng thanh toán: 0 đ");
        totalLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        totalLbl.setTextFill(Color.web("#f43f5e"));

        Button checkoutBtn = new Button("ĐI TỚI THANH TOÁN");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.getStyleClass().add("button-accent");
        checkoutBtn.setOnAction(e -> showCheckoutPanel());

        summaryBox.getChildren().addAll(subtotalLbl, discountLbl, shippingFeeLbl, new Separator(), totalLbl, checkoutBtn);

        bottom.getChildren().addAll(voucherBox, summaryBox);

        cartPanel.getChildren().addAll(title, cartTable, bottom);
    }

    private void refreshCartPanel() {
        currentCart = cartService.getCartByUserId(user.getId());
        cartTable.getItems().clear();
        
        if (currentCart == null) return;
        
        cartTable.getItems().addAll(currentCart.getCartItems());

        // Recalculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : currentCart.getCartItems()) {
            subtotal = subtotal.add(ci.getProductVariant().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (currentCart.getVoucherCode() != null && !currentCart.getVoucherCode().isEmpty()) {
            voucherInput.setText(currentCart.getVoucherCode());
            try {
                Voucher v = voucherDao.findByCode(currentCart.getVoucherCode());
                if (v != null) {
                    if ("PERCENT".equals(v.getDiscountType())) {
                        discount = subtotal.multiply(v.getDiscountValue().divide(BigDecimal.valueOf(100)));
                        if (v.getMaxDiscountAmount() != null && discount.compareTo(v.getMaxDiscountAmount()) > 0) {
                            discount = v.getMaxDiscountAmount();
                        }
                    } else if ("FIXED".equals(v.getDiscountType())) {
                        discount = v.getDiscountValue();
                    }
                    if (discount.compareTo(subtotal) > 0) discount = subtotal;
                }
            } catch (Exception e) {}
        } else {
            voucherInput.clear();
        }

        BigDecimal shippingFee = (subtotal.compareTo(BigDecimal.ZERO) == 0 || subtotal.compareTo(BigDecimal.valueOf(500000)) >= 0) ? BigDecimal.ZERO : BigDecimal.valueOf(30000);
        BigDecimal total = subtotal.subtract(discount).add(shippingFee);

        subtotalLbl.setText("Tạm tính: " + formatMoney(subtotal) + " đ");
        discountLbl.setText("Khuyến mãi: -" + formatMoney(discount) + " đ");
        shippingFeeLbl.setText("Phí vận chuyển: " + formatMoney(shippingFee) + " đ");
        totalLbl.setText("Tổng thanh toán: " + formatMoney(total) + " đ");
    }

    // =========================================================================
    // 4. CHECKOUT PANEL
    // =========================================================================
    private void showCheckoutPanel() {
        if (currentCart == null || currentCart.getCartItems().isEmpty()) {
            alertUser("Lỗi", "Giỏ hàng của bạn đang trống.");
            return;
        }

        checkoutPanel = new VBox(15);
        checkoutPanel.getStyleClass().add("root");

        Label title = new Label("Thông tin thanh toán & giao hàng");
        title.getStyleClass().add("title-main");

        // Back button to cart
        Button backToCart = new Button("← Quay lại sửa giỏ hàng");
        backToCart.setOnAction(e -> showPanel("cart"));

        HBox mainRow = new HBox(30);

        // Form Fields (Left)
        VBox form = new VBox(12);
        form.setPrefWidth(450);
        form.getStyleClass().add("card-panel");

        TextField nameField = new TextField(user.getFullName());
        nameField.setPromptText("Tên người nhận...");
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Số điện thoại nhận hàng...");

        TextField provField = new TextField("Hồ Chí Minh");
        TextField distField = new TextField("Quận 12");
        TextField wardField = new TextField("Tân Thới Nhất");
        TextField streetField = new TextField("140 Lê Trọng Tấn");

        ComboBox<String> payMethod = new ComboBox<>();
        payMethod.getItems().addAll("COD", "BANK_TRANSFER");
        payMethod.setValue("COD");
        payMethod.setMaxWidth(Double.MAX_VALUE);

        TextArea noteField = new TextArea();
        noteField.setPromptText("Ghi chú cho shipper (ví dụ: giao giờ hành chính)...");
        noteField.setPrefHeight(60);

        form.getChildren().addAll(
            new Label("Họ tên người nhận"), nameField,
            new Label("Số điện thoại nhận"), phoneField,
            new Label("Tỉnh / Thành phố"), provField,
            new Label("Quận / Huyện"), distField,
            new Label("Phường / Xã"), wardField,
            new Label("Số nhà, Tên đường"), streetField,
            new Label("Phương thức thanh toán"), payMethod,
            new Label("Ghi chú"), noteField
        );

        // Order Summary (Right)
        VBox summary = new VBox(12);
        HBox.setHgrow(summary, Priority.ALWAYS);
        summary.getStyleClass().add("card-panel");

        Label sumTitle = new Label("Tóm tắt đơn hàng");
        sumTitle.getStyleClass().add("title-sub");

        VBox itemsList = new VBox(5);
        for (CartItem ci : currentCart.getCartItems()) {
            Label il = new Label("• " + ci.getProductVariant().getProduct().getName() + " x" + ci.getQuantity());
            itemsList.getChildren().add(il);
        }

        // Summary Labels copy
        Label sumSub = new Label(subtotalLbl.getText());
        Label sumDisc = new Label(discountLbl.getText());
        Label sumShip = new Label(shippingFeeLbl.getText());
        Label sumTotal = new Label(totalLbl.getText());
        sumTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sumTotal.setTextFill(Color.web("#f43f5e"));

        Button placeOrderBtn = new Button("ĐẶT HÀNG NGAY");
        placeOrderBtn.setMaxWidth(Double.MAX_VALUE);
        placeOrderBtn.getStyleClass().add("button-accent");

        placeOrderBtn.setOnAction(e -> {
            String nameVal = nameField.getText().trim();
            String phoneVal = phoneField.getText().trim();
            String streetVal = streetField.getText().trim();
            
            if (nameVal.isEmpty() || phoneVal.isEmpty() || streetVal.isEmpty()) {
                alertUser("Lỗi", "Vui lòng nhập đầy đủ Tên, SĐT và Địa chỉ giao hàng.");
                return;
            }

            // Create shipping address JSON
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode root = objectMapper.createObjectNode();
            root.put("receiver_name", nameVal);
            root.put("receiver_phone", phoneVal);
            root.put("province", provField.getText());
            root.put("district", distField.getText());
            root.put("ward", wardField.getText());
            root.put("street_address", streetVal);

            CreateOrderRequest req = new CreateOrderRequest();
            req.setPaymentMethod(payMethod.getValue());
            req.setShippingAddressJson(root.toString());
            req.setNote(noteField.getText());

            try {
                OrderResponseDto res = orderService.createOrder(user.getId(), req);
                alertUser("Thành công", "Đơn hàng của bạn đã được đặt thành công! Mã đơn: " + res.getCode());
                showPanel("orders");
            } catch (Exception ex) {
                alertUser("Đặt hàng thất bại", ex.getMessage());
            }
        });

        summary.getChildren().addAll(sumTitle, itemsList, new Separator(), sumSub, sumDisc, sumShip, new Separator(), sumTotal, placeOrderBtn);

        mainRow.getChildren().addAll(form, summary);

        checkoutPanel.getChildren().addAll(backToCart, title, mainRow);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(checkoutPanel);
    }

    // =========================================================================
    // 5. ORDER HISTORY PANEL
    // =========================================================================
    private TableView<OrderResponseDto> ordersTable;

    private void buildOrdersPanel() {
        ordersPanel = new VBox(15);
        ordersPanel.getStyleClass().add("root");

        Label title = new Label("Lịch sử mua hàng");
        title.getStyleClass().add("title-main");

        ordersTable = new TableView<>();
        ordersTable.setPlaceholder(new Label("Bạn chưa thực hiện đơn mua hàng nào."));
        VBox.setVgrow(ordersTable, Priority.ALWAYS);

        TableColumn<OrderResponseDto, String> codeCol = new TableColumn<>("Mã đơn hàng");
        codeCol.setPrefWidth(150);
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCode()));

        TableColumn<OrderResponseDto, String> dateCol = new TableColumn<>("Ngày mua");
        dateCol.setPrefWidth(140);
        dateCol.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
        });

        TableColumn<OrderResponseDto, String> payCol = new TableColumn<>("Thanh toán");
        payCol.setPrefWidth(120);
        payCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentStatus()));

        TableColumn<OrderResponseDto, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        TableColumn<OrderResponseDto, String> totalCol = new TableColumn<>("Tổng tiền");
        totalCol.setPrefWidth(140);
        totalCol.setCellValueFactory(cellData -> new SimpleStringProperty(formatMoney(cellData.getValue().getTotal()) + "đ"));

        // Detail Action
        TableColumn<OrderResponseDto, Void> actCol = new TableColumn<>("Hành động");
        actCol.setPrefWidth(150);
        actCol.setCellFactory(col -> new TableCell<OrderResponseDto, Void>() {
            private final Button detBtn = new Button("Chi tiết");
            {
                detBtn.getStyleClass().add("button-primary");
                detBtn.setOnAction(e -> {
                    OrderResponseDto order = getTableView().getItems().get(getIndex());
                    showOrderDetailModal(order.getId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(detBtn);
            }
        });

        ordersTable.getColumns().addAll(codeCol, dateCol, payCol, statusCol, totalCol, actCol);
        ordersPanel.getChildren().addAll(title, ordersTable);
    }

    private void refreshOrdersPanel() {
        ordersTable.getItems().clear();
        try {
            List<OrderResponseDto> list = orderService.getOrdersByUserId(user.getId(), 1, 100);
            ordersTable.getItems().addAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOrderDetailModal(int orderId) {
        OrderResponseDto o = orderService.getOrderById(orderId);
        if (o == null) return;

        Dialog<Void> diag = new Dialog<>();
        diag.setTitle("Chi tiết đơn hàng " + o.getCode());
        diag.getDialogPane().getStyleClass().add("dialog-pane");

        ButtonType closeBtnType = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        diag.getDialogPane().getButtonTypes().add(closeBtnType);

        VBox content = new VBox(15);
        content.setPrefWidth(550);

        HBox top = new HBox(15);
        Label codeLbl = new Label("Đơn hàng: " + o.getCode());
        codeLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        codeLbl.setTextFill(Color.WHITE);
        Label dateLbl = new Label(o.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateLbl.getStyleClass().add("label-muted");
        top.getChildren().addAll(codeLbl, new Spacer(), dateLbl);

        VBox addrBox = new VBox(5);
        addrBox.getStyleClass().add("card-panel");
        addrBox.getChildren().addAll(
            new Label("Người nhận: " + o.getRecipientName() + " | SĐT: " + o.getRecipientPhone()),
            new Label("Địa chỉ giao: " + o.getFullAddress()),
            new Label("Hình thức: " + o.getPaymentMethod() + " | Trạng thái: " + o.getStatus())
        );

        // Items list
        VBox itemsBox = new VBox(5);
        itemsBox.getStyleClass().add("card-panel");
        itemsBox.getChildren().add(new Label("Sản phẩm đã mua:"));
        for (OrderItemDto item : o.getItems()) {
            Label label = new Label("• " + item.getProductName() + " | SKU: " + item.getSku() + " | " + formatMoney(item.getUnitPrice()) + "đ x" + item.getQuantity());
            itemsBox.getChildren().add(label);
            
            // Show serial numbers if items are shipped / completed
            if (item.getSerialNumbers() != null && !item.getSerialNumbers().isEmpty()) {
                Label snLbl = new Label("   Số Serial (IMEI): " + String.join(", ", item.getSerialNumbers()));
                snLbl.setTextFill(Color.web("#38bdf8"));
                snLbl.setFont(Font.font("Segoe UI", 11));
                itemsBox.getChildren().add(snLbl);
            }
        }

        // Totals
        VBox totalBox = new VBox(5);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.getChildren().addAll(
            new Label("Tạm tính: " + formatMoney(o.getSubtotal()) + "đ"),
            new Label("Giảm giá: -" + formatMoney(o.getDiscount()) + "đ"),
            new Label("Phí giao: " + formatMoney(o.getShippingFee()) + "đ"),
            new Label("Tổng cộng: " + formatMoney(o.getTotal()) + "đ")
        );
        totalBox.getChildren().get(3).setStyle("-fx-text-fill: #f43f5e; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Status History Timeline
        VBox timelineBox = new VBox(5);
        timelineBox.getStyleClass().add("card-panel");
        timelineBox.getChildren().add(new Label("Lịch sử trạng thái đơn hàng:"));
        for (OrderStatusHistoryDto hist : o.getStatusHistory()) {
            Label l = new Label("🕒 [" + hist.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "] " + hist.getStatus() + " - " + hist.getNote());
            l.setFont(Font.font("Segoe UI", 11));
            timelineBox.getChildren().add(l);
        }

        // Cancel order button inside dialog if PENDING
        if ("PENDING".equals(o.getStatus()) || "CONFIRMED".equals(o.getStatus())) {
            Button cancelBtn = new Button("Yêu cầu hủy đơn hàng");
            cancelBtn.getStyleClass().add("button-danger");
            cancelBtn.setOnAction(e -> {
                boolean ok = orderService.cancelOrder(o.getId(), "Khách hàng yêu cầu hủy đơn");
                if (ok) {
                    alertUser("Thành công", "Hủy đơn hàng thành công!");
                    diag.close();
                    refreshOrdersPanel();
                } else {
                    alertUser("Thất bại", "Hủy đơn hàng thất bại.");
                }
            });
            content.getChildren().addAll(top, addrBox, itemsBox, totalBox, timelineBox, cancelBtn);
        } else {
            content.getChildren().addAll(top, addrBox, itemsBox, totalBox, timelineBox);
        }

        diag.getDialogPane().setContent(content);
        diag.showAndWait();
    }

    // =========================================================================
    // USER PROFILE & ADDRESS PANELS
    // =========================================================================
    private void buildProfilePanel() {
        profilePanel = new VBox(20);
        profilePanel.getStyleClass().add("root");

        Label title = new Label("Quản lý tài khoản");
        title.getStyleClass().add("title-main");

        HBox splitLayout = new HBox(30);
        splitLayout.setAlignment(Pos.TOP_LEFT);

        VBox profileForm = new VBox(12);
        profileForm.setPrefWidth(350);
        profileForm.getStyleClass().add("card-panel");
        profileForm.setPadding(new Insets(20));

        Label section1Title = new Label("Thông tin cá nhân");
        section1Title.getStyleClass().add("title-sub");

        profFullName = new TextField();
        profFullName.setPromptText("Họ và tên");
        
        profEmail = new TextField();
        profEmail.setEditable(false);
        profEmail.setStyle("-fx-opacity: 0.7;");
        
        profPhone = new TextField();
        profPhone.setPromptText("Số điện thoại");
        
        profNewPassword = new PasswordField();
        profNewPassword.setPromptText("Mật khẩu mới (bỏ trống nếu không đổi)");

        Button updateProfileBtn = new Button("Cập nhật thông tin");
        updateProfileBtn.getStyleClass().add("button-primary");
        updateProfileBtn.setMaxWidth(Double.MAX_VALUE);

        updateProfileBtn.setOnAction(e -> {
            String name = profFullName.getText().trim();
            String phone = profPhone.getText().trim();
            String pass = profNewPassword.getText().trim();
            if (name.isEmpty()) {
                alertUser("Lỗi", "Họ tên không được để trống.");
                return;
            }
            boolean success = userService.updateProfile(user.getId(), name, phone, pass);
            if (success) {
                alertUser("Thành công", "Đã cập nhật thông tin tài khoản thành công!");
                profNewPassword.clear();
                user.setFullName(name);
                buildSidebar();
            } else {
                alertUser("Thất bại", "Cập nhật thất bại.");
            }
        });

        profileForm.getChildren().addAll(
            section1Title,
            new Label("Họ tên"), profFullName,
            new Label("Email (Tên đăng nhập)"), profEmail,
            new Label("Số điện thoại"), profPhone,
            new Label("Mật khẩu mới"), profNewPassword,
            updateProfileBtn
        );

        VBox addressSection = new VBox(15);
        HBox.setHgrow(addressSection, Priority.ALWAYS);
        addressSection.getStyleClass().add("card-panel");
        addressSection.setPadding(new Insets(20));

        HBox addressHeader = new HBox(10);
        Label section2Title = new Label("Sổ địa chỉ nhận hàng");
        section2Title.getStyleClass().add("title-sub");
        Button addAddrBtn = new Button("+ Thêm địa chỉ mới");
        addAddrBtn.getStyleClass().add("button-accent");
        addressHeader.getChildren().addAll(section2Title, new Spacer(), addAddrBtn);

        addressListContainer = new VBox(10);
        ScrollPane addressScroll = new ScrollPane(addressListContainer);
        addressScroll.setFitToWidth(true);
        addressScroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(addressScroll, Priority.ALWAYS);

        addAddrBtn.setOnAction(e -> showAddAddressDialog());

        addressSection.getChildren().addAll(addressHeader, new Separator(), addressScroll);

        splitLayout.getChildren().addAll(profileForm, addressSection);
        VBox.setVgrow(splitLayout, Priority.ALWAYS);
        profilePanel.getChildren().addAll(title, splitLayout);
    }

    private void refreshProfilePanel() {
        com.huitshop.model.User freshUser = new com.huitshop.dao.UserDao().findById(user.getId());
        if (freshUser != null) {
            profFullName.setText(freshUser.getFullName());
            profEmail.setText(freshUser.getEmail());
            profPhone.setText(freshUser.getPhone());
        }
        
        addressListContainer.getChildren().clear();
        List<com.huitshop.model.Address> addresses = userService.getAddresses(user.getId());
        if (addresses.isEmpty()) {
            addressListContainer.getChildren().add(new Label("Chưa có địa chỉ nào được thêm."));
        } else {
            for (com.huitshop.model.Address addr : addresses) {
                VBox addrCard = new VBox(8);
                addrCard.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8px; -fx-border-color: #334155; -fx-border-radius: 8px; -fx-padding: 12px;");
                
                HBox row1 = new HBox(10);
                Label labelLbl = new Label("[" + addr.getLabel().toUpperCase() + "]");
                labelLbl.setTextFill(Color.web("#38bdf8"));
                labelLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                
                Label namePhone = new Label(addr.getReceiverName() + " | SĐT: " + addr.getReceiverPhone());
                namePhone.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                namePhone.setTextFill(Color.WHITE);
                
                row1.getChildren().addAll(labelLbl, namePhone);
                if (addr.isDefault()) {
                    Label defTag = new Label("Mặc định");
                    defTag.setStyle("-fx-background-color: #15803d; -fx-text-fill: white; -fx-padding: 2px 6px; -fx-background-radius: 4px; -fx-font-size: 10px;");
                    row1.getChildren().add(defTag);
                }
                
                String fullAddrText = addr.getStreetAddress() + ", " + addr.getWard() + ", " + addr.getDistrict() + ", " + addr.getProvince();
                Label detailLbl = new Label(fullAddrText);
                detailLbl.setTextFill(Color.LIGHTGRAY);
                detailLbl.setWrapText(true);
                
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_RIGHT);
                
                if (!addr.isDefault()) {
                    Button setDefBtn = new Button("Đặt mặc định");
                    setDefBtn.getStyleClass().add("button-accent");
                    setDefBtn.setStyle("-fx-font-size: 11px;");
                    setDefBtn.setOnAction(e -> {
                        userService.setDefaultAddress(addr.getId(), user.getId());
                        refreshProfilePanel();
                    });
                    actions.getChildren().add(setDefBtn);
                }
                
                Button delAddrBtn = new Button("Xóa");
                delAddrBtn.getStyleClass().add("button-danger");
                delAddrBtn.setStyle("-fx-font-size: 11px;");
                delAddrBtn.setOnAction(e -> {
                    userService.removeAddress(addr.getId(), user.getId());
                    refreshProfilePanel();
                });
                actions.getChildren().add(delAddrBtn);
                
                addrCard.getChildren().addAll(row1, detailLbl, actions);
                addressListContainer.getChildren().add(addrCard);
            }
        }
    }

    private void showAddAddressDialog() {
        Dialog<Void> diag = new Dialog<>();
        diag.setTitle("Thêm địa chỉ giao hàng mới");
        diag.getDialogPane().getStyleClass().add("dialog-pane");

        ButtonType saveBtnType = new ButtonType("Thêm mới", ButtonBar.ButtonData.OK_DONE);
        diag.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane layout = new GridPane();
        layout.setHgap(10);
        layout.setVgap(10);
        layout.setPadding(new Insets(20));

        TextField labelF = new TextField();
        labelF.setPromptText("VD: Nhà riêng, Văn phòng...");
        
        TextField receiverF = new TextField(user.getFullName());
        receiverF.setPromptText("Tên người nhận...");
        
        TextField phoneF = new TextField();
        phoneF.setPromptText("SĐT người nhận...");
        
        TextField provF = new TextField("Hồ Chí Minh");
        TextField distF = new TextField();
        distF.setPromptText("VD: Quận Tân Bình...");
        TextField wardF = new TextField();
        wardF.setPromptText("VD: Phường 15...");
        TextField streetF = new TextField();
        streetF.setPromptText("VD: 140 Lê Trọng Tấn...");
        
        CheckBox isDefaultCB = new CheckBox("Đặt làm địa chỉ mặc định");
        isDefaultCB.setTextFill(Color.WHITE);

        layout.add(new Label("Nhãn địa chỉ:"), 0, 0);
        layout.add(labelF, 1, 0);
        layout.add(new Label("Tên người nhận:"), 0, 1);
        layout.add(receiverF, 1, 1);
        layout.add(new Label("Số điện thoại nhận:"), 0, 2);
        layout.add(phoneF, 1, 2);
        layout.add(new Label("Tỉnh / Thành phố:"), 0, 3);
        layout.add(provF, 1, 3);
        layout.add(new Label("Quận / Huyện:"), 0, 4);
        layout.add(distF, 1, 4);
        layout.add(new Label("Phường / Xã:"), 0, 5);
        layout.add(wardF, 1, 5);
        layout.add(new Label("Số nhà, Tên đường:"), 0, 6);
        layout.add(streetF, 1, 6);
        layout.add(isDefaultCB, 1, 7);

        diag.getDialogPane().setContent(layout);
        diag.getDialogPane().getStylesheets().add(getClass().getResource("/com/huitshop/css/style.css").toExternalForm());

        diag.setResultConverter(db -> {
            if (db == saveBtnType) {
                String labelVal = labelF.getText().trim();
                String recVal = receiverF.getText().trim();
                String phoneVal = phoneF.getText().trim();
                String provVal = provF.getText().trim();
                String distVal = distF.getText().trim();
                String wardVal = wardF.getText().trim();
                String streetVal = streetF.getText().trim();

                if (labelVal.isEmpty() || recVal.isEmpty() || phoneVal.isEmpty() || streetVal.isEmpty()) {
                    alertUser("Lỗi", "Vui lòng nhập đầy đủ các trường thông tin bắt buộc.");
                    return null;
                }

                com.huitshop.model.Address addr = new com.huitshop.model.Address();
                addr.setLabel(labelVal);
                addr.setReceiverName(recVal);
                addr.setReceiverPhone(phoneVal);
                addr.setProvince(provVal);
                addr.setDistrict(distVal);
                addr.setWard(wardVal);
                addr.setStreetAddress(streetVal);
                addr.setDefault(isDefaultCB.isSelected());

                boolean success = userService.addAddress(user.getId(), addr);
                if (success) {
                    alertUser("Thành công", "Đã thêm địa chỉ mới thành công!");
                    refreshProfilePanel();
                } else {
                    alertUser("Thất bại", "Không thể thêm địa chỉ.");
                }
            }
            return null;
        });

        diag.showAndWait();
    }

    // =========================================================================
    // WARRANTY LOOKUP PANELS
    // =========================================================================
    private void buildWarrantyPanel() {
        warrantyPanel = new VBox(20);
        warrantyPanel.getStyleClass().add("root");

        Label title = new Label("Tra cứu bảo hành thiết bị");
        title.getStyleClass().add("title-main");

        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        warrantySearchField = new TextField();
        warrantySearchField.setPromptText("Nhập số IMEI / Serial number của sản phẩm...");
        warrantySearchField.setPrefWidth(350);
        Button checkBtn = new Button("Tra cứu");
        checkBtn.getStyleClass().add("button-primary");

        searchRow.getChildren().addAll(new Label("Mã thiết bị:"), warrantySearchField, checkBtn);

        HBox bodyRow = new HBox(25);
        bodyRow.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(bodyRow, Priority.ALWAYS);

        warrantyResultCard = new VBox(15);
        warrantyResultCard.setMinWidth(350);
        warrantyResultCard.setPrefWidth(400);
        warrantyResultCard.setMaxWidth(400);
        warrantyResultCard.getStyleClass().add("card-panel");
        warrantyResultCard.setPadding(new Insets(20));
        
        Label guideLbl = new Label("Nhập số IMEI/Serial sản phẩm và click nút 'Tra cứu' để kiểm tra thời hạn bảo hành.");
        guideLbl.setWrapText(true);
        guideLbl.setTextFill(Color.WHITE);
        warrantyResultCard.getChildren().add(guideLbl);

        VBox recentLookupsBox = new VBox(15);
        HBox.setHgrow(recentLookupsBox, Priority.ALWAYS);
        recentLookupsBox.getStyleClass().add("card-panel");
        recentLookupsBox.setPadding(new Insets(20));

        Label recentTitle = new Label("Danh sách thiết bị bảo hành gần đây");
        recentTitle.getStyleClass().add("title-sub");
        
        recentWarrantiesContainer = new VBox(10);
        ScrollPane recentScroll = new ScrollPane(recentWarrantiesContainer);
        recentScroll.setFitToWidth(true);
        recentScroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(recentScroll, Priority.ALWAYS);

        recentLookupsBox.getChildren().addAll(recentTitle, new Separator(), recentScroll);

        bodyRow.getChildren().addAll(warrantyResultCard, recentLookupsBox);

        warrantyPanel.getChildren().addAll(title, searchRow, bodyRow);

        checkBtn.setOnAction(e -> performWarrantySearch());
    }

    private void performWarrantySearch() {
        String serial = warrantySearchField.getText().trim();
        if (serial.isEmpty()) {
            alertUser("Lỗi", "Vui lòng nhập số IMEI/Serial.");
            return;
        }
        
        com.huitshop.dto.WarrantyDtos.WarrantyDto dto = warrantyService.getWarrantyBySerial(serial);
        warrantyResultCard.getChildren().clear();
        
        if (dto == null) {
            VBox errorBox = new VBox(10);
            errorBox.setAlignment(Pos.CENTER);
            Label err = new Label("❌ Không tìm thấy thông tin bảo hành");
            err.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            err.setTextFill(Color.web("#ef4444"));
            Label desc = new Label("Vui lòng kiểm tra lại số IMEI/Serial chính xác in trên hộp hoặc thân thiết bị.");
            desc.setWrapText(true);
            desc.getStyleClass().add("label-muted");
            errorBox.getChildren().addAll(err, desc);
            warrantyResultCard.getChildren().add(errorBox);
        } else {
            Label successTitle = new Label("Thông Tin Bảo Hành");
            successTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            successTitle.setTextFill(Color.WHITE);
            
            VBox details = new VBox(8);
            details.getChildren().addAll(
                new Label("• Thiết bị: " + dto.getProductName() + " - " + dto.getVariantName()),
                new Label("• Serial/IMEI: " + dto.getSerialNumber())
            );
            
            if (dto.getOrderCode() != null) {
                details.getChildren().addAll(
                    new Label("• Đơn hàng mua: " + dto.getOrderCode()),
                    new Label("• Khách hàng: " + dto.getCustomerName())
                );
            } else {
                details.getChildren().add(new Label("• Đơn hàng: Chưa bán / Tồn kho"));
            }
            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            if (dto.getOutboundDate() != null) {
                details.getChildren().add(new Label("• Ngày xuất kho: " + dto.getOutboundDate().format(dtf)));
            }
            
            if (dto.getExpireDate() != null) {
                details.getChildren().add(new Label("• Hạn bảo hành: " + dto.getExpireDate().format(df)));
            } else {
                details.getChildren().add(new Label("• Hạn bảo hành: Chưa kích hoạt"));
            }
            
            HBox tagRow = new HBox(10);
            tagRow.setAlignment(Pos.CENTER_LEFT);
            Label tagLabel = new Label("Trạng thái:");
            Label statusTag = new Label();
            if ("ACTIVE".equals(dto.getStatus())) {
                statusTag.setText("CÒN HẠN BẢO HÀNH (" + dto.getDaysRemaining() + " ngày)");
                statusTag.setStyle("-fx-background-color: #15803d; -fx-text-fill: white; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;");
            } else if ("EXPIRED".equals(dto.getStatus())) {
                statusTag.setText("HẾT HẠN BẢO HÀNH");
                statusTag.setStyle("-fx-background-color: #b91c1c; -fx-text-fill: white; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;");
            } else {
                statusTag.setText("TỒN KHO / CHƯA KÍCH HOẠT");
                statusTag.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-padding: 4px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;");
            }
            tagRow.getChildren().addAll(tagLabel, statusTag);
            
            Label notesLbl = new Label("• Ghi chú: " + (dto.getNotes() != null ? dto.getNotes() : "Không có"));
            notesLbl.setWrapText(true);
            
            warrantyResultCard.getChildren().addAll(successTitle, new Separator(), details, tagRow, notesLbl);
        }
    }

    private void refreshWarrantyPanel() {
        warrantySearchField.clear();
        warrantyResultCard.getChildren().clear();
        Label guideLbl = new Label("Nhập số IMEI/Serial sản phẩm và click nút 'Tra cứu' để kiểm tra thời hạn bảo hành.");
        guideLbl.setWrapText(true);
        warrantyResultCard.getChildren().add(guideLbl);

        recentWarrantiesContainer.getChildren().clear();
        List<com.huitshop.dto.WarrantyDtos.WarrantyDto> recents = warrantyService.getRecentWarranties();
        if (recents.isEmpty()) {
            recentWarrantiesContainer.getChildren().add(new Label("Chưa có thông tin bảo hành nào trong CSDL."));
        } else {
            for (com.huitshop.dto.WarrantyDtos.WarrantyDto dto : recents) {
                HBox rRow = new HBox(10);
                rRow.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 6px; -fx-padding: 8px;");
                rRow.setAlignment(Pos.CENTER_LEFT);
                
                VBox textInfo = new VBox(2);
                Label nameLbl = new Label(dto.getProductName() + " (" + dto.getSerialNumber() + ")");
                nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                nameLbl.setTextFill(Color.WHITE);
                
                String dateText = dto.getExpireDate() != null ? "Hạn: " + dto.getExpireDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Chưa kích hoạt";
                Label dateLbl = new Label(dateText);
                dateLbl.getStyleClass().add("label-muted");
                dateLbl.setFont(Font.font("Segoe UI", 11));
                textInfo.getChildren().addAll(nameLbl, dateLbl);
                
                Region rSpacer = new Region();
                HBox.setHgrow(rSpacer, Priority.ALWAYS);
                
                Button quickCheck = new Button("👁️");
                quickCheck.setStyle("-fx-background-color: transparent; -fx-text-fill: #38bdf8; -fx-font-size: 14px; -fx-cursor: hand;");
                quickCheck.setOnAction(e -> {
                    warrantySearchField.setText(dto.getSerialNumber());
                    performWarrantySearch();
                });
                
                rRow.getChildren().addAll(textInfo, rSpacer, quickCheck);
                recentWarrantiesContainer.getChildren().add(rRow);
            }
        }
    }

    private Image getProductImage(String dbPath) {
        if (dbPath == null || dbPath.isEmpty()) {
            return null;
        }
        String filename = dbPath;
        if (dbPath.contains("/")) {
            filename = dbPath.substring(dbPath.lastIndexOf("/") + 1);
        } else if (dbPath.contains("\\")) {
            filename = dbPath.substring(dbPath.lastIndexOf("\\") + 1);
        }
        String resourcePath = "/com/huitshop/Anh/" + filename;
        try {
            var stream = getClass().getResourceAsStream(resourcePath);
            if (stream != null) {
                return new Image(stream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================================
    // HELPERS & DIALOGS
    // =========================================================================
    private String formatMoney(BigDecimal val) {
        if (val == null) return "0";
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(val.doubleValue());
    }

    private void alertUser(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("dialog-pane");
        dialogPane.getStylesheets().add(getClass().getResource("/com/huitshop/css/style.css").toExternalForm());
        
        alert.showAndWait();
    }

    // Custom layout components
    private static class Spacer extends Region {
        public Spacer() {
            VBox.setVgrow(this, Priority.ALWAYS);
            HBox.setHgrow(this, Priority.ALWAYS);
        }
    }
}
