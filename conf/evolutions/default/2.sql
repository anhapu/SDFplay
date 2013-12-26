# --- !Ups
SELECT setval('account_seq', 10, FALSE);

# --- !Downs
SELECT setval('account_seq', 1, FALSE);
