CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_masked VARCHAR(32) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    avatar_url VARCHAR(512),
    risk_status VARCHAR(24) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE TABLE community (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE vehicle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plate_number VARCHAR(24) NOT NULL,
    vehicle_type VARCHAR(32) NOT NULL,
    verification_status VARCHAR(24) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_vehicle_plate UNIQUE (plate_number),
    CONSTRAINT fk_vehicle_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE TABLE parking_space (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    community_id BIGINT NOT NULL,
    space_code VARCHAR(64) NOT NULL,
    title VARCHAR(128) NOT NULL,
    access_instructions VARCHAR(1000) NOT NULL,
    vehicle_limit VARCHAR(64) NOT NULL,
    status VARCHAR(24) NOT NULL,
    review_note VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT fk_space_owner FOREIGN KEY (owner_id) REFERENCES app_user (id),
    CONSTRAINT fk_space_community FOREIGN KEY (community_id) REFERENCES community (id)
);

CREATE TABLE availability_slot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    space_id BIGINT NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    status VARCHAR(24) NOT NULL,
    free_of_charge BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT fk_slot_space FOREIGN KEY (space_id) REFERENCES parking_space (id),
    CONSTRAINT ck_slot_time CHECK (end_at > start_at)
);

CREATE TABLE booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_no VARCHAR(48) NOT NULL,
    slot_id BIGINT NOT NULL,
    space_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    renter_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    status VARCHAR(24) NOT NULL,
    cancel_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_booking_no UNIQUE (booking_no),
    CONSTRAINT fk_booking_slot FOREIGN KEY (slot_id) REFERENCES availability_slot (id),
    CONSTRAINT fk_booking_space FOREIGN KEY (space_id) REFERENCES parking_space (id),
    CONSTRAINT fk_booking_owner FOREIGN KEY (owner_id) REFERENCES app_user (id),
    CONSTRAINT fk_booking_renter FOREIGN KEY (renter_id) REFERENCES app_user (id),
    CONSTRAINT fk_booking_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id),
    CONSTRAINT ck_booking_time CHECK (end_at > start_at)
);

CREATE INDEX idx_booking_space_time ON booking (space_id, start_at, end_at, status);
CREATE INDEX idx_booking_renter ON booking (renter_id, created_at);
CREATE INDEX idx_booking_owner ON booking (owner_id, created_at);

CREATE TABLE check_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    check_type VARCHAR(24) NOT NULL,
    operator_id BIGINT NOT NULL,
    checked_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT fk_check_booking FOREIGN KEY (booking_id) REFERENCES booking (id),
    CONSTRAINT fk_check_operator FOREIGN KEY (operator_id) REFERENCES app_user (id)
);

CREATE TABLE review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    content VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_review_booking UNIQUE (booking_id),
    CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES booking (id),
    CONSTRAINT fk_review_user FOREIGN KEY (reviewer_id) REFERENCES app_user (id),
    CONSTRAINT ck_review_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE complaint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    complainant_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    status VARCHAR(24) NOT NULL,
    resolution_note VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT fk_complaint_booking FOREIGN KEY (booking_id) REFERENCES booking (id),
    CONSTRAINT fk_complaint_user FOREIGN KEY (complainant_id) REFERENCES app_user (id)
);

CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id BIGINT NOT NULL,
    detail VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT fk_audit_operator FOREIGN KEY (operator_id) REFERENCES app_user (id)
);
