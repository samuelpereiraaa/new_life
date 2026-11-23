ALTER TABLE anexo_pdf ADD COLUMN enviado_por BIGINT REFERENCES usuario(id);
