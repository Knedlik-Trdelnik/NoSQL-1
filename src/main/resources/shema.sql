DROP TABLE IF EXISTS bid_time_window CASCADE;
DROP TABLE IF EXISTS bid_handler CASCADE;
DROP TABLE IF EXISTS time_windows CASCADE;
DROP TABLE IF EXISTS bids CASCADE;
DROP TABLE IF EXISTS classrooms CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       login VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role_id BIGINT NOT NULL REFERENCES roles(id)
);

CREATE TABLE classrooms (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL
);

CREATE TABLE bids (
                      id BIGSERIAL PRIMARY KEY,
                      name VARCHAR(255),
                      description TEXT,
                      time_to_life TIMESTAMP,
                      classroom_id BIGINT NOT NULL REFERENCES classrooms(id)
);

CREATE TABLE bid_handler (
                             bid_id BIGINT NOT NULL REFERENCES bids(id) ON DELETE CASCADE,
                             applicant_id BIGINT NOT NULL REFERENCES users(id),
                             inspector_id BIGINT NOT NULL REFERENCES users(id),
                             PRIMARY KEY (bid_id, applicant_id, inspector_id)
);

CREATE TABLE time_windows (
                              id BIGSERIAL PRIMARY KEY,
                              time_start TIME NOT NULL,
                              time_end TIME NOT NULL,
                              time_duration INT
);

CREATE TABLE bid_time_window (
                                 bid_id BIGINT NOT NULL REFERENCES bids(id) ON DELETE CASCADE,
                                 time_window_id BIGINT NOT NULL REFERENCES time_windows(id) ON DELETE CASCADE,
                                 PRIMARY KEY (bid_id, time_window_id)
);