const login = document.querySelector('#login-form');
const dashboard = document.querySelector('#dashboard');
const show = () => { login.hidden = true; dashboard.hidden = false; };
fetch('/api/auth/me').then(response => { if (response.ok) show(); });
login.addEventListener('submit', async event => {
  event.preventDefault();
  const response = await fetch('/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ loginId: document.querySelector('#login-id').value, password: document.querySelector('#login-pass').value }) });
  if (response.ok) show();
  else document.querySelector('#login-error').textContent = 'IDまたはパスワードが正しくありません。';
});
document.querySelector('#logout').addEventListener('click', async () => { await fetch('/api/auth/logout', { method: 'POST' }); location.reload(); });
