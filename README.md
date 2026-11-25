# StreamFlow 🎬

<p align="center">
  <img src="docs/banner.png" alt="StreamFlow Banner" width="600"/>
</p>

<p align="center">
  <a href="https://github.com/YOUR_USERNAME/StreamFlow/releases"><img src="https://img.shields.io/github/v/release/YOUR_USERNAME/StreamFlow?style=flat-square" /></a>
  <img src="https://img.shields.io/badge/Android-10%2B-green?style=flat-square" />
  <img src="https://img.shields.io/badge/Kotlin-2.0-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/License-GPL--3.0-orange?style=flat-square" />
  <img src="https://img.shields.io/badge/Arabic-✓-brightgreen?style=flat-square" />
</p>

> **StreamFlow** is a privacy-first, feature-rich YouTube client for Android that merges the best of [LibreTube](https://github.com/libre-tube/LibreTube) and [Flow](https://github.com/Flow-Tube/Flow) — with all their known bugs fixed and full Arabic (RTL) support.

---

## ✨ Features

| Feature | Status |
|---|---|
| 🔒 Privacy — no Google account required | ✅ |
| 📺 Video streaming via Piped API | ✅ |
| 🎵 Background playback | ✅ |
| 📱 Picture-in-Picture (PiP) | ✅ |
| ⏭ SponsorBlock (auto-skip sponsors/intros/outros) | ✅ |
| 👎 Return YouTube Dislike counter | ✅ |
| 🏷 DeArrow (community titles & thumbnails) | ✅ |
| 📥 Video downloads | ✅ |
| 📋 Local playlists | ✅ |
| 🔔 Subscriptions (local + Piped account) | ✅ |
| 🗂 Watch history | ✅ |
| 🔍 Search with autocomplete & filters | ✅ |
| 💬 Comments | ✅ |
| 📑 Video chapters | ✅ |
| 🌙 Material You dynamic theming | ✅ |
| 🌍 Arabic / English (RTL support) | ✅ |
| 📲 Share YouTube links into app | ✅ |
| 🔁 Multi-instance Piped support | ✅ |

---

## 🏗 Architecture

```
StreamFlow/
├── data/
│   ├── api/          Retrofit services (Piped, SponsorBlock, RYD, DeArrow)
│   ├── model/        Kotlin data classes
│   ├── local/        Room DB (history, subscriptions, playlists, downloads)
│   ├── network/      OkHttp DownloaderImpl for NewPipeExtractor
│   └── repository/   Single source of truth — VideoRepository
├── domain/           Use-cases (future)
├── di/               Hilt dependency injection modules
├── service/          PlayerService (Media3), DownloadService, BootReceiver
├── ui/
│   ├── screens/      Jetpack Compose screens
│   ├── components/   Reusable UI components
│   ├── viewmodel/    MVVM ViewModels
│   ├── navigation/   Type-safe NavGraph
│   └── theme/        Material 3 theme + Arabic typography
└── utils/            Extensions, PreferencesManager
```

**Tech stack:** Kotlin 2.0 · Jetpack Compose · Material 3 · Media3/ExoPlayer · Hilt · Retrofit · Room · DataStore · Coil · NewPipeExtractor

---

## 🐛 Known Issues Fixed (from LibreTube & Flow)

### From LibreTube
| Issue | Fix Applied |
|---|---|
| `ForegroundServiceStartNotAllowedException` on Android 14+ | Declared `foregroundServiceType="mediaPlayback"` in Manifest |
| `Session ID must be unique` MediaSession crash | MediaSession created once in `onCreate()`, never recreated during autoplay |
| App freeze when Piped instance is down | User-switchable instance URL in Settings + friendly error messages |
| Crashes on HTTP 403/429 (rate-limit) | `DownloaderImpl` throws `ReCaptchaException`; UI shows actionable error |
| Stream URL expiry causing playback failure | `PlayerViewModel` catches `PlaybackException`, re-fetches stream from last position |
| No battery optimization exemption guidance | Documented in TROUBLESHOOTING.md |

### From Flow
| Issue | Fix Applied |
|---|---|
| AI-generated code crashes without error handling | Every repository call uses `runCatching` → sealed Result |
| OkHttpClient socket exhaustion | Singleton `OkHttpClient` via Hilt `@Singleton` |
| `NetworkOnMainThreadException` | All extraction on `Dispatchers.IO` |
| Compose + AndroidView player conflicts | `AndroidView` wraps native `PlayerView`; controls in separate Compose layer |
| ProGuard stripping NewPipeExtractor | Explicit `-keep` rules in `proguard-rules.pro` |

### Common AI-generated project failures
| Problem | Solution |
|---|---|
| Hardcoded URLs break on instance changes | All URLs in DataStore, editable from Settings |
| Duplicate class build failures (NewPipe deps) | `packaging { resources { excludes += ... } }` in app/build.gradle.kts |
| Java 8+ API unavailable on Android 10 | `coreLibraryDesugaringEnabled = true` |
| ABI mismatch (arm64 only) | All 4 ABIs listed in `ndk { abiFilters }` + ABI splits |
| Blocking I/O on main thread | Coroutine `Dispatchers.IO` enforced in all repository methods |

---

## 🚀 Building

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 35

```bash
git clone https://github.com/YOUR_USERNAME/StreamFlow.git
cd StreamFlow
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Release build
```bash
./gradlew assembleRelease
```

---

## ⚙️ Configuration

### Switching Piped Instance
If the default instance (`pipedapi.kavin.rocks`) is down or slow:
1. Open **Settings → Network → Piped Instance URL**
2. Enter a healthy instance from [piped-instances.kavin.rocks](https://piped-instances.kavin.rocks/)
3. Tap **Save**

### Troubleshooting Playback
| Symptom | Fix |
|---|---|
| "YouTube is rate-limiting" | Switch Piped instance |
| Endless loading | Toggle **Disable Piped Proxy** in Settings |
| Background play stops | Go to Android Settings → Apps → StreamFlow → Battery → **Unrestricted** |
| PiP not working | Grant "Display over other apps" permission |

---

## 🌍 Localization

Currently supported languages:
- 🇸🇦 Arabic (`ar`) — full RTL support
- 🇬🇧 English (`en`) — default

To add a new language, copy `res/values/strings.xml` to `res/values-XX/strings.xml` and translate.

---

## 📄 License

StreamFlow is released under the **GNU General Public License v3.0**.  
See [LICENSE](LICENSE) for details.

---

## 🙏 Credits

- [LibreTube](https://github.com/libre-tube/LibreTube) — Piped-based architecture inspiration
- [Flow](https://github.com/Flow-Tube/Flow) — Feature set and UX patterns
- [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor) — Stream extraction
- [SponsorBlock](https://sponsor.ajay.app/) — Segment skipping
- [Return YouTube Dislike](https://returnyoutubedislike.com/) — Dislike counts
- [DeArrow](https://dearrow.ajay.app/) — Community titles/thumbnails
