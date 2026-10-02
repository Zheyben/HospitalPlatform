"""Historical C1A2-20.1 probes, including deliberately reproduced blockers.

Use C1-A2-20-REMEDIATION-VALIDATE.py for the final C1A2-20.2 gate. Never
target the project database with either script.
"""

from __future__ import annotations

import subprocess
import sys
import time

CONTAINER = "c1a2-20-pg16"
BASE = "c1_upgrade"
P = "44444444-4444-4444-4444-444444444444"
S1 = "33333333-3333-3333-3333-333333333331"
S2 = "33333333-3333-3333-3333-333333333332"
SCH = "66666666-6666-6666-6666-666666666661"
SLOT = "77777777-7777-7777-7777-777777777771"
APT = "88888888-8888-8888-8888-888888888881"
PAT = "55555555-5555-5555-5555-555555555555"
SLOT2 = "77777777-7777-7777-7777-777777777772"
APT2 = "88888888-8888-8888-8888-888888888882"


def cmd(db: str, statement: str, user: str = "postgres") -> list[str]:
    return [
        "docker", "exec", CONTAINER, "psql", "-U", user, "-d", db,
        "-X", "-v", "ON_ERROR_STOP=1", "-v", "VERBOSITY=verbose", "-Atc", statement,
    ]


def sql(db: str, statement: str, error: str | None = None, user: str = "postgres") -> str:
    result = subprocess.run(cmd(db, statement, user), text=True, capture_output=True)
    output = result.stdout + result.stderr
    if error is None and result.returncode:
        raise AssertionError(f"unexpected SQL failure: {output}")
    if error is not None and (result.returncode == 0 or error not in output):
        raise AssertionError(f"expected SQLSTATE {error}, got: {output}")
    return output.strip()


def check(label: str, actual: str, expected: str) -> None:
    if actual != expected:
        raise AssertionError(f"{label}: expected {expected}, got {actual}")
    print(f"PASS {label}: {actual}", flush=True)


def clone(name: str) -> None:
    sql("postgres", f"CREATE DATABASE {name} TEMPLATE {BASE}")


def test_schedules() -> None:
    db = "c1_schedules"
    clone(db)
    dow = "extract(dow from current_date+1)::int"
    def insert(spec: str, start: str, end: str) -> str:
        return ("INSERT INTO schedules(professional_id,specialty_id,day_of_week,start_time,end_time,active) "
                f"VALUES ('{P}','{spec}',{dow},time '{start}',time '{end}',true)")
    sql(db, insert(S1, "09:30", "10:30"), "23P01")
    sql(db, insert(S2, "09:30", "10:30"), "23P01")
    sql(db, insert(S1, "12:00", "13:00"))
    check("same/cross-specialty overlap and adjacency", sql(db, "SELECT count(*) FROM schedules"), "2")
    sql(db, f"UPDATE schedules SET active=false WHERE id='{SCH}'")
    check("deactivation protects without prior read", sql(db, f"SELECT capacity_protected::int FROM schedules WHERE id='{SCH}'"), "1")
    sql(db, insert(S2, "09:30", "10:30"), "23P01")
    sql(db, f"UPDATE schedules SET capacity_protected=false WHERE id='{SCH}'", "23514")
    sql(db, f"BEGIN; UPDATE appointments SET appointment_status='CANCELLED' WHERE id='{APT}'; "
            f"UPDATE availability_slots SET status='AVAILABLE' WHERE id='{SLOT}'; COMMIT;")
    sql(db, f"UPDATE schedules SET capacity_protected=false WHERE id='{SCH}'")
    sql(db, insert(S2, "09:30", "10:30"))
    check("release after cancellation", sql(db, "SELECT count(*) FROM schedules"), "3")


def test_slots() -> None:
    db = "c1_slots"
    clone(db)
    sql(db, f"INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status) "
            f"VALUES ('{SLOT2}','{SCH}',current_date+1,time '09:15',time '09:45','AVAILABLE')", "23P01")
    sql(db, f"INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status) "
            f"VALUES ('{SLOT2}','{SCH}',current_date+2,time '10:00',time '10:30','AVAILABLE')", "23514")
    sql(db, f"BEGIN; INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status) "
            f"VALUES ('{SLOT2}','{SCH}',current_date+1,time '10:00',time '10:30','RESERVED'); COMMIT;", "23514")
    check("orphan RESERVED rolled back", sql(db, f"SELECT count(*) FROM availability_slots WHERE id='{SLOT2}'"), "0")
    sql(db, f"INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status) "
            f"VALUES ('{SLOT2}','{SCH}',current_date+1,time '10:00',time '10:30','AVAILABLE')")
    sql(db, f"INSERT INTO appointments(id,patient_id,professional_id,slot_id,appointment_status) "
            f"VALUES ('{APT2}','{PAT}','{P}','{SLOT2}','SCHEDULED')", "23514")
    sql(db, f"UPDATE availability_slots SET status='AVAILABLE' WHERE id='{SLOT}'", "23514")
    check("appointment/slot final coherence", sql(db, "SELECT count(*) FROM appointments"), "1")
    sql(db, f"BEGIN; UPDATE appointments SET appointment_status='RESCHEDULED' WHERE id='{APT}'; "
            f"UPDATE availability_slots SET status='AVAILABLE' WHERE id='{SLOT}'; "
            f"UPDATE availability_slots SET status='RESERVED' WHERE id='{SLOT2}'; "
            f"INSERT INTO appointments(id,patient_id,professional_id,slot_id,appointment_status,rescheduled_from_id) "
            f"VALUES ('{APT2}','{PAT}','{P}','{SLOT2}','SCHEDULED','{APT}'); COMMIT;")
    check("reschedule old/new slot", sql(db, f"SELECT status FROM availability_slots WHERE id='{SLOT2}'"), "RESERVED")
    sql(db, f"UPDATE appointments SET appointment_status='COMPLETED',flow_stage='FINISHED' WHERE id='{APT2}'")
    sql(db, f"UPDATE availability_slots SET status='AVAILABLE' WHERE id='{SLOT2}'", "23514")
    check("COMPLETED consumes only its slot", sql(db, f"SELECT status FROM availability_slots WHERE id='{SLOT}'"), "AVAILABLE")


def run_pair(db: str, a: str, b: str, delay: float = 0.35) -> tuple[str, str]:
    pa = subprocess.Popen(cmd(db, a), text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    time.sleep(delay)
    pb = subprocess.Popen(cmd(db, b), text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    ao, ae = pa.communicate(timeout=20)
    bo, be = pb.communicate(timeout=20)
    return f"exit={pa.returncode} {ao}{ae}", f"exit={pb.returncode} {bo}{be}"


def test_races() -> None:
    db = "c1_race_reserve_first"
    clone(db)
    sql(db, f"INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status) "
            f"VALUES ('{SLOT2}','{SCH}',current_date+1,time '10:00',time '10:30','AVAILABLE')")
    a = (f"BEGIN; UPDATE availability_slots SET status='RESERVED' WHERE id='{SLOT2}' AND status='AVAILABLE'; "
         f"INSERT INTO appointments(id,patient_id,professional_id,slot_id,appointment_status) "
         f"VALUES ('{APT2}','{PAT}','{P}','{SLOT2}','SCHEDULED'); SELECT pg_sleep(1.5); COMMIT;")
    b = f"BEGIN; UPDATE schedules SET active=false WHERE id='{SCH}'; COMMIT;"
    ar, br = run_pair(db, a, b)
    check("reserve-first connection results", str("exit=0" in ar and "exit=0" in br), "True")
    check("reserve-first inactive protection", sql(db, f"SELECT (NOT active AND capacity_protected)::int FROM schedules WHERE id='{SCH}'"), "1")
    check("reserve-first appointment", sql(db, f"SELECT count(*) FROM appointments WHERE slot_id='{SLOT2}'"), "1")

    db = "c1_race_deactivate_first"
    clone(db)
    sql(db, f"INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status) "
            f"VALUES ('{SLOT2}','{SCH}',current_date+1,time '10:00',time '10:30','AVAILABLE')")
    a = f"BEGIN; UPDATE schedules SET active=false WHERE id='{SCH}'; SELECT pg_sleep(1.5); COMMIT;"
    b = f"BEGIN; UPDATE availability_slots SET status='RESERVED' WHERE id='{SLOT2}' AND status='AVAILABLE'; COMMIT;"
    ar, br = run_pair(db, a, b)
    check("deactivate-first results", str("exit=0" in ar and "23514" in br), "True")
    check("deactivate-first slot", sql(db, f"SELECT status FROM availability_slots WHERE id='{SLOT2}'"), "AVAILABLE")


def test_time_and_roles() -> None:
    db = "c1_roles"
    clone(db)
    check("business zone initially Lima", sql(db, "SELECT zone_name FROM hospital_business_config"), "America/Lima")
    check("business time matches PG conversion", sql(db, "SELECT abs(extract(epoch from (hospital_business_now()-(clock_timestamp() AT TIME ZONE 'America/Lima')))) < 1"), "t")
    sql(db, "UPDATE hospital_business_config SET zone_name='Invalid/Zone'", "22023")
    sql("postgres", "CREATE ROLE c1_runtime LOGIN")
    sql(db, "GRANT USAGE ON SCHEMA public TO c1_runtime; GRANT SELECT,INSERT,UPDATE ON ALL TABLES IN SCHEMA public TO c1_runtime; "
            "REVOKE ALL ON hospital_business_config FROM c1_runtime; GRANT EXECUTE ON FUNCTION hospital_business_now() TO c1_runtime;")
    check("runtime SELECT allowed", sql(db, "SELECT count(*) FROM schedules", user="c1_runtime"), "1")
    sql(db, f"UPDATE schedules SET updated_at=clock_timestamp() WHERE id='{SCH}'", user="c1_runtime")
    sql(db, "UPDATE hospital_business_config SET zone_name='UTC'", "42501", user="c1_runtime")
    sql(db, "ALTER TABLE schedules ADD COLUMN forbidden int", "42501", user="c1_runtime")
    sql(db, "ALTER TABLE schedules DISABLE TRIGGER trg_schedule_capacity_guard", "42501", user="c1_runtime")
    sql(db, "CREATE OR REPLACE FUNCTION hospital_business_now() RETURNS timestamp LANGUAGE sql AS 'SELECT now()::timestamp'", "42501", user="c1_runtime")
    sql(db, "CREATE EXTENSION hstore", "42501", user="c1_runtime")
    sql(db, "DELETE FROM appointments", "42501", user="c1_runtime")
    sql(db, "TRUNCATE appointments", "42501", user="c1_runtime")
    print("PASS runtime privilege boundaries", flush=True)


def test_double_booking_and_deadlock() -> None:
    db = "c1_double_booking"
    clone(db)
    sql(db, f"INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status) "
            f"VALUES ('{SLOT2}','{SCH}',current_date+1,time '10:00',time '10:30','AVAILABLE')")
    a = (f"BEGIN; UPDATE availability_slots SET status='RESERVED' WHERE id='{SLOT2}' AND status='AVAILABLE'; "
         f"INSERT INTO appointments(id,patient_id,professional_id,slot_id,appointment_status) "
         f"VALUES ('{APT2}','{PAT}','{P}','{SLOT2}','SCHEDULED'); SELECT pg_sleep(1.5); COMMIT;")
    b = (f"BEGIN; WITH changed AS (UPDATE availability_slots SET status='RESERVED' "
         f"WHERE id='{SLOT2}' AND status='AVAILABLE' RETURNING id) "
         f"INSERT INTO appointments(patient_id,professional_id,slot_id,appointment_status) "
         f"SELECT '{PAT}','{P}',id,'SCHEDULED' FROM changed; COMMIT;")
    ar, br = run_pair(db, a, b)
    check("two reservation transactions commit safely", str("exit=0" in ar and "exit=0" in br), "True")
    check("only one appointment occupies slot", sql(db, f"SELECT count(*) FROM appointments WHERE slot_id='{SLOT2}'"), "1")
    sql(db, f"INSERT INTO appointments(patient_id,professional_id,slot_id,appointment_status) "
            f"VALUES ('{PAT}','{P}','{SLOT2}','SCHEDULED')", "23505")

    db = "c1_deadlock_probe"
    clone(db)
    sql(db, f"INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status) "
            f"VALUES ('{SLOT2}','{SCH}',current_date+1,time '10:00',time '10:30','AVAILABLE')")
    a = (f"BEGIN; SELECT id FROM schedules WHERE id='{SCH}' FOR UPDATE; "
         f"SELECT pg_sleep(0.7); UPDATE availability_slots SET status='BLOCKED' WHERE id='{SLOT2}'; COMMIT;")
    b = f"BEGIN; UPDATE availability_slots SET status='BLOCKED' WHERE id='{SLOT2}'; COMMIT;"
    ar, br = run_pair(db, a, b, 0.2)
    check("raw slot UPDATE lock inversion found", str("40P01" in ar or "40P01" in br), "True")
    check("deadlock does not corrupt slot", sql(db, f"SELECT status FROM availability_slots WHERE id='{SLOT2}'"), "BLOCKED")


def test_completed_future_gap() -> None:
    db = "c1_future_completion_gap"
    clone(db)
    sql(db, f"UPDATE appointments SET appointment_status='COMPLETED', flow_stage='FINISHED' WHERE id='{APT}'")
    check("unfixed candidate permits premature completion", sql(db, f"SELECT appointment_status FROM appointments WHERE id='{APT}'"), "COMPLETED")
    sql(db, f"UPDATE schedules SET active=false WHERE id='{SCH}'")
    sql(db, f"UPDATE schedules SET capacity_protected=false WHERE id='{SCH}'", "23514")
    print("GAP premature COMPLETED blocks future capacity until slot end", flush=True)


def test_generation_and_professional_gap() -> None:
    db = "c1_generate_edit"
    clone(db)
    sql(db, f"UPDATE schedules SET start_time=time '08:00' WHERE id='{SCH}'", "23514")
    sql(db, f"INSERT INTO schedules(id,professional_id,specialty_id,day_of_week,start_time,end_time,active) "
            f"VALUES ('66666666-6666-6666-6666-666666666663','{P}','{S1}',"
            "extract(dow from current_date+1)::int,time '13:00',time '14:00',true)")
    a = ("BEGIN; SELECT id FROM schedules WHERE id='66666666-6666-6666-6666-666666666663' FOR UPDATE; "
         "INSERT INTO availability_slots(schedule_id,slot_date,start_time,end_time,status) "
         "VALUES ('66666666-6666-6666-6666-666666666663',current_date+1,time '13:00',time '13:30','AVAILABLE'); "
         "SELECT pg_sleep(1.0); COMMIT;")
    b = "BEGIN; UPDATE schedules SET start_time=time '14:00',end_time=time '15:00' WHERE id='66666666-6666-6666-6666-666666666663'; COMMIT;"
    ar, br = run_pair(db, a, b)
    check("generation first commits, structural edit rejects", str("exit=0" in ar and "23514" in br), "True")

    db = "c1_professional_gap"
    clone(db)
    sql(db, f"UPDATE appointments SET appointment_status='CONFIRMED',flow_stage='IN_ATTENTION' WHERE id='{APT}'")
    sql(db, f"UPDATE professionals SET deleted_at=clock_timestamp() WHERE id='{P}'")
    check("unfixed candidate permits in-progress professional deactivation",
          sql(db, f"SELECT (deleted_at IS NOT NULL)::int FROM professionals WHERE id='{P}'"), "1")
    print("GAP professional deactivation guard is absent", flush=True)


def test_deadlock_detail() -> None:
    db = "c1_deadlock_detail"
    clone(db)
    sql(db, f"INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status) "
            f"VALUES ('{SLOT2}','{SCH}',current_date+1,time '10:00',time '10:30','AVAILABLE')")
    a = (f"BEGIN; SELECT id FROM schedules WHERE id='{SCH}' FOR UPDATE; SELECT pg_sleep(0.7); "
         f"UPDATE availability_slots SET status='BLOCKED' WHERE id='{SLOT2}'; COMMIT;")
    b = f"BEGIN; UPDATE availability_slots SET status='BLOCKED' WHERE id='{SLOT2}'; COMMIT;"
    ar, br = run_pair(db, a, b, 0.2)
    print("DEADLOCK A:", ar, flush=True)
    print("DEADLOCK B:", br, flush=True)
    check("40P01 captured", str("40P01" in ar or "40P01" in br), "True")


if __name__ == "__main__":
    tests = (test_schedules, test_slots, test_races, test_time_and_roles,
             test_double_booking_and_deadlock, test_completed_future_gap,
             test_generation_and_professional_gap, test_deadlock_detail)
    chosen = [test for test in tests if len(sys.argv) == 1 or test.__name__ in sys.argv[1:]]
    for test in chosen:
        print(f"RUN {test.__name__}", flush=True)
        test()
