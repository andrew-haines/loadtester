package com.ahaines.loadtest.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XmlWriter<MessageType extends Message> implements ReportWriter<MessageType> {

	private final XMLEventWriter eventWriter;
	private final XMLEventFactory eventFactory;
	private static final String DOC_ELEMENT_NAME = "stats";
	private static final String REQUEST_ELEMENT_NAME = "message";
	private static final String REQUEST_NUM_ATTR_NAME = "requestNum";
	private final XMLEvent end;
	private final File xmlFile;
	private int countLine = 0;
	
	public XmlWriter(File output) throws FileNotFoundException, WriterException{
		this.xmlFile = output;
		System.out.println("setting up xml");
		if (output.exists()){
			System.out.println("deleting existing file: "+output);
			output.delete();
		}
		
		try{
			FileOutputStream fos = new FileOutputStream(output);
			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			eventWriter = outputFactory.createXMLEventWriter(fos);
			
			eventFactory = XMLEventFactory.newInstance();
			
			end = eventFactory.createDTD("\n");
			
			// Create and write Start Tag
			StartDocument startDocument = eventFactory.createStartDocument();
			eventWriter.add(startDocument);
			
			StartElement configStartElement = eventFactory.createStartElement("","", DOC_ELEMENT_NAME);
			eventWriter.add(configStartElement);
			eventWriter.add(end);
		} catch (XMLStreamException e){
			throw new WriterException("unable to instantiate writer to file: "+output, e);
		}
	}

	@Override
	public void writeStats(MessageType message) {

		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent end = eventFactory.createDTD("\n");
		XMLEvent tab = eventFactory.createDTD("\t");
		// Create Start node
		
		List<Attribute> attrs = new LinkedList<Attribute>();
		
		attrs.add(eventFactory.createAttribute(REQUEST_NUM_ATTR_NAME, ""+countLine++));
		for (Stat stat: message.getValues()){
			attrs.add(eventFactory.createAttribute(stat.getKey(), ""+stat.getValue()));
		}
		
		final Iterator<Attribute> attrIt = attrs.iterator();

		StartElement sElement = eventFactory.createStartElement("", "", REQUEST_ELEMENT_NAME, attrIt, Collections.<Namespace>emptyList().iterator());
		try {
			eventWriter.add(tab);
			eventWriter.add(sElement);
			// Create End node
			EndElement eElement = eventFactory.createEndElement("", "", REQUEST_ELEMENT_NAME);
			eventWriter.add(eElement);
			eventWriter.add(end);
		} catch (XMLStreamException e) {
			throw new RuntimeException("ERROR IN WRITING XML: ", e);
		}
	}
	
	
	
	@Override
	public void close() throws WriterException{
		
		try{
			eventWriter.add(eventFactory.createEndElement("", "", DOC_ELEMENT_NAME));
			eventWriter.add(end);
			eventWriter.add(eventFactory.createEndDocument());
			eventWriter.close();
		} catch (XMLStreamException e){
			throw new WriterException("unable to close stream for file: "+xmlFile, e);
		}
	}

}
