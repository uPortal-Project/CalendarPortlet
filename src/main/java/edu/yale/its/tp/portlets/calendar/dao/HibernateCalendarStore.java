package edu.yale.its.tp.portlets.calendar.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarDefinition;
import edu.yale.its.tp.portlets.calendar.PredefinedCalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.PredefinedCalendarDefinition;
import edu.yale.its.tp.portlets.calendar.UserDefinedCalendarConfiguration;

public class HibernateCalendarStore extends HibernateDaoSupport implements
		CalendarStore {

	private static Log log = LogFactory.getLog(HibernateCalendarStore.class);

	public void storeCalendarDefinition(CalendarDefinition listing) {
		try {

			getHibernateTemplate().saveOrUpdate(listing);
			getHibernateTemplate().flush();

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	public void storeCalendarConfiguration(CalendarConfiguration configuration) {
		try {

			getHibernateTemplate().saveOrUpdate(configuration);
			getHibernateTemplate().flush();

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	public List<CalendarDefinition> getCalendarDefinitions(String subscribeId) {
		try {

			return (List<CalendarDefinition>) getHibernateTemplate().find(
					"from CalendarDefinition def");

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	public List<CalendarConfiguration> getCalendarConfigurations(
			String subscribeId) {
		try {

			log.debug("fetching calendar configurations for " + subscribeId);
			return (List<CalendarConfiguration>) getHibernateTemplate().find(
					"from CalendarConfiguration config where "
							+ "subscribeId = ? and displayed = true "
							+ "order by calendarDefinition.name", subscribeId);

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	public List<UserDefinedCalendarConfiguration> getUserDefinedCalendarConfigurations(
			String subscribeId, boolean visibleOnly) {
		try {

			String query = "from CalendarConfiguration config where "
					+ "subscribeId = ? and "
					+ "config.class = UserDefinedCalendarConfiguration "
					+ "order by calendarDefinition.name";
			if (visibleOnly)
				query = query.concat(" and visibleOnly = true");

			return (List<UserDefinedCalendarConfiguration>) getHibernateTemplate()
					.find(query, subscribeId);

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	public List<PredefinedCalendarConfiguration> getPredefinedCalendarConfigurations(
			String subscribeId, boolean visibleOnly) {
		try {
			String query = "from CalendarConfiguration config "
					+ "where subscribeId = ? and "
					+ "config.class = PredefinedCalendarConfiguration "
					+ "order by calendarDefinition.name";
			if (visibleOnly)
				query = query.concat(" and visibleOnly = true");

			return (List<PredefinedCalendarConfiguration>) getHibernateTemplate()
					.find(query, subscribeId);

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	public List<PredefinedCalendarDefinition> getHiddenPredefinedCalendarDefinitions(String subscribeId, String role){
		try {
			
			String query = "from PredefinedCalendarDefinition def "
				+ "where ? not in elements(def.defaultRoles) "
				+ "and ? not in (select config.subscribeId "
				+ "from def.userConfigurations config) ";
			
			return (List<PredefinedCalendarDefinition>) getHibernateTemplate()
					.find(query, new Object[]{role, subscribeId});

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}
	
	public void initCalendar(String subscribeId, String role) {
		try {

			List<PredefinedCalendarDefinition> defs = getHibernateTemplate()
					.find(
							"from PredefinedCalendarDefinition def "
									+ "left join fetch def.defaultRoles role " 
									+ "where role = ? and ? not in "
									+ "(select config.subscribeId "
									+ "from def.userConfigurations config) ",
							new Object[] { role, subscribeId });
			for (PredefinedCalendarDefinition def : defs) {
				PredefinedCalendarConfiguration config = new PredefinedCalendarConfiguration();
				config.setCalendarDefinition(def);
				config.setSubscribeId(subscribeId);
				storeCalendarConfiguration(config);
			}

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	public CalendarDefinition getCalendarDefinition(Long id) {

		try {

			return (CalendarDefinition) getHibernateTemplate().get(CalendarDefinition.class, id);
			
		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}

	}

	public CalendarConfiguration getCalendarConfiguration(Long id) {

		try {

			return (CalendarConfiguration) getHibernateTemplate().load(
					CalendarConfiguration.class, id);

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}

	}

	public void deleteCalendarConfiguration(CalendarConfiguration configuration) {
		try {

			getHibernateTemplate().delete(configuration);
			getHibernateTemplate().flush();

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

}
