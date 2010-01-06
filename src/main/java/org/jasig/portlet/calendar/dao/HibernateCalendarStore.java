/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.dao;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarDefinition;
import org.jasig.portlet.calendar.PredefinedCalendarConfiguration;
import org.jasig.portlet.calendar.PredefinedCalendarDefinition;
import org.jasig.portlet.calendar.UserDefinedCalendarConfiguration;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * HibernateCalendarStore provides a hibernate implementation of the CalendarStore.
 *
 * @author Jen Bourey
 */
public class HibernateCalendarStore extends HibernateDaoSupport implements
		CalendarStore {

	protected final Log log = LogFactory.getLog(this.getClass());

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#storeCalendarDefinition(org.jasig.portlet.calendar.CalendarDefinition)
	 */
	public void storeCalendarDefinition(CalendarDefinition listing) {
		try {

			getHibernateTemplate().saveOrUpdate(listing);
			getHibernateTemplate().flush();

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#storeCalendarConfiguration(org.jasig.portlet.calendar.CalendarConfiguration)
	 */
	public void storeCalendarConfiguration(CalendarConfiguration configuration) {
		try {

			getHibernateTemplate().saveOrUpdate(configuration);
			getHibernateTemplate().flush();

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#getCalendarConfigurations(java.lang.String)
	 */
	public List<CalendarConfiguration> getCalendarConfigurations(
			String subscribeId) {
		try {

			log.debug("fetching calendar configurations for " + subscribeId);
			@SuppressWarnings("unchecked")
			List<CalendarConfiguration> configurations = (List<CalendarConfiguration>) getHibernateTemplate().find(
					"from CalendarConfiguration config where "
							+ "subscribeId = ? and displayed = true "
							+ "order by calendarDefinition.name", subscribeId);
			return configurations;

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#getUserDefinedCalendarConfigurations(java.lang.String, boolean)
	 */
	public List<UserDefinedCalendarConfiguration> getUserDefinedCalendarConfigurations(
			String subscribeId, boolean visibleOnly) {
		try {

			String query = "from CalendarConfiguration config where "
					+ "subscribeId = ? and "
					+ "config.class = UserDefinedCalendarConfiguration "
					+ "order by calendarDefinition.name";
			if (visibleOnly)
				query = query.concat(" and visibleOnly = true");

			@SuppressWarnings("unchecked")
			List<UserDefinedCalendarConfiguration> configurations = (List<UserDefinedCalendarConfiguration>) getHibernateTemplate()
					.find(query, subscribeId);
			return configurations;

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#getPredefinedCalendarConfigurations(java.lang.String, boolean)
	 */
	public List<PredefinedCalendarConfiguration> getPredefinedCalendarConfigurations(
			String subscribeId, boolean visibleOnly) {
		try {
			String query = "from CalendarConfiguration config "
					+ "where subscribeId = ? and "
					+ "config.class = PredefinedCalendarConfiguration "
					+ "order by calendarDefinition.name";
			if (visibleOnly)
				query = query.concat(" and visibleOnly = true");

			@SuppressWarnings("unchecked")
			List<PredefinedCalendarConfiguration> configurations = (List<PredefinedCalendarConfiguration>) getHibernateTemplate()
					.find(query, subscribeId);
			return configurations;

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#getPredefinedCalendarConfigurations()
	 */
	public List<PredefinedCalendarConfiguration> getPredefinedCalendarConfigurations() {
		try {

			String query = "from CalendarDefinition def "
					+ "where def.class = PredefinedCalendarDefinition "
					+ "order by def.name";
			@SuppressWarnings("unchecked")
			List<PredefinedCalendarConfiguration> configurations = (List<PredefinedCalendarConfiguration>) getHibernateTemplate()
					.find(query);
			return configurations;

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#getHiddenPredefinedCalendarDefinitions(java.lang.String, java.lang.String)
	 */
	public List<PredefinedCalendarDefinition> getHiddenPredefinedCalendarDefinitions(String subscribeId, Set<String> roles){
		try {
			
			String query = "from PredefinedCalendarDefinition def "
				+ "where :subscribeId not in (select config.subscribeId "
				+ "from def.userConfigurations config) ";
			for (int i = 0; i < roles.size(); i++) {
				query = query.concat(
					"and :role" + i + " not in elements(def.defaultRoles) ");
			}
			
			Query q = this.getSession().createQuery(query);
			q.setString("subscribeId", subscribeId);
			int count = 0;
			for (String role : roles) {
				q.setString("role" + count, role);
				count++;
			}
			
			@SuppressWarnings("unchecked")
			List<PredefinedCalendarDefinition> definitions =  (List<PredefinedCalendarDefinition>) q.list();
			return definitions;

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#initCalendar(java.lang.String, java.util.Set)
	 */
	public void initCalendar(String subscribeId, Set<String> roles) {
		try {

			// if the user doesn't have any roles, we don't have any
			// chance of getting predefined calendars, so just go ahead
			// and return
			if (roles.isEmpty())
				return;
			
			String query = "from PredefinedCalendarDefinition def "
				+ "left join fetch def.defaultRoles role where " 
				+ ":subscribeId not in (select config.subscribeId "
				+ "from def.userConfigurations config)";
			if (roles.size() > 0)
				query = query.concat("and role in (:roles)");
			Query q = this.getSession().createQuery(query);
			q.setString("subscribeId", subscribeId);
			if (roles.size() > 0)
				q.setParameterList("roles", roles);
			@SuppressWarnings("unchecked")
			List<PredefinedCalendarDefinition> defs = q.list();

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

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#getPredefinedCalendarDefinition(java.lang.Long)
	 */
	public PredefinedCalendarDefinition getPredefinedCalendarDefinition(Long id) {

		try {

			String query = "from PredefinedCalendarDefinition def "
				+ "left join fetch def.defaultRoles role where " 
				+ "def.id = :id";
			Query q = this.getSession().createQuery(query);
			q.setLong("id", id);
			return (PredefinedCalendarDefinition) q.uniqueResult();
			
		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#getCalendarDefinition(java.lang.Long)
	 */
	public CalendarDefinition getCalendarDefinition(Long id) {

		try {

			return (CalendarDefinition) getHibernateTemplate().get(CalendarDefinition.class, id);
			
		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#getCalendarConfiguration(java.lang.Long)
	 */
	public CalendarConfiguration getCalendarConfiguration(Long id) {

		try {

			return (CalendarConfiguration) getHibernateTemplate().load(
					CalendarConfiguration.class, id);

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#deleteCalendarConfiguration(org.jasig.portlet.calendar.CalendarConfiguration)
	 */
	public void deleteCalendarConfiguration(CalendarConfiguration configuration) {
		try {

			getHibernateTemplate().delete(configuration);
			getHibernateTemplate().flush();

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}
	
	public void deleteCalendarDefinition(CalendarDefinition definition) {
		try {

			getHibernateTemplate().delete(definition);
			getHibernateTemplate().flush();

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.dao.CalendarStore#getUserRoles()
	 */
	public List<String> getUserRoles() {
		try {
			
			String query = "select distinct elements(def.defaultRoles) " +
					"from PredefinedCalendarDefinition def ";

			@SuppressWarnings("unchecked")
			List<String> userRoles = (List<String>) getHibernateTemplate()
					.find(query);
			return userRoles;

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

}
