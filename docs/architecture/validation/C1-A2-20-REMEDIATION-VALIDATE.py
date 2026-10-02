"""Two-session PostgreSQL 16 capacity design probe; isolated container only.

Prerequisite: V1-V4, fixture, candidate, gateway and runtime grants in c1_remed_final2.
The script clones that database. It never connects to the project database.
"""

from __future__ import annotations

import subprocess
import time
from uuid import uuid4

CONTAINER = "c1a2-20-pg16-remed"
BASE = "c1_remed_final2"
PAT = "55555555-5555-5555-5555-555555555555"
PRO = "44444444-4444-4444-4444-444444444444"
SPEC = "33333333-3333-3333-3333-333333333331"
SCH = "66666666-6666-6666-6666-666666666661"
SLOT = "77777777-7777-7777-7777-777777777771"
APT = "88888888-8888-8888-8888-888888888881"
SLOT_FREE = "77777777-7777-7777-7777-777777777772"
SCH_B = "66666666-6666-6666-6666-666666666662"
SLOT_B = "77777777-7777-7777-7777-777777777773"
SLOT_B_FREE = "77777777-7777-7777-7777-777777777774"
APT_B = "88888888-8888-8888-8888-888888888882"
RUN_ID = uuid4().hex[:8]


def command(db: str, statement: str, user: str = "postgres") -> list[str]:
    return ["docker", "exec", CONTAINER, "psql", "-U", user, "-d", db,
            "-X", "-v", "ON_ERROR_STOP=1", "-v", "VERBOSITY=verbose", "-Atc", statement]


def sql(db: str, statement: str, user: str = "postgres", expected_error: str | None = None) -> str:
    p = subprocess.run(command(db, statement, user), text=True, capture_output=True, timeout=30)
    output = (p.stdout + p.stderr).strip()
    if expected_error:
        assert p.returncode and expected_error in output, (statement, output)
    else:
        assert not p.returncode, (statement, output)
    return output


def assert_sql(db: str, label: str, statement: str, expected: str) -> None:
    actual = sql(db, statement)
    assert actual == expected, (label, expected, actual)
    print(f"PASS {label}: {actual}", flush=True)


def clone(index: int, base: str = BASE) -> str:
    db = f"c1r_{RUN_ID}_{index}"
    sql("postgres", f"CREATE DATABASE {db} TEMPLATE {base}")
    return db


def apply_candidate(db: str, expected_error: str | None = None) -> str:
    p = subprocess.run(["docker", "exec", CONTAINER, "psql", "-U", "postgres", "-d", db,
                        "-X", "-v", "ON_ERROR_STOP=1", "-v", "VERBOSITY=verbose",
                        "-f", "/tmp/candidate-final2.sql"], text=True, capture_output=True, timeout=30)
    output = p.stdout + p.stderr
    if expected_error:
        assert p.returncode and expected_error in output, output
    else:
        assert not p.returncode, output
    return output


def apply_file(db: str, filename: str) -> None:
    p = subprocess.run(["docker", "exec", CONTAINER, "psql", "-U", "postgres", "-d", db,
                        "-X", "-v", "ON_ERROR_STOP=1", "-f", filename],
                       text=True, capture_output=True, timeout=30)
    assert not p.returncode, p.stdout + p.stderr


def pair(db: str, label: str, first: str, second: str) -> tuple[str, str]:
    def transaction(statement: str) -> str:
        return f"BEGIN; SET LOCAL lock_timeout='8s'; {statement}; SELECT pg_sleep(0.65); COMMIT"

    a = subprocess.Popen(command(db, transaction(first), "c1_runtime"),
                         text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    time.sleep(0.12)
    b = subprocess.Popen(command(db, transaction(second), "c1_runtime"),
                         text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    ao, ae = a.communicate(timeout=25)
    bo, be = b.communicate(timeout=25)
    left, right = ao + ae, bo + be
    assert "40P01" not in left + right, (label, left, right)
    assert "55P03" not in left + right, (label, left, right)
    print(f"PASS race {label}: A={a.returncode}, B={b.returncode}, deadlocks=0", flush=True)
    return left, right


def setup_free_slot(db: str) -> None:
    sql(db, f"INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status) "
            f"VALUES ('{SLOT_FREE}','{SCH}',current_date+1,time '10:00',time '10:30','AVAILABLE')")


def setup_confirmed(db: str, stage: str | None = None) -> None:
    sql(db, f"SELECT capacity_appointment_confirm('{APT}')", "c1_runtime")
    if stage in ("CHECK_IN", "WAITING"):
        sql(db, f"SELECT capacity_appointment_stage('{APT}','CHECK_IN')", "c1_runtime")
    if stage == "WAITING":
        sql(db, f"SELECT capacity_appointment_stage('{APT}','WAITING')", "c1_runtime")


def races() -> None:
    db = clone(1)
    setup_free_slot(db)
    first, second = pair(db, "reserve/reserve", f"SELECT capacity_reserve('{SLOT_FREE}','{PAT}')",
                         f"SELECT capacity_reserve('{SLOT_FREE}','{PAT}')")
    assert "SLOT_UNAVAILABLE" in second and "40P01" not in first + second
    assert_sql(db, "single occupant", f"SELECT count(*) FROM appointments WHERE slot_id='{SLOT_FREE}' AND appointment_status='SCHEDULED'", "1")

    db = clone(2)
    setup_free_slot(db)
    pair(db, "reserve/deactivate schedule", f"SELECT capacity_reserve('{SLOT_FREE}','{PAT}')",
         f"SELECT capacity_schedule_status('{SCH}',false)")
    assert_sql(db, "inactive schedule protected after reserve",
               f"SELECT (NOT active AND capacity_protected)::int FROM schedules WHERE id='{SCH}'", "1")

    db = clone(21)
    setup_free_slot(db)
    first, second = pair(db, "deactivate schedule/reserve reverse",
                         f"SELECT capacity_schedule_status('{SCH}',false)",
                         f"SELECT capacity_reserve('{SLOT_FREE}','{PAT}')")
    assert "23514" in second and "40P01" not in first + second
    assert_sql(db, "deactivated schedule rejects new reservation",
               f"SELECT count(*) FROM appointments WHERE slot_id='{SLOT_FREE}'", "0")

    db = clone(3)
    setup_free_slot(db)
    pair(db, "reserve/edit schedule", f"SELECT capacity_reserve('{SLOT_FREE}','{PAT}')",
         f"SELECT capacity_schedule_edit('{SCH}',time '08:00',time '12:00')")
    assert_sql(db, "edit rejected for schedule with slots", f"SELECT start_time::text FROM schedules WHERE id='{SCH}'", "09:00:00")

    db = clone(4)
    pair(db, "cancel/deactivate schedule", f"SELECT capacity_cancel('{APT}')",
         f"SELECT capacity_schedule_status('{SCH}',false)")
    assert_sql(db, "cancel releases slot", f"SELECT status FROM availability_slots WHERE id='{SLOT}'", "AVAILABLE")

    db = clone(41)
    pair(db, "cancel/reserve same slot", f"SELECT capacity_cancel('{APT}')",
         f"SELECT capacity_reserve('{SLOT}','{PAT}')")
    assert_sql(db, "cancel then reserve has one occupant",
               f"SELECT count(*) FROM appointments WHERE slot_id='{SLOT}' "
               "AND appointment_status IN ('SCHEDULED','CONFIRMED','COMPLETED')", "1")

    db = clone(42)
    first, second = pair(db, "reserve/cancel same slot reverse",
                         f"SELECT capacity_reserve('{SLOT}','{PAT}')",
                         f"SELECT capacity_cancel('{APT}')")
    assert "SLOT_UNAVAILABLE" in first and "40P01" not in first + second
    assert_sql(db, "failed reserve then cancel frees slot",
               f"SELECT status FROM availability_slots WHERE id='{SLOT}'", "AVAILABLE")

    db = clone(5)
    sql(db, f"INSERT INTO schedules(id,professional_id,specialty_id,day_of_week,start_time,end_time,active) "
            f"VALUES ('{SCH_B}','{PRO}','{SPEC}',extract(dow from current_date+1)::int,time '13:00',time '15:00',true)")
    setup_free_slot(db)
    sql(db, f"INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status) VALUES "
            f"('{SLOT_B}','{SCH_B}',current_date+1,time '13:00',time '13:30','RESERVED'),"
            f"('{SLOT_B_FREE}','{SCH_B}',current_date+1,time '13:30',time '14:00','AVAILABLE'); "
            f"INSERT INTO appointments(id,patient_id,professional_id,slot_id,appointment_status) "
            f"VALUES ('{APT_B}','{PAT}','{PRO}','{SLOT_B}','SCHEDULED')")
    pair(db, "reschedule A to B/B to A",
         f"SELECT capacity_reschedule('{APT}','{SLOT_B_FREE}')",
         f"SELECT capacity_reschedule('{APT_B}','{SLOT_FREE}')")
    assert_sql(db, "both reschedules committed",
               "SELECT count(*) FROM appointments WHERE rescheduled_from_id IS NOT NULL", "2")

    db = clone(6)
    sql(db, f"INSERT INTO schedules(id,professional_id,specialty_id,day_of_week,start_time,end_time,active) "
            f"VALUES ('{SCH_B}','{PRO}','{SPEC}',extract(dow from current_date+1)::int,time '13:00',time '15:00',true)")
    pair(db, "generation/edit schedule",
         f"SELECT capacity_generate_slot('{SCH_B}',current_date+1,time '13:00',time '13:30')",
         f"SELECT capacity_schedule_edit('{SCH_B}',time '13:30',time '15:00')")
    assert_sql(db, "generated slot stays in schedule",
               f"SELECT count(*) FROM availability_slots sl JOIN schedules s ON s.id=sl.schedule_id "
               f"WHERE sl.schedule_id='{SCH_B}' AND (sl.start_time<s.start_time OR sl.end_time>s.end_time)", "0")

    for index, (label, prior, target) in enumerate([
        ("check-in/deactivate professional", None, "CHECK_IN"),
        ("waiting/deactivate professional", "CHECK_IN", "WAITING"),
        ("in-attention/deactivate professional", "WAITING", "IN_ATTENTION"),
    ], 7):
        for reverse in (False, True):
            db = clone(index * 10 + int(reverse))
            setup_confirmed(db, prior)
            stage = f"SELECT capacity_appointment_stage('{APT}','{target}')"
            deactivate = f"SELECT capacity_professional_deactivate('{PRO}')"
            pair(db, label + (" reverse" if reverse else ""),
                 deactivate if reverse else stage, stage if reverse else deactivate)
            assert_sql(db, "no inactive professional with in-progress attention",
                       f"SELECT count(*) FROM professionals p JOIN appointments a ON a.professional_id=p.id "
                       f"WHERE p.id='{PRO}' AND p.deleted_at IS NOT NULL AND a.flow_stage IN "
                       "('CHECK_IN','WAITING','IN_ATTENTION') AND a.appointment_status='CONFIRMED'", "0")


def privileges_and_rules() -> None:
    db = clone(180)
    for label, statement in [
        ("slot DML", f"UPDATE availability_slots SET status='BLOCKED' WHERE id='{SLOT}'"),
        ("schedule DML", f"UPDATE schedules SET active=false WHERE id='{SCH}'"),
        ("professional DML", f"UPDATE professionals SET deleted_at=now() WHERE id='{PRO}'"),
        ("appointment DML", f"UPDATE appointments SET appointment_status='CANCELLED' WHERE id='{APT}'"),
        ("trigger alteration", "ALTER TABLE schedules DISABLE TRIGGER trg_schedule_capacity_guard"),
        ("function alteration", "CREATE OR REPLACE FUNCTION hospital_business_now() RETURNS timestamp LANGUAGE sql AS 'SELECT now()::timestamp'"),
        ("table alteration", "ALTER TABLE schedules ADD COLUMN forbidden int"),
        ("business zone alteration", "UPDATE hospital_business_config SET zone_name='UTC'"),
    ]:
        sql(db, statement, "c1_runtime", "42501")
        print(f"PASS runtime denies {label}", flush=True)
    sql(db, f"UPDATE professionals SET deleted_at=now() WHERE id='{PRO}'")
    assert_sql(db, "direct professional deactivation with no attention", f"SELECT (deleted_at IS NOT NULL)::int FROM professionals WHERE id='{PRO}'", "1")

    db = clone(181)
    setup_confirmed(db, "WAITING")
    sql(db, f"UPDATE professionals SET deleted_at=now() WHERE id='{PRO}'", expected_error="23514")
    sql(db, "UPDATE users SET enabled=false WHERE id='11111111-1111-1111-1111-111111111111'", expected_error="23514")
    sql(db, "UPDATE users SET deleted_at=now() WHERE id='11111111-1111-1111-1111-111111111111'", expected_error="23514")
    sql(db, f"SELECT capacity_appointment_complete('{APT}')", "c1_runtime", "23514")
    print("PASS direct SQL guards for professional and linked account in attention", flush=True)

    db = clone(182)
    setup_free_slot(db)
    sql(db, "UPDATE users SET enabled=false WHERE id='11111111-1111-1111-1111-111111111111'")
    sql(db, f"SELECT capacity_reserve('{SLOT_FREE}','{PAT}')", "c1_runtime", "23514")
    print("PASS disabled professional account blocks new reservation", flush=True)

    assert_sql(db, "Lima business zone", "SELECT zone_name FROM hospital_business_config", "America/Lima")
    sql(db, "UPDATE hospital_business_config SET zone_name='UTC'", expected_error="22023")
    assert_sql(db, "23:59 Lima / next UTC date",
               "SELECT (timestamptz '2026-10-01 04:59:00+00' AT TIME ZONE 'America/Lima')::text",
               "2026-09-30 23:59:00")
    assert_sql(db, "00:00 Lima / same UTC date",
               "SELECT (timestamptz '2026-10-01 05:00:00+00' AT TIME ZONE 'America/Lima')::text",
               "2026-10-01 00:00:00")

    db = clone(183)
    first = sql(db, f"SELECT capacity_generate_slot('{SCH}',current_date+1,time '10:00',time '10:30')",
                "c1_runtime")
    second = sql(db, f"SELECT capacity_generate_slot('{SCH}',current_date+1,time '10:00',time '10:30')",
                 "c1_runtime")
    assert first == second, (first, second)
    assert_sql(db, "gateway slot generation idempotent",
               f"SELECT count(*) FROM availability_slots WHERE id='{first}'", "1")


def upgrade_checks() -> None:
    db = clone(200, "c1_v4_remed")
    apply_candidate(db)
    assert_sql(db, "V4 compatible upgrade preserves appointment",
               f"SELECT count(*) FROM appointments WHERE id='{APT}'", "1")
    assert_sql(db, "V4 compatible upgrade pins Lima",
               "SELECT zone_name FROM hospital_business_config", "America/Lima")

    db = clone(201, "c1_v4_remed")
    sql(db, f"INSERT INTO schedules(professional_id,specialty_id,day_of_week,start_time,end_time,active) "
            f"VALUES ('{PRO}','33333333-3333-3333-3333-333333333332',"
            "extract(dow from current_date+1)::int,time '10:00',time '11:00',true)")
    apply_candidate(db, "23P01")
    assert_sql(db, "conflicting history rolls candidate back",
               "SELECT count(*) FROM information_schema.columns WHERE table_name='schedules' "
               "AND column_name='capacity_protected'", "0")

    db = clone(202, "c1_v4_remed")
    sql(db, f"UPDATE availability_slots SET status='AVAILABLE' WHERE id='{SLOT}'")
    apply_candidate(db, "23514")
    assert_sql(db, "orphan RESERVED history rolls candidate back",
               "SELECT count(*) FROM information_schema.tables WHERE table_name='hospital_business_config'", "0")

    db = clone(203, "c1_v4_remed")
    sql(db, f"UPDATE schedules SET day_of_week=extract(dow from current_date-7)::int WHERE id='{SCH}'; "
            f"UPDATE availability_slots SET slot_date=current_date-7 WHERE id='{SLOT}'; "
            f"UPDATE appointments SET appointment_status='CONFIRMED',flow_stage='IN_ATTENTION' WHERE id='{APT}'")
    apply_candidate(db)
    apply_file(db, "/tmp/gateway-final3.sql")
    apply_file(db, "/tmp/role-final3.sql")
    sql(db, f"SELECT capacity_appointment_complete('{APT}')", "c1_runtime")
    assert_sql(db, "completed history keeps its reserved slot",
               f"SELECT status FROM availability_slots WHERE id='{SLOT}'", "RESERVED")
    sql(db, f"UPDATE schedules SET active=false WHERE id='{SCH}'")
    sql(db, f"UPDATE schedules SET capacity_protected=false WHERE id='{SCH}'")
    sql(db, f"INSERT INTO schedules(professional_id,specialty_id,day_of_week,start_time,end_time,active) "
            f"SELECT professional_id,'33333333-3333-3333-3333-333333333332',day_of_week,"
            f"start_time,end_time,true FROM schedules WHERE id='{SCH}'")
    assert_sql(db, "past completed slot does not block future weekly capacity",
               "SELECT count(*) FROM schedules WHERE active", "1")
    print("PASS V4 upgrade, historical conflict, orphan preflight", flush=True)


if __name__ == "__main__":
    races()
    privileges_and_rules()
    upgrade_checks()
    print("PASS remediation matrix: 9 required race types plus cancel/reserve, "
          "15 two-session cases, 0 deadlocks", flush=True)
