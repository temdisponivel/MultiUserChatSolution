package gui;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import service.ClientService;
import service.Message;
import service.ServiceListener;
import service.User;

public class GUI extends JFrame implements ServiceListener {
	
	private static final long serialVersionUID = 1L;
	protected JTextField _textMessage = null;
	protected JTextArea _messageArea = null;
	protected JScrollPane _scroolPane = null;
	protected JButton _buttonSend = null;
	protected ClientService _service = null;
	
	public GUI() {
		this.setSize(800, 600);		
		
		_textMessage = new javax.swing.JTextField();
		_scroolPane = new javax.swing.JScrollPane();
        _messageArea = new javax.swing.JTextArea();
        _buttonSend = new javax.swing.JButton();
		
		_messageArea.setColumns(20);
		_messageArea.setEditable(false);
		_messageArea.setRows(5);
		_scroolPane.setViewportView(_messageArea);

		_buttonSend.setText("Enviar");
		_buttonSend.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                SendClick(evt);
            }
        });

        this.getContentPane().setLayout(new BorderLayout(5, 5));
		add(_scroolPane, BorderLayout.CENTER); 
		add(_textMessage, BorderLayout.SOUTH); 
		add(_buttonSend, BorderLayout.EAST); 
		add(_buttonSend, BorderLayout.NORTH);
		
		this.setVisible(true);
	}
	
	public void Init() {
		try {
			_service = new ClientService(new User("temdisponivel"), "127.0.0.1", 5597, this);
			_service.Connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void OnClose() {
		 
	}

	@Override
	public void OnReceive(Message message) {
		_messageArea.append(message.toString() + "\n");
	}
	
	public void SendClick(MouseEvent evt) {
		if (_textMessage.getText().isEmpty()) {
			return;
		}
		String message = _textMessage.getText();
		_textMessage.setText("");
		try {
			_service.Send(new Message(new Message.TextMessage(message), null));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
