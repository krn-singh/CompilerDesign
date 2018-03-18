package compiler.sematic;

import java.util.ArrayList;
import java.util.LinkedList;

import compiler.constants.CompilerEnum.SymTableEntryCategory;

public class SymTable {

	private String tableName = null;
	private ArrayList<SymTableEntry> entries = new ArrayList<SymTableEntry>();
	private SymTable globalTable = null;
	private LinkedList<SymTable> tables = new LinkedList<SymTable>();
	private SymTable parent = null;
	
	public SymTable() {
		tableName = null;
		parent = null;
		entries = new ArrayList<SymTableEntry>();
	}
	
	public SymTable(String name, SymTable parent) {
		tableName = name;
		entries = new ArrayList<SymTableEntry>();
		this.parent = parent;
	}
	
	public void addEntry(SymTableEntry entry) {
		entries.add(entry);
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public SymTable getParent() {
		return parent;
	}
	
	public ArrayList<SymTableEntry> getEntries() {
		return entries;
	}

	public void setEntries(ArrayList<SymTableEntry> entries) {
		this.entries = entries;
	}

	public LinkedList<SymTable> getTables() {
		return tables;
	}

	public void setTables(LinkedList<SymTable> tables) {
		this.tables = tables;
	}

	public void addTables(SymTable table) {
		tables.add(table);
	}

	public SymTable createGlobalTable() {
		globalTable = new SymTable("global", null);
		addTables(globalTable);
		
		return globalTable;
	}
	
	public SymTable createTable(String name, SymTable parent) {
		
		SymTable table = new SymTable(name, parent);
		addTables(table);
		
		return table;
	}
	
	public SymTable findTable(String name) {
		
		for (int i = 0; i < tables.size(); i++) {
			if (tables.get(i).getTableName().equals(name)) {
				return tables.get(i);
			}
		}
		
		return null;
	}
	
	public SymTableEntry searchRecord(SymTable table, String name, String type) {
		
		ArrayList<SymTableEntry> current = table.getEntries();
		
		for (int i = 0; i < current.size(); i++) {
			if (current.get(i).getName().equals(name) && current.get(i).getType().equals(type)) {
				return current.get(i);
			}
		}
		
		return null;						
	}
	
	public SymTableEntry searchRecord(SymTable table, String name, SymTableEntryCategory category) {
		
		ArrayList<SymTableEntry> current = table.getEntries();
		
		for (int i = 0; i < current.size(); i++) {
			if (current.get(i).getName().equals(name) && current.get(i).getCategory() == category) {
				return current.get(i);
			}
		}
		
		return null;						
	}

}
