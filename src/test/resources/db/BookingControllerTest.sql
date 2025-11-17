SET @room_pricing_id = UUID_TO_BIN(UUID());
SET @user_id = UUID_TO_BIN(UUID());
SET @user2_id = UUID_TO_BIN(UUID());
SET @user3_id = UUID_TO_BIN(UUID());
SET @room_id = UUID_TO_BIN(UUID());
SET @dorm_id = UUID_TO_BIN(UUID());
SET @slot_id = UUID_TO_BIN(UUID());
SET @slot2_id = UUID_TO_BIN(UUID());
SET @slot3_id = UUID_TO_BIN(UUID());
SET @semester_id = UUID_TO_BIN(UUID());
SET @semester_id2 = UUID_TO_BIN(UUID());
SET @slot_history_id = UUID_TO_BIN(UUID());
SET @question_1 = UUID_TO_BIN(UUID());
SET @question_2 = UUID_TO_BIN(UUID());
SET @question_3 = UUID_TO_BIN(UUID());
SET @answer_1_1 = UUID_TO_BIN(UUID());
SET @answer_1_2 = UUID_TO_BIN(UUID());
SET @answer_2_1 = UUID_TO_BIN(UUID());
SET @answer_2_2 = UUID_TO_BIN(UUID());
SET @answer_3_1 = UUID_TO_BIN(UUID());
SET @answer_3_2 = UUID_TO_BIN(UUID());

INSERT INTO semester(id, name, start_date, end_date)
VALUES (@semester_id, 'Current Semester', DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY),
        DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY)),
       (@semester_id2, 'Next Semester', DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY),
        DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY));
INSERT INTO room_pricing(id, price, total_slot)
VALUES (@room_pricing_id, 100000, 3);
INSERT INTO user(id, dob, email, full_name, gender, password, phone_number, role, user_code, username)
VALUES (@user_id, '2025-01-01', 'test@gmail.com', 'User Test', 'MALE',
        '$2a$12$Iw6V17tKjVLn6KBt4A/kPeLf5qnPPsL6hLMZ18i01EJXh5jZ1qfz6', '0987654321', 'RESIDENT', 'RESIDENT',
        'test'),
       (@user2_id, '2025-01-01', 'test2@gmail.com', 'User Test 2', 'MALE', '', '0987654322', 'RESIDENT', 'RESIDENT2',
        'test2'),
       (@user3_id, '2025-01-01', 'test3@gmail.com', 'User Test 3', 'MALE', '', '0987654323', 'RESIDENT', 'RESIDENT3',
        'test3');
INSERT INTO dorm(id, dorm_name, status, total_floor, total_room)
VALUES (@dorm_id, 'Dorm Test', null, 1, 1);
INSERT INTO room(id, floor, room_number, status, total_slot, dorm_id, pricing_id)
VALUES (@room_id, 1, 'Room Test', 'AVAILABLE', 3, @dorm_id, @room_pricing_id);
INSERT INTO slot(id, slot_name, status, room_id, user_id)
VALUES (@slot_id, 'Slot Test', 'UNAVAILABLE', @room_id, @user2_id),
       (@slot2_id, 'Slot Test 2', 'UNAVAILABLE', @room_id, @user3_id),
       (@slot3_id, 'Slot Test 3', 'AVAILABLE', @room_id, null);
INSERT INTO survey_question(id, question_content)
VALUES (@question_1, 'Do you smoke'),
       (@question_2, 'Do you have gf'),
       (@question_3, 'Do you sleep before 11pm');
INSERT INTO survey_option(id, option_content, survey_question_id)
VALUES (@answer_1_1, 'Yes', @question_1),
       (@answer_1_2, 'No', @question_1),
       (@answer_2_1, 'Yes', @question_2),
       (@answer_2_2, 'No', @question_2),
       (@answer_3_1, 'Yes', @question_3),
       (@answer_3_2, 'No', @question_3);
INSERT INTO survey_quetion_selected(id, survey_option_id, user_id)
VALUES (UUID_TO_BIN(UUID()), @answer_1_1, @user_id),
       (UUID_TO_BIN(UUID()), @answer_2_1, @user_id);