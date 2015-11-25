import java.net.*;
import java.io.*;

/*
* Takes messages from server thread and sends them
* out to other clients
*/
class ServerThread{

}

/*
* Holds messages which are awaiting to be sent to other
* clients
*/
class MessageBuffer{

	//something like a queue of Message objects

	//public void add(String msg)
}


class ConnectedClients {
	private ArrayList<Client> clients;
	private int numClients;

	public ConnectedClients() {
		clients = new ArrayList<Client>();
	}

	public synchronized void add(Client client) {
		clients.add(client);
		numClients++;
	}

	public synchronized void remove(Client client) {
		clients.remove(client);
		numClients--;
	}

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
class Client implements Runnable{

	Socket socket;
	String nickname;
	PrintWriter out;
	BufferedReader in;
	MessageBuffer msgBuffer;
	ConnectedClients clients;

	public Client(Socket socket, MessageBuffer msgBuffer, ConnectedClients clients){

		this.socket = socket;
		this.msgBuffer = msgBuffer;
		this.clients = clients;
	}

	public void run(){
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		// Retrieve nickname
		nickname = in.readLine();
		msgBuffer.add(nickname + " just joined the chatroom...");

		String message;
		while ((message = in.readLine()) != null) {
			msgBuffer.add(nickname + "says: " + message);
		}

		clients.remove(this);
		in.close();
		out.close();
		socket.close();
		msgBuffer.add(nickname + " just left the chatroom...");
	}
}

class ChatServer{

	public static void main(String[] args){

		MessageBuffer msgBuffer = new MessageBuffer();
		ConnectedClients clients = new ConnectedClients();
		ServerSocket serverSocket = null;

		int port = 34000;

		System.out.println("Starting up the server");
		try{

			serverSocket = new ServerSocket(port);
		} catch(IOException e){

			System.out.println("Could not open connection on port " + port);
			e.printStackTrace();
		}

		// Begin consumer thread
		Thread serverThread = new Thread(new ServerThread(msgBuffer, clients));
		serverThread.start();

		// Begin accepting chat clients
		while (true) {
			Socket clientSocket = server.accept();
			Client chatClient = new Client(clientSocket, msgBuffer, clients);
			clients.add(chatClient);

			Thread clientThread = new Thread(chatClient);
			clientThread.start();
		}

		System.out.println("Closing the server");
		try{

			serverSocket.close();
		} catch(IOException e){

			System.out.println("Could not close connection");
			e.printStackTrace();
		}
	}
}