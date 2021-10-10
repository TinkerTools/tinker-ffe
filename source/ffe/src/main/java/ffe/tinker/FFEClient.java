/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2021 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.10
 */

package ffe.tinker;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

/*
 * The FFEClient class encapsulates a socket connection to an TinkerServer
 * started by an executing Tinker program; TinkerSystem and TinkerUpdate
 * objects are sent by the TinkerServer to the FFEClient on request
 */

public class FFEClient {
	private static final Logger logger = Logger.getLogger("ffe");

	private Socket client; // Socket connection to the server

	private InetSocketAddress address; // Server address

	private InputStream in; // Input from the server

	private ObjectInputStream oin; // Input from the server

	private OutputStream out; // Output to the server

	private ObjectOutputStream oout; // Output to the server

	private TinkerSystem system; // Tinker system definition

	private TinkerUpdate update; // Tinker update information

	private FFEMessage message; // Message Passed Between Client & Server

	// Various connection status variables
	private int retryCount = 0; // Count of Client attempts to Connect

	private int retryLimit = 10000; // Maximum number of retries

	private boolean connectionMade = false; // True when connection is made

	// closed is True if the server closes an open connection
	// or if the retryLimit is reached
	private boolean closed = false;

	public FFEClient() {
		address = new InetSocketAddress(2000);
	}

	public FFEClient(InetSocketAddress a) {
		address = a;
	}

	public FFEClient(int port) {
		address = new InetSocketAddress(port);
	}

	/*
	 * Attempts to connect to a Tinker FServer; if this FClient is
	 * already connected, the connection will be closed.
	 */
	public void connect() {
		if (isConnected()) {
			release();
		}
		closed = false;
		connectionMade = false;
		client = new Socket();
		try {
			client.connect(address, 100);
			client.setTcpNoDelay(true);
			out = client.getOutputStream();
			oout = new ObjectOutputStream(out);
			in = client.getInputStream();
			oin = new ObjectInputStream(in);
			connectionMade = true;
			logger.info("Connected to Tinker Server: " + client);
		} catch (Exception e) {
			connectionMade = false;
			logger.info("Connection to Tinker Server Failed: " + client);
			client = null;
		} finally {
			if (!isConnected()) {
				release();
			}
		}
	}

	public TinkerSystem getSystem() {
		readSocket();
		return system;
	}

	public TinkerUpdate getUpdate() {
		readSocket();
		return update;
	}

	public boolean isClosed() {
		return closed;
	}

	public boolean isConnected() {
		if (client != null && client.isConnected()) {
			return true;
		}
		return false;
	}

	public void readSocket() {
		try {
			while (oin != null && in.available() > 0) {
				Object o = oin.readObject();
				if (o instanceof FFEMessage) {
					message = (FFEMessage) o;
					//logger.info(message.toString());
					if (message.getMessage() == FFEMessage.SYSTEM) {
						system = (TinkerSystem) oin.readObject();
						system.read = false;
					} else if (message.getMessage() == FFEMessage.UPDATE) {
						update = (TinkerUpdate) oin.readObject();
						update.read = false;
					} else if (message.getMessage() == FFEMessage.CLOSING) {
						closed = true;
						release();
					}
				}
			}
			if (system == null) {
				message = new FFEMessage(FFEMessage.SYSTEM);
				oout.reset();
				oout.writeObject(message);
				oout.flush();
			} else if (update == null || update.read) {
				message = new FFEMessage(FFEMessage.UPDATE);
				if (update != null) {
					if (update.type == TinkerUpdate.SIMULATION) {
						message.setTime(update.time);
					} else {
						message.setStep(update.step);
					}
				}
				oout.reset();
				oout.writeObject(message);
				oout.flush();
			}
		} catch (Exception e) {
			logger.warning("Exception reading data from Tinker\n" + e.toString());
			release();
		}
	}

	public void release() {
		if (client == null) {
			return;
		}
		retryCount++;
		if (retryCount > retryLimit || connectionMade) {
			closed = true;
		}
		if (client != null && client.isConnected() && oout != null) {
			try {
				FFEMessage close = new FFEMessage(FFEMessage.CLOSING);
				oout.reset();
				oout.writeObject(close);
				oout.flush();
			} catch (Exception e) {
				oout = null;
			}
		}
		try {
			if (oin != null) {
				oin.close();
			}
			if (in != null) {
				in.close();
			}
			if (oout != null) {
				oout.close();
			}
			if (out != null) {
				out.close();
			}
			if (client != null) {
				client.close();
			}
		} catch (Exception e) {
			client = null;
		} finally {
			in = null;
			oin = null;
			out = null;
			oout = null;
			client = null;
		}
	}
}
