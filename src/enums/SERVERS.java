package enums;

public enum SERVERS {

	TOR(9000), 
	MTL(9001), 
	OTW(9002);

	public final Integer label;

	SERVERS(Integer label) {
		this.label = label;
	}

}
