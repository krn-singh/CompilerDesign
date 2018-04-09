package sematic;

import java.util.ArrayList;

import constants.CompilerEnum.SymTableEntryCategory;

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
	
	public SymTableEntry(String name, SymTableEntryCategory category, String type) {
		this.name = name;
		this.category = category;
		this.type = type;
		inheritedClassList = new ArrayList<SymTable>();
		arraySizeList = new ArrayList<String>();
	}
	
	public SymTableEntry(String name, SymTableEntryCategory category, String type, Integer lineNumber) {
		this.name = name;
		this.category = category;
		this.type = type;
		this.lineNumber = lineNumber;
		inheritedClassList = new ArrayList<SymTable>();
		arraySizeList = new ArrayList<String>();
	}
	
	public SymTableEntry(String name, SymTableEntryCategory category, SymTable link) {
		this.name = name;
		this.category = category;
		type = null;
		this.link = link;
		inheritedClassList = new ArrayList<SymTable>();
		arraySizeList = new ArrayList<String>();
	}
	
	public SymTableEntry(String name, SymTableEntryCategory category, String type, SymTable link) {
		this.name = name;
		this.category = category;
		this.type = type;
		this.link = link;
		inheritedClassList = new ArrayList<SymTable>();
		arraySizeList = new ArrayList<String>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SymTableEntryCategory getCategory() {
		return category;
	}

	public void setCategory(SymTableEntryCategory category) {
		this.category = category;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public SymTable getLink() {
		return link;
	}

	public void setLink(SymTable link) {
		this.link = link;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}

	public ArrayList<SymTable> getInheritedClassList() {
		return inheritedClassList;
	}

	public void setInheritedClassList(ArrayList<SymTable> inheritedClassList) {
		this.inheritedClassList = inheritedClassList;
	}

	public void addInheritedClass(SymTable inheritedClass) {
		inheritedClassList.add(inheritedClass);
	}
	
	public ArrayList<String> getArraySizeList() {
		return arraySizeList;
	}

	public void setArraySizeList(ArrayList<String> arraySizeList) {
		this.arraySizeList = arraySizeList;
	}

	public void addArraySize(String arraySize) {
		arraySizeList.add(arraySize);
	}
	
	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	public SymTableEntry createEntry(String name, SymTableEntryCategory category, String type) {
		
		SymTableEntry entry = new SymTableEntry(name, category, type);
		
		return entry;
	}
	
	public SymTableEntry createEntry(String name, SymTableEntryCategory category, String type, Integer lineNumber) {
		
		SymTableEntry entry = new SymTableEntry(name, category, type, lineNumber);
		
		return entry;
	}
	
	public SymTableEntry createEntry(String name, SymTableEntryCategory category, SymTable link) {
		
		SymTableEntry entry = new SymTableEntry(name, category, link);
		
		return entry;
	}
	
	public SymTableEntry createEntry(String name, SymTableEntryCategory category, String type, SymTable link) {
		
		SymTableEntry entry = new SymTableEntry(name, category, type, link);
		
		return entry;
	}
	
}