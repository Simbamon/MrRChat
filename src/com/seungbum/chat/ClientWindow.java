package com.seungbum.chat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.border.LineBorder;
import javax.swing.JList;

public class ClientWindow extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtMessage;
	private JTextArea history;
	private DefaultCaret caret;
	private Thread run, listen;
	private Client client;
	private Date date;
	
	private boolean running = false;
	private JLabel lblName;
	private JList list;

	
	public ClientWindow(String name, String address, int port) {
		setForeground(Color.BLACK);
		setIconImage(Toolkit.getDefaultToolkit().getImage("D:\\eclipse-workspace\\Chat\\resources\\chat.png"));
		setTitle("Terminal - root@" + name + ":~");
		client = new Client(name, address, port);
		boolean connect = client.openConnection(address);
		if (!connect) {
			System.out.println("Connection Failed");
			console("Connection Failed");
		}
		
		SimpleDateFormat formatDate = new SimpleDateFormat("EEEEE MMMMM yyyy HH:mm:ss"); 
		date = new Date();
		String newDate = formatDate.format(date);
		
		
		createWindow();
		console("Last Login: " + newDate);
		console("User's port: " + port);
		console("Receiving connection from " + address);
		console("");
		String connection = "/c/" + name + "/e/";
		client.send(connection.getBytes());
		running = true;
		run = new Thread(this, "Running");
		run.start();
		
	}

	private void createWindow() {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000, 600);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{130, 2000};
		gbl_contentPane.rowHeights = new int[]{45};
		gbl_contentPane.columnWeights = new double[]{0.0, 1.0};
		gbl_contentPane.rowWeights = new double[]{1.0};
		contentPane.setLayout(gbl_contentPane);
		
		history = new JTextArea();
		history.setBackground(Color.BLACK);
		history.setForeground(Color.WHITE);
		history.setEditable(false);
		history.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		JScrollPane scroll = new JScrollPane(history);
		; 
		caret = (DefaultCaret) history.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		list = new JList();
		list.setFont(new Font("Tahoma", Font.PLAIN, 12));
		list.setForeground(Color.WHITE);
		list.setBackground(Color.BLACK);
		GridBagConstraints gbc_list_1 = new GridBagConstraints();
		gbc_list_1.insets = new Insets(0, 0, 5, 5);
		gbc_list_1.fill = GridBagConstraints.BOTH;
		gbc_list_1.gridx = 0;
		gbc_list_1.gridy = 0;
		JScrollPane p = new JScrollPane();
		p.setViewportView(list);
		p.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 
		contentPane.add(p, gbc_list_1);
		GridBagConstraints scrollConstraints = new GridBagConstraints();
		scrollConstraints.insets = new Insets(0, 0, 5, 0);
		scrollConstraints.fill = GridBagConstraints.BOTH;
		scrollConstraints.gridx = 1;
		scrollConstraints.gridy = 0;
		scrollConstraints.gridwidth = 0;
		scrollConstraints.insets = new Insets (1, 0, 5, 0);
		contentPane.add(scroll, scrollConstraints);
		
		
		txtMessage = new JTextField();
		txtMessage.setFont(new Font("Arial", Font.PLAIN, 12));
		txtMessage.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		txtMessage.setForeground(Color.WHITE);
		txtMessage.setBackground(Color.BLACK);
		txtMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					send(txtMessage.getText(), true);
				}
			}
		});
		
		lblName = new JLabel("freenode (IRC)");
		lblName.setFont(new Font("Arial", Font.PLAIN, 12));
		lblName.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(2, 0, 0, 5);
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 2;
		contentPane.add(lblName, gbc_lblName);
		GridBagConstraints gbc_txtMessage = new GridBagConstraints();
		gbc_txtMessage.insets = new Insets(0, 0, 0, 0);
		gbc_txtMessage.gridwidth = 2;
		gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMessage.gridx = 0;
		gbc_txtMessage.gridy = 1;
		contentPane.add(txtMessage, gbc_txtMessage);
		txtMessage.setColumns(10);
		
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				String disconnect = "/d/" + client.getID() + "/e/";
				send(disconnect, false);
				client.close();
				running = false;
			}
		});
		setVisible(true);
		
		txtMessage.requestFocusInWindow();
	}

	public void run() {
		listen();
	}
	
	public void send(String message, boolean text) {
		if(message.equals("") || message.equals(null) ){
			return;
		}
		SimpleDateFormat formatedDate = new SimpleDateFormat("HH:mm:ss");
		date = new Date();
		String newDate = formatedDate.format(date);
		if(text) {
			message = "[" + newDate + "] == - <" + client.getName() + "> " + message;
			message = "/m/" + message;
			txtMessage.setText("");
		}
		client.send(message.getBytes());
		txtMessage.requestFocusInWindow();
	}
	
	public void listen() {
		listen = new Thread("Listen") {
			public void run() {
				while(running) {
					String message = client.receive();
					 if (message.startsWith("/c/")) {
						client.setID(Integer.parseInt(message.split("/c/|/e/")[1]));
						console("Connection succesful.");
						console("Your assigned ID: " + client.getID());
						console("");
						console("   [   <=====================================================>   ]");
						console("");
					 }
					 else if (message.startsWith("/m/")) {
						 String text = message.substring(3);
						 text = text.split("/e/")[0];
						 console(text);
					 }
					 else if (message.startsWith("/i/")) {
						 String text = "/i/" + client.getID() + "/e/";
						 send(text, false);
					 }
					 else if (message.startsWith("/u/")) {
						 String[] u = message.split("/u/|/n/|/e/");
						 update(Arrays.copyOfRange(u, 1, u.length - 1));
					 }
				}
				
			}
			
		};
		listen.start();
	}
	
	public void update(String[] users) {
		list.setListData(users);
	}
	
	public void console(String message) {
		history.append(message + "\n\r");
	}
	
}
