/*
 * Matheus de Almeida
 * Victor Dias
 */

package service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ClientService implements Runnable {
	public Socket _socket = null;
	protected ServiceListener _listener = null;
	protected String _serverAddress = null;
	protected int _serverPort = 0;
	protected ObjectInputStream _inStream = null;
	protected ObjectOutputStream _outStream = null;
	protected User _user = null;
	protected boolean _active = false;
	
	public ClientService(User user, String serveraddress, int port, ServiceListener listener) {
		_serverAddress = serveraddress;
		_serverPort = port;
		_listener = listener;
		_user = user;
	}
	
	/**
	 * Connnect to server through the address and port in the constructor of this class.
	 * @throws IOException In case it is not possible to connect.
	 */
	public void Connect() throws IOException, SocketException {
		_socket = new Socket(_serverAddress, _serverPort);
		_outStream = new ObjectOutputStream(_socket.getOutputStream());
		_inStream = new ObjectInputStream(_socket.getInputStream());
		_active = true;
		this.Send(new Message(new Message.UserConnect(_user)));
		new Thread(this).start();
	}
	
	/**
	 * Disconnect this service of the server, if is connected. If not, nothing happens.
	 * @throws IOException In case it is not possible to disconnect.
	 */
	public void Disconnect() throws IOException {
		if (_active) {
			this.Send(new Message(new Message.UserDisconnected(_user)));
			_socket.close();
		}
		this.ClearSocketInformation();
		_listener.OnClose();
	}
	
	/***
	 * Send a message to server.
	 * @param message Message to send.
	 * @throws IOException If it is not possible to send.
	 */
	public void Send(Message message) throws IOException, SocketException {
		try
		{
			message.SetSender(_user);
			if (_active) {
				if (_outStream != null) {
					_outStream.writeObject(message);
				}
			}
		}
		catch (SocketException ex) {
			System.out.println(ex);
			this.Finish();
			throw ex;
		}
		catch (IOException ex)
		{
			System.out.println(ex);
			throw ex;
		}
	}
	
	public void run() {
		while (_active) {
			if (_socket != null && !_socket.isClosed() && _socket.isConnected() && _socket.isBound()) {
				try {
					if (_inStream != null) {
						Message received = (Message) _inStream.readObject();
						if (_listener != null) {
							_listener.OnReceive(received);
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SocketException e) {
					this.Finish();
				} catch (IOException e) {
					this.Finish();
				}
			}
			else {
				this.Finish();
			}
		}
	}
	
	public void Finish() {
		try {
			this.Disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		_active = false;
	}
	
	/**
	 * @return The socket current in use by this service.
	 */
	public Socket getSocket()
	{
		return _socket;
	}
	
	/**
	 * Clear the information of the socket.
	 */
	protected void ClearSocketInformation() {
		_socket = null;
		_inStream = null;
		_outStream = null;
	}
	
	public boolean IsActive() {
		return _active;
	}
}