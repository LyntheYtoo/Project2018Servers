package node.detection;

import java.util.logging.Level;

import node.IServiceModule;
import node.NodeControlCore;
import node.device.DeviceInfoManager;
import node.network.NetworkUtil;
import node.network.communicator.SocketHandler;
import node.network.packet.Packet;
import node.network.packet.PacketBuildFailureException;
import node.network.packet.PacketBuilder;
import node.network.packet.PacketUtil;

public class MasterNodeService implements Runnable, IServiceModule
{	
	public static final String PROP_DELAY_MASTER_MSG = "delayMasterNodeBroadcast";
	public static final String KPROTO_MASTER_BROADCAST = "masterNodeBroadcast";
	
	private DeviceInfoManager deviceInfoManager;
	private SocketHandler socketHandler;
	private boolean isRun;
	private Thread broadcastThread;
	private int broadCastDelay;
	
	/*public static void main(String[] args)
	{
		NodeControlCore.init();
		DB_Handler db = new DB_Handler();
		db.startModule();
		DeviceInfoManager infoManager = new DeviceInfoManager(db);
		infoManager.startModule();
		SocketHandler sock = new SocketHandler();
		sock.startModule();
		MasterNodeBroadcast inst = new MasterNodeBroadcast(infoManager, sock);
		inst.startModule();
		
		sock.addObserver(KPROTO_MASTER_BROADCAST, new Observer<NetworkEvent>()
		{
			@Override
			public void update(Observable<NetworkEvent> object, NetworkEvent data)
			{
				System.out.println(data.packet.toString());
				
			}
		});
		
		db.getInstaller().complete();
	}*/
	
	public MasterNodeService(DeviceInfoManager deviceInfoManager, SocketHandler socketHandler)
	{
		this.deviceInfoManager = deviceInfoManager;
		this.socketHandler = socketHandler;
	}

	@Override
	public boolean startModule()
	{
		if(this.isRun) return true;
		this.isRun = true;
		
		this.broadCastDelay = Integer.parseInt(NodeControlCore.getProp(PROP_DELAY_MASTER_MSG));
		this.broadcastThread = new Thread(this);
		this.broadcastThread.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		if(!this.isRun) return;
		this.isRun = false;
		
		this.broadcastThread.interrupt();
	}

	@Override
	public void run()
	{
		StringBuffer msgBuffer;
		PacketBuilder packetBuilder;
		String[][] queryArr;
		Packet packet;
		
		NodeDetectionService.nodeDetectionLogger.log(Level.INFO, "마스터 브로드캐스트 간격: " + this.broadCastDelay);
		
		while(this.isRun)
		{
			try
			{
				Thread.sleep(this.broadCastDelay);
			}
			catch (InterruptedException e) {}
			
			msgBuffer = new StringBuffer();
			packetBuilder = new PacketBuilder();
			queryArr = this.deviceInfoManager.getDeviceIPTable();
			
			for(int i = 0; i < queryArr.length; ++i)
			{
				msgBuffer.append(queryArr[0]);
				msgBuffer.append(PacketUtil.DPROTO_SEP_COL);
				msgBuffer.append(queryArr[1]);
				msgBuffer.append(PacketUtil.DPROTO_SEP_ROW);
			}
			
			try
			{
				packetBuilder.setSender(this.deviceInfoManager.getMyDevice().uuid);
				packetBuilder.setBroadCast();
				packetBuilder.setKey(KPROTO_MASTER_BROADCAST);
				packetBuilder.setData(msgBuffer.toString());
				packet = packetBuilder.createPacket();
			}
			catch (PacketBuildFailureException e)
			{
				
				NodeDetectionService.nodeDetectionLogger.log(Level.SEVERE, "패킷 빌드중 오류", e);
				continue;
			}
			
			
			this.socketHandler.sendMessage(NetworkUtil.broadcastIA(), packet);
			
		}
		
	}
}