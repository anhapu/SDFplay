# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account (
  id                        bigint not null,
  email                     varchar(255),
  username                  varchar(255),
  firstname                 varchar(255),
  lastname                  varchar(255),
  role                      varchar(1),
  password                  varchar(255),
  constraint ck_account_role check (role in ('0','1')),
  constraint pk_account primary key (id))
;

create sequence account_seq;




# --- !Downs

drop table if exists account cascade;

drop sequence if exists account_seq;

