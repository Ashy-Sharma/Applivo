-- EMULATOR_INSTANCES : Representing emulator containers.
create table emulator_instances (
    id bigint auto_increment primary key,
    container_id varchar(100) not null unique,
    status enum (
        'STARTING',
        'RUNNING',
        'STOPPING',
        'STOPPED',
        'FAILED'
    ) not null default 'STARTING',
    session_id bigint null,
    adb_port int not null,
    vnc_port int null,
    created_at timestamp not null default current_timestamp,
    last_access_at timestamp not null default current_timestamp,

    foreign key (session_id) references sessions (id) on delete set null,

    INDEX idx_emulator_status (status),
    INDEX idx_emulator_session (session_id),
    INDEX idx_emulator_heartbeat (last_access_at)

);