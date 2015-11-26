package com.motus.motusupper;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;



class DataToDOF
{
	byte nCheckID;
	byte nCmd;
	byte nAct;
	byte nReserved;	//����

	float DOFs[];//{��ҡ�����㣬����ǰ�򣬲�������	}
	float Vxyz[];	//{ǰ�򣬲�������}������Ϊ��������Ϊ��
	float Axyz[];	//...
	boolean isready=false;
	DataToDOF()
	{
		nCheckID=55; nCmd=0; nAct=0; nReserved=0;
		DOFs=new float[6];
		Vxyz=new float[3];
		Axyz=new float[3];
		for(int i=0;i<6;i++)
		{
			if(i<3)
			{
				Vxyz[i]=0.0f;
				Axyz[i]=0.0f;
			}
			DOFs[i]=0.0f;
		}
	}
}

class DataToHost
{
	byte nCheckID;
	byte nDOFStatus;
	byte nRev0;
	byte nRev1;
	
	float attitude[];	      //ʵ����̬
	float para[];              //�������
	float motor_code[];  //�����ֵ
	DataToHost()
	{
		nCheckID=0;nDOFStatus=0;nRev0=0;nRev1=0;
		attitude=new float[6];
		para=new float[6];
		motor_code=new float[6];
		for(int i=0;i<6;i++)
		{
			attitude[i]=0.0f;
			para[i]=0.0f;
			motor_code[i]=0.0f;
		}
	}
	
}

class  RecvThread extends Thread 
{
	private final String VR="TAG";
	private DataToHost recvDataThread=null;
	public  DatagramSocket socketRecv=null;//����socket
	private boolean mstart=false;
	private DatagramPacket packet=null;
	private byte data[]=null;
	public RecvThread(DataToHost tempRecvDataThread)
	{
		super();
		recvDataThread=tempRecvDataThread;
	}
    public void run() 
    {
    	try {
			socketRecv = new DatagramSocket(10000);
			data = new byte[256]; 
			packet= new DatagramPacket(data,data.length); 
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	while(!mstart)
    	{
 
    		try {
				socketRecv.receive(packet);
				byte tempRecvData[]=packet.getData();
				String str=""+packet.getData();
				Log.i(VR,str);
				recvDataThread.nCheckID=tempRecvData[0];
				recvDataThread.nDOFStatus=tempRecvData[1];
				recvDataThread.nRev0=tempRecvData[2];
				recvDataThread.nRev1=tempRecvData[3];
				for(int i=0;i<6;i++)
				{
					recvDataThread.attitude[i]=getFloat(tempRecvData, 4+4*i);
					recvDataThread.para[i]=getFloat(tempRecvData, 28+4*i);
					recvDataThread.motor_code[i]=getFloat(tempRecvData, 52+4*i);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
    	}
    	socketRecv.close();
    }
    
    public void mstop() 
    {
    	mstart=true;
    }
    
    public static float getFloat(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }

 }

class  SendThread extends Thread 
{
	private InetAddress underMachineIpAdd=null;
	private DataToDOF sendDataThread=null;
	private DatagramSocket socketSend=null;//����socket
	private final int  underMachineIpPort=5000;
	private boolean mstart=false;
	private byte dataSendArr[];
	private byte buf[];
	private final int MAXLENGTH=100;
	private DatagramPacket packet;
	public SendThread(DataToDOF tempSendDataThread)
	{
		super();
		sendDataThread=tempSendDataThread;
		dataSendArr=new byte[52];
		buf=new byte[MAXLENGTH];
		try {
			socketSend = new DatagramSocket(10001);
			underMachineIpAdd = InetAddress.getByName("192.168.0.125");
			packet = new DatagramPacket(buf, MAXLENGTH);
			packet.setPort(underMachineIpPort);
			packet.setAddress(underMachineIpAdd);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public void run() 
    {
    	while(!mstart)
    	{
    		if(sendDataThread.isready)
    		{
	    		dataSendArr[0]=sendDataThread.nCheckID; dataSendArr[1]=sendDataThread.nCmd;dataSendArr[2]=sendDataThread.nAct;dataSendArr[3]=sendDataThread.nReserved;
	    		for(int i=0;i<6;i++)
	    		{
	    			putFloat(dataSendArr, sendDataThread.DOFs[i], 4+4*i);
	    		}
	    		for(int i=0;i<3;i++)
	    		{
	    			putFloat(dataSendArr, sendDataThread.Vxyz[i], 28+4*i);
	    		}
	    		for(int i=0;i<3;i++)
	    		{
	    			putFloat(dataSendArr, sendDataThread.Axyz[i], 40+4*i);
	    		}
	            //����һ��DatagramPacket�������ڷ������ݡ�    
	            //����һ��Ҫ���͵�����  �����������ݵĳ���  ������������˵������ַ  ����
	            packet.setData(dataSendArr);
	            packet.setLength(dataSendArr.length);
	          try {
	    			socketSend.send(packet);
	    		}catch (IOException e) 
	            {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}//�����ݷ��͵�����ˡ�    
	         sendDataThread.isready=false;
    		}
    	}
    	socketSend.close();
    }
    
    public void mstop() 
    {
    	mstart=true;
    }
    
    public static void putFloat(byte[] bb, float x, int index) 
	{
	    int l = Float.floatToIntBits(x);
	    for (int i = 0; i < 4; i++) 
	    {
	        bb[index + i] = new Integer(l).byteValue();
	        l = l >> 8;
	    }
	}
}

public class MotusUdp {
	private RecvThread cThread;
	private SendThread sThread;
	private DataToDOF senddata;
	public DataToHost recvData;
	public boolean init()
	{
		senddata =new DataToDOF();
		recvData =new DataToHost();
		cThread=new RecvThread(recvData);
		sThread=new SendThread(senddata);
		cThread.start();
		sThread.start();
		return true;
	}
	
	public void closeSocket()
	{
		cThread.mstop();
		sThread.mstop();
	}
	byte getStatus()
	{
		return recvData.nDOFStatus;
	}
	public void sendData(byte ncmd,float Data[],float atitude1[])
	{
		senddata.nCmd=ncmd;
		senddata.isready=false;
		senddata.DOFs[0] = Data[0] / 4;//�Ƕ�����
		senddata.DOFs[1] = Data[1] / 4;
		senddata.DOFs[2] = 0.0f;
		senddata.DOFs[3] = 0.0f;
		senddata.DOFs[4] = 0.0f;
		senddata.DOFs[5] = 0.0f;
	
		if(ncmd==2||ncmd==4||ncmd==6||ncmd==7)
		{
			for(int i=0;i<6;i++)
			{
				senddata.DOFs[i]=0.0f;
			}
		}
		senddata.isready=true;
	}
}


