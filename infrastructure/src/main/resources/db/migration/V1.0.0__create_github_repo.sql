CREATE TABLE github_repo
(
    id               BIGINT       PRIMARY KEY,
    owner            VARCHAR(255) NOT NULL,
    name             VARCHAR(255) NOT NULL,
    full_name        VARCHAR(511) NOT NULL UNIQUE,
    description      TEXT,
    language         VARCHAR(100),
    stargazers_count INT          NOT NULL DEFAULT 0,
    forks_count      INT          NOT NULL DEFAULT 0,
    is_private       BOOLEAN      NOT NULL DEFAULT false,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_github_repo_owner ON github_repo (owner);
CREATE INDEX idx_github_repo_language ON github_repo (language);
CREATE INDEX idx_github_repo_stargazers ON github_repo (stargazers_count DESC);
