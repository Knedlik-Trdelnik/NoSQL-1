INSERT INTO roles (role_name) VALUES
                                  ('Applicant'),
                                  ('Administrator'),
                                  ('Inspector');

INSERT INTO users (login, password, role_id) VALUES
                                                 ('applicant_user', 'pass123', 1),
                                                 ('admin_user', 'pass123', 2),
                                                 ('inspector_user', 'pass123', 3);

INSERT INTO classrooms (name) VALUES
                                  ('Аудитория 101'),
                                  ('Аудитория 202');

INSERT INTO time_windows (time_start, time_end, time_duration) VALUES
                                                                   ('09:00:00', '10:30:00', 90),
                                                                   ('10:45:00', '12:15:00', 90);

INSERT INTO bids (name, description, time_to_life, classroom_id) VALUES
    ('Заявка на лекцию', 'Лекция по БД', '2026-12-31 23:59:59', 1);

INSERT INTO bid_handler (bid_id, applicant_id, inspector_id) VALUES
    (1, 1, 3);

INSERT INTO bid_time_window (bid_id, time_window_id) VALUES
    (1, 1);