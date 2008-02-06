//drop table calendar_preference;
//drop table calendar_configuration;
//drop table calendar_parameter;
//drop table calendar_role;
//drop table calendar_store;


insert into calendar_store (calendar_id, calendar_listing_type, calendar_name, calendar_class) 
	values (hibernate_sequence.nextval, 'PD', 'US Holidays', 
	'edu.yale.its.tp.portlets.calendar.adapter.HttpICalAdapter');
insert into calendar_store (calendar_id, calendar_listing_type, calendar_name, calendar_class) 
	values (hibernate_sequence.nextval, 'PD', 'Course Schedule', 
	'edu.yale.its.tp.portlets.calendar.adapter.CasifiedICalAdapter');
insert into calendar_store (calendar_id, calendar_listing_type, calendar_name, calendar_class) 
	values (hibernate_sequence.nextval, 'PD', 'Yale Events', 
	'edu.yale.its.tp.portlets.calendar.yaleopa.YaleEventCalendarAdapter');

insert into calendar_parameter (calendar_id, calendar_parameter_value, calendar_parameter_name) 
	values ((select calendar_id from calendar_store where calendar_name='US Holidays'),
	'http://ical.mac.com/ical/US32Holidays.ics', 'url');
insert into calendar_parameter (calendar_id, calendar_parameter_value, calendar_parameter_name) 
	values ((select calendar_id from calendar_store where calendar_name='Course Schedule'),
	'https://www7.sis.yale.edu:4445/pls/ban2/web_calendar_pkg.get_course_calendar', 'url');
insert into calendar_parameter (calendar_id, calendar_parameter_value, calendar_parameter_name) 
	values ((select calendar_id from calendar_store where calendar_name='Yale Events'), 
	'Music,Theater,Talks,Films,Sports,Language Tables', 'categories');

insert into calendar_role (calendar_id, role_name) values (
	(select calendar_id from calendar_store where calendar_name='US Holidays'),'student');
insert into calendar_role (calendar_id, role_name) values (
	(select calendar_id from calendar_store where calendar_name='US Holidays'),'staff');
insert into calendar_role (calendar_id, role_name) values (
	(select calendar_id from calendar_store where calendar_name='US Holidays'),'faculty');
insert into calendar_role (calendar_id, role_name) values (
	(select calendar_id from calendar_store where calendar_name='Course Schedule'),'student');
insert into calendar_role (calendar_id, role_name) values (
	(select calendar_id from calendar_store where calendar_name='Course Schedule'),'faculty');
commit;
