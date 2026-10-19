CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE documento_tag (
    documento_id BIGINT NOT NULL REFERENCES documento(id),
    tag_id BIGINT NOT NULL REFERENCES tag(id),
    PRIMARY KEY (documento_id, tag_id)
);
