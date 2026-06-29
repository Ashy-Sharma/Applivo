-- USERS : Representing user details of all users.
create table users(
    id bigint auto_increment primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    username varchar(50) not null unique,
    role enum (
        'USER',
        'DEVELOPER',
        'ADMIN'
    ) not null default 'USER',
    avatar_url varchar(500) null,
    is_active boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,

    index idx_users_email (email),
    index idx_users_role (role),
    index idx_users_username (username)
);