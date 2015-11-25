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
	private Queue buffer = new LinkedList<String>();
	
	//add method
	public synchronized void add(String msg) {
		buffer.add(msg);
		notifyAll();
	}
	
	//remove method
	public synchronized String remove(){
		while(queue.isEmpty()){		
			try(){
				wait();
			}
			catch (InterruptedException e) { } 
		}
		
		notifyAll();
		return buffer.remove();
		
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

	public Client(Socket socket, MessageBuffer msgBuffer){

		this.socket = socket;
		this.msgBuffer = msgBuffer;
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

		in.close();
		out.close();
		socket.close();
		msgBuffer.add(nickname + " just left the chatroom...");
	}

	public synchronized void relayMessage() {

	}
}

class ChatServer{

	public static void main(String[] args){

		MessageBuffer msgBuffer = new MessageBuffer();
		ServerSocket serverSocket = null;
		int port = 34000;

		System.out.println("Starting up the server");
		try{

			serverSocket = new ServerSocket(port);
		} catch(IOException e){

			System.out.println("Could not open connection on port " + port);
			e.printStackTrace();
		}

		// Begin accepting chat clients
		while (true) {
			Socket clientSocket = server.accept();
			Client chatClient = new Client(clientSocket, msgBuffer);
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
