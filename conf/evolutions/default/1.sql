# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table favorite_recipe (
  id                            bigint auto_increment not null,
  recipe_id                     bigint not null,
  user_id                       bigint not null,
  constraint pk_favorite_recipe primary key (id)
);

create table ingredient (
  id                            bigint auto_increment not null,
  constraint pk_ingredient primary key (id)
);

create table ingredient_alt_name (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  ingredient_name_id            bigint not null,
  constraint pk_ingredient_alt_name primary key (id)
);

create table ingredient_name (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  ingredient_id                 bigint,
  language_id                   bigint,
  constraint pk_ingredient_name primary key (id)
);

create table ingredient_tag (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint pk_ingredient_tag primary key (id)
);

create table ingredient_tag_ingredient (
  ingredient_tag_id             bigint not null,
  ingredient_id                 bigint not null,
  constraint pk_ingredient_tag_ingredient primary key (ingredient_tag_id,ingredient_id)
);

create table language (
  id                            bigint auto_increment not null,
  iso_name                      varchar(255),
  constraint pk_language primary key (id)
);

create table measure (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint pk_measure primary key (id)
);

create table recipe (
  id                            bigint auto_increment not null,
  name                          clob,
  url                           clob,
  date_added                    timestamp,
  numofings                     integer,
  time                          integer,
  source_page_id                bigint,
  constraint pk_recipe primary key (id)
);

create table recipe_description (
  id                            bigint auto_increment not null,
  recipe_id                     bigint not null,
  language_id                   bigint not null,
  text                          clob,
  constraint pk_recipe_description primary key (id)
);

create table recipe_ingredient (
  id                            bigint auto_increment not null,
  ingredient_id                 bigint not null,
  recipe_id                     bigint not null,
  amount                        integer,
  measure_id                    bigint,
  constraint pk_recipe_ingredient primary key (id)
);

create table recipe_search (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  user_id                       bigint,
  query                         varchar(255),
  constraint pk_recipe_search primary key (id)
);

create table source_page (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  language_id                   bigint not null,
  constraint pk_source_page primary key (id)
);

create table user (
  id                            bigint auto_increment not null,
  constraint pk_user primary key (id)
);

create index ix_favorite_recipe_recipe_id on favorite_recipe (recipe_id);
alter table favorite_recipe add constraint fk_favorite_recipe_recipe_id foreign key (recipe_id) references recipe (id) on delete restrict on update restrict;

create index ix_favorite_recipe_user_id on favorite_recipe (user_id);
alter table favorite_recipe add constraint fk_favorite_recipe_user_id foreign key (user_id) references user (id) on delete restrict on update restrict;

create index ix_ingredient_alt_name_ingredient_name_id on ingredient_alt_name (ingredient_name_id);
alter table ingredient_alt_name add constraint fk_ingredient_alt_name_ingredient_name_id foreign key (ingredient_name_id) references ingredient_name (id) on delete restrict on update restrict;

create index ix_ingredient_name_ingredient_id on ingredient_name (ingredient_id);
alter table ingredient_name add constraint fk_ingredient_name_ingredient_id foreign key (ingredient_id) references ingredient (id) on delete restrict on update restrict;

create index ix_ingredient_name_language_id on ingredient_name (language_id);
alter table ingredient_name add constraint fk_ingredient_name_language_id foreign key (language_id) references language (id) on delete restrict on update restrict;

create index ix_ingredient_tag_ingredient_ingredient_tag on ingredient_tag_ingredient (ingredient_tag_id);
alter table ingredient_tag_ingredient add constraint fk_ingredient_tag_ingredient_ingredient_tag foreign key (ingredient_tag_id) references ingredient_tag (id) on delete restrict on update restrict;

create index ix_ingredient_tag_ingredient_ingredient on ingredient_tag_ingredient (ingredient_id);
alter table ingredient_tag_ingredient add constraint fk_ingredient_tag_ingredient_ingredient foreign key (ingredient_id) references ingredient (id) on delete restrict on update restrict;

create index ix_recipe_source_page_id on recipe (source_page_id);
alter table recipe add constraint fk_recipe_source_page_id foreign key (source_page_id) references source_page (id) on delete restrict on update restrict;

create index ix_recipe_description_recipe_id on recipe_description (recipe_id);
alter table recipe_description add constraint fk_recipe_description_recipe_id foreign key (recipe_id) references recipe (id) on delete restrict on update restrict;

create index ix_recipe_description_language_id on recipe_description (language_id);
alter table recipe_description add constraint fk_recipe_description_language_id foreign key (language_id) references language (id) on delete restrict on update restrict;

create index ix_recipe_ingredient_ingredient_id on recipe_ingredient (ingredient_id);
alter table recipe_ingredient add constraint fk_recipe_ingredient_ingredient_id foreign key (ingredient_id) references ingredient (id) on delete restrict on update restrict;

create index ix_recipe_ingredient_recipe_id on recipe_ingredient (recipe_id);
alter table recipe_ingredient add constraint fk_recipe_ingredient_recipe_id foreign key (recipe_id) references recipe (id) on delete restrict on update restrict;

create index ix_recipe_ingredient_measure_id on recipe_ingredient (measure_id);
alter table recipe_ingredient add constraint fk_recipe_ingredient_measure_id foreign key (measure_id) references measure (id) on delete restrict on update restrict;

create index ix_recipe_search_user_id on recipe_search (user_id);
alter table recipe_search add constraint fk_recipe_search_user_id foreign key (user_id) references user (id) on delete restrict on update restrict;

create index ix_source_page_language_id on source_page (language_id);
alter table source_page add constraint fk_source_page_language_id foreign key (language_id) references language (id) on delete restrict on update restrict;


# --- !Downs

alter table favorite_recipe drop constraint if exists fk_favorite_recipe_recipe_id;
drop index if exists ix_favorite_recipe_recipe_id;

alter table favorite_recipe drop constraint if exists fk_favorite_recipe_user_id;
drop index if exists ix_favorite_recipe_user_id;

alter table ingredient_alt_name drop constraint if exists fk_ingredient_alt_name_ingredient_name_id;
drop index if exists ix_ingredient_alt_name_ingredient_name_id;

alter table ingredient_name drop constraint if exists fk_ingredient_name_ingredient_id;
drop index if exists ix_ingredient_name_ingredient_id;

alter table ingredient_name drop constraint if exists fk_ingredient_name_language_id;
drop index if exists ix_ingredient_name_language_id;

alter table ingredient_tag_ingredient drop constraint if exists fk_ingredient_tag_ingredient_ingredient_tag;
drop index if exists ix_ingredient_tag_ingredient_ingredient_tag;

alter table ingredient_tag_ingredient drop constraint if exists fk_ingredient_tag_ingredient_ingredient;
drop index if exists ix_ingredient_tag_ingredient_ingredient;

alter table recipe drop constraint if exists fk_recipe_source_page_id;
drop index if exists ix_recipe_source_page_id;

alter table recipe_description drop constraint if exists fk_recipe_description_recipe_id;
drop index if exists ix_recipe_description_recipe_id;

alter table recipe_description drop constraint if exists fk_recipe_description_language_id;
drop index if exists ix_recipe_description_language_id;

alter table recipe_ingredient drop constraint if exists fk_recipe_ingredient_ingredient_id;
drop index if exists ix_recipe_ingredient_ingredient_id;

alter table recipe_ingredient drop constraint if exists fk_recipe_ingredient_recipe_id;
drop index if exists ix_recipe_ingredient_recipe_id;

alter table recipe_ingredient drop constraint if exists fk_recipe_ingredient_measure_id;
drop index if exists ix_recipe_ingredient_measure_id;

alter table recipe_search drop constraint if exists fk_recipe_search_user_id;
drop index if exists ix_recipe_search_user_id;

alter table source_page drop constraint if exists fk_source_page_language_id;
drop index if exists ix_source_page_language_id;

drop table if exists favorite_recipe;

drop table if exists ingredient;

drop table if exists ingredient_alt_name;

drop table if exists ingredient_name;

drop table if exists ingredient_tag;

drop table if exists ingredient_tag_ingredient;

drop table if exists language;

drop table if exists measure;

drop table if exists recipe;

drop table if exists recipe_description;

drop table if exists recipe_ingredient;

drop table if exists recipe_search;

drop table if exists source_page;

drop table if exists user;

