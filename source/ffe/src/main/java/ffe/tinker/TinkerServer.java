/*
 * <p>Title: Force Field Explorer</p>
 * <p>Description: Force Field Explorer Molecular Modeling Program</p>
 * <p>Copyright: Copyright (c) 2004-2020 Jay William Ponder</p>
 * <p>Institution: Jay Ponder Lab, Washington University in Saint Louis</p>
 * @author Michael J. Schnieders
 * @version 8.8
 */

package ffe.tinker;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ListIterator;
import java.util.Vector;
import java.util.logging.Logger;


/*
 * The TinkerServer is launched by Tinker executables to allow
 * Force Field Explorer Clients to connect.
 */

public class TinkerServer implements Runnable {

	private Logger logger = Logger.getLogger("ffe");
	
	private static FFEMessage closing = new FFEMessage(FFEMessage.CLOSING);

	private ServerSocket server;

	private int serverPort = 2000;

	private int serverTimeout = 100;

	private Thread thread;

	private int sleepTime = 1;

	private boolean init = true;

	private int cycle = 0;

	private boolean shutdown = false;

	private boolean request = false;

	private Vector<Socket> clients = new Vector<Socket>();

	private Vector<ObjectOutputStream> outputs = new Vector<ObjectOutputStream>();

	private Vector<ObjectInputStream> inputs = new Vector<ObjectInputStream>();

	private TinkerSystem system = null;

	private TinkerUpdate update = null;

	public TinkerServer(TinkerSystem s) {
		system = s;
	}

	private void accept() {
		Socket client = null;
		ObjectInputStream oin = null;
		ObjectOutputStream oout = null;
		if (server != null) {
			try {
				client = server.accept();
				if (client != null && client.isConnected()) {
					client.setTcpNoDelay(true);
					clients.add(client);
					oin = new ObjectInputStream(client.getInputStream());
					inputs.add(oin);
					oout = new ObjectOutputStream(client.getOutputStream());
					outputs.add(oout);
					Logger.getLogger("ffe").info(
							"Client connected\n" + client.toString());
				}
			} catch (Exception e) {
				if (client != null) {
					clients.remove(client);
				}
				if (oout != null) {
					outputs.remove(oout);
				}
				if (oin != null) {
					inputs.remove(oin);
				}
			}
		}
	}

	private void closeClient(int index) {
		Socket client = clients.get(index);
		ObjectOutputStream oout = outputs.get(index);
		ObjectInputStream oin = inputs.get(index);
		try {
			oout.reset();
			oout.writeObject(closing);
			oout.flush();
		} catch (Exception e) {
			try {
				outputs.remove(index);
				inputs.remove(index);
				clients.remove(index);
				if (oout != null) {
					oout.close();
				}
				if (oin != null) {
					oin.close();
				}
				if (client != null) {
					client.close();
				}
			} catch (Exception ex) {
				return;
			}
		}
	}

	private void closeServer() {
		for (int i = 0; i < clients.size(); i++) {
			lastUpdate(i);
		}
		while (!clients.isEmpty()) {
			closeClient(0);
			try {
				synchronized (this) {
					wait(10);
				}
			} catch (Exception e) {
				Logger.getLogger("ffe").severe(e.toString());
			}
		}
		try {
			if (server != null) {
				server.close();
				server = null;
			}
		} catch (Exception e) {
			return;
		}
	}

	public boolean isAlive() {
		if (thread == null) {
			return false;
		} else if (thread.isAlive()) {
			return true;
		} else {
			return false;
		}
	}

	private void lastUpdate(int index) {
		try {
			ObjectOutputStream oout = outputs.get(index);
			Socket client = clients.get(index);
			if (client != null && client.isConnected() && oout != null) {
				FFEMessage last = new FFEMessage(FFEMessage.SYSTEM);
				if (system != null) {
					oout.reset();
					oout.writeObject(last);
					oout.writeObject(system);
					oout.flush();
				}
				if (update != null) {
					oout.reset();
					last.setMessage(FFEMessage.UPDATE);
					oout.writeObject(last);
					oout.writeObject(update);
					oout.flush();
				}
				last.setMessage(FFEMessage.CLOSING);
				oout.reset();
				oout.writeObject(last);
				oout.flush();
			}
		} catch (Exception e) {
			logger.severe("" + e);
		}
	}

	public void loadUpdate(TinkerUpdate u) {
		update = u;
	}

	public boolean needUpdate() {
		if (clients.size() == 0) {
			sleepTime = 1;
			return false;
		}
		if (request != true) {
			return false;
		}
		sleepTime = 1;
		return true;
	}

	public void run() {
		startServer();
		while (!shutdown) {
			accept();
			send();
			try {
				Thread.sleep(sleepTime);
			} catch (Exception e) {}
			if (init) {
				cycle++;
				if (cycle >= 10) {
					init = false;
				}
			}
		}
		accept();
		send();
		closeServer();
	}

	private void send() {
		if (system == null) {
			return;
		}
		if (clients.size() == 0) {
			return;
		}
		ObjectInputStream oin;
		ObjectOutputStream oout;
		Socket client;
		ListIterator<ObjectOutputStream> lout;
		ListIterator<ObjectInputStream> lin;
		ListIterator<Socket> lclient;
		Vector<Socket> closed = new Vector<Socket>();
		for (lout = outputs.listIterator(), lin = inputs.listIterator(), lclient = clients
				.listIterator(); lout.hasNext();) {
			oin = lin.next();
			oout = lout.next();
			client = lclient.next();
			if (!client.isConnected() || client.isClosed()) {
				closed.add(client);
			} else {
				try {
					FFEMessage message = null;
					while (oin != null
							&& client.getInputStream().available() > 0) {
						Object o = oin.readObject();
						if (o instanceof FFEMessage) {
							message = (FFEMessage) o;
							if (message.getMessage() == FFEMessage.CLOSING) {
								closed.add(client);
								message = null;
								break;
							}
						}
					}
					if (message != null) {
						if (message.getMessage() == FFEMessage.SYSTEM) {
							synchronized (system) {
								oout.reset();
								oout.writeObject(message);
								oout.writeObject(system);
								oout.flush();
							}
						} else if (message.getMessage() == FFEMessage.UPDATE) {
							request = true;
							if (update != null && update.isNewer(message)) {
								synchronized (update) {
									//logger.info("Sending Update");
									oout.reset();
									oout.writeObject(message);
									oout.writeObject(update);
									oout.flush();
								}
							}
						}
					}
				} catch (Exception e) {
					closed.add(client);
				}
			}
		}
		for (Socket s : closed) {
			int index = closed.indexOf(s);
			closeClient(index);
		}
	}

	public void setUpdated() {
		request = false;
	}

	public void start() {
		if (thread == null || !thread.isAlive()) {
			thread = new Thread(this);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
	}

	private void startServer() {
		try {
			server = new ServerSocket();
			server.setSoTimeout(serverTimeout);
			server.setReuseAddress(true);
			server.bind(new InetSocketAddress(InetAddress.getLocalHost(),
					serverPort));
		} catch (Exception e) {
			try {
				server.bind(new InetSocketAddress(InetAddress.getByName(null),
						serverPort));
			} catch (Exception ex) {
				Logger.getLogger("ffe").severe(
						"SERVER -- Could not start\n" + e.toString());
				return;
			}
		}
		Logger.getLogger("ffe").info(
				"Tinker Server Address: " + server.getLocalSocketAddress());
//		 JOptionPane.showMessageDialog(null, server.getLocalSocketAddress(),
//		 "Server Address", JOptionPane.ERROR_MESSAGE);
	}

	public void stop() {
		shutdown = true;
	}
}
