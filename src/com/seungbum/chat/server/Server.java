package com.seungbum.chat.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable {
	
	
	private List<ServerClient> clients = new ArrayList<ServerClient>();
	private List<Integer> clientResponse = new ArrayList<Integer>();
	
	private DatagramSocket socket;
	private int port;
	private boolean running = false;
	private Thread run, manage, send, receive;
	private boolean raw = false;
	private Date date;
	
	private final int MAX_ATTEMPTS = 5;	
	
	
	
	public Server(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		run = new Thread(this, "Server");
		run.start();
	}

	
	public void run() {
		SimpleDateFormat formatedDate = new SimpleDateFormat("HH:mm:ss");
		date = new Date();
		String newDate = formatedDate.format(date);
		running = true;
		System.out.println("Server started on port " + port);
		manageClients();
		receive();
		Scanner scanner = new Scanner(System.in);
		while (running) {
			String text = scanner.nextLine();
			if (!text.startsWith("/")) {
				sendToAll("/m/[" + newDate + "] == - <ADMIN> " + text + "/e/");
				continue;
			}
			text = text.substring(1);
			if (text.startsWith("raw")) {
				raw = !raw;
			}
			else if (text.equals("clients")){
				System.out.println("Client List: ");
				System.out.println("[=====================================]");
				for (int i = 0; i < clients.size(); i++) {
					ServerClient c = clients.get(i);
					System.out.println(c.name.trim() + "(" + c.getID() + "): " + c.address.toString() + ":" + c.port);
				}
				System.out.println("[=====================================]");
			}
			else if (text.startsWith("kick")) {
				String name = text.split(" ")[1];
				int id = -1;
				boolean number = true;
				try {
					id = Integer.parseInt(name);
				} catch (NumberFormatException e) {
					number = false;
				}
				if (number) {
					boolean exists = false;
					for (int i = 0; i < clients.size(); i++) {
						if (clients.get(i).getID() == id) {
							exists = true;
							break;
						}
					}
					if (exists) {
						for (int i = 0; i < clients.size(); i++) {
						ServerClient c = clients.get(i);
						sendToAll("/m/" + "<SYSTEM - ADMIN> " + c.name + "(ID: " + c.getID() + ")" + " has been kicked." + "/e/");
						disconnect(id, true);
						}
					}
					else {
						System.out.println("Client " + id + " doesn't exist! Check ID number.");
					}
				} else {
					for (int i = 0; i < clients.size(); i++) {
						ServerClient c = clients.get(i);
						if (name.equals(c.name)) {
							sendToAll("/m/" + "<SYSTEM - ADMIN> " + c.name + "(ID: " + c.getID() + ")" + " has been kicked." + "/e/");
							disconnect(c.getID(), true);
							
							break;
						}
					}
				}
			}
			else if (text.equals("quit")){
				quit();
			}
			else {
				System.out.println("Unknown command.");
			}
			
		}
		scanner.close();
	}
	

	
	private void manageClients() { 
		manage = new Thread("Manage") {
			public void run() {
				while (running) {
					sendToAll("/i/server");
					sendStatus();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for (int i = 0; i < clients.size(); i++) {
						ServerClient c = clients.get(i);
						if(!clientResponse.contains(c.getID())) {
							if(c.attempt >= MAX_ATTEMPTS) {
								disconnect(c.getID(), false);
							} else {
								c.attempt++;
							}
						} else {
							clientResponse.remove(new Integer(c.getID()));
							c.attempt = 0;
						}
					}
				}
			}
		};
		manage.start();
	}
	
	private void sendStatus() {
		if (clients.size() <= 0) {
			return;
		}
		String users = "/u/";
		for (int i = 0; i < clients.size() - 1; i++) {
			users += clients.get(i).name + "/n/";
		}
		users += clients.get(clients.size() - 1).name + "/e/";
		sendToAll(users);
	}
	
	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while (running) {
					byte [] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (SocketException s) {
						System.out.println("Server has shut down.");
					} catch (IOException e) {
						e.printStackTrace();
					}
					process(packet);
				}
			}
		};
		receive.start();
	}
	
	private void sendToAll(String message) {
		if(message.startsWith("/m/")) {
			String text = message.substring(3);
			text = text.split("/e/")[0];
			System.out.println(message);
		}
		
		for (int i = 0; i < clients.size(); i++) {
			ServerClient client = clients.get(i);
			send(message.getBytes(), client.address, client.port);
		}
	}
	
	private void send(final byte[] data, final InetAddress address, final int port ) {
		send = new Thread ("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}
	
	private void send (String message, InetAddress address, int port) {
		message += "/e/";
		send(message.getBytes(), address, port);
	}
	
	private void process(DatagramPacket packet) {
		String string = new String (packet.getData());
		if (raw) {
			System.out.println(string);
		}
		if(string.startsWith("/c/")) {
			SimpleDateFormat formatedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = new Date();
			String newDate = formatedDate.format(date);
			int id = UniqueIdentifier.getIdentifier();
			String name =  string.split("/c/|/e/")[1];
			System.out.println(name + "(" + id + ") connected");
			clients.add(new ServerClient(name, packet.getAddress(), packet.getPort(), id));
			String ID = "/c/" + id;
			send(ID, packet.getAddress(), packet.getPort());
			sendToAll("/m/" + "<SYSTEM> " + name + "(ID: " + id + ")" +  " has joined. (" + newDate +")"+ "/e/");
		}
		else if(string.startsWith("/m/")) {
			sendToAll(string);
		}
		else if(string.startsWith("/d/")) {
			String id = string.split("/d/|/e/")[1];
			disconnect(Integer.parseInt(id), true);
		}
		else if(string.startsWith("/i/")) {
			clientResponse.add(Integer.parseInt(string.split("/i/|/e/")[1]));
		}
		else {
			System.out.println(string);
		}
	}
	
	private void quit () {
		for (int i = 0; i < clients.size(); i++) {
			disconnect(clients.get(i).getID(), true);
		}
		running = false;
		socket.close();
	}
	private void disconnect(int id, boolean status) {
		SimpleDateFormat formatedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		date = new Date();
		String newDate = formatedDate.format(date);
		ServerClient c = null;
		boolean existed = false;
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).getID() == id) {
				c = clients.get(i);
				clients.remove(i);
				existed = true;
				break;
			}
		}
		if (!existed) {
			return;
		}
		if (status) {
			System.out.println("Client " + c.name.trim() + "(" + c.getID() + "): " + c.address.toString() + ":" + c.port + " is disconnected.");
			sendToAll("/m/" + "<SYSTEM> " + c.name + "(" + c.getID() + ")" +  " left the room. (" + newDate +")"+ "/e/");
		} else {
			System.out.println("Client " + c.name.trim() + "(" + c.getID() + "): " + c.address.toString() + ":" + c.port + " is timed out.");
			sendToAll("/m/" + "<SYSTEM> " + c.name + "(" + c.getID() + ")" +  " timed out. (" + newDate +")"+ "/e/");
		}

	}
}
