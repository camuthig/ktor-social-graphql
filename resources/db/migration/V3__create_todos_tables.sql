create table todo_lists (
  id serial primary key,
  title text not null,
  description text default null,
  created_by integer not null,
  foreign key (created_by) references users(id)
);

create table todos (
  id serial primary key,
  title text not null,
  description text default null,
  is_completed boolean default false,
  assigned_to_id integer default null,
  todo_list_id integer default null,
  created_by integer not null,
  foreign key (assigned_to_id) references users(id),
  foreign key (todo_list_id) references  todo_lists(id),
  foreign key (created_by) references users(id)
);