package de.unistuttgart.isw.serviceorchestration.servicecore;

import com.siemens.ct.exi.EXIFactory;
import com.siemens.ct.exi.GrammarFactory;
import com.siemens.ct.exi.api.sax.EXISource;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.grammars.Grammars;
import com.siemens.ct.exi.helpers.DefaultEXIFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by jan on 7/4/16.
 */
public class MessageDecoder {
	private final String xsdUrl;
	private EXIFactory exiFactory;

	public MessageDecoder(String xsdUrl) {
		this.xsdUrl = xsdUrl;
	}

	public String decodeXml(byte[] encoded)
			throws EXIException, IOException, SAXException, TransformerException {
		EXISource source = new EXISource(initExiFactory());
		XMLReader reader = source.getXMLReader();
		return decode(reader, encoded);
	}

	private String decode(XMLReader reader, byte[] encoded) throws TransformerException,
			IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(encoded);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		SAXSource exiSource = new SAXSource(new InputSource(in));
		exiSource.setXMLReader(reader);

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			transformer.transform(exiSource, new StreamResult(out));
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		}
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
