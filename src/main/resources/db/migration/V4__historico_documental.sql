ALTER TABLE documento_versao ADD COLUMN conteudo BYTEA;
ALTER TABLE documento_versao ALTER COLUMN enviado_por DROP NOT NULL;
ALTER TABLE historico_status ALTER COLUMN alterado_por DROP NOT NULL;
