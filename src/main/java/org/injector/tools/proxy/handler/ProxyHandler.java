package org.injector.tools.proxy.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.injector.tools.config.HostProxyConfig;
import org.injector.tools.event.EventRunnableHandler;
import org.injector.tools.log.Logger;
import org.injector.tools.payload.Payload;

public abstract class ProxyHandler implements EventRunnableHandler {

	private ChannelSelector channelSelector;

	public void setChannelSelector(ChannelSelector channelSelector) {
		this.channelSelector = channelSelector;
	}

	public ChannelSelector getChannelSelector() {
		return channelSelector;
	}

	public Selector getSelector() {
		return channelSelector.getSelector();
	}

	public ExecutorService getService() {
		return channelSelector.getService();
	}

	protected SocketChannel client = null;
	protected SocketChannel remote = null;

	protected Payload payload;
	protected String requestLine;

	protected boolean skipReadRequestLine = false;

	protected HostProxyConfig proxyConfig;

	

	public Payload getPayload() {
		return payload;
	}

	public ProxyHandler() {
		fireInitListener();
	}

	public ProxyHandler(SocketChannel client, HostProxyConfig proxyConfig, ChannelSelector channelSelector) {
		this();
		this.client = client;
		this.proxyConfig = proxyConfig;
		this.channelSelector = channelSelector;
		payload = new Payload(proxyConfig.getPayload());
	}

	public HostProxyConfig getProxyConfig() {
		return proxyConfig;
	}

	public void setProxyConfig(HostProxyConfig proxyConfig) {
		this.proxyConfig = proxyConfig;
	}

	public void startHandler() {
		fireStartListener();
		defaultLifeCycleCaller();
	}

	private void defaultLifeCycleCaller() {

		// read client request line and setup payload placeholder
		if (!skipReadRequestLine)
			readClientRequestLine();

		// setup proxy/server mode
		// and setup in/out stream
		connectToProxyServer();

		/* in case of all things goes well, connect server with client direct */
		// addErrorListener(this::closeConnection);
		// addSuccessListener(this::transferDataFromClientToProxy);
		// addSuccessListener(this::transferDataFromProxyToClient);

		readResponseFromProxy();
		writePayloadToRemoteHost();

		// transferDataFromClientToProxy();
		// transferDataFromProxyToClient();

		registerChannelToSelector();
	}
	
	protected void readResponseFromProxy() {
		getService().execute(() -> {
			handelProxyResponse();
			fireSuccessListener();
		});
	}

	/**
	 * start to read response from proxywrapper server and <b>analysis</b> that
	 * response. <br>
	 * then write the edited response back to client <br>
	 * <br>
	 * can ignore write that response at all to client
	 */
	abstract void handelProxyResponse();


	protected void setChannelsBlockMode(boolean block) throws IOException {
		client.configureBlocking(block);
		remote.configureBlocking(block);
	}
	protected void registerChannelToSelector() {

		try {
			setChannelsBlockMode(false);
			client.register(getSelector(), SelectionKey.OP_READ, remote);
			remote.register(getSelector(), SelectionKey.OP_READ, client);
		} catch (ClosedChannelException e) {
			Logger.debug(getClass(), "registerChannelToSelector", e.getMessage());
		} catch (IOException e) {
			Logger.debug(getClass(), "registerChannelToSelector", e.getMessage());
		}
	}

	protected void connectToProxyServer() {
		try {
			connectToProxyServer(proxyConfig.getProxyHost(), proxyConfig.getProxyPort());
		} catch (IOException e) {
			Logger.debug(e.getClass(), "error", "Can't connect to " + proxyConfig.getProxyHost() + ":"
					+ proxyConfig.getProxyPort() + "\n".concat(e.getMessage()));
		}
	}

	protected void connectToProxyServer(String host, int port) throws IOException {
		InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
		remoteConnect(remoteAddress);
	}

	/**
	 * @param host
	 * @param port
	 * @param remoteAddress
	 * @throws IOException
	 */
	protected void remoteConnect(InetSocketAddress remoteAddress) throws IOException {
		remote = SocketChannel.open();
		remote.connect(remoteAddress);
		remote.finishConnect();
		Logger.debug(getClass(), "connect to host", remoteAddress.toString());
	}


	protected void readClientRequestLine() {
		ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
		int bytes_read = 0;
		try {
			bytes_read = client.read(buffer);
			buffer.flip();
			requestLine = new String(buffer.array(), 0, bytes_read);

			if (payload == null)
				payload = new Payload(proxyConfig.getPayload());
			payload.setRequest(requestLine);
			Logger.debug(getClass(), "Request Line <==> Payload Mode : ", payload.getRawPayload());
			Logger.debug(getClass(), "Client Request Line", requestLine);
		} catch (IOException e) {
			Logger.debug(e.getClass(), "Can't read request line.\n".concat(e.getMessage()));
		}
	}

	protected void writePayloadToRemoteHost() {
		String raw = payload.getRawPayload();
		ArrayList<Integer> index = getSplitIndex(raw);
		try {

			if (index == null) {
				remote.write(ByteBuffer.wrap(raw.getBytes()));
				Logger.debug(getClass(), "write payload raw to Proxy", raw);
			} else {
				ByteBuffer buffer;
				for (int i = 0; i < index.size(); i += 2) {
					buffer = ByteBuffer.wrap(raw.substring(index.get(i), index.get(i + 1)).getBytes());
					buffer.flip();
					remote.write(buffer);
					Logger.debug(getClass(), "write payload raw#" + i / 2,
							raw.substring(index.get(i), index.get(i + 1)));
				}
			}
		} catch (IOException e) {
			Logger.debug(e.getClass(), "Message", e.getMessage());
		}

	}



	public void sleepTime(int timeout) {
		try {
			TimeUnit.MILLISECONDS.sleep(timeout);
			Logger.debug("\t sleeped for :" + timeout);
		} catch (InterruptedException e) {
			Logger.debug(e.getClass(), "Message", e.getMessage());
		}
	}

	public void sendNormalRequest() throws IOException {
		Logger.debug(getClass(), "try to send normal request");
		Logger.debug(getClass(), requestLine);
		remote.write(ByteBuffer.wrap(requestLine.getBytes()));
	}

	void closeConnection() {
		try {
			remote.close();
			client.close();
			Logger.debug(getClass(), "remote is " + (remote.isConnected() ? "alive" : "closed"));
			Logger.debug(getClass(), "client is " + (client.isBlocking() ? "alive" : "closed"));
		} catch (IOException e) {
			Logger.debug(getClass(), "error - close i/o sockets");
			Logger.debug(e.getClass(), "Message", e.getMessage());
		}
	}

	/**
	 * @param requestPayload
	 *            payload request to find split in it
	 * @return null if no split found or index of data to write [0,46] [54, 80]
	 */
	public ArrayList<Integer> getSplitIndex(String requestPayload) {
		return Payload.getSplitIndexs(requestPayload);
	}

	public void debugSocketsChannel(Exception e) {
		Logger.debug(getClass(), "remote is " + (remote.isConnected() ? "alive" : "closed"));
		Logger.debug(getClass(), "client is " + (client.isBlocking() ? "alive" : "closed"));
		Logger.debug(e.getClass(), "Message", e.getMessage());
	}

	public String readClientRequest(SocketChannel client) {
		ByteBuffer buffer = readClientRequestBytes(client);
		return new String(buffer.array(), 0, buffer.limit());
	}

	public ByteBuffer readClientRequestBytes(SocketChannel client) {
		ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);
		try {
			client.read(buffer);
		} catch (IOException e) {
			Logger.debug(getClass(), "error", "Can't read request line.\n".concat(e.getMessage()));
		}
		buffer.flip();
		return ByteBuffer.wrap(buffer.array(), 0, buffer.limit());
	}

	
	protected void clearMomery() {
		try {
			client.close();
			remote.close();
		} catch (IOException e) {
			Logger.debug(e.getClass(), "Error closeing client and remote channel", e.getMessage());
		}

		payload = null;
		requestLine = null;

		skipReadRequestLine = false;

		proxyConfig = null;

	}
	
	@Override
	protected void finalize() throws Throwable {
		clearMomery();
		clearListeners();
		super.finalize();
	}
}
