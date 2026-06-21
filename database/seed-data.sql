-- Passwords are 'password'
INSERT INTO users (username, password, role) VALUES
('alice', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ROLE_USER'),
('bob', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ROLE_USER'),
('charlie', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ROLE_USER'),
('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ROLE_ADMIN');

INSERT INTO accounts (id, user_id, holder_name, balance, status) VALUES
  (87654321, 1, 'Alice', 10000.00, 'ACTIVE'),
  (12345678, 2, 'Bob', 5000.00, 'ACTIVE'),
  (98765432, 3, 'Charlie', 2500.00, 'LOCKED'),
  (11223344, 4, 'Admin', 0.00, 'ACTIVE');

