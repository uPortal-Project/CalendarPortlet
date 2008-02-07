/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
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