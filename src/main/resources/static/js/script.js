// Точки подключения к Spring Boot REST API. Контракты исходной версии сохранены.
const API_URL = '/api';

document.addEventListener('DOMContentLoaded', () => {
    fetchServices();
    fetchCart();
    fetchBookings();
});

function showNotification(text, isError = false) {
    const msg = document.getElementById('status-message');
    msg.textContent = text;
    msg.className = `message ${isError ? 'error' : 'success'}`;
    clearTimeout(showNotification.timer);
    showNotification.timer = setTimeout(() => msg.className = 'message hidden', 4000);
}

function safeText(value) {
    return String(value ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[c]));
}

function request(url, options) {
    return fetch(url, options).then(r => {
        if (r.status === 429) throw new Error('Превышен лимит запросов. Попробуйте чуть позже.');
        if (!r.ok) throw new Error('Не удалось получить данные');
        return r;
    });
}

function fetchServices() {
    request(`${API_URL}/services`)
        .then(r => r.json())
        .then(services => {
            window.availableServices = services;
            const list = document.getElementById('services-list');
            if (list) {
                document.getElementById('service-count').textContent = services.length;
                list.innerHTML = services.length ? services.map(s => `
                    <li>
                        <span class="service-name">${safeText(s.name)}<small>Доступно для бронирования</small></span>
                        <span class="price">${safeText(s.price)} ₽</span>
                        <button class="add-button" onclick="addToCart(${Number(s.id)})">Добавить</button>
                    </li>
                `).join('') : '<li class="empty-state">Список услуг пока пуст</li>';
            }

            // Заполняем селект в клиентской форме, если он есть на странице
            const userServiceSelect = document.getElementById('user-service');
            if (userServiceSelect) {
                userServiceSelect.innerHTML = '<option value="">Выберите аудиторию / услугу</option>' + services.map(s => `
                    <option value="${Number(s.id)}">${safeText(s.name)} — ${safeText(s.price)} ₽</option>
                `).join('');
            }
        })
        .catch(err => {
            const list = document.getElementById('services-list');
            if (list) list.innerHTML = '<li class="empty-state">Каталог временно недоступен</li>';
            showNotification(err.message, true);
        });
}

function addToCart(serviceId) {
    request(`${API_URL}/cart/add?serviceId=${encodeURIComponent(serviceId)}`, { method: 'POST' })
        .then(r => r.json())
        .then(cart => {
            renderCart(cart);
            showNotification('Услуга добавлена во временную корзину');
        })
        .catch(err => showNotification(err.message, true));
}

function fetchCart() {
    fetch(`${API_URL}/cart`)
        .then(r => r.ok ? r.json() : null)
        .then(renderCart)
        .catch(() => renderCart(null));
}

function renderCart(cart) {
    const box = document.getElementById('cart-content');
    if (!box) return;
    if (!cart?.items?.length) {
        box.innerHTML = '<div class="cart-empty"><span>✦</span><p>В корзине пока нет<br>выбранных услуг</p></div>';
        return;
    }
    box.innerHTML = `
        <ul class="cart-items">
            ${cart.items.map(i => `
                <li>
                    <span>${safeText(i.serviceName)}</span>
                    <span>${safeText(i.price)} ₽</span>
                </li>
            `).join('')}
        </ul>
    `;
}

function clearCart() {
    fetch(`${API_URL}/cart`, { method: 'DELETE' })
        .then(() => {
            renderCart(null);
            showNotification('Временная корзина очищена');
        })
        .catch(() => showNotification('Не удалось очистить корзину', true));
}

function createBooking() {
    request(`${API_URL}/bookings/create`, { method: 'POST' })
        .then(r => r.json())
        .then(() => {
            showNotification('Заявка успешно создана');
            fetchCart();
            fetchBookings();
        })
        .catch(err => showNotification(
            err.message === 'Не удалось получить данные' ? 'Корзина пуста или время её жизни истекло' : err.message,
            true
        ));
}

function statusClass(value) {
    const n = String(value).toLowerCase();
    return n.includes('approv') || n.includes('одобр') ? 'status-approved' :
        n.includes('reject') || n.includes('отклон') ? 'status-rejected' : 'status-pending';
}

function fetchBookings() {
    request(`${API_URL}/bookings`)
        .then(r => r.json())
        .then(bookings => {
            const bookingCountElem = document.getElementById('booking-count');
            if (bookingCountElem) bookingCountElem.textContent = bookings.length;

            const body = document.getElementById('bookings-table-body');
            if (body) {
                body.innerHTML = bookings.length ? bookings.map(b => `
                    <tr>
                        <td>#${safeText(b.id)}</td>
                        <td>${safeText(b.serviceName)}</td>
                        <td><span class="status ${statusClass(b.status)}">${safeText(b.status)}</span></td>
                        <td>
                            <div class="action-group">
                                <button class="action-button approve" onclick="updateStatus(${Number(b.id)}, 'APPROVED')">Одобрить</button>
                                <button class="action-button reject" onclick="updateStatus(${Number(b.id)}, 'REJECTED')">Отклонить</button>
                            </div>
                        </td>
                    </tr>
                `).join('') : '<tr><td colspan="4" class="empty-cell">Заявок пока нет</td></tr>';
            }
        })
        .catch(err => {
            const body = document.getElementById('bookings-table-body');
            if (body) body.innerHTML = '<tr><td colspan="4" class="empty-cell">Очередь временно недоступна</td></tr>';
            showNotification(err.message, true);
        });
}

// Заглушка для клиентской истории заявок (если используется на странице кабинета)
function fetchMyBookings() {
    fetchBookings();
}

function updateStatus(id, newStatus) {
    request(`${API_URL}/bookings/${encodeURIComponent(id)}/status?status=${encodeURIComponent(newStatus)}`, { method: 'PUT' })
        .then(() => {
            showNotification(`Статус заявки #${id} обновлён`);
            fetchBookings();
        })
        .catch(err => showNotification(err.message, true));
}

function openAccess(mode) {
    const modal = document.getElementById('access-modal');
    if (!modal) return;
    const login = mode === 'login';
    modal.classList.remove('hidden');
    document.getElementById('login-form').classList.toggle('hidden', !login);
    document.getElementById('register-form').classList.toggle('hidden', login);
    document.getElementById('access-title').textContent = login ? 'С возвращением!' : 'Начнём сиять!';
    document.getElementById('access-lead').textContent = login ? 'Войдите, чтобы продолжить работу с бронированиями.' : 'Создайте профиль для работы с системой бронирования.';
}

function closeAccess() {
    const modal = document.getElementById('access-modal');
    if (modal) modal.classList.add('hidden');
}

function loginUser(event) {
    event.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    request(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    })
        .then(() => {
            closeAccess();
            showNotification('Добро пожаловать в UniReserve!');
        })
        .catch(() => showNotification('Вход пока ожидает подключения Spring Boot API', true));
}

function registerUser(event) {
    event.preventDefault();
    const username = document.getElementById('reg-username').value;
    const password = document.getElementById('reg-password').value;
    const role = document.getElementById('reg-role').value;

    request(`${API_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, role })
    })
        .then(() => {
            closeAccess();
            showNotification('Аккаунт создан — добро пожаловать в академию!');
        })
        .catch(() => showNotification('Регистрация ожидает подключения Spring Boot API', true));
}

function openBookingForm() {
    const select = document.getElementById('booking-service');
    const services = window.availableServices || [];
    if (select) {
        select.innerHTML = '<option value="">Выберите услугу</option>' + services.map(s => `
            <option value="${Number(s.id)}">${safeText(s.name)} — ${safeText(s.price)} ₽</option>
        `).join('');
    }
    const modal = document.getElementById('booking-modal');
    if (modal) modal.classList.remove('hidden');
}

function closeBookingForm() {
    const modal = document.getElementById('booking-modal');
    if (modal) modal.classList.add('hidden');
}

function submitBooking(event) {
    event.preventDefault();
    const serviceId = Number(document.getElementById('booking-service')?.value || document.getElementById('user-service')?.value);
    const startsAt = document.getElementById('booking-date')?.value || `${document.getElementById('booking-day')?.value}T${document.getElementById('user-start-time')?.value || '00:00'}`;
    const comment = document.getElementById('booking-comment')?.value.trim() || document.getElementById('user-comment')?.value.trim() || '';

    if (!serviceId) {
        showNotification('Выберите услугу для заявки', true);
        return;
    }

    request(`${API_URL}/bookings`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ serviceId, startsAt, comment })
    })
        .then(() => {
            closeBookingForm();
            event.target.reset();
            showNotification('Заявка отправлена в очередь администратора!');
            fetchBookings();
        })
        .catch(() => showNotification('Отправка ждёт подключения POST /api/bookings в Spring Boot', true));
}

// Заглушки для специфичных клиентских функций интерфейса бронирования слотов
function loadAvailableSlots() {
    const slotsContainer = document.getElementById('available-slots');
    if (!slotsContainer) return;
    // Пример генерации доступных слотов для демонстрации интерфейса
    slotsContainer.innerHTML = `
        <button type="button" class="slot-button" onclick="selectSlot('10:00')">10:00</button>
        <button type="button" class="slot-button" onclick="selectSlot('12:00')">12:00</button>
        <button type="button" class="slot-button" onclick="selectSlot('14:00')">14:00</button>
        <button type="button" class="slot-button" onclick="selectSlot('16:00')">16:00</button>
    `;
}

function selectSlot(time) {
    const startTimeInput = document.getElementById('user-start-time');
    const endTimeInput = document.getElementById('user-end-time');
    if (startTimeInput) startTimeInput.value = time;
    // Автоматически ставим плюс час для окончания слота
    if (endTimeInput) {
        const [h, m] = time.split(':');
        const endH = String(Number(h) + 1).padStart(2, '0');
        endTimeInput.value = `${endH}:${m}`;
    }
    showNotification(`Выбран временной слот: ${time}`);
}

function showClientNotice() {
    showNotification('Выход из аккаунта клиента выполнен');
}