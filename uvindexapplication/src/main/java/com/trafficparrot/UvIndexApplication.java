package com.trafficparrot;

import com.thoughtworks.xstream.XStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import static java.awt.BorderLayout.*;
import static java.lang.String.format;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class UvIndexApplication {
    private static final String NEW_LINE = System.getProperty("line.separator");

    public static void main(String[] args) {
        JFrame frame = new JFrame("UV Index Application");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container container = frame.getContentPane();

        JPanel serverDetails = new JPanel();
        serverDetails.add(new JLabel("Server host"));
        JTextField host = new JTextField("https://iaspub.epa.gov", 20);
        serverDetails.add(host);
        serverDetails.add(new JLabel("Server port"));
        JTextField port = new JTextField("443", 6);
        serverDetails.add(port);
        container.add(serverDetails, PAGE_START);

        JPanel outputPanel = new JPanel();
        JTextArea output = new JTextArea("", 30, 60);
        outputPanel.add(output);
        container.add(outputPanel, PAGE_END);

        JPanel orderDetails = new JPanel();
        orderDetails.add(new JLabel("ZIP code"));
        JTextField zip = new JTextField("10131", 10);
        orderDetails.add(zip);
        JButton order = new JButton("Get");
        order.addActionListener(event -> {
            try {
                String hostname = host.getText();
                int portNum = Integer.parseInt(port.getText());
                String zipCode = zip.getText();
                try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                    HttpPost httpPost = new HttpPost(hostname + ":" + portNum + "/uvindexalert/services/UVIndexAlertPort");
                    httpPost.addHeader("accept-encoding", "identity");
                    httpPost.addHeader("Accept", "text/xml");
                    StringEntity stringRequestEntity = new StringEntity(format("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:uvindexalert\">\n" +
                            "   <soapenv:Header/>\n" +
                            "   <soapenv:Body>\n" +
                            "      <urn:getUVIndexAlertByZipCode>\n" +
                            "         <in0>%s</in0>\n" +
                            "      </urn:getUVIndexAlertByZipCode>\n" +
                            "   </soapenv:Body>\n" +
                            "</soapenv:Envelope>", zipCode), ContentType.create("text/xml; charset=UTF-8"));
                    httpPost.setEntity(stringRequestEntity);
                    ResponseHandler<String> responseHandler = response -> {
                        int status = response.getStatusLine().getStatusCode();
                        String entityString = toString(response.getEntity());
                        if (status >= 200 && status < 300) {
                            System.out.println("Response: '" + entityString + "'");
                            return entityString;
                        } else {
                            System.err.println("Response: '" + entityString + "'");
                            return "ERROR! Unexpected response status: " + status;
                        }
                    };
                    String response = httpclient.execute(httpPost, responseHandler);
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(new ByteArrayInputStream(response.getBytes()));
                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    String alert = (String) xpath.compile("//*[local-name() = 'alert']").evaluate(doc, XPathConstants.STRING);
                    String index = (String) xpath.compile("//*[local-name() = 'index']").evaluate(doc, XPathConstants.STRING);
                    String forecastDate = (String) xpath.compile("//*[local-name() = 'forecastDate']").evaluate(doc, XPathConstants.STRING);
                    output.append(format("Got UV Index data, alert is '%s' index is '%s' forecastDate is '%s'%s", alert, index, forecastDate, NEW_LINE));
                }
            } catch (Exception e) {
                e.printStackTrace();
                output.append("ERROR! " + e.getMessage() + NEW_LINE);
            }
        });
        orderDetails.add(order);
        container.add(orderDetails, CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    private static String toString(HttpEntity entity) throws IOException {
        return entity != null ? EntityUtils.toString(entity) : null;
    }
}
