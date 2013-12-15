# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table book (
  id                        bigint not null,
  author                    varchar(255),
  title                     varchar(255),
  isbn                      varchar(255),
  cover_url                 varchar(255),
  year                      bigint,
  tradeable                 boolean,
  comment                   varchar(255),
  owner_id                  bigint,
  constraint pk_book primary key (id))
;

create table tradetransaction (
  id                        bigint not null,
  owner_id                  bigint,
  recipient_id              bigint,
  state                     varchar(12),
  comment_owner             varchar(255),
  comment_recipient         varchar(255),
  init_time                 timestamp not null,
  constraint ck_tradetransaction_state check (state in ('INIT','REFUSE','RESPONSE','FINAL_REFUSE','APPROVE','INVALID')),
  constraint uq_tradetransaction_1 unique (owner_id,recipient_id),
  constraint pk_tradetransaction primary key (id))
;

create table account (
  id                        bigint not null,
  email                     varchar(255),
  username                  varchar(255),
  firstname                 varchar(255),
  lastname                  varchar(255),
  role                      varchar(1),
  password                  varchar(255),
  token                     varchar(255),
  token_created_at          timestamp,
  constraint ck_account_role check (role in ('0','1')),
  constraint pk_account primary key (id))
;


create table tradetransaction_book (
  tradetransaction_id            bigint not null,
  book_id                        bigint not null,
  constraint pk_tradetransaction_book primary key (tradetransaction_id, book_id))
;
create sequence book_seq;

create sequence tradetransaction_seq;

create sequence account_seq;

alter table book add constraint fk_book_owner_1 foreign key (owner_id) references account (id);
create index ix_book_owner_1 on book (owner_id);
alter table tradetransaction add constraint fk_tradetransaction_owner_2 foreign key (owner_id) references account (id);
create index ix_tradetransaction_owner_2 on tradetransaction (owner_id);
alter table tradetransaction add constraint fk_tradetransaction_recipient_3 foreign key (recipient_id) references account (id);
create index ix_tradetransaction_recipient_3 on tradetransaction (recipient_id);



<<<<<<< HEAD

# --- !Downs

=======
alter table tradetransaction_book add constraint fk_tradetransaction_book_trad_01 foreign key (tradetransaction_id) references tradetransaction (id);

alter table tradetransaction_book add constraint fk_tradetransaction_book_book_02 foreign key (book_id) references book (id);

# --- !Downs

>>>>>>> 933e8a52ef931b4d5eaafc54dcec2bb023488748
drop table if exists book cascade;

drop table if exists tradetransaction_book cascade;

drop table if exists tradetransaction cascade;

drop table if exists account cascade;

drop sequence if exists book_seq;

drop sequence if exists tradetransaction_seq;

drop sequence if exists account_seq;

