# --- !Ups

SELECT setval('account_seq', 2, FALSE);

# --- !Downs

SELECT setval('account_seq', 1, FALSE);