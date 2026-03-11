TRUNCATE TABLE appointments, specialist_services, specialists, services, users CASCADE;

ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE services_id_seq RESTART WITH 1;
ALTER SEQUENCE specialist_services_id_seq RESTART WITH 1;
ALTER SEQUENCE appointments_id_seq RESTART WITH 1;
