# ComicBookReader

Android app (single `:app` module) for reading local `.cbz` comic files, organized into
folder-derived collections with reading progress tracking and user-curated reading lists.

## Architecture

Package-by-feature within the single `:app` module — no multi-module split (not warranted at
this app's scale; the package boundaries below map 1:1 to future Gradle modules if that ever
changes).

```
core/
├── database/     Room: ComicBookDatabase, DAOs, entities
├── domain/       error/Result wrapper, domain models, repository interfaces (no Android imports)
├── data/         data sources, repository impls, mappers (entity <-> domain model)
├── di/           Hilt modules (DatabaseModule, RepositoryModule)
├── presentation/ UiText, ObserveAsEvents, error-to-UiText mapping
├── ui/           theme, shared composables (ComicThumbnail, etc.)
└── navigation/   Route.kt, NavigationRoot.kt (Nav3)
feature/
├── library/      entry screen; folder tiles + root-level comic thumbnails, owns the SAF folder picker
├── collection/   detail grid for a single folder's comics (list view was absorbed into library/)
├── reader/       paged comic reader; reading-list "up next" transitions
├── readinglists/ create/manage reading lists and their ordered items
└── settings/     placeholder screen (no state yet — not a full MVI screen)
```

Each screen follows MVI: `State` + `Action` + `Event` + `ViewModel`, with a stateful `*Root`
composable (owns the ViewModel) wrapping a stateless `*Screen(state, onAction)` composable. A
placeholder screen with no state or interaction yet (e.g. `settings/`) is a deliberate, temporary
exception — it should be given the full MVI shape once it has real content.

**DI**: Hilt. **Navigation**: Navigation3 (`androidx.navigation3`) — every `Route` subclass must
be registered in `NavigationRoot.kt`'s polymorphic `SerializersModule` or process-death restore
breaks. `NavigationRoot` wraps `NavDisplay` in a `Scaffold` with a bottom `NavigationBar`
(Library / Reading Lists / Settings) shown only on those three top-level routes; tab switches
reset the back stack to a single entry rather than stacking.

## Data model notes

- **Collections are derived, not stored.** A comic's `folderPath` (relative to the user's chosen
  root) is the only persisted signal; `GetLibraryContentsUseCase` (in `feature/library`) partitions
  the flat comic list into folder groups (non-empty `folderPath`) and root-level comics (empty
  `folderPath`, shown as individual thumbnails rather than a synthetic folder) at query time. No
  `CollectionEntity` table — avoids keeping a second table in sync with filesystem scans.
- **Reading lists are real tables** (`reading_list`, `reading_list_item`) since ordering has no
  filesystem equivalent. Item order is a plain integer `position`, renumbered on reorder.
- Domain repository interfaces (`core/domain/repository`) have zero Android framework imports —
  `Uri`/`Context` are confined to the data-layer implementations. This is what makes the fakes in
  `app/src/test/.../fakes/` possible.
- The DB currently uses `fallbackToDestructiveMigration`. That's fine pre-release (no real user
  data to preserve) — replace with real `Migration`/`autoMigration` objects before any beta/release
  build.

## Error handling

`core/domain/error/Result<D, E>` (`Result.Success`/`Result.Error`) + `DataError.Local` sealed
type, mapped to `UiText` for display via `core/presentation/mapper/DataErrorToUiText.kt`.

## Testing

JUnit5 + Turbine + AssertK + `kotlinx-coroutines-test` (`UnconfinedTestDispatcher`). Fakes live in
`app/src/test/.../fakes/` and implement the domain repository interfaces directly.