/**
 * Package holds the classes that performs the semantic analysis of the input
 */
package sematic;

import java.util.ArrayList;

import constants.CompilerEnum.SymTableEntryCategory;

/**
 * This class contains the attributes for the symbol table entry
 * 
 * @author krn-singh
 */
public class SymTableEntry {

	private String name;
	private SymTableEntryCategory category;
	private String type;
	private SymTable link;
	private Integer lineNumber;
	private ArrayList<SymTable> inheritedClassList;
	private ArrayList<String> arraySizeList;
	private String size;
	private String offset;

	/**
	 * Constructor
	 */
	public SymTableEntry() {
		name = null;
		category = null;
		type = null;
		link = null;
		lineNumber = null;
		inheritedClassList = new ArrayList<SymTable>();
		arraySizeList = new ArrayList<String>();
		size = null;
		offset = null;
	}
	
	/**
	 * Constructor
	 * 
	 * @param name Entry name
	 * @param category Entry category (Parameter, Variable, Function and Class)
	 * @param type Entry type (int, float and class type)
	 */
	public SymTableEntry(String name, SymTableEntryCategory category, String type) {
		this.name = name;
		this.category = category;
		this.type = type;
		inheritedClassList = new ArrayList<SymTable>();
		arraySizeList = new ArrayList<String>();
	}
	
	/**
	 * Constructor
	 * 
	 * @param name Entry name
	 * @param category Entry category (Parameter, Variable, Function and Class)
	 * @param type Entry type (int, float and class type)
	 * @param lineNumber Line in the input program corresponding to which entry was created
	 */
	public SymTableEntry(String name, SymTableEntryCategory category, String type, Integer lineNumber) {
		this.name = name;
		this.category = category;
		this.type = type;
		this.lineNumber = lineNumber;
		inheritedClassList = new ArrayList<SymTable>();
		arraySizeList = new ArrayList<String>();
	}
	
	/**
	 * Constructor
	 * 
	 * @param name Entry name
	 * @param category Entry category (Parameter, Variable, Function and Class)
	 * @param link Symbol table for an entry (eg. in case of member functions in class)
	 */
	public SymTableEntry(String name, SymTableEntryCategory category, SymTable link) {
		this.name = name;
		this.category = category;
		type = null;
		this.link = link;
		inheritedClassList = new ArrayList<SymTable>();
		arraySizeList = new ArrayList<String>();
	}
	
	/**
	 * Constructor
	 * 
	 * @param name Entry name
	 * @param category Entry category (Parameter, Variable, Function and Class)
	 * @param type Entry type (int, float and class type)
	 * @param link Symbol table for an entry (eg. in case of member functions in class)
	 */
	public SymTableEntry(String name, SymTableEntryCategory category, String type, SymTable link) {
		this.name = name;
		this.category = category;
		this.type = type;
		this.link = link;
		inheritedClassList = new ArrayList<SymTable>();
		arraySizeList = new ArrayList<String>();
	}
	
	/**
	 * Getter for entry name
	 * 
	 * @return Entry name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for entry name
	 * 
	 * @param name Entry name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for category (Parameter, Variable, Function and Class) of entry
	 * 
	 * @return Entry category
	 */
	public SymTableEntryCategory getCategory() {
		return category;
	}

	/**
	 *  Setter for category
	 *  
	 * @param category Entry category
	 */
	public void setCategory(SymTableEntryCategory category) {
		this.category = category;
	}

	/**
	 * Getter for Entry Type
	 * 
	 * @return Entry type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter for Entry Type
	 * 
	 * @param Entry type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter for Symbol table link of the entry
	 * 
	 * @return Symbol table for an entry (eg. in case of member functions in class)
	 */
	public SymTable getLink() {
		return link;
	}

	/**
	 * Setter for Symbol table link of the entry
	 * 
	 * @param Symbol table for an entry (eg. in case of member functions in class)
	 */
	public void setLink(SymTable link) {
		this.link = link;
	}

	/**
	 * Getter for Line number
	 * 
	 * @return Line number
	 */
	public Integer getLineNumber() {
		return lineNumber;
	}

	/**
	 * Setter for line number
	 * 
	 * @param lineNumber Line in the input program corresponding to which entry was created
	 */
	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * Getter for inherited class list
	 * 
	 * @return Inherited class list
	 */
	public ArrayList<SymTable> getInheritedClassList() {
		return inheritedClassList;
	}

	/**
	 * Setter for inherited class list
	 * 
	 * @param inheritedClassList Inherited class list
	 */
	public void setInheritedClassList(ArrayList<SymTable> inheritedClassList) {
		this.inheritedClassList = inheritedClassList;
	}

	/**
	 * Add a new class to the Inherited class list of an entry
	 * 
	 * @param inheritedClass Symbol table of the inherited class
	 */
	public void addInheritedClass(SymTable inheritedClass) {
		inheritedClassList.add(inheritedClass);
	}
	
	/**
	 * Getter for element list of an array
	 * 
	 * @return element list of an array
	 */
	public ArrayList<String> getArraySizeList() {
		return arraySizeList;
	}

	/**
	 * Setter for element list of an array
	 * 
	 * @param arraySizeList element list of an array
	 */
	public void setArraySizeList(ArrayList<String> arraySizeList) {
		this.arraySizeList = arraySizeList;
	}

	/**
	 * Add an index to the array size list
	 * 
	 * @param arraySize array index
	 */
	public void addArraySize(String arraySize) {
		arraySizeList.add(arraySize);
	}
	
	/**
	 * Getter for size of the variable
	 * 
	 * @return Size of the variable
	 */
	public String getSize() {
		return size;
	}

	/**
	 * Setter for size of the variable
	 * 
	 * @param size Size of the variable
	 */
	public void setSize(String size) {
		this.size = size;
	}

	/**
	 * Getter for memory offset of variable
	 * 
	 * @return memory offset of variable
	 */
	public String getOffset() {
		return offset;
	}

	/**
	 * Setter for memory offset of variable
	 * 
	 * @param offset memory offset of variable
	 */
	public void setOffset(String offset) {
		this.offset = offset;
	}

	/**
	 * Create an entry for the symbol table
	 * 
	 * @param name Entry name
	 * @param category Entry category (Parameter, Variable, Function and Class)
	 * @param type Entry type (int, float and class type)
	 * @return newly created table entry
	 */
	public SymTableEntry createEntry(String name, SymTableEntryCategory category, String type) {
		
		SymTableEntry entry = new SymTableEntry(name, category, type);
		
		return entry;
	}
	
	/**
	 * Create an entry for the symbol table
	 * 
	 * @param name Entry name
	 * @param category Entry category (Parameter, Variable, Function and Class)
	 * @param type Entry type (int, float and class type)
	 * @param lineNumber Line in the input program corresponding to which entry was created
	 * @return newly created table entry
	 */
	public SymTableEntry createEntry(String name, SymTableEntryCategory category, String type, Integer lineNumber) {
		
		SymTableEntry entry = new SymTableEntry(name, category, type, lineNumber);
		
		return entry;
	}
	
	/**
	 * Create an entry for the symbol table
	 * 
	 * @param name Entry name
	 * @param category Entry category (Parameter, Variable, Function and Class)
	 * @param link Symbol table for an entry (eg. in case of member functions in class)
	 * @return newly created table entry
	 */
	public SymTableEntry createEntry(String name, SymTableEntryCategory category, SymTable link) {
		
		SymTableEntry entry = new SymTableEntry(name, category, link);
		
		return entry;
	}
	
	/**
	 * Create an entry for the symbol table
	 * 
	 * @param name Entry name
	 * @param category Entry category (Parameter, Variable, Function and Class)
	 * @param type Entry type (int, float and class type)
	 * @param link Symbol table for an entry (eg. in case of member functions in class)
	 * @return newly created table entry
	 */
	public SymTableEntry createEntry(String name, SymTableEntryCategory category, String type, SymTable link) {
		
		SymTableEntry entry = new SymTableEntry(name, category, type, link);
		
		return entry;
	}
	
}