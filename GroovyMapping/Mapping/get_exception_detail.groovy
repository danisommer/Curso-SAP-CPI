import com.sap.gateway.ip.core.customdev.util.Message;
import org.w3c.dom.Node;
import groovy.xml.*

def Message processData(Message message) {
                
	def map = message.getProperties();
	
	def ex = map.get("CamelExceptionCaught");
	if (ex!=null) {
					
		//HTTP Error
		if (ex.getClass().getCanonicalName().equals("org.apache.camel.component.ahc.AhcOperationFailedException")) {
			message.setBody(ex.getResponseBody());
			message.setProperty("http.responseBody", ex.getResponseBody());
			message.setProperty("http.statusCode", ex.getStatusCode());	
		}
		
		//OData V2 Error
		if (ex.getClass().getCanonicalName().equals("com.sap.gateway.core.ip.component.odata.exception.OsciException")) {                                      
			message.setBody(message.getBody());
            message.setProperty("http.responseBody", message.getBody());
            message.setProperty("http.statusCode", message.getHeaders().get("CamelHttpResponseCode").toString());
        }
		
		//SOAP Error
		if (ex.getClass().getCanonicalName().equals("org.apache.cxf.binding.soap.SoapFault")) {
			def xml = XmlUtil.serialize(ex.getOrCreateDetail());
			message.setBody(xml);
			message.setProperty("http.responseBody", xml);
			message.setProperty("http.statusCode", ex.getStatusCode());	
		}
	}
	
	return message;
}