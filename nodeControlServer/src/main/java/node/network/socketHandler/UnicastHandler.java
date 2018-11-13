package node.network.socketHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.log.LogWriter;
import node.network.NetworkUtil;
import node.network.packet.PacketUtil;

public class UnicastHandler
{
	public static final Logger logger = LogWriter.createLogger(UnicastHandler.class, "unicast");
	
	private InetAddress inetAddress;
	private DatagramSocket socket;

	private boolean isWork;
	
	private Thread worker;
	

	private BiConsumer<InetAddress, byte[]> receiveCallback;
	
	public UnicastHandler(BiConsumer<InetAddress, byte[]> receiveCallback)
	{
		this.receiveCallback = receiveCallback;
		
		this.socket = null;
		this.isWork = false;
		
		this.worker = null;
	}
	
	public void start(InetAddress inetAddress)
	{
		if(this.isWork) return;
		this.isWork = true;
		
		logger.log(Level.INFO, "유니캐스트 송수신기 로드");
		
		try
		{
			this.socket = new DatagramSocket(null);
			this.socket.bind(new InetSocketAddress(this.inetAddress, NetworkUtil.unicastPort()));
		}
		catch (SocketException e)
		{
			logger.log(Level.SEVERE, "소캣 열기 오류", e);
		}
		
		this.worker = new Thread(this::run);
		this.worker.start();
	}
	
	public synchronized void sendMessage(byte[] data, InetAddress addr)
	{
		if(!this.isWork)
		{
			logger.log(Level.WARNING, "소켓 닫힘");
			return;
		}
		
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setAddress(addr);
		packet.setPort(NetworkUtil.unicastPort());
		try
		{
			this.socket.send(packet);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "패킷 전송 실패", e);
		}
	}
	
	public void run()
	{
		byte[] packetBuffer = new byte[PacketUtil.HEADER_SIZE + PacketUtil.MAX_SIZE_KEY + PacketUtil.MAX_SIZE_DATA];
		DatagramPacket dgramPacket;
		
		while(this.isWork)
		{
			dgramPacket = new DatagramPacket(packetBuffer, packetBuffer.length);

			try
			{
				this.socket.receive(dgramPacket);
				byte[] copyBuf = Arrays.copyOf(packetBuffer, dgramPacket.getLength());
				this.receiveCallback.accept(dgramPacket.getAddress(), copyBuf);
			}
			catch (IOException e)
			{
				continue;
			}
		}
		logger.log(Level.INFO, "유니캐스트 송수신기 종료");
	}

	public void stop()
	{
		if(!this.isWork) return;
		this.isWork = false;
		
		if(this.socket != null && !this.socket.isClosed())
		{
			this.socket.close();
		}
		this.worker.interrupt();
	}
}