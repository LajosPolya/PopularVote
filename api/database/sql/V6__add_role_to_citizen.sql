alter table citizen
add role enum('admin', 'citizen', 'politician') not null default 'citizen';
