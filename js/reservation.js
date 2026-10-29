const form = document.querySelector('#reservation-form');
if (form) {
  const date = document.querySelector('#reserve-date');
  const type = document.querySelector('#patient-type');
  const cardField = document.querySelector('#card-number-field');
  const card = document.querySelector('#card-number');
  const result = document.querySelector('#reservation-result');
  const availability = document.querySelector('#availability');
  date.min = new Date().toISOString().slice(0, 10);
  const updateCard = () => {
    const existing = type.value === 'existing';
    cardField.hidden = !existing;
    card.required = existing;
    if (!existing) card.value = '';
  };
  const draw = async () => {
    if (!date.value) { availability.textContent = '日付を選択してください。'; return; }
    const response = await fetch(`/api/availability?date=${encodeURIComponent(date.value)}`);
    const data = await response.json();
    availability.innerHTML = data.slots.map(slot => `<p>${slot.time}：${slot.available ? '予約可能' : '予約済み'}</p>`).join('');
  };
  type.addEventListener('change', updateCard);
  date.addEventListener('change', draw);
  form.addEventListener('submit', async event => {
    event.preventDefault();
    result.classList.remove('is-error');
    const payload = { patientType: type.value, department: document.querySelector('#department').value, cardNumber: card.value, patientName: document.querySelector('#patient-name').value, patientPhone: document.querySelector('#patient-phone').value, reserveDate: date.value, reserveTime: document.querySelector('#reserve-time').value };
    const response = await fetch('/api/reservations', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
    const data = await response.json();
    result.textContent = data.message || data.error;
    if (!response.ok) result.classList.add('is-error');
    else { form.reset(); updateCard(); draw(); }
  });
  updateCard();
  draw();
}
