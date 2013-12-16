package com.karthik.lucene.twitter.demo;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Demo of a sample Lucene Indexer. Takes in two arguments. The directory in your filesystem where you want to index
 * and a tweet csv data file. Source of tweet data: http://help.sentiment140.com/for-students.
 *
 * Usage: java TweetIndexer <index directory> <csv data file>
 *
 * @author karthik.kumar
 */
public class TweetIndexer {

    protected static final String COMMA = "\",\"";
    protected static final String POLARITY = "polarity";
    protected static final String ID = "id";
    protected static final String DATE = "date";
    protected static final String QUERY = "qury";
    protected static final String USER = "user";
    protected static final String TEXT = "text";

    public static void main(String[] args) throws Exception {
        try {
            String indexDir = args[0];
            String dataFile = args[1];

            TweetIndexer tweetIndexer = new TweetIndexer();

            long start = System.currentTimeMillis();

            int count = tweetIndexer.index(new File(indexDir), new File(dataFile));

            System.out.print(String.format("Indexed %d documents in %d seconds", count, (System.currentTimeMillis() - start) / 1000));
        }
        catch (Exception e) {
            System.out.println("Usage: java TweetIndexer <index directory> <csv data file>");
        }
    }

    private int index(File indexDir, File dataFile) throws Exception {
        IndexWriter indexWriter = new IndexWriter(
                FSDirectory.open(indexDir),
                new IndexWriterConfig(Version.LUCENE_44, new KeywordAnalyzer()));
        
        int count = indexFile(indexWriter, dataFile);

        indexWriter.close();

        return count;
    }

    private int indexFile(IndexWriter indexWriter, File dataFile) throws IOException {
        FieldType fieldType = new FieldType();
        fieldType.setStored(true);
        fieldType.setIndexed(true);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(dataFile));
        String line = "";
        int count = 0;
        while ((line = bufferedReader.readLine()) != null) {
            // Hack to ignore commas within elements in csv (so we can split on "," rather than just ,)
            line = line.substring(1, line.length() - 1);
            String[] tweetInfo = line.split(COMMA);

            Document document = new Document();

            document.add(new Field(POLARITY, tweetInfo[0], fieldType));
            document.add(new Field(ID, tweetInfo[1], fieldType));
            document.add(new Field(DATE, tweetInfo[2], fieldType));
            document.add(new Field(QUERY, tweetInfo[3], fieldType));
            document.add(new StringField(USER, tweetInfo[4], Field.Store.YES));
            document.add(new StringField(TEXT, tweetInfo[5], Field.Store.YES));

            indexWriter.addDocument(document);
            count++;
        }
        return count;
    }
}
