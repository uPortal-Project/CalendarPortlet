/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.dao;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarDefinition;
import edu.yale.its.tp.portlets.calendar.PredefinedCalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.PredefinedCalendarDefinition;
import edu.yale.its.tp.portlets.calendar.UserDefinedCalendarConfiguration;

/**
 * HibernateCalendarStore provides a hibernate implementation of the CalendarStore.
 *
 * @author Jen Bourey
 */
public class HibernateCalendarStore extends HibernateDaoSupport implements
		CalendarStore {

	private static Log log = LogFactory.getLog(HibernateCalendarStore.class);

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#storeCalendarDefinition(edu.yale.its.tp.portlets.calendar.CalendarDefinition)
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
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#storeCalendarConfiguration(edu.yale.its.tp.portlets.calendar.CalendarConfiguration)
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
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#getCalendarConfigurations(java.lang.String)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#getUserDefinedCalendarConfigurations(java.lang.String, boolean)
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

			return (List<UserDefinedCalendarConfiguration>) getHibernateTemplate()
					.find(query, subscribeId);

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#getPredefinedCalendarConfigurations(java.lang.String, boolean)
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

			return (List<PredefinedCalendarConfiguration>) getHibernateTemplate()
					.find(query, subscribeId);

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#getPredefinedCalendarConfigurations()
	 */
	public List<PredefinedCalendarConfiguration> getPredefinedCalendarConfigurations() {
		try {

			String query = "from CalendarDefinition def "
					+ "where def.class = PredefinedCalendarDefinition "
					+ "order by def.name";
			return (List<PredefinedCalendarConfiguration>) getHibernateTemplate()
					.find(query);

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#getHiddenPredefinedCalendarDefinitions(java.lang.String, java.lang.String)
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
			return (List<PredefinedCalendarDefinition>) q.list();

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#initCalendar(java.lang.String, java.util.Set)
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
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#getPredefinedCalendarDefinition(java.lang.Long)
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
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#getCalendarDefinition(java.lang.Long)
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
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#getCalendarConfiguration(java.lang.Long)
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
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#deleteCalendarConfiguration(edu.yale.its.tp.portlets.calendar.CalendarConfiguration)
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
	 * @see edu.yale.its.tp.portlets.calendar.dao.CalendarStore#getUserRoles()
	 */
	public List<String> getUserRoles() {
		try {
			
			String query = "select distinct elements(def.defaultRoles) " +
					"from PredefinedCalendarDefinition def ";

			return (List<String>) getHibernateTemplate()
					.find(query);

		} catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

}


/*
 * HibernateCalendarStore.java
 * 
 * Copyright (c) Feb 5, 2008 Yale University. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met.
 * 
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by Yale University," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse or
 * promote products derived from this software.
 */