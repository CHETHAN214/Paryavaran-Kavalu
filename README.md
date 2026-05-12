# Paryavaran-Kavalu 🌿
### Android Kotlin App — Environment Guardian

A full Android Kotlin replica of the Paryavaran-Kavalu web app with **real Google Maps** integration.

---

## 📱 Features
| Feature | Status |
|---|---|
| Splash screen with leaf pulse animation | ✅ |
| Top bar with live Eco-Karma counter | ✅ |
| Tab navigation (Home / Report / Map / Karma) | ✅ |
| Dashboard with hero card, stats grid, recent reports | ✅ |
| Report form: photo capture/gallery, waste type chips, severity, GPS, description | ✅ |
| Real Google Maps with colour-coded markers (🔴 Pending / 🟢 Cleaned) | ✅ |
| My Location button on map | ✅ |
| Green custom map style | ✅ |
| Report detail bottom sheet | ✅ |
| Mark as Cleaned (+5 Eco-Karma) | ✅ |
| Leaderboard with rank cards & level progress bar | ✅ |
| Badges grid (locked/earned) | ✅ |
| Persistent storage (SharedPreferences + Gson) | ✅ |
| Success overlay after submission | ✅ |
| Toast notifications | ✅ |

---

## 🚀 Setup Instructions

### 1. Get a Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select existing)
3. Enable **Maps SDK for Android**
4. Go to **Credentials → Create Credentials → API Key**
5. Restrict the key to **Android apps** and add your package name: `com.paryavaran.kavalu`

### 2. Add the API Key

Open `app/build.gradle` and replace:
```groovy
manifestPlaceholders = [mapsApiKey: "YOUR_GOOGLE_MAPS_API_KEY"]
```
with your actual key:
```groovy
manifestPlaceholders = [mapsApiKey: "AIzaSy...your_key_here"]
```

> **Security tip:** For production, store the key in `local.properties` and read it via `buildConfigField`.

### 3. Open in Android Studio

1. Open **Android Studio** (Hedgehog or later recommended)
2. **File → Open** → select the `ParyavaranKavalu` folder
3. Wait for Gradle sync to complete
4. Connect a device or start an emulator (API 26+)
5. Click ▶ Run

---

## 📁 Project Structure

```
app/src/main/
├── java/com/paryavaran/kavalu/
│   ├── SplashActivity.kt           ← Splash screen
│   ├── MainActivity.kt             ← Tab navigation host
│   ├── data/
│   │   ├── WasteReport.kt          ← Data models + Levels
│   │   ├── AppRepository.kt        ← SharedPreferences storage
│   │   └── AppViewModel.kt         ← Shared ViewModel
│   └── ui/
│       ├── dashboard/
│       │   ├── DashboardFragment.kt
│       │   └── RecentReportsAdapter.kt
│       ├── report/
│       │   ├── ReportFragment.kt   ← Camera, GPS, form
│       │   ├── ReportDetailBottomSheet.kt
│       │   └── SuccessOverlayDialog.kt
│       ├── map/
│       │   ├── MapFragment.kt      ← Real Google Maps
│       │   └── MapReportListAdapter.kt
│       └── leaderboard/
│           ├── LeaderboardFragment.kt
│           ├── LeaderboardAdapter.kt
│           └── BadgesAdapter.kt
├── res/
│   ├── layout/         ← All XML layouts
│   ├── drawable/       ← Shapes, selectors, gradients, icons
│   ├── values/         ← colors.xml, strings.xml, themes.xml
│   ├── raw/            ← map_style_green.json
│   ├── anim/           ← leaf_pulse.xml
│   └── xml/            ← file_paths.xml (FileProvider)
└── AndroidManifest.xml
```

---

## 🗺️ Google Maps Details

- **Real SupportMapFragment** embedded inside a rounded container
- **Green custom map style** loaded from `res/raw/map_style_green.json`
- **Red markers** for pending reports, **green markers** for cleaned
- **My Location layer** enabled when permission granted
- **Auto-zoom** to fit all markers on the map when reports exist
- Tapping a marker opens the **Report Detail bottom sheet**
- **My Location FAB** centers the camera on the user

---

## 🎨 Color Palette

| Name | Hex |
|---|---|
| Green Deep | `#0f4d2a` |
| Green Main | `#1a7a4a` |
| Green Light | `#2ecc71` |
| Green Pale | `#d4f5e2` |
| Amber | `#f39c12` |
| Red | `#e74c3c` |

---

## 📦 Dependencies

```groovy
// Maps
com.google.android.gms:play-services-maps:18.2.0
com.google.android.gms:play-services-location:21.1.0

// Image Loading
com.github.bumptech.glide:glide:4.16.0

// JSON
com.google.code.gson:gson:2.10.1

// UI
com.google.android.material:material:1.11.0
androidx.recyclerview:recyclerview:1.3.2
```

---

## ⚙️ Permissions Used

- `INTERNET` — Google Maps tiles
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — GPS capture
- `CAMERA` — Photo evidence capture
- `READ_MEDIA_IMAGES` — Gallery photo selection

---

*Built with ❤️ for Swachh Bharat 2.0*
