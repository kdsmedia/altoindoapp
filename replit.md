# ALTOINDO - Android Native Application

## Project Overview
ALTOINDO adalah aplikasi Android native (Java) ekosistem keuangan MLM yang dikembangkan oleh Altomedia Management (com.altomedia.altoindo). Mengelola transaksi finansial (transfer, top-up QRIS, penarikan dana) antar member dengan sistem bonus afiliasi.

## Technology Stack
- **Platform**: Android Native (minSdk 24, targetSdk 34)
- **Language**: Java
- **Backend/Database**: Firebase Authentication, Firestore, Storage
- **Build System**: Gradle AGP 8.2.0 + ViewBinding + Java 8 compile options
- **UI**: Android XML Layouts + RecyclerView + CardView (3D Modern Dark Theme)
- **Libraries**: ZXing (QR scan), QRGen/JitPack (QR generate), Glide 4.16 (images), Firebase BOM 32.7.0, AdMob (play-services-ads)
- **Repo Management**: JitPack added to settings.gradle

## AdMob Configuration
- **App ID**: ca-app-pub-6881903056221433~7084995742
- **Interstitial**: ca-app-pub-6881903056221433/5741161893 (tampil saat buka app di MainActivity)
- **Reward Video**: ca-app-pub-6881903056221433/2876283260 (tombol "Tonton" di BonusActivity, bonus Rp.10-Rp.100 random)
- **Banner**: ca-app-pub-6881903056221433/3584734431 (ProfileActivity, BonusActivity, ProductsActivity)
- **Native**: ca-app-pub-6881903056221433/3989096525 (SplashActivity saat app dibuka)

## Project Structure
```
app/src/main/java/com/altomedia/altoindoapp/
  activities/   - Semua Activity (24 class)
  adapters/     - RecyclerView adapters (TransactionAdapter, NotificationAdapter)
  models/       - Data models (User, Transaction, NotificationMessage, BonusConfig, AppSetting, WithdrawRequest, TopupRequest, Product)
  utils/        - Helper classes (IDGenerator, FirebaseHelper)
app/src/main/res/
  layout/       - 25+ XML layout files
  drawable/     - gradient_header.xml, btn_action_card.xml, btn_action_card2.xml, gradient_card.xml, gradient_green.xml, btn_primary_3d.xml
  values/       - strings.xml (includes account_types array)
app/src/main/AndroidManifest.xml  - SplashActivity set as launcher
server.js       - Node.js overview server on port 5000
```

## Key Activities (COMPLETED)
### Member Flow
- `SplashActivity` - Splash screen + Native Ad saat app dibuka (3 detik)
- `LoginActivity` - Firebase Auth login + Member ID login + Admin routing
- `RegisterActivity` - New member registration
- `MainActivity` - Dashboard utama + Interstitial Ad + Bottom Navigation (Home)
- `BonusActivity` - Pusat bonus + Reward Video Ad (Tonton = Rp.10-Rp.100) + Banner Ad + Bottom Nav (Bonus)
- `ProductsActivity` - Katalog produk + Dropship toggle + Banner Ad + Bottom Nav (Produk)
- `SaldoActivity` - Halaman saldo wallet + aksi cepat + Bottom Nav (Saldo)
- `ProfileActivity` - Profil + QR Code + Profile warning + Banner Ad + Bottom Nav (Profil)
- `TransferActivity` - Transfer saldo (PIN verif, QR scan, auto-deduct)
- `TopUpActivity` - Top-up via QRIS dengan timer 3 menit + save ke galeri
- `WithdrawActivity` - Penarikan dana minimum Rp.100.000 + auto-isi rekening + profile check
- `UserProfileActivity` - Edit profil lengkap: nama, telepon, alamat pengiriman, nama dropship, rekening bank
- `TransactionHistoryActivity` - RecyclerView riwayat transaksi user
- `NotificationsActivity` - RecyclerView notifikasi publik
- `SettingsActivity` - Setelan kontak & URL
- `ScannerActivity` - QR Code scanner

### Admin Flow
- `AdminDashboardActivity` - Panel admin
- `AdminUserManagementActivity` - Kelola pengguna
- `AdminTransactionManagementActivity` - Kelola transaksi & penarikan
- `AdminNotificationManagementActivity` - Kelola notifikasi publik
- `AdminProductManagementActivity` - Kelola produk
- `AdminSettingsActivity` - Kelola setelan aplikasi
- `BonusManagementActivity` - Konfigurasi bonus + distribusi massal afiliasi

## Bottom Navigation (5 Menu)
Urutan: 🏠 Home → 🎁 Bonus → 🛒 Produk → 💰 Saldo → 👤 Profil
File: `app/src/main/res/layout/layout_bottom_nav.xml`

## User Model Fields (Firestore)
```
users/{uid}:
  uid, member_id, upline_id, full_name, email, phone
  address          - BARU: alamat lengkap untuk pengiriman
  dropship_name    - BARU: nama pengirim dropship (opsional)
  account_type, account_name, account_owner, account_number
  role, tier, security_pin, is_active
  balance_wallet, points_personal, points_group
```

## Business Rules
- **Profil wajib lengkap**: full_name + phone + address + account_name + account_owner + account_number wajib diisi untuk transaksi
- **Penarikan minimum**: Rp.100.000
- **Dropship**: user dapat input nama pengirim berbeda saat pembelian produk
- **Bonus video**: Rp.10 - Rp.100 random dikreditkan setelah menonton reward video

## Permissions (AndroidManifest)
INTERNET, CAMERA, POST_NOTIFICATIONS, READ_CONTACTS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE

## Firestore Collections
- `users` - profil member
- `transactions` - riwayat transaksi
- `topups` - permintaan top-up QRIS
- `withdraw_requests` - permintaan penarikan dana
- `notifications` - notifikasi publik admin
- `bonus_config` - konfigurasi persentase bonus MLM
- `app_settings` - setelan aplikasi
- `products` - produk MLM
- `id_card_requests` - permintaan cetak ID Card fisik (uid, memberId, fullName, address, status, createdAt, amount=50000)

## Admin Credentials
- Email: appsidhanie@gmail.com
- Password: Kdsmedia@123
- Member ID: 14061993

## Running in Replit
Aplikasi Android tidak bisa dijalankan langsung di Replit. Node.js server (`server.js`) menampilkan project overview di port 5000.

Untuk build APK: `./gradlew assembleDebug` (memerlukan Android SDK)

## Workflow
- **Start application**: `node server.js` — port 5000
