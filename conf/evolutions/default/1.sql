# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table book (
  id                        bigint not null,
  author                    varchar(255),
  title                     varchar(255),
  isbn                      varchar(255),
  cover_url                 varchar(255),
  year                      integer,
  exchangeable              boolean,
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
  password                  varchar(255),
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

