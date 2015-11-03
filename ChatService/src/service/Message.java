/*
 * Matheus de Almeida
 * Victor Dias
 */

package service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JOptionPane;

/**
 * A message to send or receive from chat.
 * @author temdi
 *
 */
public class Message implements Serializable {
	
	static public class TextMessage extends BaseMessage<String> {
		private static final long serialVersionUID = 1L;
		
		public TextMessage(String data) {
			super();
			super.SetData(data);
		}
	}
	
	static public class FileMessage extends BaseMessage<byte[]> {
		private static final long serialVersionUID = 1L;
		public String name = "";
		public FileMessage(File data) throws IOException {
			super();
			if (!data.canRead()) {
				JOptionPane.showMessageDialog(null, "Access denied. We cannot read this file.");
				throw new IOException("Access denied");
			}
			try {
				FileInputStream fileData = new FileInputStream(data);
				byte[] bytes = new byte[(int) data.length()];
				fileData.read(bytes);
				super.SetData(bytes);
				name = data.getName();
				fileData.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String toString() {
			return name;
		}
	}
	
	static public class ObjectMessage<T extends Serializable> extends BaseMessage<T> {
		private static final long serialVersionUID = 1L;

		public ObjectMessage(T data) {
			super();
			super.SetData(data);
		}
	}
	
	static public class UserConnect extends BaseMessage<User> {
		private static final long serialVersionUID = 1L;

		public UserConnect(User data) {
			super();
			super.SetData(data);
		}
	}
	
	static public class UserDisconnected extends BaseMessage<User> {
		private static final long serialVersionUID = 1L;

		public UserDisconnected(User data) {
			super();
			super.SetData(data);
		}
	}
	
	private static final long serialVersionUID = 1L;
	protected BaseMessage<?> _message = null;
	protected MessageType _type = MessageType.Text;
	protected User _sender = null;
	protected User _receiver = null;
	
	/**
	 * Create a message.
	 */
	public Message(BaseMessage<?> message, User receiver) {
		_message = message;
		_receiver = receiver;
	}
	
	/**
	 * Create a message that will send a File.
	 * @param message Message to send.
	 * @param receiver User that will receive. If null, this message will be public.
	 */
	public Message(FileMessage message, User receiver) {
		this((BaseMessage<?>)message, receiver);
		_type = MessageType.File;
	}

	/**
	 * Create a message that will send a text.
	 * @param message Message to send.
	 * @param receiver User that will receive. If null, this message will be public.
	 */
	public Message(TextMessage message, User receiver) {
		this((BaseMessage<?>)message, receiver);
		_type = MessageType.Text;
	}
	
	/**
	 * Create a message that will send a user connect.
	 * @param message Message to send.
	 * @param receiver User that will receive. If null, this message will be public.
	 */
	public Message(UserConnect message) {
		this((BaseMessage<?>)message, null);
		_type = MessageType.UserConnected;
		_sender = message.GetData();
	}
	
	/**
	 * Create a message that will send a user disconnected.
	 * @param message Message to send.
	 * @param receiver User that will receive. If null, this message will be public.
	 */
	public Message(UserDisconnected message) {
		this((BaseMessage<?>)message, null);
		_type = MessageType.UserDisconnected;
		_sender = message.GetData();
	}
	
	/**
	 * Create a message that will send a text.
	 * @param message Message to send.
	 * @param receiver User that will receive. If null, this message will be public.
	 */
	public Message(ObjectMessage<? extends Serializable> message, User receiver) {
		this((BaseMessage<?>)message, receiver);
		_type = MessageType.Object;
	}
	
	/**
	 * @return The message of this message object. It must have a cast that can be done by using GetMessageType();
	 */
	public BaseMessage<?> GetMessage() {
		return _message;
	}
	
	/**
	 * @return The type of this message to aux make cast.
	 */
	public MessageType GetMessageType() {
		return _type;
	}
	
	/**
	 * @return User that send this message.
	 */
	public User GetSender() {
		return _sender;
	}
	
	/**
	 * @return True if this message is a private one.
	 */
	public boolean IsPrivate() {
		return _receiver != null;
	}
	
	/**
	 * Set the sender of this message.
	 * @param sender User that is sending this.
	 */
	public void SetSender(User sender) {
		_sender = sender;
	}
	
	/**
	 * @return User that is supposed to receive this message. This return null if it's not a private message.
	 */
	public User GetReceiver() {
		return _receiver;
	}
	
	@Override
	public String toString() {
		if (_type == MessageType.UserConnected) {
			return _sender.toString() + " just enter.";
		}
		else if (_type == MessageType.UserDisconnected) {
			return _sender.toString() + " just left.";
		}
		else if (this.IsPrivate()){
			return _sender.toString() + " says in private to " + _receiver.toString() + ": " + _message;
		} else {
			return _sender.toString() + " says to all: " + _message;
		}
	}
}
