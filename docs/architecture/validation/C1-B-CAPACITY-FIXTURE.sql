-- Representative synthetic V4 data for the isolated upgrade database only.
INSERT INTO users(id,username,email,password_hash,enabled)
VALUES
 ('11111111-1111-1111-1111-111111111111','validation_prof','validation.prof@example.test','not-a-real-password',true),
 ('22222222-2222-2222-2222-222222222222','validation_patient','validation.patient@example.test','not-a-real-password',true);
INSERT INTO patients(id,user_id,document_type,document_number,birth_date,phone,insurance)
VALUES ('55555555-5555-5555-5555-555555555555','22222222-2222-2222-2222-222222222222',
        'DNI','99000001',date '1995-01-01','999999999','Demo');
INSERT INTO specialties(id,name,active) VALUES
 ('33333333-3333-3333-3333-333333333331','Validation A',true),
 ('33333333-3333-3333-3333-333333333332','Validation B',true);
INSERT INTO professionals(id,user_id,license_number)
VALUES ('44444444-4444-4444-4444-444444444444','11111111-1111-1111-1111-111111111111','990001');
INSERT INTO professional_specialties(professional_id,specialty_id) VALUES
 ('44444444-4444-4444-4444-444444444444','33333333-3333-3333-3333-333333333331'),
 ('44444444-4444-4444-4444-444444444444','33333333-3333-3333-3333-333333333332');
INSERT INTO schedules(id,professional_id,specialty_id,day_of_week,start_time,end_time,active)
VALUES ('66666666-6666-6666-6666-666666666661','44444444-4444-4444-4444-444444444444',
        '33333333-3333-3333-3333-333333333331',extract(dow from current_date+1)::int,
        time '09:00',time '12:00',true);
INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status)
VALUES ('77777777-7777-7777-7777-777777777771','66666666-6666-6666-6666-666666666661',
        current_date+1,time '09:00',time '09:30','RESERVED');
INSERT INTO appointments(id,patient_id,professional_id,slot_id,appointment_status,reason)
VALUES ('88888888-8888-8888-8888-888888888881','55555555-5555-5555-5555-555555555555',
        '44444444-4444-4444-4444-444444444444','77777777-7777-7777-7777-777777777771',
        'SCHEDULED','Capacity validation');

