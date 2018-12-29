package com.trafficparrot;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentNavigableMap;

import static java.awt.BorderLayout.*;
import static java.awt.Color.BLACK;
import static java.awt.Component.*;
import static java.lang.Integer.*;
import static java.lang.String.format;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class UvIndexApplication {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final Color YELLOW = new Color(167, 167, 0);
    private static final Color RED = new Color(191, 0, 0);
    private static final Color GREEN = new Color(0, 123, 0);

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
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
        JTextPane output = new JTextPane();
        output.setAlignmentX(CENTER_ALIGNMENT);
        output.setPreferredSize(new Dimension(500, 400));
        output.setBackground(new Color(225, 225, 225));
        outputPanel.add(output, PAGE_START);
        JButton clear = new JButton("Clear all messages");
        clear.setAlignmentX(CENTER_ALIGNMENT);
        clear.addActionListener(e -> output.setText(""));
        outputPanel.add(clear, PAGE_END);
        container.add(outputPanel, PAGE_END);

        JPanel orderDetails = new JPanel();
        orderDetails.add(new JLabel("ZIP code"));
        JTextField zip = new JTextField("10131", 10);
        orderDetails.add(zip);
        JButton order = new JButton("Get");
        order.addActionListener(event -> {
            try {
                String hostname = host.getText();
                int portNum = parseInt(port.getText());
                String zipCode = zip.getText();
                try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                    String apiUrl = hostname + ":" + portNum + "/uvindexalert/services/UVIndexAlertPort";
                    HttpPost httpPost = new HttpPost(apiUrl);
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
                    Color color;
                    switch (parseInt(index)) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                            color = GREEN;
                            break;
                        case 6:
                        case 7:
                        case 8:
                            color = YELLOW;
                            break;
                        case 9:
                        case 10:
                            color = RED;
                            break;
                        default:
                            color = BLACK;
                    }
                    addColoredText(output,
                            format("Fetched UV Index from %s.%sThe response alert is '%s'. The UV Index is '%s'.%s%s", apiUrl, NEW_LINE, alert, index, NEW_LINE, NEW_LINE),
                            color);
                }
            } catch (Exception e) {
                e.printStackTrace();
                addColoredText(output, "ERROR! " + e.getMessage() + NEW_LINE + NEW_LINE, BLACK);
            }
        });
        orderDetails.add(order);
        container.add(orderDetails, CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    private static void addColoredText(JTextPane pane, String text, Color color) {
        StyledDocument doc = pane.getStyledDocument();
        Style style = pane.addStyle("Color Style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String toString(HttpEntity entity) throws IOException {
        return entity != null ? EntityUtils.toString(entity) : null;
    }
}
