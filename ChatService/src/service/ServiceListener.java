/*
 * Matheus de Almeida
 * Victor Dias
 */

package service;

/**
 * Definition of a listener for service.
 *
 */
public interface ServiceListener {
	/**
	 * Called when a socket is closed.
	 */
	void OnClose();
	/**
	 * Called when receive a message from server.
	 * @param message Message received.
	 */
	void OnReceive(Message message);
}
