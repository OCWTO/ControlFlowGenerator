package model;

public enum StatementTypes
{
	IFTYPE(25),
	WHILETYPE(61),
	RETURNTYPE(41),
	SWITCHTYPE(50),
	THROWTYPE(53),
	FORTYPE(24),
	SWITCHCASE(49),
	DOTYPE(19);
	
	private int statementType;
	
	private StatementTypes(int statementType){
		this.statementType = statementType;
	}

	public int getStatementType() {
		return statementType;
	}
}