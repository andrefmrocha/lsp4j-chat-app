/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package io.typefox.lsp4j.chat.server;

import io.typefox.lsp4j.chat.shared.ChatClient;
import org.eclipse.lsp4j.jsonrpc.Launcher;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServerLauncher {

	public static void main(String[] args) throws Exception {
		// create the chat server
		ChatServerImpl chatServer = new ChatServerImpl();
		ExecutorService threadPool = Executors.newCachedThreadPool();

		Integer port = Integer.valueOf(args[0]);
		// create the socket server
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("The chat server is running on port " + port);
			threadPool.submit(() -> {
				while (true) {
					// wait for clients to connect
					Socket socket = serverSocket.accept();
					// create a JSON-RPC connection for the accepted socket
					Launcher<ChatClient> launcher = new Launcher.Builder<ChatClient>()
							.setRemoteInterface(ChatClient.class)
							.setExecutorService(threadPool)
							.setInput(socket.getInputStream())
							.setOutput(socket.getOutputStream())
							.setLocalService(chatServer)
							.create();
					// connect a remote chat client proxy to the chat server
					Runnable removeClient = chatServer.addClient(launcher.getRemoteProxy());
                    /*
                     * Start listening for incoming messages.
                     * When the JSON-RPC connection is closed
                     * disconnect the remote client from the chat server.
                     */
					new Thread(() -> {
						try {
							launcher.startListening().get();
							removeClient.run();
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					}).start();
				}
			});
			System.out.println("Enter any character to stop");
			System.in.read();
			System.exit(0);
		}
	}

}
