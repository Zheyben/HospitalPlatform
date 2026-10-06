# DOMAIN BASELINE - HOSPITALPLATFORM

**Version:** 1.0
**Cut-off date:** 2026-09-29
**Branch audited:** `feat/domain-definition-baseline`
**Base commit:** `5656b70a9bc7a65ccaab36c23b6edf3a96225d62`
**Purpose:** official functional and architectural baseline for the current repository state.

## 1. Authority and status model

This document does not create product behavior. It classifies the behavior found in code, migrations, tests, accepted ADRs and planning documents.

Evidence is interpreted in this order:

1. `database/migrations/V1__initial_schema.sql` through `V3__support_appointment_lifecycle.sql` govern the persistent schema.
2. Production code and executable tests govern current behavior.
3. Accepted ADRs govern approved architectural decisions, including decisions not yet implemented.
4. This baseline governs the cross-document classification of current, pending, future and open scope.
5. Specialized architecture documents provide detail when they do not contradict the sources above.
6. The roadmap, PRD, SRS, app flow and implementation plan describe product intent; a mention there does not prove implementation.

| Label | Meaning |
|---|---|
| `IMPLEMENTED` | Production behavior exists and has direct test evidence. |
| `PARTIAL` | A real subset exists, but the documented requirement is broader. |
| `DEFINED_NOT_IMPLEMENTED` | The product or architecture describes the capability, but no functional implementation exists. |
| `FUTURE` | Explicitly outside the current MVP or deferred until a later expansion. |
| `OPEN` | A human or institutional decision is required before implementation. |

## 2. Product scope

HospitalPlatform is an academic platform for scheduled outpatient appointment management at the Hospital de Huaycan case study. The implemented backend currently supports identity and access, administrative user/patient/professional management, schedule configuration, slot consultation, appointment reservation and lifecycle, operational appointment flow, and transactional audit of appointment transitions.

The repository does not establish that the platform is an official hospital system, does not contain validated institutional AS-IS evidence, and must use synthetic data for development and demonstration.

### 2.1 Implemented and validated

- Modular Spring Boot backend under `/api/v1` with PostgreSQL and Flyway V1-V3.
- JWT login, persisted hashed refresh tokens, refresh rotation, logout revocation, disabled/soft-deleted user rejection and role authorities.
- Administrative management of users and roles.
- Administrative management of patients, patient-to-user linkage and patient soft delete; own-profile read for `PATIENT`.
- Administrative management of professionals and professional ownership lookup.
- Administrative schedule CRUD/status and availability-slot read operations.
- Atomic slot reservation and release contracts.
- Appointment creation, own/admin/reception consultation, confirmation, cancellation and rescheduling.
- Operational flow `CHECK_IN -> WAITING -> IN_ATTENTION -> FINISHED` and completion of the appointment.
- Transactional appointment audit, pessimistic locking, rollback and PostgreSQL concurrency coverage.

### 2.2 Defined for the MVP but not fully implemented

- Patient self-registration and self-service profile update.
- Specialty/catalog management and professional-specialty administration.
- Patient/reception availability consultation.
- Slot creation or automatic slot generation.
- Public institutional portal, reservation web portal, operational frontend and patient mobile application.
- Waitlist, slot offers, notifications, ambulatory priority review, no-show policy and dashboards.
- Accessibility preferences, operational metrics, CI/CD, performance and recovery evidence.

Items in this group are not automatically ready for implementation: those marked `OPEN` in section 18 require an explicit decision first.

### 2.3 Future or outside the current MVP

- Complete clinical record, clinical notes, diagnoses, prescriptions and treatments.
- Pharmacy, laboratory, diagnostic imaging, hospitalization, emergency and surgery modules.
- Clinical documents with electronic/digital signature and derived PDF.
- Official SIS, SIHCE, MINSA, FHIR, WhatsApp or other external integration.
- Billing, insurance, payment and coverage workflows.
- Redis, queues and distributed architecture without a measured trigger.

### 2.4 Current functional boundary

The executable product is a backend foundation and appointment core. Empty package structures and existing database tables for future modules do not make those modules functional. There is no tracked functional web or mobile client in `apps/frontend` or `apps/mobile`.

## 3. Actors and roles

### 3.1 Current actors

| Actor | Current purpose and responsibilities | Current prohibitions and ownership |
|---|---|---|
| `PATIENT` | Authenticate, read own user/patient profile, create an appointment for the linked active patient, read/manage own appointments. | Cannot provide another `patientId`, administer master data, or change `FlowStage`. |
| `RECEPTIONIST` | Create/read/manage appointments for active patients; perform check-in and waiting transitions. | No user/patient/professional/agenda administration; cannot start or complete professional attention. |
| `PROFESSIONAL` | Start and complete operational attention for an appointment linked to the authenticated professional profile. | Cannot create/read appointment resources through current appointment query endpoints; cannot perform reception transitions or administer modules. |
| `ADMIN` | Administer users, patients, professionals and agenda; create/read/manage appointments. | Has no override for check-in, waiting, start-attention or complete. |
| HospitalPlatform system | Validate tokens, reserve/release slots, enforce constraints, persist and roll back transactions, and record approved audit events. | It is a technical actor, not a human role and does not diagnose or assign clinical priority. |

`TRIAGE` and `SYSTEM` exist in `RoleName`, but no controller grants them a current business operation. Visitor, clinical reviewer and auditor are documented target actors without implemented access surfaces.

### 3.2 Permission matrix

Legend: `YES`, `OWN`, `ASSIGNED`, `NO`, `PUBLIC`, `ADMINISTRATIVE`.

| Function | PATIENT | RECEPTIONIST | PROFESSIONAL | ADMIN |
|---|---:|---:|---:|---:|
| Login / refresh | PUBLIC | PUBLIC | PUBLIC | PUBLIC |
| Logout / own user profile | YES | YES | YES | YES |
| Manage users and roles | NO | NO | NO | YES |
| Manage patients / link user / deactivate | NO | NO | NO | YES |
| Read own patient profile | OWN | NO | NO | NO |
| Manage professionals | NO | NO | NO | YES |
| Manage schedules and read availability | NO | NO | NO | YES |
| Create appointment | OWN | ADMINISTRATIVE | NO | ADMINISTRATIVE |
| List/read appointment | OWN | YES | NO | YES |
| Confirm/cancel/reschedule | OWN | ADMINISTRATIVE | NO | ADMINISTRATIVE |
| Check-in / waiting | NO | YES | NO | NO |
| Start-attention / complete | NO | NO | ASSIGNED | NO |

Every non-public operation also requires authentication. Appointment role checks are supplemented by patient or professional ownership and state validation in `AppointmentService`.

## 4. Module responsibilities

| Module | Current responsibility | Owned persistence | Public capability | Status |
|---|---|---|---|---|
| Auth/Security | Login, JWT validation, refresh rotation/revocation, logout and security chain. | `refresh_tokens`; security uses users. | HTTP auth API; no module contract. | `IMPLEMENTED` |
| Users | Access identity, credentials, enabled state, roles and permissions. | `users`, `roles`, `permissions`, joins. | `UserLookupService`, `CurrentUserService`, authenticated principal services. | `IMPLEMENTED` |
| Patients | Administrative patient identity, user link and active state derived from `deleted_at`. | `patients`. | `PatientLookupService`, `PatientReference`. | `IMPLEMENTED` |
| Professionals | License identity, optional user link and active state derived from `deleted_at`. | `professionals`. | `ProfessionalLookupService`. | `IMPLEMENTED`, specialties are partial |
| Agenda | Schedules, availability-slot queries, usability, atomic reservation and release. | `schedules`, `availability_slots`. | Three slot services and `AvailabilitySlotReference`. | `IMPLEMENTED`, slot generation absent |
| Appointments | Appointment ownership, lifecycle, operational flow and orchestration. | `appointments`. | HTTP API; no cross-module public contract. | `IMPLEMENTED` |
| Audit | Persist approved appointment events in the caller transaction. | `audit_logs`. | `AuditLogService`. | `PARTIAL` event coverage |
| Catalogs | Intended specialty/service ownership. | Schema has `specialties` and `professional_specialties`. | None. | `DEFINED_NOT_IMPLEMENTED` |
| Waitlist | Intended waitlist and offers. | V1 has `waitlist_entries`; no slot-offer table. | None. | `OPEN` / not implemented |
| Priority | Intended ambulatory priority review. | V1 has `priority_requests`. | None. | `OPEN` / not implemented |
| Notifications | Intended reminders and messages. | No table or implementation. | None. | `OPEN` / not implemented |
| Dashboard | Intended operational indicators. | No dedicated table or implementation. | None. | `DEFINED_NOT_IMPLEMENTED` |

## 5. Dependencies and contracts

### 5.1 Dependency matrix

| Consumer | Provider | Contract or dependency | Purpose | Assessment |
|---|---|---|---|---|
| Patients | Users | `UserLookupService`, `CurrentUserService` | Validate a user link and resolve own profile. | Public service interfaces; no external repository/entity access. |
| Agenda | Professionals | `ProfessionalLookupService` | Require an active professional for schedules. | Correct contract boundary. |
| Appointments | Patients | `PatientLookupService`, `PatientReference` | Active patient and patient ownership. | Correct contract boundary. |
| Appointments | Professionals | `ProfessionalLookupService` | Active professional and professional ownership. | Correct contract boundary. |
| Appointments | Agenda | Reservation/release services and slot reference | Atomic slot state and derived professional/specialty. | Correct contract boundary. |
| Appointments | Audit | `AuditLogService`, `AuditEventType` | Transactional lifecycle audit. | Correct contract boundary. |
| Appointments | Users | `CurrentUserService` | Current actor and ownership. | Public service interface. |
| Audit | Users | `CurrentUserService` | Resolve audit actor. | Public service interface. |
| Auth/Security | Users | User entity/repository and user-detail services | Authenticate and persist refresh-token user relation. | Direct dependency remains an explicit architecture decision to resolve; not introduced here. |

No direct imports from Appointments to external repositories or entities were found.

### 5.2 Current public contracts

| Contract | Owner / consumers | Operations and exposed data | Does not expose |
|---|---|---|---|
| `UserLookupService` | Users / Patients | `existsActiveUser(UUID)` -> boolean. | User entity, credentials, roles, repository. |
| `CurrentUserService` | Users / Patients, Appointments, Audit | `currentUserId()` -> UUID. | SecurityContext implementation or User entity. |
| `PatientLookupService` | Patients / Appointments | Existence, active existence, minimal reference, active reference by user. `PatientReference` exposes `id` and derived `active`. | Patient entity, document/contact data, repository. |
| `ProfessionalLookupService` | Professionals / Agenda, Appointments | `existsActiveProfessional`; `isActiveProfessionalLinkedToUser`. | Professional entity, license, repository. |
| `AvailabilitySlotService` | Agenda / potential readers | Existence, persisted availability and usability. | Slot/Schedule entities and repository. |
| `AvailabilitySlotReservationService` | Agenda / Appointments | Atomically reserves a usable slot and returns slot/professional/specialty/date/time reference. | JPA entities and SQL. |
| `AvailabilitySlotReleaseService` | Agenda / Appointments | Atomically releases a reserved slot. | JPA entities and SQL. |
| `AuditLogService` | Audit / Appointments | Records event, entity, actor-derived context and old/new maps in an existing transaction. | Audit entity/repository and PostgreSQL JSONB details. |

No future contract is promoted to current status by this baseline.

## 6. State machines

### 6.1 AppointmentStatus

Current values: `SCHEDULED`, `CONFIRMED`, `CANCELLED`, `RESCHEDULED`, `COMPLETED`.

| Initial state | Operation | Final state | Actor | Idempotency |
|---|---|---|---|---|
| creation | create | `SCHEDULED` | PATIENT own; ADMIN/RECEPTIONIST for active patient | Not idempotent; slot uniqueness prevents duplicate active allocation. |
| `SCHEDULED` | confirm | `CONFIRMED` | PATIENT own; ADMIN/RECEPTIONIST | Repeating in `CONFIRMED` returns success without duplicate audit. |
| `SCHEDULED` or `CONFIRMED` | cancel | `CANCELLED` | PATIENT own; ADMIN | Repeating in `CANCELLED` preserves cancellation data and side effects. RECEPTIONIST receives 403. |
| `SCHEDULED` or `CONFIRMED` | reschedule | original `RESCHEDULED`; successor `SCHEDULED` | PATIENT own; ADMIN | Not repeatable on the original; one direct successor is enforced. RECEPTIONIST receives 403. |
| `CONFIRMED` + `IN_ATTENTION` | complete | `COMPLETED` | assigned PROFESSIONAL | Repeating `COMPLETED` + `FINISHED` returns success without duplicate audit. |

All other transitions are invalid. `NO_SHOW` is not a current state.

### 6.2 FlowStage

Initial stage is `null`. The only valid chain is:

```text
CONFIRMED/null -> CHECK_IN -> WAITING -> IN_ATTENTION -> FINISHED/COMPLETED
```

- `RECEPTIONIST` owns `CHECK_IN` and `WAITING`.
- The assigned active `PROFESSIONAL` owns `IN_ATTENTION` and `FINISHED`.
- Repeating the operation for the current stage is idempotent.
- Skips, reversals, incompatible appointment status and wrong ownership are rejected.
- There is no `FlowStage.COMPLETED`; completion is `AppointmentStatus.COMPLETED` plus `FlowStage.FINISHED`.

### 6.3 Slot state

Current values are `AVAILABLE`, `RESERVED`, `BLOCKED`. A slot is usable only when its status is `AVAILABLE` and its schedule is active. No past-date, lead-time or timezone rule is applied. Completion leaves the slot `RESERVED`; cancellation and rescheduling release the old reserved slot.

## 7. Business rules baseline

The identifiers are preserved from `docs/requisitos/REGLAS_NEGOCIO.md`.

| Rule | Current baseline | Owner | Evidence | Status |
|---|---|---|---|---|
| RB-001 | Demonstration/development uses synthetic data and no official integration. | Product | Roadmap, AS-IS. | `DEFINED_NOT_IMPLEMENTED` as operational governance |
| RB-002 | Role and ownership are enforced for implemented endpoints. | Security/modules | Security chain, `@PreAuthorize`, services/tests. | `IMPLEMENTED`, final institutional role map `OPEN` |
| RB-003 | Inactive specialty is not reservable. | Catalog/Agenda | Documented; catalog has no implementation and Agenda relies on FK only. | `DEFINED_NOT_IMPLEMENTED` |
| RB-004 | Availability is represented by schedules and discrete slots. | Agenda | V1, Agenda entities/contracts. | `IMPLEMENTED` |
| RB-005 | At most one active appointment per slot; competing reservation loses cleanly. | Agenda/Appointments | Conditional update, V3 partial unique index, concurrency IT. | `IMPLEMENTED` |
| RB-006 | Appointment derives professional/specialty context from the reserved slot and starts `SCHEDULED/null`. | Appointments | Reservation reference, constructor, tests. | `IMPLEMENTED` |
| RB-007 | Reservation response does not confirm attendance. | Appointments | Create remains `SCHEDULED`. | `IMPLEMENTED` |
| RB-008 | Confirmation performs `SCHEDULED -> CONFIRMED`; temporal policy remains open. | Appointments | Service, ADR-007, lifecycle tests. | `IMPLEMENTED` current transition / `OPEN` time rule |
| RB-009 | Cancellation preserves actor/time/history and releases slot; temporal policy remains open. | Appointments | Service, audit, rollback tests. | `IMPLEMENTED` current transition / `OPEN` time rule |
| RB-010 | Rescheduling preserves predecessor/successor and rolls back fully on failure. | Appointments | V3, service, rollback/concurrency tests. | `IMPLEMENTED` |
| RB-011 | Release and reassignment are separate; completed appointments retain consumed slots. | Agenda/Appointments | Release contract, complete operation/tests. | `IMPLEMENTED` |
| RB-012 | Waitlist is separate from appointments. | Waitlist | Roadmap/SRS only. | `OPEN`, no functional module |
| RB-013 | Slot reassignment requires consent and approved selection/expiration policy. | Waitlist | Roadmap/SRS only. | `OPEN` |
| RB-014 | Slot-offer acceptance must be atomic. | Waitlist/Agenda | Approved design only. | `DEFINED_NOT_IMPLEMENTED` |
| RB-015 | Ambulatory priority is manual, non-emergency and audited. | Priority | ADR-008; policy/role absent. | `OPEN` |
| RB-016 | AppointmentStatus is limited to five current values. | Appointments | Enum and V1 constraint. | `IMPLEMENTED` |
| RB-017 | FlowStage is the exact ordered four-stage chain from a confirmed appointment. | Appointments | Enum, ADR-007, service/tests. | `IMPLEMENTED` |
| RB-018 | Reception and assigned professional own distinct flow transitions; current-stage repeats are idempotent. | Appointments | Controller/service/authorization and IT tests. | `IMPLEMENTED` |
| RB-019 | Finish sets `FINISHED` + `COMPLETED` and retains the slot. | Appointments | Entity/service/IT. | `IMPLEMENTED` |
| RB-020 | Critical appointment transitions and audit share one transaction and roll back together. | Appointments/Audit | `MANDATORY` audit contract and rollback IT. | `IMPLEMENTED` |
| RB-021 | Notification failure must not roll back a valid appointment change. | Notifications | Design only. | `DEFINED_NOT_IMPLEMENTED` |
| RB-022 | Public content requires authorized/verifiable sources and academic disclaimer. | Institutional content | Roadmap/SRS only. | `OPEN` |
| RB-023 | State meaning cannot depend only on color; accessible visual preferences are planned. | Frontend | Roadmap/UI brief only. | `DEFINED_NOT_IMPLEMENTED` |
| RB-024 | Metrics require defined events, periods and denominators; academic figures are not hospital baselines. | Dashboard | Roadmap/SRS only. | `OPEN` |

## 8. Functional requirements catalogue

The canonical IDs come from `docs/requisitos/SRS-HOSPITALPLATFORM.md`. Their implementation labels below supersede that document's static `[PARCIAL]` inspection label for this repository cut.

### 8.1 Implemented or partially implemented RF

#### RF-002 - Authenticate user
**Description:** Validate email/password and active account, then issue access and refresh tokens.
**Actor:** Any registered user. **Preconditions:** Existing enabled, non-deleted account.
**Expected result:** JWT plus one persisted hashed refresh token, or controlled rejection.
**Status:** `IMPLEMENTED`. **Evidence:** Auth/Security services, HTTP security IT.

#### RF-003 - Renew and revoke session
**Description:** Atomically consume a refresh token, issue a successor, and revoke active tokens on logout.
**Actor:** Token holder / authenticated user. **Preconditions:** Valid active refresh token or authenticated logout.
**Expected result:** Rotated token pair or `204` logout; reuse is rejected.
**Status:** `IMPLEMENTED`. **Evidence:** V2, AuthService, concurrency and rollback IT.

#### RF-004 - Manage roles and authorize resources
**Description:** Admin assigns existing roles; server enforces role and implemented object ownership.
**Actor:** ADMIN for assignment; every authenticated actor for authorization.
**Preconditions:** Existing user/roles. **Expected result:** Updated role set or access decision.
**Status:** `PARTIAL`; permission-level administration/audit is absent. **Evidence:** Users/Security and authorization tests.

#### RF-005 - Maintain patient profile
**Description:** Admin creates/updates/deactivates/links patients; PATIENT reads own linked profile.
**Actor:** ADMIN and PATIENT. **Preconditions:** Unique document and valid active link.
**Expected result:** Administrative patient DTO or own profile.
**Status:** `PARTIAL`; patient self-registration/self-update is absent. **Evidence:** Patient module/tests.

#### RF-006 - Consult own appointments
**Description:** Return only appointments owned by the authenticated patient.
**Actor:** PATIENT. **Preconditions:** Active user-to-patient link.
**Expected result:** Own list/detail; foreign appointment is forbidden.
**Status:** `IMPLEMENTED`. **Evidence:** AppointmentService and authorization tests.

#### RF-007 - Manage professionals
**Description:** Admin creates, lists, reads and updates professional license/user reference.
**Actor:** ADMIN. **Preconditions:** Unique license; database-valid optional user ID.
**Expected result:** Professional DTO. **Status:** `PARTIAL`; no specialties management or deactivation endpoint.
**Evidence:** Professional module/tests and V1.

#### RF-011 - Configure agenda and slots
**Description:** Admin creates/reads/updates/enables schedules and reads existing slots.
**Actor:** ADMIN. **Preconditions:** Active professional, valid specialty FK and time range.
**Expected result:** Schedule state and slot views. **Status:** `PARTIAL`; no slot creation/generation or overlap rule.
**Evidence:** Agenda module/tests and V1.

#### RF-012 - Consult reservable availability
**Description:** Query slots by schedule/professional/date/status and expose derived usability.
**Actor:** Currently ADMIN only. **Preconditions:** Authenticated ADMIN.
**Expected result:** Slot DTO list/detail. **Status:** `PARTIAL`; patient/reception access is not implemented.
**Evidence:** AgendaController, AgendaService and tests.

#### RF-013 - Reserve appointment
**Description:** Reserve a usable slot and persist a `SCHEDULED` appointment with server-derived professional.
**Actor:** PATIENT own; ADMIN/RECEPTIONIST for an active patient.
**Preconditions:** Active patient/professional and usable slot. **Expected result:** `201` appointment and reserved slot.
**Status:** `IMPLEMENTED`. **Evidence:** Appointment module and PostgreSQL IT.

#### RF-014 - Prevent concurrent double assignment
**Description:** Ensure at most one active appointment obtains a slot.
**Actor:** System. **Preconditions:** Concurrent requests for one usable slot.
**Expected result:** One success; remaining requests conflict without partial state.
**Status:** `IMPLEMENTED`. **Evidence:** Conditional update, V3 index and concurrency IT.

#### RF-015 - Issue reservation receipt
**Description:** Return the created appointment ID and administrative state after reservation.
**Actor:** System. **Preconditions:** Successful RF-013.
**Expected result:** `AppointmentResponseDTO` and `Location`. **Status:** `PARTIAL`; no user-facing receipt and DTO omits resolved slot schedule details.
**Evidence:** AppointmentController/DTO tests.

#### RF-016 - Confirm attendance
**Description:** Change an owned/administratively authorized `SCHEDULED` appointment to `CONFIRMED`.
**Actor:** PATIENT own; ADMIN/RECEPTIONIST. **Preconditions:** Allowed current state.
**Expected result:** `200`, one transition and one audit event; repeat is idempotent.
**Status:** `IMPLEMENTED` without temporal policy. **Evidence:** lifecycle service/controller/IT.

#### RF-017 - Cancel appointment
**Description:** Cancel an eligible appointment, persist actor/time, release slot and audit atomically.
**Actor:** PATIENT own; ADMIN/RECEPTIONIST. **Preconditions:** `SCHEDULED` or `CONFIRMED`.
**Expected result:** `CANCELLED`; repeat is idempotent. **Status:** `IMPLEMENTED` without temporal policy.
**Evidence:** lifecycle service and rollback/concurrency IT.

#### RF-018 - Release eligible slot
**Description:** Release a reserved slot during cancellation or rescheduling.
**Actor:** System. **Preconditions:** Slot is `RESERVED` inside the caller transaction.
**Expected result:** `AVAILABLE` or full rollback. **Status:** `IMPLEMENTED` for current lifecycle; no waitlist reaction.
**Evidence:** Agenda release contract and integration tests.

#### RF-019 - Reschedule atomically
**Description:** Reserve a new slot, create one successor, mark original rescheduled, release old slot and audit.
**Actor:** PATIENT own; ADMIN/RECEPTIONIST. **Preconditions:** Eligible original and new usable slot.
**Expected result:** `201` successor; failure preserves all original state.
**Status:** `IMPLEMENTED`. **Evidence:** V3, service, HTTP, rollback and concurrency IT.

#### RF-026 - Register check-in
**Description:** Move `CONFIRMED/null` to `CHECK_IN`.
**Actor:** RECEPTIONIST. **Preconditions:** Exact state. **Expected result:** `200`, audit once.
**Status:** `IMPLEMENTED`. **Evidence:** controller/service/operations IT.

#### RF-027 - Move patient to waiting
**Description:** Move `CHECK_IN` to `WAITING`.
**Actor:** RECEPTIONIST. **Preconditions:** Exact prior stage. **Expected result:** `200`, idempotent repeat.
**Status:** `IMPLEMENTED`. **Evidence:** controller/service and HTTP idempotency IT.

#### RF-028 - Start operational attention
**Description:** Move `WAITING` to `IN_ATTENTION` for the assigned active professional.
**Actor:** PROFESSIONAL. **Preconditions:** Professional ownership and exact stage.
**Expected result:** `200`, audit once. **Status:** `IMPLEMENTED`.
**Evidence:** professional lookup contract, service and ownership/concurrency tests.

#### RF-029 - Finish operational attention
**Description:** Move `IN_ATTENTION` to `FINISHED` and appointment to `COMPLETED`.
**Actor:** Assigned PROFESSIONAL. **Preconditions:** Exact state and ownership.
**Expected result:** `200`; slot remains `RESERVED`; repeat is idempotent.
**Status:** `IMPLEMENTED`. **Evidence:** service and complete-flow IT.

#### RF-030 - Consult operation by role
**Description:** Provide role-filtered operational appointment/agenda views.
**Actor:** RECEPTIONIST/PROFESSIONAL. **Preconditions:** Authenticated role/ownership.
**Expected result:** Minimum operational data. **Status:** `PARTIAL`; reception can list appointments, but professional query/agenda surfaces are absent.
**Evidence:** current controllers and authorization tests.

#### RF-032 - Audit critical actions
**Description:** Persist actor, action, entity and state change in the business transaction.
**Actor:** System. **Preconditions:** One of seven implemented appointment transitions.
**Expected result:** One audit row per effective transition or full rollback.
**Status:** `PARTIAL`; creation, auth, user/admin actions, HTTP/IP and audit query are absent. **Evidence:** Audit module and IT.

### 8.2 Defined but pending implementation RF

#### RF-001 - Register patient account
**Actor:** PATIENT. **Preconditions:** Approved fields, identity/link and bootstrap policy.
**Expected result:** Atomic user and patient account without duplicates.
**Status:** `OPEN`; only separate ADMIN creation/link operations exist. **Evidence:** SRS versus User/Patient controllers.

#### RF-008 - Manage specialty catalog
**Actor:** ADMIN. **Preconditions:** Catalog ownership and status rules.
**Expected result:** Specialty CRUD/status without breaking history.
**Status:** `DEFINED_NOT_IMPLEMENTED`; V1 table only. **Evidence:** SRS, V1, empty catalogs module.

#### RF-009 - Maintain authorized public content
**Actor:** ADMIN/editor. **Preconditions:** Source and publishing authority.
**Expected result:** Versioned public content. **Status:** `OPEN`; no model/API. **Evidence:** Roadmap/SRS.

#### RF-010 - Consult public portal and access reservations
**Actor:** Visitor. **Preconditions:** Approved content and frontend architecture.
**Expected result:** Public information and reservation entry. **Status:** `DEFINED_NOT_IMPLEMENTED`. **Evidence:** Roadmap/SRS; no frontend files.

#### RF-020 - Manage waitlist entry
**Actor:** PATIENT. **Preconditions:** Approved eligibility/cancellation policy.
**Expected result:** Separate waitlist entry lifecycle. **Status:** `OPEN`; table exists, module is empty. **Evidence:** V1, SRS/RB-012.

#### RF-021 - Request ambulatory priority review
**Actor:** PATIENT. **Preconditions:** Approved non-emergency criteria.
**Expected result:** Review request without self-approval. **Status:** `OPEN`. **Evidence:** ADR-008 and V1 only.

#### RF-022 - Review ambulatory priority
**Actor:** Authorized clinical reviewer. **Preconditions:** Institutional role and criteria.
**Expected result:** Audited approval/rejection. **Status:** `OPEN`. **Evidence:** ADR-008; no role mapping/service/API.

#### RF-023 - Generate notifications and reminders
**Actor:** System. **Preconditions:** Approved channels, timing and provider.
**Expected result:** Non-clinical notification whose failure does not reverse the appointment.
**Status:** `OPEN`; module is empty. **Evidence:** Roadmap/SRS/RB-021.

#### RF-024 - Create released-slot offer
**Actor:** System. **Preconditions:** Approved selection/fairness policy.
**Expected result:** Offer without automatic reassignment. **Status:** `OPEN`; no slot-offer persistence. **Evidence:** Roadmap/SRS.

#### RF-025 - Respond to offer and assign slot
**Actor:** Offered PATIENT. **Preconditions:** Own pending, unexpired offer and usable slot.
**Expected result:** Exactly one atomic acceptance or rejection/expiration. **Status:** `OPEN`. **Evidence:** Roadmap/SRS only.

#### RF-031 - Consult operational indicators
**Actor:** ADMIN/auditor. **Preconditions:** Approved definitions, cohorts and access.
**Expected result:** Reproducible metric or not-measurable result. **Status:** `OPEN`; dashboard module is empty. **Evidence:** SRS/RB-024.

#### RF-033 - Store visual accessibility preferences
**Actor:** UI user. **Preconditions:** Implemented client and storage decision.
**Expected result:** Restorable theme/color-vision preference without color-only meaning.
**Status:** `DEFINED_NOT_IMPLEMENTED`. **Evidence:** Roadmap/UI brief/SRS.

### 8.3 Future RF

No canonical RF number is assigned to the explicitly future clinical workspace, clinical documents, prescription, orders, coverage or external interoperability. This baseline does not invent identifiers for them. They require separate scope, security, legal and domain approval before becoming requirements.

## 9. Non-functional requirements catalogue

| RNF | Requirement | Current status | Evidence / pending work |
|---|---|---|---|
| RNF-001 | Identity security and authorization | `PARTIAL` | BCrypt, JWT, refresh, role/ownership tests exist; HTTPS, rate limiting and complete security inventory are not demonstrated. |
| RNF-002 | Privacy and minimization | `PARTIAL` | DTO boundaries and minimal JWT claims exist; field-by-role matrix, retention and institutional privacy validation are open. |
| RNF-003 | Availability/reservation performance | `OPEN` | No approved baseline, load suite or measured p95. |
| RNF-004 | Prototype availability | `OPEN` | Actuator health/info exists; no approved availability window or staging evidence. |
| RNF-005 | Reservation usability | `DEFINED_NOT_IMPLEMENTED` | No client or usability protocol. |
| RNF-006 | Accessibility | `DEFINED_NOT_IMPLEMENTED` | Documented target only; no client. |
| RNF-007 | Channel compatibility | `DEFINED_NOT_IMPLEMENTED` | No functional web/mobile clients. |
| RNF-008 | Modular maintainability | `PARTIAL` | Package-by-feature and contracts are present; Auth -> Users direct dependency remains open. |
| RNF-009 | Evaluated scalability | `OPEN` | No reproducible load profiles or accepted degradation threshold. |
| RNF-010 | Critical-action traceability | `PARTIAL` | Appointment lifecycle is reconstructible; other critical domains and retention are not covered. |
| RNF-011 | Transactional integrity | `IMPLEMENTED` for auth/appointments | PostgreSQL constraints, transactions, rollback and concurrency tests. Waitlist-offer atomicity is unimplemented. |
| RNF-012 | Backup and recovery | `OPEN` | No RTO/RPO or restoration evidence. |
| RNF-013 | Technical observability | `PARTIAL` | Health/info only; no structured correlation, tracing, dashboards or alert thresholds. |

No performance, availability or scalability target is treated as achieved merely because the SRS contains an academic target.

## 10. Current functional flows

### 10.1 Authentication

- **Actor/precondition:** Registered active user with valid credentials.
- **Flow/result:** Login -> access JWT + hashed persisted refresh token; refresh atomically consumes old token and creates successor; logout revokes active refresh tokens.
- **Errors:** Invalid/expired/reused token, bad credentials, missing/disabled/soft-deleted user -> rejection.
- **Ownership/audit/slot/idempotency:** User identity only; no access audit; no slot; refresh token is one-use rather than idempotent.

### 10.2 Availability consultation

- **Actor/precondition:** Currently ADMIN and authenticated.
- **Flow/result:** Filter existing slots and derive `usable = AVAILABLE && schedule.active`.
- **Errors:** Missing detail -> 404; invalid input/auth -> 400/401/403.
- **Ownership/audit/slot/idempotency:** No object ownership, mutation or audit; read is repeatable.

### 10.3 Appointment creation

- **Actor/precondition:** PATIENT with active linked patient, or ADMIN/RECEPTIONIST with explicit active patient; usable slot and active derived professional.
- **Flow/result:** Conditional slot reservation -> appointment `SCHEDULED/null` -> `201` and `Location`.
- **Errors/rollback:** Invalid ownership/request, inactive party or slot conflict; any persistence failure restores slot.
- **Audit/idempotency:** No creation audit. Request is not idempotent, but active-slot uniqueness prevents double allocation.

### 10.4 Confirmation

- **Actor/precondition:** PATIENT owner or ADMIN/RECEPTIONIST; `SCHEDULED`.
- **Flow/result:** Lock -> `CONFIRMED` -> audit -> `200`.
- **Errors/rollback:** Invalid state/ownership; audit failure rolls back.
- **Slot/idempotency:** Slot stays `RESERVED`; repeated confirmation returns current DTO without a new audit.

### 10.5 Cancellation

- **Actor/precondition:** PATIENT owner or ADMIN/RECEPTIONIST; `SCHEDULED` or `CONFIRMED`.
- **Flow/result:** Lock -> `CANCELLED` with actor/time -> release slot -> audit -> `200`.
- **Errors/rollback:** Invalid state/ownership or release/audit failure; transaction restores appointment, slot and audit.
- **Idempotency:** Repeated cancellation preserves original cancellation data and does not release/audit twice.

### 10.6 Rescheduling

- **Actor/precondition:** PATIENT owner or ADMIN/RECEPTIONIST; eligible original, no direct successor, usable new slot.
- **Flow/result:** Lock original -> reserve new -> create `SCHEDULED` successor -> original `RESCHEDULED` -> release old -> audit -> `201` with successor `Location`.
- **Errors/rollback:** Any failure returns all appointment, slot, successor and audit state to the original state.
- **Idempotency:** A second direct successor is rejected; chains are possible by rescheduling the latest successor.

### 10.7 Check-in and waiting

- **Actor/precondition:** RECEPTIONIST; exact states `CONFIRMED/null` then `CONFIRMED/CHECK_IN`.
- **Flow/result:** Pessimistic lock -> stage change -> transactional audit -> `200`.
- **Errors/rollback:** Wrong role/state/skip/reversal or audit failure leaves no partial effect.
- **Slot/idempotency:** Slot remains `RESERVED`; repeat of the current stage is successful without duplicate audit.

### 10.8 Start and complete attention

- **Actor/precondition:** Active PROFESSIONAL linked to appointment professional; `WAITING`, then `IN_ATTENTION`.
- **Flow/result:** Lock -> `IN_ATTENTION`; then `FINISHED` + `COMPLETED`; each effective transition audits and returns `200`.
- **Errors/rollback:** Wrong professional/role/state or audit failure leaves no partial effect.
- **Slot/idempotency:** Slot remains consumed as `RESERVED`; current-stage repeat is successful without duplicate audit.

## 11. API baseline

All paths include the configured `/api/v1` context path.

### 11.1 Implemented endpoints

| Method and route | Authorization | Request / response | Main errors |
|---|---|---|---|
| POST `/auth/login` | Public | Email/password -> token response | 400, 401 |
| POST `/auth/refresh` | Public | Refresh token -> rotated token response | 400, 401 |
| POST `/auth/logout` | Authenticated | No body -> 204 | 401 |
| POST/GET `/users` | ADMIN | Create DTO / list DTO | 400, 403, 409 |
| GET `/users/me` | Authenticated | User DTO | 401, 404 |
| GET/PUT `/users/{id}` | ADMIN | User DTO/update DTO | 400, 403, 404, 409 |
| PATCH `/users/{id}/status` | ADMIN | `{enabled}` -> User DTO | 400, 403, 404 |
| POST `/users/{id}/roles` | ADMIN | Role set -> User DTO | 400, 403, 404 |
| POST/GET `/patients` | ADMIN | Create DTO / list DTO | 400, 403, 409 |
| GET `/patients/me` | PATIENT | Patient DTO | 401, 403, 404 |
| GET/PUT `/patients/{id}` | ADMIN | Patient DTO/update DTO | 400, 403, 404, 409 |
| PATCH `/patients/{id}/status` | ADMIN | `INACTIVE` -> Patient DTO | 400, 403, 404 |
| POST `/patients/{id}/user` | ADMIN | User UUID -> Patient DTO | 400, 403, 404, 409 |
| POST/GET `/professionals` | ADMIN | Create DTO / list DTO | 400, 403, 409 |
| GET/PUT `/professionals/{id}` | ADMIN | Professional DTO/update DTO | 400, 403, 404, 409 |
| POST/GET `/agendas` | ADMIN | Create DTO / filtered list | 400, 403, 404 |
| GET/PUT `/agendas/{id}` | ADMIN | Agenda DTO/update DTO | 400, 403, 404 |
| PATCH `/agendas/{id}/status` | ADMIN | `{active}` -> Agenda DTO | 400, 403, 404 |
| GET `/availability` | ADMIN | Optional filters -> list | 400, 403 |
| GET `/availability/{id}` | ADMIN | Slot DTO | 400, 403, 404 |
| POST/GET `/appointments` | PATIENT/ADMIN/RECEPTIONIST | Create DTO -> 201; role-filtered list | 400, 401, 403, 404, 409 |
| GET `/appointments/{id}` | PATIENT own/ADMIN/RECEPTIONIST | Appointment DTO | 400, 401, 403, 404 |
| POST `/appointments/{id}/confirm` | PATIENT own/ADMIN/RECEPTIONIST | No body -> Appointment DTO | 400, 401, 403, 404, 409 |
| POST `/appointments/{id}/cancel` | PATIENT own/ADMIN | No body -> Appointment DTO | 400, 401, 403, 404, 409 |
| POST `/appointments/{id}/reschedule` | PATIENT own/ADMIN | Slot DTO -> 201 successor | 400, 401, 403, 404, 409 |
| POST `/appointments/{id}/check-in` | RECEPTIONIST | No body -> Appointment DTO | 400, 401, 403, 404, 409 |
| POST `/appointments/{id}/waiting` | RECEPTIONIST | No body -> Appointment DTO | 400, 401, 403, 404, 409 |
| POST `/appointments/{id}/start-attention` | Assigned PROFESSIONAL | No body -> Appointment DTO | 400, 401, 403, 404, 409 |
| POST `/appointments/{id}/complete` | Assigned PROFESSIONAL | No body -> Appointment DTO | 400, 401, 403, 404, 409 |

Controllers return DTOs/lists directly, except explicit `ResponseEntity` and `204` operations. A universal success envelope and pagination are not implemented.

### 11.2 Documented or planned but not implemented

- `/auth/register`, `/auth/me` (current profile is `/users/me`).
- Role and permission resources.
- Patient self-update `/patients/me`.
- Specialty/content/public resources.
- Professional status endpoint and professional operational query views.
- Patient/reception availability API.
- Waitlist, priority, notifications, workflow aggregate and dashboard/metrics resources.
- OpenAPI contract file, rate limiting and a universal response envelope.

## 12. Persistence baseline

| Domain | Tables / migration | Current use |
|---|---|---|
| Identity/RBAC | `users`, `roles`, `permissions`, `user_roles`, `role_permissions` / V1 | Entities/repositories/services implemented; no seed migration. |
| Patients | `patients` / V1 | Implemented with optional user FK and `deleted_at`. |
| Professionals/catalog | `professionals`, `specialties`, `professional_specialties` / V1 | Professional entity implemented; specialty/catalog APIs absent. |
| Agenda | `schedules`, `availability_slots` / V1 | Implemented schedules/reads and slot state contracts; no slot generation. |
| Appointments | `appointments` / V1 + V3 | Implemented. V3 adds predecessor FK/unique constraint and active-slot partial unique index. |
| Waitlist | `waitlist_entries` / V1 | Schema only; documented target states differ from V1 and require a migration decision before implementation. |
| Priority | `priority_requests` / V1 | Schema only; ADR-008 concept exists, policy/API absent. |
| Audit | `audit_logs` / V1 | Implemented for seven appointment transition events; IP/user-agent remain null. |
| Refresh | `refresh_tokens` / V2 | Implemented hash, expiry, revocation and user FK. |

Identifiers are UUID. Flyway owns the schema and Hibernate uses `ddl-auto=validate`. V1-V3 are immutable historical migrations for this baseline.

## 13. Security baseline

### Implemented

- Stateless Spring Security; CSRF, HTTP Basic, form login and framework logout disabled.
- Public routes are only POST `/auth/login` and POST `/auth/refresh`; every other route requires authentication.
- HS256 access JWT with `sub=user-id` and `roles`; secret and expirations are external configuration.
- Every JWT request loads the current user and authenticates only enabled, non-deleted users.
- BCrypt password hashing; password hashes are not exposed by API DTOs.
- Random refresh tokens are stored only as SHA-256 hashes and consumed atomically.
- Method-level roles and appointment object ownership.

### Pending or open

- HTTPS is a deployment requirement, not demonstrated by the application repository.
- Rate limiting, access-login audit and refresh retention/cleanup are absent.
- Permission records are mapped to authorities, but current controllers authorize primarily by role.
- Mobile OAuth2 + PKCE from ADR-004 has no implemented mobile client/provider flow.
- There is no production bootstrap decision for initial ADMIN, roles, permissions or catalog data.

## 14. Test baseline

The latest local Maven reports inspected at the baseline cut contain:

| Suite | Reports | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|---:|
| Surefire unit/slice | 27 | 154 | 0 | 0 | 0 |
| Failsafe integration | 8 | 63 | 0 | 0 | 0 |
| Total | 35 | 217 | 0 | 0 | 0 |

Validated evidence includes:

- Unit tests for services, mappers, validation, roles, ownership, lifecycle and contracts.
- MockMvc/controller authorization and error mapping.
- PostgreSQL 16 Testcontainers with Flyway V1-V3 and Hibernate validation.
- Real foreign keys, uniqueness, active-slot reuse and pessimistic locks.
- Appointment reservation, release, lifecycle, audit, rollback and concurrent transitions.
- Refresh-token contention with PostgreSQL blocking evidence and physical rollback after successor failure.
- HTTP JWT cases for active, disabled, missing and soft-deleted users plus invalid and expired tokens.

Not validated by current tests: frontend/mobile behavior, accessibility, usability, load/performance, backup/restore, notifications, waitlist, priority, dashboard, external integrations and production deployment.

## 15. Canonical documentation map

| Document | Authority after this baseline | Use |
|---|---|---|
| `docs/DOMAIN-BASELINE.md` | Cross-cutting current baseline | Current/pending/future/open classification and traceability. |
| Accepted ADRs | Binding by decision topic | Architecture and state decisions. |
| V1-V3 migrations | Binding for persistence | Physical schema and constraints. |
| Module architecture docs | Specialized, conditional | Detail when aligned with code/ADRs/baseline. |
| `docs/requisitos/SRS-HOSPITALPLATFORM.md` and `REGLAS_NEGOCIO.md` | Product requirement proposal | IDs and target behavior; use baseline labels for implementation status. |
| Roadmap/PRD/TRD/App Flow/UI/API/Implementation Plan | Planning and design history | Product intent; not proof of current delivery. |
| AS-IS/TO-BE and BPMN/IDEF0 | Analysis/modeling | AS-IS remains institutionally unvalidated; TO-BE includes planned scope. |

## 16. Document contradictions register

| Document | Declaration | Real state | Action |
|---|---|---|---|
| API Specification | `/auth/register`, `/auth/me`, roles, permissions, specialties, dashboard and other resources appear as API. | Endpoints do not exist; own user route is `/users/me`. | Keep as planned only; reconcile in a dedicated API baseline/OpenAPI task. |
| API Specification | `PUT /users/{id}/roles`, `PUT /patients/me`, filters on users and standard response envelope. | Actual role assignment is POST; no patient self-update/filters/universal envelope. | Correct after contract ownership decisions; do not implement implicitly. |
| API Specification | Availability is part of patient/reception flow. | Current `/availability` endpoints are ADMIN-only. | `OPEN`: approve actors before changing API/security. |
| API Specification | HTTPS, rate limiting and full audit are included. | HTTPS depends on deployment; rate limiting absent; audit is appointment-transition only. | Classify as pending RNF. |
| Professional Architecture | Java entity/repository/service/controller and integrations are future/missing. | Professional module, contract, Agenda and Appointment integrations exist. | Document is stale; update in a focused documentation remediation. |
| Module Contracts Architecture | One paragraph says Professional and Agenda implementations are future. | Both providers are implemented; the same document elsewhere says so. | Remove stale paragraph in focused remediation. |
| Appointment Architecture | Lists `APPOINTMENT_CREATED` as expected. | `AuditEventType` and creation flow do not record it. | `OPEN`: decide whether creation audit is required. |
| ADR-010 | Soft delete application lists users, professionals and specialties. | Patient uses `deleted_at` from V1 and approved patient architecture. | Preserve current implementation; decide whether to amend ADR scope. |
| ADR-005 | TRIAGE manages operational flow. | ADR-007/current code assigns flow to RECEPTIONIST and PROFESSIONAL; TRIAGE has no operation. | Validate final institutional role mapping. |
| Roadmap | Package root `com.integrador.salud.api` and candidate module names. | Code root is `com.hospital.platform` with approved current modules. | Treat roadmap tree as obsolete candidate architecture. |
| Roadmap | ADR-001 through ADR-011 are listed as pending with different topics. | Files ADR-001 through ADR-011 already exist and are accepted. | Renumber future ADR backlog before creating any ADR. |
| Roadmap | Several MVP capabilities are listed as included. | Many are planned/open, not implemented. | Preserve MVP intent but use this baseline for delivery status. |
| PRD/TRD/UI/Test docs | Triaje/clinical attention and some clinical data are current MVP flows. | ADR-007 excludes independent triage and clinical data from current flow. | Mark as future/open; do not extend current state machine. |
| Backend Schema document | Earlier model uses incompatible IDs/state structures. | Migrations use UUID and enum-like checks. | Migration schema remains authoritative; remediate document separately. |
| SRS | All backend RF are labeled proposed/partial from a static audit. | Multiple RF now have executable unit/integration evidence. | Use section 8 of this baseline for current status. |
| SRS/older requirements | Parallel RF numbering exists in PRD and research chapter. | SRS RF-001-RF-033 is internally unique, but semantic equivalence is not one-to-one. | Preserve old IDs as history; do not reuse them as canonical IDs. |
| V1 schema | Waitlist and priority tables exist. | Corresponding Java modules are empty and policies are open. | Do not infer functionality from tables. |

## 17. Scope exclusions status review

| Item reviewed | Classification |
|---|---|
| Triage, no-show, temporal rules, hospital timezone | `OPEN`; not current states/behavior. |
| Waitlist, notifications, priority, dashboard | MVP intent but `OPEN`/not implemented. |
| Automatic slot generation and advanced overlap | `OPEN`; not implemented. |
| Release slot on complete | Closed current decision: **do not release**; any change requires a new decision. |
| Automatic check-in | Not defined; current check-in is manual RECEPTIONIST operation. |
| Operational timestamps per stage | `OPEN`; explicitly absent from ADR-007 current phase. |
| HTTP/IP/user-agent audit | `OPEN`; columns exist, behavior absent. |
| Clinical data, diagnosis, prescription, complete record | `FUTURE`, outside current MVP. |
| Payments and external integrations | `FUTURE`, outside current MVP. |
| CI/CD, SonarQube, JaCoCo, Postman | Engineering backlog; not current functional domain. |

## 18. Decisions still open

| Decision | Impact / module | Why it is open | Recommended stage |
|---|---|---|---|
| Validate institutional AS-IS and product approval | Whole product | No direct MAPRO/interview/observation evidence in repository. | Before institutional claims or workflow expansion. |
| Final role map, especially TRIAGE/reviewer/auditor | Security and future modules | ADR-005, roadmap candidates and current endpoints differ. | Before Priority/Waitlist/operational expansion. |
| Initial ADMIN, role, permission and catalog bootstrap | Users/Catalogs/deployment | Migrations contain no seeds; APIs require an ADMIN to create users. | Next platform-operability design. |
| Auth -> Users module boundary | Auth/Users | Auth uses User entity/repository directly; prior audit left this as pending. | Architecture decision before further auth expansion. |
| Professional-to-user lifecycle and validation | Professionals/Users | Optional FK exists; creation relies on DB FK and no public user contract. | Before professional self-service or broader ownership. |
| Specialty ownership and professional-specialty management | Catalogs/Professionals/Agenda | Schema exists but module/API/rules do not. | Before public availability. |
| Actors allowed to query availability | Agenda/API/Security | Product flows require patient/reception; code allows only ADMIN. | Before frontend or public reservation work. |
| Slot generation, duration, calendar and overlap | Agenda | Only schedules and pre-existing slots are supported. | Agenda increment before production-like booking. |
| Hospital timezone and temporal rules | Appointments/Notifications | Past slot, confirmation, cancellation, reprogramming and no-show windows are undefined. | Domain decision gate before temporal automation. |
| No-show representation and slot effect | Appointments/Agenda | Not a current state/event; roadmap expects capability. | Separate state/event ADR. |
| Waitlist eligibility, selection, fairness, offer expiry and consent | Waitlist/Agenda/Appointments | Tables/design are insufficient for executable behavior. | Waitlist domain decision gate. |
| Priority criteria and authorized reviewer | Priority/Security | ADR-008 defines principle, not institutional policy/role. | Priority domain decision gate. |
| Notification timing, channels, provider and delivery semantics | Notifications | T-7 is only a proposed configurable rule. | Notification architecture decision. |
| Appointment creation and broader audit event inventory | Audit/all modules | Current audit starts at lifecycle transitions; expected creation event is inconsistent. | Audit policy/retention decision. |
| Professional deactivation HTTP capability | Professionals/API | Service method exists but controller endpoint does not. | API contract review. |
| Patient self-registration and self-update fields | Auth/Users/Patients | Current flow is ADMIN-created and linked. | Identity/product decision gate. |
| Public content ownership, sources and disclaimer | Catalogs/Institutional | No approved content model or editor role. | Public portal definition. |
| API envelope, pagination, filters and OpenAPI authority | API/all modules | API Specification overstates uniformity and exposes planned routes. | API contract baseline before frontend. |
| Permission granularity versus role-only annotations | Security | Permission entities/authorities exist but business endpoints use roles. | Security authorization matrix review. |
| Mobile OAuth2 + PKCE realization | Auth/Mobile | ADR-004 accepted; no client/provider flow exists. | Before mobile implementation. |
| Audit retention, access, IP and user-agent | Audit/Security | Columns exist but capture/query/retention policy does not. | Audit hardening stage. |
| Performance, availability, backup and observability targets | Platform | Current SRS numbers are academic targets without accepted baseline. | ETAPA C quality baseline and later staging. |

## 19. Final domain baseline matrix

| Area | State | Evidence | Pending |
|---|---|---|---|
| Scope | 🟡 OPEN | Roadmap, SRS, code inventory | Institutional validation and MVP policy decisions. |
| Actors | 🟡 OPEN | RoleName, controllers, ADR-005/007 | TRIAGE/reviewer/auditor mapping. |
| Roles | 🟡 OPEN | Security annotations and tests | Bootstrap and permission granularity. |
| Modules | 🟢 CLOSED for current backend | Package inventory and code | Planned modules remain non-functional. |
| Dependencies | 🟡 OPEN | Import/contract audit | Auth -> Users decision. |
| Contracts | 🟢 CLOSED for current consumers | Public interfaces and tests | No new contract approved. |
| AppointmentStatus | 🟢 CLOSED | Enum, V1, ADR-007, tests | `NO_SHOW` requires separate decision. |
| FlowStage | 🟢 CLOSED | Enum, V1, ADR-007, tests | No additional stage approved. |
| Business Rules | 🟡 OPEN | RB catalogue and implementation evidence | Temporal, waitlist, priority and content policies. |
| RF | 🟡 OPEN | SRS catalogue plus section 8 | Multiple MVP RF pending/open. |
| RNF | 🟡 OPEN | Security/integration tests and SRS | Metrics, deployment and quality baselines. |
| API | 🟡 OPEN | Controllers versus API Specification | Planned endpoints and contract normalization. |
| Database | 🟢 CLOSED for V1-V3 | Flyway migrations and integration tests | Future modules may require approved migrations. |
| Security | 🟡 OPEN | Security code and tests | Mobile strategy, rate limiting, access audit, bootstrap. |
| Tests | 🟢 CLOSED for implemented backend scope | 217 latest local test results | No evidence for unimplemented clients/modules or NFR targets. |
| Documentation | 🟡 OPEN | Contradictions register | Focused remediations remain; baseline now provides classification. |

## 20. Gate conclusion

The current appointment core domain is sufficiently defined for maintenance and for work that does not depend on unresolved policy. The overall product domain is **not yet fully closed** for unrestricted implementation.

ETAPA C may safely begin as a decision-and-contract stage for one selected next increment. It must first close the relevant open decisions, with highest priority on: availability actors and specialty/catalog ownership; role/bootstrap policy; timezone and temporal rules; and, if selected, the complete waitlist/priority/notification policy. No implementation of those areas should begin merely from their mention in the roadmap or existing V1 tables.
