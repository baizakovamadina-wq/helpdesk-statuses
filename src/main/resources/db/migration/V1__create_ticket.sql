
CREATE TABLE ticket (
                        id UUID PRIMARY KEY,
                        business_key TEXT NOT NULL UNIQUE,
                        status TEXT NOT NULL,
                        title TEXT NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                        CONSTRAINT ticket_status_known
                            CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED'))
);
