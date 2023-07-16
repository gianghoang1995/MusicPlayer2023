package com.downloadmp3player.musicdownloader.freemusicdownloader.net.utilsonline;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.downloadmp3player.musicdownloader.freemusicdownloader.net.VolleySingleton;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class RequestSuggestionUtils {
    private Context context;
    private SearchSuggestionCallback suggestionListener;

    public RequestSuggestionUtils(Context context, SearchSuggestionCallback mlistener) {
        this.context = context;
        suggestionListener = mlistener;
    }

    public void querySearch(String text) {
        String urlChange = text.replaceAll(" ", "%20");
        String BASE_URL_REQUEST = "http://suggestqueries.google.com/complete/search?client=toolbar&q=";
        String urlRequest = BASE_URL_REQUEST + urlChange;
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                urlRequest,
                this::parseXML,
                error -> {
                }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    private void parseXML(String xml) {
        try {
            ArrayList<String> lst = new ArrayList<>();
            lst.clear();
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            builder = fac.newDocumentBuilder();

            Document doc = null;

            if (builder != null) {
                doc = builder.parse(new InputSource(new StringReader(xml)));
                if (doc != null) {
                    try {
                        doc.getDocumentElement().normalize();
                        NodeList list = doc.getElementsByTagName("suggestion");
                        for (int i = 0; i < list.getLength(); i++) {
                            Node node = list.item(i);
                            NamedNodeMap attr = node.getAttributes();
                            String data = attr.getNamedItem("data").getNodeValue();
                            lst.add(data);
                        }
                        suggestionListener.onSearchSuggestionSuccess(lst);
                    } catch (Exception ex) {
                    }
                }
            }

        } catch (IOException | SAXException | ParserConfigurationException ignored) {
        }
    }
}
