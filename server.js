import path from 'node:path';
import { fileURLToPath } from 'node:url';
import express from 'express';
import cookieParser from 'cookie-parser';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import mysql from 'mysql2/promise';
import 'dotenv/config';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const app = express();
const port = Number(process.env.PORT || 3000);
const times = ['09:00〜10:00', '10:00〜11:00', '11:00〜12:00', '13:00〜14:00', '14:00〜15:00', '15:00〜16:00', '16:00〜17:00', '17:00〜18:00'];
const pool = mysql.createPool({
  host: process.env.DB_HOST || '127.0.0.1', port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || 'sato_app', password: process.env.DB_PASSWORD || 'change-me',
  database: process.env.DB_NAME || 'sato_clinic', waitForConnections: true, connectionLimit: 10
});

app.use(express.json());
app.use(cookieParser());
app.use(express.static(__dirname));

const requireAdmin = (req, res, next) => {
  try {
    req.admin = jwt.verify(req.cookies.sato_admin, process.env.JWT_SECRET || 'development-secret');
    next();
  } catch { res.status(401).json({ error: '管理者ログインが必要です。' }); }
};

const validDate = value => /^\d{4}-\d{2}-\d{2}$/.test(value) && value >= new Date().toISOString().slice(0, 10);
const validateReservation = body => {
  const required = ['patientType', 'department', 'patientName', 'patientPhone', 'reserveDate', 'reserveTime'];
  if (required.some(key => typeof body[key] !== 'string' || !body[key].trim())) return '必須項目を入力してください。';
  if (!['new', 'existing'].includes(body.patientType)) return '患者区分が不正です。';
  if (!times.includes(body.reserveTime) || !validDate(body.reserveDate)) return '予約日時が不正です。';
  if (body.patientType === 'existing' && !/^\d{8}$/.test(body.cardNumber || '')) return '診察券番号は8桁で入力してください。';
  if (!/^0\d{9,10}$/.test(body.patientPhone.replace(/[^0-9]/g, ''))) return '電話番号を正しく入力してください。';
  return null;
};

app.get('/api/availability', async (req, res) => {
  const date = String(req.query.date || '');
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) return res.status(400).json({ error: '日付が不正です。' });
  const [rows] = await pool.query("SELECT reserve_time FROM reservations WHERE reserve_date = ? AND status = 'booked'", [date]);
  const booked = new Set(rows.map(row => row.reserve_time));
  res.json({ date, slots: times.map(time => ({ time, available: !booked.has(time) })) });
});

app.post('/api/reservations', async (req, res) => {
  const error = validateReservation(req.body);
  if (error) return res.status(400).json({ error });
  try {
    const [result] = await pool.execute(
      'INSERT INTO reservations (patient_type, card_number, department, patient_name, patient_phone, reserve_date, reserve_time) VALUES (?, ?, ?, ?, ?, ?, ?)',
      [req.body.patientType, req.body.cardNumber || null, req.body.department.trim(), req.body.patientName.trim(), req.body.patientPhone.trim(), req.body.reserveDate, req.body.reserveTime]
    );
    res.status(201).json({ id: result.insertId, message: '予約を受け付けました。' });
  } catch (error) {
    if (error.code === 'ER_DUP_ENTRY') return res.status(409).json({ error: 'その時間帯はすでに予約済みです。' });
    console.error(error); res.status(500).json({ error: '予約を登録できませんでした。' });
  }
});

app.post('/api/auth/login', async (req, res) => {
  const { loginId, password } = req.body || {};
  const ok = loginId === (process.env.ADMIN_ID || 'admin') && password === (process.env.ADMIN_PASSWORD || 'change-this-admin-password');
  if (!ok) return res.status(401).json({ error: 'IDまたはパスワードが正しくありません。' });
  const token = jwt.sign({ loginId }, process.env.JWT_SECRET || 'development-secret', { expiresIn: '8h' });
  res.cookie('sato_admin', token, { httpOnly: true, sameSite: 'lax', secure: process.env.NODE_ENV === 'production', maxAge: 8 * 60 * 60 * 1000 });
  res.json({ message: 'ログインしました。' });
});

app.post('/api/auth/logout', (req, res) => { res.clearCookie('sato_admin'); res.status(204).end(); });
app.get('/api/auth/me', requireAdmin, (req, res) => res.json({ loginId: req.admin.loginId }));
app.get('/api/admin/reservations', requireAdmin, async (req, res) => {
  const [rows] = await pool.query("SELECT id, patient_type AS patientType, card_number AS cardNumber, department, patient_name AS name, patient_phone AS phone, reserve_date AS date, reserve_time AS time, status, created_at AS createdAt FROM reservations WHERE status = 'booked' ORDER BY reserve_date, reserve_time");
  res.json(rows);
});
app.delete('/api/admin/reservations/:id', requireAdmin, async (req, res) => {
  const [result] = await pool.execute("UPDATE reservations SET status = 'cancelled' WHERE id = ? AND status = 'booked'", [req.params.id]);
  if (!result.affectedRows) return res.status(404).json({ error: '予約が見つかりません。' });
  res.status(204).end();
});

app.listen(port, () => console.log(`Sato Clinic API listening on :${port}`));
