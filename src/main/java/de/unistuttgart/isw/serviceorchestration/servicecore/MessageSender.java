package de.unistuttgart.isw.serviceorchestration.servicecore;

import com.siemens.ct.exi.exceptions.EXIException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * A sender that can send messages through an output port
 */
public class MessageSender {
	private final MessageEncoder encoder;
	private final KafkaProducer<Integer, byte[]> producer;
	private final String topicName;

	MessageSender(KafkaProducer<Integer, byte[]> producer, MessageEncoder encoder,
			String topicName) {
		this.encoder = encoder;
		this.producer = producer;
		this.topicName = topicName;
	}

	public void send(String xml) throws IOException, SAXException, EXIException {
		byte[] message = encoder.encodeXml(xml);
		producer.send(new ProducerRecord<>(topicName, 0, message));
	}
}
