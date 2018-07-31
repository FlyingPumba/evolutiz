package de.freewarepoint.whohasmystuff.database;

import android.net.Uri;
import de.freewarepoint.whohasmystuff.LentObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class XMLContentHandler extends DefaultHandler {

    public int databaseVersion;
    public List<LentObject> lentObjects;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if ("DatabaseBackup".equals(localName)) {
            lentObjects = new LinkedList<LentObject>();
            parseDatabaseBackupAttributes(attributes);
        }
        else if ("LentObject".equals(localName)) {
            parseLentObjectAttributes(attributes);
        }
    }

    private void parseDatabaseBackupAttributes(Attributes attributes) {
        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getLocalName(i);
            if ("version".equals(name)) {
                databaseVersion = Integer.parseInt(attributes.getValue(i));
            }
        }
    }

    private void parseLentObjectAttributes(Attributes attributes) {

        LentObject lentObject = new LentObject();

        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getLocalName(i);
            if ("description".equals(name)) {
                lentObject.description = attributes.getValue(i);
            }
            else if ("type".equals(name)) {
                lentObject.type = Integer.parseInt(attributes.getValue(i));
            }
            else if ("date".equals(name)) {
                lentObject.date = new Date(Long.parseLong(attributes.getValue(i)));
            }
            else if ("personName".equals(name)) {
                lentObject.personName = attributes.getValue(i);
            }
            else if ("personKey".equals(name)) {
                lentObject.personKey = attributes.getValue(i);
            }
            else if ("returned".equals(name)) {
                lentObject.returned = (Integer.parseInt(attributes.getValue(i)) == 1);
            }
            else if ("calendarEvent".equals(name)) {
                lentObject.calendarEventURI = Uri.parse(attributes.getValue(i));
            }
        }

        lentObjects.add(lentObject);

    }


}
