package org.jasig.portlet.calendar.util;

import java.io.InputStream;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class StringCleaningUtil implements InitializingBean,
		ApplicationContextAware {

	private Policy policy;

	private String filePath;

	/**
	 * Set the file path to the Anti-samy policy file to be used for cleaning
	 * strings.
	 * 
	 * @param path
	 */
	public void setSecurityFile(String path) {
		this.filePath = path;
	}

	/**
	 * Clean a string using the configured Anti-samy policy file
	 * 
	 * @param dirtyInput
	 * @return
	 * @throws ScanException
	 * @throws PolicyException
	 */
	public String getCleanString(String dirtyInput) throws ScanException,
			PolicyException {
		AntiSamy as = new AntiSamy();
		CleanResults cr = as.scan(dirtyInput, policy);
		return cr.getCleanHTML();
	}

	private ApplicationContext ctx;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		InputStream stream = ctx.getResource(filePath).getInputStream();
		policy = Policy.getInstance(stream);
	}

}
