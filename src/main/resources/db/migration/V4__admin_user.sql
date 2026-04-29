INSERT INTO users (email, password_hash, role, created_at, updated_at)
VALUES (
           'admin@cinema.com',
           '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8RqtKwUgRSYMucqK2W',
           'ADMIN',
           NOW(),
           NOW()
       );