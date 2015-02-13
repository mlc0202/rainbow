drop table if exists T_USER;

drop table if exists T_RY;

create table T_USER (
	ID		INTEGER 		not null,
	CODE		VARCHAR(10)		not null,
	PASS		VARCHAR(10)		not null,
	constraint PK_T_USER PRIMARY KEY(ID)
) ;

create table T_RY (
	ID		INTEGER 		not null,
	NAME		VARCHAR(0),
	constraint PK_T_RY PRIMARY KEY(ID)
) ;

insert into T_RY values(1, '韦小宝');
insert into T_RY values(2, '陈近南');
insert into T_USER values(1, 'xiaobao', '123');
insert into T_USER values(2, 'shifu', '123');