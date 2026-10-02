-- Synthetic V4-only upgrade fixture; no dump of the project database.
INSERT INTO users(id,username,email,password_hash,enabled)
VALUES ('11111111-1111-1111-1111-111111111111','c1b-prof','c1b.prof@example.test','synthetic',true);
INSERT INTO patients(id,document_type,document_number,birth_date) VALUES
('093e273b-43d9-4bb1-b275-c9bed2ccb358','DNI','1790812486462683','1990-01-01'),
('1ad64dbe-35eb-4b93-a411-39fd309ee39e','DNI','1790812366391234','1990-01-01'),
('2295852d-0f98-4cbd-8f77-f3539c59fc3e','DNI','B2-56a05b4757fa43e0','1990-01-01'),
('33db4d03-1a03-4165-a91b-2ba09149d1a8','DNI','B2-26fd4ea41c8a40e2','1990-01-01'),
('43d10d5d-6e03-4d80-8f9a-4018039d78f0','DNI','1790813053314','1990-01-01'),
('63ac1e83-d3e2-402a-a47f-30fae309e177','DNI','B2F-b810c705cef24f7b','1990-01-01'),
('6b8bde3d-5014-434f-8c74-f45de837ab23','DNI','1790812176982980','1990-01-01'),
('80c2e11a-2910-458f-af27-77e2bd9f7e93','DNI','9020260930180602','1990-01-01'),
('85bb8787-8ef8-42a5-81de-cbdd60ec789d','DNI','1790813533229783','1990-01-01'),
('8dbac740-af9c-4c97-ab95-39d122b0fbb1','DNI','1790813322872346','1990-01-01'),
('aaba452c-4a0b-4fb3-a600-8ac74a45881c','DNI','1790812870450706','1990-01-01'),
('af3e4cb6-b6b8-4fd3-bbad-a739585708d8','DNI','1790812195194439','1990-01-01'),
('dc0a1aeb-5077-4002-bd2e-baaadcbeb5b7','DNI','1790812286082548','1990-01-01'),
('f7e7e408-03b7-4f14-8ee7-b85e3851ffff','DNI','1790812462256890','1990-01-01');
INSERT INTO specialties(id,name,active)
VALUES ('33333333-3333-3333-3333-333333333331','Synthetic specialty',true);
INSERT INTO professionals(id,user_id,license_number)
VALUES ('88743663-5b1b-3868-bf1a-aa0371adfac3','11111111-1111-1111-1111-111111111111','DEMO-CMP-0001');
INSERT INTO professional_specialties(professional_id,specialty_id)
VALUES ('88743663-5b1b-3868-bf1a-aa0371adfac3','33333333-3333-3333-3333-333333333331');
INSERT INTO schedules(id,professional_id,specialty_id,day_of_week,start_time,end_time,active)
VALUES ('66666666-6666-6666-6666-666666666661','88743663-5b1b-3868-bf1a-aa0371adfac3',
        '33333333-3333-3333-3333-333333333331',extract(dow from current_date+1)::int,
        time '09:00',time '12:00',true);
INSERT INTO availability_slots(id,schedule_id,slot_date,start_time,end_time,status)
VALUES ('77777777-7777-7777-7777-777777777771','66666666-6666-6666-6666-666666666661',
        current_date+1,time '09:00',time '09:30','RESERVED');
INSERT INTO appointments(id,patient_id,professional_id,slot_id,appointment_status)
VALUES ('88888888-8888-8888-8888-888888888881','093e273b-43d9-4bb1-b275-c9bed2ccb358',
        '88743663-5b1b-3868-bf1a-aa0371adfac3','77777777-7777-7777-7777-777777777771','SCHEDULED');
