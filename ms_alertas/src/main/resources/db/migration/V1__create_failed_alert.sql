-- Flyway migration: create failed_alert table
CREATE TABLE IF NOT EXISTS failed_alert (
  id BIGSERIAL PRIMARY KEY,
  report_id BIGINT,
  payload text,
  error varchar(1000),
  attempts integer DEFAULT 0,
  created_at timestamp with time zone DEFAULT now()
);
