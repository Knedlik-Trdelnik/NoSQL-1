// ============================================
// CONFIG
// ============================================

const API_URL = '/api';
const TOKEN_KEY = 'token';
const LOGIN_URL = `${API_URL}/auth/login`;
const SERVICES_URL = `${API_URL}/services`



// ============================================
// LIVE2D WIDGET (MADOKA)
// ============================================

function initLive2D() {
    if (window.L2Dwidget) {
        window.L2Dwidget.init({
            "model": {
                "jsonPath": "https://cdn.jsdelivr.net/gh/evrstr/live2d-widget-models/live2d_evrstr/madoka/model.json"
            },
            "display": {
                "position": "right",
                "width": 460,
                "height": 560,
                "hOffset": -150,
                "vOffset": 0
            },
            "mobile": {
                "show": true,
                "scale": 0.3,
                "motion": true
            }
        });
    } else {
        // Динамическая загрузка скрипта Live2D CDN
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/live2d-widget@3.1.4/lib/L2Dwidget.min.js';
        script.onload = () => initLive2D();
        document.head.appendChild(script);
    }
}


// ============================================
// NOTIFICATIONS
// ============================================

function notify(text, error = false) {
    const el = document.getElementById('status-message');
    if (!el) return;

    el.textContent = text;
    el.className = `message ${error ? 'error' : 'success'}`;

    clearTimeout(notify.timer);
    notify.timer = setTimeout(() => {
        el.className = 'message hidden';
    }, 4000);
}


// ============================================
// HTML ESCAPE
// ============================================

function esc(v) {
    return String(v ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[c]));
}


// ============================================
// AUTH
// ============================================

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function saveToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

function removeToken() {
    localStorage.removeItem(TOKEN_KEY);
}

function showAuthScreen() {
    document.body.classList.add('auth-locked');
    const authScreen = document.getElementById('auth-screen');
    if (authScreen) {
        authScreen.classList.remove('hidden');
    }
}

function hideAuthScreen() {
    document.body.classList.remove('auth-locked');
    const authScreen = document.getElementById('auth-screen');
    if (authScreen) {
        authScreen.classList.add('hidden');
    }
}

function checkAuth() {
    const token = getToken();
    if (token) {
        hideAuthScreen();
        return true;
    }
    showAuthScreen();
    return false;
}


// ============================================
// LOGIN / LOGOUT
// ============================================

async function handleLogin(event) {
    event.preventDefault();

    const loginInput = document.getElementById('auth-login');
    const passwordInput = document.getElementById('auth-password');
    const errorBox = document.getElementById('auth-error');

    if (!loginInput || !passwordInput) return;

    const login = loginInput.value.trim();
    const password = passwordInput.value;

    if (errorBox) errorBox.textContent = '';

    if (!login || !password) {
        if (errorBox) errorBox.textContent = 'Введите логин и пароль';
        return;
    }

    try {
        const response = await fetch(LOGIN_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: login, password: password })
        });

        if (!response.ok) {
            if (response.status === 401) throw new Error('Неверный логин или пароль');
            throw new Error('Ошибка авторизации');
        }

        const data = await response.json();
        const token = data.accessToken || data.token || data.jwt;

        if (!token) throw new Error('Сервер не вернул токен');

        saveToken(token);
        hideAuthScreen();
        notify('Добро пожаловать!');

        getServices();
        fetchMyBookings();

    } catch (error) {
        console.error('Login error:', error);
        if (errorBox) errorBox.textContent = error.message || 'Не удалось войти';
    }
}

function logoutUser() {
    removeToken();
    showAuthScreen();

    const passwordInput = document.getElementById('auth-password');
    if (passwordInput) passwordInput.value = '';

    const loginInput = document.getElementById('auth-login');
    if (loginInput) loginInput.value = '';

    notify('Вы вышли из системы');
}


// ============================================
// REGISTER
// ============================================

function showLoginForm() {
    document.getElementById('login-form-container')?.classList.remove('hidden');
    document.getElementById('register-form-container')?.classList.add('hidden');
    const registerError = document.getElementById('register-error');
    if (registerError) registerError.textContent = '';
}

function showRegisterForm() {
    document.getElementById('login-form-container')?.classList.add('hidden');
    document.getElementById('register-form-container')?.classList.remove('hidden');
    const authError = document.getElementById('auth-error');
    if (authError) authError.textContent = '';
}

async function handleRegister(event) {
    event.preventDefault();

    const login = document.getElementById('register-login')?.value.trim();
    const email = document.getElementById('register-email')?.value.trim();
    const password = document.getElementById('register-password')?.value;
    const repeatPassword = document.getElementById('register-password-repeat')?.value;
    const errorBox = document.getElementById('register-error');

    if (errorBox) errorBox.textContent = '';

    if (!login || !email || !password || !repeatPassword) {
        if (errorBox) errorBox.textContent = 'Заполните все поля';
        return;
    }

    if (password !== repeatPassword) {
        if (errorBox) errorBox.textContent = 'Пароли не совпадают';
        return;
    }

    if (password.length < 6) {
        if (errorBox) errorBox.textContent = 'Пароль должен содержать минимум 6 символов';
        return;
    }

    try {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: login, email: email, password: password })
        });

        if (!response.ok) {
            if (response.status === 409) throw new Error('Пользователь с таким логином уже существует');
            throw new Error('Не удалось зарегистрировать пользователя');
        }

        const data = await response.json().catch(() => null);

        if (data) {
            const token = data.accessToken || data.token || data.jwt;
            if (token) {
                saveToken(token);
                hideAuthScreen();
                notify('Регистрация успешно завершена!');
                getServices();
                fetchMyBookings();
                return;
            }
        }

        showLoginForm();
        const loginInput = document.getElementById('auth-login');
        if (loginInput) loginInput.value = login;
        notify('Аккаунт создан. Теперь войдите в систему.');

    } catch (error) {
        console.error('Registration error:', error);
        if (errorBox) errorBox.textContent = error.message || 'Ошибка регистрации';
    }
}


// ============================================
// API
// ============================================

async function api(url, options = {}) {
    const token = getToken();
    const headers = { ...(options.headers || {}) };

    if (options.body && typeof options.body === 'string' && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, { ...options, headers });

    if (response.status === 429) {
        throw new Error('Слишком много запросов. Попробуйте позднее.');
    }

    if (response.status === 401) {
        removeToken();
        showAuthScreen();
        throw new Error('Сессия истекла. Войдите снова.');
    }

    if (!response.ok) {
        throw new Error('Сервис временно недоступен');
    }

    return response;
}


// ============================================
// SERVICES & SLOTS
// ============================================

function getServices() {
    if (!getToken()) return;

    // Путь должен соответствовать вашему @GetMapping в Spring Boot
    api(`${SERVICES_URL}/classrooms`)
        .then(r => r.json())
        .then(classrooms => {
            const select = document.getElementById('user-service');
            if (!select) return;

            select.innerHTML = '<option value="">Выберите аудиторию</option>' +
                classrooms.map(c => `<option value="${Number(c.id)}">${esc(c.name)}</option>`).join('');
        })
        .catch(e => {
            const select = document.getElementById('user-service');
            if (select) select.innerHTML = '<option value="">Аудитории недоступны</option>';
            notify(e.message, true);
        });
}

function slotTime(value) {
    const text = String(value ?? '');
    return (text.includes('T') ? text.split('T')[1] : text).slice(0, 5);
}

function loadAvailableSlots() {
    if (!getToken()) return;

    const serviceId = document.getElementById('user-service')?.value;
    const box = document.getElementById('available-slots');

    if (!box) return;

    if (!serviceId) {
        box.innerHTML = '<p>Выберите аудиторию, чтобы увидеть доступное время.</p>';
        return;
    }

    box.innerHTML = '<p>Ищем доступные слоты…</p>';

    // Убираем параметр date из запроса
    api(`${API_URL}/services/${encodeURIComponent(serviceId)}/available-slots`)
        .then(r => r.json())
        .then(slots => {
            window.freeSlots = slots;
            if (!slots.length) {
                box.innerHTML = '<p>Для этой аудитории нет слотов.</p>';
                return;
            }

            box.innerHTML = slots.map((slot, index) => {
                const start = slotTime(slot.timeStart);
                const end = slotTime(slot.timeEnd);
                return `
                    <button class="slot-button" type="button" onclick="selectSlot(${index}, this)">
                        ${esc(start)} — ${esc(end)}
                    </button>
                `;
            }).join('');
        })
        .catch(e => {
            box.innerHTML = '<p>Слоты недоступны. Время можно указать вручную ниже.</p>';
            console.error(e);
        });
}

// Привязываем вызов только к изменению аудитории
document.getElementById('user-service')?.addEventListener('change', loadAvailableSlots);
// Слушатель на выбор дня (booking-day) можно полностью удалить, он больше не нужен для слотов

// Глобальная переменная для отслеживания выбранного из списка слота
window.selectedSlotId = null;

function selectSlot(index, button) {
    const slot = window.freeSlots?.[index];
    if (!slot) return;

    // Учитываем ваши имена полей (timeStart / timeEnd)
    const start = slotTime(slot.timeStart || slot.start || slot.startsAt);
    const end = slotTime(slot.timeEnd || slot.end || slot.endsAt);

    const startInput = document.getElementById('user-start-time');
    const endInput = document.getElementById('user-end-time');

    if (startInput) startInput.value = start;
    if (endInput) endInput.value = end;

    // Сохраняем ID выбранного слота из базы
    window.selectedSlotId = slot.id;

    document.querySelectorAll('.slot-button').forEach(b => b.classList.remove('selected'));
    button.classList.add('selected');
}

// Если пользователь начал сам менять инпуты времени руками, сбрасываем привязку к готовому слоту
document.getElementById('user-start-time')?.addEventListener('input', () => { window.selectedSlotId = null; });
document.getElementById('user-end-time')?.addEventListener('input', () => { window.selectedSlotId = null; });

// ============================================
// BOOKINGS
// ============================================

function submitUserBooking(event) {
    event.preventDefault();

    if (!getToken()) {
        showAuthScreen();
        return;
    }

    const serviceId = Number(document.getElementById('user-service')?.value);
    const startTime = document.getElementById('user-start-time')?.value;
    const endTime = document.getElementById('user-end-time')?.value;
    const comment = document.getElementById('user-comment')?.value.trim() || '';

    if (!serviceId || !startTime || !endTime) {
        notify('Заполните аудиторию и время', true);
        return;
    }

    if (startTime >= endTime) {
        notify('Время окончания должно быть позже времени начала', true);
        return;
    }

    // Формируем объект запроса в зависимости от способа выбора:
    // Если window.selectedSlotId существует — пользователь выбрал готовый слот.
    // Иначе — ввёл время вручную.
    const requestBody = {
        serviceId,
        comment
    };

    if (window.selectedSlotId) {
        requestBody.timeWindowId = window.selectedSlotId; // Передаем ID выбранного слота
    } else {
        requestBody.timeStart = startTime; // Или кастомное время, если бэкенд принимает строки
        requestBody.timeEnd = endTime;
    }

    console.log('Отправляемые данные заявки:', requestBody);

    api(`${SERVICES_URL}/bookings`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody)
    })
        .then((r) => {
            event.target.reset();
            window.selectedSlotId = null; // Сбрасываем выбранный слот
            const slotsBox = document.getElementById('available-slots');
            if (slotsBox) slotsBox.innerHTML = '<p>Выберите аудиторию, чтобы увидеть доступное время.</p>';
            notify('Заявка отправлена администратору!');
            fetchMyBookings();
        })
        .catch(e => notify(e.message, true));
}

function statusClass(s) {
    const v = String(s).toLowerCase();
    return v.includes('approv') ? 'status-approved' : v.includes('reject') ? 'status-rejected' : 'status-pending';
}

function fetchMyBookings() {
    if (!getToken()) return;

    api(`${SERVICES_URL}/bookings/my`, {
        method: 'GET'
    })
        .then(r => r.json())
        .then(rows => {
            const body = document.getElementById('my-bookings');
            if (!body) return;

            body.innerHTML = rows.length ? rows.map(b => {
                // Достаем название аудитории из объекта classroom
                const classroomName = b.classroom ? b.classroom.name : 'Не указана';

                // Достаем время из первого тайм-слота, если он есть
                let timeStr = '—';
                if (b.timeWindows && b.timeWindows.length > 0) {
                    const tw = b.timeWindows[0];
                    timeStr = `${tw.timeStart || ''} — ${tw.timeEnd || ''}`;
                }

                // Статус (если его нет в сущности, выводим заглушку)
                const status = b.status || 'В обработке';

                return `
                    <tr>
                        <td>#${esc(b.id)}</td>
                        <td>${esc(classroomName)}</td>
                        <td><span class="status ${statusClass(status)}">${esc(status)}</span></td>
                        <td>${esc(timeStr)}</td>
                    </tr>
                `;
            }).join('') : `
                <tr><td colspan="4" class="empty-cell">У вас ещё нет заявок</td></tr>
            `;
        })
        .catch(e => {
            const body = document.getElementById('my-bookings');
            if (body) body.innerHTML = `<tr><td colspan="4" class="empty-cell">Не удалось загрузить заявки</td></tr>`;
            notify(e.message, true);
        });
}

// ============================================
// INITIALIZATION
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    // Инициализация Мадоки
    initLive2D();

    const authForm = document.getElementById('auth-form');
    if (authForm) authForm.addEventListener('submit', handleLogin);

    const registerForm = document.getElementById('register-form');
    if (registerForm) registerForm.addEventListener('submit', handleRegister);

    const isAuthenticated = checkAuth();
    if (!isAuthenticated) return;

    getServices();
    fetchMyBookings();
});

function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        return JSON.parse(jsonPayload);
    } catch (e) {
        return null;
    }
}
document.addEventListener('DOMContentLoaded', () => {
    const adminLinks = document.querySelectorAll('a[href="/admin.html"]');

    adminLinks.forEach(link => {
        link.addEventListener('click', (event) => {
            event.preventDefault(); // Отменяем стандартный переход браузера

            const token = localStorage.getItem('token'); // или TOKEN_KEY

            if (!token) {
                // Нет токена — отправляем на 403
                window.location.href = '/403.html';
                return;
            }

            const payload = parseJwt(token);
            console.log(payload)
            // // Проверяем срок годности токена (exp в секундах)
            // if (!payload || (payload.exp && payload.exp * 1000 < Date.now())) {
            //     localStorage.removeItem('token');
            //     window.location.href = '/403.html';
            //     return;
            // }

            const roles = payload.roles || payload.authorities || [payload.role];
            const isAdmin = Array.isArray(roles)
                ? roles.includes('ADMIN') || roles.includes('ROLE_ADMIN')
                : roles === 'ADMIN' || roles === 'ROLE_ADMIN';
            console.log(roles)
            if (isAdmin) {
                // Роль подтверждена — переходим в админку
                window.location.href = '/admin.html';
            } else {
                // Если обычный USER — на страницу 403
                window.location.href = '/403.html';
            }
        });
    });
});
