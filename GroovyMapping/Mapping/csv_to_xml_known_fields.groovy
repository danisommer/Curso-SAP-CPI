import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.*
import groovy.xml.*
import org.supercsv.cellprocessor.*
import org.supercsv.cellprocessor.constraint.*
import org.supercsv.cellprocessor.ift.*
import org.supercsv.io.*
import org.supercsv.prefs.*
import org.supercsv.quote.*

def Message processData(Message message) {
    def body = message.getBody(String)
    def headers = message.getHeaders()
    def properties = message.getProperties()

    message.setBody(DoMapping(body, headers, properties))

    return message
}

def DoMapping(String body, Map headers, Map properties) {
    /*If want drop the first header line*/
    //body = body.readLines().drop(1).join('\r\n')

    ICsvMapReader mapReader = new CsvMapReader(new StringReader(body), CsvPreference.STANDARD_PREFERENCE)
    String[] header = ["idoc_no", "order_no", "ship_to", "sold_to", "order_date", "line_item", "material", "quantity", "quantity_uom", "weight", "weight_uom"]
    int columnsCount = header.length
    CellProcessor[] processor = new CellProcessor[columnsCount]
    def rowMap = [:]

    def writer = new StringWriter()
    def builder = new StreamingMarkupBuilder()

    def OutputMarkup = {
        root {
            while ((rowMap = mapReader.read(header, processor)) != null) {
                row {
                    for (int i = 0; i < header.length; i++) {
                        def fieldName = header[i]
                        "$fieldName"(rowMap[header[i]])
                    }
                }
            }
        }
    }

    writer << builder.bind(OutputMarkup)
    def output = XmlUtil.serialize(writer.toString())

    return output
}