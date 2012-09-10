package com.jy.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * 
 * @author Jason.Yan
 *
 */
public class ChatFrame extends JFrame
{
	private static final long serialVersionUID = -112360202711439056L;
	private JTextField textField = new JTextField("Put text here.", 50);
	private static JTextArea textArea = new JTextArea("Output.\n", 20, 50);
	private int localPort;
	private Socket sock;
	private String userName = "guest";
	private PrintWriter toServer;
	private JLabel userNameLabel;
	private JLabel localPortNumberLabel;
	
	public ChatFrame(Socket sockParameter, String userNameParameter, 
			PrintWriter toServerParameter)
	{
		sock = sockParameter;
		userName = userNameParameter;
		toServer = toServerParameter;
		setVisible(true);
		textArea.setEditable(false);
		localPort = sock.getLocalPort();

		addWindowListener(new WindowAdapter()
		{
			@Override //override windowClosing in class WindowAdapter
			public void windowClosing(WindowEvent e)
			{
				toServer.println("logout%");
				try
				{
					sock.close();
				}
				catch(IOException ioe) {}
				finally
				{
					System.exit(0);
				}
			}
		});
		
		//creates and handles the east panel components
		JPanel eastPanel = new JPanel(new GridLayout(3, 1));
		JPanel userPanel = new JPanel(new BorderLayout());
		JPanel localPortPanel = new JPanel(new BorderLayout());
		JLabel userLabel = new JLabel("Username: ");
		JLabel localPortLabel = new JLabel("Port: ");
		userNameLabel = new JLabel(userName);
		localPortNumberLabel = new JLabel(String.valueOf(localPort));
		add(new JScrollPane(textArea), BorderLayout.CENTER);
		add(textField, BorderLayout.SOUTH);
		add(eastPanel, BorderLayout.EAST);
		eastPanel.add(userPanel);
		eastPanel.add(localPortPanel);
		userPanel.add(userLabel, BorderLayout.WEST);
		userPanel.add(userNameLabel, BorderLayout.EAST);
		localPortPanel.add(localPortLabel, BorderLayout.WEST);
		localPortPanel.add(localPortNumberLabel, BorderLayout.EAST);
		surroundWithBorder(userPanel, Color.RED, 2, "Username info");
		surroundWithBorder(localPortPanel, Color.BLUE, 2, "Local Port info");
		pack();

		//listener for textfield, writes to socket
		textField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String lineOfText = userName + ": " + textField.getText();
				//printlnToTextArea(lineOfText);
				textField.setText("");
				toServer.println(lineOfText);
			}
		});
	}
	
	//self-explanatory methods
	public static void printlnToTextArea(String text)
	{
		textArea.append(text + "\n");
	}
	public void setUserName(String userName)
	{
		this.userName = userName;
		userNameLabel.setText(userName);
	}
	private void surroundWithBorder(
			JComponent component, Color borderColor, int thickness, String title)
	{
		component.setBorder( //one lengthy parameter
			BorderFactory.createTitledBorder( //one lengthy parameter
				BorderFactory.createLineBorder(borderColor, thickness), title));
	}
}