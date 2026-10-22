CREATE TABLE assistant_conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assistant_conversations_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE assistant_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL,
    content VARCHAR(8000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assistant_messages_conversation FOREIGN KEY (conversation_id)
        REFERENCES assistant_conversations(id) ON DELETE CASCADE
);

CREATE INDEX idx_assistant_messages_conversation_created
    ON assistant_messages(conversation_id, created_at);

CREATE TABLE blood_request_responses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blood_request_id BIGINT NOT NULL,
    donor_user_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_blood_request_responses_request FOREIGN KEY (blood_request_id)
        REFERENCES blood_requests(id),
    CONSTRAINT fk_blood_request_responses_donor FOREIGN KEY (donor_user_id)
        REFERENCES users(id),
    CONSTRAINT uq_blood_request_response_donor UNIQUE (blood_request_id, donor_user_id)
);

CREATE INDEX idx_blood_request_responses_request ON blood_request_responses(blood_request_id);
