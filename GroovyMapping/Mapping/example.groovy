import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def body = message.getBody(String)
    def headers = message.getHeaders()
    def properties = message.getProperties()

    message.setBody(DoMapping(body, headers, properties))

    return message
}

def DoMapping(String body, Map headers, Map properties) {
    String output = ""

    String v_field1 = headers.get("field1") as String
    String v_text1 = properties.get("text1") as String

    headers.put("field1", v_field1 + " (modified)")
    headers.put("field2", "This is field2 (new)")
    properties.put("text1", v_text1 + " (modified)")
    properties.put("text2", "This is text2 (new)")

    output = body + " (modified)"

    return output
}
