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
	
	public SymTableEntry searchRecord(SymTable table, String name) {
		
		ArrayList<SymTableEntry> current = table.getEntries();
		
		for (int i = 0; i < current.size(); i++) {
			if (current.get(i).getName().equals(name)) {
				return current.get(i);
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
	
	public boolean checkDeclInSameScope(SymTable table, String name) {
		
		if (searchRecord(table, name) != null) {
			return true;
		}
		
		return false;
	}

	public boolean checkDeclInParentScope(SymTable table, LinkedList<SymTable> tables, String name) {
		
		this.tables = tables;
		SymTable current = findTable(table.getParent().getTableName());

		while (!current.getTableName().equals("global")) {
			
			if (searchRecord(current, name) == null) {
				if (current.getParent().getTableName().equals("global")) {
					return false;
				} else {
					current = findTable(current.getParent().getTableName());
				}
			} else {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean checkDeclInSuperClass(SymTable global, SymTable table, LinkedList<SymTable> tables, String name) {
		
		this.tables = tables;
		SymTableEntry entry = searchRecord(global, table.getTableName());
		if (entry == null || entry.getInheritedClassList().size() == 0) {
			return false;
		}
		for (SymTable symTable : entry.getInheritedClassList()) {
			if (searchRecord(symTable, name) != null) {
				return true;
			}
		}
		return false;
	}
	
	public SymTable searchInSuperClass(SymTable global, SymTable table, LinkedList<SymTable> tables, String name) {
		
		this.tables = tables;
		if (searchRecord(table, name) != null) {
			return table;
		}
		SymTableEntry entry = searchRecord(global, table.getTableName());
		if (entry == null || entry.getInheritedClassList().size() == 0) {
			return null;
		}
		for (SymTable symTable : entry.getInheritedClassList()) {
			if (searchRecord(symTable, name) != null) {
				return symTable;
			}
		}
		return null;
	}
	
	public boolean validateParamsCount(SymTable table, int paramCount) {
		
		if (table == null) {
			return false;
		}
		
		int tableParamsCount = 0;
		
		for (SymTableEntry symTableEntry : table.getEntries()) {
			
			if (symTableEntry.getCategory() == SymTableEntryCategory.Parameter) {
				tableParamsCount++;
			}
		}
		
		if (tableParamsCount == paramCount) {
			return true;
		}
		
		return false;
	}
	
	public boolean validateParams(SymTable table, int i, String type) {
		
		String tableEntryType = table.getEntries().get(i).getType();
		if (tableEntryType.equals(type)) {
			return true;
		}
		
		return false;
	}
}
