drop table if exists records;
create table records (
	id               serial      primary key,
	service_id       int,
	identifier_1     char(10)    not null,
	identifier_2     char(10)    not null,
	identifier_full  char(60),
	process_complete char(1)     default 'N',
	datestamp        timestamp   not null,
	setSpec          char(10)
) engine=MyISAM default charset=utf8;

create index records_identifier_2_idx on records(identifier_2);
create index records_datestamp_idx on records(datestamp);
create index records_service_id_idx on records(service_id);
create index records_process_complete on records(process_complete);

/* this may have to change to a blob in the future */
drop table if exists records_xml;
create table records_xml (
	id             serial         primary key,
	xml            longtext
) engine=MyISAM default charset=utf8;
