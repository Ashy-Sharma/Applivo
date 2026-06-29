-- EVENTS : Representing event logs for a session.
create table events (
    id bigint auto_increment primary key,
    app_id bigint null,
    user_id bigint null,
    event_type enum (
        'DEMO_STARTED',
        'DEMO_ENDED',
        'APP_VIEWED',
        'APP_SEARCHED',
        'APK_UPLOADED',
        'APK_DOWNLOADED'
    ) not null,
    metadata json null,
    session_id bigint null,
    created_at timestamp not null default current_timestamp,

    foreign key (app_id) references apps (id) on delete set null,
    foreign key (user_id) references users (id) on delete set null,
    foreign key (session_id) references sessions (id) on delete set null,

    index idx_analytics_app (app_id),
    index idx_analytics_type (event_type),
    index idx_analytics_created (created_at),
    index idx_analytics_app_type_data (app_id, event_type, created_at),
    index idx_analytics_session (session_id)

);
