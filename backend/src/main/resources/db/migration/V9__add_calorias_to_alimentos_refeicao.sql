-- ====================================
-- V9: Adicionar campo calorias em alimentos_refeicao
-- ====================================

ALTER TABLE tbl_alimentos_refeicao ADD COLUMN calorias DECIMAL(8,2) AFTER unidade;
