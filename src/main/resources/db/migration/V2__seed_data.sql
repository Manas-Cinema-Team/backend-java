-- Залы
INSERT INTO halls (name, rows, seats_per_row) VALUES ('Зал 1', 10, 12);
INSERT INTO halls (name, rows, seats_per_row) VALUES ('Зал 2', 8, 10);

-- Фильмы
INSERT INTO movies (title, description, genre, duration, age_rating, poster_url, release_date, is_active)
VALUES ('Inception', 'Вор, крадущий секреты из снов', 'Action', 148, '12+',
        'https://example.com/inception.jpg', '2010-07-16', true);

INSERT INTO movies (title, description, genre, duration, age_rating, poster_url, release_date, is_active)
VALUES ('The Dark Knight', 'Бэтмен против Джокера', 'Action', 152, '12+',
        'https://example.com/darknight.jpg', '2008-07-18', true);

INSERT INTO movies (title, description, genre, duration, age_rating, poster_url, release_date, is_active)
VALUES ('Interstellar', 'Путешествие сквозь червоточину', 'Drama', 169, '6+',
        'https://example.com/interstellar.jpg', '2014-11-07', true);

-- Сеансы
INSERT INTO sessions (movies_id, hall_id, start_datetime, end_datetime, is_active)
VALUES (1, 1, '2026-05-01T10:00:00', '2026-05-01T12:28:00', true);

INSERT INTO sessions (movies_id, hall_id, start_datetime, end_datetime, is_active)
VALUES (2, 1, '2026-05-01T13:00:00', '2026-05-01T15:32:00', true);

INSERT INTO sessions (movies_id, hall_id, start_datetime, end_datetime, is_active)
VALUES (3, 2, '2026-05-01T16:00:00', '2026-05-01T18:49:00', true);

INSERT INTO sessions (movies_id, hall_id, start_datetime, end_datetime, is_active)
VALUES (1, 2, '2026-05-02T10:00:00', '2026-05-02T12:28:00', true);

INSERT INTO sessions (movies_id, hall_id, start_datetime, end_datetime, is_active)
VALUES (3, 1, '2026-05-02T14:00:00', '2026-05-02T16:49:00', true);

-- Цены
INSERT INTO ticket_prices (session_id, amount, currency, pricing_source)
VALUES (1, 350.00, 'KGS', 'standard');

INSERT INTO ticket_prices (session_id, amount, currency, pricing_source)
VALUES (2, 350.00, 'KGS', 'standard');

INSERT INTO ticket_prices (session_id, amount, currency, pricing_source)
VALUES (3, 400.00, 'KGS', 'standard');

INSERT INTO ticket_prices (session_id, amount, currency, pricing_source)
VALUES (4, 350.00, 'KGS', 'standard');

INSERT INTO ticket_prices (session_id, amount, currency, pricing_source)
VALUES (5, 400.00, 'KGS', 'standard');
