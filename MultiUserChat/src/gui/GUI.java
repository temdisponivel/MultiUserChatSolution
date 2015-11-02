package gui;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import service.ClientService;
import service.Message;
import service.Message.FileMessage;
import service.ServiceListener;
import service.User;

public class GUI extends JFrame implements ServiceListener {
	
	private static final long serialVersionUID = 1L;
	protected JTextField _textMessage = null;
	protected JTextArea _messageArea = null;
	protected JScrollPane _scroolPane = null;
	protected JButton _buttonSend = null;
	protected JButton _buttonConnect = null;
	protected JButton _buttonFile = null;
	protected JList<User> _listUsers = null;
	protected ClientService _service = null;
	protected String _serverAddress = "";
	protected String _serverPort = "";
	protected String _nickName = "";
	protected User _allUsers = new User("All");
	
	public GUI() {
		this.setSize(800, 600);		
		
		_textMessage = new javax.swing.JTextField();
		_scroolPane = new javax.swing.JScrollPane();
        _messageArea = new javax.swing.JTextArea();
        _buttonSend = new javax.swing.JButton();
        _buttonConnect = new javax.swing.JButton();
        _buttonFile = new javax.swing.JButton();
        _textMessage.setColumns(20);
		_messageArea.setColumns(20);
		_messageArea.setEditable(false);
		_messageArea.setRows(5);
		_scroolPane.setViewportView(_messageArea);
		
		_listUsers = new JList<User>();
		_listUsers.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		_listUsers.setLayoutOrientation(JList.VERTICAL);
		_listUsers.setVisibleRowCount(-1);
		_listUsers.setModel(new DefaultListModel<User>());
		JScrollPane listScroller = new JScrollPane(_listUsers);
		_buttonConnect.setText("Connect");
		_buttonConnect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
        		if (_service != null && _service.IsActive()) {
        			_service.Finish();
        		}
        		else {
        			Init();
        		}
            }
        });
		_buttonFile.setText("Send file");
		_buttonFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                SendFile(evt);
            }
        });
		_buttonSend.setText("Enviar");
		_buttonSend.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                SendClick(evt);
            }
        });

        this.getContentPane().setLayout(new BorderLayout(5, 5));
        JPanel panelUserChat = new JPanel();
        panelUserChat.setLayout(new BorderLayout(5, 5));
        panelUserChat.add(_scroolPane, BorderLayout.CENTER);
        panelUserChat.add(listScroller, BorderLayout.EAST);
        this.add(panelUserChat, BorderLayout.CENTER);
        JPanel panelSend = new JPanel();
        panelSend.setLayout(new BorderLayout(5, 5));
        panelSend.add(_textMessage, BorderLayout.CENTER);
        panelSend.add(_buttonSend, BorderLayout.EAST);
        this.add(panelSend, BorderLayout.SOUTH);
        add(_buttonConnect, BorderLayout.NORTH);
        add(_buttonFile, BorderLayout.WEST);
		
		this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
            	if (_service != null) {
            		_service.Finish();
            	}
                e.getWindow().dispose();
            }
        });
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		_buttonFile.setEnabled(false);
		_buttonSend.setEnabled(false);
	}
	
	public void Init() {
		try {
			this.ShowConnectDialog();
			_service = new ClientService(new User(_nickName), _serverAddress, Integer.parseInt(_serverPort), this);
			_service.Connect();
			((DefaultListModel<User>)_listUsers.getModel()).addElement(_allUsers);
			_listUsers.setSelectedIndex(0);
			_buttonConnect.setText("Disconnect");
			_buttonFile.setEnabled(true);
			_buttonSend.setEnabled(true);
		} catch (ConnectException ex) {
			JOptionPane.showMessageDialog(this, ex);
			ex.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, e);
			e.printStackTrace();
		} catch (NumberFormatException e) {
			//e.printStackTrace();
		}
	}
	
	public void ShowConnectDialog() {
		_serverAddress = (String)JOptionPane.showInputDialog(this, "Type the address of the server");
		_serverPort = (String)JOptionPane.showInputDialog(this, "Type the port of the server");
		_nickName = (String)JOptionPane.showInputDialog(this, "Type your nickname for the chat");
	}
	
	@Override
	public void OnClose() {
		((DefaultListModel<User>) _listUsers.getModel()).clear();
		_messageArea.setText("");
		_messageArea.append("You was disconnected. \n");
		_buttonConnect.setText("Connect");
		_buttonFile.setEnabled(false);
		_buttonSend.setEnabled(false);
	}

	@Override
	public void OnReceive(Message message) {
		switch (message.GetMessageType()) {
		case UserConnected:
			User user = ((Message.UserConnect)message.GetMessage()).GetData();
			((DefaultListModel<User>)_listUsers.getModel()).addElement(user);
			this.AddMessage(message);
			break;
		case UserDisconnected:
			user = ((Message.UserDisconnected)message.GetMessage()).GetData();
			((DefaultListModel<User>)_listUsers.getModel()).removeElement(user);
			this.AddMessage(message);
			break;
		case File:
			this.HandleFile(message);
			break;
		default:
			this.AddMessage(message);
			break;
		}
	}
	
	/**
	 * Handle a message that constains a file.
	 * @param message Message with the file.
	 */
	public void HandleFile(Message message) {
		int result = JOptionPane.showConfirmDialog(this, message.GetSender() + " sent you a file. Do you want to save?");
		if (result == JOptionPane.OK_OPTION) {
			JFileChooser chooser = new JFileChooser(); 
		    chooser.setCurrentDirectory(new java.io.File("."));
		    chooser.setDialogTitle("Select a directory to save a file");
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    chooser.setAcceptAllFileFilterUsed(false);
		    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
		    	File directory = chooser.getSelectedFile();
		    	FileMessage fileMessage = ((FileMessage)message.GetMessage());
		    	String name = fileMessage.name;
		    	byte[] fileReceived = fileMessage.GetData();
		    	File newFile = new File(directory.getAbsolutePath() + File.separatorChar + name);
		    	FileOutputStream out;
				try {
					out = new FileOutputStream(newFile);
					out.write(fileReceived);
					out.close();
					this.AddMessage(message);
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(this, e);
					e.printStackTrace();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, e);
					e.printStackTrace();
				}
		    }
		}
	}
	
	public void SendFile(User receiver) {
		JFileChooser chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setDialogTitle("Select a directory to save a file");
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(true);
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
	    	File fileToSend = chooser.getSelectedFile();
	    	try {
		    	FileMessage message = new FileMessage(fileToSend);
		    	Message m;
		    	_service.Send(m = new Message(message, receiver));
		    	this.AddMessage(m);
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(this, e);
				e.printStackTrace();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, e);
				e.printStackTrace();
			}
	    }
	}
	
	public void AddMessage(Message received) {
		switch (received.GetMessageType()) {
		case File:
			_messageArea.append(received.GetSender() + "send a file named: " + ((FileMessage)received.GetMessage()).name + " to " + (received.IsPrivate() ? received.GetReceiver() : " all.") + "\n" );
			break;
		default:
			_messageArea.append(received.toString() + "\n");
			break;
		}
	}
	
	public void SendClick(MouseEvent evt) {
		if (_textMessage.getText().isEmpty()) {
			return;
		}
		String message = _textMessage.getText();
		_textMessage.setText("");
		try {
			Message m;
			if (_listUsers.getSelectedValue() == _allUsers) {
				_service.Send(m = new Message(new Message.TextMessage(message), null));
			}
			else {
				_service.Send(m = new Message(new Message.TextMessage(message), _listUsers.getSelectedValue()));
			}
			this.AddMessage(m);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, e);
			e.printStackTrace();
		}
	}
	
	public void SendFile(MouseEvent evt) {
		if (_listUsers.getSelectedValue() == _allUsers) {
			this.SendFile((User)null);	
		}
		else {
			this.SendFile(_listUsers.getSelectedValue());
		}
	}
}
