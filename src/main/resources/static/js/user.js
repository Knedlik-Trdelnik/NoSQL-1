// Точки подключения к Spring Boot REST API. Контракты исходной версии сохранены.
const API_URL = '/api';

function notify(text, error = false) {
    const el = document.getElementById('status-message');
    if (!el) return;
    el.textContent = text;
    el.className = `message ${error ? 'error' : 'success'}`;
    clearTimeout(notify.timer);
    notify.timer = setTimeout(() => el.className = 'message hidden', 4000);
}

function esc(v) {
    return String(v ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[c]));
}

function api(url, options) {
    return fetch(url, options).then(r => {
        if (r.status === 429) throw new Error('Слишком много запросов. Попробуйте позднее.');
        if (!r.ok) throw new Error('Сервис временно недоступен');
        return r;
    });
}

function getServices() {
    api(`${API_URL}/services`)
        .then(r => r.json())
        .then(services => {
            const select = document.getElementById('user-service');
            if (!select) return;
            select.innerHTML = '<option value="">Выберите услугу</option>' + services.map(s => `
                <option value="${Number(s.id)}">${esc(s.name)} — ${esc(s.price)} ₽</option>
            `).join('');
        })
        .catch(e => {
            const select = document.getElementById('user-service');
            if (select) select.innerHTML = '<option value="">Услуги недоступны</option>';
            notify(e.message, true);
        });
}

function slotTime(value) {
    const text = String(value ?? '');
    return (text.includes('T') ? text.split('T')[1] : text).slice(0, 5);
}

function loadAvailableSlots() {
    const serviceId = document.getElementById('user-service')?.value;
    const day = document.getElementById('booking-day')?.value;
    const box = document.getElementById('available-slots');

    if (!box) return;
    if (!serviceId || !day) {
        box.innerHTML = '<p>Выберите аудиторию и дату, чтобы увидеть свободное время.</p>';
        return;
    }

    box.innerHTML = '<p>Ищем свободные слоты…</p>';

    api(`${API_URL}/services/${encodeURIComponent(serviceId)}/available-slots?date=${encodeURIComponent(day)}`)
        .then(r => r.json())
        .then(slots => {
            window.freeSlots = slots;
            box.innerHTML = slots.length ? slots.map((slot, index) => {
                const start = slotTime(slot.start || slot.startsAt);
                const end = slotTime(slot.end || slot.endsAt);
                return `<button class="slot-button" type="button" onclick="selectSlot(${index}, this)">${esc(start)} — ${esc(end)}</button>`;
            }).join('') : '<p>На выбранную дату свободных слотов нет.</p>';
        })
        .catch(() => {
            box.innerHTML = '<p>Подключите GET /api/services/{id}/available-slots?date=YYYY-MM-DD. Время можно указать вручную ниже.</p>';
        });
}

function selectSlot(index, button) {
    const slot = window.freeSlots[index];
    if (!slot) return;

    const start = slotTime(slot.start || slot.startsAt);
    const end = slotTime(slot.end || slot.endsAt);

    const startTimeInput = document.getElementById('user-start-time');
    const endTimeInput = document.getElementById('user-end-time');

    if (startTimeInput) startTimeInput.value = start;
    if (endTimeInput) endTimeInput.value = end;

    document.querySelectorAll('.slot-button').forEach(b => b.classList.remove('selected'));
    button.classList.add('selected');
}

function submitUserBooking(event) {
    event.preventDefault();

    const serviceId = Number(document.getElementById('user-service')?.value);
    const day = document.getElementById('booking-day')?.value;
    const startTime = document.getElementById('user-start-time')?.value;
    const endTime = document.getElementById('user-end-time')?.value;
    const comment = document.getElementById('user-comment')?.value.trim() || '';

    if (!serviceId || !day || !startTime || !endTime) {
        notify('Заполните аудиторию, дату и время', true);
        return;
    }

    if (startTime >= endTime) {
        notify('Время окончания должно быть позже времени начала', true);
        return;
    }

    const startsAt = `${day}T${startTime}`;
    const endsAt = `${day}T${endTime}`;

    api(`${API_URL}/bookings`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ serviceId, startsAt, endsAt, comment })
    })
        .then(() => {
            event.target.reset();
            const slotsBox = document.getElementById('available-slots');
            if (slotsBox) {
                slotsBox.innerHTML = '<p>Выберите аудиторию и дату, чтобы увидеть свободное время.</p>';
            }
            notify('Заявка отправлена администратору!');
            fetchMyBookings();
        })
        .catch(() => notify('Нужно подключить POST /api/bookings в Spring Boot', true));
}

function statusClass(s) {
    const v = String(s).toLowerCase();
    return v.includes('approv') ? 'status-approved' :
        v.includes('reject') ? 'status-rejected' : 'status-pending';
}

function fetchMyBookings() {
    api(`${API_URL}/bookings/my`)
        .then(r => r.json())
        .then(rows => {
            const body = document.getElementById('my-bookings');
            if (!body) return;
            body.innerHTML = rows.length ? rows.map(b => `
                <tr>
                    <td>#${esc(b.id)}</td>
                    <td>${esc(b.serviceName)}</td>
                    <td><span class="status ${statusClass(b.status)}">${esc(b.status)}</span></td>
                    <td>${esc(b.startsAt || '—')}</td>
                </tr>
            `).join('') : '<tr><td colspan="4" class="empty-cell">У вас ещё нет заявок</td></tr>';
        })
        .catch(() => {
            const body = document.getElementById('my-bookings');
            if (body) {
                body.innerHTML = '<tr><td colspan="4" class="empty-cell">Подключите GET /api/bookings/my для истории заявок</td></tr>';
            }
        });
}

function showClientNotice() {
    notify('Выход будет подключён к Spring Security');
}

document.addEventListener('DOMContentLoaded', () => {
    getServices();
    fetchMyBookings();
});