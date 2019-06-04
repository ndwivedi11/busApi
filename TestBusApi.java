/**
 * 
 */
package busApi;

import static org.junit.Assert.*;
import org.junit.Test; 

/**
 * @author dwivedin
 *
 */
public class TestBusApi {
	
	public void testGetBusDetails() {
		
		assertEquals(12,BusTime.getBusDetails("http://svc.metrotransit.org/NexTrip/Routes?format=json", "Description", "Route", "METRO Red Line"));
		
	}
}
