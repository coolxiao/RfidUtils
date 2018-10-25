package cn.pda.rfid.hf;

public class Iso15693InventoryInfo {
	private byte flag ;
	private byte dsfid ;
	private byte[] uid ;
	
	public byte getFlag() {
		return flag;
	}
	public void setFlag(byte flag) {
		this.flag = flag;
	}
	public byte getDsfid() {
		return dsfid;
	}
	public void setDsfid(byte dsfid) {
		this.dsfid = dsfid;
	}
	public byte[] getUid() {
		return uid;
	}
	public void setUid(byte[] uid) {
		this.uid = uid;
	}
	
	

}
