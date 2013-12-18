# --- !Ups
SELECT setval('account_seq', 3, FALSE);

# --- !Downs
SELECT setval('account_seq', 1, FALSE);
