package com.pda.hf;

public class HfNative {
	

	static{
		System.loadLibrary("devapi") ;
		System.loadLibrary("fxjni") ;
	}
	public native int open(int port , int baudrate, int powerCode ) ;
	
	public native int close(int port) ;
	
	public native int findM1Card(byte[] uid) ;
	
	public native int authM1(int keyType, int sector, byte[] keys, int keyLen, byte[] uid, int uLen) ;
	
	public native int readM1Block(int block, byte[] blockData) ;
	
	public native int writeM1Block(int block , byte[] writeData, int wLen) ;
	
	public native int find15693(byte[] result) ;
	
	public native int get15693PICC(byte[] uid, int flag , byte[] result) ;
	
	public native int readBlock15693(byte[] uid, int flag , int block , byte[] result) ;
	
	public native int writeBlock15693(byte[] uid, int flag , int block ,byte[] wData, int wLen , byte[] result) ;
}
