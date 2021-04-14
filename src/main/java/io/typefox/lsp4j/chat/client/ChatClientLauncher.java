/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package io.typefox.lsp4j.chat.client;

import io.typefox.lsp4j.chat.shared.ChatServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;

import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ChatClientLauncher {

	public static void main(String[] args) throws Exception {
		// create the chat client
		ChatClientImpl chatClient = new ChatClientImpl();

		String host = args[0];
		Integer port = Integer.valueOf(args[1]);
		// connect to the server
		try (Socket socket = new Socket(host, port)) {
			// open a JSON-RPC connection for the opened socket
			Launcher<ChatServer> launcher = new Launcher.Builder<ChatServer>()
					.setRemoteInterface(ChatServer.class)
					.setExecutorService(Executors.newSingleThreadExecutor())
					.setInput(socket.getInputStream())
					.setOutput(socket.getOutputStream())
					.setLocalService(chatClient)
					.create();
			/*
			 * Start listening for incoming messages.
			 * When the JSON-RPC connection is closed
			 * disconnect the remote client from the chat server.
			 */
			Future<Void> future = launcher.startListening();
			chatClient.start(launcher.getRemoteProxy());
			future.get();
		}
	}

}
