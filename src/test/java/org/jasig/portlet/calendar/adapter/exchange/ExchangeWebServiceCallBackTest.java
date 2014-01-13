package org.jasig.portlet.calendar.adapter.exchange;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public class ExchangeWebServiceCallBackTest {

	private String username = "username";
	private String actionCallbackType = "actionCallbackType";
	private String requestServerVersion = "requestServerVersion";
	private SoapMessage request;
	private ExchangeWebServiceCallBack testee;
	private String localDomainName="ed.ac.uk";
	@Mock
	private Log mockLog;
	@Mock
	private WebServiceMessageCallback actionCallBackMock;
	private ArgumentCaptor<String> logmessage;

	@Before
	public void setUp() throws IOException, SOAPException {
		initMocks(this);
		testee = new ExchangeWebServiceCallBack(username, actionCallbackType,
				requestServerVersion, localDomainName);
		File soapFile = new File(
				"src/test/resources/TestGetAvailabilitySoapMessage.xml");
		InputStream is = new ByteArrayInputStream(FileUtils.readFileToString(
				soapFile).getBytes());
		request = new SaajSoapMessageFactory(MessageFactory.newInstance())
				.createWebServiceMessage(is);
		ReflectionTestUtils.setField(testee, "log", mockLog);
		logmessage = ArgumentCaptor.forClass(String.class);
	}

	@Test
	public void testCorrectValuesWrittenToSoapMessage() throws IOException,
			TransformerException {
		testee.doWithMessage((WebServiceMessage) request);
		verify(mockLog).debug(logmessage.capture());
		assertTrue(logmessage.getValue().indexOf("requestServerVersion") != -1);
		assertTrue(logmessage.getValue().indexOf("username") != -1);
	}

	@Test
	// for some reason actioncallback is not written to soap body, goes into
	// some kind of header which is visible in
	// the logs if debug is on
	public void testActionSetOnSoapMessage() throws IOException,
			TransformerException {
		ReflectionTestUtils.setField(testee, "actionCallback",
				actionCallBackMock);
		testee.doWithMessage((WebServiceMessage) request);
		verify(actionCallBackMock).doWithMessage(eq(request));
	}

	@Test
	public void testImpersonationAddedAsSoapHeader() throws IOException,
			TransformerException {
		testee.doWithMessage((WebServiceMessage) request);
		verify(mockLog).debug(logmessage.capture());
		assertTrue(logmessage.getValue().indexOf(
				"<t:ExchangeImpersonation"
			    		+ " xmlns:t=\"http://schemas.microsoft.com/exchange/services/2006/types\">"
			    		+ "<t:ConnectingSID><t:PrincipalName>") != -1);
		assertTrue(logmessage.getValue().indexOf(
				username+"@"+localDomainName+ "</t:PrincipalName></t:ConnectingSID></t:ExchangeImpersonation>") != -1);
	}
}
