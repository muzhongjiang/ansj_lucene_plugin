package org.ansj.solr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import org.nlpcn.commons.lang.util.logging.Log;
import org.nlpcn.commons.lang.util.logging.LogFactory;


/**
 * Copyright (C),MZJ<br>
 * 
 * 供参考(不要用！！！)
* 
* @author muzhongjiang
* @date 2017年9月21日
*/
public class AnsjTokenizerFactory extends TokenizerFactory {
    public static final Log logger = LogFactory.getLog();


    boolean pstemming;
    boolean isQuery;
    private String stopwordsDir;
    public List<StopRecognition> filters;

    public AnsjTokenizerFactory(Map<String, String> args) {
        super(args);
        filters = new ArrayList<StopRecognition>();
        getLuceneMatchVersion();
        isQuery = getBoolean(args, "isQuery", true);
        pstemming = getBoolean(args, "pstemming", false);
        stopwordsDir = get(args, "stopwords");
        addStopwords(stopwordsDir);
    }

    // add stopwords list to filter
    private void addStopwords(String dir) {
        if (dir == null) {
            logger.info("no stopwords dir");
            return;
        }

        // read stoplist
        logger.info("stopwords: " + dir);
        File file = new File(dir);
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader br = new BufferedReader(reader);
            StopRecognition testFilter = new StopRecognition();
            String word = br.readLine();
            while (word != null) {
                testFilter.insertStopWords(word);
                word = br.readLine();
            }

            filters.add(testFilter);

            br.close();
        } catch (FileNotFoundException e) {
            logger.info("No stopword file found");

        } catch (IOException e) {
            logger.info("stopword file io exception");
        }
    }

    @Override
    public Tokenizer create(AttributeFactory factory) {
        if (isQuery == true) {
            // query
            return new AnsjTokenizer(new ToAnalysis(), filters, null);
        } else {
            // index
            return new AnsjTokenizer(new IndexAnalysis(), filters, null);
        }
    }
}