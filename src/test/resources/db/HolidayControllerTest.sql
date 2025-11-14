SET @semester_id = UUID_TO_BIN(UUID());
SET @user_id = UUID_TO_BIN(UUID());
SET @semester_start_date = DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY);
SET @semester_end_date = DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY);

INSERT INTO semester(id, end_date, name, start_date) VALUES (@semester_id, @semester_end_date, 'Semester Test', @semester_start_date);
INSERT INTO user(id, username, password, role) VALUES (@user_id, 'test', '$2a$12$Iw6V17tKjVLn6KBt4A/kPeLf5qnPPsL6hLMZ18i01EJXh5jZ1qfz6', 'RESIDENT')