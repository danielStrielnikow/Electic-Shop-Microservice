INSERT INTO users (uuid, email, password, user_role, email_verified, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin@electricshop.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCy',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
