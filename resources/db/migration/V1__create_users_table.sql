create table users (
	id serial primary key,
	name varchar not null,
	nickname varchar not null,
	email varchar not null,
	avatar_url varchar default null
)