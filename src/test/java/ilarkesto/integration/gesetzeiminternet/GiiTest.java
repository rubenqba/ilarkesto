package ilarkesto.integration.gesetzeiminternet;

import ilarkesto.law.Book;
import ilarkesto.law.BookCacheCollection;
import ilarkesto.law.BookIndex;
import ilarkesto.law.BookIndexCache;
import ilarkesto.law.BookRef;
import ilarkesto.law.Norm;
import ilarkesto.law.Searcher;
import ilarkesto.law.Searcher.SearchResultConsumer;
import ilarkesto.testng.ATest;

import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.Test;

public class GiiTest extends ATest {

	@Test
	public void testIndex() {
		BookIndexCache indexCache = getBookCaches().getBookIndexCache();
		indexCache.update(true);

		BookIndex index = indexCache.getPayload();
		assertNotEmpty(index.getBooks());

		BookRef binSchStrOAbweichV = index.getBookByCode("64. BinSchStrOAbweichV");
		assertNotNull(binSchStrOAbweichV);
		assertEquals(binSchStrOAbweichV.getTitle(),
			"Vierundsechzigste Verordnung zur vorübergehenden Abweichung von der Binnenschifffahrtsstraßen-Ordnung");

	}

	@Test
	public void searchBGB() {
		SearchResultConsumerImpl consumer = new SearchResultConsumerImpl();
		Searcher searcher = new Searcher("BGB", consumer, getBookCaches());
		searcher.run();

		assertContains(consumer.books, getBookIndex().getBookByCode("BGB"));
		assertTrue(consumer.norms.isEmpty());
	}

	@Test
	public void searchUrhg2() {
		SearchResultConsumerImpl consumer = new SearchResultConsumerImpl();
		BookCacheCollection bookCaches = getBookCaches();
		Searcher searcher = new Searcher("UrhG 10", consumer, bookCaches);
		searcher.run();

		BookRef urhgRef = getBookIndex().getBookByCode("UrhG");
		Book urhg = bookCaches.getBookCache(urhgRef).getPayload_ButUpdateIfNull();
		Norm urhg10 = urhg.getNormByCodeNumber("10");

		assertContains(consumer.books, urhgRef);
		assertContains(consumer.norms, urhg10);
	}

	@Test
	public void testStvg() {
		BookRef ref = getBookIndex().getBookByCode("StVG");
		Book book = getGii().loadBook(ref);
		assertEquals(book.getRef().getCode(), "StVG");
		assertEquals(book.getRef().getTitle(), "Straßenverkehrsgesetz");

		List<Norm> norms = book.getAllNorms();
		assertSize(norms, 99);

		Norm n1 = norms.get(0);
		assertStartsWith(n1.getTextAsString(), "(1) Kraftfahrzeuge ");
		assertContains(n1.getTextAsString(), "(2) Als Kraftfahrzeuge im Sinne ");
	}

	@Test
	public void testBgb() {
		BookRef ref = getBookIndex().getBookByCode("BGB");
		Book book = getGii().loadBook(ref);
		log.debug(book.getJson().toFormatedString());
		assertEquals(book.getRef().getCode(), "BGB");
		assertEquals(book.getRef().getTitle(), "Bürgerliches Gesetzbuch");
	}

	@Test
	public void testAabg() {
		BookRef ref = getBookIndex().getBookByCode("AABG");
		Book book = getGii().loadBook(ref);
		assertEquals(book.getRef().getCode(), "AABG");
		assertEquals(book.getRef().getTitle(),
			"Gesetz zur Begrenzung der Arzneimittelausgaben der gesetzlichen Krankenversicherung");

		assertSize(book.getAllNorms(), 4);
	}

	// --- ---

	private BookIndex getBookIndex() {
		BookIndexCache indexCache = getBookCaches().getBookIndexCache();
		indexCache.update(false);

		BookIndex index = indexCache.getPayload();
		assertNotEmpty(index.getBooks());
		return index;
	}

	private BookCacheCollection getBookCaches() {
		return new BookCacheCollection(getGii());
	}

	private GiiLawProvider getGii() {
		return new GiiLawProvider(getTestOutputFile("books"));
	}

	class SearchResultConsumerImpl implements SearchResultConsumer {

		private List<BookRef> books = new LinkedList<BookRef>();
		private List<Norm> norms = new LinkedList<Norm>();

		@Override
		public void onBookFound(BookRef book) {
			books.add(book);
			log.info("Book found:", book);
		}

		@Override
		public void onNormFound(Norm norm) {
			norms.add(norm);
			log.info("Norm found:", norm);
		}

		@Override
		public void onSearchFinished() {}

	}

}
