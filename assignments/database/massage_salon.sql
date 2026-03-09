CREATE TABLE users (
    id serial PRIMARY KEY,
    login varchar(30) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    name varchar(60) NOT NULL,
    role varchar(20) NOT NULL DEFAULT 'CLIENT',
    phone varchar(15) UNIQUE,
    additional_info text,
    created_at timestamp NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_users_phone CHECK (phone ~ '^\+?[0-9]{7,15}$'),
    CONSTRAINT chk_users_role CHECK (role IN ('CLIENT', 'SPECIALIST', 'ADMIN'))
);

CREATE TABLE specialists (
    id int PRIMARY KEY,
    experience int NOT NULL DEFAULT 0,
    active boolean NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_specialists_experience CHECK (experience >= 0),
    CONSTRAINT fk_specialists_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE services (
    id serial PRIMARY KEY,
    title varchar(100) NOT NULL UNIQUE,
    active boolean NOT NULL DEFAULT TRUE
);

CREATE TABLE specialist_services (
    id serial PRIMARY KEY,
    specialist_id int NOT NULL,
    service_id int NOT NULL,
    price numeric(10, 2) NOT NULL,
    active boolean NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_ss_price CHECK (price > 0),
    CONSTRAINT uq_ss_specialist_service_price UNIQUE (specialist_id, service_id, price),
    CONSTRAINT fk_ss_specialist FOREIGN KEY (specialist_id) REFERENCES specialists(id) ON DELETE CASCADE,
    CONSTRAINT fk_ss_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE TABLE appointments (
    id serial PRIMARY KEY,
    client_id int NOT NULL,
    specialist_service_id int NOT NULL,
    appointment_time timestamp NOT NULL,
    status varchar(15) NOT NULL DEFAULT 'PENDING',

    CONSTRAINT chk_appointments_status CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT uq_app_client_service_time UNIQUE (client_id, specialist_service_id, appointment_time),
    CONSTRAINT fk_app_client FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_specialist_service FOREIGN KEY (specialist_service_id) REFERENCES specialist_services(id) ON DELETE RESTRICT
);


CREATE OR REPLACE FUNCTION fn_check_active_specialist_service()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_active boolean;
BEGIN
    SELECT ss.active INTO v_active
    FROM specialist_services ss
    WHERE ss.id = NEW.specialist_service_id;

    IF NOT v_active THEN
        RAISE EXCEPTION
            'Нельзя создать запись: услуга специалиста (id=%) неактивна',
            NEW.specialist_service_id;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_check_active_specialist_service
    BEFORE INSERT ON appointments
    FOR EACH ROW EXECUTE FUNCTION fn_check_active_specialist_service();



CREATE OR REPLACE FUNCTION fn_check_active_on_ss_insert()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_specialist_active boolean;
    v_service_active boolean;
BEGIN
    SELECT active INTO v_specialist_active
    FROM specialists
    WHERE id = NEW.specialist_id;

    SELECT active INTO v_service_active
    FROM services
    WHERE id = NEW.service_id;

    IF NOT v_specialist_active THEN
        RAISE EXCEPTION
            'Нельзя добавить услугу: специалист (id=%) неактивен',
            NEW.specialist_id;
    END IF;

    IF NOT v_service_active THEN
        RAISE EXCEPTION
            'Нельзя добавить услугу: сервис (id=%) неактивен',
            NEW.service_id;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_check_active_on_ss_insert
    BEFORE INSERT ON specialist_services
    FOR EACH ROW EXECUTE FUNCTION fn_check_active_on_ss_insert();

