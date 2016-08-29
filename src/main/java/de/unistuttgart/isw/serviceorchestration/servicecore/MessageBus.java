package de.unistuttgart.isw.serviceorchestration.servicecore;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;

import java.util.*;

/**
 * Provides access to the input and output ports
 */
public class MessageBus {
	private static final String OUTPUT_ENV_PREFIX = "OUTPUT_";
	private static final String INPUT_ENV_PREFIX = "INPUT_";
	private static final String BOOTSTRAP_SERVER_ENV = "bootstrap.servers";
	private static final String INPUT_TOPIC_SEPARATOR = ",";
	private static final long POLL_TIMEOUT = 1000;

	private Map<String, MessageEncoder> encodersByXsdUrl = new HashMap<>();
	private Map<String, MessageDecoder> decodersByXsdUrl = new HashMap<>();
	private List<String> allTopicNames = new ArrayList<>();
	private Map<String, List<MessageReceiver>> receivers = new HashMap<>();

	private static class Producer {
		static final KafkaProducer<Integer, byte[]> PRODUCER = initProducer();

		private static KafkaProducer<Integer, byte[]> initProducer() {
			return new KafkaProducer<>(initKafkaProperties());
		}
	}

	private static class Consumer {
		static final KafkaConsumer<Integer, byte[]> CONSUMER = initConsumer();

		private static KafkaConsumer<Integer, byte[]> initConsumer() {
			return new KafkaConsumer<>(initKafkaProperties());
		}
	}

	private static Properties initKafkaProperties() {
		Properties props = new Properties();
		props.put("bootstrap.servers", System.getenv(BOOTSTRAP_SERVER_ENV));
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("group.id", "test");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", IntegerSerializer.class.getName());
		props.put("value.serializer", ByteArraySerializer.class.getName());
		props.put("key.deserializer", IntegerDeserializer.class.getName());
		props.put("value.deserializer", ByteArrayDeserializer.class.getName());
		return props;
	}

	public MessageSender createSender(String portName, String xsdUrl, Boolean useSystemEnv) {
		if(useSystemEnv)
			return new MessageSender(Producer.PRODUCER, getEncoder(xsdUrl), getOutputTopicName(portName));
		return new MessageSender(Producer.PRODUCER, getEncoder(xsdUrl), portName);
	}

	public MessageReceiver createReceiver(String portName, String xsdUrl, MessageHandler handler, Boolean useSystemEnv) {
		List<String> topicNames = new ArrayList<>();
		if(useSystemEnv)
			topicNames = getInputTopicNames(portName);
		else topicNames.add(portName);
		allTopicNames.addAll(topicNames);
		Consumer.CONSUMER.subscribe(allTopicNames);
		MessageReceiver receiver = new MessageReceiver(getDecoder(xsdUrl), handler);
		for (String topic : topicNames) {
			receivers.computeIfAbsent(topic, d -> new ArrayList<>()).add(receiver);
		}
		return receiver;
	}

	public void runListener() {
		while (true) {
			ConsumerRecords<Integer, byte[]> records = Consumer.CONSUMER.poll(POLL_TIMEOUT);
			for (ConsumerRecord<Integer, byte[]> record : records) {
				if (!(receivers.containsKey(record.topic()))) {
					continue;
				}

				for (MessageReceiver receiver : receivers.get(record.topic())) {
					receiver.onMessageReceived(record.value());
				}
			}
		}
	}

	private MessageDecoder getDecoder(String xsdUrl) {
		return decodersByXsdUrl.computeIfAbsent(xsdUrl, (k) -> new MessageDecoder(xsdUrl));
	}

	private String getOutputTopicName(String portName) {
		return System.getenv(OUTPUT_ENV_PREFIX + portName);
	}

	private List<String> getInputTopicNames(String portName) {
		String topics = System.getenv(INPUT_ENV_PREFIX + portName);
		return Arrays.asList(topics.split(INPUT_TOPIC_SEPARATOR));
	}

	private MessageEncoder getEncoder(String xsdUrl) {
		return encodersByXsdUrl.computeIfAbsent(xsdUrl, (k) -> new MessageEncoder(xsdUrl));
	}
}
