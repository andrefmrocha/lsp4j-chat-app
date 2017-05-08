package io.typefox.lsp4j.chat.generic.client;

import java.net.Socket;

import io.typefox.lsp4j.chat.generic.shared.SocketLauncher;

public class ChatClientLauncher {

	public static void main(String[] args) throws Exception {
		ChatClient chatClient = new ChatClient();

		String host = args[0];
		Integer port = Integer.valueOf(args[1]);
		try (Socket socket = new Socket(host, port)) {
			SocketLauncher launcher = new SocketLauncher(socket, chatClient);
			launcher.startListening().thenRun(() -> System.exit(0));
			chatClient.start(launcher.getRemoteProxy());
		}
	}

}