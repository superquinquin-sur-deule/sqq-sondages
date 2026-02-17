CREATE SEQUENCE survey_response_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE service_shift_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE survey_response (
    id BIGINT PRIMARY KEY DEFAULT nextval('survey_response_SEQ'),
    submitted_at TIMESTAMP NOT NULL,
    shopping_slots TEXT
);

CREATE TABLE service_shift (
    id BIGINT PRIMARY KEY DEFAULT nextval('service_shift_SEQ'),
    response_id BIGINT NOT NULL REFERENCES survey_response(id),
    day VARCHAR(255) NOT NULL,
    time_slot VARCHAR(255) NOT NULL,
    priority INTEGER NOT NULL
);
