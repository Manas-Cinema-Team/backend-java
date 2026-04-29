-- Session
ALTER TABLE sessions
ALTER COLUMN start_datetime TYPE TIMESTAMP WITH TIME ZONE
        USING start_datetime AT TIME ZONE 'UTC',
    ALTER COLUMN end_datetime TYPE TIMESTAMP WITH TIME ZONE
        USING end_datetime AT TIME ZONE 'UTC';

-- Booking
ALTER TABLE bookings
ALTER COLUMN confirmed_at TYPE TIMESTAMP WITH TIME ZONE
        USING confirmed_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE
        USING created_at AT TIME ZONE 'UTC';

-- SeatHold
ALTER TABLE seat_holds
ALTER COLUMN expires_at TYPE TIMESTAMP WITH TIME ZONE
        USING expires_at AT TIME ZONE 'UTC';

-- User
ALTER TABLE users
ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE
        USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE
        USING updated_at AT TIME ZONE 'UTC';