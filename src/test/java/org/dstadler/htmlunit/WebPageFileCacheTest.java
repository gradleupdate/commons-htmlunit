package org.dstadler.htmlunit;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.dstadler.commons.http.NanoHTTPD;
import org.dstadler.commons.testing.MemoryLeakVerifier;
import org.dstadler.commons.testing.MockRESTServer;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class WebPageFileCacheTest {
    private static final String AMAZON_URL = "https://www.amazon.de/dp/B009S4DVI2";

    private final MemoryLeakVerifier verifier = new MemoryLeakVerifier();

    @After
    public void tearDown() {
        verifier.assertGarbageCollected();
    }

    @Test
    public void testHandle() throws Exception {
        try (MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, "text/html", "<html><body><a href=\"http://www.google.at/\">link</a></body></html")) {
            try (WebClient webClient = HtmlUnitUtils.createWebClient(false)) {
                verifier.addObject(webClient);

                WebPageFileCache cache = new WebPageFileCache();

                // make sure the cache is empty initially
                cache.clear();

                // load the file the first time
                HtmlPage page = cache.handle(webClient, "http://localhost:" + server.getPort());
                checkLink(page);

                // load a second time, now from the cache
                page = cache.handle(webClient, "http://localhost:" + server.getPort());
                checkLink(page);

                verifier.addObject(page);
            }
        }
    }

    @Test
    public void testHandleComplexPage() throws Exception {
        try (WebClient webClient = HtmlUnitUtils.createWebClient(false)) {
            verifier.addObject(webClient);

            WebPageFileCache cache = new WebPageFileCache();

            // make sure the cache is empty initially
            cache.clear();

            // load the file the first time
            HtmlPage page = cache.handle(webClient, AMAZON_URL);
            //checkAmazon(page);
            assertNotNull(page);

            // load a second time, now from the cache
            page = cache.handle(webClient, AMAZON_URL);
            //checkAmazon(page);
            assertNotNull(page);

            verifier.addObject(page);
        }
    }

    @Test
    public void testHandleComplexPage2() throws Exception {
        try (WebClient webClient = HtmlUnitUtils.createWebClient(false)) {
            verifier.addObject(webClient);

            WebPageFileCache cache = new WebPageFileCache();

            // make sure the cache is empty initially
            cache.clear();

            // load the file the first time
            HtmlPage page = cache.handle(webClient, "https://www.amazon.de/Tr%C3%A4umeland-T015231-Babymatratze-Fr%C3%BChlingsluft-wei%C3%9F/dp/B009S4DVI2/ref=sr_1_1/261-6670114-6177027?ie=UTF8&amp;qid=1546893038&amp;sr=8-1&amp;keywords=tr%C3%A4umeland%2BFr%C3%BChlingsluft");
            //checkAmazon(page);
            assertNotNull(page);

            // load a second time, now from the cache
            page = cache.handle(webClient, "https://www.amazon.de/Tr%C3%A4umeland-T015231-Babymatratze-Fr%C3%BChlingsluft-wei%C3%9F/dp/B009S4DVI2/ref=sr_1_1/261-6670114-6177027?ie=UTF8&amp;qid=1546893038&amp;sr=8-1&amp;keywords=tr%C3%A4umeland%2BFr%C3%BChlingsluft");
            //checkAmazon(page);
            assertNotNull(page);

            verifier.addObject(page);
        }
    }

    /*private static void checkAmazon(HtmlPage page) throws HtmlUnitException {
        assertNotNull(page);
        DomNodeList<DomElement> hrefs = page.getElementsByTagName("a");
        assertFalse(hrefs.isEmpty());

        final List<HtmlSpan> priceSpans = HtmlUnitUtils.getElementsByAttribute(page, "span", "class", "price", HtmlSpan.class);
        priceSpans.addAll(HtmlUnitUtils.getElementsByAttribute(page, "div", "class", "price", HtmlSpan.class));

        priceSpans.addAll(HtmlUnitUtils.getElementsByAttributeContains(page, "span", "class", "a-color-price", HtmlSpan.class));
        priceSpans.addAll(HtmlUnitUtils.getElementsByAttributeContains(page, "div", "class", "a-color-price", HtmlSpan.class));

        priceSpans.addAll(HtmlUnitUtils.getElementsByAttributeContains(page, "span", "class", "olpOfferPrice", HtmlSpan.class));
        priceSpans.addAll(HtmlUnitUtils.getElementsByAttributeContains(page, "div", "class", "olpOfferPrice", HtmlSpan.class));

        for(HtmlSpan span : priceSpans) {
            DomNode seller = span.getParentNode().getParentNode();
            for (HtmlElement element : seller.getHtmlElementDescendants()) {
                if (element instanceof HtmlAnchor) {
                    String href = element.getAttribute("href");
                    if (href.contains("A3TN1BADY8I80N")) {
                        // done!
                        return;
                    }
                }
            }
        }
        System.out.println(page.asXml());
        fail("Could not find link to shop baby-direkt at " + AMAZON_URL + ", had " + priceSpans.size() + " spans/divs");
    }*/

    private static void checkLink(HtmlPage page) {
        assertNotNull(page);
        DomNodeList<DomElement> hrefs = page.getElementsByTagName("a");
        assertEquals(1, hrefs.size());

        assertEquals("http://www.google.at/", ((HtmlAnchor)hrefs.get(0)).getHrefAttribute());
    }
}
