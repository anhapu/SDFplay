# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table book (
  id                        bigint not null,
  author                    varchar(255),
  title                     varchar(255),
  subtitle                  varchar(255),
  isbn                      varchar(255),
  cover_url                 varchar(255),
  year                      integer,
  tradeable                 boolean,
  comment                   varchar(255),
  owner_id                  bigint,
  constraint pk_book primary key (id))
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

create sequence book_seq;

create sequence account_seq;

alter table book add constraint fk_book_owner_1 foreign key (owner_id) references account (id);
create index ix_book_owner_1 on book (owner_id);



# --- !Downs

drop table if exists book cascade;

drop table if exists account cascade;

drop sequence if exists book_seq;

drop sequence if exists account_seq;

