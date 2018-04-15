/**
 * Package holds the classes that performs the semantic analysis of the input
 */
package sematic;

import java.util.ArrayList;
import java.util.LinkedList;

import constants.CompilerEnum.SymTableEntryCategory;

/**
 * This class provides the attributes of the symbol table as well as 
 * the operations that can be performed on the tables.
 * @author krn-singh
 */
public class SymTable {

	private String tableName = null;
	private ArrayList<SymTableEntry> entries = new ArrayList<SymTableEntry>();
	private SymTable globalTable = null;
	private LinkedList<SymTable> tables = new LinkedList<SymTable>();
	private SymTable parent = null;
	private Integer tableSize = null;
	
	/**
	 * Constructor
	 */
	public SymTable() {
		tableName = null;
		parent = null;
		tableSize = null;
		entries = new ArrayList<SymTableEntry>();
	}
	
	/**
	 * Constructor
	 * 
	 * @param name Table Name
	 * @param parent Parent of the current table
	 */
	public SymTable(String name, SymTable parent) {
		tableName = name;
		entries = new ArrayList<SymTableEntry>();
		this.parent = parent;
		tableSize = 0;
	}
	
	/**
	 * Add an entry to the table
	 * 
	 * @param entry current entry to be added
	 */
	public void addEntry(SymTableEntry entry) {
		entries.add(entry);
	}
	
	/**
	 * Getter for table name
	 * 
	 * @return Table name
	 */
	public String getTableName() {
		return tableName;
	}
	
	/**
	 * Getter for parent of the table
	 * 
	 * @return parent of the table
	 */
	public SymTable getParent() {
		return parent;
	}
	
	/**
	 * Getter for table entries
	 * 
	 * @return Table entries
	 */
	public ArrayList<SymTableEntry> getEntries() {
		return entries;
	}

	/**
	 * Setter for Table entries
	 * 
	 * @param entries table entries
	 */
	public void setEntries(ArrayList<SymTableEntry> entries) {
		this.entries = entries;
	}

	/**
	 * Getter for list of tables
	 * 
	 * @return table list
	 */
	public LinkedList<SymTable> getTables() {
		return tables;
	}

	/**
	 * Setter for tables
	 * 
	 * @param tables list of tables
	 */
	public void setTables(LinkedList<SymTable> tables) {
		this.tables = tables;
	}

	/**
	 * Add a table to the table list
	 * 
	 * @param table Table to be added
	 */
	public void addTables(SymTable table) {
		tables.add(table);
	}

	/**
	 * Getter for table memory size
	 * 
	 * @return Memory size of the table
	 */
	public Integer getTableSize() {
		return tableSize;
	}

	/**
	 * Setter for table size
	 * 
	 * @param tableSize Memory size of the table
	 */
	public void setTableSize(Integer tableSize) {
		this.tableSize = tableSize;
	}

	/**
	 * Create the global table
	 * 
	 * @return the global symbol table
	 */
	public SymTable createGlobalTable() {
		globalTable = new SymTable("global", null);
		addTables(globalTable);
		
		return globalTable;
	}
	
	/**
	 * Create a Symbol table
	 * 
	 * @param name name of the table
	 * @param parent parent of the current table
	 * @return the newly created symbol table
	 */
	public SymTable createTable(String name, SymTable parent) {
		
		SymTable table = new SymTable(name, parent);
		addTables(table);
		
		return table;
	}
	
	/**
	 * Find a table
	 * 
	 * @param name name of the table
	 * @return the global symbol table
	 */
	public SymTable findTable(String name) {
		
		for (int i = 0; i < tables.size(); i++) {
			if (tables.get(i).getTableName().equals(name)) {
				return tables.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Search a record in the table
	 * 
	 * @param table Table to be searched
	 * @param name Table name
	 * @return searched entry
	 */
	public SymTableEntry searchRecord(SymTable table, String name) {
		
		ArrayList<SymTableEntry> current = table.getEntries();
		
		for (int i = 0; i < current.size(); i++) {
			if (current.get(i).getName().equals(name)) {
				return current.get(i);
			}
		}
		
		return null;						
	}
	
	/**
	 * Search a record in the table
	 * 
	 * @param table Table to be searched
	 * @param name Table name
	 * @param type Type of the record to be searched
	 * @return searched entry
	 */
	public SymTableEntry searchRecord(SymTable table, String name, String type) {
		
		ArrayList<SymTableEntry> current = table.getEntries();
		
		for (int i = 0; i < current.size(); i++) {
			if (current.get(i).getName().equals(name) && current.get(i).getType().equals(type)) {
				return current.get(i);
			}
		}
		
		return null;						
	}
	
	/**
	 * Search a record in the table
	 * 
	 * @param table Table to be searched
	 * @param name Table name
	 * @param category Category of the record to be searched
	 * @return searched entry
	 */
	public SymTableEntry searchRecord(SymTable table, String name, SymTableEntryCategory category) {
		
		ArrayList<SymTableEntry> current = table.getEntries();
		
		for (int i = 0; i < current.size(); i++) {
			if (current.get(i).getName().equals(name) && current.get(i).getCategory() == category) {
				return current.get(i);
			}
		}
		
		return null;						
	}
	
	/**
	 * Delete a record in the table
	 * 
	 * @param table Table to be searched
	 * @param name Record name
	 */
	public void deleteRecord(SymTable table, String name) {
		
		ArrayList<SymTableEntry> current = table.getEntries();
		
		for (int i = 0; i < current.size(); i++) {
			if (current.get(i).getName().equals(name)) {
				current.remove(i);
			}
		}
	}
	
	/**
	 * Search a record in the table
	 * 
	 * @param table Table to be searched
	 * @param name Record name
	 * @return true if record exists else false
	 */
	public boolean checkDeclInSameScope(SymTable table, String name) {
		
		if (searchRecord(table, name) != null) {
			return true;
		}
		
		return false;
	}

	/**
	 * Search a record in the table
	 * 
	 * @param table Table to be searched
	 * @param tables list of tables
	 * @param name Record name
	 * @return true if record exists else false
	 */
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
	
	/**
	 * Search a record in the table
	 * 
	 * @param global Global Table
	 * @param table Table to be searched
	 * @param tables list of tables
	 * @param name Record name
	 * @return true if record exists else false
	 */
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
	
	/**
	 * Search a record in the table
	 * 
	 * @param global Global Table
	 * @param table Table to be searched
	 * @param tables list of tables
	 * @param name Record name
	 * @return Symbol table which contains the required record
	 */
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
	
	/**
	 * Validate the parameter count of the function
	 * 
	 * @param table Table to be searched
	 * @param paramCount number of parameters of the function
	 * @return true if valid number of parameters else false
	 */
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
	
	/**
	 * Type checking of the parameter
	 * @param table Table to be searched
	 * @param i current parameter of the function
	 * @param type Type of the parameter
	 * @return true if parameter type is valid else false
	 */
	public boolean validateParams(SymTable table, int i, String type) {
		
		String tableEntryType = table.getEntries().get(i).getType();
		if (tableEntryType.equals(type)) {
			return true;
		}
		
		return false;
	}
}
