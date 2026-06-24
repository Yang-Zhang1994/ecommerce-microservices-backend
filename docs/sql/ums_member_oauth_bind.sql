-- PostgreSQL: Google (and future) OAuth subject → ums_member
-- Apply on the member database before using POST /member/member/oauth/google.

CREATE TABLE IF NOT EXISTS ums_member_oauth_bind (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    provider VARCHAR(32) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_oauth_provider_subject UNIQUE (provider, provider_user_id),
    CONSTRAINT fk_oauth_member FOREIGN KEY (member_id) REFERENCES ums_member (id)
);

CREATE INDEX IF NOT EXISTS idx_ums_member_oauth_bind_member_id ON ums_member_oauth_bind (member_id);
