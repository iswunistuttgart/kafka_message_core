package de.unistuttgart.isw.serviceorchestration.servicecore;

/**
 * Created by jan on 7/4/16.
 */
public class MessageReceiver {
	private final MessageDecoder decoder;
	private final MessageHandler handler;

	public MessageReceiver(MessageDecoder decoder, MessageHandler handler) {
		this.decoder = decoder;
		this.handler = handler;
	}

	void onMessageReceived(byte[] value) {
		try {
			handler.handleMessage(decoder.decodeXml(value));
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}
}
