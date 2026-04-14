const http = require('http');
const fs = require('fs');
const path = require('path');

const html = `<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>ALTOINDO - Android MLM App</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Segoe UI', sans-serif; background: #F0F2FF; color: #333; }
    .header {
      background: linear-gradient(135deg, #1A237E, #3949AB);
      color: white;
      padding: 48px 20px 40px;
      text-align: center;
    }
    .logo {
      width: 130px;
      height: 130px;
      border-radius: 50%;
      object-fit: cover;
      border: 4px solid rgba(255,255,255,0.4);
      box-shadow: 0 8px 32px rgba(0,0,0,0.3);
      margin-bottom: 20px;
    }
    .header h1 { font-size: 2.6rem; letter-spacing: 2px; margin-bottom: 6px; }
    .header .sub { font-size: 0.95rem; opacity: 0.82; margin-bottom: 16px; }
    .badges { display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }
    .badge {
      display: inline-block;
      background: rgba(255,255,255,0.18);
      border: 1px solid rgba(255,255,255,0.35);
      border-radius: 20px;
      padding: 5px 16px;
      font-size: 0.82rem;
    }
    .container { max-width: 960px; margin: 0 auto; padding: 30px 16px; }
    .notice {
      background: #FFF8E1;
      border-left: 5px solid #FFC107;
      border-radius: 8px;
      padding: 16px 20px;
      margin-bottom: 24px;
    }
    .notice h3 { color: #E65100; margin-bottom: 6px; font-size: 0.95rem; }
    .notice p { font-size: 0.88rem; color: #555; }
    .notice code { background: #FFE082; padding: 2px 6px; border-radius: 4px; font-family: monospace; }
    .card {
      background: white;
      border-radius: 14px;
      padding: 24px;
      margin-bottom: 20px;
      box-shadow: 0 2px 12px rgba(0,0,0,0.07);
    }
    .card h2 {
      font-size: 1.05rem;
      color: #1A237E;
      margin-bottom: 16px;
      padding-bottom: 10px;
      border-bottom: 2px solid #E8EAF6;
    }
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; }
    .feat {
      background: linear-gradient(135deg, #E8EAF6, #F5F5F5);
      border-radius: 10px;
      padding: 14px;
      font-size: 0.88rem;
    }
    .feat strong { display: block; color: #1A237E; margin-bottom: 4px; font-size: 0.9rem; }
    .pill {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 12px;
      font-size: 0.78rem;
      font-weight: 600;
      margin-bottom: 6px;
    }
    .done { background: #C8E6C9; color: #1B5E20; }
    .list { list-style: none; }
    .list li {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      padding: 7px 0;
      border-bottom: 1px solid #F0F0F0;
      font-size: 0.9rem;
    }
    .list li:last-child { border-bottom: none; }
    .dot { width: 8px; height: 8px; border-radius: 50%; background: #1A237E; flex-shrink: 0; margin-top: 5px; }
    .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    @media (max-width: 600px) { .two-col { grid-template-columns: 1fr; } }
    .admin-box {
      background: #F3E5F5;
      border-radius: 10px;
      padding: 14px 18px;
      font-size: 0.88rem;
    }
    .admin-box h4 { color: #6A1B9A; margin-bottom: 8px; }
    .admin-box code { background: #CE93D8; padding: 2px 6px; border-radius: 4px; font-family: monospace; color: #4A148C; }
    .asset-preview {
      display: flex;
      gap: 24px;
      align-items: center;
      flex-wrap: wrap;
    }
    .asset-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      font-size: 0.8rem;
      color: #666;
    }
    .icon-48 { width: 48px; height: 48px; border-radius: 10px; object-fit: cover; }
    .icon-72 { width: 72px; height: 72px; border-radius: 14px; object-fit: cover; }
    .icon-96 { width: 96px; height: 96px; border-radius: 20px; object-fit: cover; }
    .icon-round { border-radius: 50% !important; }
    footer {
      text-align: center;
      padding: 24px;
      font-size: 0.82rem;
      color: #999;
    }
  </style>
</head>
<body>
  <div class="header">
    <img class="logo" src="/logo.png" alt="Altomedia Logo" onerror="this.style.display='none'" />
    <h1>ALTOINDO</h1>
    <p class="sub">MLM Financial Ecosystem &mdash; by Altomedia Management</p>
    <div class="badges">
      <span class="badge">Android Native &middot; Java</span>
      <span class="badge">Firebase Firestore &amp; Auth</span>
      <span class="badge">ViewBinding &middot; RecyclerView</span>
      <span class="badge">QRIS / QR Code</span>
    </div>
  </div>

  <div class="container">
    <div class="notice">
      <h3>&#x26A0; Lingkungan Replit</h3>
      <p>Aplikasi ini adalah <strong>Android native (Java)</strong> yang tidak bisa dijalankan langsung di browser. Untuk build APK, gunakan Android Studio dengan perintah <code>./gradlew assembleDebug</code>. Halaman ini adalah project overview.</p>
    </div>

    <div class="card">
      <h2>&#x1F3A8; Asset Icon &amp; Logo</h2>
      <div class="asset-preview">
        <div class="asset-item">
          <img class="icon-48" src="/logo.png" alt="mdpi" />
          <span>mdpi (48px)</span>
        </div>
        <div class="asset-item">
          <img class="icon-72" src="/logo.png" alt="hdpi" />
          <span>hdpi (72px)</span>
        </div>
        <div class="asset-item">
          <img class="icon-96" src="/logo.png" alt="xhdpi" />
          <span>xhdpi (96px)</span>
        </div>
        <div class="asset-item">
          <img class="icon-96 icon-round" src="/logo.png" alt="round" />
          <span>Round icon</span>
        </div>
      </div>
      <p style="margin-top:14px; font-size:0.85rem; color:#666;">
        Icon ditempatkan di: <code>mipmap-mdpi</code>, <code>mipmap-hdpi</code>, <code>mipmap-xhdpi</code>, <code>mipmap-xxhdpi</code>, <code>mipmap-xxxhdpi</code> &bull;
        Logo full-res di: <code>drawable/ic_logo.png</code> (512px) &bull;
        Digunakan di: Launcher icon, Splash screen, Login screen
      </p>
    </div>

    <div class="card">
      <h2>&#x2705; Fitur Member</h2>
      <div class="grid">
        <div class="feat"><span class="pill done">SELESAI</span><strong>Login &amp; Register</strong>Login email/password + Member ID, routing ke Admin</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Dashboard Utama</strong>8 tombol navigasi: Transfer, Topup, Tarik, Produk, Profil, Riwayat, Notifikasi, Setelan</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Transfer Saldo</strong>Verifikasi PIN, scan QR member, simpan ke Firestore, potong saldo real-time</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Top Up QRIS</strong>Generate QR QRIS dengan timer 3 menit, simpan ke galeri, catat ke Firestore</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Penarikan Dana</strong>Validasi saldo, auto-isi rekening dari profil, potong saldo otomatis</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Profil &amp; QR Code</strong>Tampilan profil lengkap + QR Code member untuk di-scan</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Edit Profil</strong>Update nama, telepon, rekening bank, tipe akun</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Riwayat Transaksi</strong>RecyclerView semua transaksi user berurutan</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Notifikasi</strong>RecyclerView notifikasi publik dari Firestore</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Setelan</strong>Info kontak, email, URL sosial &amp; fitur</div>
      </div>
    </div>

    <div class="card">
      <h2>&#x1F6E1; Fitur Admin</h2>
      <div class="grid">
        <div class="feat"><span class="pill done">SELESAI</span><strong>Admin Dashboard</strong>6 menu kelola + tombol logout aman</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Manajemen Bonus</strong>Set persentase bonus + distribusi massal ke upline</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Manajemen Pengguna</strong>AdminUserManagementActivity</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Manajemen Transaksi</strong>AdminTransactionManagementActivity</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Manajemen Notifikasi</strong>AdminNotificationManagementActivity</div>
        <div class="feat"><span class="pill done">SELESAI</span><strong>Manajemen Produk</strong>AdminProductManagementActivity</div>
      </div>
    </div>

    <div class="two-col">
      <div class="card">
        <h2>&#x1F4E6; Stack Teknologi</h2>
        <ul class="list">
          <li><span class="dot"></span>Android Native Java &mdash; minSdk 24, targetSdk 34</li>
          <li><span class="dot"></span>Firebase Auth + Firestore + Storage</li>
          <li><span class="dot"></span>ViewBinding (semua Activity)</li>
          <li><span class="dot"></span>RecyclerView + Adapter pattern</li>
          <li><span class="dot"></span>ZXing QR Scanner + QRGen library</li>
          <li><span class="dot"></span>Glide 4.16 (image loading)</li>
          <li><span class="dot"></span>Gradle AGP 8.2.0 + JitPack</li>
        </ul>
      </div>
      <div class="card">
        <h2>&#x1F5C2; Koleksi Firestore</h2>
        <ul class="list">
          <li><span class="dot"></span><code>users</code> &mdash; profil, saldo, rekening</li>
          <li><span class="dot"></span><code>transactions</code> &mdash; semua riwayat transaksi</li>
          <li><span class="dot"></span><code>topups</code> &mdash; permintaan top-up QRIS</li>
          <li><span class="dot"></span><code>withdraw_requests</code> &mdash; permintaan tarik</li>
          <li><span class="dot"></span><code>notifications</code> &mdash; notifikasi publik</li>
          <li><span class="dot"></span><code>bonus_config</code> &mdash; konfigurasi bonus MLM</li>
          <li><span class="dot"></span><code>app_settings</code> &mdash; setelan aplikasi</li>
        </ul>
      </div>
    </div>

    <div class="card">
      <h2>&#x1F511; Kredensial Admin (Dev)</h2>
      <div class="admin-box">
        <h4>Login Admin</h4>
        <p>Email: <code>appsidhanie@gmail.com</code><br/>
        Password: <code>Kdsmedia@123</code><br/>
        Member ID: <code>14061993</code></p>
      </div>
    </div>
  </div>
  <footer>ALTOINDO &copy; 2024 Altomedia Management &mdash; Semua hak dilindungi</footer>
</body>
</html>`;

const server = http.createServer((req, res) => {
  if (req.url === '/logo.png') {
    const logoPath = path.join(__dirname, 'public', 'logo.png');
    try {
      const data = fs.readFileSync(logoPath);
      res.writeHead(200, { 'Content-Type': 'image/png' });
      res.end(data);
    } catch {
      res.writeHead(404);
      res.end();
    }
    return;
  }
  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(html);
});

server.listen(5000, '0.0.0.0', () => {
  console.log('ALTOINDO project overview running on port 5000');
});
