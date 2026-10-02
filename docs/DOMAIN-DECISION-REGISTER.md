# Domain Decision Register

## 1. Purpose and scope

This register is the decision gate for ETAPA B.1 and records the explicit B.1.1 approvals dated
2026-09-29. It is based on the repository state at commit
`9f651eb48fb6dffdb3959f1d008afbe726f7577d` on branch
`chore/domain-definition-closure`.

It does not approve product behavior by inference. A technical recommendation is an input for
human review, not an accepted business rule. No code, API, schema, migration, security
configuration or test behavior is changed by this document. HOSPITALPLATFORM is an academic case
study; the approval authority is the academic project / responsible course instructor, not Hospital
de Huaycan or its personnel.

The 22 rows in section 18 of `DOMAIN-BASELINE.md` are represented as 23 decisions here because
the compound item "Hospital timezone and temporal rules" is split into two independently
approvable decisions. This split adds no requirement.

### Status vocabulary

| Status | Meaning |
|---|---|
| `CLOSED - APROBADA` | An explicit architectural decision or human approval defines the decision for its stated scope. |
| `PROPOSED - REQUIERE APROBACIÓN` | Evidence supports a bounded recommendation, but a human decision is still required. |
| `OPEN - REQUIERE DECISIÓN HUMANA` | More than one valid policy exists or explicit academic project approval/evidence is missing. |
| `FUTURE` | Not required for the current implemented domain; it needs its own future gate before implementation. |
| `NOT APPLICABLE` | The question does not apply to the current product model. |

### Priority vocabulary

| Priority | Meaning |
|---|---|
| `P0` | Blocks every possible next implementation. |
| `P1` | Blocks one or more identified next increments. |
| `P2` | Can be resolved after the next bounded increment. |
| `P3` | Future capability outside the next increment. |

## 2. Decision inventory after B.1.1

| ID | Decision | Current status | Evidence | Impact |
|---|---|---|---|---|
| DEC-001 | Institutional AS-IS and product approval | `CLOSED` | B.1.1 approves option B: explicit academic/prototype scope with no claim of Hospital de Huaycan approval. | Governs all project claims and future domain decisions. |
| DEC-002 | Final institutional role map | `CLOSED` | B.1.1 approves only `ADMIN`, `PATIENT`, `RECEPTIONIST` and `PROFESSIONAL` for the current case-study scope. | Current security matrix; any additional role requires a future decision. |
| DEC-003 | Initial ADMIN, role, permission and catalog bootstrap | `OPEN` | V1-V3 contain schema only; no production seed/bootstrap; user creation requires ADMIN. | First deployable environment and administrative operability. |
| DEC-004 | Auth to Users module boundary | `PROPOSED` | Auth/Security imports `User`, `UserRepository`, `AuthenticatedUser` and `PlatformUserDetailsService`; no auth-facing public port exists. | Modular coupling and future auth evolution. |
| DEC-005 | Professional-to-user lifecycle | `OPEN` | Optional unique FK `professionals.user_id`; create accepts `userId`; ownership uses `ProfessionalLookupService`; no controlled link lifecycle exists. | Professional authentication, ownership and self-service. |
| DEC-006 | Specialty ownership and professional-specialty policy | `CLOSED` | B.1.1 approves Catalogs as definition owner, Professionals as assignment owner through a public contract, and N:M business cardinality. | Agenda, professional profiles and availability search. |
| DEC-007 | Actors and exposure for availability queries | `CLOSED` | B.1.1 approves sanitized authenticated access for PATIENT/RECEPTIONIST and additional operational detail for ADMIN. | Future availability API design and data minimization. |
| DEC-008 | Slot generation, duration, calendar and overlap | `OPEN` | Schedules and existing slots are implemented; generation and cross-schedule overlap policy are absent. | Production-like agenda population. |
| DEC-009 | Hospital business timezone | `CLOSED` | B.1.1 approves a configurable IANA business zone with initial academic value `America/Lima`; technical UTC remains distinct. | Every future rule that compares civil time. |
| DEC-010 | Appointment and availability temporal rules | `OPEN` | No past-slot, lead-time, confirmation, cancellation, rescheduling, check-in or completion window is implemented. | Temporal validation and automation. |
| DEC-011 | No-show representation and slot effect | `FUTURE` | No enum value, audit event or service behavior exists; roadmap references are not executable rules. | Future appointment lifecycle extension. |
| DEC-012 | Waitlist eligibility, selection, fairness, expiry and consent | `FUTURE` | V1 table exists; module/contracts/services are absent; documentary states are not fully aligned with V1. | Future waitlist workflow and slot offers. |
| DEC-013 | Priority criteria and authorized reviewer | `FUTURE` | V1 table and ADR-008 principles exist; policy, actor mapping and implementation are absent. | Future ambulatory priority review. |
| DEC-014 | Notification timing, channels, provider and delivery semantics | `FUTURE` | No table, provider, template or service; timing/channel references are proposals. | Future asynchronous communication. |
| DEC-015 | Appointment creation and broader audit-event inventory | `PROPOSED` | Seven lifecycle events exist; `APPOINTMENT_CREATED` appears in architecture documentation but not in code. | Audit completeness and compliance claims. |
| DEC-016 | Professional deactivation HTTP capability | `PROPOSED` | Soft-delete service behavior exists, but no controller endpoint exposes it. | Administrative API contract. |
| DEC-017 | Patient self-registration and self-update | `OPEN` | Current user/patient creation and linking are ADMIN operations; PATIENT reads own profile only. | Patient onboarding and identity verification. |
| DEC-018 | Public content ownership, sources and disclaimer | `FUTURE` | Roadmap/SRS mention public content; no model, owner or API exists. | Public portal content governance. |
| DEC-019 | API envelope, pagination, filters and OpenAPI authority | `CLOSED` | B.1.1 approves current controller behavior as the functional contract and future formal OpenAPI as authoritative; no universal envelope/pagination. | Client integration and API compatibility. |
| DEC-020 | Permission granularity versus role-only annotations | `PROPOSED` | Roles and permissions load as authorities, while business endpoints primarily authorize by role. | Fine-grained authorization policy. |
| DEC-021 | Mobile OAuth2 plus PKCE target | `CLOSED` | ADR-004 explicitly selects the target; no mobile implementation exists yet. | Future mobile authentication implementation. |
| DEC-022 | Audit retention, access, IP and user-agent | `OPEN` | V1 columns exist; capture, query authorization and retention policy are absent. | Audit operations, privacy and compliance. |
| DEC-023 | Performance, availability, backup and observability targets | `OPEN` | Current SRS figures are academic targets without accepted baselines or staging evidence. | Release and operational acceptance criteria. |

No inventory item is `NOT APPLICABLE`. Several are `FUTURE`, which means they must not be
implemented from schema or roadmap references alone.

## 3. Decisions closed by current evidence or B.1.1 approval

### 3.1 Current roles and authorities

| Actor/role | Exists in code | Current approved use | Closure state |
|---|---|---|---|
| `ADMIN` | Yes | User, patient, professional and agenda administration; appointment administration. | Closed for current endpoints. |
| `PATIENT` | Yes | Own patient profile and own appointment operations where explicitly authorized. | Closed for current endpoints. |
| `RECEPTIONIST` | Yes | Appointment administration plus check-in and waiting transitions. | Closed for current endpoints. |
| `PROFESSIONAL` | Yes | Start and complete attention only for the assigned active professional. | Closed for current endpoints. |
| `TRIAGE` | Enum and ADR mention only | No current controller grants a business operation. | Explicitly excluded from the current case-study scope by DEC-002. |
| `SYSTEM` | Enum only for business access | No current controller grants a business operation. | No additional operation is approved by DEC-002. |
| Reviewer | Documentary concept only | No enum role or endpoint mapping. | Explicitly excluded from the current scope; a future decision is required. |
| Auditor | Documentary concept only | No enum role or audit-query surface. | Explicitly excluded from the current scope; a future decision is required. |

`UserSecurityMapper` derives `ROLE_<name>` authorities from roles and also loads permission names.
The implemented business authorization annotations remain role-oriented. This is existing behavior,
not approval of a future permission matrix.

### 3.2 Appointment operations

| Operation | Actor | Preconditions and transition | Ownership | Idempotency | Slot | Audit | Implemented |
|---|---|---|---|---|---|---|---|
| Create | PATIENT; ADMIN/RECEPTIONIST | Active patient and active professional derived from a usable slot; creates `SCHEDULED/null`. | PATIENT can create only for own linked patient. | Not idempotent; atomic slot reservation and DB uniqueness prevent duplicate active allocation. | `AVAILABLE -> RESERVED`. | No creation event. See DEC-015. | Yes |
| Confirm | PATIENT; ADMIN/RECEPTIONIST | `SCHEDULED -> CONFIRMED`. | PATIENT only own appointment. | Repeated `CONFIRMED` returns success without duplicate audit. | Remains `RESERVED`. | `APPOINTMENT_CONFIRMED`. | Yes |
| Cancel | PATIENT; ADMIN/RECEPTIONIST | `SCHEDULED/CONFIRMED -> CANCELLED`; actor/time persisted. | PATIENT only own appointment. | Repeated cancellation preserves prior result and side effects. | `RESERVED -> AVAILABLE`. | `APPOINTMENT_CANCELLED`. | Yes |
| Reschedule | PATIENT; ADMIN/RECEPTIONIST | Original `SCHEDULED/CONFIRMED -> RESCHEDULED`; successor starts `SCHEDULED/null`. | PATIENT only own appointment. | Original cannot create a second direct successor. | New slot reserved; old slot released atomically. | `APPOINTMENT_RESCHEDULED`. | Yes |
| Check-in | RECEPTIONIST | `CONFIRMED/null -> CONFIRMED/CHECK_IN`. | Role operation, not patient self-service. | Repeated current stage returns success without duplicate audit. | Remains `RESERVED`. | `APPOINTMENT_CHECKED_IN`. | Yes |
| Waiting | RECEPTIONIST | `CONFIRMED/CHECK_IN -> CONFIRMED/WAITING`. | Role operation. | Repeated current stage returns success without duplicate audit. | Remains `RESERVED`. | `APPOINTMENT_WAITING`. | Yes |
| Start attention | Assigned PROFESSIONAL | `CONFIRMED/WAITING -> CONFIRMED/IN_ATTENTION`. | Active professional must be linked to current user. | Repeated current stage returns success without duplicate audit. | Remains `RESERVED`. | `APPOINTMENT_ATTENTION_STARTED`. | Yes |
| Complete | Assigned PROFESSIONAL | `CONFIRMED/IN_ATTENTION -> COMPLETED/FINISHED`. | Active professional must be linked to current user. | Repeated final state returns success without duplicate audit. | Remains `RESERVED` as consumed capacity. | `APPOINTMENT_COMPLETED`. | Yes |

All implemented transition and audit side effects share the service transaction. Existing rollback
and PostgreSQL concurrency tests are evidence for current behavior; they do not define the open
temporal policies in DEC-010. DEC-009 closes only the business-timezone choice.

### 3.3 Closed state machines

- `AppointmentStatus`: `SCHEDULED`, `CONFIRMED`, `CANCELLED`, `RESCHEDULED`, `COMPLETED`.
- `FlowStage`: `CHECK_IN`, `WAITING`, `IN_ATTENTION`, `FINISHED`; initial stage is `null`.
- Valid operational chain: `CONFIRMED/null -> CHECK_IN -> WAITING -> IN_ATTENTION -> FINISHED/COMPLETED`.
- There is no `FlowStage.COMPLETED` and no approved `AppointmentStatus.NO_SHOW`.
- Slot status is `AVAILABLE`, `RESERVED` or `BLOCKED`.
- A slot is currently usable only when it is `AVAILABLE` and its schedule is active.
- Cancellation and rescheduling release the prior slot; completion retains the slot as `RESERVED`.

### 3.4 Closed modular contracts

| Consumer | Provider | Current contract | Exposed information | Future decision |
|---|---|---|---|---|
| Patients | Users | `UserLookupService`, `CurrentUserService` | Active-user existence and current user ID. | No change approved. |
| Agenda | Professionals | `ProfessionalLookupService` | Active professional existence. | Professional/user lifecycle remains DEC-005. |
| Appointments | Patients | `PatientLookupService`, `PatientReference` | Active patient and ownership reference. | No change approved. |
| Appointments | Professionals | `ProfessionalLookupService` | Active professional and user linkage check. | No change approved. |
| Appointments | Agenda | Reservation/release contracts and `AvailabilitySlotReference` | Atomic slot state plus minimum professional/specialty/date/time data. | DEC-007 closes query actors/exposure conceptually; query contract design remains future work, while generation remains open in DEC-008. |
| Appointments | Audit | `AuditLogService`, `AuditEventType` | Transactional event recording without audit entity/repository exposure. | Event inventory remains DEC-015. |
| Appointments/Audit | Users | `CurrentUserService` | Current actor ID. | No change approved. |
| Auth/Security | Users | Direct entities/repository plus user-details services | Authentication identity, roles/permissions and refresh-token user relation. | Boundary remains DEC-004. |

Appointments has no direct imports to external module repositories or entities. No concrete new
contract is defined or implemented by this register. DEC-006 approves only the future modular
boundary: Professionals will manage professional-specialty assignments through a public contract;
the interface and operations remain ETAPA C design work.

## 4. Decision records

For `CLOSED` records, the options below preserve the B.1 comparison history; only the
**Opción seleccionada** and **Alcance aprobado** fields state the decision in force.

## DEC-001 - Institutional AS-IS and product approval

**Estado:** `CLOSED - APROBADA`

**Prioridad:** `P1`

**Contexto:** The repository describes an academic case study inspired by a fictitious platform in the context of Hospital de Huaycan.

**Evidencia:** `DOMAIN-BASELINE.md`, roadmap, SRS and AS-IS documents; formal B.1.1 project approval dated 2026-09-29.

**Problema:** The project needed an explicit authority and disclaimer so academic design hypotheses could not be presented as official hospital decisions.

**Opciones:**

- **A. Formal institutional validation:** validate flows, actors and terminology with authorized hospital representatives. Advantage: reliable product baseline. Disadvantage: external coordination and lead time.
- **B. Keep an explicit academic/prototype scope:** continue only with synthetic data and label institutional claims as unvalidated. Advantage: engineering can proceed in bounded areas. Disadvantage: no production-readiness claim.
- **C. Hybrid:** validate only the next selected workflow before implementing it. Advantage: incremental. Disadvantage: global inconsistencies may remain longer.

**Opción seleccionada:** Option B - keep the project explicitly within an academic/prototype scope.

**Alcance aprobado:** HOSPITALPLATFORM is an academic case study. Domain rules are project design assumptions governed by the course's academic authority; direct Hospital de Huaycan approval is neither claimed nor required.

**Justificación:** The repository contains no official institutional validation, and the educational objective can be met without attributing requirements or decisions to the real hospital.

**Impacto:** All requirements, architecture documents, API descriptions and future decisions must distinguish academic project approval from real institutional validation.

**Recomendación técnica:** Apply the approved option B throughout the academic/prototype scope. Do not require or claim institutional validation by Hospital de Huaycan.

**Decisión requerida:** None. Closed by explicit human approval for the academic case study.

**Dependencias:** None. This decision becomes the interpretation basis for every other project decision.

**Exclusiones:** No official approval, endorsement, process validation or requirement attribution by Hospital de Huaycan, its executives, medical staff or real users. No production-readiness claim.

**Autoridad de aprobación:** Academic project / responsible course instructor.

**Fecha de aprobación:** 2026-09-29.

**Aclaración institucional:** This decision represents approval for the academic case study only and does not represent institutional approval by Hospital de Huaycan.

**Bloquea:** It no longer blocks academic domain design. Official institutional or production claims remain prohibited unless separately validated in the future.

**Etapa recomendada:** Applies immediately as the governing scope for ETAPA C and every later academic phase.

## DEC-002 - Final institutional role map

**Estado:** `CLOSED - APROBADA`

**Prioridad:** `P1`

**Contexto:** Four roles have current endpoint behavior; additional enum values and documentary actor mentions have no approved current access surface.

**Evidencia:** `RoleName`, method-security annotations, security tests, ADR-005 and ADR-007.

**Problema:** Documentary role names and enum values must not silently grant operations outside the approved current scope.

**Opciones:**

- **A. Preserve the four current human roles:** map future review/audit capabilities onto separately approved permissions later. Advantage: smallest security surface. Disadvantage: may not match institutional staffing.
- **B. Activate TRIAGE as a distinct human role:** define exact operations and tests first. Advantage: clearer operational separation. Disadvantage: new authorization matrix and provisioning obligations.
- **C. Introduce dedicated reviewer/auditor roles:** only after priority/audit use cases are approved. Advantage: stronger segregation. Disadvantage: more governance and bootstrap complexity.

**Opción seleccionada:** Option A - preserve `PATIENT`, `RECEPTIONIST`, `PROFESSIONAL` and `ADMIN` as the four roles used by the current case-study scope.

**Alcance aprobado:** Current role ownership and authorization remain exactly as implemented. `TRIAGE`, reviewer, auditor and any other additional institutional role receive no operation from this decision.

**Justificación:** The four current roles have consistent controller, service, ownership and test evidence. Additional actors lack a complete approved use case.

**Impacto:** ETAPA C must design availability using only the four approved roles. Any future role requires a new explicit decision before API or security changes.

**Recomendación técnica:** Keep current endpoint roles unchanged; decide extra roles per approved use case rather than granting broad access from the enum alone.

**Decisión requerida:** None. Closed by explicit human approval for the current academic scope.

**Dependencias:** DEC-001, now closed. Future priority or audit-reader work still requires separate DEC-013/DEC-022 decisions.

**Exclusiones:** No TRIAGE, reviewer, auditor or new role operation; no role seed, endpoint, permission or security change.

**Autoridad de aprobación:** Academic project / responsible course instructor.

**Fecha de aprobación:** 2026-09-29.

**Aclaración institucional:** This role map is approved only for the academic case study and is not an official Hospital de Huaycan staffing or authorization model.

**Bloquea:** It no longer blocks the current availability-policy design. Any increment requiring an excluded role remains blocked by a new decision.

**Etapa recomendada:** Use unchanged in ETAPA C; revisit only for a separately approved new-role increment.

## DEC-003 - Initial ADMIN, roles, permissions and catalog bootstrap

**Estado:** `OPEN - REQUIERE DECISIÓN HUMANA`

**Prioridad:** `P1`

**Contexto:** Administrative APIs require an existing ADMIN, while V1-V3 contain no production seed data.

**Evidencia:** Migrations, `UserController`, `UserService`, role repositories and test-only fixtures.

**Problema:** A clean deployment has no documented, secure path to create the first administrative identity or required reference data.

**Opciones:**

- **A. Operations-managed provisioning command/runbook:** one controlled, audited bootstrap outside public HTTP. Advantage: no permanent bootstrap endpoint. Disadvantage: operational tooling is required.
- **B. One-time startup bootstrap from secret-managed environment inputs:** disabled after success. Advantage: automation-friendly. Disadvantage: lifecycle, retry and secret-handling risks.
- **C. Versioned seed migration:** deterministic roles/permissions/catalogs, but not a human password. Advantage: repeatable reference data. Disadvantage: environment-specific identity must still be provisioned and immutable seeds need careful evolution.

**Impacto:** Deployability, recovery, security and role/catalog consistency.

**Recomendación técnica:** Separate deterministic reference-data seeding from first-ADMIN identity provisioning; prefer an operations-controlled one-time mechanism for the identity.

**Decisión requerida:** `APROBACIÓN HUMANA` by the academic project authority, informed by security and operations concerns.

**Dependencias:** DEC-002, DEC-006 and DEC-020 for the data sets to provision.

**Bloquea:** A usable clean deployment; does not block isolated domain design.

**Etapa recomendada:** Platform operability increment before any pilot environment.

## DEC-004 - Auth to Users module boundary

**Estado:** `PROPOSED - REQUIERE APROBACIÓN`

**Prioridad:** `P2`

**Contexto:** Auth/Security must load credentials, account state, roles and permissions and persist a refresh-token relation to users.

**Evidencia:** `AuthService`, `RefreshTokenService`, `RefreshToken`, `JwtAuthenticationFilter`, `SecurityConfiguration` and `PlatformUserDetailsService`.

**Problema:** The current direct dependency is functional but is an exception to public-contract-only communication used by business modules.

**Opciones:**

- **A. Document Auth/Security to Users as an allowed foundational dependency.** Advantage: no speculative refactor. Disadvantage: stronger module coupling remains.
- **B. Add an authentication identity port owned by Users.** Advantage: hides repository/entity reads. Disadvantage: refresh-token JPA relation still requires a persistence decision and can lead to an anemic wrapper.
- **C. Move security identity ownership to a dedicated bounded core.** Advantage: explicit ownership. Disadvantage: broad redesign beyond current need.

**Impacto:** Module boundaries, test design and future auth changes; no current HTTP behavior requires change.

**Recomendación técnica:** Formally allow the current dependency for the existing monolith, then reconsider option B only before substantial Auth expansion.

**Decisión requerida:** `APROBACIÓN HUMANA` by architecture.

**Dependencias:** None for current behavior; DEC-020 for future authorization evolution.

**Bloquea:** Further auth redesign, not the next unrelated domain increment.

**Etapa recomendada:** Architecture decision before new identity providers or auth flows.

## DEC-005 - Professional-to-user lifecycle

**Estado:** `OPEN - REQUIERE DECISIÓN HUMANA`

**Prioridad:** `P1`

**Contexto:** A professional may currently have a nullable, unique `user_id`; ownership succeeds only when the active professional is linked to the authenticated user.

**Evidencia:** V1 FK/unique constraint, `Professional`, create/update DTOs, `ProfessionalService`, `ProfessionalLookupService`, appointment ownership tests.

**Problema:** The repository does not define when linkage becomes mandatory, who may link/unlink, or how account state and professional state interact.

**Opciones:**

- **A. Optional administrative link:** professional records can exist before a user account; ADMIN links once through an approved operation. Advantage: fits staffing setup. Disadvantage: unlinked professionals cannot perform owned operations.
- **B. Mandatory user at professional creation:** enforce one atomic onboarding flow. Advantage: simple ownership invariant. Disadvantage: couples HR/profile setup to identity provisioning.
- **C. Separate invitation/activation lifecycle:** create professional, invite user, activate link after verification. Advantage: robust onboarding. Disadvantage: new state, notifications and security design.

**Impacto:** Professional self-service, appointment ownership, deactivation and account recovery.

**Recomendación técnica:** Option A is the smallest compatible policy, with explicit one-time/relink rules still requiring approval.

**Decisión requerida:** `APROBACIÓN HUMANA`.

**Dependencias:** DEC-002 and DEC-003; option C also depends on DEC-014.

**Bloquea:** Professional self-service and broader professional-owned APIs.

**Etapa recomendada:** Professional identity decision gate.

## DEC-006 - Specialty ownership and professional-specialty policy

**Estado:** `CLOSED - APROBADA`

**Prioridad:** `P1`

**Contexto:** PostgreSQL models a specialty catalog and a professional-specialty join table, while schedules require one specialty ID.

**Evidencia:** V1 `specialties`, `professional_specialties`, `schedules.specialty_id`; Agenda currently relies on FK integrity only.

**Problema:** The physical N:M capability required an explicit business interpretation and modular owner before specialty behavior could be designed.

**Opciones:**

- **A. Catalogs owns specialties; ADMIN manages catalog and professional assignments.** Advantage: explicit boundary and governance. Disadvantage: adds catalog APIs and assignment workflow.
- **B. Professionals owns assignments while Catalogs owns definitions.** Advantage: clear split. Disadvantage: requires a public specialty contract and cross-module policy.
- **C. Keep specialty IDs as database-managed reference data for the next internal increment.** Advantage: minimal. Disadvantage: no supported operational management or public semantics.

**Opción seleccionada:** Option B - Catalogs owns specialty definitions; Professionals owns professional-specialty assignments through a public contract.

**Alcance aprobado:** The academic business model allows one specialty to be associated with multiple professionals and one professional with multiple specialties. The existing N:M schema is compatible with this policy.

**Justificación:** Option B preserves modular ownership while matching the existing `specialties` and `professional_specialties` persistence model.

**Impacto:** ETAPA C may design catalog definitions, assignment contracts and specialty-aware availability without importing external repositories/entities.

**Recomendación técnica:** Apply the approved option B: Catalogs owns definitions and Professionals owns N:M assignments through a future public contract. The business cardinality is approved for the academic case study; the join table alone was not its authority.

**Decisión requerida:** None. Closed by explicit human approval for the academic case study.

**Dependencias:** DEC-001 and DEC-002 are closed. DEC-003 remains open only for future operational seeding/bootstrap, not for conceptual ownership.

**Exclusiones:** No Catalogs or Professionals implementation, no public contract signature, no endpoint, no entity/repository change, no seed and no migration are approved by this decision.

**Autoridad de aprobación:** Academic project / responsible course instructor.

**Fecha de aprobación:** 2026-09-29.

**Aclaración institucional:** This ownership and N:M policy is a design assumption approved for the academic case study, not an official Hospital de Huaycan policy.

**Bloquea:** It no longer blocks conceptual specialty/availability design. Concrete contract and API implementation require ETAPA C design and later implementation authorization.

**Etapa recomendada:** ETAPA C - Agenda Availability and Specialty Policy design/contracts.

## DEC-007 - Availability-query actors and exposed data

**Estado:** `CLOSED - APROBADA`

**Prioridad:** `P1`

**Contexto:** Agenda exposes detailed availability only to ADMIN, while booking flows require patients/reception staff to discover usable slots.

**Evidencia:** `AgendaController`, `AgendaService`, controller authorization tests, API specification and app flow.

**Problema:** The target actors and disclosure boundary needed approval before a patient/reception availability contract could be designed.

**Opciones:**

- **A. Authenticated PATIENT and RECEPTIONIST query a sanitized availability view; ADMIN keeps operational detail.** Advantage: least disclosure with usable booking. Disadvantage: requires a separate API contract/DTO decision.
- **B. Public anonymous sanitized availability.** Advantage: low-friction discovery. Disadvantage: scraping, privacy/operational exposure and abuse controls.
- **C. Keep ADMIN-only and let staff perform all booking.** Advantage: no new exposure. Disadvantage: conflicts with intended patient self-service.

**Opción seleccionada:** Option A - authenticated PATIENT and RECEPTIONIST receive a sanitized availability view; ADMIN may receive additional operational detail.

**Alcance aprobado:** Availability discovery for future reservation is allowed for authenticated PATIENT and RECEPTIONIST. ADMIN retains operational consultation. PROFESSIONAL receives no additional permission from this decision. Internal implementation details and unnecessary data remain excluded.

**Justificación:** This supports the academic booking flow while preserving authentication, least disclosure and the current role map.

**Impacto:** ETAPA C may define separate sanitized and operational response contracts, field minimization, filters and authorization behavior.

**Recomendación técnica:** Apply the approved option A. Define the sanitized fields, operational detail and filters during ETAPA C contract design; this approval does not add an ownership or filtering rule.

**Decisión requerida:** None. Closed by explicit human approval for the academic case study.

**Dependencias:** DEC-002, DEC-006, DEC-009 and DEC-019 are closed by B.1.1.

**Exclusiones:** No endpoint, DTO, controller, authorization annotation, filter or frontend is implemented; no anonymous access and no automatic PROFESSIONAL access are approved.

**Autoridad de aprobación:** Academic project / responsible course instructor.

**Fecha de aprobación:** 2026-09-29.

**Aclaración institucional:** These actors and exposure rules are approved for the academic case study only and do not represent an official Hospital de Huaycan access policy.

**Bloquea:** It no longer blocks conceptual availability API design. Functional API changes remain outside B.1.1.

**Etapa recomendada:** ETAPA C - availability API and contract design, without implementation.

## DEC-008 - Slot generation, duration, calendar and overlap

**Estado:** `OPEN - REQUIERE DECISIÓN HUMANA`

**Prioridad:** `P1`

**Contexto:** Current Agenda manages schedules and reads pre-existing discrete slots; reservation/release are atomic.

**Evidencia:** ADR-006, V1 constraints, Agenda entities/repositories/contracts/tests; no slot creation/generation service or endpoint.

**Problema:** There is no approved source of working calendars, slot duration, generation horizon, regeneration behavior or overlap rule.

**Opciones:**

- **A. Deterministic batch generation from active schedules and an approved calendar.** Advantage: materialized slots fit current reservation model and can be idempotent. Disadvantage: requires duration, horizon, timezone and overlap decisions.
- **B. Administrative import/manual slot creation.** Advantage: fastest controlled operation. Disadvantage: error-prone and hard to scale.
- **C. On-demand virtual slots.** Advantage: less precomputation. Disadvantage: conflicts with current persisted-slot reservation design and would be a larger redesign.

**Impacto:** Agenda population, uniqueness, concurrency and operational maintenance.

**Recomendación técnica:** Option A after its policy inputs are approved; preserve discrete persisted slots and atomic status updates.

**Decisión requerida:** `APROBACIÓN HUMANA`.

**Dependencias:** DEC-006 and DEC-009; any booking window depends on DEC-010.

**Bloquea:** Production-like availability generation.

**Etapa recomendada:** Agenda generation design after timezone/catalog closure.

## DEC-009 - Hospital business timezone

**Estado:** `CLOSED - APROBADA`

**Prioridad:** `P1`

**Contexto:** Technical persistence is configured for UTC, but schedules and slots represent local civil date/time and no business zone is declared.

**Evidencia:** `hibernate.jdbc.time_zone=UTC`, Java date/time types, SQL `DATE`, `TIME` and `TIMESTAMP` columns, no approved timezone policy.

**Problema:** UTC transport/storage settings did not define the case study's civil-time interpretation.

**Opciones:**

- **A. Configurable IANA hospital zone, initially approved as `America/Lima`.** Advantage: explicit domain semantics and future portability. Disadvantage: configuration and conversion rules are required.
- **B. Fixed `America/Lima` business zone.** Advantage: simple for one institution. Disadvantage: hard-coded deployment assumption.
- **C. UTC as business time.** Advantage: technically uniform. Disadvantage: likely misrepresents local schedules and would require explicit project approval.

**Opción seleccionada:** Option A - a configurable IANA business timezone, with initial academic value `America/Lima`.

**Alcance aprobado:** Future domain rules that need civil time must resolve it through the explicit configurable business timezone. Technical UTC remains a separate persistence/transport concern.

**Justificación:** An IANA identifier makes civil-time semantics explicit without hard-coding infrastructure timezone behavior.

**Impacto:** ETAPA C may use `America/Lima` as the initial design value and must keep future temporal logic/configuration explicit and replaceable.

**Recomendación técnica:** Option A; B.1.1 explicitly approves `America/Lima` as the initial academic value. Do not infer future values from server timezone or geography alone.

**Decisión requerida:** None. Closed by explicit human approval for the academic case study.

**Dependencias:** DEC-001, now closed. DEC-010 remains open for actual temporal windows.

**Exclusiones:** No configuration, clock abstraction, conversion logic, temporal validation, notification timing or no-show behavior is implemented by this decision.

**Autoridad de aprobación:** Academic project / responsible course instructor.

**Fecha de aprobación:** 2026-09-29.

**Aclaración institucional:** `America/Lima` is an academic project decision and must not be represented as an official timezone decision made by Hospital de Huaycan.

**Bloquea:** The timezone choice no longer blocks ETAPA C design. DEC-008 and DEC-010 remain independently open for generation and temporal rules.

**Etapa recomendada:** Apply as a design constraint in ETAPA C; implementation requires a later authorized phase.

## DEC-010 - Temporal rules for appointments and availability

**Estado:** `OPEN - REQUIERE DECISIÓN HUMANA`

**Prioridad:** `P1`

**Contexto:** Current operations validate state, actor and ownership but intentionally do not apply appointment-time windows.

**Evidencia:** ADR-007, current services/tests and baseline RB-008/RB-009.

**Problema:** Past slots and minimum/maximum windows for reservation, confirmation, cancellation, rescheduling, check-in, attention and completion are undefined.

**Opciones:**

- **A. Approve independent configurable windows per operation.** Advantage: explicit and adaptable. Disadvantage: larger policy/configuration/test matrix.
- **B. Approve a minimal first set (past-slot rejection plus cancellation/rescheduling cutoff).** Advantage: bounded risk reduction. Disadvantage: remaining operations stay undefined.
- **C. Preserve state-only validation for the next internal increment.** Advantage: no invented policy. Disadvantage: not production-ready for time-sensitive operation.

**Impacto:** Service validation, errors, UI messaging, tests and future automation.

**Recomendación técnica:** Decide the minimal set in option B first, then extend only through explicit academic project decisions.

**Decisión requerida:** `APROBACIÓN HUMANA` for each window and clock-boundary rule.

**Dependencias:** DEC-009 and DEC-001.

**Bloquea:** Temporal automation, production booking rules and DEC-011/DEC-014 timing.

**Etapa recomendada:** Appointment temporal-policy ADR.

## DEC-011 - No-show representation and slot effect

**Estado:** `FUTURE`

**Prioridad:** `P3`

**Contexto:** The roadmap mentions no-show capability, but the current state machines are deliberately closed without it.

**Evidencia:** Current enums, V1/V3 constraints, ADR-007, services/tests and absence of an audit event.

**Problema:** Actor, trigger time, eligible prior state, representation, audit and slot semantics are undefined.

**Opciones:**

- **A. New terminal `AppointmentStatus.NO_SHOW`.** Advantage: explicit reporting. Disadvantage: schema/enums/API migration and transition policy.
- **B. Separate outcome/event while retaining appointment status.** Advantage: avoids expanding status. Disadvantage: requires a new persisted concept and reporting semantics.
- **C. Exclude from current MVP.** Advantage: protects the closed state machine. Disadvantage: no automated no-show reporting.

**Impacto:** Appointments, Agenda, Audit, metrics and potentially notifications.

**Recomendación técnica:** Keep option C now; run a dedicated ADR before choosing A or B.

**Decisión requerida:** `APROBACIÓN HUMANA` in a future gate.

**Dependencias:** DEC-009, DEC-010, DEC-015 and DEC-023.

**Bloquea:** Only a future no-show increment.

**Etapa recomendada:** Future lifecycle extension, not ETAPA C.

## DEC-012 - Waitlist eligibility, selection, fairness, offer expiry and consent

**Estado:** `FUTURE`

**Prioridad:** `P3`

**Contexto:** V1 has `waitlist_entries`, but no functional module, offer persistence or approved executable policy.

**Evidencia:** V1, roadmap/SRS, baseline RB-012 to RB-014 and empty module structure.

**Problema:** A table does not define eligibility, ranking, consent, fairness, expiry, retry or atomic acceptance.

**Opciones:**

- **A. FIFO within specialty/date constraints.** Advantage: simple and auditable. Disadvantage: may ignore clinical/operational priorities.
- **B. Approved scored ordering.** Advantage: flexible. Disadvantage: policy transparency and fairness risk.
- **C. Staff-curated offers.** Advantage: operational control. Disadvantage: manual workload and consistency risk.

**Impacto:** Waitlist, Agenda, Appointments, Notifications, Audit and potentially new persistence.

**Recomendación técnica:** Do not select an algorithm in B.1; create a dedicated waitlist decision gate with explicit academic project approval and atomic acceptance design.

**Decisión requerida:** `APROBACIÓN HUMANA` in a future gate.

**Dependencias:** DEC-001, DEC-002, DEC-006, DEC-009, DEC-010, DEC-013 and DEC-014.

**Bloquea:** Only waitlist implementation.

**Etapa recomendada:** Future waitlist domain phase.

## DEC-013 - Priority criteria and authorized reviewer

**Estado:** `FUTURE`

**Prioridad:** `P3`

**Contexto:** ADR-008 defines ambulatory, manual, non-emergency and audited priority principles, but not criteria or reviewer identity.

**Evidencia:** ADR-008, V1 `priority_requests`; no entity/service/controller or role mapping.

**Problema:** The system cannot infer clinical criteria or authorize `TRIAGE`, reviewer or another actor without an explicit academic case-study policy.

**Opciones:**

- **A. Dedicated reviewer role with approved criteria.** Advantage: clear segregation. Disadvantage: new role governance.
- **B. Existing PROFESSIONAL with conflict-of-interest restrictions.** Advantage: fewer roles. Disadvantage: ownership and review independence must be defined.
- **C. Staff-only external process, no software workflow yet.** Advantage: avoids premature clinical logic. Disadvantage: no digital trace beyond generic audit.

**Impacto:** Priority, Security, Appointments, Audit and Waitlist.

**Recomendación técnica:** Keep option C until criteria and reviewer authority are explicitly approved for the academic case study.

**Decisión requerida:** `APROBACIÓN HUMANA` by the academic project authority; no real clinical validation is implied.

**Dependencias:** DEC-001, DEC-002, DEC-015 and DEC-022.

**Bloquea:** Only priority and priority-aware waitlist behavior.

**Etapa recomendada:** Future priority domain gate.

## DEC-014 - Notification timing, channels, provider and delivery semantics

**Estado:** `FUTURE`

**Prioridad:** `P3`

**Contexto:** Notification concepts exist in plans, but no executable notification domain exists.

**Evidencia:** Roadmap/SRS/RB-021; empty module; no provider, template or table.

**Problema:** Email, SMS, WhatsApp, push, reminder timing, consent, retry and delivery guarantees are undecided.

**Opciones:**

- **A. Transactional outbox plus asynchronous providers.** Advantage: reliable decoupling. Disadvantage: new persistence and worker operations.
- **B. Best-effort asynchronous event handler.** Advantage: smaller initial scope. Disadvantage: weaker delivery guarantees.
- **C. No automated notifications in the current MVP.** Advantage: no external dependency. Disadvantage: manual communication remains.

**Impacto:** Notifications, Appointments, Waitlist, privacy, operations and external cost.

**Recomendación técnica:** Keep option C until channel/consent policy is approved; then design delivery independently so provider failure cannot roll back a valid appointment.

**Decisión requerida:** `APROBACIÓN HUMANA` in a future architecture phase.

**Dependencias:** DEC-009, DEC-010, DEC-017 and possibly DEC-012.

**Bloquea:** Only automated communication and offer delivery.

**Etapa recomendada:** Future notifications architecture phase.

## DEC-015 - Appointment creation and broader audit-event inventory

**Estado:** `PROPOSED - REQUIERE APROBACIÓN`

**Prioridad:** `P2`

**Contexto:** Seven appointment transition events are implemented transactionally; appointment creation is not audited despite one architecture-document reference.

**Evidencia:** `AuditEventType`, `AppointmentService`, audit integration/rollback tests, V1 `audit_logs`, appointment architecture document.

**Problema:** The authoritative event inventory and required old/new values are not formally approved across modules.

**Opciones:**

- **A. Add `APPOINTMENT_CREATED` to the required inventory in a future implementation.** Advantage: complete appointment provenance. Disadvantage: code/test change and retention volume.
- **B. Treat persistence timestamps as sufficient for creation.** Advantage: no new event. Disadvantage: actor/context and explicit action are absent.
- **C. Define a risk-based cross-module audit matrix before adding events.** Advantage: coherent policy. Disadvantage: delays isolated creation-event work.

**Impacto:** Audit completeness, compliance claims, storage and reporting.

**Recomendación técnica:** Option C, with appointment creation evaluated as the first explicit gap.

**Decisión requerida:** `APROBACIÓN HUMANA`.

**Dependencias:** DEC-022 and the academic-scope constraints closed in DEC-001.

**Bloquea:** Claims of complete business-action auditing; not current appointment behavior.

**Etapa recomendada:** Audit policy phase.

## DEC-016 - Professional deactivation HTTP capability

**Estado:** `PROPOSED - REQUIERE APROBACIÓN`

**Prioridad:** `P2`

**Contexto:** The professional service can soft-delete/deactivate, but the controller exposes only create/read/update.

**Evidencia:** `ProfessionalService`, service tests, `ProfessionalController`, V1 `deleted_at`.

**Problema:** It is unclear whether deactivation is intentionally internal or an omitted ADMIN operation, and effects on future schedules are not defined.

**Opciones:**

- **A. Expose an ADMIN status/deactivation operation later.** Advantage: completes administration. Disadvantage: requires effects on schedules and future appointments to be approved.
- **B. Keep deactivation as an internal service capability.** Advantage: no incomplete HTTP behavior. Disadvantage: no supported operational path.
- **C. Use a DELETE-style soft-delete API.** Advantage: common REST semantics. Disadvantage: restoration/status semantics become less explicit.

**Impacto:** Professionals API, Agenda consistency and audit policy.

**Recomendación técnica:** Option A only after defining effects; do not expose the existing method in isolation.

**Decisión requerida:** `APROBACIÓN HUMANA`.

**Dependencias:** DEC-005, DEC-008 and DEC-015.

**Bloquea:** Supported professional deactivation, not current appointment operations.

**Etapa recomendada:** Professional administration API review.

## DEC-017 - Patient self-registration and self-update

**Estado:** `OPEN - REQUIERE DECISIÓN HUMANA`

**Prioridad:** `P1`

**Contexto:** ADMIN currently creates users and patients and links them; PATIENT can read only the linked own profile.

**Evidencia:** User/Patient controllers and services, ownership contracts/tests, app flow and SRS.

**Problema:** Identity proof, duplicate resolution, allowed fields, consent and atomic user-patient creation are undefined.

**Opciones:**

- **A. Staff-assisted onboarding only.** Advantage: strongest control with current model. Disadvantage: no self-service.
- **B. Patient account self-registration followed by staff verification/linking.** Advantage: bounded self-service. Disadvantage: pending-account lifecycle and abuse controls.
- **C. Fully automatic user plus patient profile creation.** Advantage: best user convenience. Disadvantage: highest identity, duplicate and privacy risk.

**Impacto:** Auth, Users, Patients, API, privacy and support operations.

**Recomendación técnica:** Option B is safer than full automation, but identity assumptions require explicit academic project approval.

**Decisión requerida:** `APROBACIÓN HUMANA`.

**Dependencias:** DEC-001, DEC-002, DEC-003, DEC-019 and possibly DEC-014.

**Bloquea:** Patient self-onboarding and profile editing.

**Etapa recomendada:** Identity/onboarding domain gate.

## DEC-018 - Public content ownership, sources and disclaimer

**Estado:** `FUTURE`

**Prioridad:** `P3`

**Contexto:** Product documents mention case-study institutional/public content, but no content model or editor workflow exists.

**Evidencia:** Roadmap, SRS and UI/UX brief only.

**Problema:** Content authority, versioning, source verification, disclaimer and publication approval are undefined.

**Opciones:**

- **A. Versioned content managed by a project-authorized editor.** Advantage: governance and traceability. Disadvantage: new module/workflow.
- **B. Static deployment-managed content.** Advantage: simple. Disadvantage: slower updates and limited editorial audit.
- **C. Exclude public content from current MVP.** Advantage: protects scope. Disadvantage: public portal remains limited.

**Impacto:** Public portal, content governance, security and audit.

**Recomendación técnica:** Option C for the next backend increment.

**Decisión requerida:** `APROBACIÓN HUMANA` before public-content implementation.

**Dependencias:** DEC-001, DEC-002 and DEC-015.

**Bloquea:** Only a public content increment.

**Etapa recomendada:** Future public-portal phase.

## DEC-019 - API envelope, pagination, filters and OpenAPI authority

**Estado:** `CLOSED - APROBADA`

**Prioridad:** `P1`

**Contexto:** Current controllers return direct DTOs/lists, while API documentation includes broader and sometimes planned behavior.

**Evidencia:** Controllers/DTOs/tests, `06-API-SPECIFICATION.md`, springdoc dependency/runtime documentation.

**Problema:** The project needed an authoritative contract hierarchy and compatibility rule before future frontend/API design.

**Opciones:**

- **A. Current controller behavior is canonical; document direct DTO/list responses and add pagination only where approved.** Advantage: no breaking redesign. Disadvantage: heterogeneous response shapes may remain.
- **B. Introduce a universal envelope and pagination baseline.** Advantage: uniform clients/metadata. Disadvantage: broad breaking API change.
- **C. Treat generated OpenAPI from code as authority and maintain narrative docs as explanatory.** Advantage: reduces drift. Disadvantage: requires annotation/schema quality governance.

**Opción seleccionada:** Combined options A and C - current controller behavior is the functional contract; formally incorporated OpenAPI will become the future authoritative API source.

**Alcance aprobado:** Narrative documentation is complementary and must not invent endpoints, requests or responses. No universal envelope or pagination is introduced. Pagination is decided per resource when justified by its use case, with explicit compatibility for implemented behavior.

**Justificación:** This preserves working contracts, prevents documentary drift and avoids a broad breaking redesign before a concrete client need exists.

**Impacto:** ETAPA C must design availability contracts from actual controller/API conventions and identify any future OpenAPI incorporation explicitly.

**Recomendación técnica:** Apply the approved combination of A and C: preserve implemented controller contracts; formally incorporated OpenAPI becomes authoritative in a later phase. Decide pagination per resource when required.

**Decisión requerida:** None. Closed by explicit human approval for the academic case study.

**Dependencias:** DEC-007 is closed for availability actors/exposure. Resource-specific filters and pagination still require concrete ETAPA C design.

**Exclusiones:** No universal envelope, universal pagination, new endpoint, DTO, OpenAPI artifact or controller change is approved or implemented in B.1.1.

**Autoridad de aprobación:** Academic project / responsible course instructor.

**Fecha de aprobación:** 2026-09-29.

**Aclaración institucional:** This API governance decision belongs to the academic project and does not represent an official Hospital de Huaycan integration standard.

**Bloquea:** It no longer blocks ETAPA C API design. Functional API implementation and formal OpenAPI incorporation require later authorization.

**Etapa recomendada:** ETAPA C contract design, followed by a separately authorized implementation phase.

## DEC-020 - Permission granularity versus role-only annotations

**Estado:** `PROPOSED - REQUIERE APROBACIÓN`

**Prioridad:** `P2`

**Contexto:** The identity model loads role and permission authorities, but current business methods mostly use `hasRole`/`hasAnyRole`.

**Evidencia:** V1 role/permission tables, `UserSecurityMapper`, method-security annotations and authorization tests.

**Problema:** There is no approved permission catalog or policy for mixing role and permission checks.

**Opciones:**

- **A. Keep role-based endpoint authorization for the current MVP.** Advantage: tested and understandable. Disadvantage: coarse-grained.
- **B. Define permissions per use case and map roles to them.** Advantage: flexible least privilege. Disadvantage: larger bootstrap, governance and test matrix.
- **C. Hybrid: roles for broad entry, permissions for high-risk operations only.** Advantage: incremental. Disadvantage: mixed semantics require clear conventions.

**Impacto:** Security annotations, provisioning, tests and audit.

**Recomendación técnica:** Preserve A until an explicit matrix justifies C; do not introduce isolated permission strings.

**Decisión requerida:** `APROBACIÓN HUMANA`.

**Dependencias:** DEC-002 and DEC-003.

**Bloquea:** Fine-grained authorization, not current role-protected behavior.

**Etapa recomendada:** Security authorization matrix review.

## DEC-021 - Mobile OAuth2 plus PKCE target

**Estado:** `CLOSED - APROBADA`

**Prioridad:** `P3`

**Contexto:** The mobile authentication target was selected architecturally but has no implementation in the current backend/mobile scope.

**Evidencia:** ADR-004; current backend remains JWT access/refresh authentication.

**Problema:** None at the decision level; provider/client realization remains future work.

**Opciones:** Not reopened by B.1.

**Impacto:** Future mobile client and identity-provider integration only.

**Recomendación técnica:** Preserve the ADR; create a separate implementation design when mobile work starts.

**Decisión requerida:** None unless the architecture is intentionally revisited.

**Dependencias:** DEC-001, DEC-003 and DEC-023 before production rollout.

**Bloquea:** Mobile auth implementation only.

**Etapa recomendada:** Future mobile architecture phase.

## DEC-022 - Audit retention, access, IP and user-agent

**Estado:** `OPEN - REQUIERE DECISIÓN HUMANA`

**Prioridad:** `P2`

**Contexto:** Audit logs persist actor/action/entity and JSON old/new values; V1 has IP/user-agent columns that are not populated.

**Evidencia:** V1 `audit_logs`, ADR-011, Audit module and integration tests.

**Problema:** Retention period, authorized readers, export, privacy controls and request-metadata capture are not defined.

**Opciones:**

- **A. Application-managed retention/access policy and request metadata.** Advantage: integrated controls. Disadvantage: larger security/compliance surface.
- **B. Database/operations-managed retention with no user-facing query API.** Advantage: smaller application surface. Disadvantage: operational dependency and limited product access.
- **C. Hybrid:** restricted application query plus infrastructure archival/retention. Advantage: balanced operations. Disadvantage: most design work.

**Impacto:** Privacy, compliance, storage, authorization and incident response.

**Recomendación técnica:** Approve academic privacy/retention assumptions first; any real deployment would additionally require applicable legal and institutional review.

**Decisión requerida:** `APROBACIÓN HUMANA`.

**Dependencias:** DEC-001, DEC-002, DEC-015 and DEC-020.

**Bloquea:** Audit-reader API, retention automation and complete compliance claims.

**Etapa recomendada:** Audit hardening phase.

## DEC-023 - Performance, availability, backup and observability targets

**Estado:** `OPEN - REQUIERE DECISIÓN HUMANA`

**Prioridad:** `P2`

**Contexto:** Functional integration tests and health endpoints exist, but no accepted production workload or recovery target exists.

**Evidencia:** SRS academic figures, test reports, Actuator configuration and absence of load/restore evidence.

**Problema:** Unmeasured figures cannot be used as release acceptance criteria.

**Opciones:**

- **A. Approve workload-based SLOs, RTO/RPO and observability signals before staging.** Advantage: measurable readiness. Disadvantage: environment and operations effort.
- **B. Define provisional prototype targets and explicitly exclude production claims.** Advantage: supports iterative measurement. Disadvantage: not a production commitment.
- **C. Defer all NFR targets.** Advantage: no premature numbers. Disadvantage: architectural risks surface late.

**Impacto:** Capacity, deployment, backup, monitoring and release gates.

**Recomendación técnica:** Option B for development, followed by A with representative staging evidence.

**Decisión requerida:** `APROBACIÓN HUMANA` by the academic project authority, informed by product/operations concerns.

**Dependencias:** DEC-001 and the selected deployment topology.

**Bloquea:** Production-readiness approval; does not block bounded domain design.

**Etapa recomendada:** ETAPA C quality baseline and pre-staging hardening.

## 5. Priority summary

### P0 - Blocking every possible next implementation

None. The current appointment core is closed enough for maintenance, and ETAPA C may begin as a
decision/design stage. Marking a conditional policy as P0 would overstate its reach.

### P1 - Required for identified next increments

Priority remains useful after approval: a closed P1 no longer needs a domain decision, but its
approved scope still constrains ETAPA C.

| Decision | State after B.1.1 | Increment affected |
|---|---|---|
| DEC-001 | `CLOSED` | Governs academic-scope claims for every increment. |
| DEC-002 | `CLOSED` | Restricts the current scope to four roles; additional roles need a new decision. |
| DEC-003 | `OPEN` | Clean deployment/pilot operability. |
| DEC-005 | `OPEN` | Professional self-service and expanded ownership. |
| DEC-006 | `CLOSED` | Constrains specialty ownership and future N:M assignment design. |
| DEC-007 | `CLOSED` | Constrains patient/reception/admin availability contract design. |
| DEC-008 | `OPEN` | Automatic or supported materialization of slots. |
| DEC-009 | `CLOSED` | Constrains every future civil-time-dependent behavior. |
| DEC-010 | `OPEN` | Temporal booking/lifecycle automation. |
| DEC-017 | `OPEN` | Patient self-onboarding/profile editing. |
| DEC-019 | `CLOSED` | Constrains API compatibility, documentation and future OpenAPI authority. |

### P2 - Can be resolved after the next bounded increment

- DEC-004 - Auth to Users boundary.
- DEC-015 - Audit event inventory.
- DEC-016 - Professional deactivation API.
- DEC-020 - Permission granularity.
- DEC-022 - Audit retention/access/request metadata.
- DEC-023 - Operational quality targets, before production readiness.

### P3 - Future

- DEC-011 - No-show.
- DEC-012 - Waitlist.
- DEC-013 - Priority review.
- DEC-014 - Notifications.
- DEC-018 - Public content.
- DEC-021 - Mobile OAuth2 plus PKCE implementation; the architectural target itself is closed.

## 6. Decision dependency map

```text
DEC-001 Academic/prototype scope
  +-> DEC-002 Role map
  |     +-> DEC-003 Bootstrap
  |     +-> DEC-013 Priority reviewer
  |     +-> DEC-020 Permission matrix
  |     +-> DEC-022 Audit access
  +-> DEC-005 Professional/user lifecycle
  +-> DEC-006 Specialty policy
  |     +-> DEC-007 Availability actors/data
  |     +-> DEC-008 Slot generation
  +-> DEC-009 Business timezone
        +-> DEC-008 Slot generation
        +-> DEC-010 Temporal rules
              +-> DEC-011 No-show
              +-> DEC-012 Waitlist timing/expiry
              +-> DEC-014 Notification timing

DEC-019 API contract authority
  +-> DEC-007 Availability API
  +-> DEC-017 Patient self-service API

DEC-015 Audit event inventory
  +-> DEC-013 Priority audit
  +-> DEC-016 Professional deactivation audit
  +-> DEC-022 Audit retention/access

DEC-012 Waitlist policy
  +-> DEC-014 Offer notifications

DEC-003 Bootstrap + DEC-023 Operational targets
  +-> deployable pilot / production-readiness gate
```

The map expresses design dependencies, not automatic approval. For example, DEC-009 must be
closed before time windows can be specified, but closing the timezone does not approve any
particular cancellation window.

## 7. Gate for ETAPA C

ETAPA C is approved only as a later design/contracts phase for **Agenda Availability and Specialty
Policy**. It is not yet authorized as an implementation phase. The gate remains conditional for
any scope beyond that conceptual increment.

| Decision | Blocks ETAPA C? | Reason |
|---|---|---|
| DEC-001 | No for academic design | Closed: ETAPA C must retain the academic/prototype disclaimer and must not claim institutional approval. |
| DEC-002 | No for current role scope | Closed: ETAPA C uses only PATIENT, RECEPTIONIST, PROFESSIONAL and ADMIN; any additional role is excluded. |
| DEC-003 | Yes for operability/pilot | A clean environment cannot be administered through the supported API without an initial ADMIN/reference-data process. |
| DEC-004 | No for unrelated work | Current Auth works; close before an Auth/identity expansion. |
| DEC-005 | Conditional | Required for professional self-service/link management, not current assigned-professional checks. |
| DEC-006 | No for conceptual design | Closed: Catalogs owns definitions, Professionals owns N:M assignments through a future public contract. |
| DEC-007 | No for conceptual design | Closed: authenticated PATIENT/RECEPTIONIST receive sanitized availability and ADMIN may receive operational detail. Current API remains unchanged. |
| DEC-008 | Yes for slot generation | Generation inputs and overlap policy are undefined. |
| DEC-009 | No for timezone design | Closed: use a configurable IANA business zone with initial academic value `America/Lima`; temporal rules remain DEC-010. |
| DEC-010 | Yes for temporal automation | No operation window may be invented. |
| DEC-011 | No | Future no-show phase only. |
| DEC-012 | No | Future waitlist phase only; becomes a full gate if chosen as ETAPA C. |
| DEC-013 | No | Future priority phase only; becomes a full gate if chosen as ETAPA C. |
| DEC-014 | No | Future notifications phase only; becomes a full gate if chosen as ETAPA C. |
| DEC-015 | Conditional | Required if ETAPA C changes the audited action inventory. |
| DEC-016 | No | Only professional deactivation API work. |
| DEC-017 | Conditional | Required if ETAPA C is patient onboarding/self-service. |
| DEC-018 | No | Future public-content phase only. |
| DEC-019 | No for contract design | Closed: current controllers are the functional contract; formal OpenAPI is future authority; no universal envelope/pagination. |
| DEC-020 | No | Current role checks are functional; required before fine-grained permission work. |
| DEC-021 | No | Closed target, future implementation. |
| DEC-022 | Conditional | Required for audit query/retention/request-metadata work. |
| DEC-023 | Yes for production readiness | Measurable SLO/RTO/RPO evidence is required before a production-readiness claim, not before domain design. |

### Recommended bounded ETAPA C candidate

The next concrete design/contracts increment is **Agenda Availability and Specialty Policy**. Its
minimum domain decisions are now closed:

1. DEC-006 - Catalogs owns definitions; Professionals owns N:M assignments through a public boundary;
2. DEC-007 - authenticated PATIENT/RECEPTIONIST receive sanitized data; ADMIN may receive operational detail;
3. DEC-009 - configurable IANA business timezone, initially `America/Lima` for the academic case study;
4. DEC-019 - implemented controllers remain the functional contract and formal OpenAPI is the future authority.

DEC-008 remains open and automatic/materialized slot generation is explicitly outside this approved
increment. If ETAPA C later includes generation, DEC-008 must be closed first. DEC-010 also remains
open; no temporal window may be designed or implemented from the timezone decision alone.

DEC-003 should run as a separate platform-operability decision in parallel and must close before a
clean pilot environment is presented as usable.

## 8. Contradictions and non-decisions preserved

| Subject | Documentary statement | Current evidence | Closure |
|---|---|---|---|
| TRIAGE | ADR-005 associates TRIAGE with operational flow. | Current flow uses RECEPTIONIST and assigned PROFESSIONAL; TRIAGE has no endpoint. | DEC-002 closes current scope with four roles; TRIAGE remains excluded. |
| Availability actors | Product/API flows mention PATIENT/RECEPTIONIST. | Agenda query endpoints are currently ADMIN-only. | DEC-007 approves future sanitized authenticated access; the implementation gap remains for ETAPA C design/later implementation. |
| Appointment creation audit | Appointment architecture lists `APPOINTMENT_CREATED`. | Enum/service implement seven later lifecycle events only. | DEC-015. |
| Waitlist lifecycle | Roadmap/SRS describe offer behavior. | V1 contains only `waitlist_entries`; no offer persistence/service and states are not a complete match. | DEC-012. |
| API uniformity | API specification implies broader routes/envelopes/pagination. | Controllers expose direct DTOs/lists and only implemented routes. | DEC-019 makes controller behavior canonical; narrative documentation must not invent behavior. |
| Specialty cardinality | Join table physically permits multiple specialty links. | No Java specialty-management implementation exists. | DEC-006 approves N:M business cardinality and split ownership; implementation remains absent. |
| Timezone | Hibernate/JDBC use UTC. | No business-timezone implementation/configuration exists. | DEC-009 approves configurable IANA time with initial academic value `America/Lima`; UTC remains technical. |
| Role availability | `TRIAGE` and `SYSTEM` exist in `RoleName`. | No production role seed or current business endpoint exists for them. | DEC-002 excludes additional role operations; DEC-003 bootstrap remains open. |
| Mobile auth | ADR-004 selects OAuth2 plus PKCE. | Current backend implements JWT login/refresh and no mobile provider/client flow. | Decision closed, implementation future under DEC-021. |

## 9. Validation guardrails

- `DOMAIN-BASELINE.md` is intentionally unchanged because B.1.1 authorizes updates only to this register.
- No Java source or test is changed.
- No SQL file or migration is changed or added.
- No controller, endpoint, DTO, entity, repository, service, contract or security configuration is changed.
- Only DEC-001, DEC-002, DEC-006, DEC-007, DEC-009 and DEC-019 are newly closed by explicit B.1.1 human approval dated 2026-09-29.
- Future tables or documentary mentions are not treated as implemented functionality.
- `git diff --check` and file-scope verification are required before closing B.1.

## 10. C1-A.2 closure addendum for C1-B (2026-09-30)

This dated addendum governs **only the C1-B domain-hardening scope**. Earlier B.1.1 inventory and
historical gate text above describe the state at that time; they are not a current implementation
claim. These approvals change decisions, not Java, SQL, PostgreSQL, endpoints or frontend behavior.

| ID | State for C1-B | Approved rule |
|---|---|---|
| C1A2-03 | `CLOSED` | Documentary identity is unique by `(document_type, document_number)`, including inactive patient records. `DNI`, `CE`, `PASSPORT` are the allowed persisted types. The same number may exist under different types. Minor DNI is derived from `birthDate` and age under 18, never a document type. Replace the existing global number uniqueness only in a future, validated migration. |
| C1A2-19 | `CLOSED` policy; execution pending | Preserve the 14 existing invalid DNI patients and the professional with license `DEMO-CMP-0001`. A future traceable migration must record each existing PK → unique, valid synthetic replacement while preserving UUIDs, relations, appointments and history. No manual data repair. |
| C1A2-21 / DEC-010 | `CLOSED` for ordinary past-appointment operations | Ordinary `CONFIRM`, `CANCEL` and `RESCHEDULE` must reject past appointments. This closes only that bounded rule; other lifecycle windows remain outside C1-B and no administrative exception is authorized. |
| C1A2-22 | `CLOSED` | A professional account with `users.enabled=false` cannot perform new operations, receive new reservations or start attention. Keep the professional profile, appointments and history. The development seed must use an enabled linked account if it is offered for new demo reservations. |
| C1A2-23 | `CLOSED` | Reject professional deactivation/revocation while an appointment is in `CHECK_IN`, `WAITING` or `IN_ATTENTION`. An inactive specialty blocks new associations, operative schedules and reservations; existing appointments remain and may complete normally. Preserve history. |
| C1A2-20 | `CLOSED — APPROVED FOR IMPLEMENTATION` | Active schedules and inactive schedules retaining future reservations must not allow conflicting professional capacity. The PostgreSQL 16.15 candidate, gateway and runtime-role design passed the C1A2-20.2 isolated validation. This closes the **design decision**, not the implementation. |

Prior C1-A.2 approvals remain: no overlap between active schedules of the same professional and
weekday even across specialties; `[start,end)` permits adjacency; deactivation preserves existing
appointments and blocks conflicting capacity until it is resolved; structural edits must preserve
history; slots last 30 minutes and `COMPLETED` consumes capacity; ordinary cancellation and
rescheduling are rejected at `CHECK_IN`, `WAITING` and `IN_ATTENTION`; correction of patient document
type/number is ADMIN-only and keeps `patientId`; insurance remains required free text; deactivating
the last active ADMIN or directly removing PATIENT/PROFESSIONAL roles with linked profiles is blocked.
The business zone remains configurable IANA time, initially `America/Lima`.

**C1A2-20 design gate:** `CLOSED — APPROVED FOR IMPLEMENTATION` on 2026-09-30 for the academic project.
Evidence: `docs/architecture/C1-A2-20-POSTGRESQL-CAPACITY-DESIGN.md`,
`docs/architecture/C1-A2-20-POSTGRESQL-VALIDATION-REPORT.md`,
`docs/architecture/validation/C1-A2-20-CANDIDATE.sql`,
`docs/architecture/validation/C1-A2-20-GATEWAY.sql`,
`docs/architecture/validation/C1-A2-20-RUNTIME-ROLE.sql` and
`docs/architecture/validation/C1-A2-20-REMEDIATION-VALIDATE.py`.
Validation used PostgreSQL 16.15, 15 two-session races with zero `40P01`, V4 upgrade and clean
install, Java/SQL Lima boundary checks, and Hibernate validate. The implementation, production
role cutover and migration remain separate work under C1-B.
