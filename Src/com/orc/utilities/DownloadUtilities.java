package com.orc.utilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Class which provides multi-threaded downloading facilities.
 *
 */
public class DownloadUtilities {
	
	/**
	 * Timeout for download HTTP connection.
	 */
	static int timeout = 20000;
	
	/**
	 * Set timeout to t
	 * @param t New timeout in millisecond.
	 */
	static public void setTimeout(int t) {
		timeout = t;
	}
	
	/**
	 * Download and parse in parallel html pages given by urls using maximal maxThreads threads.
	 * @param urls The urls to download
	 * @param maxThreads The maximum number of threads used for the downloading
	 * @return A table from urls to parsed DOM documents. Keys are urls; values are corresponding DOMs. If a page is not downloadable, the corresponding DOM is null.
	 */
	static public HashMap<String, Document> urlDocuments(HashSet<String> urls, int maxThreads, final Logger logger) {
		HashMap<String, Document> result = new HashMap<String, Document>();
		if(urls.size() <= maxThreads) {
			result = urlDocuments(urls, logger);
		} else {
			HashSet<String> parts = new HashSet<String>();
			for(String u : urls) {
				parts.add(u);
				if(parts.size()==maxThreads) {
					result.putAll(urlDocuments(parts, logger));
					parts = new HashSet<String>();
				}
			}
			if(parts.size() > 0) {
				result.putAll(urlDocuments(parts, logger));
			}
		}
		return result;
	}
	
	/**
	 * Download HTML pages from urls in parallel and parse them into DOM documents.
	 * @param urls The set of urls to download
	 * @return A table from urls to parsed DOM documents. Keys are urls; values are corresponding DOMs. If a page is not downloadable, the corresponding DOM is null.
	 */
	static public HashMap<String, Document> urlDocuments(HashSet<String> urls, final Logger logger) {
		HashMap<String, Document> result = new HashMap<String, Document>();
		LinkedList<Callable<HashMap<String, Document>>> tasks = new LinkedList<Callable<HashMap<String, Document>>>();
				
		for(final String u : urls) {
			tasks.add(new Callable<HashMap<String, Document>>() {
				@Override
				public HashMap<String, Document> call() throws Exception {
					return urlDocument(u, logger);
				}
			});
		}
		
		ExecutorService s = null;
		try{
			s = Executors.newFixedThreadPool(tasks.size());
			List<Future<HashMap<String, Document>>> doms = s.invokeAll(tasks);
			for(Future<HashMap<String, Document>> d : doms) {
				for(Entry<String, Document> p : d.get().entrySet()) {
					String u = p.getKey();
					Document c = p.getValue();
					if(!result.containsKey(u)) {
						result.put(u, c);
					}					
				}
			}
		}catch(Exception e) {
			Logging.logError(logger, e);
		}
		finally{
			if(s!=null) {
				s.shutdownNow();
			}
		}
		
		return result;
	}
	
	/**
	 * Download and parse the HTML page at url.
	 * @param url The url of the page to download.
	 * @return A table consisting only one key (the url) and its corresponding DOM. If the page is not downloadable, the value is null.
	 */
	static public HashMap<String, Document> urlDocument(String url, Logger logger) {
		Document dom = null;		
		try {
			dom = Jsoup.connect(url).timeout(timeout).get();
		} catch (IOException e) {
			dom = null;
			Logging.logError(logger, url, e);
		}

		HashMap<String, Document> result = new HashMap<String, Document>();
		result.put(url, dom);
		return result;
	}	

}
