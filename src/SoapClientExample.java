/**
 * Created by telegin on 09.11.2018.
 */
import javax.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

public class SoapClientExample
{
    private  boolean  belavia      = true;

    private  String   namespaceURI = null;
    private  String   soapUrl      = null;
    private  String   serviceName  = null;

    private  String   namespace    = "tns";
    private  String   soapAction   = null;

    private  boolean  useXSLT      = true;

    public static void main(String[] args)
    {
        new SoapClientExample();
        System.exit(0);
    }

    public SoapClientExample()  //После main
    {                       //Получает параметры
        setSoapParams();    // Переходит в setSoapParams()
        callSoapWebService(soapUrl, soapAction); // Переходит в callSoapWebService
    }
    private void setSoapParams()
    {
        if (belavia) {
            namespaceURI = "http://www.roskazna.ru/eb/service/PGU/1.0";
            soapUrl      = "http://172.20.18.5:15101/pgu-subscriber-service/pguSubscriberService?WSDL";
            serviceName  = "PGURecordPackage";
        } else {
            namespaceURI = "http://www.roskazna.ru/eb/service/PGU/1.0";
            soapUrl      = "http://172.20.18.5:15101/pgu-subscriber-service/pguSubscriberService?WSDL";
            serviceName  = "PGURecordPackage";
        }
        //namespace  = "tns"; // Namespace";
        soapAction = namespaceURI + "/" + serviceName;
    }
    private void callSoapWebService(String destination, String soapAction)
    {
        SOAPConnectionFactory soapFactory  = null;
        SOAPConnection        soapConnect  = null;
        SOAPMessage           soapRequest  = null;
        SOAPMessage           soapResponse = null;
        try {
            // �������� SOAP Connection
            soapFactory = SOAPConnectionFactory.newInstance();
            soapConnect = soapFactory.createConnection();

            // �������� SOAP Message ��� ��������
            soapRequest  = createSOAPRequest(soapAction);
            // ��������� SOAP Message
            soapResponse = soapConnect.call(soapRequest, destination);

            if (!useXSLT) {
                // ������ SOAP Response
                System.out.println("Response SOAP Message:");
                soapResponse.writeTo(System.out);
                System.out.println();
            } else
                printSOAPMessage (soapResponse);

            soapConnect.close();
        } catch (Exception e) {
            System.err.println("\nError occurred while sending SOAP Request to Server!\n"
                    + "Make sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }
    }

    private SOAPMessage createSOAPRequest(String soapAction) throws Exception
    {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        createSoapEnvelope(soapMessage);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", soapAction);

        soapMessage.saveChanges();

        // ������ XML ������ �������
        System.out.println("Request SOAP Message:");
        soapMessage.writeTo(System.out);
        System.out.println("\n");

        return soapMessage;
    }

    private void createSoapEnvelope(SOAPMessage soapMessage) throws SOAPException
    {
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(namespace, namespaceURI);
/*
            Constructed SOAP Request Message:
            <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
                               xmlns:myNamespace="http://www.webserviceX.NET">
                <SOAP-ENV:Header/>
                <SOAP-ENV:Body>
                    <myNamespace:GetInfoByCity>
                        <myNamespace:USCity>New York</myNamespace:USCity>
                    </myNamespace:GetInfoByCity>
                </SOAP-ENV:Body>
            </SOAP-ENV:Envelope>
*/

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem;
        SOAPElement soapBodyElem1;
        if (belavia) {

            soapBodyElem  = soapBody.addChildElement(serviceName, namespace);  // Создаем PGURecordPackage
            soapBodyElem1 = soapBodyElem.addChildElement("GUID", namespace);
            soapBodyElem1.addTextNode("00000000-0000-0000-0000-000000000000");

            SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("RecordType", namespace);
            soapBodyElem2.addTextNode("EPGU_SvcList");

            SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("Record", namespace);
            String ney = "self";
            SOAPElement soapBodyElem4 = soapBodyElem3.addChildElement("EPGU_SvcList", namespace);
            SOAPElement soapBodyElem5 = soapBodyElem4.addChildElement("GUID1");
            soapBodyElem5.addTextNode("EC672B83-8943-447C-AC4E-8BC5FF3DDD32");
            SOAPElement soapBodyElem6 = soapBodyElem4.addChildElement("RegNumber");
            soapBodyElem6.addTextNode("910112О.99.0.БА78АА00000");

        } else {
            soapBodyElem  = soapBody.addChildElement(serviceName, namespace);
            soapBodyElem1 = soapBodyElem.addChildElement("USCity", namespace);
            soapBodyElem1.addTextNode("New York");
        }
    }

    private void printSOAPMessage (SOAPMessage soapResponse)
    {
        TransformerFactory transformerFactory;
        Transformer        transformer;
        try {
            // �������� XSLT-����������
            transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
            // ��������� ����������� ������
            Source content;
            content = soapResponse.getSOAPPart().getContent();
            // ����������� ��������� ������
            StreamResult result = new StreamResult(System.out);
            transformer.transform(content, result);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

