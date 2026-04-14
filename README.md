# 🚀 ALTOINDO - Official Android Native Application
**MLM Financial Ecosystem by Altomedia Management**

Aplikasi Android Native berbasis **Java** yang dirancang untuk stabilitas tinggi, integrasi Firebase murni, dan keamanan transaksi finansial antar member.

---

## 📂 1. Struktur Folder (Setup Awal)
Gunakan perintah `mkdir` berikut di terminal untuk membangun pondasi proyek yang sesuai dengan Package Name `com.altomedia.altoindoapp`:

```bash
# Membuat folder Java (Logic)
mkdir -p app/src/main/java/com/altomedia/altoindoapp/activities
mkdir -p app/src/main/java/com/altomedia/altoindoapp/models
mkdir -p app/src/main/java/com/altomedia/altoindoapp/utils
mkdir -p app/src/main/java/com/altomedia/altoindoapp/adapters

# Membuat folder Resources (UI)
mkdir -p app/src/main/res/layout
mkdir -p app/src/main/res/values
mkdir -p app/src/main/res/drawable
mkdir -p app/src/main/res/mipmap-hdpi





# Tugas,Perintah Terminal
Memberikan Izin Gradle,chmod +x gradlew
Membersihkan Build Lama,./gradlew clean
Download Dependencies,./gradlew build --refresh-dependencies
Generate APK (Debug),./gradlew assembleDebug
Generate AAB (Release),./gradlew bundleRelease
