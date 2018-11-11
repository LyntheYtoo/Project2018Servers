package node.network.socketHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.NodeControlCore;
import node.log.LogWriter;
import node.network.NetworkManager;
import node.network.NetworkUtil;

public class BroadcastSender
{
	public static final Logger logger = LogWriter.createLogger(BroadcastSender.class, "broadcast");
	
	private DatagramSocket socket;
	private int port;

	private boolean isWork;
	
	public BroadcastSender()
	{
		this.socket = null;
		this.isWork = false;
	}
	
	public void start()
	{
		if(this.isWork) return;
		this.isWork = true;
		
		logger.log(Level.INFO, "브로드캐스트 소켓 핸들러 로드");

		try
		{
			this.port = Integer.parseInt(NodeControlCore.getProp(NetworkManager.PROP_INFOBROADCAST_PORT));

			this.socket = new DatagramSocket();
			this.socket.setBroadcast(true);
		}
		catch (IllegalStateException | IOException e)
		{
			logger.log(Level.SEVERE, "소켓 열기 실패", e);
			return;
		}

	}
	
	public void stop()
	{
		if(!this.isWork) return;
		this.isWork = false;
		
		this.socket.close();
	}
	
	

}