package eu.hohenegger.infoscraper;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jaxen.JaxenException;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ContentFilter;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Fetch {
	private static final String TAGSOUP_SAX_DRIVER = "org.ccil.cowan.tagsoup.Parser";
	private static final String IMG_TAG = "img";
	private static final String HREF_ATTRIB = "href";
	private static final String LINK_TAG = "a";
	private static final String BODY_TAG = "body";
	private static final String HEADER_TAG = "h1";
	private static final String HEAD_TAG = "head";
	private static final String CONTAINER_CLASS = "meldung_wrapper";
	private static final String XHTML = "http://www.w3.org/1999/xhtml";

	/**
	 * @param args
	 * @throws IOException
	 * @throws JDOMException
	 * @throws JaxenException
	 */
	public static void main(String[] args) throws JDOMException, IOException,
			JaxenException {
		if (args.length == 0) {
			System.err.println("Parameter missing.");
			System.exit(1);
		}
		URL url = new URL(args[0]);

		scrapeUrl(url);
	}

	static void scrapeUrl(URL url) throws JDOMException, IOException,
			JaxenException {
		System.err.println("fetching " + url);

		Document doc = createDocument(url);

		DocType docType = doc.getDocType();

		Element meta = scrapeCharset(doc);

		List<Element> newsBody = scrapeContent(doc);

		ContentFilter contentFilter = new ContentFilter(ContentFilter.COMMENT);
		ElementFilter elementFilter = new ElementFilter(IMG_TAG);

		Element title = scrapeTitle(doc);
		Element body = createSkeleton(url, title);

		for (Element element : newsBody) {
			element.removeContent(contentFilter);
			element.removeContent(elementFilter);
			body.addContent(element.detach());
		}

		XMLOutputter xmlOut = createOutputter(doc);

		Element scrapedHtml = scrapeHtml(doc);
		scrapedHtml.removeContent();
		Element html = scrapedHtml;
		Element head = new Element(HEAD_TAG);

		html.addContent(head);
		html.addContent(body);
		head.addContent(meta.detach());
		head.addContent(title.detach());

		xmlOut.output(docType, System.out);
		xmlOut.output(html, System.out);
	}

	private static Document createDocument(URL url) throws JDOMException,
			IOException {
		SAXBuilder builder = new SAXBuilder(TAGSOUP_SAX_DRIVER); 
		Document doc = builder.build(url);
		return doc;
	}

	private static Element createSkeleton(URL url, Element title) {
		Element body = new Element(BODY_TAG);
		Element headLine = new Element(HEADER_TAG);
		Element headLineLink = new Element(LINK_TAG);
		body.addContent(headLine);
		headLine.addContent(headLineLink);
		headLineLink.addContent(title.getText());
		headLineLink.setAttribute(HREF_ATTRIB, url.toString());
		return body;
	}

	private static XMLOutputter createOutputter(Document doc)
			throws JaxenException {
		Format format = scrapeFormat(doc);
		XMLOutputter xmlOut = new XMLOutputter();
		xmlOut.setFormat(format);
		return xmlOut;
	}


	private static Element scrapeHtml(Document doc) throws JaxenException {
//		return new Element(HTML_TAG);
		JDOMXPath metaPath = new JDOMXPath("/h:html");
		metaPath.addNamespace("h", XHTML);
		Element meta = (Element) metaPath.selectSingleNode(doc);
		return meta;
	}

	private static List<Element> scrapeContent(Document doc)
			throws JaxenException {
		JDOMXPath newsPath = new JDOMXPath(String.format(
				"/h:html//h:div[@class='%s']/h:p", CONTAINER_CLASS));
		newsPath.addNamespace("h", XHTML);
		@SuppressWarnings("unchecked")
		List<Element> newsBody = newsPath.selectNodes(doc);
		return newsBody;
	}

	private static Element scrapeCharset(Document doc) throws JaxenException {
		JDOMXPath metaPath = new JDOMXPath("/h:html/h:head/h:meta[@charset]");
//		JDOMXPath metaPath = new JDOMXPath(
//				"/h:html/h:head/h:meta[@http-equiv='content-type']");
		metaPath.addNamespace("h", XHTML);
		Element meta = (Element) metaPath.selectSingleNode(doc);
		return meta;
	}

	private static Element scrapeTitle(Document doc) throws JaxenException {
		JDOMXPath titlePath = new JDOMXPath("/h:html/h:head/h:title");
		titlePath.addNamespace("h", XHTML);
		Element title = (Element) titlePath.selectSingleNode(doc);
		return title;
	}

	private static Format scrapeFormat(Document doc)
			throws JaxenException {
		JDOMXPath charsetAttributePath = new JDOMXPath("/h:html/h:head/h:meta/@charset");
		charsetAttributePath.addNamespace("h", XHTML);
		Attribute charset = (Attribute) charsetAttributePath.selectSingleNode(doc);
		String encoding = charset.getValue();
		Format format = Format.getRawFormat();
		format.setEncoding(encoding);
		return format;
	}
}
