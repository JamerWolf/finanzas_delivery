# Agent Guidelines: Finanzas Delivery (Android)

You are an agentic coding assistant working on **Finanzas Delivery**, a financial and GPS tracking app for delivery drivers (DIDI, Rappi, Cabify, etc.). The app is built using modern Android standards with a strictly enforced Clean Architecture.

---

## 1. Project Overview
- **Platform:** Android (Min SDK 26, Target SDK 36)
- **Primary Language:** Kotlin (with Java for Room Database entities)
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** Clean Architecture (Domain, Data, UI) + MVVM
- **Dependency Injection:** Hilt
- **Persistence:** Room Database
- **Networking:** Retrofit + OkHttp (communicating with OSRM)
- **Maps:** MapLibre Android SDK

---

## 2. Build & Test Commands

Use `gradlew.bat` for Windows environments.

| Task | Command |
| :--- | :--- |
| **Build Debug APK** | `.\gradlew.bat assembleDebug` |
| **Full Build & Lint** | `.\gradlew.bat build` |
| **Clean Project** | `.\gradlew.bat clean` |
| **Run All Unit Tests** | `.\gradlew.bat test` |
| **Run Single Test Class** | `.\gradlew.bat :app:testDebugUnitTest --tests "com.control_delivery.finanzas_delivery.domain.usecases.ApplyTimeBasedDeductionUseCaseTest"` |
| **Run Single Test Method** | `.\gradlew.bat :app:testDebugUnitTest --tests "*.testMethodName"` |
| **Check Linting** | `.\gradlew.bat lint` |

---

## 3. Architecture Guidelines

### Layers & Dependency Rule
1. **Domain Layer:** Business logic. Contains `model`, `repository` (interfaces), and `usecases`. **Strictly NO dependencies** on Android or other layers.
2. **Data Layer:** Infrastructure. Contains `repository` (implementations), `local` (Room), and `routing` (OSRM). Depends only on the Domain layer.
3. **UI Layer:** Presentation. Contains `screens`, `viewmodels`, `components`, and `theme`. ViewModels depend on Use Cases.

### Special Constraint: Room Database
**CRITICAL:** Due to KSP/Kotlin 2.x incompatibilities with complex types (Maps, Lists) in Room, all **Database Entities and DAOs must be written in Java**.
- Location: `com.control_delivery.finanzas_delivery.db.java`
- Do NOT convert these to Kotlin.
- Use `Gson` for manual serialization within Kotlin Repositories to convert Domain Models to/from Java Entities.
- **Transactions:** Use `@Transaction` in DAOs for any query that returns a `TripWithOrders` relation to ensure data consistency.

### Hilt Injection
- All Repositories and UseCases must be provided in `AppModule.kt`.
- Use `@Inject constructor` for ViewModels and Repositories.
- Use `@ApplicationContext` when providing the Database or LocationTracker.

---

## 4. Code Style & Conventions

### Naming Conventions
- **UseCases:** Must end in `UseCase` (e.g., `CompleteTripUseCase`). Must implement `suspend operator fun invoke(...)`.
- **ViewModels:** Must end in `ViewModel`. Must expose a single `uiState` property (Compose `State` or `MutableState`).
- **UI State:** Data classes ending in `UiState` (e.g., `TripDetailUiState`).
- **Repositories:** Interfaces in Domain, implementations in Data prefixed with `Room` (e.g., `RoomTripRepository`).

### Formatting & Imports
- **Imports:** Avoid wildcard imports (`.*`). Organize imports with standard Kotlin order.
- **Compose:** Prefer the "Stateless/Stateful" pattern.
    - `ScreenName(...)`: Stateful (handles ViewModel injection and navigation).
    - `ScreenNameContent(...)`: Stateless (takes raw data and callbacks).
- **Theming:** Use `MaterialTheme.colorScheme`. Hardcoding hex colors in UI components is strictly forbidden.
- **Modifiers:** Always provide a `modifier: Modifier = Modifier` parameter to custom Composables and apply it to the root element.

### Data Types & Units
- **Money/Currency:** Always use `Long` to avoid floating-point errors (e.g., `4700` represents `$4.700`).
- **Distance:** Use `Double` representing Kilometers.
- **Location:** Use the `RoutePoint` domain model (Lat/Lng).
- **Time:** Use `Long` (milliseconds since epoch).

---

## 5. Error Handling & Logging

- **Logging:** Use `Timber` for all logging.
    - `Timber.d("Message")` for debugging.
    - `Timber.e(exception, "Error message")` for errors.
- **Defensive Programming:**
    - Always wrap Network (OSRM) and Database (Room) calls in `try-catch` blocks within Repositories.
    - Ensure all Database access happens on `Dispatchers.IO` using `withContext(Dispatchers.IO)`.
- **Domain Invariants:** Use `require()` or `check()` in model `init` blocks to enforce business rules (e.g., amounts cannot be negative).
- **Graceful Fallbacks:** If the OSRM snapping API fails, the application MUST fall back to drawing the raw GPS trace (`Trip.route`) instead of showing an empty map.

---

## 6. GPS & Mapping Logic

### OSRM Routing
- We use the public OSRM server: `https://router.project-osrm.org/`.
- **User-Agent:** Every request must include a custom User-Agent header (configured in `AppModule`).
- **Batching:** When snapping routes, chunk coordinates into batches of 90 points max to stay within URL length limits.
- **Double Fallback:** The repository should try the `Match API` first, and if it returns an error, attempt the `Route API` before giving up.

### Map Rendering
- We use MapLibre. Map styles (Light/Dark) must switch dynamically based on `isSystemInDarkTheme()`.
- **Camera:** Always respect the "Bottom Sheet padding" when zooming to fit the route. The map camera must be offset upwards so markers are not hidden by the UI panel.
- **Legend:** The map legend should be toggleable and visible by default, positioned in the top-left corner.

---

## 7. Workflow Expectations
- **Proactiveness:** If you modify a DAO or Entity, you MUST verify the build immediately as Room generation is sensitive to changes.
- **Verification:** After significant logic changes, run the relevant unit tests in `app/src/test`.
- **Sync Logic:** When a trip or order is deleted/updated, you MUST call `ReverseTripIncomeUseCase` to subtract the old values from the expense repositories before applying new ones.
- **Spanish Communication:** While code/comments are in English, always communicate with the user in Spanish as per project preference.
- **Soft Delete:** Never hard-delete data. Mark records as `isDeleted = true` and filter them out in the DAOs or Repositories.
