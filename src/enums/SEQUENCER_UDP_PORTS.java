package enums;

public enum SEQUENCER_UDP_PORTS {

	addEvent(4001), 
	removeEvent(4002), 
	listEventAvailability(4003), 
	bookEvent(4004), 
	cancelEvent(4005),
	getBookingSchedule(4006), 
	swapEvent(4007),
	deleteOldData(4008);

	public final Integer label;

	SEQUENCER_UDP_PORTS(Integer label) {
		this.label = label;
	}

}
