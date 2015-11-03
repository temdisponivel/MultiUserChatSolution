/*
 * Matheus de Almeida
 * Victor Dias
 */

package service;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import service.Message.FileMessage;

public class ServerManager extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;
	public HashMap<User, ServerService> _services = new HashMap<User, ServerService>();
	public ServerSocket _socket = null;
	protected JTextPane _textMessages = new JTextPane();
	protected SimpleAttributeSet _attributeColor = new SimpleAttributeSet();
	
	public ServerManager() {
		StyleConstants.setFontSize(_attributeColor, 16);
		StyleConstants.setForeground(_attributeColor, Color.black);
	}
	
	public void Init() {
		try {
			_socket = new ServerSocket(5597);
		} catch (BindException e) {
			JOptionPane.showMessageDialog(null, e);
			return;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e);
			return;
		}
		new Thread(this).start();
		_textMessages.setEditable(false);
		this.setSize(800, 600);
		this.getContentPane().setLayout(new BorderLayout());
		this.add(_textMessages, BorderLayout.CENTER);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("SERVER CHAT");
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Finish();
				e.getWindow().dispose();
			}
		});
		try {
			JOptionPane.showMessageDialog(null, InetAddress.getLocalHost() + ":" + String.valueOf(5597));
		} catch (HeadlessException | UnknownHostException e1) {
			e1.printStackTrace();
		}
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
		if (message.GetSender() != null) {
			StyleConstants.setForeground(_attributeColor, message.GetSender().color);
		}
		else {
			StyleConstants.setForeground(_attributeColor, Color.BLACK);
		}
		switch (message.GetMessageType()) {
		case File:
			try {
				_textMessages.getDocument().insertString(
						_textMessages.getDocument().getLength(), message.GetSender() + " sent a file named: "
								+ ((FileMessage) message.GetMessage()).name + " to "
								+ (message.IsPrivate() ? message.GetReceiver() : " all.")
								+ "\n", _attributeColor);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
			break;
		default:
			try {
				_textMessages.getDocument().insertString(
						_textMessages.getDocument().getLength(), message.toString() + "\n", _attributeColor);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
			break;
		}

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
	
	private void Finish() {
		for (Entry<User, ServerService> service : _services.entrySet()) {
			service.getValue().Finish();
		}		
	}	
}
