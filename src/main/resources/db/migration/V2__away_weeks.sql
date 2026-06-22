-- Nouveau sondage : semaines d'absence en été. Remplace l'ancien sondage.
ALTER TABLE survey_response ADD COLUMN away_weeks TEXT;

-- Nettoyage de l'ancien sondage (remplacement complet).
DROP TABLE IF EXISTS service_shift;
DROP SEQUENCE IF EXISTS service_shift_SEQ;
ALTER TABLE survey_response DROP COLUMN IF EXISTS shopping_slots;
