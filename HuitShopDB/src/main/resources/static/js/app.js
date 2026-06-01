/**
 * HuitShop Single Page Application (SPA) Engine
 * Handles routing, DOM rendering, state management, and REST API communication.
 */

// --- Global App State ---
const state = {
    user: null, // Holds AuthResponseDto: { id, email, fullName, role, accessToken }
    activePanel: 'shop',
    cart: { id: 0, cartItems: [], voucherCode: null },
    catalog: {
        items: [],
        categories: [],
        brands: [],
        filter: { categoryId: null, brandId: null, search: '', sortBy: '', page: 1, pageSize: 12 },
        totalPages: 1
    },
    activeDetailProduct: null,
    activeDetailVariant: null,
    admin: {
        products: [],
        warehouses: [],
        suppliers: [],
        variants: [],
        filter: { search: '', categoryId: '', status: '', page: 1, pageSize: 10 },
        totalPages: 1
    }
};

// --- Toast Notifications ---
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let iconClass = 'fa-check-circle';
    if (type === 'danger') iconClass = 'fa-exclamation-circle';
    if (type === 'warning') iconClass = 'fa-exclamation-triangle';
    
    toast.innerHTML = `
        <i class="fa-solid ${iconClass}"></i>
        <span>${message}</span>
    `;
    container.appendChild(toast);
    
    // Animate in
    setTimeout(() => toast.classList.add('show'), 10);
    
    // Remove after 3.5s
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    }, 3500);
}

// --- Fetch Wrapper ---
async function apiCall(endpoint, options = {}) {
    const url = endpoint.startsWith('http') ? endpoint : endpoint;
    const defaultHeaders = {
        'Content-Type': 'application/json'
    };
    
    if (state.user && state.user.accessToken) {
        defaultHeaders['Authorization'] = `Bearer ${state.user.accessToken}`;
    }
    
    const config = {
        ...options,
        headers: {
            ...defaultHeaders,
            ...options.headers
        }
    };
    
    try {
        const response = await fetch(url, config);
        const text = await response.text();
        
        let data;
        try {
            data = text ? JSON.parse(text) : {};
        } catch (e) {
            data = text; // Return raw text if not JSON
        }
        
        if (!response.ok) {
            throw new Error(data.message || data || 'Lỗi kết nối server');
        }
        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// --- App Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    // Check local storage for user session
    const savedUser = localStorage.getItem('huitshop_user');
    if (savedUser) {
        state.user = JSON.parse(savedUser);
        showApp();
    } else {
        showAuth();
    }

    // Attach Event Listeners
    setupAuthListeners();
    setupModalListeners();
    setupNavbarListeners();
});

// --- Screen Switching ---
function showAuth() {
    document.getElementById('auth-screen').classList.remove('hidden');
    document.getElementById('app-screen').classList.add('hidden');
    state.user = null;
    localStorage.removeItem('huitshop_user');
}

function showApp() {
    document.getElementById('auth-screen').classList.add('hidden');
    document.getElementById('app-screen').classList.remove('hidden');
    
    // Set user labels
    document.getElementById('user-display-name').textContent = state.user.fullName;
    document.getElementById('user-display-email').textContent = state.user.email;
    document.getElementById('user-avatar-char').textContent = state.user.fullName.charAt(0).toUpperCase();
    document.getElementById('user-role-badge').textContent = state.user.role;
    
    // Generate Sidebar Menu based on role
    renderSidebarNav();
    
    // Set default panel
    const defaultPanel = (state.user.role === 'CUSTOMER') ? 'shop' : 'admin_dashboard';
    switchPanel(defaultPanel);
    
    // Sync cart on startup
    if (state.user.role === 'CUSTOMER') {
        syncCartCount();
    }
}

// --- Sidebar Menu Generation ---
function renderSidebarNav() {
    const nav = document.getElementById('sidebar-nav-menu');
    nav.innerHTML = '';
    
    if (state.user.role === 'CUSTOMER') {
        nav.innerHTML = `
            <a href="#" class="nav-item active" data-panel="shop"><i class="fa-solid fa-store"></i> Cửa hàng</a>
            <a href="#" class="nav-item" data-panel="cart"><i class="fa-solid fa-cart-shopping"></i> Giỏ hàng</a>
            <a href="#" class="nav-item" data-panel="orders"><i class="fa-solid fa-receipt"></i> Lịch sử mua hàng</a>
            <a href="#" class="nav-item" data-panel="warranty"><i class="fa-solid fa-shield-halved"></i> Tra cứu bảo hành</a>
            <a href="#" class="nav-item" data-panel="profile"><i class="fa-regular fa-id-card"></i> Tài khoản & Sổ địa chỉ</a>
        `;
    } else {
        // ADMIN, STAFF, WAREHOUSE Menu
        nav.innerHTML = `
            <a href="#" class="nav-item active" data-panel="admin_dashboard"><i class="fa-solid fa-chart-pie"></i> Tổng quan KPI</a>
            <a href="#" class="nav-item" data-panel="admin_products"><i class="fa-solid fa-laptop"></i> Quản lý sản phẩm</a>
            <a href="#" class="nav-item" data-panel="admin_logistics"><i class="fa-solid fa-warehouse"></i> Nhập & Chuyển kho</a>
            <a href="#" class="nav-item" data-panel="admin_orders"><i class="fa-solid fa-truck-fast"></i> Xử lý đơn hàng</a>
        `;
        if (state.user.role === 'ADMIN') {
            nav.innerHTML += `
                <a href="#" class="nav-item" data-panel="admin_users"><i class="fa-solid fa-user-gear"></i> Phân quyền user</a>
            `;
        }
    }
    
    // Sidebar Navigation Click Binding
    nav.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            nav.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            switchPanel(item.getAttribute('data-panel'));
        });
    });
}

// --- Dynamic Panel Router ---
function switchPanel(panelName) {
    state.activePanel = panelName;
    const container = document.getElementById('panel-container');
    container.innerHTML = `<div class="loading-spinner"><i class="fa-solid fa-circle-notch fa-spin"></i> Đang tải dữ liệu...</div>`;
    
    // Switch logic
    switch(panelName) {
        case 'shop':
            loadCatalogPage();
            break;
        case 'cart':
            loadCartPage();
            break;
        case 'orders':
            loadOrdersPage();
            break;
        case 'warranty':
            loadWarrantyPage();
            break;
        case 'profile':
            loadProfilePage();
            break;
        case 'admin_dashboard':
            loadAdminDashboardPage();
            break;
        case 'admin_products':
            loadAdminProductsPage();
            break;
        case 'admin_logistics':
            loadAdminLogisticsPage();
            break;
        case 'admin_orders':
            loadAdminOrdersPage();
            break;
        case 'admin_users':
            loadAdminUsersPage();
            break;
        default:
            container.innerHTML = `<h2>Panel ${panelName} chưa sẵn sàng.</h2>`;
    }
}

// --- Cart Sync Badge ---
async function syncCartCount() {
    try {
        const cart = await apiCall(`/api/cart/${state.user.id}`);
        state.cart = cart;
        const totalItems = cart.cartItems.reduce((acc, item) => acc + item.quantity, 0);
        document.getElementById('cart-count').textContent = totalItems;
    } catch (e) {
        console.error('Failed to sync cart count:', e);
    }
}

// ==========================================
// CUSTOMER CHANNELS
// ==========================================

// --- Shop Catalog Panel ---
async function loadCatalogPage() {
    try {
        // Fetch catalogs if not loaded
        if (state.catalog.categories.length === 0) {
            state.catalog.categories = await apiCall('/api/products/categories');
            state.catalog.brands = await apiCall('/api/products/brands');
        }
        
        // Fetch products
        await refreshCatalogProducts();
        
        renderCatalogLayout();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

async function refreshCatalogProducts() {
    const f = state.catalog.filter;
    let query = `?page=${f.page}&pageSize=${f.pageSize}`;
    if (f.categoryId) query += `&categoryId=${f.categoryId}`;
    if (f.brandId) query += `&brandId=${f.brandId}`;
    if (f.search) query += `&search=${encodeURIComponent(f.search)}`;
    if (f.sortBy) query += `&sortBy=${f.sortBy}`;
    
    const resp = await apiCall(`/api/products${query}`);
    state.catalog.items = resp.items;
    state.catalog.totalPages = resp.totalPages;
}

function renderCatalogLayout() {
    const container = document.getElementById('panel-container');
    container.innerHTML = `
        <div class="panel-header">
            <div>
                <h1 class="panel-title">Cửa Hàng HuitShop</h1>
                <p class="panel-subtitle">Khám phá máy tính xách tay và linh kiện chính hãng</p>
            </div>
            <div class="catalog-sort-filter">
                <select id="catalog-sort" class="form-control" style="width: 180px;">
                    <option value="">Sắp xếp mặc định</option>
                    <option value="price_asc" ${state.catalog.filter.sortBy === 'price_asc' ? 'selected' : ''}>Giá: Thấp đến Cao</option>
                    <option value="price_desc" ${state.catalog.filter.sortBy === 'price_desc' ? 'selected' : ''}>Giá: Cao đến Thấp</option>
                    <option value="name" ${state.catalog.filter.sortBy === 'name' ? 'selected' : ''}>Tên sản phẩm (A-Z)</option>
                </select>
            </div>
        </div>
        
        <div class="catalog-layout">
            <!-- Sidebar filters -->
            <aside class="filter-sidebar glass-panel">
                <div class="filter-section">
                    <h3>Danh Mục Sản Phẩm</h3>
                    <div class="filter-options" id="category-filter-list">
                        <div class="filter-btn-item ${state.catalog.filter.categoryId === null ? 'active' : ''}" data-cat="">Tất cả danh mục</div>
                        ${state.catalog.categories.map(c => `
                            <div class="filter-btn-item ${state.catalog.filter.categoryId === c.id ? 'active' : ''}" data-cat="${c.id}">${c.name}</div>
                        `).join('')}
                    </div>
                </div>
                
                <div class="filter-section">
                    <h3>Thương Hiệu</h3>
                    <div class="filter-options" id="brand-filter-list">
                        <div class="filter-btn-item ${state.catalog.filter.brandId === null ? 'active' : ''}" data-brand="">Tất cả thương hiệu</div>
                        ${state.catalog.brands.map(b => `
                            <div class="filter-btn-item ${state.catalog.filter.brandId === b.id ? 'active' : ''}" data-brand="${b.id}">${b.name}</div>
                        `).join('')}
                    </div>
                </div>
            </aside>
            
            <!-- Products View -->
            <div class="catalog-products-view">
                <div class="product-grid" id="catalog-product-grid">
                    <!-- Products -->
                </div>
                
                <!-- Pagination -->
                <div class="pagination" id="catalog-pagination"></div>
            </div>
        </div>
    `;
    
    // Render products list inside grid
    renderProductCards();
    renderCatalogPagination();
    
    // Bind catalog listeners
    document.getElementById('catalog-sort').addEventListener('change', async (e) => {
        state.catalog.filter.sortBy = e.target.value;
        state.catalog.filter.page = 1;
        await refreshCatalogProducts();
        renderProductCards();
        renderCatalogPagination();
    });
    
    document.getElementById('category-filter-list').querySelectorAll('.filter-btn-item').forEach(item => {
        item.addEventListener('click', async () => {
            document.getElementById('category-filter-list').querySelectorAll('.filter-btn-item').forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            const val = item.getAttribute('data-cat');
            state.catalog.filter.categoryId = val ? parseInt(val) : null;
            state.catalog.filter.page = 1;
            await refreshCatalogProducts();
            renderProductCards();
            renderCatalogPagination();
        });
    });
    
    document.getElementById('brand-filter-list').querySelectorAll('.filter-btn-item').forEach(item => {
        item.addEventListener('click', async () => {
            document.getElementById('brand-filter-list').querySelectorAll('.filter-btn-item').forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            const val = item.getAttribute('data-brand');
            state.catalog.filter.brandId = val ? parseInt(val) : null;
            state.catalog.filter.page = 1;
            await refreshCatalogProducts();
            renderProductCards();
            renderCatalogPagination();
        });
    });
}

function renderProductCards() {
    const grid = document.getElementById('catalog-product-grid');
    grid.innerHTML = '';
    
    if (state.catalog.items.length === 0) {
        grid.innerHTML = `<div class="empty-state" style="grid-column: 1/-1; padding: 40px; text-align: center; color: var(--text-muted);">
            <i class="fa-solid fa-laptop-code" style="font-size: 40px; margin-bottom: 12px; color: var(--primary);"></i>
            <p>Không tìm thấy sản phẩm nào khớp với bộ lọc của bạn.</p>
        </div>`;
        return;
    }
    
    state.catalog.items.forEach(p => {
        const card = document.createElement('div');
        card.className = 'product-card glass-panel';
        
        // Resolve image URL
        // If image is relative classpath style, rewrite to WebConfig endpoint
        let imgUrl = p.thumbnailUrl || 'no-image.png';
        if (!imgUrl.startsWith('http') && !imgUrl.startsWith('/')) {
            imgUrl = `/com/huitshop/Anh/${imgUrl}`;
        }
        
        card.innerHTML = `
            <div class="product-img-wrapper">
                ${p.featured ? `<span class="featured-badge">Bán Chạy</span>` : ''}
                <img class="product-img" src="${imgUrl}" alt="${p.name}" onerror="this.src='https://placehold.co/200x150/111827/FFFFFF?text=Laptop'">
            </div>
            <div class="product-body">
                <span class="product-brand">${p.brand ? p.brand.name : 'Chính Hãng'}</span>
                <h3 class="product-name" title="${p.name}">${p.name}</h3>
                <div class="product-rating">
                    <i class="fa-solid fa-star"></i>
                    <span>${p.ratingAverage.toFixed(1)}</span>
                    <span class="count">(${p.reviewCount} đánh giá)</span>
                </div>
                <div class="product-price-row">
                    <div>
                        <span class="product-price">${formatVND(p.priceFrom)}</span>
                        ${p.priceTo && p.priceTo.compareTo(p.priceFrom) > 0 ? ` - <span class="product-price">${formatVND(p.priceTo)}</span>` : ''}
                    </div>
                </div>
            </div>
        `;
        card.addEventListener('click', () => openProductDetail(p.id));
        grid.appendChild(card);
    });
}

function renderCatalogPagination() {
    const pag = document.getElementById('catalog-pagination');
    pag.innerHTML = '';
    
    if (state.catalog.totalPages <= 1) return;
    
    const curr = state.catalog.filter.page;
    
    // Prev button
    const prev = document.createElement('button');
    prev.className = `btn btn-secondary btn-sm ${curr === 1 ? 'disabled' : ''}`;
    prev.innerHTML = `<i class="fa-solid fa-chevron-left"></i>`;
    if (curr > 1) {
        prev.addEventListener('click', async () => {
            state.catalog.filter.page--;
            await refreshCatalogProducts();
            renderProductCards();
            renderCatalogPagination();
        });
    }
    pag.appendChild(prev);
    
    // Page buttons
    for (let i = 1; i <= state.catalog.totalPages; i++) {
        const btn = document.createElement('button');
        btn.className = `btn btn-sm ${curr === i ? 'btn-primary' : 'btn-secondary'}`;
        btn.textContent = i;
        btn.addEventListener('click', async () => {
            state.catalog.filter.page = i;
            await refreshCatalogProducts();
            renderProductCards();
            renderCatalogPagination();
        });
        pag.appendChild(btn);
    }
    
    // Next button
    const next = document.createElement('button');
    next.className = `btn btn-secondary btn-sm ${curr === state.catalog.totalPages ? 'disabled' : ''}`;
    next.innerHTML = `<i class="fa-solid fa-chevron-right"></i>`;
    if (curr < state.catalog.totalPages) {
        next.addEventListener('click', async () => {
            state.catalog.filter.page++;
            await refreshCatalogProducts();
            renderProductCards();
            renderCatalogPagination();
        });
    }
    pag.appendChild(next);
}

// --- Cart Panel ---
async function loadCartPage() {
    try {
        const container = document.getElementById('panel-container');
        const cart = await apiCall(`/api/cart/${state.user.id}`);
        state.cart = cart;
        
        container.innerHTML = `
            <div class="panel-header">
                <div>
                    <h1 class="panel-title"><i class="fa-solid fa-cart-shopping"></i> Giỏ Hàng Của Bạn</h1>
                    <p class="panel-subtitle">Kiểm tra thông tin sản phẩm và áp dụng ưu đãi trước khi thanh toán</p>
                </div>
                <button class="btn btn-secondary btn-sm" id="btn-clear-cart">Xóa tất cả</button>
            </div>
            
            <div class="cart-layout" id="cart-content-layout">
                <!-- Cart items list dynamically rendered -->
            </div>
        `;
        
        renderCartItems();
        
        document.getElementById('btn-clear-cart').addEventListener('click', async () => {
            if (confirm('Bạn có muốn xóa toàn bộ giỏ hàng?')) {
                await apiCall(`/api/cart/${state.user.id}`, { method: 'DELETE' });
                showToast('Đã làm sạch giỏ hàng');
                loadCartPage();
                syncCartCount();
            }
        });
    } catch(e) {
        showToast(e.message, 'danger');
    }
}

function renderCartItems() {
    const layout = document.getElementById('cart-content-layout');
    
    if (state.cart.cartItems.length === 0) {
        layout.outerHTML = `
            <div class="empty-state" style="padding: 60px; text-align: center; color: var(--text-muted);">
                <i class="fa-solid fa-cart-arrow-down" style="font-size: 60px; margin-bottom: 20px; color: var(--primary);"></i>
                <h2>Giỏ hàng của bạn đang trống</h2>
                <p style="margin: 12px 0 24px;">Hãy khám phá các sản phẩm tuyệt vời của cửa hàng và thêm chúng vào giỏ hàng!</p>
                <button class="btn btn-primary" onclick="switchPanel('shop')">Mua Sắm Ngay <i class="fa-solid fa-store"></i></button>
            </div>
        `;
        return;
    }
    
    // Calculate values
    let subtotal = 0;
    state.cart.cartItems.forEach(ci => {
        const price = ci.productVariant ? ci.productVariant.price : 0;
        subtotal += price * ci.quantity;
    });
    
    // Simple shipping rule: free if order >= 500k, else 30k
    const shipping = subtotal >= 500000 ? 0 : 30000;
    
    // Mock Voucher discount mapping (actual calculation happens on placing order, here we fetch code validation details or mock check)
    let discount = 0;
    
    const total = subtotal - discount + shipping;
    
    layout.innerHTML = `
        <div class="cart-items-container">
            ${state.cart.cartItems.map(item => {
                const pv = item.productVariant;
                const pName = pv.product ? pv.product.name : 'Sản phẩm';
                const fullName = pName + (pv.variantName ? ` - ${pv.variantName}` : '');
                
                let imgUrl = pv.thumbnailUrl || 'no-image.png';
                if (!imgUrl.startsWith('http') && !imgUrl.startsWith('/')) {
                    imgUrl = `/com/huitshop/Anh/${imgUrl}`;
                }
                
                return `
                    <div class="cart-item-row glass-panel">
                        <img class="cart-item-img" src="${imgUrl}" alt="${fullName}" onerror="this.src='https://placehold.co/100x100/111827/FFFFFF?text=Laptop'">
                        <div class="cart-item-details">
                            <h3 class="cart-item-name">${fullName}</h3>
                            <p class="cart-item-sku">SKU: ${pv.sku}</p>
                            <p class="cart-item-price">${formatVND(pv.price)}</p>
                        </div>
                        <div class="quantity-spinner">
                            <button class="btn-spinner dec" data-item-id="${item.id}" data-qty="${item.quantity}">-</button>
                            <input type="number" readonly value="${item.quantity}">
                            <button class="btn-spinner inc" data-item-id="${item.id}" data-qty="${item.quantity}">+</button>
                        </div>
                        <button class="btn-remove-item" data-item-id="${item.id}"><i class="fa-solid fa-trash-can"></i></button>
                    </div>
                `;
            }).join('')}
        </div>
        
        <div class="cart-summary-card glass-panel">
            <h2 class="form-title" style="text-align: left; border-bottom: 1px solid var(--border-glass); padding-bottom: 12px; margin-bottom: 15px;">Tổng Đơn Hàng</h2>
            
            <div class="summary-row">
                <span>Tạm tính</span>
                <span>${formatVND(subtotal)}</span>
            </div>
            
            <div class="summary-row">
                <span>Phí vận chuyển</span>
                <span>${shipping === 0 ? '<span style="color:var(--success);">Miễn phí</span>' : formatVND(shipping)}</span>
            </div>
            
            <div class="summary-row" id="cart-discount-row" style="display:none;">
                <span>Giảm giá (Voucher)</span>
                <span id="cart-discount-value" style="color:var(--success);">-0đ</span>
            </div>

            <!-- Voucher Code Entry -->
            <div class="voucher-input-section" style="margin: 10px 0;">
                <div class="form-group" style="margin-bottom:8px;">
                    <label style="font-size:11px;">Mã giảm giá (Voucher)</label>
                    <div style="display:flex; gap:8px;">
                        <input type="text" id="voucher-code" placeholder="HUIT50K, AP10..." value="${state.cart.voucherCode || ''}">
                        <button class="btn btn-secondary btn-sm" id="btn-apply-voucher">Áp dụng</button>
                    </div>
                </div>
            </div>
            
            <div class="summary-row total">
                <span>Tổng cộng</span>
                <span id="cart-total-value">${formatVND(total)}</span>
            </div>
            
            <!-- Checkout fields -->
            <h3 class="section-title-divider" style="margin-top:15px;">Thông tin nhận hàng</h3>
            <div class="form-group">
                <label>Địa chỉ nhận hàng</label>
                <select id="checkout-address-select" class="form-control">
                    <!-- Address list -->
                </select>
                <small style="color: var(--text-muted); display:block; margin-top:5px;">Bạn có thể cấu hình thêm sổ địa chỉ trong trang Profile.</small>
            </div>
            
            <div class="form-group">
                <label>Ghi chú đơn hàng</label>
                <input type="text" id="checkout-note" placeholder="Nhờ giao hàng giờ hành chính...">
            </div>
            
            <div class="form-group">
                <label>Phương thức thanh toán</label>
                <select id="checkout-payment-method" class="form-control">
                    <option value="COD">Thanh toán khi nhận hàng (COD)</option>
                    <option value="BANK_TRANSFER">Chuyển khoản ngân hàng</option>
                </select>
            </div>
            
            <button class="btn btn-primary btn-block" id="btn-checkout">
                Thanh Toán Đơn Hàng <i class="fa-solid fa-credit-card"></i>
            </button>
        </div>
    `;
    
    // Bind events
    layout.querySelectorAll('.btn-spinner.dec').forEach(btn => {
        btn.addEventListener('click', async () => {
            const itemId = parseInt(btn.getAttribute('data-item-id'));
            const qty = parseInt(btn.getAttribute('data-qty')) - 1;
            await apiCall(`/api/cart/${state.user.id}/items/${itemId}`, {
                method: 'PUT',
                body: JSON.stringify({ quantity: qty })
            });
            loadCartPage();
            syncCartCount();
        });
    });
    
    layout.querySelectorAll('.btn-spinner.inc').forEach(btn => {
        btn.addEventListener('click', async () => {
            const itemId = parseInt(btn.getAttribute('data-item-id'));
            const qty = parseInt(btn.getAttribute('data-qty')) + 1;
            await apiCall(`/api/cart/${state.user.id}/items/${itemId}`, {
                method: 'PUT',
                body: JSON.stringify({ quantity: qty })
            });
            loadCartPage();
            syncCartCount();
        });
    });
    
    layout.querySelectorAll('.btn-remove-item').forEach(btn => {
        btn.addEventListener('click', async () => {
            const itemId = parseInt(btn.getAttribute('data-item-id'));
            if (confirm('Xóa sản phẩm này khỏi giỏ hàng?')) {
                await apiCall(`/api/cart/${state.user.id}/items/${itemId}`, { method: 'DELETE' });
                showToast('Đã xóa sản phẩm');
                loadCartPage();
                syncCartCount();
            }
        });
    });
    
    // Apply voucher
    document.getElementById('btn-apply-voucher').addEventListener('click', async () => {
        const code = document.getElementById('voucher-code').value.trim();
        try {
            await apiCall(`/api/cart/${state.user.id}/voucher`, {
                method: 'POST',
                body: JSON.stringify({ voucherCode: code })
            });
            showToast('Đã cập nhật mã giảm giá');
            loadCartPage();
        } catch(e) {
            showToast(e.message, 'danger');
        }
    });
    
    // Fetch customer addresses for select dropdown
    apiCall(`/api/users/${state.user.id}/addresses`).then(addrs => {
        const sel = document.getElementById('checkout-address-select');
        sel.innerHTML = '';
        if (addrs.length === 0) {
            sel.innerHTML = `<option value="">-- Chưa có địa chỉ nào! --</option>`;
            return;
        }
        addrs.forEach(a => {
            const fullStr = `${a.receiverName} (${a.receiverPhone}) - ${a.streetAddress}, ${a.ward}, ${a.district}, ${a.province}`;
            const addressJson = JSON.stringify({
                receiver_name: a.receiverName,
                receiver_phone: a.receiverPhone,
                street_address: a.streetAddress,
                ward: a.ward,
                district: a.district,
                province: a.province
            });
            const opt = document.createElement('option');
            opt.value = addressJson;
            opt.textContent = `${a.label ? `[${a.label}] ` : ''}${fullStr.substring(0, 50)}...`;
            if (a.default) opt.selected = true;
            sel.appendChild(opt);
        });
    });
    
    // Place order checkout submission
    document.getElementById('btn-checkout').addEventListener('click', async () => {
        const addrJson = document.getElementById('checkout-address-select').value;
        if (!addrJson) {
            showToast('Vui lòng chọn hoặc thêm địa chỉ nhận hàng trước khi thanh toán!', 'warning');
            return;
        }
        
        const req = {
            paymentMethod: document.getElementById('checkout-payment-method').value,
            shippingAddressJson: addrJson,
            note: document.getElementById('checkout-note').value
        };
        
        try {
            document.getElementById('btn-checkout').disabled = true;
            document.getElementById('btn-checkout').innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...`;
            
            const orderResp = await apiCall(`/api/orders/${state.user.id}`, {
                method: 'POST',
                body: JSON.stringify(req)
            });
            
            showToast(`Đặt hàng thành công! Mã đơn: ${orderResp.code}`);
            syncCartCount();
            switchPanel('orders');
        } catch(e) {
            showToast(e.message, 'danger');
            document.getElementById('btn-checkout').disabled = false;
            document.getElementById('btn-checkout').innerHTML = `Thanh Toán Đơn Hàng <i class="fa-solid fa-credit-card"></i>`;
        }
    });
}

// --- Orders History Panel ---
async function loadOrdersPage() {
    try {
        const container = document.getElementById('panel-container');
        const orders = await apiCall(`/api/orders/user/${state.user.id}`);
        
        container.innerHTML = `
            <div class="panel-header">
                <div>
                    <h1 class="panel-title"><i class="fa-solid fa-receipt"></i> Lịch Sử Đơn Hàng</h1>
                    <p class="panel-subtitle">Theo dõi trạng thái giao hàng và chi tiết sản phẩm đã mua</p>
                </div>
            </div>
            
            <div class="orders-table-container glass-panel">
                <table class="premium-table">
                    <thead>
                        <tr>
                            <th>Mã đơn hàng</th>
                            <th>Ngày đặt</th>
                            <th>Địa chỉ nhận</th>
                            <th>Hình thức</th>
                            <th>Trạng thái</th>
                            <th>Tổng tiền</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody id="customer-orders-tbody">
                        <!-- rows -->
                    </tbody>
                </table>
            </div>
        `;
        
        const tbody = document.getElementById('customer-orders-tbody');
        if (orders.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted); padding:30px;">Bạn chưa thực hiện đơn hàng nào!</td></tr>`;
            return;
        }
        
        orders.forEach(o => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong style="color:var(--primary);">${o.code}</strong></td>
                <td>${formatDate(o.createdAt)}</td>
                <td title="${o.fullAddress}">${o.recipientName} - ${o.fullAddress.substring(0, 30)}...</td>
                <td>${o.paymentMethod}</td>
                <td><span class="status-badge status-${o.status.toLowerCase()}">${translateStatus(o.status)}</span></td>
                <td><strong>${formatVND(o.total)}</strong></td>
                <td>
                    <button class="btn btn-secondary btn-sm btn-view-order" data-code="${o.code}"><i class="fa-regular fa-eye"></i> Xem</button>
                    ${o.status === 'PENDING' ? `<button class="btn btn-danger btn-sm btn-cancel-order" data-id="${o.id}"><i class="fa-solid fa-ban"></i> Hủy</button>` : ''}
                </td>
            `;
            
            tr.querySelector('.btn-view-order').addEventListener('click', (e) => {
                e.stopPropagation();
                openOrderDetailsModal(o.code);
            });
            
            if (o.status === 'PENDING') {
                tr.querySelector('.btn-cancel-order').addEventListener('click', async (e) => {
                    e.stopPropagation();
                    const reason = prompt('Nhập lý do hủy đơn hàng:');
                    if (reason !== null) {
                        try {
                            await apiCall(`/api/orders/${o.id}/cancel`, {
                                method: 'POST',
                                body: JSON.stringify({ reason: reason || 'Khách hàng yêu cầu hủy đơn' })
                            });
                            showToast('Đã gửi yêu cầu hủy đơn hàng');
                            loadOrdersPage();
                        } catch(err) {
                            showToast(err.message, 'danger');
                        }
                    }
                });
            }
            tbody.appendChild(tr);
        });
    } catch(e) {
        showToast(e.message, 'danger');
    }
}

// --- Warranty Lookup Panel ---
async function loadWarrantyPage() {
    const container = document.getElementById('panel-container');
    container.innerHTML = `
        <div class="panel-header">
            <div>
                <h1 class="panel-title"><i class="fa-solid fa-shield-halved"></i> Tra Cứu Thông Tin Bảo Hành</h1>
                <p class="panel-subtitle">Kiểm tra thời hạn bảo hành phần cứng sản phẩm bằng số Serial Number / IMEI</p>
            </div>
        </div>
        
        <div class="warranty-lookup-card glass-panel">
            <h2>Tra cứu thời hạn bảo hành</h2>
            <p>Vui lòng nhập chính xác số Serial Number ghi trên vỏ máy hoặc hóa đơn mua hàng.</p>
            
            <div class="search-input-group">
                <input type="text" id="warranty-search-serial" placeholder="Ví dụ: SN-ASUS-9801..." required>
                <button class="btn btn-primary" id="btn-search-warranty">Tra Cứu <i class="fa-solid fa-magnifying-glass"></i></button>
            </div>
            
            <div id="warranty-lookup-result" class="hidden">
                <!-- Result card loaded here -->
            </div>
        </div>
        
        <div class="panel-header" style="margin-top: 40px; margin-bottom: 15px;">
            <h2 class="panel-title" style="font-size: 18px;">Lịch sử tra cứu gần đây</h2>
        </div>
        
        <div class="orders-table-container glass-panel">
            <table class="premium-table">
                <thead>
                    <tr>
                        <th>Số Serial</th>
                        <th>Sản phẩm</th>
                        <th>Khách hàng mua</th>
                        <th>Ngày bán</th>
                        <th>Ngày hết hạn</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>
                <tbody id="recent-warranties-tbody">
                    <!-- warranty logs -->
                </tbody>
            </table>
        </div>
    `;
    
    // Bind search event
    document.getElementById('btn-search-warranty').addEventListener('click', performWarrantyLookup);
    document.getElementById('warranty-search-serial').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') performWarrantyLookup();
    });
    
    // Load recent lookup table
    loadRecentWarrantiesLogs();
}

async function performWarrantyLookup() {
    const serial = document.getElementById('warranty-search-serial').value.trim();
    if (!serial) {
        showToast('Vui lòng nhập số Serial', 'warning');
        return;
    }
    
    const resultBox = document.getElementById('warranty-lookup-result');
    resultBox.classList.add('hidden');
    
    try {
        const res = await apiCall(`/api/warranties/search?serialNumber=${encodeURIComponent(serial)}`);
        
        resultBox.className = 'warranty-result-card glass-panel active';
        resultBox.classList.remove('hidden');
        
        resultBox.innerHTML = `
            <h3 class="gradient-text" style="font-size: 18px; margin-bottom: 12px;"><i class="fa-solid fa-circle-check"></i> Thông tin Bảo Hành</h3>
            <div class="warranty-result-grid">
                <div class="warranty-result-item">
                    <span>Số Serial/IMEI</span>
                    <strong>${res.serialNumber}</strong>
                </div>
                <div class="warranty-result-item">
                    <span>Tên sản phẩm</span>
                    <strong>${res.productName}</strong>
                </div>
                <div class="warranty-result-item">
                    <span>Khách hàng</span>
                    <strong>${res.customerName || '<Chưa bán>'}</strong>
                </div>
                <div class="warranty-result-item">
                    <span>Trạng thái bảo hành</span>
                    <strong style="color:${getWarrantyStatusColor(res.status)};">${translateWarrantyStatus(res.status)}</strong>
                </div>
                <div class="warranty-result-item">
                    <span>Ngày kích hoạt</span>
                    <strong>${res.outboundDate ? formatDateOnly(res.outboundDate) : 'Chưa kích hoạt'}</strong>
                </div>
                <div class="warranty-result-item">
                    <span>Ngày hết hạn</span>
                    <strong>${res.expireDate ? formatDateOnly(res.expireDate) : 'Không có'}</strong>
                </div>
                <div class="warranty-result-item" style="grid-column: 1/-1;">
                    <span>Số ngày bảo hành còn lại</span>
                    <strong style="font-size:18px; color:var(--primary);">${res.daysRemaining} ngày</strong>
                </div>
            </div>
        `;
    } catch(e) {
        resultBox.className = 'warranty-result-card glass-panel error';
        resultBox.classList.remove('hidden');
        resultBox.innerHTML = `
            <p style="color:var(--danger); font-weight:600;"><i class="fa-solid fa-circle-exclamation"></i> Không tìm thấy số Serial "${serial}"</p>
            <p style="font-size:12px; color:var(--text-muted); margin-top:5px;">Vui lòng kiểm tra lại kí tự hoặc liên hệ bộ phận hỗ trợ kỹ thuật.</p>
        `;
    }
}

async function loadRecentWarrantiesLogs() {
    try {
        const tbody = document.getElementById('recent-warranties-tbody');
        const logs = await apiCall('/api/warranties/recent');
        
        tbody.innerHTML = '';
        if (logs.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--text-muted); padding:20px;">Không có nhật ký bảo hành nào gần đây.</td></tr>`;
            return;
        }
        
        logs.forEach(l => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${l.serialNumber}</strong></td>
                <td>${l.productName} ${l.variantName ? `(${l.variantName})` : ''}</td>
                <td>${l.customerName || '<Trong kho>'}</td>
                <td>${l.outboundDate ? formatDateOnly(l.outboundDate) : 'Chưa xuất'}</td>
                <td>${l.expireDate ? formatDateOnly(l.expireDate) : 'Chưa kích hoạt'}</td>
                <td><span class="status-badge" style="background:${getWarrantyStatusBg(l.status)}; color:${getWarrantyStatusColor(l.status)}; border:1px solid ${getWarrantyStatusColor(l.status)};">${translateWarrantyStatus(l.status)}</span></td>
            `;
            tbody.appendChild(tr);
        });
    } catch(e) {
        console.error('Failed to load recent warranties logs:', e);
    }
}

// --- Customer Profile & Address Book Panel ---
async function loadProfilePage() {
    const container = document.getElementById('panel-container');
    container.innerHTML = `
        <div class="panel-header">
            <div>
                <h1 class="panel-title"><i class="fa-regular fa-id-card"></i> Quản Lý Tài Khoản & Sổ Địa Chỉ</h1>
                <p class="panel-subtitle">Thay đổi thông tin liên lạc cá nhân và thiết lập địa chỉ giao hàng mặc định</p>
            </div>
        </div>
        
        <div class="form-grid-2">
            <!-- Account Form -->
            <div class="profile-card glass-panel" style="padding: 24px;">
                <h2 class="form-title" style="text-align:left; border-bottom:1px solid var(--border-glass); padding-bottom:10px;">Thông tin tài khoản</h2>
                <form id="profile-edit-form">
                    <div class="form-group">
                        <label>Họ và Tên</label>
                        <input type="text" id="profile-fullname" required value="${state.user.fullName}">
                    </div>
                    <div class="form-group">
                        <label>Email tài khoản</label>
                        <input type="email" readonly value="${state.user.email}" style="opacity:0.6; cursor:not-allowed;">
                    </div>
                    <div class="form-group">
                        <label>Số điện thoại</label>
                        <input type="tel" id="profile-phone" placeholder="Nhập số điện thoại">
                    </div>
                    <div class="form-group">
                        <label>Mật khẩu mới (Để trống nếu không muốn đổi)</label>
                        <input type="password" id="profile-password" placeholder="••••••••">
                    </div>
                    <button type="submit" class="btn btn-primary">Lưu Thay Đổi <i class="fa-regular fa-floppy-disk"></i></button>
                </form>
            </div>
            
            <!-- Address Book -->
            <div class="address-book-card glass-panel" style="padding: 24px;">
                <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid var(--border-glass); padding-bottom:10px; margin-bottom:15px;">
                    <h2 class="form-title" style="margin-bottom:0; text-align:left;">Sổ địa chỉ nhận hàng</h2>
                    <button class="btn btn-success btn-sm" id="btn-add-address-trigger"><i class="fa-solid fa-plus"></i> Thêm mới</button>
                </div>
                
                <div class="address-grid" id="profile-address-grid" style="display:flex; flex-direction:column; gap:12px; margin-top:0;">
                    <!-- Addresses list -->
                </div>
            </div>
        </div>

        <!-- Inline Add Address Form Modal Drawer (hidden initially) -->
        <div id="address-form-drawer" class="modal">
            <div class="modal-content glass-panel">
                <span class="close-btn" onclick="document.getElementById('address-form-drawer').classList.remove('active')">&times;</span>
                <h3 class="modal-title">Thêm địa chỉ giao nhận</h3>
                <form id="address-submit-form" style="margin-top:15px;">
                    <div class="form-group">
                        <label>Tên nhãn (ví dụ: Nhà riêng, Văn phòng)</label>
                        <input type="text" id="addr-label" required placeholder="Nhà riêng">
                    </div>
                    <div class="form-grid-2">
                        <div class="form-group">
                            <label>Họ tên người nhận</label>
                            <input type="text" id="addr-name" required placeholder="Tên người nhận">
                        </div>
                        <div class="form-group">
                            <label>SĐT người nhận</label>
                            <input type="tel" id="addr-phone" required placeholder="Số điện thoại">
                        </div>
                    </div>
                    <div class="form-grid-2">
                        <div class="form-group">
                            <label>Tỉnh / Thành phố</label>
                            <input type="text" id="addr-province" required placeholder="Hồ Chí Minh">
                        </div>
                        <div class="form-group">
                            <label>Quận / Huyện</label>
                            <input type="text" id="addr-district" required placeholder="Tân Phú">
                        </div>
                    </div>
                    <div class="form-grid-2">
                        <div class="form-group">
                            <label>Phường / Xã</label>
                            <input type="text" id="addr-ward" required placeholder="Sơn Kỳ">
                        </div>
                        <div class="form-group">
                            <label>Số nhà, Tên đường</label>
                            <input type="text" id="addr-street" required placeholder="140 Lê Trọng Tấn">
                        </div>
                    </div>
                    <div class="form-group" style="display:flex; align-items:center; gap:8px;">
                        <input type="checkbox" id="addr-default" style="width:auto;">
                        <label for="addr-default" style="margin-bottom:0;">Đặt làm địa chỉ mặc định</label>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block">Thêm Mới Địa Chỉ</button>
                </form>
            </div>
        </div>
    `;
    
    // Bind profile submit
    document.getElementById('profile-edit-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const body = {
            fullName: document.getElementById('profile-fullname').value,
            phone: document.getElementById('profile-phone').value,
            password: document.getElementById('profile-password').value
        };
        try {
            await apiCall(`/api/users/profile/${state.user.id}`, {
                method: 'PUT',
                body: JSON.stringify(body)
            });
            showToast('Đã lưu cập nhật thông tin cá nhân');
            state.user.fullName = body.fullName;
            localStorage.setItem('huitshop_user', JSON.stringify(state.user));
            showApp();
        } catch(err) {
            showToast(err.message, 'danger');
        }
    });

    // Populate addresses
    refreshAddressGrid();
    
    // Address drawer trigger
    document.getElementById('btn-add-address-trigger').addEventListener('click', () => {
        document.getElementById('address-form-drawer').classList.add('active');
    });

    // Address submit
    document.getElementById('address-submit-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const addr = {
            label: document.getElementById('addr-label').value,
            receiverName: document.getElementById('addr-name').value,
            receiverPhone: document.getElementById('addr-phone').value,
            province: document.getElementById('addr-province').value,
            district: document.getElementById('addr-district').value,
            ward: document.getElementById('addr-ward').value,
            streetAddress: document.getElementById('addr-street').value,
            default: document.getElementById('addr-default').checked
        };
        try {
            await apiCall(`/api/users/${state.user.id}/addresses`, {
                method: 'POST',
                body: JSON.stringify(addr)
            });
            showToast('Đã thêm địa chỉ mới');
            document.getElementById('address-form-drawer').classList.remove('active');
            document.getElementById('address-submit-form').reset();
            refreshAddressGrid();
        } catch(err) {
            showToast(err.message, 'danger');
        }
    });
}

async function refreshAddressGrid() {
    const grid = document.getElementById('profile-address-grid');
    grid.innerHTML = '';
    
    try {
        const list = await apiCall(`/api/users/${state.user.id}/addresses`);
        if (list.length === 0) {
            grid.innerHTML = `<p style="color:var(--text-muted); font-size:12px; text-align:center;">Bạn chưa lưu địa chỉ nhận hàng nào.</p>`;
            return;
        }
        
        list.forEach(a => {
            const card = document.createElement('div');
            card.className = 'address-card glass-panel';
            card.innerHTML = `
                <div class="address-label">
                    <span>${a.label || 'Địa chỉ'}</span>
                    ${a.default ? `<span class="address-badge-default">Mặc định</span>` : ''}
                </div>
                <div class="address-recipient">${a.receiverName} - ${a.receiverPhone}</div>
                <div class="address-text">${a.streetAddress}, ${a.ward}, ${a.district}, ${a.province}</div>
                <div class="address-actions">
                    ${!a.default ? `<button class="btn btn-secondary btn-sm btn-set-default" data-id="${a.id}">Đặt mặc định</button>` : ''}
                    <button class="btn btn-danger btn-sm btn-delete-addr" data-id="${a.id}"><i class="fa-solid fa-trash-can"></i> Xóa</button>
                </div>
            `;
            
            if (!a.default) {
                card.querySelector('.btn-set-default').addEventListener('click', async () => {
                    await apiCall(`/api/users/${state.user.id}/addresses/${a.id}/default`, { method: 'PUT' });
                    showToast('Đã đổi địa chỉ mặc định');
                    refreshAddressGrid();
                });
            }
            
            card.querySelector('.btn-delete-addr').addEventListener('click', async () => {
                if (confirm('Bạn có muốn xóa địa chỉ này?')) {
                    await apiCall(`/api/users/${state.user.id}/addresses/${a.id}`, { method: 'DELETE' });
                    showToast('Đã xóa địa chỉ');
                    refreshAddressGrid();
                }
            });
            grid.appendChild(card);
        });
    } catch(e) {
        console.error(e);
    }
}

// ==========================================
// ADMIN / STAFF CHANNELS
// ==========================================

// --- Admin Overview KPI Panel ---
async function loadAdminDashboardPage() {
    const container = document.getElementById('panel-container');
    container.innerHTML = `
        <div class="panel-header">
            <div>
                <h1 class="panel-title"><i class="fa-solid fa-chart-pie"></i> Báo Cáo Phân Tích & KPI Kho Hàng</h1>
                <p class="panel-subtitle">Thống kê trạng thái hàng hóa và định vị các điểm sản phẩm chạm ngưỡng reorder</p>
            </div>
        </div>
        
        <!-- Metrics -->
        <div class="kpi-grid" id="admin-kpi-grid">
            <div class="loading-spinner"><i class="fa-solid fa-circle-notch fa-spin"></i></div>
        </div>
        
        <!-- Charts Layout -->
        <div class="admin-charts-container">
            <div class="chart-card glass-panel">
                <h3>Thống Kê Kho Hàng (Số lượng/SKU)</h3>
                <div class="chart-wrapper">
                    <canvas id="admin-bar-chart"></canvas>
                </div>
            </div>
            
            <div class="chart-card glass-panel" style="display:flex; flex-direction:column;">
                <h3>Phân Phối Theo Kho</h3>
                <div class="chart-wrapper" style="flex-grow:1; display:flex; align-items:center; justify-content:center;">
                    <canvas id="admin-doughnut-chart"></canvas>
                </div>
            </div>
        </div>
        
        <!-- Warnings: Reorder report -->
        <div class="panel-header" style="margin-top:20px; margin-bottom:12px;">
            <h2 class="panel-title" style="font-size:18px; color:var(--warning);"><i class="fa-solid fa-triangle-exclamation"></i> Cảnh Báo Sản Phẩm Cần Nhập Kho Gấp</h2>
        </div>
        <div class="orders-table-container glass-panel">
            <table class="premium-table">
                <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>SKU Code</th>
                        <th>Tên phiên bản</th>
                        <th>Tồn kho tổng</th>
                        <th>Ngưỡng tối thiểu</th>
                        <th>Mức độ</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody id="admin-reorder-tbody">
                    <!-- reports -->
                </tbody>
            </table>
        </div>
    `;
    
    try {
        const stats = await apiCall('/api/inventory/analytics');
        const reorders = await apiCall('/api/inventory/reorder-report');
        
        // Populate KPI cards
        const kpi = document.getElementById('admin-kpi-grid');
        kpi.innerHTML = `
            <div class="kpi-card glass-panel">
                <div class="kpi-icon" style="color:var(--primary); background:rgba(99,102,241,0.15);"><i class="fa-solid fa-warehouse"></i></div>
                <div class="kpi-data">
                    <span class="kpi-label">Số Kho Hoạt Động</span>
                    <span class="kpi-value">${stats.totalWarehouses}</span>
                </div>
            </div>
            <div class="kpi-card glass-panel">
                <div class="kpi-icon" style="color:var(--secondary); background:rgba(6,182,212,0.15);"><i class="fa-solid fa-barcode"></i></div>
                <div class="kpi-data">
                    <span class="kpi-label">Mã SKU Phân Loại</span>
                    <span class="kpi-value">${stats.totalSKUs}</span>
                </div>
            </div>
            <div class="kpi-card glass-panel">
                <div class="kpi-icon" style="color:var(--success); background:rgba(16,185,129,0.15);"><i class="fa-solid fa-boxes-stacked"></i></div>
                <div class="kpi-data">
                    <span class="kpi-label">Tổng Hàng Trong Kho</span>
                    <span class="kpi-value">${stats.totalItemsInStock} cái</span>
                </div>
            </div>
            <div class="kpi-card glass-panel">
                <div class="kpi-icon" style="color:var(--warning); background:rgba(245,158,11,0.15);"><i class="fa-solid fa-triangle-exclamation"></i></div>
                <div class="kpi-data">
                    <span class="kpi-label">Mã Chạm Ngưỡng Tối Thiểu</span>
                    <span class="kpi-value" style="color:var(--warning);">${stats.lowStockItemsCount} SKU</span>
                </div>
            </div>
        `;
        
        // Render charts
        renderAdminCharts(stats);
        
        // Populate low stock warnings
        const tbody = document.getElementById('admin-reorder-tbody');
        tbody.innerHTML = '';
        if (reorders.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted); padding:20px;">Tất cả hàng hóa đều ở mức an toàn.</td></tr>`;
            return;
        }
        
        reorders.forEach(r => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${r.productName}</strong></td>
                <td><code>${r.sku}</code></td>
                <td>${r.variantName || 'Mặc định'}</td>
                <td><strong style="color:var(--danger);">${r.totalQuantityAcrossWarehouses} cái</strong></td>
                <td>${r.reorderPoint}</td>
                <td><span class="status-badge" style="background:${r.reorderStatus === 'URGENT' ? 'rgba(239,68,68,0.15)' : 'rgba(245,158,11,0.15)'}; color:${r.reorderStatus === 'URGENT' ? 'var(--danger)' : 'var(--warning)'}; border:1px solid ${r.reorderStatus === 'URGENT' ? 'var(--danger)' : 'var(--warning)'};">${r.reorderStatus}</span></td>
                <td><button class="btn btn-primary btn-sm btn-quick-import" data-variant="${r.variantId}"><i class="fa-solid fa-file-import"></i> Nhập kho</button></td>
            `;
            
            tr.querySelector('.btn-quick-import').addEventListener('click', () => {
                openImportStockModal(r.variantId);
            });
            
            tbody.appendChild(tr);
        });
    } catch(e) {
        showToast(e.message, 'danger');
    }
}

function renderAdminCharts(stats) {
    const barCtx = document.getElementById('admin-bar-chart').getContext('2d');
    const doughnutCtx = document.getElementById('admin-doughnut-chart').getContext('2d');
    
    const labels = stats.warehouseStats.map(w => w.warehouseCode);
    const inStockData = stats.warehouseStats.map(w => w.totalItems);
    const reservedData = stats.warehouseStats.map(w => w.reservedItems);
    const lowStockData = stats.warehouseStats.map(w => w.lowStockCount);
    
    // Bar Chart
    new Chart(barCtx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Tồn thực tế',
                    data: inStockData,
                    backgroundColor: 'rgba(99, 102, 241, 0.65)',
                    borderColor: 'rgba(99, 102, 241, 1)',
                    borderWidth: 1
                },
                {
                    label: 'Đặt trước (Reserved)',
                    data: reservedData,
                    backgroundColor: 'rgba(6, 182, 212, 0.65)',
                    borderColor: 'rgba(6, 182, 212, 1)',
                    borderWidth: 1
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#94a3b8' } },
                x: { grid: { display: false }, ticks: { color: '#94a3b8' } }
            },
            plugins: {
                legend: { labels: { color: '#f8fafc' } }
            }
        }
    });
    
    // Doughnut Chart
    new Chart(doughnutCtx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: inStockData,
                backgroundColor: [
                    'rgba(99, 102, 241, 0.75)',
                    'rgba(6, 182, 212, 0.75)',
                    'rgba(16, 185, 129, 0.75)',
                    'rgba(245, 158, 11, 0.75)'
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom', labels: { color: '#f8fafc' } }
            }
        }
    });
}

// --- Admin Products Catalog Management Panel ---
async function loadAdminProductsPage() {
    try {
        if (state.catalog.categories.length === 0) {
            state.catalog.categories = await apiCall('/api/products/categories');
            state.catalog.brands = await apiCall('/api/products/brands');
        }
        
        await refreshAdminProductsList();
        
        renderAdminProductsLayout();
    } catch(e) {
        showToast(e.message, 'danger');
    }
}

async function refreshAdminProductsList() {
    const f = state.admin.filter;
    let query = `?page=${f.page}&pageSize=${f.pageSize}`;
    if (f.search) query += `&search=${encodeURIComponent(f.search)}`;
    if (f.categoryId) query += `&categoryId=${f.categoryId}`;
    if (f.status) query += `&status=${f.status}`;
    
    const resp = await apiCall(`/api/products/admin${query}`);
    state.admin.products = resp.items;
    state.admin.totalPages = resp.totalPages;
}

function renderAdminProductsLayout() {
    const container = document.getElementById('panel-container');
    container.innerHTML = `
        <div class="panel-header">
            <div>
                <h1 class="panel-title"><i class="fa-solid fa-laptop"></i> Quản Lý Danh Mục Sản Phẩm</h1>
                <p class="panel-subtitle">Thêm mới, sửa đổi thông tin chi tiết các dòng máy và phiên bản cấu hình</p>
            </div>
            <button class="btn btn-primary" id="btn-add-product-modal-trigger"><i class="fa-solid fa-plus"></i> Thêm sản phẩm</button>
        </div>
        
        <!-- Filters header -->
        <div class="glass-panel" style="padding: 16px; margin-bottom: 20px; display:flex; gap:16px; align-items:center;">
            <input type="text" id="admin-prod-search" class="form-control" style="width:250px;" placeholder="Tìm tên, mã sản phẩm..." value="${state.admin.filter.search}">
            <select id="admin-prod-cat" class="form-control" style="width:180px;">
                <option value="">Tất cả danh mục</option>
                ${state.catalog.categories.map(c => `<option value="${c.id}" ${state.admin.filter.categoryId == c.id ? 'selected' : ''}>${c.name}</option>`).join('')}
            </select>
            <select id="admin-prod-status" class="form-control" style="width:150px;">
                <option value="">Tất cả trạng thái</option>
                <option value="ACTIVE" ${state.admin.filter.status === 'ACTIVE' ? 'selected' : ''}>Active</option>
                <option value="INACTIVE" ${state.admin.filter.status === 'INACTIVE' ? 'selected' : ''}>Inactive</option>
            </select>
            <button class="btn btn-secondary btn-sm" id="btn-admin-prod-filter">Lọc</button>
        </div>
        
        <div class="orders-table-container glass-panel">
            <table class="premium-table">
                <thead>
                    <tr>
                        <th>Tên sản phẩm</th>
                        <th>Danh mục</th>
                        <th>Thương hiệu</th>
                        <th>Phiên bản</th>
                        <th>Giá bán</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody id="admin-products-tbody">
                    <!-- rows -->
                </tbody>
            </table>
        </div>
        
        <div class="pagination" id="admin-products-pagination" style="margin-top:20px;"></div>
    `;
    
    // Render list
    renderAdminProductsListRows();
    renderAdminProductsPagination();
    
    // Bind listeners
    document.getElementById('btn-admin-prod-filter').addEventListener('click', async () => {
        state.admin.filter.search = document.getElementById('admin-prod-search').value.trim();
        state.admin.filter.categoryId = document.getElementById('admin-prod-cat').value;
        state.admin.filter.status = document.getElementById('admin-prod-status').value;
        state.admin.filter.page = 1;
        await refreshAdminProductsList();
        renderAdminProductsListRows();
        renderAdminProductsPagination();
    });
    
    document.getElementById('btn-add-product-modal-trigger').addEventListener('click', () => {
        openProductFormModal(null);
    });
}

function renderAdminProductsListRows() {
    const tbody = document.getElementById('admin-products-tbody');
    tbody.innerHTML = '';
    
    if (state.admin.products.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted); padding:30px;">Không tìm thấy sản phẩm nào!</td></tr>`;
        return;
    }
    
    state.admin.products.forEach(p => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <div style="display:flex; align-items:center; gap:10px;">
                    <img src="${p.thumbnailUrl ? (p.thumbnailUrl.startsWith('http') ? p.thumbnailUrl : `/com/huitshop/Anh/${p.thumbnailUrl}`) : 'https://placehold.co/40x40/111827/FFFFFF?text=L'}" 
                         style="width:36px; height:36px; object-fit:contain; background:rgba(255,255,255,0.03); padding:2px; border-radius:4px;"
                         onerror="this.src='https://placehold.co/40x40/111827/FFFFFF?text=L'">
                    <div>
                        <strong>${p.name}</strong>
                        ${p.featured ? `<span style="font-size:9px; background:var(--gradient-rose); color:white; padding:2px 6px; border-radius:3px; margin-left:5px;">Featured</span>` : ''}
                    </div>
                </div>
            </td>
            <td>${p.category ? p.category.name : ''}</td>
            <td>${p.brand ? p.brand.name : ''}</td>
            <td><code>${p.reviewCount || 0}</code> đánh giá</td>
            <td><strong>${formatVND(p.priceFrom)}</strong></td>
            <td>
                <span class="status-badge" style="background:${p.isActive ? 'rgba(16,185,129,0.15)' : 'rgba(239,68,68,0.15)'}; color:${p.isActive ? 'var(--success)' : 'var(--danger)'}; border:1px solid ${p.isActive ? 'var(--success)' : 'var(--danger)'};">
                    ${p.isActive ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td>
                <button class="btn btn-secondary btn-sm btn-edit-prod" data-id="${p.id}"><i class="fa-regular fa-pen-to-square"></i> Sửa</button>
                <button class="btn btn-sm ${p.isActive ? 'btn-danger' : 'btn-success'} btn-toggle-status" data-id="${p.id}" data-active="${p.isActive}">
                    ${p.isActive ? '<i class="fa-solid fa-eye-slash"></i> Khóa' : '<i class="fa-solid fa-eye"></i> Mở'}
                </button>
            </td>
        `;
        
        tr.querySelector('.btn-edit-prod').addEventListener('click', () => {
            openProductFormModal(p.id);
        });
        
        tr.querySelector('.btn-toggle-status').addEventListener('click', async () => {
            const nextStatus = p.isActive ? 'INACTIVE' : 'ACTIVE';
            try {
                await apiCall(`/api/products/${p.id}/toggle-status?status=${nextStatus}`, { method: 'PUT' });
                showToast('Đã đổi trạng thái sản phẩm');
                state.admin.filter.page = 1;
                await refreshAdminProductsList();
                renderAdminProductsListRows();
                renderAdminProductsPagination();
            } catch(err) {
                showToast(err.message, 'danger');
            }
        });
        tbody.appendChild(tr);
    });
}

function renderAdminProductsPagination() {
    const pag = document.getElementById('admin-products-pagination');
    pag.innerHTML = '';
    
    if (state.admin.totalPages <= 1) return;
    
    const curr = state.admin.filter.page;
    
    const prev = document.createElement('button');
    prev.className = `btn btn-secondary btn-sm ${curr === 1 ? 'disabled' : ''}`;
    prev.innerHTML = `<i class="fa-solid fa-chevron-left"></i>`;
    if (curr > 1) {
        prev.addEventListener('click', async () => {
            state.admin.filter.page--;
            await refreshAdminProductsList();
            renderAdminProductsListRows();
            renderAdminProductsPagination();
        });
    }
    pag.appendChild(prev);
    
    for (let i = 1; i <= state.admin.totalPages; i++) {
        const btn = document.createElement('button');
        btn.className = `btn btn-sm ${curr === i ? 'btn-primary' : 'btn-secondary'}`;
        btn.textContent = i;
        btn.addEventListener('click', async () => {
            state.admin.filter.page = i;
            await refreshAdminProductsList();
            renderAdminProductsListRows();
            renderAdminProductsPagination();
        });
        pag.appendChild(btn);
    }
    
    const next = document.createElement('button');
    next.className = `btn btn-secondary btn-sm ${curr === state.admin.totalPages ? 'disabled' : ''}`;
    next.innerHTML = `<i class="fa-solid fa-chevron-right"></i>`;
    if (curr < state.admin.totalPages) {
        next.addEventListener('click', async () => {
            state.admin.filter.page++;
            await refreshAdminProductsList();
            renderAdminProductsListRows();
            renderAdminProductsPagination();
        });
    }
    pag.appendChild(next);
}

// --- Admin Logistics & Inventory Panel ---
async function loadAdminLogisticsPage() {
    const container = document.getElementById('panel-container');
    container.innerHTML = `
        <div class="panel-header">
            <div>
                <h1 class="panel-title"><i class="fa-solid fa-warehouse"></i> Quản Lý Kho & Hậu Cần Logistics</h1>
                <p class="panel-subtitle">Điều chỉnh tăng giảm tồn kho trực tiếp, xuất nhập kho hàng và điều chuyển giữa các chi nhánh</p>
            </div>
            <div style="display:flex; gap:10px;">
                <button class="btn btn-success" id="btn-import-stock-trigger"><i class="fa-solid fa-truck-moving"></i> Nhập kho hàng</button>
                <button class="btn btn-primary" id="btn-transfer-stock-trigger"><i class="fa-solid fa-share-all"></i> Điều chuyển kho</button>
            </div>
        </div>

        <!-- Inventory List per Warehouse -->
        <div class="glass-panel" style="padding:16px; margin-bottom:20px; display:flex; gap:16px; align-items:center;">
            <label style="font-weight:600; font-size:13px;">Chọn kho xem tồn:</label>
            <select id="logistics-warehouse-select" class="form-control" style="width:220px;">
                <!-- warehouses -->
            </select>
        </div>

        <div class="orders-table-container glass-panel">
            <table class="premium-table">
                <thead>
                    <tr>
                        <th>Mã SKU</th>
                        <th>Tên sản phẩm</th>
                        <th>Phiên bản</th>
                        <th>Tồn thực tế</th>
                        <th>Bị khóa (Reserved)</th>
                        <th>Khả dụng (Available)</th>
                        <th>Ngưỡng tối thiểu</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody id="logistics-stock-tbody">
                    <!-- stock logs -->
                </tbody>
            </table>
        </div>

        <!-- Stock movement history logs -->
        <div class="panel-header" style="margin-top:40px; margin-bottom:15px;">
            <h2 class="panel-title" style="font-size:18px;">Nhật ký lịch sử biến động kho hàng</h2>
        </div>
        <div class="orders-table-container glass-panel">
            <table class="premium-table">
                <thead>
                    <tr>
                        <th>Thời gian</th>
                        <th>Kho hàng</th>
                        <th>Mã SKU</th>
                        <th>Sản phẩm</th>
                        <th>Số lượng thay đổi</th>
                        <th>Loại biến động</th>
                        <th>Ghi chú</th>
                    </tr>
                </thead>
                <tbody id="logistics-movements-tbody">
                    <!-- movement history rows -->
                </tbody>
            </table>
        </div>

        <!-- Transfer Stock Modal -->
        <div id="transfer-stock-modal" class="modal">
            <div class="modal-content glass-panel">
                <span class="close-btn" onclick="document.getElementById('transfer-stock-modal').classList.remove('active')">&times;</span>
                <h2 class="modal-title"><i class="fa-solid fa-share-all"></i> Điều Chuyển Hàng Giữa Các Kho</h2>
                <form id="transfer-stock-form" style="margin-top:20px;">
                    <div class="form-grid-2">
                        <div class="form-group">
                            <label>Kho nguồn (Từ kho)</label>
                            <select id="transfer-from-warehouse" class="form-control" required></select>
                        </div>
                        <div class="form-group">
                            <label>Kho đích (Đến kho)</label>
                            <select id="transfer-to-warehouse" class="form-control" required></select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label>Chọn phiên bản hàng hóa chuyển</label>
                        <select id="transfer-variant" class="form-control" required></select>
                    </div>
                    <div class="form-grid-2">
                        <div class="form-group">
                            <label>Số lượng điều chuyển</label>
                            <input type="number" id="transfer-qty" class="form-control" min="1" required value="1">
                        </div>
                        <div class="form-group">
                            <label>Ghi chú</label>
                            <input type="text" id="transfer-note" class="form-control" placeholder="Điều chuyển chi nhánh...">
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block">Xác Nhận Điều Chuyển</button>
                </form>
            </div>
        </div>

        <!-- Adjust Stock Modal -->
        <div id="adjust-stock-modal" class="modal">
            <div class="modal-content glass-panel">
                <span class="close-btn" onclick="document.getElementById('adjust-stock-modal').classList.remove('active')">&times;</span>
                <h2 class="modal-title"><i class="fa-solid fa-sliders"></i> Điều Chỉnh Tồn Kho Trực Tiếp</h2>
                <form id="adjust-stock-form" style="margin-top:20px;">
                    <input type="hidden" id="adjust-warehouse-id">
                    <input type="hidden" id="adjust-variant-id">
                    <div class="form-group">
                        <label>Sản phẩm điều chỉnh</label>
                        <input type="text" id="adjust-product-display" class="form-control" readonly style="opacity:0.6;">
                    </div>
                    <div class="form-grid-2">
                        <div class="form-group">
                            <label>Số lượng thay đổi (Có thể âm hoặc dương)</label>
                            <input type="number" id="adjust-qty-change" class="form-control" required placeholder="ví dụ: -2 hoặc +5">
                        </div>
                        <div class="form-group">
                            <label>Lý do điều chỉnh</label>
                            <input type="text" id="adjust-note" class="form-control" required placeholder="Kiểm kê lệch kho, hỏng hóc...">
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block">Cập Nhật Tồn Kho</button>
                </form>
            </div>
        </div>
    `;

    // Load master list details
    try {
        const whs = await apiCall('/api/inventory/warehouses');
        state.admin.warehouses = whs;
        
        const sel = document.getElementById('logistics-warehouse-select');
        sel.innerHTML = `<option value="0">-- Tất cả kho hàng --</option>`;
        whs.forEach(w => {
            sel.innerHTML += `<option value="${w.id}">[${w.code}] ${w.name}</option>`;
        });
        
        // Refresh grids
        refreshLogisticsStockTable();
        refreshStockMovementsLogs();
        
        // Bind selector change
        sel.addEventListener('change', () => {
            refreshLogisticsStockTable();
            refreshStockMovementsLogs();
        });

        // Trigger Import Stock modal
        document.getElementById('btn-import-stock-trigger').addEventListener('click', () => openImportStockModal(null));

        // Trigger Transfer Stock modal
        document.getElementById('btn-transfer-stock-trigger').addEventListener('click', openTransferStockModal);

        // Bind Adjust stock form submission
        document.getElementById('adjust-stock-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const req = {
                warehouseId: parseInt(document.getElementById('adjust-warehouse-id').value),
                variantId: parseInt(document.getElementById('adjust-variant-id').value),
                quantityChange: parseInt(document.getElementById('adjust-qty-change').value),
                note: document.getElementById('adjust-note').value
            };
            try {
                await apiCall('/api/inventory/adjust', {
                    method: 'POST',
                    body: JSON.stringify(req)
                });
                showToast('Điều chỉnh kho thành công');
                document.getElementById('adjust-stock-modal').classList.remove('active');
                refreshLogisticsStockTable();
                refreshStockMovementsLogs();
            } catch(err) {
                showToast(err.message, 'danger');
            }
        });

        // Bind Transfer stock form submission
        document.getElementById('transfer-stock-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const req = {
                fromWarehouseId: parseInt(document.getElementById('transfer-from-warehouse').value),
                toWarehouseId: parseInt(document.getElementById('transfer-to-warehouse').value),
                variantId: parseInt(document.getElementById('transfer-variant').value),
                quantity: parseInt(document.getElementById('transfer-qty').value),
                note: document.getElementById('transfer-note').value
            };
            
            if (req.fromWarehouseId === req.toWarehouseId) {
                showToast('Kho nguồn và kho đích không được trùng nhau', 'warning');
                return;
            }
            
            try {
                await apiCall('/api/inventory/transfer', {
                    method: 'POST',
                    body: JSON.stringify(req)
                });
                showToast('Điều chuyển kho thành công');
                document.getElementById('transfer-stock-modal').classList.remove('active');
                refreshLogisticsStockTable();
                refreshStockMovementsLogs();
            } catch(err) {
                showToast(err.message, 'danger');
            }
        });

    } catch(err) {
        showToast(err.message, 'danger');
    }
}

async function refreshLogisticsStockTable() {
    const whId = parseInt(document.getElementById('logistics-warehouse-select').value);
    const tbody = document.getElementById('logistics-stock-tbody');
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;"><i class="fa-solid fa-spinner fa-spin"></i> Đang tải...</td></tr>`;
    
    try {
        const list = await apiCall(`/api/inventory/stock/${whId}`);
        tbody.innerHTML = '';
        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--text-muted);">Không có dữ liệu tồn kho.</td></tr>`;
            return;
        }
        
        list.forEach(i => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><code>${i.sku}</code></td>
                <td><strong>${i.productName}</strong></td>
                <td>${i.variantName || 'Mặc định'}</td>
                <td><strong>${i.quantityOnHand}</strong></td>
                <td>${i.quantityReserved}</td>
                <td><strong style="color:var(--success);">${i.availableQuantity}</strong></td>
                <td>${i.reorderPoint}</td>
                <td>
                    <button class="btn btn-secondary btn-sm btn-adjust-trigger" data-wh="${i.warehouseId}" data-variant="${i.variantId}" data-name="${i.productName} - ${i.variantName || 'Mặc định'}"><i class="fa-solid fa-sliders"></i> Sửa tồn</button>
                </td>
            `;
            
            tr.querySelector('.btn-adjust-trigger').addEventListener('click', () => {
                const trigger = tr.querySelector('.btn-adjust-trigger');
                document.getElementById('adjust-warehouse-id').value = trigger.getAttribute('data-wh');
                document.getElementById('adjust-variant-id').value = trigger.getAttribute('data-variant');
                document.getElementById('adjust-product-display').value = trigger.getAttribute('data-name');
                document.getElementById('adjust-qty-change').value = '';
                document.getElementById('adjust-note').value = '';
                document.getElementById('adjust-stock-modal').classList.add('active');
            });
            tbody.appendChild(tr);
        });
    } catch(e) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--danger);">${e.message}</td></tr>`;
    }
}

async function refreshStockMovementsLogs() {
    const whId = parseInt(document.getElementById('logistics-warehouse-select').value);
    const tbody = document.getElementById('logistics-movements-tbody');
    tbody.innerHTML = '';
    
    try {
        const list = await apiCall(`/api/inventory/movements?warehouseId=${whId}`);
        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">Chưa có ghi chép biến động kho.</td></tr>`;
            return;
        }
        
        list.forEach(m => {
            const tr = document.createElement('tr');
            const color = m.quantity > 0 ? 'var(--success)' : 'var(--danger)';
            const prefix = m.quantity > 0 ? '+' : '';
            
            tr.innerHTML = `
                <td>${formatDate(m.createdAt)}</td>
                <td><code>${m.warehouseName}</code></td>
                <td><code>${m.sku}</code></td>
                <td>${m.productName}</td>
                <td><strong style="color:${color};">${prefix}${m.quantity}</strong></td>
                <td><span class="status-badge" style="font-size:9px; background:rgba(255,255,255,0.05); color:var(--text-secondary);">${m.movementType}</span></td>
                <td style="font-size:12px; max-width:200px; text-overflow:ellipsis; overflow:hidden; white-space:nowrap;" title="${m.note || ''}">${m.note || ''}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch(e) {
        console.error(e);
    }
}

// --- Admin Order Shipping desk Panel ---
async function loadAdminOrdersPage() {
    const container = document.getElementById('panel-container');
    container.innerHTML = `
        <div class="panel-header">
            <div>
                <h1 class="panel-title"><i class="fa-solid fa-truck-fast"></i> Hệ Thống Điều Phối & Xử Lý Đơn Hàng</h1>
                <p class="panel-subtitle">Duyệt xác nhận thanh toán, xuất kho cấp số Serial/IMEI thiết bị và theo dõi hành trình giao nhận</p>
            </div>
        </div>

        <div class="orders-table-container glass-panel">
            <table class="premium-table">
                <thead>
                    <tr>
                        <th>Đơn hàng</th>
                        <th>Ngày đặt</th>
                        <th>Khách hàng</th>
                        <th>Hình thức</th>
                        <th>Thanh toán</th>
                        <th>Trạng thái</th>
                        <th>Tổng cộng</th>
                        <th>Hành động xử lý</th>
                    </tr>
                </thead>
                <tbody id="admin-orders-tbody">
                    <!-- order rows -->
                </tbody>
            </table>
        </div>
    `;

    refreshAdminOrdersTable();
}

async function refreshAdminOrdersTable() {
    const tbody = document.getElementById('admin-orders-tbody');
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;"><i class="fa-solid fa-spinner fa-spin"></i> Đang tải...</td></tr>`;

    try {
        const resp = await apiCall('/api/orders?pageSize=50');
        tbody.innerHTML = '';
        if (resp.items.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--text-muted); padding:30px;">Không có đơn hàng nào cần xử lý.</td></tr>`;
            return;
        }

        resp.items.forEach(o => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>
                    <strong style="color:var(--primary); cursor:pointer;" class="btn-detail-code">${o.code}</strong>
                </td>
                <td>${formatDate(o.createdAt)}</td>
                <td>
                    <div><strong>${o.userName}</strong></div>
                    <span style="font-size:11px; color:var(--text-muted);">${o.userEmail}</span>
                </td>
                <td>${o.paymentMethod}</td>
                <td>
                    <span class="status-badge" style="background:${o.paymentStatus === 'PAID' ? 'rgba(16,185,129,0.15)' : 'rgba(245,158,11,0.15)'}; color:${o.paymentStatus === 'PAID' ? 'var(--success)' : 'var(--warning)'}; border:1px solid ${o.paymentStatus === 'PAID' ? 'var(--success)' : 'var(--warning)'};">
                        ${o.paymentStatus === 'PAID' ? 'Đã Thanh Toán' : 'Chưa Thanh Toán'}
                    </span>
                </td>
                <td>
                    <span class="status-badge status-${o.status.toLowerCase()}">${translateStatus(o.status)}</span>
                </td>
                <td><strong>${formatVND(o.total)}</strong></td>
                <td>
                    <div style="display:flex; gap:6px;">
                        <button class="btn btn-secondary btn-sm btn-view-det" data-code="${o.code}"><i class="fa-regular fa-eye"></i> Chi tiết</button>
                        ${renderOrderWorkflowButtons(o)}
                    </div>
                </td>
            `;

            // View details
            tr.querySelector('.btn-view-det').addEventListener('click', () => openOrderDetailsModal(o.code));
            tr.querySelector('.btn-detail-code').addEventListener('click', () => openOrderDetailsModal(o.code));

            // Workflow click mapping
            const btnConfirm = tr.querySelector('.btn-act-confirm');
            const btnShip = tr.querySelector('.btn-act-ship');
            const btnComplete = tr.querySelector('.btn-act-complete');
            const btnCancel = tr.querySelector('.btn-act-cancel');

            if (btnConfirm) {
                btnConfirm.addEventListener('click', async () => {
                    await apiCall(`/api/orders/${o.id}/confirm?staffId=${state.user.id}`, { method: 'POST' });
                    showToast('Đã xác nhận đơn hàng');
                    refreshAdminOrdersTable();
                });
            }

            if (btnShip) {
                btnShip.addEventListener('click', () => openShippingModal(o));
            }

            if (btnComplete) {
                btnComplete.addEventListener('click', async () => {
                    await apiCall(`/api/orders/${o.id}/complete`, { method: 'POST' });
                    showToast('Đơn hàng đã hoàn tất giao hàng');
                    refreshAdminOrdersTable();
                });
            }

            if (btnCancel) {
                btnCancel.addEventListener('click', async () => {
                    const reason = prompt('Nhập lý do hủy đơn hàng:');
                    if (reason !== null) {
                        await apiCall(`/api/orders/${o.id}/cancel`, {
                            method: 'POST',
                            body: JSON.stringify({ reason: reason || 'Admin hủy đơn' })
                        });
                        showToast('Đã hủy đơn hàng');
                        refreshAdminOrdersTable();
                    }
                });
            }

            tbody.appendChild(tr);
        });

    } catch(e) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--danger);">${e.message}</td></tr>`;
    }
}

function renderOrderWorkflowButtons(o) {
    let html = '';
    if (o.status === 'PENDING') {
        html += `<button class="btn btn-primary btn-sm btn-act-confirm"><i class="fa-solid fa-check"></i> Xác nhận</button>`;
        html += `<button class="btn btn-danger btn-sm btn-act-cancel"><i class="fa-solid fa-ban"></i> Hủy</button>`;
    } else if (o.status === 'CONFIRMED') {
        html += `<button class="btn btn-success btn-sm btn-act-ship"><i class="fa-solid fa-truck-loading"></i> Giao hàng</button>`;
    } else if (o.status === 'SHIPPING') {
        html += `<button class="btn btn-primary btn-sm btn-act-complete" style="background:var(--gradient-emerald);"><i class="fa-solid fa-circle-check"></i> Hoàn tất</button>`;
    }
    return html;
}

// --- Admin Role & Status Permissions Panel ---
async function loadAdminUsersPage() {
    const container = document.getElementById('panel-container');
    container.innerHTML = `
        <div class="panel-header">
            <div>
                <h1 class="panel-title"><i class="fa-solid fa-user-gear"></i> Phân Quyền & Quản Lý User</h1>
                <p class="panel-subtitle">Điều chỉnh phân vai trò quyền hạn và quản lý đóng mở khóa tài khoản nhân viên / khách hàng</p>
            </div>
        </div>

        <div class="orders-table-container glass-panel">
            <table class="premium-table">
                <thead>
                    <tr>
                        <th>Họ và tên</th>
                        <th>Email đăng nhập</th>
                        <th>Số điện thoại</th>
                        <th>Vai trò (Role)</th>
                        <th>Trạng thái hoạt động</th>
                        <th>Thao tác lưu</th>
                    </tr>
                </thead>
                <tbody id="admin-users-tbody">
                    <!-- users -->
                </tbody>
            </table>
        </div>
    `;

    try {
        const list = await apiCall('/api/users');
        const tbody = document.getElementById('admin-users-tbody');
        tbody.innerHTML = '';
        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">Không có dữ liệu user nào.</td></tr>`;
            return;
        }

        list.forEach(u => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>
                    <div style="display:flex; align-items:center; gap:8px;">
                        <div class="user-avatar" style="width:28px; height:28px; font-size:12px;">${u.fullName.charAt(0).toUpperCase()}</div>
                        <strong>${u.fullName}</strong>
                    </div>
                </td>
                <td>${u.email}</td>
                <td>${u.phone || 'Chưa cung cấp'}</td>
                <td>
                    <select class="form-control user-role-select" style="width:130px; padding:6px 12px; font-size:12px;">
                        <option value="CUSTOMER" ${u.role === 'CUSTOMER' ? 'selected' : ''}>CUSTOMER</option>
                        <option value="STAFF" ${u.role === 'STAFF' ? 'selected' : ''}>STAFF</option>
                        <option value="WAREHOUSE" ${u.role === 'WAREHOUSE' ? 'selected' : ''}>WAREHOUSE</option>
                        <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                    </select>
                </td>
                <td>
                    <select class="form-control user-status-select" style="width:120px; padding:6px 12px; font-size:12px;">
                        <option value="ACTIVE" ${u.status === 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                        <option value="SUSPENDED" ${u.status === 'SUSPENDED' ? 'selected' : ''}>SUSPENDED</option>
                    </select>
                </td>
                <td>
                    <button class="btn btn-primary btn-sm btn-save-user-perms" data-id="${u.id}"><i class="fa-regular fa-floppy-disk"></i> Lưu</button>
                </td>
            `;

            tr.querySelector('.btn-save-user-perms').addEventListener('click', async () => {
                const role = tr.querySelector('.user-role-select').value;
                const status = tr.querySelector('.user-status-select').value;
                try {
                    await apiCall(`/api/users/${u.id}/role-status`, {
                        method: 'PUT',
                        body: JSON.stringify({ role, status })
                    });
                    showToast('Đã lưu thay đổi phân quyền');
                } catch(err) {
                    showToast(err.message, 'danger');
                }
            });

            tbody.appendChild(tr);
        });

    } catch(err) {
        showToast(err.message, 'danger');
    }
}

// ==========================================
// MODALS INTERACTIONS & FORMS
// ==========================================

// --- Open Catalog Product Details Modal ---
async function openProductDetail(productId) {
    try {
        const p = await apiCall(`/api/products/${productId}`);
        state.activeDetailProduct = p;
        state.activeDetailVariant = p.variants.length > 0 ? p.variants[0] : null;
        
        // Render headers
        document.getElementById('modal-product-brand').textContent = p.brand ? p.brand.name : 'CHÍNH HÃNG';
        document.getElementById('modal-product-name').textContent = p.name;
        document.getElementById('modal-product-rating').textContent = p.ratingAverage.toFixed(1);
        document.getElementById('modal-product-reviews-count').textContent = `(${p.reviewCount} đánh giá)`;
        document.getElementById('modal-product-short-desc').textContent = p.description.substring(0, 150) + '...';
        document.getElementById('modal-product-long-desc').textContent = p.description;
        
        // Rating stars
        const stars = document.getElementById('modal-product-rating-stars');
        stars.innerHTML = '';
        const avg = Math.round(p.ratingAverage);
        for (let i = 1; i <= 5; i++) {
            stars.innerHTML += i <= avg ? `<i class="fa-solid fa-star"></i>` : `<i class="fa-regular fa-star"></i>`;
        }

        // Render main image
        const img = document.getElementById('modal-product-img');
        let imgUrl = p.variants.length > 0 ? p.variants[0].thumbnailUrl : 'no-image.png';
        if (!imgUrl.startsWith('http') && !imgUrl.startsWith('/')) {
            imgUrl = `/com/huitshop/Anh/${imgUrl}`;
        }
        img.src = imgUrl;

        // Render Thumbnails list
        const thumbs = document.getElementById('modal-product-thumbnails');
        thumbs.innerHTML = '';
        p.images.forEach((imgObj, idx) => {
            const thumbUrl = imgObj.imageUrl.startsWith('http') ? imgObj.imageUrl : `/com/huitshop/Anh/${imgObj.imageUrl}`;
            const tImg = document.createElement('img');
            tImg.src = thumbUrl;
            if (idx === 0) tImg.className = 'active';
            tImg.addEventListener('click', () => {
                thumbs.querySelectorAll('img').forEach(t => t.classList.remove('active'));
                tImg.classList.add('active');
                img.src = thumbUrl;
            });
            thumbs.appendChild(tImg);
        });

        // Add variants selection
        const varsGrid = document.getElementById('modal-product-variants');
        varsGrid.innerHTML = '';
        p.variants.forEach((v, idx) => {
            const btn = document.createElement('button');
            btn.className = `variant-btn ${idx === 0 ? 'active' : ''}`;
            btn.innerHTML = `
                <div><strong>${v.variantName || 'Mặc định'}</strong></div>
                <div style="font-size:10px; margin-top:2px;">${formatVND(v.price)}</div>
            `;
            btn.addEventListener('click', () => {
                varsGrid.querySelectorAll('.variant-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                state.activeDetailVariant = v;
                document.getElementById('modal-product-price').textContent = formatVND(v.price);
                document.getElementById('modal-product-orig-price').textContent = formatVND(v.originalPrice);
                
                // Swap main image
                let varImg = v.thumbnailUrl || 'no-image.png';
                if (!varImg.startsWith('http') && !varImg.startsWith('/')) {
                    varImg = `/com/huitshop/Anh/${varImg}`;
                }
                img.src = varImg;
            });
            varsGrid.appendChild(btn);
        });

        // Prices display
        if (state.activeDetailVariant) {
            document.getElementById('modal-product-price').textContent = formatVND(state.activeDetailVariant.price);
            document.getElementById('modal-product-orig-price').textContent = formatVND(state.activeDetailVariant.originalPrice);
        }

        // Quantities spinner
        document.getElementById('spinner-qty').value = 1;

        // Render Specifications Table
        const specsTable = document.getElementById('modal-product-specs-table');
        specsTable.innerHTML = '';
        if (p.specifications) {
            try {
                const specs = JSON.parse(p.specifications);
                for (const key in specs) {
                    specsTable.innerHTML += `
                        <tr>
                            <td>${key}</td>
                            <td>${specs[key]}</td>
                        </tr>
                    `;
                }
            } catch(e) {
                specsTable.innerHTML = `<tr><td colspan="2">${p.specifications}</td></tr>`;
            }
        }

        // Render Reviews
        refreshReviewsListInModal(productId);
        
        // Show Modal
        document.getElementById('product-detail-modal').classList.add('active');
    } catch(e) {
        showToast(e.message, 'danger');
    }
}

async function refreshReviewsListInModal(productId) {
    const list = document.getElementById('modal-product-reviews-list');
    list.innerHTML = '';
    
    try {
        const reviews = await apiCall(`/api/products/${productId}/reviews`);
        if (reviews.length === 0) {
            list.innerHTML = `<p style="color:var(--text-muted); font-size:12px; text-align:center; padding:15px;">Chưa có lượt đánh giá nào cho sản phẩm này.</p>`;
            return;
        }
        
        reviews.forEach(r => {
            const card = document.createElement('div');
            card.className = 'review-item';
            
            // rating stars
            let starsHtml = '';
            for (let i = 1; i <= 5; i++) {
                starsHtml += i <= r.rating ? `<i class="fa-solid fa-star"></i>` : `<i class="fa-regular fa-star"></i>`;
            }
            
            card.innerHTML = `
                <div class="review-item-header">
                    <span class="review-author">${r.reviewerName || 'Khách hàng ẩn danh'}</span>
                    <span class="review-date">${formatDate(r.createdAt)}</span>
                </div>
                <div class="review-rating">${starsHtml}</div>
                <p class="review-comment">${r.comment}</p>
            `;
            list.appendChild(card);
        });
    } catch(err) {
        console.error(err);
    }
}

// --- Open Add/Edit Product Modal (Admin) ---
async function openProductFormModal(productId) {
    const form = document.getElementById('product-edit-form');
    form.reset();

    const catSel = document.getElementById('admin-product-category');
    const brandSel = document.getElementById('admin-product-brand');
    catSel.innerHTML = '';
    brandSel.innerHTML = '';

    state.catalog.categories.forEach(c => catSel.innerHTML += `<option value="${c.id}">${c.name}</option>`);
    state.catalog.brands.forEach(b => brandSel.innerHTML += `<option value="${b.id}">${b.name}</option>`);

    if (productId === null) {
        // Create new mode
        document.getElementById('product-form-title').textContent = 'Thêm sản phẩm mới';
        document.getElementById('admin-product-id').value = '';
        document.getElementById('new-product-variant-fields').classList.remove('hidden');
        document.getElementById('admin-variant-name').required = true;
        document.getElementById('admin-variant-sku').required = true;
        document.getElementById('admin-variant-price').required = true;
    } else {
        // Edit mode
        try {
            const p = await apiCall(`/api/products/admin/${productId}`);
            document.getElementById('product-form-title').textContent = `Sửa sản phẩm: ${p.name}`;
            document.getElementById('admin-product-id').value = p.id;
            document.getElementById('new-product-variant-fields').classList.add('hidden');
            
            document.getElementById('admin-variant-name').required = false;
            document.getElementById('admin-variant-sku').required = false;
            document.getElementById('admin-variant-price').required = false;

            document.getElementById('admin-product-name').value = p.name;
            document.getElementById('admin-product-category').value = p.category.id;
            document.getElementById('admin-product-brand').value = p.brand.id;
            document.getElementById('admin-product-short-desc').value = p.description.substring(0, 100);
            document.getElementById('admin-product-desc').value = p.description;
            document.getElementById('admin-product-specs').value = p.specifications;
            document.getElementById('admin-product-status').value = p.status || 'ACTIVE';
            document.getElementById('admin-product-featured').checked = p.featured;

        } catch(err) {
            showToast(err.message, 'danger');
            return;
        }
    }

    document.getElementById('product-form-modal').classList.add('active');
}

// --- Open Import Stock modal (Admin) ---
async function openImportStockModal(preselectedVariantId) {
    try {
        const whs = await apiCall('/api/inventory/warehouses');
        const suppliers = await apiCall('/api/inventory/suppliers');
        const variants = await apiCall('/api/inventory/variants');

        const whSel = document.getElementById('import-warehouse-id');
        const supSel = document.getElementById('import-supplier-id');
        const varSel = document.getElementById('import-variant-id');

        whSel.innerHTML = '';
        suppliers.forEach(s => supSel.innerHTML += `<option value="${s.id}">[${s.code}] ${s.name}</option>`);
        whs.forEach(w => whSel.innerHTML += `<option value="${w.id}">[${w.code}] ${w.name}</option>`);
        
        varSel.innerHTML = '';
        variants.forEach(v => {
            const display = `${v.product.name} ${v.variantName ? `(${v.variantName})` : ''} [${v.sku}]`;
            const opt = document.createElement('option');
            opt.value = v.id;
            opt.textContent = display;
            if (v.id === preselectedVariantId) opt.selected = true;
            varSel.appendChild(opt);
        });

        document.getElementById('import-stock-form').reset();
        document.getElementById('import-stock-modal').classList.add('active');
    } catch(err) {
        showToast(err.message, 'danger');
    }
}

// --- Open Transfer Stock Modal (Admin) ---
async function openTransferStockModal() {
    try {
        const whs = await apiCall('/api/inventory/warehouses');
        const variants = await apiCall('/api/inventory/variants');

        const fromSel = document.getElementById('transfer-from-warehouse');
        const toSel = document.getElementById('transfer-to-warehouse');
        const varSel = document.getElementById('transfer-variant');

        fromSel.innerHTML = '';
        toSel.innerHTML = '';
        whs.forEach(w => {
            fromSel.innerHTML += `<option value="${w.id}">[${w.code}] ${w.name}</option>`;
            toSel.innerHTML += `<option value="${w.id}">[${w.code}] ${w.name}</option>`;
        });

        varSel.innerHTML = '';
        variants.forEach(v => {
            const display = `${v.product.name} ${v.variantName ? `(${v.variantName})` : ''} [${v.sku}]`;
            varSel.innerHTML += `<option value="${v.id}">${display}</option>`;
        });

        document.getElementById('transfer-stock-form').reset();
        document.getElementById('transfer-stock-modal').classList.add('active');
    } catch(e) {
        showToast(e.message, 'danger');
    }
}

// --- Open Shipping confirmation Modal (Admin dispatch) ---
async function openShippingModal(o) {
    document.getElementById('shipping-order-id').value = o.id;
    document.getElementById('shipping-modal-order-code').textContent = `Đơn hàng: ${o.code}`;
    
    try {
        const whs = await apiCall('/api/inventory/warehouses');
        const whSel = document.getElementById('shipping-warehouse-id');
        whSel.innerHTML = '';
        whs.forEach(w => whSel.innerHTML += `<option value="${w.id}">[${w.code}] ${w.name}</option>`);

        // Load items to prompt for IMEI serial keys
        const container = document.getElementById('shipping-imei-list');
        container.innerHTML = '';

        o.items.forEach(item => {
            const div = document.createElement('div');
            div.className = 'form-group imei-entry-row';
            div.style.borderBottom = '1px solid var(--border-glass)';
            div.style.paddingBottom = '10px';
            
            // Generate quantity entry fields
            let fieldsHtml = '';
            for (let q = 1; q <= item.quantity; q++) {
                fieldsHtml += `
                    <div style="margin-top:6px; display:flex; align-items:center; gap:8px;">
                        <span style="font-size:11px; color:var(--text-muted); width:80px;">Cái #${q}:</span>
                        <input type="text" class="form-control imei-serial-key" data-item-id="${item.id}" placeholder="Nhập số Serial key thiết bị..." required>
                    </div>
                `;
            }

            div.innerHTML = `
                <div style="font-weight:600; font-size:13px; color:var(--text-primary); margin-top:8px;">
                    ${item.productName} (x${item.quantity}) - SKU: ${item.sku}
                </div>
                ${fieldsHtml}
            `;
            container.appendChild(div);
        });

        document.getElementById('shipping-modal').classList.add('active');

    } catch(e) {
        showToast(e.message, 'danger');
    }
}

// --- Order detail Invoice modal view (Both customer & Admin) ---
async function openOrderDetailsModal(orderCode) {
    try {
        const o = await apiCall(`/api/orders/code/${orderCode}`);
        
        // Let's reuse the modal overlay container
        const detailModal = document.getElementById('product-detail-modal');
        const content = detailModal.querySelector('.modal-content');
        
        // Store original modal contents
        const originalHtml = content.innerHTML;
        
        // Setup close listener override
        const restoreModal = () => {
            content.innerHTML = originalHtml;
            setupModalListeners(); // Rebind original events
        };
        
        content.innerHTML = `
            <span class="close-btn" id="invoice-modal-close">&times;</span>
            <h2 class="modal-title" style="margin-bottom:5px;"><i class="fa-solid fa-receipt"></i> Hóa Đơn Bán Hàng</h2>
            <p class="modal-subtitle">Mã đơn: <strong style="color:var(--primary);">${o.code}</strong> | Ngày mua: ${formatDate(o.createdAt)}</p>
            
            <div class="form-grid-2" style="margin-top:20px; border-bottom:1px solid var(--border-glass); padding-bottom:15px; margin-bottom:15px;">
                <div>
                    <h4 style="font-size:11px; text-transform:uppercase; color:var(--text-muted); margin-bottom:4px;">Thông tin khách hàng</h4>
                    <strong>${o.userName}</strong>
                    <div style="font-size:12px; color:var(--text-secondary); margin-top:3px;">Email: ${o.userEmail}</div>
                    <div style="font-size:12px; color:var(--text-secondary);">Điện thoại: ${o.recipientPhone}</div>
                </div>
                <div>
                    <h4 style="font-size:11px; text-transform:uppercase; color:var(--text-muted); margin-bottom:4px;">Địa chỉ giao hàng</h4>
                    <strong>${o.recipientName}</strong>
                    <div style="font-size:12px; color:var(--text-secondary); margin-top:3px; line-height:1.4;">${o.fullAddress}</div>
                </div>
            </div>

            <h3>Sản phẩm thanh toán</h3>
            <table class="premium-table" style="margin-top:10px;">
                <thead>
                    <tr>
                        <th>Tên sản phẩm / Cấu hình</th>
                        <th>Đơn giá</th>
                        <th>Số lượng</th>
                        <th>Cấp Serial/IMEI</th>
                        <th>Thành tiền</th>
                    </tr>
                </thead>
                <tbody>
                    ${o.items.map(item => `
                        <tr>
                            <td>
                                <strong>${item.productName}</strong><br>
                                <span style="font-size:10px; color:var(--text-muted);">SKU: ${item.sku}</span>
                            </td>
                            <td>${formatVND(item.unitPrice)}</td>
                            <td>x${item.quantity}</td>
                            <td>
                                ${item.serialNumbers.length > 0 ? 
                                    item.serialNumbers.map(s => `<code style="display:block; font-size:10px; color:var(--success);">${s}</code>`).join('') :
                                    '<span style="color:var(--text-muted);font-size:11px;">Chưa cấp</span>'
                                }
                            </td>
                            <td><strong>${formatVND(item.totalPrice)}</strong></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>

            <div style="display:flex; justify-content:flex-end; margin-top:20px;">
                <div style="width:280px; display:flex; flex-direction:column; gap:8px; font-size:13px;">
                    <div style="display:flex; justify-content:space-between;">
                        <span>Cộng tiền hàng:</span>
                        <span>${formatVND(o.subtotal)}</span>
                    </div>
                    <div style="display:flex; justify-content:space-between; color:var(--success);">
                        <span>Giảm giá (Voucher):</span>
                        <span>-${formatVND(o.discount)}</span>
                    </div>
                    <div style="display:flex; justify-content:space-between;">
                        <span>Phí vận chuyển:</span>
                        <span>${o.shippingFee == 0 ? 'Miễn phí' : formatVND(o.shippingFee)}</span>
                    </div>
                    <div style="display:flex; justify-content:space-between; font-weight:700; border-top:1px solid var(--border-glass); padding-top:8px; font-size:15px;">
                        <span>Thực thu:</span>
                        <span style="color:var(--primary);">${formatVND(o.total)}</span>
                    </div>
                </div>
            </div>

            <h3 style="margin-top:30px; border-top:1px solid var(--border-glass); padding-top:15px;">Nhật ký hành trình đơn hàng</h3>
            <div class="timeline">
                ${o.statusHistory.map(h => `
                    <div class="timeline-item">
                        <div class="timeline-time">${formatDate(h.createdAt)}</div>
                        <div class="timeline-title">${translateStatus(h.status)}</div>
                        <div class="timeline-desc">${h.note || ''}</div>
                    </div>
                `).join('')}
            </div>
        `;
        
        detailModal.classList.add('active');
        
        const closeBtn = document.getElementById('invoice-modal-close');
        const handleClose = () => {
            detailModal.classList.remove('active');
            restoreModal();
        };
        closeBtn.addEventListener('click', handleClose);
        
    } catch(e) {
        showToast(e.message, 'danger');
    }
}

// ==========================================
// REGISTERED ACTIONS & INTERFACE LISTENERS
// ==========================================

function setupAuthListeners() {
    // Screen switcher
    document.getElementById('go-to-register').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('login-form').classList.remove('active');
        document.getElementById('register-form').classList.add('active');
    });

    document.getElementById('go-to-login').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('register-form').classList.remove('active');
        document.getElementById('login-form').classList.add('active');
    });

    // Login Form Submit
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const req = {
            email: document.getElementById('login-email').value.trim(),
            password: document.getElementById('login-password').value
        };
        try {
            const resp = await apiCall('/api/auth/login', {
                method: 'POST',
                body: JSON.stringify(req)
            });
            showToast(`Chào mừng trở lại, ${resp.fullName}!`);
            state.user = resp;
            localStorage.setItem('huitshop_user', JSON.stringify(resp));
            showApp();
        } catch(err) {
            showToast(err.message, 'danger');
        }
    });

    // Register Form Submit
    document.getElementById('register-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const req = {
            fullName: document.getElementById('reg-name').value.trim(),
            email: document.getElementById('reg-email').value.trim(),
            phone: document.getElementById('reg-phone').value.trim(),
            password: document.getElementById('reg-password').value
        };
        
        if (req.password.length < 6) {
            showToast('Mật khẩu tối thiểu 6 ký tự', 'warning');
            return;
        }

        try {
            const resp = await apiCall('/api/auth/register', {
                method: 'POST',
                body: JSON.stringify(req)
            });
            showToast('Đăng ký tài khoản thành công! Tự động đăng nhập...');
            state.user = resp;
            localStorage.setItem('huitshop_user', JSON.stringify(resp));
            showApp();
        } catch(err) {
            showToast(err.message, 'danger');
        }
    });
    
    // Logout trigger
    document.getElementById('btn-logout').addEventListener('click', () => {
        if (confirm('Bạn có muốn đăng xuất khỏi hệ thống?')) {
            showToast('Đã đăng xuất tài khoản');
            showAuth();
        }
    });
}

function setupModalListeners() {
    // Close modal triggers
    document.querySelectorAll('.modal .close-btn, .modal .close-modal-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            btn.closest('.modal').classList.remove('active');
        });
    });

    // Click outside to close modal
    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal')) {
            e.target.classList.remove('active');
        }
    });

    // Modal product detail tab toggles
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const parent = btn.closest('.modal-content');
            parent.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            parent.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
            
            btn.classList.add('active');
            parent.querySelector(`#${btn.getAttribute('data-tab')}`).classList.add('active');
        });
    });

    // Product detail modal Quantity spinners
    document.getElementById('spinner-dec').addEventListener('click', () => {
        const inp = document.getElementById('spinner-qty');
        const val = parseInt(inp.value);
        if (val > 1) inp.value = val - 1;
    });

    document.getElementById('spinner-inc').addEventListener('click', () => {
        const inp = document.getElementById('spinner-qty');
        inp.value = parseInt(inp.value) + 1;
    });

    // Add to cart action from modal
    document.getElementById('modal-btn-add-to-cart').addEventListener('click', async () => {
        if (!state.activeDetailVariant) {
            showToast('Không có phiên bản hợp lệ để thêm!', 'warning');
            return;
        }
        const qty = parseInt(document.getElementById('spinner-qty').value);
        try {
            await apiCall(`/api/cart/${state.user.id}/items`, {
                method: 'POST',
                body: JSON.stringify({
                    variantId: state.activeDetailVariant.id,
                    quantity: qty
                })
            });
            showToast(`Đã thêm x${qty} sản phẩm vào giỏ hàng`);
            document.getElementById('product-detail-modal').classList.remove('active');
            syncCartCount();
        } catch(err) {
            showToast(err.message, 'danger');
        }
    });

    // Submit product review comment
    document.getElementById('btn-submit-review').addEventListener('click', async () => {
        const comment = document.getElementById('review-comment').value.trim();
        const rating = parseInt(document.querySelector('.star-rating-input.selected')?.getAttribute('data-rating') || '5');
        
        if (!comment) {
            showToast('Vui lòng nhập bình luận đánh giá', 'warning');
            return;
        }

        const review = {
            rating: rating,
            comment: comment,
            reviewerName: state.user.fullName
        };

        try {
            await apiCall(`/api/products/${state.activeDetailProduct.id}/reviews`, {
                method: 'POST',
                body: JSON.stringify(review)
            });
            showToast('Cảm ơn bạn đã gửi đánh giá sản phẩm!');
            document.getElementById('review-comment').value = '';
            document.querySelectorAll('.star-rating-input').forEach(s => s.classList.remove('selected'));
            refreshReviewsListInModal(state.activeDetailProduct.id);
        } catch(err) {
            showToast(err.message, 'danger');
        }
    });

    // Rating stars selector logic
    document.querySelectorAll('.star-rating-input').forEach(star => {
        star.addEventListener('click', () => {
            const rating = parseInt(star.getAttribute('data-rating'));
            document.querySelectorAll('.star-rating-input').forEach(s => {
                const r = parseInt(s.getAttribute('data-rating'));
                if (r <= rating) {
                    s.classList.add('selected');
                    s.innerHTML = `<i class="fa-solid fa-star"></i>`;
                } else {
                    s.classList.remove('selected');
                    s.innerHTML = `<i class="fa-regular fa-star"></i>`;
                }
            });
        });
    });

    // Admin create/edit product form submission
    document.getElementById('product-edit-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('admin-product-id').value;
        const isEdit = id !== '';
        
        if (isEdit) {
            // Edit Product
            const req = {
                name: document.getElementById('admin-product-name').value.trim(),
                categoryId: parseInt(document.getElementById('admin-product-category').value),
                brandId: parseInt(document.getElementById('admin-product-brand').value),
                shortDescription: document.getElementById('admin-product-short-desc').value.trim(),
                description: document.getElementById('admin-product-desc').value.trim(),
                specifications: document.getElementById('admin-product-specs').value.trim(),
                status: document.getElementById('admin-product-status').value,
                featured: document.getElementById('admin-product-featured').checked
            };
            try {
                await apiCall(`/api/products/${id}`, {
                    method: 'PUT',
                    body: JSON.stringify(req)
                });
                showToast('Cập nhật sản phẩm thành công');
                document.getElementById('product-form-modal').classList.remove('active');
                loadAdminProductsPage();
            } catch(err) {
                showToast(err.message, 'danger');
            }
        } else {
            // Create Product with default variant
            const req = {
                name: document.getElementById('admin-product-name').value.trim(),
                categoryId: parseInt(document.getElementById('admin-product-category').value),
                brandId: parseInt(document.getElementById('admin-product-brand').value),
                shortDescription: document.getElementById('admin-product-short-desc').value.trim(),
                description: document.getElementById('admin-product-desc').value.trim(),
                specifications: document.getElementById('admin-product-specs').value.trim(),
                status: document.getElementById('admin-product-status').value,
                featured: document.getElementById('admin-product-featured').checked,
                
                defaultVariantName: document.getElementById('admin-variant-name').value.trim(),
                defaultSku: document.getElementById('admin-variant-sku').value.trim(),
                defaultPrice: parseFloat(document.getElementById('admin-variant-price').value),
                defaultOriginalPrice: parseFloat(document.getElementById('admin-variant-orig-price').value || '0'),
                defaultThumbnailUrl: document.getElementById('admin-variant-thumbnail').value.trim() || 'laptop_asus.jpg'
            };
            try {
                await apiCall('/api/products', {
                    method: 'POST',
                    body: JSON.stringify(req)
                });
                showToast('Tạo sản phẩm mới thành công');
                document.getElementById('product-form-modal').classList.remove('active');
                loadAdminProductsPage();
            } catch(err) {
                showToast(err.message, 'danger');
            }
        }
    });

    // Admin import stock form submission
    document.getElementById('import-stock-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        // Extract serial numbers array split by lines
        const serialText = document.getElementById('import-serials-input').value.trim();
        const serials = serialText ? serialText.split('\n').map(s => s.trim()).filter(s => s.length > 0) : [];
        
        const req = {
            warehouseId: parseInt(document.getElementById('import-warehouse-id').value),
            supplierId: parseInt(document.getElementById('import-supplier-id').value),
            variantId: parseInt(document.getElementById('import-variant-id').value),
            costPrice: parseFloat(document.getElementById('import-cost-price').value),
            quantity: parseInt(document.getElementById('import-qty').value),
            serials: serials
        };

        if (serials.length > 0 && serials.length !== req.quantity) {
            showToast('Số lượng số serial không khớp với số lượng hàng nhập!', 'warning');
            return;
        }

        try {
            await apiCall('/api/inventory/import', {
                method: 'POST',
                body: JSON.stringify(req)
            });
            showToast('Nhập hàng vào kho thành công!');
            document.getElementById('import-stock-modal').classList.remove('active');
            if (state.activePanel === 'admin_dashboard') loadAdminDashboardPage();
            if (state.activePanel === 'admin_logistics') {
                refreshLogisticsStockTable();
                refreshStockMovementsLogs();
            }
        } catch(err) {
            showToast(err.message, 'danger');
        }
    });

    // Admin ship order dispatch submit (binding scanned IMEI keys)
    document.getElementById('shipping-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const orderId = document.getElementById('shipping-order-id').value;
        const warehouseId = document.getElementById('shipping-warehouse-id').value;
        
        // Map inputs to order item serial list
        // Form schema: {"order_item_id": [serial1, serial2]}
        const inputs = document.querySelectorAll('.imei-serial-key');
        const serialMap = {};
        
        inputs.forEach(inp => {
            const itemId = inp.getAttribute('data-item-id');
            const key = inp.value.trim();
            if (key) {
                if (!serialMap[itemId]) serialMap[itemId] = [];
                serialMap[itemId].push(key);
            }
        });

        try {
            await apiCall(`/api/orders/${orderId}/ship?warehouseId=${warehouseId}`, {
                method: 'POST',
                body: JSON.stringify(serialMap)
            });
            showToast('Đã bắt đầu vận chuyển đơn hàng');
            document.getElementById('shipping-modal').classList.remove('active');
            refreshAdminOrdersTable();
        } catch(err) {
            showToast(err.message, 'danger');
        }
    });
}

function setupNavbarListeners() {
    // Quick lookup warranty floating banner trigger
    document.getElementById('quick-warranty-trigger').addEventListener('click', () => {
        switchPanel('warranty');
        // highlight sidebar
        const nav = document.getElementById('sidebar-nav-menu');
        nav.querySelectorAll('.nav-item').forEach(i => {
            i.classList.remove('active');
            if (i.getAttribute('data-panel') === 'warranty') i.classList.add('active');
        });
    });

    // Cart trigger from header
    document.getElementById('cart-navbar-trigger').addEventListener('click', () => {
        if (state.user && state.user.role === 'CUSTOMER') {
            switchPanel('cart');
            const nav = document.getElementById('sidebar-nav-menu');
            nav.querySelectorAll('.nav-item').forEach(i => {
                i.classList.remove('active');
                if (i.getAttribute('data-panel') === 'cart') i.classList.add('active');
            });
        }
    });

    // Global instant search input
    let searchTimeout = null;
    document.getElementById('global-search').addEventListener('input', (e) => {
        const val = e.target.value.trim();
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(async () => {
            if (state.activePanel === 'shop') {
                state.catalog.filter.search = val;
                state.catalog.filter.page = 1;
                await refreshCatalogProducts();
                renderProductCards();
                renderCatalogPagination();
            } else if (state.activePanel === 'admin_products') {
                state.admin.filter.search = val;
                state.admin.filter.page = 1;
                await refreshAdminProductsList();
                renderAdminProductsListRows();
                renderAdminProductsPagination();
            }
        }, 400);
    });
}

// ==========================================
// STRING & DATE LOCALIZATION UTILITIES
// ==========================================

function formatVND(value) {
    if (value === null || value === undefined) return '0đ';
    // Supporting both double/number and bigdecimal object maps
    const num = typeof value === 'number' ? value : parseFloat(value);
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleString('vi-VN', { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatDateOnly(dateStr) {
    if (!dateStr) return '';
    // Supporting standard date array serialization [yyyy, mm, dd] or string
    if (Array.isArray(dateStr)) {
        const [y, m, d] = dateStr;
        return `${d.toString().padStart(2, '0')}/${m.toString().padStart(2, '0')}/${y}`;
    }
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

function translateStatus(status) {
    switch(status) {
        case 'PENDING': return 'Chờ xử lý';
        case 'CONFIRMED': return 'Đã xác nhận';
        case 'SHIPPING': return 'Đang giao hàng';
        case 'COMPLETED': return 'Hoàn tất';
        case 'CANCELLED': return 'Đã hủy';
        default: return status;
    }
}

function translateWarrantyStatus(status) {
    switch(status) {
        case 'ACTIVE': return 'Đang bảo hành';
        case 'EXPIRED': return 'Hết hạn bảo hành';
        case 'NOT_SOLD': return 'Chưa bán (Lưu kho)';
        case 'UNKNOWN': return 'Chưa kích hoạt';
        default: return status;
    }
}

function getWarrantyStatusColor(status) {
    switch(status) {
        case 'ACTIVE': return 'var(--success)';
        case 'EXPIRED': return 'var(--danger)';
        case 'NOT_SOLD': return 'var(--warning)';
        default: return 'var(--text-muted)';
    }
}

function getWarrantyStatusBg(status) {
    switch(status) {
        case 'ACTIVE': return 'rgba(16, 185, 129, 0.15)';
        case 'EXPIRED': return 'rgba(239, 68, 68, 0.15)';
        case 'NOT_SOLD': return 'rgba(245, 158, 11, 0.15)';
        default: return 'rgba(255, 255, 255, 0.05)';
    }
}
