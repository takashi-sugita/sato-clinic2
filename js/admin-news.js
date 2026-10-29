const loginForm = document.querySelector('#login-form');
const adminContent = document.querySelector('#admin-content');
const loginError = document.querySelector('#login-error');
const newsList = document.querySelector('#news-admin-list');
const newsForm = document.querySelector('#news-form');

let news = [];

const showAdmin = () => {
  loginForm.hidden = true;
  adminContent.hidden = false;
};

const escapeHtml = value => String(value).replace(/[&<>"']/g, char => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
}[char]));

const displayDate = value => value.replaceAll('-', '.');

const render = () => {
  newsList.innerHTML = '<table class="admin-news-table"><thead><tr><th>日付</th><th>タイトル</th><th>タグ</th><th>操作</th></tr></thead><tbody>' +
    news.map(item => `<tr><td>${escapeHtml(displayDate(item.date))}</td><td>${escapeHtml(item.title)}</td><td>${escapeHtml(item.tag)}</td><td><button type="button" data-edit="${item.id}">編集</button> <button type="button" data-delete="${item.id}">削除</button></td></tr>`).join('') +
    '</tbody></table>';
};

const loadNews = async () => {
  const response = await fetch('/api/admin/news');
  if (!response.ok) throw new Error('お知らせ一覧を読み込めませんでした。');
  news = await response.json();
  render();
};

const showError = message => {
  newsList.innerHTML = `<p role="alert">${escapeHtml(message)}</p>`;
};

fetch('/api/auth/me')
  .then(async response => {
    if (!response.ok) return;
    showAdmin();
    try {
      await loadNews();
    } catch (error) {
      showError(error.message);
    }
  })
  .catch(() => {
    loginError.textContent = 'ログイン状態を確認できませんでした。ページを再読み込みしてください。';
  });

loginForm.addEventListener('submit', async event => {
  event.preventDefault();
  loginError.textContent = '';
  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        loginId: document.querySelector('#login-id').value,
        password: document.querySelector('#login-pass').value
      })
    });
    if (!response.ok) {
      loginError.textContent = 'IDまたはパスワードが正しくありません。';
      return;
    }
    showAdmin();
    await loadNews();
  } catch (error) {
    showError(error.message || 'ログインできませんでした。時間をおいて再度お試しください。');
  }
});

newsForm.addEventListener('submit', async event => {
  event.preventDefault();
  const id = document.querySelector('#edit-index').value;
  const existing = news.find(item => String(item.id) === id);
  const payload = {
    date: document.querySelector('#date').value,
    title: document.querySelector('#title').value,
    tag: document.querySelector('#tag').value,
    href: existing?.href || '/information/index.html'
  };
  try {
    const response = await fetch(id ? `/api/admin/news/${id}` : '/api/admin/news', {
      method: id ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!response.ok) {
      const result = await response.json().catch(() => ({}));
      throw new Error(result.error || 'お知らせを保存できませんでした。');
    }
    newsForm.reset();
    document.querySelector('#edit-index').value = '';
    document.querySelector('#tag').value = 'お知らせ';
    await loadNews();
  } catch (error) {
    showError(error.message);
  }
});

newsList.addEventListener('click', async event => {
  const editId = event.target.dataset.edit;
  const deleteId = event.target.dataset.delete;
  if (editId) {
    const item = news.find(entry => String(entry.id) === editId);
    if (!item) return;
    document.querySelector('#date').value = displayDate(item.date);
    document.querySelector('#title').value = item.title;
    document.querySelector('#tag').value = item.tag;
    document.querySelector('#edit-index').value = item.id;
    return;
  }
  if (!deleteId) return;
  try {
    const response = await fetch(`/api/admin/news/${deleteId}`, { method: 'DELETE' });
    if (!response.ok) throw new Error('お知らせを削除できませんでした。');
    await loadNews();
  } catch (error) {
    showError(error.message);
  }
});
