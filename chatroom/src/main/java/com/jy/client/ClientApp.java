package com.jy.client;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * 
 * @author Jason.Yan
 *
 */
public class ClientApp
{
	private static String userName = "client456";
	private static String password = "abc";
	
	public static void main(String[] args)
	{
		String hostName = "127.0.0.1";
		int portNumber = 5678;
	
		//relevant to server info dialog
		JLabel hostLabel = new JLabel("Enter hostname:");
		JTextField hostField = new JTextField(hostName, 20);
		JLabel portLabel = new JLabel("Enter port number:");
		JTextField portField = new JTextField(String.valueOf(portNumber), 20);
		Object[] components1 = {hostLabel, hostField, portLabel, portField};
		
		//relevant to register dialog
		JLabel nameLabel2 = new JLabel("What username do you want?");
		JTextField nameField2 = new JTextField(userName, 20);
	    JLabel passwordLabel2 = new JLabel("What will be your password?");
	    JTextField passwordField2 = new JTextField(password, 20);
	    Object[] components2 = {nameLabel2, nameField2, passwordLabel2, passwordField2};
		
		//relevant to login dialog
		JLabel nameLabel3 = new JLabel("Enter your user name:");
		JTextField nameField3 = new JTextField(userName, 20);
	    JLabel passwordLabel3 = new JLabel("Enter your password:");
	    JTextField passwordField3 = new JTextField(password, 20);
	    Object[] components3 = {nameLabel3, nameField3, passwordLabel3, passwordField3};
		Object[] options = {"Login", "Register", "Cancel"};
		
		int reply1 = JOptionPane.showConfirmDialog(null, components1, "Server info?",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if(reply1 != JOptionPane.OK_OPTION)
		{
			System.out.println("User chose cancel or close, exiting program...");
			System.exit(0);
		}

		hostName = hostField.getText();
		portNumber = Integer.parseInt(portField.getText());
		String message = "";
		try
		(
			Socket sock = new Socket(hostName, portNumber);
			InputStream inputStream = sock.getInputStream();
			Scanner fromServer = new Scanner(inputStream);
			OutputStream outputStream = sock.getOutputStream();
			PrintWriter toServer = new PrintWriter(outputStream, true);

		)
		{
			System.out.println("Attempting to connect to socket...");
			message = "\tConnection to socket successful.";
			System.out.println(message);
			JOptionPane.showMessageDialog(null, message, "Success!",
					JOptionPane.INFORMATION_MESSAGE);
			
			outer_infinite_loop:
			while(true)
			{
				int reply3 = JOptionPane.showOptionDialog(null, components3, "Login Info?",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
						null, options, options[0]);
				if(reply3 == JOptionPane.OK_OPTION) //used as login option
				{
					userName = nameField3.getText();
					password = passwordField3.getText();
					toServer.println("login% " + userName + " " + password);
					System.out.println("Attempting to login...");
					while(fromServer.hasNext())
					{
						message = fromServer.nextLine();
						System.out.println(message);
						if(message.matches("Server: .* has joined the chatroom."))
							break outer_infinite_loop;
						if("\tInvalid username//password, try again".equals(message))
						{
							JOptionPane.showMessageDialog(null, message, "ERROR",
									JOptionPane.ERROR_MESSAGE);
							continue outer_infinite_loop;
						}
					}
				}
				else if(reply3 == JOptionPane.NO_OPTION) //used as register option
				{
					inner_infinite_loop:
					while(true)
					{
						int reply2 = JOptionPane.showConfirmDialog(null, components2,
								"Register Info?", JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						if(reply2 == JOptionPane.OK_OPTION)
						{
							userName = nameField2.getText();
							password = passwordField2.getText();
							toServer.println("register% " + userName + " " + password);
							System.out.println("Attempting to register...");
							while(fromServer.hasNext())
							{
								message = fromServer.nextLine();
								System.out.println(message);
								if("\tRegistry entry created!".equals(message))
								{
									JOptionPane.showMessageDialog(null, message, "Success!",
											JOptionPane.INFORMATION_MESSAGE);
									break inner_infinite_loop;
								}
								else if("\tDuplicate username found, try another".equals(message))
								{
									JOptionPane.showMessageDialog(null, message, "ERROR",
											JOptionPane.ERROR_MESSAGE);
									continue inner_infinite_loop;
								}
							}
						}
						else //if reply2 was cancel or close
							break; //break inner_infinite_loop;
					}
				}
				else //if reply3 was cancel or close
				{
					System.out.println("User chose cancel or close, exiting program...");
					System.exit(0);
				}
			}
		
			EventQueue.invokeLater(new Runnable() 
			{
				public void run()
				{
					new ChatFrame(sock, userName, toServer);	
				}
			});
			
			while(fromServer.hasNext())
			{
				String lineOfText = fromServer.nextLine();
				ChatFrame.printlnToTextArea(lineOfText);
			}
		}
		catch(IOException e)
		{
			message = "Failed to connect to " + hostName + ":" +
					portNumber + ", try again";
			System.err.println(message);
			JOptionPane.showMessageDialog(null, message, "ERROR",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}