// Точки подключения к Spring Boot REST API.
const API_URL = '/api';
const SERVICE_URL = '/api/services';

document.addEventListener('DOMContentLoaded', () => {
    updateAuthUI(); // Проверяем авторизацию при загрузке страницы

    if (getToken()) {
        fetchBookings(); // Грузим заявки только если пользователь авторизован
    }
});

// Переключение форм на экране входа
function showLoginForm() {
    const loginContainer = document.getElementById('login-form-container');
    const registerContainer = document.getElementById('register-form-container');
    if (loginContainer) loginContainer.classList.remove('hidden');
    if (registerContainer) registerContainer.classList.add('hidden');
}

function showRegisterForm() {
    const loginContainer = document.getElementById('login-form-container');
    const registerContainer = document.getElementById('register-form-container');
    if (loginContainer) loginContainer.classList.add('hidden');
    if (registerContainer) registerContainer.classList.remove('hidden');
}

// Управление состоянием экрана (авторизован / не авторизован)
function updateAuthUI() {
    const token = getToken();
    const authScreen = document.getElementById('auth-screen');

    if (token) {
        // Убираем блокировку, прячем окно входа, показываем контент
        document.body.classList.remove('auth-locked');
        if (authScreen) authScreen.classList.add('hidden');
    } else {
        // Блокируем экран, показываем окно входа
        document.body.classList.add('auth-locked');
        if (authScreen) authScreen.classList.remove('hidden');
        showLoginForm(); // По умолчанию показываем форму логина
    }
}

// Выход из системы
async function logout() {
    const token = getToken();

    if (token) {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
        } catch (error) {
            console.error('Ошибка при отправке запроса на выход:', error);
        }
    }

    removeToken();
    showAuthScreen();

    const passwordInput = document.getElementById('auth-password');
    if (passwordInput) passwordInput.value = '';

    const loginInput = document.getElementById('auth-login');
    if (loginInput) loginInput.value = '';

    notify('Вы вышли из системы');
}

// Вход
function loginUser(event) {
    event.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    request(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    })
        .then(r => r.json())
        .then(data => {
            const token = data.token || data;
            localStorage.setItem('token', token);

            showNotification('Добро пожаловать в панель управления!');
            updateAuthUI();
            fetchBookings();
        })
        .catch(err => showNotification(err.message, true));
}

// Регистрация
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
            showNotification('Аккаунт создан! Теперь вы можете войти.');
            showLoginForm(); // Перекидываем на форму входа
        })
        .catch(err => showNotification('Ошибка регистрации: ' + err.message, true));
}

function showNotification(text, isError = false) {
    const msg = document.getElementById('status-message');
    if (!msg) return;
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

function statusClass(value) {
    const n = String(value).toLowerCase();
    return n.includes('approv') || n.includes('одобр') ? 'status-approved' :
        n.includes('reject') || n.includes('отклон') ? 'status-rejected' : 'status-pending';
}

function fetchBookings() {
    request(`${SERVICE_URL}/bookings/all`)
        .then(r => r.json())
        .then(bookings => {
            document.getElementById('booking-count').textContent = bookings.length;
            const body = document.getElementById('bookings-table-body');
            console.log(bookings)
            body.innerHTML = bookings.length ? bookings.map(b => {
                const classroomName = b.name;

                let timeStr = '—';
                if (b.timeWindows && b.timeWindows.length > 0) {
                    const tw = b.timeWindows[0];
                    const start = tw.timeStart || tw.startTime || '';
                    const end = tw.timeEnd || tw.endTime || '';
                    if (start || end) {
                        timeStr = `${start} — ${end}`;
                    }
                } else if (b.startsAt) {
                    timeStr = safeText(b.startsAt);
                }

                return `
                    <tr>
                        <td>#${safeText(b.id)}</td>
                        <td>${safeText(classroomName)}</td>
                        <td>${safeText(timeStr)}</td>
                        <td><span class="status ${statusClass(b.status)}">${safeText(b.status)}</span></td>
                        <td>
                            <div class="action-group">
                                <button class="action-button approve" onclick="updateStatus(${Number(b.id)}, 'APPROVED')">Одобрить</button>
                                <button class="action-button reject" onclick="updateStatus(${Number(b.id)}, 'REJECTED')">Отклонить</button>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('') : '<tr><td colspan="5" class="empty-cell">Заявок пока нет</td></tr>';
        })
        .catch(err => {
            document.getElementById('bookings-table-body').innerHTML = '<tr><td colspan="5" class="empty-cell">Очередь временно недоступна</td></tr>';
            showNotification(err.message, true);
        });
}

function updateStatus(id, newStatus) {
    request(`${SERVICE_URL}/bookings/${encodeURIComponent(id)}/status?status=${encodeURIComponent(newStatus)}`, { method: 'PUT' })
        .then(() => {
            showNotification(`Статус заявки #${id} обновлён`);
            fetchBookings();

            // Если на странице есть функция загрузки слотов — вызываем её
            if (typeof loadAvailableSlots === 'function') {
                loadAvailableSlots();
            }
        })
        .catch(err => showNotification(err.message, true));
}

function getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
}

function request(url, options = {}) {
    const token = getToken();
    const headers = {
        ...(options.headers || {}),
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };

    return fetch(url, { ...options, headers }).then(r => {
        if (r.status === 429) throw new Error('Превышен лимит запросов. Попробуйте чуть позже.');
        if (r.status === 401 || r.status === 403) throw new Error('Недостаточно прав или сессия истекла');
        if (!r.ok) throw new Error('Не удалось получить данные');
        return r;
    });
}