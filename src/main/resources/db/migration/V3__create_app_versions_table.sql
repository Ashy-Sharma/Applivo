-- APP_VERSIONS : Representing different versions of apks uploaded by a developer.
create table app_versions (
    id bigint auto_increment primary key,
    app_id bigint not null,
    version_tag varchar(50) not null,
    file_path varchar(500) not null,
    size_bytes bigint not null,
    is_active boolean not null default true,
    uploaded_at timestamp not null default current_timestamp,

    foreign key (app_id) references apps (id) on delete cascade,

    index idx_versions_app (app_id),
    unique index idx_versions_unique (app_id, version_tag)
);