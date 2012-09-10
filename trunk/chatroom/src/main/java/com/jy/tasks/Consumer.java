package com.jy.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

/**
 * 
 * @author Jason.Yan
 *
 */
public class Consumer implements Runnable
{
	private final Map<String, String> registry;
	private final BlockingQueue<String> buffer;
	private final List<Socket> clients;
	private final String registryFileName;
	private Scanner scanLine;
	
	public Consumer(Map<String, String> registry, BlockingQueue<String> buffer, 
			List<Socket> clients, String registryFileName)
	{
		this.registry = registry;
		this.buffer = buffer;
		this.clients = clients;
		this.registryFileName = registryFileName;
	}
	
	@Override
	public void run()
	{
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		File file = new File(dateFormat.format(date) + ".txt");
		try
		(
			PrintWriter toLog = new PrintWriter(file);
		)
		{
			while(true)
			{
				String lineOfText = "";
				try
				{
					lineOfText = buffer.take();
					scanLine = new Scanner(lineOfText);
					String firstWord = scanLine.next();
					String userName = "";
					String password = "";
					if("register%".equals(firstWord)) //registering
					{
						userName = scanLine.next();
						if(scanLine.hasNext()) //if there is a password
							password = scanLine.next();
						registry.put(userName, password);
						
						try
						(
							FileOutputStream fos = new FileOutputStream(registryFileName);
							ObjectOutputStream oos = new ObjectOutputStream(fos);
						)
						{
							oos.writeObject(registry);
						}
						catch(IOException e)
						{
							e.printStackTrace();
						}
						
						continue; //continue while loop
					}
					for(Socket socket : clients)
					{
						PrintWriter toClient = new PrintWriter(
								socket.getOutputStream(), true);
						toClient.println(lineOfText);
					}
				}
				catch(InterruptedException | IOException e)
				{
					e.printStackTrace();
				}
				//print to log file
				toLog.println(lineOfText);
				toLog.flush();
			}
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
