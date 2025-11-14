SET @room_pricing_id = UUID_TO_BIN(UUID());
SET @user_id = UUID_TO_BIN(UUID());
SET @room_id = UUID_TO_BIN(UUID());
SET @dorm_id = UUID_TO_BIN(UUID());
SET @slot_id = UUID_TO_BIN(UUID());
SET @semester_id = UUID_TO_BIN(UUID());
SET @semester_id2 = UUID_TO_BIN(UUID());
SET @slot_history_id = UUID_TO_BIN(UUID());

insert into semester(id, end_date, name, start_date) VALUES (@semester_id, '2025-12-01', 'Semester Test', '2025-10-01'), (@semester_id2, '2025-12-01', 'Semester Test 2', '2025-10-01');
insert into room_pricing(id, price, total_slot) values (@room_pricing_id, 100000, 1);
insert into user(id, dob, email, full_name, gender, password, phone_number, role, user_code, username) values (@user_id, '2025-01-01', 'test@gmail.com', 'User Test', 'MALE', '', '0987654321', 'RESIDENT', 'RESIDENT', 'test');
insert into dorm(id, dorm_name, status, total_floor, total_room) values (@dorm_id, 'Dorm Test', null, 1, 1);
insert into room(id, floor, room_number, status, total_slot, dorm_id, pricing_id)
values (@room_id, 1, 'Room Test', 'AVAILABLE', 1, @dorm_id, @room_pricing_id);
insert into slot(id, slot_name, status, room_id, user_id) VALUES (@slot_id, 'Slot Test', 'AVAILABLE', @room_id, @user_id);
insert into slot_history(id, checkin, checkout, dorm_name, price, room_number, slot_id, slot_name, semester_id, user_id, room_id) VALUES (@slot_history_id, null, null, 'Dorm Test', '100000', 'Room Test', @slot_id, 'Slot Test', @semester_id, @user_id, @room_id);