package org.right_brothers.utils;

import java.util.List;
import java.util.Vector;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.right_brothers.data.models.Client;
import org.right_brothers.data.models.Meta;
import org.right_brothers.data.models.Bakery;

import com.fasterxml.jackson.core.type.TypeReference;

public class InputParserTest {
	@Test
	public void testClientsParse() throws Throwable {
		InputParser<Vector<Client>> parser = new InputParser<>
			("/config/sample/clients.json", new TypeReference<Vector<Client>>(){});
		
		List<Client> clients = parser.parse();
		
		assertEquals(clients.size(), 1);
		assertEquals(clients.get(0).getGuid(), "customer-001");
		assertEquals(clients.get(0).getName(), "King's Landing Shop");
	}
	
	@Test
	public void testMetaParse() throws Throwable {
		InputParser<Meta> parser = new InputParser<>
			("/config/sample/meta.json", new TypeReference<Meta>(){});
		
		Meta meta = parser.parse();
		
		assertEquals(meta.getBakeries(), 2);
		assertEquals(meta.getCustomers().size(), 3);
	}

	@Test
	public void testBakeriesParse() throws Throwable {
		InputParser<Vector<Bakery>> parser = new InputParser<>
			("/config/sample/bakeries.json", new TypeReference<Vector<Bakery>>(){});
		
		List<Bakery> bakeries = parser.parse();
		
		assertEquals(bakeries.size(), 1);
		assertEquals(bakeries.get(0).getGuid(), "bakery-001");
		assertEquals(bakeries.get(0).getName(), "Sunspear Bakery");
	}
}
