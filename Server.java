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
}

/*
* A custom type for a message
*/
class Message{

	private String text;
	private String nickname;

	public Message(String nickname, String text){

		this.nickname = nickname;
		this.text = text;
	}

	public String getText(){

		return text;
	}

	public String getNickname(){

		return nickname;
	}
}

/*
* One per client connected to the server,
* receives messages from client and queues them in the buffer
*/
class ChatThread implements Runnable{

	Socket socket;
	String nickname;

	public ChatThread(Socket socket, String nickname){

		this.socket = socket;
		this.nickname = nickname;
	}

	public void run(){

	}
}

class Server{

	public static void main(String[] args){

		ServerSocket serverSocket = null;
		int port = 34000;

		System.out.println("Starting up the server");
		try{

			serverSocket = new ServerSocket(port);
		} catch(IOException e){

			System.out.println("Could not open connection on port " + port);
			e.printStackTrace();
		}

		try{

			serverSocket.close();
		} catch(IOException e){

			System.out.println("Could not close connection");
			e.printStackTrace();
		}
	}
}