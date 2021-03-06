package Server_Side.Server_LVL;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Map;

import Record_Type.DoctorRecord;
import Record_Type.NurseRecord;
import Record_Type.RecordInfo;

public class Clinic_LVL_UDP_Listener implements Runnable{
	
	/**
	 * Default constructor.
	 */
	public Clinic_LVL_UDP_Listener() {
		
	}
	
	@Override
	public void run() {
		DatagramSocket socket = null;
		try{
			socket = new DatagramSocket(Config_LVL.LOCAL_LISTENING_PORT);
			while(true){
				byte[] buffer = new byte[1000]; 
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);
				Config_LVL.LOGGER.info("Get request: " + (new String(request.getData()).trim())+ "\n" + "Start a new thread to handle this.");
				new Connection(socket, request);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(socket != null) socket.close();
		}		
	}
	
	/**
	 * New thread to handle the newly request
	 * @author AlexChen
	 *
	 */
	static class Connection extends Thread{
		DatagramSocket socket = null;
		DatagramPacket request = null;
		String result = null;
		public Connection(DatagramSocket n_socket, DatagramPacket n_request) {
			this.socket = n_socket;
			this.request = n_request;
			String requestcode = new String(request.getData()).trim().substring(0, 3);
			switch (requestcode) {
			case "001":
				Config_LVL.LOGGER.info("Request code: " + requestcode + ", " + "Check ManagerID: " + (new String(request.getData()).trim().substring(4)+ " valid or not."));
				result = checkManagerID(new String(request.getData()).trim().substring(4));
				break;
			case "002":
				Config_LVL.LOGGER.info("Request code: " + requestcode + ", " + "Search HashMap, SearchType: " + (new String(request.getData()).trim().substring(4)));
				result = getLocalHashSize(new String(request.getData()).trim().substring(4));
				break;
			case "003":
				Config_LVL.LOGGER.info("Request code: " + requestcode + ", " + "Transfer Record, Record: " + (new String(request.getData()).trim().substring(4)));
				result = insertRecordInLocalHashMap(new String(request.getData()).trim().substring(4));
				break;
			}
			this.start();
		}
		
		@Override
		public void run() {
			try {
				DatagramPacket reply = new DatagramPacket(result.getBytes(),result.getBytes().length, request.getAddress(), request.getPort()); 
				socket.send(reply);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Check ManagerID and return valid or invalid.
	 * @param managerID
	 * @return
	 */
	public static String checkManagerID(String managerID){
		for(String account: Config_LVL.MANAGER_ACCOUNT){
			if(account.equalsIgnoreCase(managerID)){
				return "valid";
			}
		}
		return "invalid";
	}
	
	/**
	 * Check local hash table size and return the value.
	 * @param recordType
	 * @return
	 */
	public static synchronized String getLocalHashSize(String recordType){
		int dr_num = 0;
		int nr_num = 0;
		
		for(Map.Entry<Character, ArrayList<RecordInfo>> entry:Config_LVL.HASH_TABLE.entrySet()){
			for(RecordInfo record:entry.getValue()){
				switch(record.getRecordID().substring(0, 2)){
				case "DR":
					dr_num++;
					break;
				case "NR":
					nr_num++;
					break;
				}
			}
		}
		if(recordType.equalsIgnoreCase("dr")){
			return "LVL "+"DR: "+dr_num;
		}else if(recordType.equalsIgnoreCase("nr")){
			return "LVL "+"NR: "+nr_num;
		}else{
			return "LVL "+"ALL: "+(dr_num+nr_num);
		}
	}
	
	/**
	 * Input new record which transfered from other server into local hash map
	 * @param recordInfo
	 * @return
	 */
	public static synchronized String insertRecordInLocalHashMap(String recordInfo){
		String[] record = recordInfo.split("\n");
		if(record[0].contains("DR")){
			if(Config_LVL.HASH_TABLE.containsKey(record[2].split(": ")[1].charAt(0))){
				Config_LVL.RECORD_LIST = Config_LVL.HASH_TABLE.get(record[2].split(": ")[1].charAt(0));
			}else{
				Config_LVL.RECORD_LIST = new ArrayList<RecordInfo>();
			}
			Config_LVL.RECORD_LIST.add(new RecordInfo(record[0].split(": ")[1], new DoctorRecord(record[1].split(": ")[1], record[2].split(": ")[1], record[3].split(": ")[1], record[4].split(": ")[1], record[5].split(": ")[1], record[6].split(": ")[1])));
			Config_LVL.HASH_TABLE.put(record[2].split(": ")[1].charAt(0), Config_LVL.RECORD_LIST);
			return "Transfer doctor record success.";
		}else if(record[0].contains("NR")){
			if(Config_LVL.HASH_TABLE.containsKey(record[2].split(": ")[1].charAt(0))){
				Config_LVL.RECORD_LIST = Config_LVL.HASH_TABLE.get(record[2].split(": ")[1].charAt(0));
			}else{
				Config_LVL.RECORD_LIST = new ArrayList<RecordInfo>();
			}
			Config_LVL.RECORD_LIST.add(new RecordInfo(record[0].split(": ")[1], new NurseRecord(record[1].split(": ")[1], record[2].split(": ")[1], record[3].split(": ")[1], record[4].split(": ")[1], record[5].split(": ")[1])));
			Config_LVL.HASH_TABLE.put(record[2].split(": ")[1].charAt(0), Config_LVL.RECORD_LIST);
			return "Transfer nurse record success.";
		}
		return "Transfer record fail.";
	}
}
