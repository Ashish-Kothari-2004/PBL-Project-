CREATE TABLE blood_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_user_id BIGINT NOT NULL,
    blood_group VARCHAR(6) NOT NULL,
    city VARCHAR(120),
    approx_lat DOUBLE,
    approx_lng DOUBLE,
    radius_km INT NOT NULL DEFAULT 50,
    status VARCHAR(24) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_blood_requests_recipient FOREIGN KEY (recipient_user_id) REFERENCES users(id)
);

CREATE INDEX idx_blood_requests_recipient ON blood_requests(recipient_user_id, status);

CREATE TABLE chat_rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blood_request_id BIGINT NOT NULL,
    donor_user_id BIGINT NOT NULL,
    recipient_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_rooms_request FOREIGN KEY (blood_request_id) REFERENCES blood_requests(id),
    CONSTRAINT fk_chat_rooms_donor FOREIGN KEY (donor_user_id) REFERENCES users(id),
    CONSTRAINT fk_chat_rooms_recipient FOREIGN KEY (recipient_user_id) REFERENCES users(id),
    CONSTRAINT uq_chat_rooms_request_donor UNIQUE (blood_request_id, donor_user_id)
);

CREATE INDEX idx_chat_rooms_donor ON chat_rooms(donor_user_id);
CREATE INDEX idx_chat_rooms_recipient ON chat_rooms(recipient_user_id);

CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    sender_user_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_messages_room FOREIGN KEY (room_id) REFERENCES chat_rooms(id),
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_user_id) REFERENCES users(id)
);

CREATE INDEX idx_chat_messages_room_created ON chat_messages(room_id, created_at);