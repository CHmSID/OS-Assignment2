import java.net.*;
import java.io.*;
import java.util.*;

/*
* Takes messages from server thread and sends them
* out to other clients
*/
class ServerThread implements Runnable {
	private MessageBuffer msgBuffer;
	private ConnectedClients clients;

	public ServerThread(MessageBuffer msgBuffer, ConnectedClients clients) {
		this.msgBuffer = msgBuffer;
		this.clients = clients;
	}

	public void run() {
		// Loop forever trying to remove a message from the queue
		while(true) {
			String msg = msgBuffer.remove();
			clients.relayMessage(msg);
		}
	}
}

/*
* Holds messages which are awaiting to be sent to other
* clients
*/
class MessageBuffer {
	//something like a queue of Message objects
	private Queue<String> buffer = new LinkedList<String>();
	
	//add method
	public synchronized void add(String msg) {
		buffer.add(msg);
		notifyAll();
	}
	
	//remove method
	public synchronized String remove(){
		while (buffer.isEmpty()) {		
			try {
				wait();
			}
			catch (InterruptedException e) { } 
		}
		
		notifyAll();
		return buffer.remove();
	}
}

/*
* Data structure for holding connected clients
*/
class ConnectedClients {
	private ArrayList<Client> clients;
	private int numClients;

	public ConnectedClients() {
		clients = new ArrayList<Client>();
	}

	// Adds a client to the data structure
	public synchronized void add(Client client) {
		clients.add(client);
		System.out.println("Clients: "  + (++numClients));
	}

	// Removes a client from the data structure
	public synchronized void remove(Client client) {
		clients.remove(client);
		System.out.println("Clients: "  + (--numClients));
	}

	// Relays a message to all clients
	public synchronized void relayMessage(String message) {
		for (Client client : clients) {
			client.send(message);
		}
	}
}

/*
* One per client connected to the server,
* receives messages from client and queues them in the buffer
*/
class Client implements Runnable {
	Socket socket;
	String nickname;
	PrintWriter out;
	BufferedReader in;
	MessageBuffer msgBuffer;
	ConnectedClients clients;

	public Client(Socket socket, MessageBuffer msgBuffer, 
		ConnectedClients clients) {
		this.socket = socket;
		this.msgBuffer = msgBuffer;
		this.clients = clients;
	}

	public void run() {
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));

			// Retrieve nickname
			nickname = in.readLine();
			msgBuffer.add(nickname + " just joined the chatroom...");

			// Read for messages until the client closes			
			String message;
			try {
				while ((message = in.readLine()) != null) {
					msgBuffer.add(nickname + " says: " + message);
				}
			}
			catch (SocketException e) { }

			// Remove client from clients
			// We don't relay anymore messages to them
			clients.remove(this);

			// Close reader, writer and socket
			in.close();
			out.close();
			socket.close();

			msgBuffer.add(nickname + " just left the chatroom...");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Sends a message to the client
	public void send(String message) {
		if (out != null)
			out.println(message);
	}
}

class ChatServer {
	public static void main(String[] args) {
		MessageBuffer msgBuffer = new MessageBuffer();
		ConnectedClients clients = new ConnectedClients();
		ServerSocket serverSocket = null;
		boolean running = true;
		int port = 7777;

		// Create the socket
		System.out.println("Starting up the server");
		try {
			serverSocket = new ServerSocket(port);
		} catch(IOException e) {
			System.out.println("Could not open connection on port " + port);
			e.printStackTrace();
		}

		// Begin consumer thread
		Thread serverThread = new Thread(new ServerThread(msgBuffer, clients));
		serverThread.start();

		// Begin accepting chat clients
		while (running) {
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Create new client
			Client chatClient = new Client(clientSocket, msgBuffer, clients);

			// Create and run client thread
			Thread clientThread = new Thread(chatClient);
			clientThread.start();

			// Add client to the clients data structure			
			clients.add(chatClient);
		}

		// Close socket
		System.out.println("Closing the server");
		try {
			serverSocket.close();
		} catch(IOException e) {
			System.out.println("Could not close connection");
			e.printStackTrace();
		}
	}
}
