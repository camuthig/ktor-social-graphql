create table user_identities (
  user_id integer not null,
  provider varchar not null,
	id varchar not null,
	foreign key (user_id) references "users"(id)
);

create unique index idx__user_identities__user_id__provider__id on user_identities(provider, id, user_id);
create index idx__user_identities__provider__id on user_identities(provider, id);
