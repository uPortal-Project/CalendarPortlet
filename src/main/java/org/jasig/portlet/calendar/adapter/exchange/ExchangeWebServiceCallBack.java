package org.jasig.portlet.calendar.adapter.exchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.http.annotation.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.xml.transform.StringSource;

@NotThreadSafe
public class ExchangeWebServiceCallBack implements WebServiceMessageCallback {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String impersonationFirstPart= "<t:ExchangeImpersonation"
            + " xmlns:t=\"http://schemas.microsoft.com/exchange/services/2006/types\">"
            + "<t:ConnectingSID><t:PrincipalName>";
    private static final String impersonationSecondPart =  "</t:PrincipalName></t:ConnectingSID></t:ExchangeImpersonation>";

	private String impersonatedAccountId;
    private final WebServiceMessageCallback actionCallback;
	private String requestServerVersion;

    /**
     * Create callback to add SOAP headers potentially including impersonated account ID for Exchange Impersonation
     * to an Exchange Web Service SOAP message.
     *
     * @param actionCallbackType Action type string
     * @param requestServerVersion Minimum Exchange Server version code (see http://msdn.microsoft.com/en-us/library/exchange/exchangewebservices.exchangeversiontype(v=exchg.140).aspx)
     * @param impersonatedAccountDomainId Account (username@NTdomain) to provide data for when using Exchange
     *                                      Impersonation. Empty string or null to not use Exchange Impersonation.
     */
    public ExchangeWebServiceCallBack(String actionCallbackType, String requestServerVersion,
                                      String impersonatedAccountDomainId){
        this.actionCallback= new SoapActionCallback(
				actionCallbackType);
		this.requestServerVersion=requestServerVersion;
		this.impersonatedAccountId = impersonatedAccountDomainId;
	}
	@Override
    public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
        actionCallback.doWithMessage(message);
        SoapMessage soap = (SoapMessage) message;
        QName rsv = new QName(
                "http://schemas.microsoft.com/exchange/services/2006/types", 
                "RequestServerVersion", 
                "ns3"); 

        SoapHeaderElement version = soap.getEnvelope().getHeader().addHeaderElement(rsv);
        version.addAttribute(new QName("Version"), requestServerVersion);

        if (StringUtils.isNotBlank(impersonatedAccountId)) {
            // TODO create header using message construction not derived from String XML value
            StringSource headerSource = new StringSource(impersonationFirstPart + impersonatedAccountId
                    + impersonationSecondPart);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(headerSource, soap.getEnvelope().getHeader().getResult());
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            message.writeTo(bout);
            log.debug("Including impersonation header in SOAP message for account {}", impersonatedAccountId);
        }
    }
}
