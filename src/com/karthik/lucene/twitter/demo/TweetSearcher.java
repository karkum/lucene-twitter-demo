package com.karthik.lucene.twitter.demo;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;

import static com.opower.lucene.twitter.demo.TweetIndexer.POLARITY;
import static com.opower.lucene.twitter.demo.TweetIndexer.TEXT;
import static com.opower.lucene.twitter.demo.TweetIndexer.USER;

/**
 * Demo of a sample Lucene Searcher. Takes the directory of the index as a command line argument and demos various types
 * of search queries. Also takes in the number of hits as an argument.
 *
 * Usage: java TweetSearcher <index directory> <num hits>
 * @author karthik.kumar
 */
public class TweetSearcher {

    public static void main(String[] args) throws Exception {
        try {
            String indexDir = args[0];
            int numHits = Integer.parseInt(args[1]);

            TweetSearcher tweetSearcher = new TweetSearcher();
            tweetSearcher.termSearch(new File(indexDir), numHits);
            tweetSearcher.wildcardQuery(new File(indexDir), numHits);
            tweetSearcher.booleanQuery(new File(indexDir), numHits);
        }
        catch (Exception e) {
            System.out.println("Usage: java TweetSearcher <index directory>");
        }
    }

    /**
     * Example of a TermQuery. Finds all tweets by the user "scotthamilton"
     */
    private void termSearch(File indexDir, int numHits) throws Exception {
        System.out.println("Find tweets by user @scotthamilton:");

        Directory directory = FSDirectory.open(indexDir);
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);

        Term term = new Term(USER, "scotthamilton");

        Query query = new TermQuery(term);

        TopDocs topDocs = indexSearcher.search(query, numHits);

        printResults(topDocs.scoreDocs, indexSearcher);
    }

    /**
     * Example of a wildcard query. Finds tweets that are replies (begin with an @).
     */
    private void wildcardQuery(File indexDir, int numHits) throws Exception {
        System.out.println("Find tweets that mention another user:");

        Directory directory = FSDirectory.open(indexDir);
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);

        Term term = new Term(TEXT, "*@*");
        Query query = new WildcardQuery(term);

        TopDocs topDocs = indexSearcher.search(query, numHits);

        printResults(topDocs.scoreDocs, indexSearcher);
    }

    /**
     * Example of a boolean query. Finds tweets that are positve AND include a hash tag.
     */
    private void booleanQuery(File indexDir, int numHits) throws Exception {
        System.out.println("Find tweets with a positive polarity that include a #hashtag:");

        Directory directory = FSDirectory.open(indexDir);
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);

        BooleanQuery booleanQuery = new BooleanQuery();

        Term term = new Term(TEXT, "*#*");
        Query query = new WildcardQuery(term);

        Term term1 = new Term(POLARITY, "4");
        Query query1 = new TermQuery(term1);

        booleanQuery.add(query, BooleanClause.Occur.MUST);
        booleanQuery.add(query1, BooleanClause.Occur.MUST);

        TopDocs topDocs = indexSearcher.search(booleanQuery, numHits);

        printResults(topDocs.scoreDocs, indexSearcher);
    }

    /**
     * Prints out the user and the content of the tweet.
     */
    private void printResults(ScoreDoc[] results, IndexSearcher indexSearcher) throws Exception {
        System.out.println("----------------------------------------------------------------------");
        for (int i = 0; i < results.length; i++) {
            int docId = results[i].doc;
            Document foundDocument = indexSearcher.doc(docId);
            System.out.println(foundDocument.get(USER) + " : " + foundDocument.get(TEXT));
        }
        System.out.println("Found " + results.length + " results");
        System.out.println("----------------------------------------------------------------------");
    }

}
