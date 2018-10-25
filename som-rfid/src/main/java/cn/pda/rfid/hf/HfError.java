package cn.pda.rfid.hf;

public class HfError implements Error{
	
	private int errorCode = -1;

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	

}
