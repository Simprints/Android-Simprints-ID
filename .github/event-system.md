## Event System

### Event Hierarchy

* `Event` is a **sealed class** with 60+ concrete subtypes (e.g., `AuthenticationEvent`, `FaceCaptureEvent`, `EnrolmentEventV4`, `OneToOneMatchEvent`).
* Each `Event` has: `id`, `type: EventType` (enum discriminator), `payload: EventPayload`, `scopeId`, and `projectId`.
* `EventPayload` is a sealed base class with: `type`, `eventVersion: Int` (for schema versioning), `createdAt`, `endedAt`, and `toSafeString()` for safe logging.
* Each concrete event defines its own nested `Payload` data class extending `EventPayload`.
* Serialization uses **manual polymorphic dispatch** via `concreteSerializer` instead of Kotlinx's built-in discriminator, to avoid collision with the business `type` field.

### Event Scopes

* Events are grouped into **scopes** via `EventScope` — a container with metadata about the session (app version, device info, language, modalities, project config).
* `EventScopeType` enum: `SESSION` (user workflow), `UP_SYNC`, `DOWN_SYNC`, `SAMPLE_UP_SYNC`.
* Scopes have a lifecycle: created with `createEventScope()`, ended with `closeEventScope()` which sets `endedAt` and `endCause` (`WORKFLOW_ENDED` or `NEW_SESSION`).
* Events reference their scope via `scopeId`.

### Adding Events

* Use `SessionEventRepository.addOrUpdateEvent(event)` to add an event to the current session (auto-scopes).
* Use `EventRepository.addOrUpdateEvent(scope, event)` for explicit scope association.
* Events are validated before saving (e.g., duplicate GUID checks).
* Use @SessionCoroutineScope for adding or updating events and sessions
* Feature modules usually create events in use cases (e.g., `AddCaptureEventsUseCase`) and call the repository to persist them.

### Local Storage

* Events are stored in Room as `DbEvent` with the full event serialized as JSON in `eventJson`.
* Scopes are stored as `DbEventScope` with metadata in `payloadJson`.

### Event Sync

* **Upload**: `EventUpSyncTask` loads closed scopes + events, tokenizes sensitive fields, maps domain models to API models (`ApiEvent`, `ApiEventScope`), and uploads in batch.
* **Download**: `SimprintsEventDownSyncTask` fetches remote events and creates local enrolment records.
* API models (`ApiEvent`, `ApiEventScope`, `ApiEventPayloadType`) are separate from domain models; mapping is done via `MapDomainEventScopeToApiUseCase` and `MapDomainEventToApiUseCase`.

### Tokenization (Sensitive Data)

* Sensitive fields (e.g., `attendantId`, `moduleId`, `userId`) use `TokenizableString` — a sealed class with `Raw` and `Tokenized` subtypes.
* Events declare tokenizable fields via `getTokenizableFields()` and accept encrypted values via `setTokenizedFields()`.
* `TokenizeEventPayloadFieldsUseCase` encrypts raw fields before upload. The `tokenizedFields` list on `ApiEvent` documents which JSON paths contain encrypted data.
