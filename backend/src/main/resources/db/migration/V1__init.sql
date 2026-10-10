CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS donor_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    blood_group VARCHAR(6) NOT NULL,
    date_of_birth DATE,
    city VARCHAR(120),
    approx_lat DOUBLE,
    approx_lng DOUBLE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    verification_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    last_donation_date DATE,
    next_eligible_date DATE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recipient_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    city VARCHAR(120),
    approx_lat DOUBLE,
    approx_lng DOUBLE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_donor_profiles_availability ON donor_profiles(is_available, blood_group);
CREATE INDEX idx_donor_profiles_location ON donor_profiles(approx_lat, approx_lng);
