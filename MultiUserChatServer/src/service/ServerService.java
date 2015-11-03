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

public class ServerService implements Runnable {
	protected Socket _socket = null;
	protected ObjectOutputStream _outStream = null;
	protected ObjectInputStream _inStream = null;
	protected ServerManager _manager = null;
	protected boolean _active = true;
	
	public ServerService(ServerManager manager, Socket client) {
		if (manager == null || client == null) {
			throw new NullPointerException();
		}
		_manager = manager;
		_socket = client;
		try {
			_outStream = new ObjectOutputStream(_socket.getOutputStream());
			_inStream = new ObjectInputStream(_socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Thread(this).start();
	}
	
	/***
	 * Send a message to server.
	 * @param message Message to send.
	 * @throws IOException If it is not possible to send.
	 */
	public void Send(Message message) throws IOException {
		try
		{
			if (_socket != null && !_socket.isClosed() && _socket.isConnected()) {
				if (_outStream != null) {
					_outStream.writeObject(message);
				}
			}
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
						if (_manager != null) {
							_manager.OnReceive(received, this);
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
			else if (_socket != null && _socket.isClosed() && _socket.isConnected()) {
				this.Finish();
			}
		}
	}
	
	protected void Disconnect() {
		try {
			if (_socket != null && !_socket.isClosed()) {
				_socket.close();
			}
			this.ClearSocketInformation();
			_manager.CloseServer(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void Finish() {
		_active = false;
		this.Disconnect();
	}
	
	/**
	 * Clear the information of the socket.
	 */
	protected void ClearSocketInformation() {
		_socket = null;
		_inStream = null;
		_outStream = null;
	}
}