import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*
import groovy.xml.*

def Message processData(Message message) {
    def body = message.getBody(String)
    def headers = message.getHeaders()
    def properties = message.getProperties()

    message.setBody(DoMapping(body, headers, properties))

    return message
}

def DoMapping(String body, Map headers, Map properties) {
    def InputPayload = new XmlSlurper().parseText(body)

    def sb = new StringBuilder()

    sb.with {
        append "--batch_xyz"
        append '\r\n' append "Content-Type: multipart/mixed; boundary=changeset_1"
        append '\r\n'

        InputPayload.row.each { this_row ->

            String V_ID = this_row.ID.toString()
            String V_Action = this_row.Action.toString()

            append '\r\n' append "--changeset_1"
            append '\r\n' append "Content-Type: application/http"
            append '\r\n' append "Content-Transfer-Encoding:binary"
            append '\r\n'
            if(V_Action == "C") {
                append '\r\n' append "POST Products HTTP/1.1"
            }
            else{
                String V_ID_URLEncoded = URLEncoder.encode(V_ID, "UTF-8")
                append '\r\n' append "PUT Products($V_ID_URLEncoded) HTTP/1.1"
            }
            append '\r\n' append "Content-Type: application/json"
            append '\r\n'

            def writer = new StringWriter()
            def builder = new StreamingJsonBuilder(writer)

            append '\r\n'
            builder {
                ID(this_row.ID)
                Name(this_row.Name.toString())
                Price(this_row.Price.toString())
            }
            append writer.toString()
            append '\r\n'

        }
        append '\r\n' append "--changeset_1--"
        append '\r\n'
        append '\r\n' append "--batch_xyz--"
    }

    def output = sb.toString()

    return output
}
