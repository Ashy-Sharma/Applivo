alter table app_versions
add column package_name varchar(255) null;

create index idx_versions_package_name on app_versions (package_name);