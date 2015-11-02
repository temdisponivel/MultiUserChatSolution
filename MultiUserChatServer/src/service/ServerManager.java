package service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JOptionPane;

public class ServerManager implements Runnable {

	public HashMap<User, ServerService> _services = new HashMap<User, ServerService>();
	public ServerSocket _socket = null;
	
	public ServerManager() {
		try {
			_socket = new ServerSocket(5597);
			JOptionPane.showMessageDialog(null, InetAddress.getLocalHost() + ":" + String.valueOf(5597));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void Init() {
		new Thread(this).start();
	}
	
	synchronized void CloseServer(ServerService serviceClosed) {
		User userDisconnected = null;
		for (Entry<User, ServerService> service : _services.entrySet()) {
			if (service.getValue().equals(serviceClosed)) {
				userDisconnected = service.getKey();
				break;
			}
		}
		if (userDisconnected != null) {
			this.SendMessageToAll(new Message(new Message.UserDisconnected(userDisconnected)));
			_services.remove(userDisconnected);
		}
	}

	synchronized public void OnReceive(Message message, ServerService service) {
		switch (message.GetMessageType()) {
		case UserConnected:
			User user  = ((Message.UserConnect)message.GetMessage()).GetData();
			this.SendAllUserConnect(service);
			_services.put(user, service);
			this.HandleMessage(message);
			break;
		case UserDisconnected:
			_services.remove(((Message.UserDisconnected)message.GetMessage()).GetData());
			this.HandleMessage(message);
			break;
		default:
			this.HandleMessage(message);
		}
	}
	
	synchronized public void SendAllUserConnect(ServerService service) {
		for (User user : _services.keySet()) {
			try {
				service.Send(new Message(new Message.UserConnect(user)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	synchronized public Collection<User> GetUsers() {
		return _services.keySet();
	}
	
	/**
	 * message Routine to handle a text message.
	 * @param message Message to send.
	 */
	synchronized public void HandleMessage(Message message) {
		System.out.println(message);
		if (message.IsPrivate()) {
			try {
				_services.get(message.GetReceiver()).Send(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			this.SendMessageToAll(message);
		}
	}
	/**
	 * Sends a message to all users, except the sender, if there is any.
	 * @param message Message to send.
	 */
	synchronized public void SendMessageToAll(Message message) {
		for (Entry<User, ServerService> service : _services.entrySet()) {
			if (message.GetSender() != null && service.getKey().equals(message.GetSender())) {
				continue;
			}
			try {
				service.getValue().Send(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Socket client = _socket.accept();
				new ServerService(this, client);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
}
