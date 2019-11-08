package com.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import com.replicas.replicaOne.City;
import com.replicas.replicaOne.EventType;
import com.replicas.replicaOne.Logger;
import com.replicas.replicaOne.OperationResult;
import com.replicas.replicaOne.TimeSlot;

import com.fe.idl.compilation.*;

public class Client implements Runnable {
	
	private ArrayList<City> cities;
	private ArrayList<ClientRole> clientRoles;
	private ArrayList<EventType> eventTypes;
	private ArrayList<TimeSlot> timeSlots;
	private Scanner scanner;
	private Integer numberId;
	public String uniqueIdentifier;
	private City clientLocation;
	private ClientRole clientRole;
	private Logger log;
	public EventManagerClient eventManager;
	public CustomerClient customerClient;
	
	Client(){
		scanner = new Scanner(System.in);
		eventManager = null;
		customerClient = null;
		getCities();
		getClientRoles();
		getEventTypes();
		getTimeSlots();
	}
	
	public Client(City clientLocation, Integer idNumber, ClientRole clientRole){
		scanner = new Scanner(System.in);
		eventManager = null;
		customerClient = null;
		getCities();
		getClientRoles();
		getEventTypes();
		getTimeSlots();
		this.clientLocation = clientLocation;
		setNumberId(idNumber);
		this.clientRole = clientRole;
		setEventManagerOrCustomerClient(clientLocation, clientRole);
		generateIdentifier();
		initialiateLogger();
	}
	
	public void startClient(){
		System.out.println("Please provide the location for this client: ");
		printLocations();
		Integer locationSelection = retriveNumberFromUser(0 , cities.size(), "Please select an option:");
		clientLocation = cities.get(locationSelection - 1);
		Integer idNumber = retriveNumberFromUser(1000, 9999, "Please select a number id:");
		setNumberId(idNumber);
		System.out.println("Please select a role for this client: ");
		printClientRoles();
		Integer roleSelection = retriveNumberFromUser(0, clientRoles.size(), "Please select an option:");
		clientRole = clientRoles.get(roleSelection - 1);
		setEventManagerOrCustomerClient(clientLocation, clientRole);
		generateIdentifier();
		initialiateLogger();
		main();
	}
	
	private void main(){
		Integer actionSelection;
		Integer limitActions;
		
		if(clientRole == ClientRole.CUSTOMER){
			limitActions = 4;
		} else {
			limitActions = 7;
		}
		
		while(true){
			printUniqueIdentifier();
			printAvailableActions();
			actionSelection = retriveNumberFromUser(0, limitActions, "Please select an option:");
			performOperation(actionSelection);
		}
	}
	
	private void initialiateLogger(){
		log = new Logger(uniqueIdentifier);
	}
	
	
	private void performOperation(Integer operationNumber){
		String event = null;
		String[] events = null;
		switch(operationNumber){
		case 1:
			event = bookEvent();
			log.writeInTheLog(event, "BookEvent", uniqueIdentifier);
			System.err.println("Response: " + event);
			break;
		case 2:
			events = getBookingSchedule();
			printBookingSchedule(events);
			log.writeInTheLog(events, "BookingSchedule", uniqueIdentifier);
			break;
		case 3:
			event = cancelEvent();
			log.writeInTheLog(event, "CancelEvent", uniqueIdentifier);
			System.err.println("Response: " + event);
			break;
		case 4:
			event = swapEvent();
			log.writeInTheLog(event, "SwapEvent", uniqueIdentifier);
			System.err.println("Response: " + event);
			break;
		case 5:
			event = addEvent();
			log.writeInTheLog(event, "AddEvent", uniqueIdentifier);
			System.err.println("Response: " + event);
			break;
		case 6:
			event = removeEvent();
			log.writeInTheLog(event, "RemoveEvent", uniqueIdentifier);
			System.err.println("Response: " + event);
			break;
		case 7:
			 events = listEventAvailability();
			 printAvailability(events);
			 log.writeInTheLog(events, "ListEventAvailability", uniqueIdentifier);
			 break;
		}
	}
	
	private void setEventManagerOrCustomerClient(City clientLocation, ClientRole clientRole){
		Properties properties = System.getProperties();
		properties.put( "org.omg.CORBA.ORBInitialHost", "localhost" );
		properties.put( "org.omg.CORBA.ORBInitialPort", "1050" );
		
		switch (clientRole) {
		case CUSTOMER:
			try {
				ORB orb = ORB.init(new String [1], properties);
				org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
				NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
				customerClient = (CustomerClient) CustomerClientHelper.narrow(ncRef.resolve_str("FrontEnd"));
			} catch (InvalidName | NotFound | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName e) {
				System.err.println("Something went wrong with orb init");
			}
			break;
		case EVENT_MANAGER:
			try {
				ORB orb = ORB.init(new String [1], properties);
				org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
				NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
				eventManager = (EventManagerClient) EventManagerClientHelper.narrow(ncRef.resolve_str("FrontEnd"));
			} catch (InvalidName | NotFound | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName e) {
				System.err.println("Something went wrong with orb init");
			}
			break;
		default:
			break;
		}
	}
	
	public String addEvent(){
		String eventId = retrieveEventIdFromUser();
		printEventTypes();
		Integer eventTypeSelection = retriveNumberFromUser(0, eventTypes.size(), "Please select an event type:");
		EventType eventType = eventTypes.get(eventTypeSelection - 1);
		Integer bookingCapacity = retriveNumberFromUser(0, Integer.MAX_VALUE, "Please input the booking capacity:");	
		String result = "";
		if(clientLocation.getCityCoding().equals(eventId.substring(0, 3))){
			if(clientRole == ClientRole.EVENT_MANAGER){
				result = eventManager.addEvent(uniqueIdentifier, eventId, eventType.getEventType(), bookingCapacity);
			}
		} else {
			result = "You are not allowed to add events in other city";
		}
		return result.toUpperCase() + " " + eventId + " " + eventType.getEventType() + " Capacity: " + bookingCapacity;
	}
	
	public String removeEvent(){
		String eventId = retrieveEventIdFromUser();
		printEventTypes();
		Integer eventTypeSelection = retriveNumberFromUser(0, eventTypes.size(), "Please select an event type:");
		EventType eventType = eventTypes.get(eventTypeSelection - 1);
		String result = "";
		if(clientLocation.getCityCoding().equals(eventId.substring(0, 3))){
			if(clientRole == ClientRole.EVENT_MANAGER){
				result = eventManager.removeEvent(uniqueIdentifier, eventId, eventType.getEventType());
			}
		} else {
			result = "You are not allowed to remove events in other city";
		}
		return result.toUpperCase() + " " + eventId + " " + eventType.getEventType();
	}
	
	
	public String bookEvent(){
		String eventId = retrieveEventIdFromUser();
		System.out.println("Please select an event type:");
		printEventTypes();
		Integer eventTypeSelection = retriveNumberFromUser(0, eventTypes.size(), "Please select an option:");
		EventType eventType = eventTypes.get(eventTypeSelection - 1);
		String result = "";
		if(clientRole == ClientRole.EVENT_MANAGER){
			String customerId = retriveStringFromUser("Please provide the customer id:");
			result = eventManager.bookEvent(customerId, eventId, eventType.getEventType());
		} else {
			result = customerClient.bookEvent(uniqueIdentifier, eventId, eventType.getEventType());
		}
		return result.toUpperCase() + " " + eventId + " " + eventType.getEventType();
	}
	
	public String cancelEvent(){
		String eventId = retrieveEventIdFromUser();
		printEventTypes();
		Integer eventTypeSelection = retriveNumberFromUser(0, eventTypes.size(), "Please select an event type:");
		EventType eventType = eventTypes.get(eventTypeSelection - 1);
		String result = "";
		if(clientRole == ClientRole.EVENT_MANAGER){
			String customerId = retriveStringFromUser("Please provide the customer id:");
			result = eventManager.cancelEvent(customerId, eventId, eventType.getEventType());
		} else {
			result = customerClient.cancelEvent(uniqueIdentifier, eventId, eventType.getEventType());
		}
		return result + " " + eventId + " " + eventType.getEventType();
	}
	
	public String swapEvent(){
		System.out.println("OLD Event");
		String oldEventId = retrieveEventIdFromUser();
		System.out.println("Please select the OLD event type:");
		printEventTypes();
		Integer eventTypeSelection = retriveNumberFromUser(0, eventTypes.size(), "Please select an option:");
		EventType oldEventType = eventTypes.get(eventTypeSelection - 1);
		System.out.println("NEW event");
		String newEventId = retrieveEventIdFromUser();
		System.out.println("Please select the NEW event type:");
		printEventTypes();
		eventTypeSelection = retriveNumberFromUser(0, eventTypes.size(), "Please select an option:");
		EventType newEventType = eventTypes.get(eventTypeSelection - 1);
		String result = "";
		if(clientRole == ClientRole.EVENT_MANAGER){
			String customerId = retriveStringFromUser("Please provide the customer id:");
			result = eventManager.swapEvent(customerId, newEventId, newEventType.getEventType(), oldEventId, oldEventType.getEventType());
		} else {
			result = customerClient.swapEvent(uniqueIdentifier, newEventId, newEventType.getEventType(), oldEventId, oldEventType.getEventType());
		}
		return result.toUpperCase();
	}
	
	public String[] getBookingSchedule(){
		String[] eventResults = null;
		if(clientRole == ClientRole.EVENT_MANAGER){
			String customerId = retriveStringFromUser("Please provide the customer id:");
			eventResults = eventManager.getBookingSchedule(customerId);
		} else {
			eventResults = customerClient.getBookingSchedule(uniqueIdentifier);
		}
		return eventResults;
	}
	
	public String[] listEventAvailability(){
		String[] events = null;
		printEventTypes();
		Integer eventTypeSelection = retriveNumberFromUser(0, eventTypes.size(), "Please select an event type:");
		EventType eventType = eventTypes.get(eventTypeSelection - 1);
		if(clientRole == ClientRole.EVENT_MANAGER){
			events = eventManager.listEventAvailability(uniqueIdentifier, eventType.getEventType());
		}
		System.out.println(eventType.getEventType());
		return events;
		
	}
	
	private void printAvailableActions(){
		if(clientRole == ClientRole.CUSTOMER){
			System.out.println("\nYou are a CUSTOMER these are the available actions:\n");
		} else {
			System.out.println("\nYou are a EVENT MANAGER these are the available actions:\n");
		}
		
		System.out.println("\t1: Book event");
		System.out.println("\t2: Get booking schedule");
		System.out.println("\t3: Cancel event");
		System.out.println("\t4: Swap event");
		if(clientRole == ClientRole.EVENT_MANAGER){
			System.out.println("\t5: Add event");
			System.out.println("\t6: Remove event");
			System.out.println("\t7: List Event Availability\n");
		}
	}
	
	private void printUniqueIdentifier(){
		if(clientRole == ClientRole.CUSTOMER){
			System.out.println("\nYour customerID is: " + uniqueIdentifier);
		} else{
			System.out.println("\nYour managerID is: " + uniqueIdentifier);
		}
	}
	
	private void generateIdentifier(){
		uniqueIdentifier = clientLocation.getCityCoding() + clientRole.getClientRole() + getNumberId();
	}
	
	private void getClientRoles(){
		clientRoles = new ArrayList<ClientRole>();
		for(ClientRole clientRole : ClientRole.values()){
			clientRoles.add(clientRole);
		}
	}
	
	private void getTimeSlots(){
		timeSlots = new ArrayList<TimeSlot>();
		for(TimeSlot timeSlot : TimeSlot.values()){
			timeSlots.add(timeSlot);
		}
	}
	
	private void getCities(){
		cities = new ArrayList<City>();
		for(City city : City.values()){
			cities.add(city);
		}
	} 
	
	private void getEventTypes(){
		eventTypes = new ArrayList<EventType>();
		for(EventType type : EventType.values()){
			eventTypes.add(type);
		}
	}
	
	public void printAvailability(String[] events){
		System.err.println("Event(s) available.\n");
		System.err.flush();
		for(int i = 0; i < events.length; i++){
			System.out.println("\t" + events[i]);
		}
	}
	
	private void printBookingSchedule(String[] eventResults){
		System.err.println("You " + uniqueIdentifier + " have " + eventResults.length + " event(s) scheduled.\n");
		System.err.flush();
		for(int i = 0; i < eventResults.length; i++){
			System.out.println("\t" + eventResults[i]);
		}
	}
	
	private void printClientRoles(){
		System.out.println("");
		for(int i = 0; i < clientRoles.size(); i++){
			System.out.println("\t" + (i + 1) +":" + " " + clientRoles.get(i).name());
		}
		System.out.println("");
	}
	
	private void printLocations(){
		System.out.println("");
		for(int i = 0; i < cities.size(); i++){
			System.out.println("\t" + (i + 1) +":" + " " + cities.get(i).name());
		}
		System.out.println("");
	}
	
	private void printEventTypes(){
		System.out.println("");
		for(int i = 0; i < eventTypes.size(); i++){
			System.out.println("\t" + (i + 1) +":" + " " + eventTypes.get(i).name());
		}
		System.out.println("");
	}
	
	private Integer retriveNumberFromUser(Integer minLimit, Integer maxLimit, String message){
		Integer selectedOption = 0;
		do{
			System.out.println(message);
			if(scanner.hasNextInt()){
				selectedOption = scanner.nextInt();
			} else {
				scanner.next();
			}
		}while(selectedOption > maxLimit || selectedOption <= minLimit);
		return selectedOption;
	}
	
	private String retrieveEventIdFromUser(){
		boolean isValid = false;
		String eventId = " ";
		do{
			System.out.print("Please provide the event id: ");
			eventId = scanner.nextLine();
			try{
				String eventLocation = eventId.substring(0, 3);
				String eventTime = eventId.substring(3,4);
				Integer.parseInt(eventId.substring(4,6));
				Integer.parseInt(eventId.substring(6,8));
				Integer.parseInt(eventId.substring(8));
				if((eventLocation.equals(City.MONTREAL.getCityCoding()) || eventLocation.equals(City.TORONTO.getCityCoding()) || eventLocation.equals(City.OTTAWA.getCityCoding())) && 
						(eventTime.equals(TimeSlot.MORNING.getTimeSlotCoding()) || eventTime.equals(TimeSlot.AFTERNOON.getTimeSlotCoding()) || eventTime.equals(TimeSlot.EVENING.getTimeSlotCoding()))){
					isValid = true;
				} else {
					isValid = false;
				}
			} catch(Exception e){
				isValid = false;
			}
		} while(!isValid);
		
		return eventId;
	}

	private String retriveStringFromUser(String message){
		String input = "";
		System.out.println(message);
		do{
			if(scanner.hasNextLine()){
				input = scanner.nextLine();
			} else {
				scanner.next();
			}
		}while(input.trim().equals(""));
		
		return input;
	}

	public Integer getNumberId() {
		return numberId;
	}

	public void setNumberId(Integer numberId) {
		this.numberId = numberId;
	}
	
	public static void main(String[] args) {
		Client client = new Client();
		client.startClient();
	}
	
	@Override
	public void run() {
		/*
		if(clientRole == ClientRole.EVENT_MANAGER) {
			String result = eventManager.addEvent(uniqueIdentifier, "MTLM100819", EventType.SEMINAR.getEventType(), 4);
			System.out.println(result);
			result = eventManager.addEvent(uniqueIdentifier, "MTLA110819", EventType.TRADE_SHOW.getEventType(), 2);
			return;
		}
		
		Integer counter = 0;
		String eventResult = customerClient.bookEvent(uniqueIdentifier, "MTLM100819", EventType.SEMINAR.getEventType());
		log.writeInTheLog(eventResult, "BookEvent", uniqueIdentifier);
		if(eventResult.equals("Event has been booked")){
			counter++;
		}
		System.out.println(uniqueIdentifier + " Number of booked events: " + counter);
		try {
			Thread.sleep(500);
			eventResult = customerClient.swapEvent(uniqueIdentifier, "MTLA110819", EventType.TRADE_SHOW.getEventType(), "MTLM100819", EventType.SEMINAR.getEventType());
			System.out.println(uniqueIdentifier + " Result of swap: " + eventResult);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
		
	}	
}
