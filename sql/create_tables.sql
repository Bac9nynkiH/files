create table if not exists file_info
(
    id             serial primary key,
    file_name      text,
    rows_count     bigint
);

create table if not exists longest_lines
(
    id             serial primary key,
    line      text,
    length     bigint
);
