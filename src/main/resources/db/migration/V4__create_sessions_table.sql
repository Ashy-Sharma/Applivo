create table sessions (
    id bigint auto_increment primary key,
    app_version_id bigint not null,
    user_id bigint not null,
    emulator_container_id varchar(100) null,
    status enum (
        'CREATING',
        'ACTIVE',
        'FAILED',
        'ENDED',
        'TIMED_OUT'
    ) not null default 'CREATING',
    started_at timestamp not null default current_timestamp,
    ended_at timestamp null,
    last_activity_at timestamp not null default current_timestamp,

    foreign key (app_version_id) references app_versions (id),
    foreign key (user_id) references users (id),

    index idx_sessions_user (user_id),
    index idx_sessions_status (status),
    index idx_sessions_user_active (user_id, status),
    index idx_sessions_versions (app_version_id)
);