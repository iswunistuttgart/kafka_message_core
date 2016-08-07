package de.unistuttgart.isw.serviceorchestration.servicecore;

import com.siemens.ct.exi.EXIFactory;
import com.siemens.ct.exi.GrammarFactory;
import com.siemens.ct.exi.api.sax.EXIResult;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.grammars.Grammars;
import com.siemens.ct.exi.helpers.DefaultEXIFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

/**
 * Created by jan on 7/4/16.
 */
public class MessageEncoder {

	private final String xsdUrl;
	private EXIFactory exiFactory;

	public MessageEncoder(String xsdUrl) {
		this.xsdUrl = xsdUrl;
	}

	public byte[] encodeXml(String xml) throws EXIException, IOException, SAXException {
		ByteArrayOutputStream exiOS = new ByteArrayOutputStream();
		EXIResult exiResult = new EXIResult(initExiFactory());
		exiResult.setOutputStream(exiOS);
		encode(xml, exiResult.getHandler());
		exiOS.close();
		return exiOS.toByteArray();
	}

	private void encode(String xml, ContentHandler handler) throws IOException, SAXException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(handler);
		xmlReader.parse(new InputSource(new StringReader(xml)));
	}

	private EXIFactory initExiFactory() throws IOException, EXIException {
		if (exiFactory == null) {
			// create default factory and EXI grammar for schema
			exiFactory = DefaultEXIFactory.newInstance();
			GrammarFactory grammarFactory = GrammarFactory.newInstance();
			Grammars g = grammarFactory.createGrammars(new URL(xsdUrl).openStream());
			exiFactory.setGrammars(g);
		}
		return exiFactory;
	}
}
