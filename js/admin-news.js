const loginForm=document.querySelector('#login-form');
const adminContent=document.querySelector('#admin-content');
const isAdmin=localStorage.getItem('satoClinicAdmin')==='true';
if(isAdmin){loginForm.hidden=true;adminContent.hidden=false}else{adminContent.hidden=true;}
loginForm.addEventListener('submit',e=>{e.preventDefault();if(document.querySelector('#login-id').value==='admin'&&document.querySelector('#login-pass').value==='edix'){localStorage.setItem('satoClinicAdmin','true');loginForm.hidden=true;adminContent.hidden=false;render()}else{document.querySelector('#login-error').textContent='IDまたはパスワードが正しくありません。'}});
document.querySelector('#logout').addEventListener('click',()=>{localStorage.removeItem('satoClinicAdmin');location.reload()});
const key='satoClinicNews';
const defaults=[{date:'2025.11.20',title:'インフルエンザ予防接種について',tag:'お知らせ',href:'../information/index.html#flu'},{date:'2025.11.10',title:'年末年始の休診について',tag:'休診',href:'../information/index.html#holiday'},{date:'2025.10.28',title:'健康診断のご案内',tag:'ご案内',href:'../information/index.html#checkup'},{date:'2025.10.15',title:'新型コロナウイルス対策について',tag:'お知らせ',href:'../information/index.html#covid'},{date:'2025.10.01',title:'オンライン診療の導入について',tag:'ご案内',href:'../information/index.html#online'}];let news=JSON.parse(localStorage.getItem(key)||'null');if(!Array.isArray(news)||news.length===0){news=defaults;localStorage.setItem(key,JSON.stringify(news));}
const save=()=>{localStorage.setItem(key,JSON.stringify(news));render()};
const render=()=>{document.querySelector('#news-admin-list').innerHTML='<table class="admin-news-table"><thead><tr><th>日付</th><th>タイトル</th><th>タグ</th><th>操作</th></tr></thead><tbody>'+news.map((n,i)=>`<tr><td>${n.date}</td><td>${n.title}</td><td>${n.tag}</td><td><button onclick="editNews(${i})">編集</button> <button onclick="deleteNews(${i})">削除</button></td></tr>`).join('')+'</tbody></table>'};
document.querySelector('#news-form').addEventListener('submit',e=>{e.preventDefault();const i=document.querySelector('#edit-index').value;const n={date:document.querySelector('#date').value,title:document.querySelector('#title').value,tag:document.querySelector('#tag').value,href:'../information/index.html'};i===''?news.unshift(n):news[Number(i)]=n;save();e.target.reset();document.querySelector('#edit-index').value=''});
window.editNews=i=>{const n=news[i];document.querySelector('#date').value=n.date;document.querySelector('#title').value=n.title;document.querySelector('#tag').value=n.tag;document.querySelector('#edit-index').value=i};
window.deleteNews=i=>{news.splice(i,1);save()};
render();