package com.geckotechnology.mySqlDataCompare;

public class OneRow {

	private String pk;
	private String md5;
	
	public OneRow(String pk, String md5) {
		this.pk = pk;
		this.md5 = md5;
	}

	public String getPk() {
		return pk;
	}

	public String getMd5() {
		return md5;
	}

	/**
	 * @return true if the PKs are equal. MD5 value is not checked
	 */
	public boolean equals(Object o) {
		if(o == null || !(o instanceof OneRow))
			return false;
		return pk.equals(((OneRow)o).getPk());
	}
	
}
