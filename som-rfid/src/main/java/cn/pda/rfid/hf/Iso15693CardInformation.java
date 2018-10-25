package cn.pda.rfid.hf;

/**
 * ISO15693 PICC系统信息
 * @author Administrator
 *
 */
public class Iso15693CardInformation {
	
	private byte flag ;
	private byte informationFlag ;
	private byte[] uid ;
	private byte dsfid;
	private byte afi;
	private int blocksCount;
	private int blockLen ;
	private byte IC_reference;
	
	public byte getFlag() {
		return flag;
	}
	public void setFlag(byte flag) {
		this.flag = flag;
	}
	public byte getInformationFlag() {
		return informationFlag;
	}
	public void setInformationFlag(byte informationFlag) {
		this.informationFlag = informationFlag;
	}
	public byte[] getUid() {
		return uid;
	}
	public void setUid(byte[] uid) {
		this.uid = uid;
	}
	public byte getDsfid() {
		return dsfid;
	}
	public void setDsfid(byte dsfid) {
		this.dsfid = dsfid;
	}
	public byte getAfi() {
		return afi;
	}
	public void setAfi(byte afi) {
		this.afi = afi;
	}
	public int getBlocksCount() {
		return blocksCount;
	}
	public void setBlocksCount(int blocksCount) {
		this.blocksCount = blocksCount;
	}
	public int getBlockLen() {
		return blockLen;
	}
	public void setBlockLen(int blockLen) {
		this.blockLen = blockLen;
	}
	public byte getIC_reference() {
		return IC_reference;
	}
	public void setIC_reference(byte iC_reference) {
		IC_reference = iC_reference;
	}
	
	

}
