package service;

import java.io.Serializable;
import java.util.Random;

/**
 * Class that represents a user in chat.
 * @author temdi
 *
 */
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * Name of the user.
	 */
	public String name;
	private int _hash = 0;
	
	public User(String name) {
		this.name = name;
		_hash = ~~new Random().nextInt() << 3;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(obj.toString()) && this.hashCode() == obj.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return _hash;
	}
}
