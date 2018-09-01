package kr.dja.project2018.node.dhcp;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPResponseFactory;
import org.dhcp4java.DHCPServerInitException;
import org.dhcp4java.DHCPServlet;

import kr.dja.project2018.node.NodeControlCore;

public class DHCPService extends DHCPServlet
{
	public static final Logger dhcpLogger = DHCPCoreServer.logger;
	
	static
	{
		NodeControlCore.initLogger(dhcpLogger, "dhcp");
	}
	
	public DHCPService()
	{
		dhcpLogger.log(Level.INFO, "DHCP �ε�");
		String loadInterface = NodeControlCore.properties.get(NodeControlCore.PROP_INTERFACE).toString();
		dhcpLogger.log(Level.INFO, "��Ʈ��ũ �������̽� ����: " + loadInterface);
		NetworkInterface network = getNetworkInterfaces(loadInterface);
		
		if(network == null)
		{
			dhcpLogger.log(Level.SEVERE, "�ùٸ��� ���� �������̽�");
			return;
		}
		
		Enumeration<InetAddress> addrList = network.getInetAddresses();
		String addrStr = null;
		while(addrList.hasMoreElements())
		{
			InetAddress addr = addrList.nextElement();
			if(addr instanceof Inet4Address)
			{
				addrStr = addr.getHostAddress() + ":67";
				break;
			}
		}
		
		dhcpLogger.log(Level.INFO, "���� �ּ�: " + addrStr);
		try
		{
			Properties prop = new Properties();
			prop.setProperty(DHCPCoreServer.SERVER_ADDRESS, addrStr);
			
			DHCPCoreServer server = DHCPCoreServer.initServer(this, prop);
			new Thread(server).start();
		}
		catch (DHCPServerInitException e)
		{
			dhcpLogger.log(Level.SEVERE, "Server init", e);
		}
	}
	
	@Override
	public DHCPPacket service(DHCPPacket request)
	{
		dhcpLogger.log(Level.INFO, request.toString());
		byte[] deviceInfo = request.getOptionRaw((byte)224);
		dhcpLogger.log(Level.INFO, "DeviceInfo="+new String(deviceInfo));

		//DHCPResponseFactory.makeDHCPAck(request, offeredAddress, leaseTime, serverIdentifier, message, options)
		//DHCPResponseFactory.makeDHCPOffer(request, offeredAddress, leaseTime, serverIdentifier, message, options)
		return null;
	}
	
	private static NetworkInterface getNetworkInterfaces(String name)
	{
		Enumeration<NetworkInterface> nets = null;
		NetworkInterface findInterface = null;

		try
		{
			nets = NetworkInterface.getNetworkInterfaces();
		}
		catch (SocketException e)
		{
			dhcpLogger.log(Level.SEVERE, "��Ʈ��ũ �������̽� ����� ������ �� �����ϴ�.", e);
		}
		if (nets == null)
			return null;

		StringBuffer netInfoBuf = new StringBuffer();
		netInfoBuf.append("��Ʈ��ũ �������̽� ��ĵ\n");
		while (nets.hasMoreElements())
		{
			NetworkInterface net = nets.nextElement();
			try
			{
				if (!net.isUp())
					continue;
			}
			catch (SocketException e)
			{
				continue;
			}

			if (net.getName().equals(name))
			{
				findInterface = net;
				netInfoBuf.append("<SELECT>");
			}

			netInfoBuf.append("  Name:");
			netInfoBuf.append(net.getName());
			netInfoBuf.append(" Addr=>\n");

			int count = 0;
			Enumeration<InetAddress> addressItr = net.getInetAddresses();
			while (addressItr.hasMoreElements())
			{
				++count;
				InetAddress addr = addressItr.nextElement();
				netInfoBuf.append("    IP");
				netInfoBuf.append(count);
				netInfoBuf.append(": ");
				netInfoBuf.append(addr.getHostAddress());
				netInfoBuf.append("\n");
			}
			// netInfoBuf.deleteCharAt(netInfoBuf.length() - 1);

		}
		dhcpLogger.log(Level.INFO, netInfoBuf.toString());

		return findInterface;
	}
}


