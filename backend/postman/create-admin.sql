INSERT INTO users (login, password_hash, name, role, phone, created_at) 
VALUES (
  'test_admin',
  '$2a$10$6xXvsgmQGpwnPRU1kF9juOscydDAoAhukbjm/a6FI4nOk3kxIzopG',
  'Admin',
  'ADMIN',
  '+10000000001',
  NOW()
);
