package eu.hohenegger.infoscraper;

import java.io.IOException;
import java.net.URL;

import org.jaxen.JaxenException;
import org.jdom.JDOMException;
import org.junit.Test;

public class FetchTest {

	@Test
	public void test() throws JaxenException, JDOMException, IOException {
		URL url = new URL(
				"http://www.heise.de/security/meldung/CCC-knackt-Staatstrojaner-1357670.html");
		Fetch.scrapeUrl(url);
	}

}
