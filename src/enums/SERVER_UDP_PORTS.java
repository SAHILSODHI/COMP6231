package enums;

public enum SERVER_UDP_PORTS {
	
	TORaddEvent(6001), 
	TORremoveEvent(6002), 
	TORlistEventAvailability(6003), 
	TORbookEvent(6004), 
	TORcancelEvent(6005),
	TORgetBookingSchedule(6006), 
	TORswapEvent(6007),
	TORdeleteOldData(6008),
	MTLaddEvent(7001), 
	MTLremoveEvent(7002), 
	MTLlistEventAvailability(7003), 
	MTLbookEvent(7004), 
	MTLcancelEvent(7005),
	MTLgetBookingSchedule(7006), 
	MTLswapEvent(7007),
	MTLdeleteOldData(7008),
	OTWaddEvent(8001), 
	OTWremoveEvent(8002), 
	OTWlistEventAvailability(8003), 
	OTWbookEvent(8004), 
	OTWcancelEvent(8005),
	OTWgetBookingSchedule(8006), 
	OTWswapEvent(8007),
	OTWdeleteOldData(8008);
	
	public final Integer label;

	SERVER_UDP_PORTS(Integer label) {
		this.label = label;
	}
}
