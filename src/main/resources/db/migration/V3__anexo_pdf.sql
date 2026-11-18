ALTER TABLE documento ADD COLUMN nome_pdf VARCHAR(255);
CREATE TABLE anexo_pdf (
    documento_id BIGINT PRIMARY KEY REFERENCES documento(id) ON DELETE CASCADE,
    conteudo BYTEA NOT NULL
);
