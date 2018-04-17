package helper;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Util {

	Map<Integer, ArrayList<String>> map = new TreeMap<Integer,  ArrayList<String>>();

	public Map<Integer, ArrayList<String>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, ArrayList<String>> map) {
		this.map = map;
	}

	public Map<Integer, ArrayList<String>> reportError(Integer key, String value) {
		addValues(key,value);	
		return map;
	}

	private void addValues(Integer key, String value) {
		ArrayList<String> tempList = null;
		if (map.containsKey(key)) {
			tempList = map.get(key);
			if (tempList == null)
				tempList = new ArrayList<String>();
			tempList.add(value);
		} else {
			tempList = new ArrayList<String>();
			tempList.add(value);
		}
		map.put(key, tempList);
	}

	
}
