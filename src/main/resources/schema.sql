CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL
);

-- Optional trigger to send NOTIFY events
DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM pg_trigger WHERE tgname = 'users_notify_changes') THEN
    CREATE OR REPLACE FUNCTION notify_users_change() RETURNS trigger AS $$
    DECLARE
      payload text;
      action text;
    BEGIN
      IF (TG_OP = 'INSERT') THEN
        action := 'CREATED';
        payload := 'users:' || action || ':' || NEW.id::text;
      ELSIF (TG_OP = 'UPDATE') THEN
        action := 'UPDATED';
        payload := 'users:' || action || ':' || NEW.id::text;
      ELSIF (TG_OP = 'DELETE') THEN
        action := 'DELETED';
        payload := 'users:' || action || ':' || OLD.id::text;
      END IF;
      PERFORM pg_notify(coalesce(current_setting('cache.pg.channel', true), 'cache_changes'), payload);
      RETURN COALESCE(NEW, OLD);
    END;
    $$ LANGUAGE plpgsql;

    CREATE TRIGGER users_notify_changes
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW EXECUTE PROCEDURE notify_users_change();
  END IF;
END$$;
