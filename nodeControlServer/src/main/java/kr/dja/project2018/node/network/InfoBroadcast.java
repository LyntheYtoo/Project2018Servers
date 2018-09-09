package kr.dja.project2018.node.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kr.dja.project2018.node.IServiceModule;
import kr.dja.project2018.node.NodeControlCore;
import kr.dja.project2018.node.db.DB_Handler;

public class InfoBroadcast implements Runnable, IServiceModule
{
	public static final String PROP_INFOBROADCAST_PORT = "infoBroadcastPort";
	public static final Logger broadcastLogger = NodeControlCore.createLogger(DB_Handler.class.getName().toLowerCase(), "broadcast");
	private static InetAddress broadcastIA;
	
	private Thread broadcastThread = null;
	private boolean isRun = false;
	private DatagramSocket dgramSocket = null;
	private String infoString;
	private DatagramPacket packet;
	
	static
	{
		try
		{
			broadcastIA = InetAddress.getByName("255.255.255.255");
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}
	
	public InfoBroadcast()
	{

	}

	@Override
	public void run()
	{
		broadcastLogger.log(Level.INFO, "��� �˸� ����");
		while(this.isRun)
		{
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {}
			try
			{
				this.dgramSocket.send(packet);
			}
			catch (IOException e)
			{
				broadcastLogger.log(Level.SEVERE, "��Ŷ ���� ����");
			}
		}
	}
	
	private void broadCastInfo()
	{
		byte[] infoMessage = this.infoString.getBytes();
		
		
	}
	
	public boolean start()
	{
		if(this.isRun) this.stop();
		this.isRun = true;
		
		try
		{
			this.dgramSocket = new DatagramSocket();
		}
		catch (SocketException e)
		{
			broadcastLogger.log(Level.SEVERE, "��Ĺ ���� ����", e);
			return false;
		}
		
		byte[] infoMessage = this.infoString.getBytes();
		int port = Integer.valueOf(NodeControlCore.getProp(PROP_INFOBROADCAST_PORT));
		this.packet = new DatagramPacket(infoMessage, infoMessage.length, broadcastIA, port);
		
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return true;
	}
	
	public void stop()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		this.broadcastThread.interrupt();
	}
}