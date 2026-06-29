-- APPS : Representing android application uploaded on the platform.
create table apps (
    id bigint auto_increment primary key,
    dev_id bigint not null,
    name varchar(100) not null,
    description text null,
    icon_url varchar(500) null,
    category varchar(50) null,
    is_published boolean not null default false,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,

    foreign key (dev_id) references users (id) on delete cascade,

    index idx_apps_developer (dev_id),
    index idx_apps_published (is_published),
    index idx_apps_category (category),
    index idx_apps_name (name),

    fulltext index idx_apps_search (name, description)
);