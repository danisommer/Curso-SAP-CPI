import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*
import groovy.xml.*

class ZRecord {
    public String ProductID = ''
    public String ProductName = ''
    public String SupplierID = ''
    public String SupplierCompanyName = ''
    public String SupplierCountry = ''
}

def Message processData(Message message) {
    def body = message.getBody(String)
    def headers = message.getHeaders()
    def properties = message.getProperties()

    def payload_product = properties.get("payload_product")
    def payload_supplier = properties.get("payload_supplier")

    message.setBody(DoMapping(payload_product, payload_supplier, headers, properties))

    return message
}

//Need comment this TestRun() before upload to CPI. This TestRun() for local debug only
//TestRun()

void TestRun() {
    def scriptDir = new File(getClass().protectionDomain.codeSource.location.toURI().path).parent
    def dataDir = scriptDir + "\\Data"

    Map headers = [:]
    Map props = [:]

    File inputFile_SourceProduct = new File("$dataDir\\payload_product.txt")
    File inputFile_LookupSupplier = new File("$dataDir\\payload_supplier.txt")
    File outputFile = new File("$dataDir\\payload_product_supplier_combined.txt")

    def payload_product = inputFile_SourceProduct.getText("UTF-8")
    def payload_supplier = inputFile_LookupSupplier.getText("UTF-8")

    def outputBody = DoMapping(payload_product, payload_supplier, headers, props)

    println outputBody
    outputFile.write outputBody
}

def DoMapping(String payload_product, String payload_supplier, Map headers, Map properties) {
    def SourceProduct = new JsonSlurper().parseText(payload_product)
    def LookupSupplier = new JsonSlurper().parseText(payload_supplier)

    def zrecord_list = []

    SourceProduct.value.each{ this_product ->
        ZRecord zrecord = new ZRecord()

        zrecord.ProductID = this_product.ProductID.toString()
        zrecord.ProductName = this_product.ProductName.toString()

        zrecord.SupplierID = this_product.SupplierID.toString()
        def found_Supplier = LookupSupplier.value.findAll { find ->
            find.SupplierID.toString() == zrecord.SupplierID
        }
        found_Supplier.each { this_Supplier ->
            zrecord.SupplierCompanyName = this_Supplier.CompanyName.toString()
            zrecord.SupplierCountry = this_Supplier.Country.toString()
        }

        zrecord_list.add(zrecord)
    }

    def sw = new StringWriter()
    def builder = new StreamingJsonBuilder(sw)

    builder {
        data(zrecord_list) { this_row ->
            ProductID(this_row.ProductID.toString())
            ProductName(this_row.ProductName.toString())
            SupplierID(this_row.SupplierID.toString())
            SupplierCompanyName(this_row.SupplierCompanyName.toString())
            SupplierCountry(this_row.SupplierCountry.toString())
        }
    }

    def output = JsonOutput.prettyPrint(sw.toString())

    return output
}