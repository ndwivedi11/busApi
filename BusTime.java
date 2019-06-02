package busApi;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import com.google.gson.*;

public class BusTime {
	public static URL url = null;
	public static String urlString;

	public static int busRouteID;
	public static int busDirID;
	public static String direction;

	public static String busRoute;
	public static String busStop;
	public static String busDir;

	public static String busStopID = "";
	public static String busTime = "";

	/**
	 * the main method will call methods for api invocations and time calculations
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// Check to make sure the program is run with three arguments
		if (args.length != 3) {
			System.out.println("Arguments not entered correctly.");
			System.exit(-1);
		}

		// set the value for route, stop and direction
		busRoute = args[0];
		busStop = args[1];
		busDir = args[2];

		// Dummy values for testing
		// can be commented
		busRoute = "METRO Red Line";
		busStop = "Mall of America Station";
		busDir = "south";

		// get routeID
		urlString = "http://svc.metrotransit.org/NexTrip/Routes?format=json";
		busRouteID = getBusDetails(urlString, "Description", "Route", busRoute);

		if (busRouteID == -1) {
			System.out.println(busRoute + " was not found.");
			System.exit(-1);
		}

		// convert direction to the format used in api
		if (busDir.equals("north")) {
			direction = "NORTHBOUND";
		} else if (busDir.equals("south")) {
			direction = "SOUTHBOUND";
		} else if (busDir.equals("east")) {
			direction = "EASTBOUND";
		} else if (busDir.equals("west")) {
			direction = "WESTBOUND";
		} else {
			System.out.println("Invalid direction entered");
			System.exit(-1);
		}

		// get direction Id by passing routeId returned before
		urlString = "http://svc.metrotransit.org/NexTrip/Directions/" + busRouteID + "?format=json";
		busDirID = getBusDetails(urlString, "Text", "Value", direction);

		if (busDirID == -1) {
			System.out.println(busDir + " is not available for the bus route." + busRoute);
			System.exit(-1);
		}

		// get busStopID String by passing routeId, dirId returned before
		urlString = "http://svc.metrotransit.org/NexTrip/Stops/" + busRouteID + "/" + busDirID + "?format=json";
		getBusDetails(urlString, "Text", "Value", busStop);

		if (busStopID.equals("")) {
			System.out.println(busStop + " was not found.");
			System.exit(-1);
		}

		// get time details from api by passing routeId,dirId and stop ID
		urlString = "http://svc.metrotransit.org/NexTrip/" + busRouteID + "/" + busDirID + "/" + busStopID
				+ "?format=json";
		getBusDetails(urlString, "RouteDirection", "DepartureTime", busTime);

		if (busTime.equals("")) {
			System.out.println(" Bus already left");
			System.exit(0);
		}

		// get the time and print the result
		getReachingTime();
	}

	/**
	 * @param apiUrl       the full String url where we are sending the GET request
	 *                     to in order to recieve a Json object
	 * @param propertyOne  used to search through the json object: RouteDirection,
	 *                     Text or Description
	 * @param propertyTwo  used to search through the json object: DepartureTime,
	 *                     Value, Route
	 * @param apiSearchVal used to ensure we return a String when using this method:
	 *                     route, dir, stop, timeStamp
	 * @return the int value we are searching for, -1 if FetchInformation fails and
	 *         0 if we return a string
	 */
	public static int getBusDetails(String apiUrl, String propertyOne, String propertyTwo, String apiSearchVal) {
		// set up a connection to get ready to send a GET request
		HttpURLConnection request;
		try {
			url = new URL(apiUrl);
			request = (HttpURLConnection) url.openConnection();
			request.setDoOutput(true);
			request.setRequestMethod("GET");

			// get the JSon Array
			request.connect();
			JsonParser jp = new JsonParser();
			JsonElement element = jp.parse(new InputStreamReader((InputStream) request.getInputStream()));

			// check if status is correct
			if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
				System.out.println(request.getErrorStream());
			}

			JsonArray jsonArrayObj = element.getAsJsonArray();

			// iterate array and check for the vale, return int or string value based on
			// input
			for (JsonElement obj : jsonArrayObj) {
				if (obj.getAsJsonObject().get(propertyOne).getAsString().contains(apiSearchVal)) {
					if (apiSearchVal.equals(busStop)) {
						busStopID = obj.getAsJsonObject().get(propertyTwo).getAsString();
						return 0;
					}
					if (apiSearchVal.equals(busTime)) {
						busTime = obj.getAsJsonObject().get(propertyTwo).getAsString();
						return 0;
					}
					return obj.getAsJsonObject().get(propertyTwo).getAsInt();
				}
			}
		} catch (IOException e) {
			System.out.println("IOException occured");
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * calculate time based on current time of the day
	 */
	public static void getReachingTime() {
		Date todayDate = new Date();
		// extract correct time
		busTime = busTime.substring(6, 19);
		Long longTime = Long.valueOf(busTime).longValue();

		// take the difference between the today time and the departure time (longTime).
		// Divide by 60000 to account for milliseconds and minutes
		long timeTillBus = (longTime - todayDate.getTime()) / 60000;
		if (timeTillBus > 1) {
			System.out.println(timeTillBus + " minutes");
		} else if (timeTillBus == 0) {
			System.out.println("Bus is leaving now");
		} else {
			System.out.println(" Bus left");
		}
	}

}
