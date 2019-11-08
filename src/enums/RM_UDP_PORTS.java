package enums;

public enum RM_UDP_PORTS {
	
	addEvent(5001), 
	removeEvent(5002), 
	listEventAvailability(5003), 
	bookEvent(5004), 
	cancelEvent(5005),
	getBookingSchedule(5006), 
	swapEvent(5007),
	listenForFaultyMessage(5008),
	errorAcknowledgements(5009),
	deleteOldData(5010);
	
	public final Integer label;

	RM_UDP_PORTS(Integer label) {
		this.label = label;
	}
	
}
