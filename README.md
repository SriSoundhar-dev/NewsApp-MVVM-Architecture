# NewsApp — MVVM Architecture

A small Android sample app that shows the **top news headlines** for a country using the
[NewsAPI](https://newsapi.org/) `top-headlines` endpoint. It is written entirely in **Kotlin**
and demonstrates a clean **MVVM + Repository** setup with Dagger 2, Retrofit, Coroutines and Flow.

> Scope today: a single screen (`TopHeadlineActivity`) that fetches US headlines on launch and
> renders them in a `RecyclerView`. There is no local database or offline cache yet — see
> [Roadmap](#roadmap).

## Features

- Fetches top headlines from NewsAPI on startup
- List of articles with banner image, title, description and source name
- Explicit `Loading` / `Success` / `Error` UI states
- Errors surfaced to the user via a `Toast`

## Tech stack

| Concern            | Choice |
|--------------------|--------|
| Language           | Kotlin |
| Async              | Coroutines + `Flow` / `StateFlow` |
| Networking         | Retrofit 2 + Gson converter |
| Dependency injection | Dagger 2 (hand-written components, no Hilt) |
| Image loading      | Glide |
| UI                 | Android Views + ViewBinding, `RecyclerView` |
| Lifecycle          | `ViewModel`, `viewModelScope`, `repeatOnLifecycle` |
| Min / target SDK   | 24 / 34 |

## Architecture

```
TopHeadlineActivity
      │  collects StateFlow<UiState<List<Article>>> with repeatOnLifecycle
      ▼
TopHeadlineViewModel  ── viewModelScope ──▶ TopHeadlineRepository
                                                   │  Flow<List<Article>>
                                                   ▼
                                            NetworkService (Retrofit)
                                                   │
                                                   ▼
                                             newsapi.org/v2
```

- **View** (`TopHeadlineActivity`) is passive: it injects its dependencies with Dagger, then
  collects `uiState` and renders each state. No business logic.
- **ViewModel** (`TopHeadlineViewModel`) calls the repository in `init`, maps results/exceptions
  into a `UiState` sealed interface, and exposes them as a `StateFlow`.
- **Repository** (`TopHeadlineRepository`) is the single source of truth for data. It wraps the
  Retrofit `NetworkService` call in a `Flow` and maps the response to `List<Article>`.
- **DI**: `ApplicationComponent` provides app-wide singletons (`Retrofit`, `NetworkService`,
  `TopHeadlineRepository`, app `Context`). `ActivityComponent` (`@ActivityScope`) depends on it
  and provides the `ViewModel` (via `ViewModelProviderFactory`) and the `RecyclerView` adapter.

## Project structure

```
app/src/main/java/com/sri/soundhar/newsapp_mvvm_architecture/
├── NewsApplication.kt              # builds the Dagger ApplicationComponent
├── data/
│   ├── api/NetworkService.kt       # Retrofit interface: GET top-headlines
│   ├── model/                      # Article, Source, TopHeadlinesResponse (Gson DTOs)
│   └── repository/TopHeadlineRepository.kt
├── di/
│   ├── component/                  # ApplicationComponent, ActivityComponent
│   ├── module/                     # ApplicationModule, ActivityModule
│   ├── qualifiers.kt               # @ApplicationContext, @ActivityContext, @BaseUrl
│   └── scopes.kt                   # @ActivityScope
├── ui/
│   ├── base/                       # UiState, ViewModelProviderFactory
│   └── topheadline/                # Activity, ViewModel, RecyclerView adapter
└── uitils/AppConstant.kt           # API key + country code
```

## Requirements

- **JDK 17 or newer** — the build uses Kotlin 1.9.x / Android Gradle Plugin 8.3. Android Studio's
  bundled JDK works. For command-line builds, point `JAVA_HOME` at a JDK 17+ install, e.g.
  ```bash
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  ```
- **Android SDK Platform 34**
- Android Studio Iguana (2023.2.1) or newer is recommended

## Getting started

1. **Clone**
   ```bash
   git clone https://github.com/SriSoundhar-dev/NewsApp-MVVM-Architecture.git
   ```
2. **Add a NewsAPI key.** Create a free key at [newsapi.org](https://newsapi.org/register) and set
   it in [`AppConstant.kt`](app/src/main/java/com/sri/soundhar/newsapp_mvvm_architecture/uitils/AppConstant.kt):
   ```kotlin
   object AppConstant {
       const val API_KEY = "YOUR_API_KEY"
       const val COUNTRY = "us"   // any ISO 3166-1 code NewsAPI supports
   }
   ```
   > The key currently checked in is a shared demo key on the free tier and may be rate-limited or
   > revoked at any time — use your own.
3. **Run** from Android Studio (Run ▶) or install a debug build from the command line:
   ```bash
   ./gradlew installDebug
   ```

## Building an APK

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Roadmap

Things the architecture is set up for but that are not implemented yet:

- Offline caching / single-source-of-truth with Room (DB first, then network refresh)
- Article detail — opening `article.url` in a Chrome Custom Tab (the click handler in
  `TopHeadlineAdapter` is stubbed out)
- Pull-to-refresh and manual retry on error
- Unit tests for the ViewModel and repository, instrumented tests for the list screen
- Migrate the hard-coded API key to `local.properties` / `BuildConfig`

## Notes

- Compose is enabled in `app/build.gradle` (`buildFeatures { compose true }`) but the UI is
  currently built with Android Views + ViewBinding; the Compose dependencies are unused.
- There is no `LICENSE` file in the repo yet.

---

<p align="center">Made with <b><a href="https://kotlinlang.org/">Kotlin</a></b></p>
