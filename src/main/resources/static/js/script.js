// Базовый URL вашего REST API в Spring Boot
const API_URL = '/api';

// Выполняется при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    fetchServices();
    fetchCart();
    fetchBookings();
});

// Вспомогательная функция для показа уведомлений (например, Rate Limiter 429)
function showNotification(text, isError = false) {
    const msgDiv = document.getElementById('status-message');
    msgDiv.textContent = text;
    msgDiv.className = `message ${isError ? 'error' : 'success'}`;
    setTimeout(() => msgDiv.className = 'message hidden', 4000);
}

// 1. Получение списка услуг (Кэш в Redis)
function fetchServices() {
    fetch(`${API_URL}/services`)
        .then(response => {
            if (response.status === 429) throw new Error('Превышен лимит запросов! (Rate Limiter)');
            return response.json();
        })
        .then(services => {
            const list = document.getElementById('services-list');
            list.innerHTML = '';
            services.forEach(service => {
                list.innerHTML += `
                    <li>
                        <span>${service.name} (${service.price} руб.)</span>
                        <button onclick="addToCart(${service.id})">+ В корзину</button>
                    </li>
                `;
            });
        })
        .catch(err => showNotification(err.message, true));
}

// 2. Добавление во временную корзину (TTL в Redis)
function addToCart(serviceId) {
    fetch(`${API_URL}/cart/add?serviceId=${serviceId}`, { method: 'POST' })
        .then(response => {
            if (response.status === 429) throw new Error('Превышен лимит запросов!');
            if (!response.ok) throw new Error('Ошибка добавления');
            return response.json();
        })
        .then(cart => {
            renderCart(cart);
            showNotification('Добавлено в корзину (установлен TTL)');
        })
        .catch(err => showNotification(err.message, true));
}

// Получение состояния корзины
function fetchCart() {
    fetch(`${API_URL}/cart`)
        .then(res => res.ok ? res.json() : null)
        .then(cart => renderCart(cart))
        .catch(() => {});
}

// Отображение корзины
function renderCart(cart) {
    const container = document.getElementById('cart-content');
    if (!cart || !cart.items || cart.items.length === 0) {
        container.innerHTML = '<p>Корзина пуста (или время жизни TTL истекло)</p>';
        return;
    }

    let html = '<ul class="list">';
    cart.items.forEach(item => {
        html += `<li>${item.serviceName} - ${item.price} руб.</li>`;
    });
    html += '</ul>';
    container.innerHTML = html;
}

// Очистить корзину
function clearCart() {
    fetch(`${API_URL}/cart`, { method: 'DELETE' })
        .then(() => {
            renderCart(null);
            showNotification('Корзина очищена');
        });
}

// 3. Создание заявки из корзины
function createBooking() {
    fetch(`${API_URL}/bookings/create`, { method: 'POST' })
        .then(res => {
            if (res.status === 404) throw new Error('Корзина пуста или истек TTL!');
            if (!res.ok) throw new Error('Ошибка создания заявки');
            return res.json();
        })
        .then(() => {
            showNotification('Заявка успешно создана!');
            fetchCart();
            fetchBookings();
        })
        .catch(err => showNotification(err.message, true));
}

// 4. Получение списка заявок (Админка)
function fetchBookings() {
    fetch(`${API_URL}/bookings`)
        .then(res => res.json())
        .then(bookings => {
            const tbody = document.getElementById('bookings-table-body');
            tbody.innerHTML = '';
            bookings.forEach(b => {
                tbody.innerHTML += `
                    <tr>
                        <td>#${b.id}</td>
                        <td>${b.serviceName}</td>
                        <td><b>${b.status}</b></td>
                        <td>
                            <button onclick="updateStatus(${b.id}, 'APPROVED')" class="btn-primary">Одобрить</button>
                            <button onclick="updateStatus(${b.id}, 'REJECTED')" class="btn-danger">Отклонить</button>
                        </td>
                    </tr>
                `;
            });
        })
        .catch(err => showNotification(err.message, true));
}

// Изменение статуса заявки администратором
function updateStatus(id, newStatus) {
    fetch(`${API_URL}/bookings/${id}/status?status=${newStatus}`, { method: 'PUT' })
        .then(res => {
            if (res.ok) {
                showNotification(`Заявка #${id} обновлена на ${newStatus}`);
                fetchBookings();
            }
        });
}