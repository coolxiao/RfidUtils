package cn.pda.rfid.hf;

import java.util.List;


public interface HfConmmand {
	
	public byte[] findCard14443A(Error error);

	public int auth14443A(int authType, byte[] access, byte[] uid, int sector, Error error);

	public byte[] read14443A(int block, Error error);
	public int write14443A(byte[] writeData, int block, Error error);

	public List<Iso15693InventoryInfo> findCard15693(Error error);

	public Iso15693CardInformation getInformation15693(byte[] uid, int flags, Error error);

	public byte[] readSingleBlock15693(byte[] uid, int flag, int block, Error error);

	public int writeSingleBlock(byte[] uid, int flag, int block, byte[] writeData, Error error);

}
