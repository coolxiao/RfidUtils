package cn.pda.rfid.hf;

import android.util.Log;

import com.pda.hf.HfNative;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.pda.serialport.SerialPort;
import cn.pda.serialport.Tools;

public class HfReader implements HfConmmand {

	private InputStream is;
	private OutputStream os;
	
	/////////////////////POWER CODE/////////////////////////
	public static final int  POWER_3_3V	=			0x00 ;
	public static final int POWER_PSAM	=			0x01;
	public static final int POWER_RFID	=			0x02;
	public static final int POWER_5V		=		    0x03;
	public static final int POWER_SCAN	=			0x04;
	

	private static final byte HEAD_H = (byte) 0x55;
	private static final byte HEAD_L = (byte) 0xAA;
	private static final byte END_L = (byte) 0x55;
	private static final byte END_H = (byte) 0xAA;

	private static final byte CMD_TYTE_APPL = (byte) 0x44; 				// 应用命令
	private static final byte CMD_TYTE_RESET = (byte) 0x65; 			// 模组复位
	private static final byte CMD_TYTE_GET_SYSINFO = (byte) 0x66; 		// 固件信息
	private static final byte CMD_TYTE_GET_VERSION = (byte) 0x67; 		// 固件版本

	/** command code **/
	private static final byte ISO_14443A_INIT = (byte) 0xA0; 			// 初始化14443a
	private static final byte ISO_14443A_DEINIT = (byte) 0xAF; 			// 取消初始化
	private static final byte ISO_14443A_FIND_CARD = (byte) 0xA1; 		// 寻卡
	private static final byte MIFARE_INIT = (byte) 0xE0; 				// Mifare初始化
	private static final byte MIFARE_DEINIT = (byte) 0xEF; 				// Mifare取消初始化
	private static final byte MIFARE_AUTH_A = (byte) 0xE1; 				// Mifare认证A
	private static final byte MIFARE_AUTH_B = (byte) 0xE2; 				// Mifare认证B
	private static final byte MIFARE_READ_SINGLE = (byte) 0xE5; 		// Mifare读单独块
	private static final byte MIFARE_WRITE_SINGLE = (byte) 0xE6; 		// Mifare写单独块
	
	private static final byte ISO_15693_INIT = (byte) 0xD0 ; 			//初始化
	private static final byte ISO_15693_DEINIT = (byte) 0xDF ; 			//取消初始化
	private static final byte ISO_15693_INVENTORY = (byte) 0xD2 ; 		//寻卡
	private static final byte ISO_15693_GET_PICC = (byte) 0xD6 ; 		//获取PICC系统信息
	private static final byte ISO_15693_READ_SINGLE = (byte) 0xD7 ; 	//读单独块
	private static final byte ISO_15693_WRITE_SINGLE = (byte) 0xD8 ; 	//写单独快
	
	public static final int AUTH_A = 0;
	public static final int AUTH_B = 1;
	
	/////////////////new version////////////////////
	private HfNative hf ;
	boolean isNew = false ;
	private SerialPort mSerial ;
	
	/** Error Code **/
	private static final int ERROR_INPUT_OUTPUT_STREAM = -2 ; 	//串口输入输出流出错
	

	public HfReader(InputStream input, OutputStream output) {
		
		is = input;
		os = output;
		try {
			Thread.sleep(200);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			is.read(new byte[16]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
	}
	
	
	private int port ; 
	private int powerCode ;
	/**
	 * 
	 * @param port 串口号
	 * @param powerCode  供电口
	 */
	public HfReader(int port, int powerCode) throws IOException {
		
		this.port = port ; 
		this.powerCode = powerCode ;
		mSerial = new SerialPort(port, 115200, 0) ;
		switch (powerCode) {
		case POWER_PSAM:
			mSerial.psam_poweron() ;
//				mSerial.rfid_poweron() ;
			break;
		case POWER_RFID:
			mSerial.rfid_poweron() ;
//				mSerial.psam_poweron() ;
			break;
		case POWER_5V:
			mSerial.power_5Von() ;
			break;
		}
		is = mSerial.getInputStream() ;
		os = mSerial.getOutputStream() ;
		isNew = !getVersion(new HfError()) ;
		if(isNew){
			mSerial.close(port) ;
			is = null ;
			os = null ;
			hf = new HfNative() ;
			//新模块初始化
			int ret  = hf.open(port, 115200, powerCode) ;
		}
	}

	/**
	 * 填充指令
	 * @return
	 *   dataLenH   |   dataLenL     |  rspH   |   rspL   |    data   |
	 *     1byte		  1byte          1byte    1byte      dataLen	 
	 * 
	 */
	private byte[] fillContent(byte rspH, byte rspL, byte[] data, int dataLen){
		byte dataLenH = (byte)(dataLen / 256);
		byte dataLenL = (byte) (dataLen % 256);
		byte[] content = new byte[4 + dataLen];
		content[0] = dataLenH;
		content[1] = dataLenL ;
		content[2] = rspH;
		content[3] = rspL;
		if(dataLen > 0){
			System.arraycopy(data, 0, content, 4, dataLen);
		}
		return content;
	}
	
	/**
	 * 填充指令包
	 * @param content
	 * @return
	 * | headH  | headL |  cmd type  |  content       |  endH   |   endL   |
	 *   1byte    1byte     1byte        contentLen      1byte      1byte
	 */
	private byte[] genPackage(byte cmdType, byte[] content, int contentLen){
		byte[] cmdPackage = new byte[5 + contentLen];
		cmdPackage[0] = HEAD_H;
		cmdPackage[1] = HEAD_L;
		cmdPackage[2] = cmdType;
		System.arraycopy(content, 0, cmdPackage, 3, contentLen);
		cmdPackage[3 + contentLen] = END_H;
		cmdPackage[4 + contentLen] = END_L ;
		return cmdPackage;
	}
	
	private boolean sendCMD(byte[] cmd){
		boolean flag = false ;
		if(os != null){
			try {
				os.write(cmd);
				flag = true ;
			} catch (IOException e) {
			}
		}
		return flag ;
	}
	
	
	boolean reading = true ;
	//取得返回包
	private byte[] getResponse(byte cmdType){
		int size = 0;
		byte[] buffer = new byte[512];
		byte[] temp = new byte[1024];
		byte[] dataPackage ;
		int index = 0;  //temp有效数据指向
		int count = 0;  //temp有效数据长度
		reading = true;
		//超时保护
		Timer mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				reading = false ;
			}
		}, 500);
		while(reading){
			try {
				size = is.read(buffer);
				if(size > 0){
					count+=size ;
					count += size ;
					//超出temp长度清空
					if(count > 512){
						count = 0;
						Arrays.fill(temp, (byte)0x00);
					}
					//先将接收到的数据拷到temp中
					System.arraycopy(buffer, 0, temp, index, size);
					index = index + size ;
					if(count > 8){
//						Log.e("getResponse", "temp : " + Tools.Bytes2HexString(temp, index));
						//判断包头和指令类型
						if(temp[0] == HEAD_H && temp[1] == HEAD_L && temp[2] == cmdType){
							int dataLen = (temp[5]&0xff)*256 + temp[6]&0xff;
							if(count < 9 + dataLen){//未收到完整的数据包
								continue;
							}
							if(temp[dataLen + 7] == END_H && temp[dataLen + 8] == END_L){
//								Log.e("getResponse", "getpackage" );
								dataPackage = new byte[dataLen + 9];
								System.arraycopy(temp, 0, dataPackage, 0, dataLen + 9);
								mTimer.cancel();
								return dataPackage;
							}
							
						}else{
							//包错误清空
							count = 0;
							index = 0;
							Arrays.fill(temp, (byte)0x00);
						}
					}
				}
			} catch (IOException e) {
				
				return null;
			}
		}
		return null;
	}
	
	//解析返回数据包，状态信息存入error
	private byte[] handleResp(byte[] resp, Error error){
		int stute = (resp[3]&0xff)*256 + (resp[4]&0xff);
		int dataLen = (resp[5]&0xff)*256 + (resp[6]&0xff);
		byte[] data = null;
		error.setErrorCode(stute);
		if(stute == 0){//状态正确
			if(dataLen > 0){
				data = new byte[dataLen];
				System.arraycopy(resp, 7, data, 0, dataLen);
			}
		}
		return data;
	}
//==============================================================	
	//读版本号
	private boolean getVersion(Error error){
		try {
			//清除上电时的无用数据
			Thread.sleep(500) ;
			byte[] temp = new byte[16] ;
			is.read(temp) ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//指令
		byte[] cmd = Tools.HexString2Bytes("55AA66000000FFAA55");
		byte[] dataPackage ;
		Log.e("cmd", Tools.Bytes2HexString(cmd, cmd.length)) ;
		if(sendCMD(cmd)){
			dataPackage = read();
			if(dataPackage != null && dataPackage.length > 20){
				Log.e("data", Tools.Bytes2HexString(dataPackage, dataPackage.length)) ;
				return true ;
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		
		return false ;
	}
	
	//从串口当中读取数据
	private byte[] read(){
		int count = 0;
		int index = 0;
		byte[] recvData = null;
		try {
		while(count < 1){
			count = is.available();
			//读取数据超时
			if(index > 50){
				return null;
			}else{
				index++;
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		count = is.available();
		recvData = new byte[count];
		is.read(recvData);
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*=============测试数据==============*/
		if(recvData != null){
			Log.e("recvData", Tools.Bytes2HexString(recvData, recvData.length));
		}
		return recvData;
	}
	
	
	//==================================================
	
	//14443A初始化
	private int init14443A(Error error){
		if(isNew){
			return 0 ;
		}
		byte[] content = fillContent((byte)0x00, (byte)0x00, new byte[]{CMD_TYTE_GET_VERSION}, 1);
		byte[] cmd = genPackage(CMD_TYTE_GET_VERSION, content, content.length);
		byte[] dataPackage ;
		if(sendCMD(cmd)){
			dataPackage = getResponse(CMD_TYTE_APPL);
			if(dataPackage != null){
				handleResp(dataPackage, error);
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		return 0;
	}
	
	//14443A取消初始化
	private int deInit14443A(){
		if(isNew){
			return 0 ;
		}
		HfError error = new HfError();
		byte[] content = fillContent((byte)0x00, (byte)0x00, new byte[]{ISO_14443A_DEINIT, (byte)0x01}, 2);
		byte[] cmd = genPackage(CMD_TYTE_APPL, content, content.length);
		byte[] dataPackage ;
//		Log.e("deInit14443A", Tools.Bytes2HexString(cmd, cmd.length));
		if(sendCMD(cmd)){
			dataPackage = getResponse(CMD_TYTE_APPL);
			if(dataPackage != null){
				handleResp(dataPackage, error);
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		return 0;
	}
	
	//Mifare初始化
	private int initMifare(){
		if(isNew){
			return 0 ;
		}
		Error error = new HfError();
		byte[] content = fillContent((byte)0x00, (byte)0x00, new byte[]{MIFARE_INIT}, 1);
		byte[] cmd = genPackage(CMD_TYTE_APPL, content, content.length);
		byte[] dataPackage ;
//		Log.e("initMifare", Tools.Bytes2HexString(cmd, cmd.length));
		if(sendCMD(cmd)){
			dataPackage = getResponse(CMD_TYTE_APPL);
			if(dataPackage != null){
				handleResp(dataPackage, error);
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		return 0;
	}
	
	//Mifare取消初始化
	
	//
	
	@Override
	public byte[] findCard14443A(Error error) {
		if(isNew){
			byte[] result = new byte[64] ;
			byte[] uid = null ; 
			int ret = hf.findM1Card(result) ;
			if(ret > 0){
				uid = new byte[ret] ;
				System.arraycopy(result, 0, uid, 0, ret) ;
			}
			return uid ;
		}
		init14443A(error);
		byte[] content = fillContent((byte)0x00, (byte)0x12, 
				new byte[]{ISO_14443A_FIND_CARD, (byte)0x52}, 2);
		byte[] cmd = genPackage(CMD_TYTE_APPL, content, content.length);
		byte[] uid = null;
		byte[] dataPackage = null;
		if(sendCMD(cmd)){
			dataPackage = getResponse(CMD_TYTE_APPL);
			if(dataPackage != null){
				byte[] temp = handleResp(dataPackage, error);
				if(temp != null){
//					Log.e("find card 14443A temp", Tools.Bytes2HexString(temp, temp.length));
					if(temp[3] == (byte)0x01){  //s50
						uid = new byte[4];
						System.arraycopy(temp, 7, uid, 0, uid.length);
					}else if(temp[3] == (byte)0x02){
						uid = new byte[7];
						System.arraycopy(temp, 7, uid, 0, uid.length);
					}
				}
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		//取消14443A初始化
		deInit14443A();
		//Mifare初始化
		initMifare();
		return uid;
	}

	@Override
	public int auth14443A(int authType, byte[] access, byte[] uid, int sector, Error error) {
		if(isNew){
			int ret = hf.authM1(authType, sector, access, access.length, uid, uid.length) ;
			if(ret >=0){
				return 0 ;
			}
			return -1 ;
		}
		byte[] contentData = new byte[3 + access.length + uid.length];
		byte[] content ;
		byte[] cmd ; 
		byte[] resp = null;
		if(authType == AUTH_A){
			contentData[0] = MIFARE_AUTH_A;
		}else if(authType == AUTH_B){
			contentData[0] = MIFARE_AUTH_B;
		}
		contentData[1] = (byte) sector;
		System.arraycopy(access, 0, contentData, 2, access.length);
		contentData[2 + access.length] = (byte) uid.length;
		System.arraycopy(uid, 0, contentData, 3 + access.length, uid.length);
		content = fillContent((byte)0x00, (byte)0x01, contentData, contentData.length);
		cmd = genPackage(CMD_TYTE_APPL, content, content.length);
//		Log.e("auth14443A cmd", Tools.Bytes2HexString(cmd, cmd.length));
		if(sendCMD(cmd)){
			resp = getResponse(CMD_TYTE_APPL);
			if(resp != null){
//				Log.e("auth14443A resp", Tools.Bytes2HexString(resp, resp.length));
				handleResp(resp, error);
//				if(data != null){
//					Log.e("auth14443A data", Tools.Bytes2HexString(data, data.length));
//				}
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		return error.getErrorCode();
	}

	@Override
	public byte[] read14443A(int block, Error error) {
		if(isNew){
			byte[] buffer = new byte[64] ;
			byte[] result = null ;
			int ret = hf.readM1Block(block, buffer) ;
			if(ret > 0){
				result = new byte[ret] ;
				System.arraycopy(buffer, 0, result, 0, ret) ;
			}
			return result ;
		}
		byte[] content ;
		byte[] cmd ;
		byte[] resp ;
		byte[] data = null;
		content = fillContent((byte)0x00, (byte)0x12, new byte[]{MIFARE_READ_SINGLE, (byte)block}, 2);
		cmd = genPackage(CMD_TYTE_APPL, content, content.length);
		if(sendCMD(cmd)){
			resp = getResponse(CMD_TYTE_APPL);
			if(resp != null){
//				Log.e("read14443A resp", Tools.Bytes2HexString(resp, resp.length));
				data = handleResp(resp, error);
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		return data;
	}

	@Override
	public int write14443A(byte[] writeData, int block, Error error) {
		if(isNew){
			int ret = hf.writeM1Block(block, writeData, writeData.length) ;
			if(ret >= 0){
				return 0 ;
			}
			return -1 ;
		}
		byte[] contentData ;
		byte[] content ;
		byte[] cmd ;
		byte[] resp ;
		contentData = new byte[2 + writeData.length];
		contentData[0] = MIFARE_WRITE_SINGLE;
		contentData[1] = (byte) block;
		System.arraycopy(writeData, 0, contentData, 2, writeData.length);
		content = fillContent((byte)0x00, (byte)0x00, contentData, contentData.length);
		cmd = genPackage(CMD_TYTE_APPL, content, content.length);
		if(sendCMD(cmd)){
			resp = getResponse(CMD_TYTE_APPL);
			if(resp != null){
//				Log.e("write14443A resp", Tools.Bytes2HexString(resp, resp.length));
				handleResp(resp, error);
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		
		return error.getErrorCode();
	}

	
	////////////////////////////////  ISO 15693  /////////////////////////////////////
	private void init15693(Error error){
		if(isNew){
			return  ;
		}
		byte[] content ;
		byte[] cmd ;
		byte[] resp ;
		content  = fillContent((byte)0x00, (byte)0x04, 
				new byte[]{ISO_15693_INIT, (byte)0x00, (byte)0x80}, 3);
		cmd = genPackage(CMD_TYTE_APPL, content, content.length);
//		Log.e("init15693 cmd", Tools.Bytes2HexString(cmd, cmd.length));
		if(sendCMD(cmd)){
			resp = getResponse(CMD_TYTE_APPL);
			if(resp != null){
				handleResp(resp, error);
//				Log.e("init15693 resp", Tools.Bytes2HexString(resp, resp.length));
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
	}
	
	private void deinit15693(Error error){
		if(isNew){
			return  ;
		}
		byte[] content ;
		byte[] cmd ;
		byte[] resp ;
		content  = fillContent((byte)0x00, (byte)0x00, 
				new byte[]{ISO_15693_DEINIT, (byte)0x00, }, 2);
		cmd = genPackage(CMD_TYTE_APPL, content, content.length);
//		Log.e("deinit15693 cmd", Tools.Bytes2HexString(cmd, cmd.length));
		if(sendCMD(cmd)){
			resp = getResponse(CMD_TYTE_APPL);
			if(resp != null){
				handleResp(resp, error);
//				Log.e("deinit15693 resp", Tools.Bytes2HexString(resp, resp.length));
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
	}
	
	@Override
	public List<Iso15693InventoryInfo> findCard15693(Error error) {
		byte[] muid = null;
		List<Iso15693InventoryInfo> listInfo = new ArrayList<Iso15693InventoryInfo>();
		if(isNew){
			byte[] result = new byte[64] ;
			
			int ret = hf.find15693(result) ;
			if(ret > 0){
				muid = new byte[ret - 2] ;
				System.arraycopy(result, 2, muid, 0, ret - 2) ;
				Iso15693InventoryInfo info = new Iso15693InventoryInfo() ;
				info.setUid(muid) ;
				info.setFlag(result[0]) ;
				info.setDsfid(result[1]) ;
				listInfo.add(info) ;
			}
			return listInfo ;
		}
		deinit15693(error);
		init15693(error);
		byte[] content ;
		byte[] cmd ;
		byte[] resp ;
		
		content = fillContent((byte)0x00, (byte)0xFF, new byte[]{ISO_15693_INVENTORY}, 1);
		cmd = genPackage(CMD_TYTE_APPL, content, content.length);
//		Log.e("findCard15693 cmd", Tools.Bytes2HexString(cmd, cmd.length));
		if(sendCMD(cmd)){
			resp = getResponse(CMD_TYTE_APPL);
			if(resp != null){
				byte[] data = handleResp(resp, error);
				
//				Log.e("findCard15693 resp", Tools.Bytes2HexString(resp, resp.length));
				if(data != null){
//					Log.e("findCard15693 data", Tools.Bytes2HexString(data, data.length));
					int cardCount = data[0]&0xff ;
					while(cardCount > 0){
						Iso15693InventoryInfo info = new Iso15693InventoryInfo();
						info.setFlag(data[10*(cardCount - 1) + 1]);
						info.setDsfid(data[10*(cardCount - 1) + 2]);
						byte[] uid = new byte[8];
						System.arraycopy(data, 10*(cardCount - 1) + 3, uid, 0, 8);
						cardCount--;
						info.setUid(uid);
						listInfo.add(info);
					}
					
				}
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		return listInfo;
	}

	@Override
	public Iso15693CardInformation getInformation15693(byte[] uid, int flag ,Error error) {
		if(isNew){
			
			return null ;
		}
		byte[] contentData ;
		byte[] content ;
		byte[] cmd ;
		byte[] resp ;
		Iso15693CardInformation info = null;
		contentData = new byte[2 + uid.length];
		contentData[0] = ISO_15693_GET_PICC;
		contentData[1] = (byte) flag ;
		System.arraycopy(uid, 0, contentData, 2, uid.length);
		content = fillContent((byte)0x00, (byte)0x0f, contentData, contentData.length);
		cmd = genPackage(CMD_TYTE_APPL, content, content.length);
		if(sendCMD(cmd)){
			resp = getResponse(CMD_TYTE_APPL);
			if(resp != null){
//				Log.e("getInformation15693 resp", Tools.Bytes2HexString(resp, resp.length));
				byte[] data = handleResp(resp, error);
				if(data != null ){
//					Log.e("getInformation15693 data", Tools.Bytes2HexString(data, data.length));
					info = new Iso15693CardInformation();
					info.setFlag(data[0]);
					info.setInformationFlag(data[1]);
					byte[] mUid = new byte[8];
					System.arraycopy(data, 2, mUid, 0, 8);
					info.setUid(mUid);
					info.setDsfid(data[10]);
					info.setAfi(data[11]);
					info.setBlocksCount(data[12]);
					info.setBlockLen(data[13]);
					info.setIC_reference(data[14]);
				}
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		return info;
	}

	@Override
	public byte[] readSingleBlock15693(byte[] uid, int flag,int block, Error error) {
		byte[] contentData ;
		byte[] content ;
		byte[] cmd ;
		byte[] resp ;
		byte[] readData = null;
		contentData = new byte[3 + uid.length];
		contentData[0] = ISO_15693_READ_SINGLE;
		contentData[1] = (byte) flag;
		System.arraycopy(uid, 0, contentData, 2, uid.length);
		contentData[2 + uid.length] = (byte) block;
		content = fillContent((byte)0x00, (byte)0x28, contentData, contentData.length);
		cmd = genPackage(CMD_TYTE_APPL, content, content.length);
		if(sendCMD(cmd)){
			resp = getResponse(CMD_TYTE_APPL);
			if(resp != null){
//				Log.e("readSingleBlock15693 resp", Tools.Bytes2HexString(resp, resp.length));
				byte[] data = handleResp(resp, error);
				if(data != null){
//					Log.e("readSingleBlock15693 data", Tools.Bytes2HexString(data, data.length));
					readData = new byte[4];
					System.arraycopy(data, 1, readData, 0, 4);
				}
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		return readData;
	}

	@Override
	public int writeSingleBlock(byte[] uid,int flag, int block, byte[] writeData,
			Error error) {
		byte[] contentData ;
		byte[] content ;
		byte[] cmd ;
		byte[] resp ;
		contentData = new byte[3 + uid.length + writeData.length];
		contentData[0] = ISO_15693_WRITE_SINGLE;
		contentData[1] = (byte) flag;
		System.arraycopy(uid, 0, contentData, 2, uid.length);
		contentData[2 + uid.length] = (byte) block;
		System.arraycopy(writeData, 0, contentData, 3 + uid.length, writeData.length);
		content = fillContent((byte)0x00, (byte)0x02, contentData, contentData.length);
		cmd = genPackage(CMD_TYTE_APPL, content, content.length);
		if(sendCMD(cmd)){
			resp = getResponse(CMD_TYTE_APPL);
			if(resp != null){
//				Log.e("writeSingleBlock resp", Tools.Bytes2HexString(resp, resp.length));
				byte[] data = handleResp(resp, error);
				if(data != null){
//					Log.e("writeSingleBlock data", Tools.Bytes2HexString(data, data.length));
				}
			}
		}else{
			error.setErrorCode(ERROR_INPUT_OUTPUT_STREAM);
		}
		return error.getErrorCode();
	}
	
	/**
	 * 关闭串口电源
	 */
	public void close(){
		if(!isNew){
			if(is != null){
				try {
					is.close() ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(os != null){
				try {
					os.close() ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			switch (powerCode) {
			case POWER_PSAM:
				mSerial.psam_poweroff() ;
//				mSerial.rfid_poweroff() ;
				break;
			case POWER_RFID:
				mSerial.rfid_poweroff() ;
//				mSerial.psam_poweroff() ;
				break;
			case POWER_5V:
				mSerial.power_5Voff() ;
				break;

			default:
				break;
			}
			mSerial.close(port) ;
		}else{
			hf.close(port) ;
		}
	}

}
