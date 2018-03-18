package compiler.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Util {

	Map<String, ArrayList<String>> hashMap = new HashMap<String,  ArrayList<String>>();
	
	public Map<String, ArrayList<String>> getHashMap() {
		return hashMap;
	}

	public void setHashMap(Map<String, ArrayList<String>> hashMap) {
		this.hashMap = hashMap;
	}

	public Map<String, ArrayList<String>> reportError(String key, String value) {
		addValues(key,value);	
		return hashMap;
	}

	private void addValues(String key, String value) {
		ArrayList<String> tempList = null;
		if (hashMap.containsKey(key)) {
			tempList = hashMap.get(key);
			if (tempList == null)
				tempList = new ArrayList<String>();
			tempList.add(value);
		} else {
			tempList = new ArrayList<String>();
			tempList.add(value);
		}
		hashMap.put(key, tempList);
	}

}
