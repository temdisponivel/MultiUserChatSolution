/*
 * Matheus de Almeida
 * Victor Dias
 */

package service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public abstract class BaseMessage<T extends Serializable> implements Serializable  {
	private static final long serialVersionUID = 1L;
	protected byte[] _encodedData = null;
	
	protected void SetData(T data) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream objOut;
		try {
			objOut = new ObjectOutputStream(out);
			objOut.writeObject(data);
			_encodedData = Base64.getEncoder().encode(out.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Return the data of this message.
	 */
	@SuppressWarnings("unchecked")
	public T GetData() {
		ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(_encodedData));
		try {
			ObjectInputStream objIn = new ObjectInputStream(in);
			return (T) objIn.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return this.GetData().toString();
	}
}