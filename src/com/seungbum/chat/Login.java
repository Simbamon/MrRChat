package com.seungbum.chat;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;

public class Login extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtName;
	private JTextField txtAddress;
	private JTextField txtPort;

	public Login() {
		setIconImage(Toolkit.getDefaultToolkit().getImage("D:\\eclipse-workspace\\Chat\\resources\\chat.png"));
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		setTitle("Chat Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(250,300);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtName = new JTextField();
		txtName.setBounds(32, 40, 170, 25);
		contentPane.add(txtName);
		txtName.setColumns(10);
		
		JLabel lblUserName = new JLabel("User Name:");
		lblUserName.setBounds(84, 22, 66, 16);
		contentPane.add(lblUserName);
		
		txtAddress = new JTextField();
		txtAddress.setText("127.0.0.1");
		txtAddress.setBounds(32, 93, 170, 25);
		contentPane.add(txtAddress);
		txtAddress.setColumns(10);
		
		JLabel lblIpAddress = new JLabel("IP Address:");
		lblIpAddress.setBounds(84, 77, 66, 16);
		contentPane.add(lblIpAddress);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(100, 130, 34, 16);
		contentPane.add(lblPort);
		
		txtPort = new JTextField();
		txtPort.setText("8192");
		txtPort.setColumns(10);
		txtPort.setBounds(32, 145, 170, 25);
		contentPane.add(txtPort);
		
		JButton btnNewButton = new JButton("Login");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = txtName.getText();
				String address = txtAddress.getText();
				int port = Integer.parseInt(txtPort.getText());
				login(name, address, port);
				
			}
		});
		btnNewButton.setBounds(68, 199, 98, 26);
		contentPane.add(btnNewButton);
	}
	
	private void login(String name, String address, int port) {
		dispose();
		new ClientWindow(name, address, port);
		
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
