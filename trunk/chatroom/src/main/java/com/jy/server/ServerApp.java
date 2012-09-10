package com.jy.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author Jason.Yan
 *
 */
public class ServerApp
{
	//fields of class ServerTest
	private static List<Socket> clients;
	private static BlockingQueue<String> buffer;
	private static Map<String, String> registry;
	private static final String registryFileName = "registry.ser";
	
	public static void main(String[] args)
	{
		final int portNumber;
		final int BUFFERSIZE = 10;
		clients = new CopyOnWriteArrayList<Socket>();
		buffer = new ArrayBlockingQueue<String>(BUFFERSIZE);
		
		if(!new File(registryFileName).exists())
		{
			//if registry file not found, create new registry
			registry = new ConcurrentHashMap<>();
			updateRegistryFile();
		}
		else
		{
			try
			(
				FileInputStream fIn = new FileInputStream(registryFileName);
				ObjectInputStream oIn = new ObjectInputStream(fIn);
			)
			{
				registry = (Map<String, String>) oIn.readObject();
			}
			catch(ClassNotFoundException | IOException e)
			{
				e.printStackTrace();
			}
		}
		
		//port number can be from command line
		portNumber = (args.length >= 1 ? Integer.parseInt(args[0]) : 5678);
		
		new Server(portNumber, buffer, clients, registry).start();
	}
		private static void updateRegistryFile()
		{
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
		}
} //end of class ServerTest