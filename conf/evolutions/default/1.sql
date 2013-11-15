# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table books (
  id                        bigint not null,
  isbn                      varchar(255) not null,
  author                    varchar(255),
  title                     varchar(255),
  cover_url                 varchar(255),
  year                      integer,
  swabable                  boolean,
  comment                   varchar(255),
  owner_id                  bigint)
;

create table account (
  id                        bigint not null,
  email                     varchar(255),
  username                  varchar(255),
  firstname                 varchar(255),
  lastname                  varchar(255),
  password                  varchar(255),
  constraint pk_account primary key (id))
;

create sequence books_seq;

create sequence account_seq;

alter table books add constraint fk_books_owner_1 foreign key (owner_id) references account (id);
create index ix_books_owner_1 on books (owner_id);



# --- !Downs

drop table if exists books cascade;

drop table if exists account cascade;

drop sequence if exists books_seq;

drop sequence if exists account_seq;

