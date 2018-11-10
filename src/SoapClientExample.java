/**
 * Created by telegin on 09.11.2018.
 */

import javax.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

public class SoapClientExample {
    private String namespaceURI = null;
    private String soapUrl = null;
    private String serviceName = null;
    private String namespace = null;
    private String soapAction = null;

    private boolean useXSLT = true;

    public static void main(String[] args) {
        new SoapClientExample();
        System.exit(0);
    }

    public SoapClientExample()                      //После main
    {                                               //Получает параметры
        setSoapParams();                            // Переходит в setSoapParams()
        callSoapWebService(soapUrl, soapAction);    // Переходит в callSoapWebService
    }

    private void setSoapParams() {

        namespaceURI = "http://www.roskazna.ru/eb/service/PGU/1.0";
        soapUrl = "http://172.20.18.5:15101/pgu-subscriber-service/pguSubscriberService?WSDL";
        serviceName = "PGURecordPackage";

        namespace = "tns"; // Namespace";
        soapAction = namespaceURI + "/" + serviceName;
    }

    private void callSoapWebService(String destination, String soapAction) {
        SOAPConnectionFactory soapFactory = null;
        SOAPConnection soapConnect = null;
        SOAPMessage soapRequest = null;
        SOAPMessage soapResponse = null;
        try {
            // �������� SOAP Connection
            soapFactory = SOAPConnectionFactory.newInstance();
            soapConnect = soapFactory.createConnection();

            // �������� SOAP Message ��� ��������
            soapRequest = createSOAPRequest(soapAction);
            // ��������� SOAP Message
            soapResponse = soapConnect.call(soapRequest, destination);

            if (!useXSLT) {
                // ������ SOAP Response
                System.out.println("Response SOAP Message:");
                soapResponse.writeTo(System.out);
                System.out.println();
            } else
                printSOAPMessage(soapResponse);

            soapConnect.close();
        } catch (Exception e) {
            System.err.println("\nError occurred while sending SOAP Request to Server!\n"
                    + "Make sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }
    }

    private SOAPMessage createSOAPRequest(String soapAction) throws Exception {
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

    private void createSoapEnvelope(SOAPMessage soapMessage) throws SOAPException {
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(namespace, namespaceURI);

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();

        SOAPElement PGURecordPackage = soapBody.addChildElement(serviceName, namespace);  // Создаем PGURecordPackage
        SOAPElement GUIDBody = PGURecordPackage.addChildElement("GUID", namespace);
        GUIDBody.addTextNode("00000000-0000-0000-0000-000000000000");

        SOAPElement RecordTypeBody = PGURecordPackage.addChildElement("RecordType", namespace);
        RecordTypeBody.addTextNode("EPGU_SvcList");

        SOAPElement xRecord = PGURecordPackage.addChildElement("Record", namespace);
        SOAPElement xEPGU_SvcList = xRecord.addChildElement("EPGU_SvcList", namespace);
        /*
         * !!!! Вставка основных полей записи
         */
        //TODO Тут должен быть апрос к БД на получение этих полей, пока строковым массивом реализуем
        String[][] tSVC_Val = {
                {"GUID", "EC672B83-8943-447C-AC4E-8BC5FF3DDD32"},
                {"RegNumber", "910112О.99.0.БА78АА00000"},
                {"RegrNumber", "33012000000000000000100"},
                {"Pbl_Actual", "true"},
                {"SvcCode", "БА78"},
                {"Name_Code", "33.012.0"},
                {"Name_Name", "Предоставление архивных справок и копий архивных документов, связанных с социальной защитой граждан, предусматривающей их пенсионное обеспечение, а также получение льгот и компенсаций в соответствии с законодательстом Российской Федерации и международными обязательствами Российской Федерации"},
                {"ActvtyDomn_Code", "33"},
                {"ActvtyDomn_Name", "Архивное дело"},
                {"SvcKind_Code", "0"},
                {"SvcKind_Name", "Услуга"},
                {"Belong210FL", "false"},
                {"NcsrlyBelong210FL", "false"},
                {"ApprovedAt", "2017-12-14"},
                {"EffectiveFrom", "2019-01-01"},
                {"EffectiveBefore", "2099-01-01"},
                {"paidcode", "2"},
                {"paidname", "государственная (муниципальная) услуга или работа бесплатная"},
                {"IsRegional", "О"},
                {"IsRegionalName", "Общероссийский классификатор"},
                {"ListKind", "all"},
                {"ListNumber", "01"}
        };
        for (int i = 0; i < tSVC_Val.length; i++) {
            //String localName = SVC_Val[i][0];
            // Вроде такой код работает (SOAPElement xSVC_Val) посмотрим как дельше будет, в примере все разные были
            SOAPElement xSVC_Val = xEPGU_SvcList.addChildElement(tSVC_Val[i][0]);
            xSVC_Val.addTextNode(tSVC_Val[i][1]);
        }
        /*
         * !!!! Вставка табличных полей записи
         */
        // Вставка таблицы RuClsPrEcAcs
        //TODO Тут должен быть апрос к БД на получение этих полей, пока строковым массивом реализуем
        String[][] tRuClsPrEcAcs = {
                {"RuClsPrEcAcs_Code", "91.01.12.000"},
                {"RuClsPrEcAcs_Name", "Услуги архивов"}};
        SvcListItems("RuClsPrEcAcs", tRuClsPrEcAcs, xEPGU_SvcList);
        // Вставка таблицы RuClsEcActs
        //TODO Тут должен быть апрос к БД на получение этих полей, пока строковым массивом реализуем
        String[][] tRuClsEcActs = {
                {"RuClsEcActs_Code", "91.01"},
                {"RuClsEcActs_Name", "Деятельность библиотек и архивов"}};
        SvcListItems("RuClsEcActs", tRuClsEcActs, xEPGU_SvcList);
        /*
            TODO Следующие по аналогии с вышестоящими
         */
        // Вставка таблицы PblcEntKnd
        // Вставка таблицы InstnKnd
        // Вставка таблицы CsmCtgy
        // Вставка таблицы VolInd
        // Вставка таблицы LglAct
        // Вставка таблицы SvcBudgInstTypes

        // TODO Каких то таблиц не хватает... надо узнать каких

        // Вставка даты создания CreateDate
        SOAPElement xGUID = xEPGU_SvcList.addChildElement("CreateDate");
        xGUID.addTextNode("2017-12-14");
    }

    // Заполненеие табличных атрибутов
    private void SvcListItems(String NameElem, String[][] ItemsVal, SOAPElement xEPGU_SvcList) throws SOAPException {
        SOAPElement xSvcListChild = xEPGU_SvcList.addChildElement(NameElem);
        //TODO Тут должен быть апрос к БД на получение этих полей, пока строковым массивом реализуем
        SOAPElement xSvcListChild_ITEM = xSvcListChild.addChildElement(NameElem + "_ITEM");
        for (int i = 0; i < ItemsVal.length; i++) {
            //String localName = SVC_Val[i][0];
            // Вроде такой код работает (SOAPElement xSVC_Val) посмотрим как дельше будет, в примере все разные были
            SOAPElement xITEMS = xSvcListChild_ITEM.addChildElement(ItemsVal[i][0]);
            xITEMS.addTextNode(ItemsVal[i][1]);
        }
    }

    //TODO Раскоментировать

    private void printSOAPMessage(SOAPMessage soapResponse) {
        /*TransformerFactory transformerFactory;
        Transformer transformer;
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
        }*/
    }

}

