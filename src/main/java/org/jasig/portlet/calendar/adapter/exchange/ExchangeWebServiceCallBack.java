package org.jasig.portlet.calendar.adapter.exchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.xml.transform.StringSource;

@NotThreadSafe
public class ExchangeWebServiceCallBack implements WebServiceMessageCallback {
	private String localDomainName;
	private final Log log = LogFactory.getLog(getClass());
	private final String username;
	private final WebServiceMessageCallback actionCallback;
	private String requestServerVersion;
	private final String impersonationFirstPart= "<t:ExchangeImpersonation"
    		+ " xmlns:t=\"http://schemas.microsoft.com/exchange/services/2006/types\">"
    		+ "<t:ConnectingSID><t:PrincipalName>";
	private final String impersonationSecondPart =  "</t:PrincipalName></t:ConnectingSID></t:ExchangeImpersonation>";
	public ExchangeWebServiceCallBack(String username,String actionCallbackType, String requestServerVersion, String localDomainName){
		this.username=username;
		this.actionCallback= new SoapActionCallback(
				actionCallbackType);
		this.requestServerVersion=requestServerVersion;
		this.localDomainName=localDomainName;
	}
	@Override
    public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
        actionCallback.doWithMessage(message);
        SoapMessage soap = (SoapMessage) message;
        QName rsv = new QName(
                "http://schemas.microsoft.com/exchange/services/2006/types", 
                "RequestServerVersion", 
                "ns3"); 
        SoapHeaderElement version = soap.getEnvelope().getHeader()
                                        .addHeaderElement(rsv);
        version.addAttribute(new QName("Version"), requestServerVersion);
        StringSource headerSource = new StringSource(impersonationFirstPart+username+"@"+localDomainName+impersonationSecondPart);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(headerSource, soap.getEnvelope().getHeader().getResult());
        ByteArrayOutputStream bout = new ByteArrayOutputStream();  
        message.writeTo(bout); 
        String msg = bout.toString("UTF-8");  
        log.debug("Sending a SOAP message: " + msg);
    }
}
