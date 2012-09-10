package com.jy.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

/**
 * 
 * @author Jason.Yan
 *
 */
public class Producer implements Runnable{

	private Socket socket;
	private BlockingQueue<String> buffer;
	
	private String userName;
	private String password;
	private Map<String, String> registry;
	private Scanner scanLine;
	private List<Socket> clients;
	
	public Producer(Map<String, String> registry, Socket socket, 
			BlockingQueue<String> buffer, List<Socket> clients)
	{
		this.registry = registry;
		this.socket = socket;
		this.buffer = buffer;
		this.clients = clients;
	}
	
	@Override
	public void run()
	{
		try
		(
			InputStream inputStream = socket.getInputStream();
			Scanner fromClient = new Scanner(inputStream);
			OutputStream outputStream = socket.getOutputStream();
			PrintWriter toClient = new PrintWriter(outputStream, true);
		)
		{
			while(fromClient.hasNext())
			{
				String lineOfText = fromClient.nextLine();
				scanLine = new Scanner(lineOfText);
				String firstWord = scanLine.next();
				String message; //the message sent to client(s)
				
				if("register%".equals(firstWord)) //registering
				{
					userName = scanLine.next();
					if(scanLine.hasNext()) //if there is a password
						password = scanLine.next();
					if(!registry.containsKey(userName))
					{
						try
						{
							buffer.put(lineOfText);
						}
						catch(InterruptedException e)
						{
							socket.close();
						}
						message = "\tRegistry entry created!";
					}
					else //if duplicate userName
						message = "\tDuplicate username found, try another";
					System.out.println(message);
					toClient.println(message);
				}
				else if("login%".equals(firstWord)) //login attempt
				{
					userName = scanLine.next();
					if(scanLine.hasNext()) //if there is a password
						password = scanLine.next();
					if(registry.containsKey(userName) && 
							registry.get(userName).equals(password))
					{
						message = "Server: " + userName + " has joined the chatroom.";
						try
						{
							buffer.put(message);
						}
						catch(InterruptedException e)
						{
							socket.close();
						}
					}
					else //invalid login info
						message = "\tInvalid username//password, try again";
					System.out.println(message);
					toClient.println(message);
				}
				else if("logout%".equals(firstWord))
				{
					message = "Server: " + userName + " has left the chatroom.";
					try
					{
						buffer.put(message);
						socket.close();
						clients.remove(socket);
					}
					catch (IOException | InterruptedException e)
					{
						e.printStackTrace();
					}
					System.out.println(message);
				}
				else
					try
					{
						buffer.put(lineOfText);
					}
					catch(InterruptedException e)
					{
						socket.close();
					}
			} //end of while loop
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
