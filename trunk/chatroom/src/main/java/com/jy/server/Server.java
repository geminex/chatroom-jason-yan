package com.jy.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jy.tasks.Consumer;
import com.jy.tasks.Producer;

/**
 * 
 * @author Jason.Yan
 *
 */
public class Server
{
	private List<Socket> clients;
	private BlockingQueue<String> buffer;
	private Map<String, String> registry;
	private String registryFileName = "registry.ser";
	
	private final int portNumber;
	
	private ExecutorService producers = Executors.newCachedThreadPool();
	private ExecutorService consumer = Executors.newSingleThreadScheduledExecutor();
	
	public Server(int portNumber, BlockingQueue<String> buffer, List<Socket> clients, 
			Map<String, String> registry)
	{
		this.portNumber = portNumber;
		this.buffer = buffer;
		this.clients = clients;
		this.registry = registry;
	}
	
	public void start() {
		
		consumer.execute(new Consumer(registry, buffer, clients, registryFileName));
		try 
		(
			ServerSocket servSock = new ServerSocket(portNumber);
		)
		{
			while(true)
			{
				try //responsibility of closing of sockets is passed to the Producer class
				{
					System.out.println("Listening on port " + portNumber + "...");
					Socket socket = servSock.accept();
					System.out.println("\t(A client has connected)");
					clients.add(socket);
					
					//one producer thread per iteration of while loop
					producers.execute(new Producer(registry, socket, buffer, clients));
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (IOException e) {
			System.err.println("ServerSocket could not be created, exiting...");
			System.exit(1);
		}
	}
}
