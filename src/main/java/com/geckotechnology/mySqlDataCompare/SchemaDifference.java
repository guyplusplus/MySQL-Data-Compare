package com.geckotechnology.mySqlDataCompare;

public class SchemaDifference {
	
	enum Criticality {
		WARNING,
		ERROR
	}
	
	enum DifferenceType {
		INSTANCE_DIFFERENT_VERSION,
		TABLE_MISSING_IN_SLAVE_SCHEMA,
		TABLE_EXCESS_IN_SLAVE_SCHEMA,
		COLUMN_MISSING_IN_SLAVE_TABLE,
		COLUMN_EXCESS_IN_SLAVE_TABLE,
		NO_PRIMARY_KEY,
		COLUMN_DIFFERENT_TYPE,
		COLUMN_DIFFERENT_IS_NULLABLE,
		COLUMN_DIFFERENT_ORDINAL_POSITION,
		COLUMN_DIFFERENT_PRIMARY_KEY_ORDINAL_POSITION,
		COLUMN_DIFFERENT_PRIMARY_KEY,
		DATA_ROW_DIFFERENT_MD5,
		DATA_MISSING_ROW,
		DATA_ROW_IN_EXCESS
	}
	
	private Criticality criticality;
	private String objectName;
	private DifferenceType differenceType;
	private String note;
	
	public SchemaDifference(Criticality criticality, String objectName, DifferenceType differenceType, String note) {
		this(criticality, objectName, differenceType);
		this.note = note;
		if(note == null || note.length() == 0)
			throw new NullPointerException("note can not be null or empty");
	}
	
	public SchemaDifference(Criticality criticality, String objectName, DifferenceType differenceType) {
		this.criticality = criticality;
		this.objectName = objectName;
		this.differenceType = differenceType;
		if(objectName == null)
			throw new NullPointerException("differenceType can not be null");
	}
	
	public DifferenceType getDifferenceType() {
		return differenceType;
	}
	
	public Criticality getCriticality() {
		return criticality;
	}
	
	public void printDetails() {
		System.out.print("    " + criticality + " object:" + objectName + ", differenceType:" + differenceType);
		if(note != null && note.length() > 0)
			System.out.println(", note:" + note);
		else
			System.out.println();
	}
	
	/**
	 * @return true if all 4 attributes are equal
	 */
	public boolean equals(Object o) {
		if(o == null || !(o instanceof SchemaDifference))
			return false;
		SchemaDifference sd = (SchemaDifference)o;
		return sd.criticality == this.criticality &&
				sd.objectName.equals(this.objectName) &&
				sd.differenceType == differenceType &&
				(sd.note == null ? this.note == null : sd.note.equals(this.note));
	}
}
