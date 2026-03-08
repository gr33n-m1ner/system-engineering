CREATE TYPE role_enum AS ENUM ('client', 'specialist', 'admin');
CREATE TYPE appointment_status_enum AS ENUM ('pending', 'confirmed', 'completed', 'cancelled');

CREATE TABLE users (
    id serial PRIMARY KEY,
    login varchar(30) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    name varchar(60) NOT NULL,
    role role_enum NOT NULL DEFAULT 'client',
    phone varchar(15) UNIQUE,
    additional_info text,
    created_at timestamp NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_users_phone CHECK (phone ~ '^\+?[0-9]{7,15}$')
);

CREATE TABLE specialists (
    id int PRIMARY KEY,
    experience int NOT NULL DEFAULT 0 CHECK (experience >= 0),

    CONSTRAINT fk_specialists_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE services (
    id serial PRIMARY KEY,
    title varchar(100) NOT NULL UNIQUE
);

CREATE TABLE specialist_services (
    id serial PRIMARY KEY,
    specialist_id int NOT NULL,
    service_id int NOT NULL,
    price numeric(10, 2) NOT NULL CHECK (price > 0),

    CONSTRAINT uq_specialist_service UNIQUE (specialist_id, service_id),
    CONSTRAINT fk_ss_specialist FOREIGN KEY (specialist_id) REFERENCES specialists(id) ON DELETE CASCADE,
    CONSTRAINT fk_ss_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE TABLE slots (
    id serial PRIMARY KEY,
    specialist_id int NOT NULL,
    day_of_week int NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time time NOT NULL,
    end_time time NOT NULL,

    CONSTRAINT uq_slot UNIQUE (specialist_id, day_of_week, start_time),
    CONSTRAINT chk_slot_times CHECK (end_time > start_time),
    CONSTRAINT fk_slots_specialist FOREIGN KEY (specialist_id) REFERENCES specialists(id) ON DELETE CASCADE
);

CREATE TABLE appointments (
    id serial PRIMARY KEY,
    client_id int NOT NULL,
    slot_id int NOT NULL,
    service_id int NOT NULL,
    appointment_date date NOT NULL,
    status appointment_status_enum NOT NULL DEFAULT 'pending',
    created_at timestamp NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_appointment_slot_date UNIQUE (slot_id, appointment_date),
    CONSTRAINT fk_app_client FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_slot FOREIGN KEY (slot_id) REFERENCES slots(id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_service FOREIGN KEY (service_id) REFERENCES specialist_services(id) ON DELETE RESTRICT
);


CREATE OR REPLACE FUNCTION fn_check_slot_service_match()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    slot_specialist_id int;
    service_specialist_id int;
BEGIN
    SELECT specialist_id INTO slot_specialist_id
    FROM slots WHERE id = NEW.slot_id;

    SELECT specialist_id INTO service_specialist_id
    FROM specialist_services WHERE id = NEW.service_id;

    IF slot_specialist_id != service_specialist_id THEN
        RAISE EXCEPTION 'Слот и услуга принадлежат разным специалистам (slot_specialist=%, service_specialist=%)',
            slot_specialist_id, service_specialist_id;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_appointment_consistency
    BEFORE INSERT OR UPDATE ON appointments
    FOR EACH ROW EXECUTE FUNCTION fn_check_slot_service_match();


CREATE OR REPLACE FUNCTION fn_check_slot_overlap()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM slots
        WHERE specialist_id = NEW.specialist_id
          AND day_of_week = NEW.day_of_week
          AND id != NEW.id
          AND (NEW.start_time, NEW.end_time) OVERLAPS (start_time, end_time)
    ) THEN
        RAISE EXCEPTION 'Новый слот пересекается с уже существующим у специалиста %', NEW.specialist_id;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_slot_overlap
    BEFORE INSERT OR UPDATE ON slots
    FOR EACH ROW EXECUTE FUNCTION fn_check_slot_overlap();


