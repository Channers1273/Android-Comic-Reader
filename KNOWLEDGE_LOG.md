# Knowledge Log

This file tracks concepts introduced into this codebase (by AI assistance or otherwise)
that I need to actually learn, not just approve. It is a living document — update it
as part of normal dev work, not as a separate chore.

---

## Instructions for Claude Code (read this before writing code in this repo)

1. **When you introduce a concept, pattern, or library I haven't used before in this
   project**, add an entry to the log below with status `INTRODUCED`. This includes:
   architectural patterns (e.g. MVI, Clean Architecture layers), libraries/APIs
   (e.g. WorkManager, a new Flow operator), and non-obvious design decisions (e.g.
   "why a sealed interface instead of an enum here").
2. **Do not write the "My Understanding" section yourself.** That section is filled in
   by me, not you. If it's empty, leave it empty — don't pre-fill it "to be helpful."
3. **When I write something in "My Understanding,"** you may add a short "Accuracy Check"
   note below it — confirm what's right, flag what's missing or wrong, but don't rewrite
   my explanation for me.
4. **When asked to quiz me**, pull from entries with status `RESEARCHED` or
   `EXPLAINED_IN_OWN_WORDS`. Quiz questions must be adversarial, not confirmatory:
    - Good: "Here's a modified version of ReaderReducer.kt with a bug — what breaks and why?"
    - Good: "Why would using a StateFlow instead of a SharedFlow here cause a bug?"
    - Bad: "What does MVI stand for?"
    - Bad: anything answerable by re-reading my own "My Understanding" text back to me.
5. **Only advance a status after a quiz is actually passed**, not after I say "yeah I get it."
   If I get a quiz wrong or give a vague answer, keep the status where it is and note the
   gap under "Open Questions."
6. **Link every entry to real files/lines in this repo.** Abstract understanding of a
   pattern and understanding of *why this codebase uses it this way* are different
   things — track both.
7. Never mark something `CAN_TEACH` yourself — that one's my call.

Status progression: `INTRODUCED` → `RESEARCHED` → `EXPLAINED_IN_OWN_WORDS` → `QUIZ_PASSED` → `CAN_TEACH`

---

## Entry Template

```
## [Concept Name]
- Status: INTRODUCED (YYYY-MM-DD)
- Where it appears: [file paths / line refs]
- Why it was introduced: [1 line — the problem it solved, not a definition]
- Research links: [docs, articles I read]
- My Understanding:
  (empty until I write it)
- Accuracy Check: (Claude Code fills this in only after "My Understanding" is written)
- Quiz Log:
  - [date] Q: ... | My answer: ... | Result: pass/fail
- Open Questions:
  -
```

---

## Log

## MVI (Model-View-Intent)
- Status: INTRODUCED (2026-08-11)
- Where it appears: every `feature/*/presentation` screen follows the same 4-file shape —
  e.g. `feature/reader/presentation/ReaderState.kt`, `ReaderAction.kt`, `ReaderEvent.kt`,
  `ReaderViewModel.kt` (same pattern in `feature/library`, `feature/collection`,
  `feature/readinglists`)
- Why it was introduced: unidirectional state flow for each screen instead of
  ad hoc ViewModel state updates
- Research links:
  -
- My Understanding:
  (empty — fill in after research, in your own words, before asking to be quizzed)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
    - Why MVI over plain MVVM/UDF here specifically — what did it solve that UDF didn't?
    - What's the actual difference between "Intent" here and just calling a ViewModel function?

---

## Architecture & Patterns

## Root/Screen composable split
- Status: INTRODUCED (2026-08-11)
- Where it appears: every feature — e.g. `LibraryRoot`/`LibraryScreen` in
  `feature/library/presentation/LibraryScreen.kt`, `ReaderRoot`/`ReaderScreen` in
  `feature/reader/presentation/ReaderScreen.kt`. `*Root` calls `hiltViewModel()`, collects
  `state`, wires `ObserveAsEvents`, and passes plain data + a lambda down to the stateless
  `*Screen`.
- Why it was introduced: keeps the actual UI composable free of DI/ViewModel wiring so it can
  be used in `@Preview`s and tested without a Hilt graph.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Package-by-feature layering (core vs feature)
- Status: INTRODUCED (2026-08-11)
- Where it appears: top-level `core/` (database, domain, data, di, presentation, ui,
  navigation) vs `feature/` (library, collection, reader, readinglists, settings) — documented
  in `CLAUDE.md`.
- Why it was introduced: keeps feature code isolated while sharing cross-cutting concerns
  (DB, DI, error mapping, theme) through `core/`; package boundaries map 1:1 to future Gradle
  modules if the app ever needs a multi-module split.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Repository pattern (domain interface / data impl)
- Status: INTRODUCED (2026-08-11)
- Where it appears: interfaces in `core/domain/repository/` (`ComicRepository.kt`,
  `ReadingListRepository.kt`, `UserSettingsRepository.kt`) implemented by
  `core/data/repository/DefaultComicRepository.kt`,
  `DefaultReadingListRepository.kt`, `DataStoreUserSettingsRepository.kt`; wired together with
  `@Binds` in `core/di/RepositoryModule.kt`.
- Why it was introduced: domain layer has zero Android imports, which is what makes the
  hand-written fakes in `app/src/test/.../fakes/` possible.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Data source split within a repository (file vs local DB)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/data/source/ComicFileDataSource.kt` (interface) /
  `SafComicFileDataSource.kt` (impl, talks to SAF+zip) vs `ComicLocalDataSource.kt` /
  `RoomComicDataSource.kt` (impl, talks to Room); both injected into
  `DefaultComicRepository.kt` and bound in `RepositoryModule.kt`.
- Why it was introduced: separates "where files physically live" from "what's persisted about
  them," so `DefaultComicRepository.syncLibrary()` can diff scanned files against DB rows.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Use case / interactor pattern
- Status: INTRODUCED (2026-08-11)
- Where it appears: `feature/library/domain/usecase/GetLibraryContentsUseCase.kt`,
  `feature/reader/domain/usecase/GetNextComicInListUseCase.kt` — both a single class with an
  `operator fun invoke(...)`, injected into their ViewModels.
- Why it was introduced: pulls multi-repository orchestration/grouping logic (e.g. partitioning
  comics into folders vs root-level, per `CLAUDE.md`) out of the ViewModel and out of the
  repository itself.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Custom `Result<D, E>` wrapper + `Error` sealed type
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/domain/error/Result.kt` (`Result.Success`/`Result.Error`, `map`,
  `onSuccess`, `onFailure`, `asEmptyResult`/`EmptyResult`), `core/domain/error/Error.kt`
  (marker interface), `core/domain/error/DataError.kt` (`DataError.Local` enum: NOT_FOUND,
  DISK_FULL, PERMISSION_DENIED, CORRUPTED_ARCHIVE, UNKNOWN). Used throughout
  `core/data/source/` and `core/data/repository/`.
- Why it was introduced: typed, exhaustive-`when`-able success/failure instead of exceptions
  or nullable returns, for operations with well-known local-only failure modes.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - Why a generic `Result<D, E : Error>` instead of Kotlin's built-in `kotlin.Result`?

## UiText pattern
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/presentation/UiText.kt` (`DynamicString` vs `StringResource`, with a
  `@Composable asString()` and a plain `asString(context)`); produced by
  `core/presentation/mapper/DataErrorToUiText.kt` and consumed in e.g.
  `ReaderViewModel.kt`/`ReaderState.kt`/`ReaderScreen.kt`.
- Why it was introduced: lets a ViewModel (no Android `Context`/resources access by
  convention) describe an error as data, deferring actual string resolution to the
  `@Composable` layer.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Entity ↔ domain mapper functions
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/data/mapper/ComicMappers.kt` (`ComicEntity.toComic()` /
  `Comic.toComicEntity()`), `core/data/mapper/ReadingListMappers.kt`.
- Why it was introduced: keeps Room `@Entity` classes out of the domain/presentation layers —
  domain models (`core/domain/model/`) never reference database annotations.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Derived data over stored state (collections)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `feature/library/domain/usecase/GetLibraryContentsUseCase.kt` partitions
  the flat `Comic` list by `folderPath` at query time; there is no `CollectionEntity` table
  (see `CLAUDE.md` "Data model notes").
- Why it was introduced: avoids keeping a second table in sync with filesystem scans — a
  comic's `folderPath` is the only persisted signal, folders are computed, not stored.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

---

## Dependency Injection (Hilt)

## Hilt setup (`@HiltAndroidApp` / `@AndroidEntryPoint` / `@HiltViewModel`)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `ComicBookReaderApp.kt` (`@HiltAndroidApp`), `MainActivity.kt`
  (`@AndroidEntryPoint`), every `*ViewModel.kt` (`@HiltViewModel` + `@Inject constructor`),
  `hiltViewModel()` calls in every `*Root` composable (e.g. `LibraryScreen.kt` line ~41).
- Why it was introduced: constructor-injects repositories/use cases into ViewModels without
  manual factories.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## `@Provides` vs `@Binds` Hilt modules
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/di/DatabaseModule.kt` — `object` + `@Provides` (builds a
  `Room.databaseBuilder(...)`, can't be constructor-injected directly) vs
  `core/di/RepositoryModule.kt` — `abstract class` + `@Binds` (interface → impl, one-liners).
- Why it was introduced: `@Provides` is used where a concrete instance must be *constructed*
  (Room DB, DAOs pulled off it); `@Binds` is used where an existing `@Inject`-constructed impl
  just needs to satisfy an interface type.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - Why can't `@Binds` be used for the `ComicBookDatabase` itself?

---

## Concurrency (Kotlin Coroutines / Flow)

## StateFlow for state, Channel for one-off events
- Status: INTRODUCED (2026-08-11)
- Where it appears: every ViewModel — e.g. `ReaderViewModel.kt`: `_state` is a
  `MutableStateFlow<ReaderState>` exposed via `.asStateFlow()`; `eventChannel` is a
  `Channel<ReaderEvent>()` exposed via `.receiveAsFlow()`. Consumed via
  `collectAsStateWithLifecycle()` (state) and `ObserveAsEvents` (events) in `*Screen.kt` files.
- Why it was introduced: `StateFlow` replays its last value to new collectors (correct for
  screen state surviving recomposition/rotation); `Channel` does not replay (correct for
  one-shot things like navigation events, so they don't re-fire on process/config changes).
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - What breaks if `ReaderEvent`s were modeled as a second `StateFlow` instead of a `Channel`?

## Flow combinators (`combine`, `map`, `onEach`, `launchIn`, `flowOn`, `update`)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `LibraryViewModel.kt` (`combine` of `comicDirectory` + library contents
  flows, `.onEach { }.launchIn(viewModelScope)`), `ReadingListDetailViewModel.kt` (`combine` of
  list items + all comics to build available-comics list), `GetLibraryContentsUseCase.kt`
  (`.map`), `SafComicFileDataSource.kt` (`.flowOn(Dispatchers.IO)`), all `_state.update { }`
  calls.
- Why it was introduced: declarative composition of multiple reactive sources into one screen
  state instead of manually subscribing to each and merging by hand.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - Why `combine` and not `zip` for `LibraryViewModel`'s two flows?

## `Dispatchers.IO` + `withContext`/`flowOn` for blocking file work
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/data/source/SafComicFileDataSource.kt` — `scanForComicFiles` and
  `readMetadata` wrap their body in `withContext(Dispatchers.IO)`; `unzipPages` uses
  `.flowOn(Dispatchers.IO)` on the emitting `flow { }`.
- Why it was introduced: `DocumentFile`/`ZipInputStream`/`ContentResolver` calls are blocking
  I/O and must not run on the main thread.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - Why `withContext` for the suspend functions but `flowOn` for `unzipPages` — why not
    `withContext` there too?

## Manual `Job` cancellation to guard stale loads
- Status: INTRODUCED (2026-08-11)
- Where it appears: `ReaderViewModel.loadComic()`, `CollectionDetailViewModel.load()`,
  `ReadingListDetailViewModel.load()` — each keeps a `private var loadJob: Job?`, calls
  `loadJob?.cancel()` before starting a new load, and early-returns if already loading the same
  id/args.
- Why it was introduced: these load functions are called again on recomposition/navigation
  with new arguments (e.g. `LaunchedEffect(comicId, readingListId)` in `ReaderScreen.kt`); without
  cancellation, a slow first load could overwrite a newer one's state after the fact.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - What race is actually prevented here — could `viewModelScope` alone (without the manual
    `Job` tracking) have handled this?

## Coroutine test dispatcher setup
- Status: INTRODUCED (2026-08-11)
- Where it appears: every `*ViewModelTest.kt` (e.g.
  `feature/library/presentation/LibraryViewModelTest.kt`) — `UnconfinedTestDispatcher()` +
  `Dispatchers.setMain()`/`resetMain()` in `@BeforeEach`/`@AfterEach`, tests wrapped in
  `runTest { }`.
- Why it was introduced: `viewModelScope.launch` needs `Dispatchers.Main` to be set in a JVM
  unit test environment (no real Android main looper); `Unconfined` executes coroutines
  eagerly so `_state.value` is up to date immediately after an action.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

---

## Persistence

## Room (entities / DAOs / `@Upsert` / `@Transaction`)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/database/ComicBookDatabase.kt`, `core/database/entity/*.kt`
  (`ComicEntity`, `ReadingListEntity`, `ReadingListItemEntity`), `core/database/dao/*.kt`
  (`ComicDao`, `ReadingListDao`). `ComicDao.upsertComic` uses `@Upsert`;
  `ReadingListDao.reorderItems` uses `@Transaction` over a loop of single updates.
- Why it was introduced: local relational persistence for comics and reading lists.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Room foreign keys + composite unique index (no `@Relation`)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/database/entity/ReadingListItemEntity.kt` — `ForeignKey` to both
  `ReadingListEntity` and `ComicEntity` with `onDelete = CASCADE`, plus
  `Index(["listId","comicId"], unique = true)`. Joined manually (not via Room `@Relation`) in
  `feature/reader/domain/usecase/GetNextComicInListUseCase.kt` and
  `ReadingListDetailViewModel.load()` (combines two flows and does `associateBy`/`mapNotNull`
  in Kotlin).
- Why it was introduced: cascade delete keeps orphaned reading-list items from surviving a
  deleted list/comic; the join is done in the repository/ViewModel layer instead of a Room
  `@Relation` query.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - What would a Room `@Relation`-based approach have looked like here, and why wasn't it used?

## `fallbackToDestructiveMigration` (deliberate, temporary)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/di/DatabaseModule.kt` —
  `.fallbackToDestructiveMigration(dropAllTables = true)`; DB is currently at
  `version = 6` in `ComicBookDatabase.kt`. Flagged in `CLAUDE.md` as "replace before any
  beta/release build."
- Why it was introduced: pre-release, no real user data to preserve yet — trades away
  migration correctness for velocity while the schema is still churning.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Room `TypeConverter` for `LocalDateTime`
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/database/converters/LocalDateTimeConverter.kt`, registered via
  `@TypeConverters(LocalDateTimeConverter::class)` on `ComicBookDatabase.kt`.
- Why it was introduced: Room only persists primitive/String-like columns natively;
  `java.time.LocalDateTime` needs an explicit two-way string conversion.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## DataStore Preferences
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/data/datastore/DataStoreInstance.kt` (`Context.dataStore` delegate
  via `preferencesDataStore(name = "user_prefs")`),
  `core/data/repository/DataStoreUserSettingsRepository.kt` (`stringPreferencesKey`,
  `.data.map { }`, `.edit { }`).
- Why it was introduced: persists a single small piece of user state (the chosen comic root
  folder URI) without pulling in a Room table for it.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Storage Access Framework (SAF)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/data/source/SafComicFileDataSource.kt` (`DocumentFile.fromTreeUri`,
  `DocumentFile.fromSingleUri`, recursive `listFiles()` walk), `LibraryScreen.kt`
  (`rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree())` +
  `context.contentResolver.takePersistableUriPermission(...)`).
- Why it was introduced: the app reads user-chosen folders on shared/external storage without
  requesting broad storage permissions — scoped, persistable access via content `Uri`s instead
  of raw file paths.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - What happens to a saved `comicDirectory` URI permission across a device reboot or app
    reinstall?

## CBZ/ZIP parsing + on-disk extraction cache
- Status: INTRODUCED (2026-08-11)
- Where it appears: `SafComicFileDataSource.kt` — `ZipInputStream`/`ZipEntry` walk in
  `readMetadata` (thumbnail extraction into `context.filesDir`) and `unzipPages` (page
  extraction into `context.cacheDir`, one `Result` emitted per page via `flow { emit(...) }`).
- Why it was introduced: `.cbz` is just a zip of images; pages are unzipped to cache
  incrementally (emitted as a stream) so the reader can show early pages before the whole
  archive is extracted.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - `cacheDir` can be cleared by the OS under storage pressure — what's the failure mode if a
    page file disappears mid-read?

---

## Navigation (Navigation3)

## Navigation3 core (`NavBackStack` / `NavDisplay` / `NavEntry` / `NavKey`)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `navigation/Route.kt` (`sealed interface Route : NavKey`),
  `navigation/NavigationRoot.kt` (`rememberNavBackStack`, `NavDisplay`, `entryProvider` `when`
  over route types).
- Why it was introduced: type-safe, Compose-first navigation (successor to
  androidx.navigation's string-route Compose Navigation).
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Polymorphic serialization for `NavKey` (process-death restore)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `NavigationRoot.kt` — `SavedStateConfiguration { serializersModule =
  SerializersModule { polymorphic(NavKey::class) { subclass(...) } } }` registers every
  `Route` subclass; `Route.kt` marks the sealed interface and every variant `@Serializable`.
  Documented as a hard requirement in `CLAUDE.md`.
- Why it was introduced: the back stack is saved across process death as serialized `NavKey`s;
  a `Route` subclass missing from the polymorphic module breaks restoration.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - What's the actual crash/symptom if you forget to register a new `Route` subclass here?

## Nav entry decorators (`ViewModelStore` + `SaveableStateHolder`)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `NavigationRoot.kt` — `entryDecorators = listOf(
  rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator())`.
- Why it was introduced: gives each back-stack entry its own retained `ViewModelStore` (so
  `hiltViewModel()` scopes correctly per entry) and its own saveable-state slot (so scroll
  position/local `remember {}` survives popping back to it).
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Bottom-tab backstack-reset navigation
- Status: INTRODUCED (2026-08-11)
- Where it appears: `NavigationRoot.kt` — `switchTab()` clears the back stack down to a single
  entry on tab switch; `TOP_LEVEL_ROUTES` set gates when the bottom `NavigationBar` shows.
- Why it was introduced: tab switching (Library / Reading Lists / Settings) resets to that
  tab's root rather than stacking histories across tabs.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

---

## Compose UI

## Material3 theming (light/dark + gated dynamic color)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/ui/theme/Theme.kt` (`ComicBookReaderTheme`, `dynamicColor` param
  defaults `false` with a comment explaining why), `Color.kt`, `Type.kt`.
- Why it was introduced: custom Light/DarkColorScheme by default; Material You dynamic color
  is supported (API 31+) but opt-in rather than silently overriding the custom palette.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## `LazyVerticalGrid` with stable item keys
- Status: INTRODUCED (2026-08-11)
- Where it appears: `feature/library/presentation/LibraryScreen.kt` —
  `GridCells.Fixed(2)`, two `items(..., key = { ... })` blocks (folders, then root comics) in
  one grid.
- Why it was introduced: mixed-content grid (folder tiles + individual comic thumbnails) with
  keys to preserve item identity across recomposition/reordering.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## `HorizontalPager` with an appended "next comic" sentinel page
- Status: INTRODUCED (2026-08-11)
- Where it appears: `feature/reader/presentation/ReaderScreen.kt` — `pageCount = state.pages.size
  + if (hasNextComicPage) 1 else 0`; the pager's `page` lambda renders `NextComicTransitionPage`
  when `page >= state.pages.size`.
- Why it was introduced: the "up next" transition screen is modeled as just one more page in
  the same pager rather than a separate overlay/navigation event.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Coil3 `AsyncImage`
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/ui/components/ComicThumbnail.kt`, `ReaderScreen.kt` — loads from
  plain local file path strings (`thumbnailPath`) or content `Uri` strings (`state.pages`).
  `coil3-compose` + `coil3-network-okhttp` in `app/build.gradle.kts`.
- Why it was introduced: async image loading/caching/decoding without hand-rolled bitmap
  loading on the main thread.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - Why pull in `coil-network-okhttp` at all if every image source here is a local file/URI?

## `ObserveAsEvents` (lifecycle-aware one-shot event collection)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `core/presentation/ObserveAsEvents.kt` — `LaunchedEffect(flow,
  lifecycle) { lifecycle.repeatOnLifecycle(STARTED) { flow.collect(onEvent) } }`; called in
  every `*Root` composable (e.g. `LibraryScreen.kt`, `ReaderScreen.kt`) against the ViewModel's
  `events` flow.
- Why it was introduced: prevents events from being collected (and re-fired/dropped) while the
  screen is backgrounded, and re-collects cleanly when it returns to `STARTED`.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## `collectAsStateWithLifecycle`
- Status: INTRODUCED (2026-08-11)
- Where it appears: every `*Root` composable, e.g. `val state by
  viewModel.state.collectAsStateWithLifecycle()` in `LibraryScreen.kt`, `ReaderScreen.kt`.
- Why it was introduced: lifecycle-aware `StateFlow` collection (stops collecting when
  backgrounded) instead of plain `collectAsState()`.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## SAF folder picker via `ActivityResultContracts.OpenDocumentTree`
- Status: INTRODUCED (2026-08-11)
- Where it appears: `LibraryScreen.kt` — `rememberLauncherForActivityResult(contract =
  ActivityResultContracts.OpenDocumentTree(), onResult = { ... })`, launched in response to a
  `LibraryEvent.RequestDirectoryPicker` event.
- Why it was introduced: standard Activity Result API wiring for the system folder-picker UI,
  triggered from a ViewModel event rather than called directly (keeps the picker launch out of
  the ViewModel, which has no Activity context).
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

---

## Testing

## JUnit5 (Jupiter) instead of JUnit4
- Status: INTRODUCED (2026-08-11)
- Where it appears: every file in `app/src/test/` uses `org.junit.jupiter.api.Test`/
  `@BeforeEach`/`@AfterEach`; `app/build.gradle.kts` has `tasks.withType<Test> {
  useJUnitPlatform() }` plus `testImplementation(libs.junit.jupiter)` and
  `testRuntimeOnly(libs.junit.platform.launcher)`.
- Why it was introduced: newer JUnit API (`@BeforeEach` etc.) — note plain `junit` (JUnit4) is
  also still a dependency, used for `androidx.test`/Espresso instrumented tests.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - Why does `libs.junit` (JUnit4) still appear in `app/build.gradle.kts` alongside Jupiter?

## Turbine (Flow/Channel testing)
- Status: INTRODUCED (2026-08-11)
- Where it appears: e.g. `LibraryViewModelTest.kt` — `viewModel.events.test { onAction(...);
  assertThat(awaitItem()).isEqualTo(...) }`.
- Why it was introduced: deterministic assertions against `Flow`/`Channel` emissions (the
  `events` flow) without manual `toList()`/timing hacks.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## AssertK fluent assertions
- Status: INTRODUCED (2026-08-11)
- Where it appears: every test file, e.g. `assertThat(viewModel.state.value.folders).hasSize(1)`
  in `LibraryViewModelTest.kt`.
- Why it was introduced: fluent, type-safe assertion library (`assertThat(x).isEqualTo(y)`
  style) instead of raw JUnit `assertEquals`.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Hand-written fake repositories (test doubles)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `app/src/test/.../fakes/FakeComicRepository.kt`,
  `FakeReadingListRepository.kt`, `FakeUserSettingsRepository.kt`, `TestFixtures.kt` — each
  implements a domain repository interface directly with in-memory `MutableStateFlow`-backed
  state and call-recording lists (e.g. `syncLibraryCalls`).
- Why it was introduced: possible only because domain repository interfaces have zero Android
  imports (per `CLAUDE.md`); avoids a mocking framework entirely.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - What's the actual tradeoff of a hand-written fake vs. e.g. MockK here?

---

## Build Configuration

## Gradle version catalog (`libs.versions.toml`)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `gradle/libs.versions.toml` (`[versions]`/`[libraries]`/`[plugins]`
  sections), referenced throughout `app/build.gradle.kts` as `libs.xxx`.
- Why it was introduced: centralizes dependency versions in one TOML file instead of
  scattering version strings across build files.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## KSP instead of kapt (Room + Hilt annotation processing)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `app/build.gradle.kts` — `alias(libs.plugins.ksp)` plugin,
  `ksp(libs.androidx.room.compiler)`, `ksp(libs.hilt.compiler)`.
- Why it was introduced: KSP is the faster, kapt-replacement annotation-processing API; both
  Room and Hilt support it directly.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -

## Forced dependency resolution (`kotlinx-metadata-jvm` pin)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `app/build.gradle.kts` —
  `configurations.all { resolutionStrategy { force("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0") } }`,
  alongside an explicit `ksp("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.0")` and
  `implementation(libs.kotlinx.metadata.jvm)`.
- Why it was introduced: build-config quirk — a transitive dependency conflict on
  `kotlinx-metadata-jvm`'s version being force-resolved to a specific version across all
  configurations.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  - What was actually failing before this `force(...)` was added — what error would you see
    without it?

## kotlinx.serialization plugin (for Nav3 type-safe routes)
- Status: INTRODUCED (2026-08-11)
- Where it appears: `app/build.gradle.kts` —
  `alias(libs.plugins.jetbrains.kotlin.serialization)`, noted in `libs.versions.toml` as
  "Optional Plugin for Nav 3"; used by every `@Serializable` `Route` in `navigation/Route.kt`.
- Why it was introduced: Navigation3's back stack serialization (for process-death restore)
  depends on `kotlinx.serialization`, not just the `NavKey` interface.
- Research links:
  -
- My Understanding:
  (empty)
- Accuracy Check:
  (pending)
- Quiz Log:
  -
- Open Questions:
  -