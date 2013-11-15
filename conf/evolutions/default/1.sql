# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account (
  id                        bigint not null,
  email                     varchar(255),
  username                  varchar(255),
  firstname                 varchar(255),
  lastname                  varchar(255),
  password                  varchar(255),
  constraint pk_account primary key (id))
;

create sequence account_seq;

create table books (
  id                        bigint not null,
  title                     varchar(255),
  author                    varchar(255),
  isbn                      varchar(255),
  coverUrl                  varchar(255),
  year                      integer,
  swabable                  boolean,
  comment                   varchar(255)
  owner                     bigint not null,
  constraint pk_book primary key (id),
  contraint  owner_fk   foreign key(account.id))
;

create sequence book_seq;

# --- !Downs

drop table if exists account cascade;
drop table if exists books cascade;

drop sequence if exists book_seq;
drop sequence if exists account_seq;

