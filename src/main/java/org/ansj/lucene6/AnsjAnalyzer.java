package org.ansj.lucene6;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ansj.commons.Parameter;
import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.CrfLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.library.StopLibrary;
import org.ansj.library.SynonymsLibrary;
import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.recognition.impl.SynonymsRecgnition;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.SmartForest;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.commons.lang.util.logging.Log;
import org.nlpcn.commons.lang.util.logging.LogFactory;


public class AnsjAnalyzer extends Analyzer implements Parameter {
    public static final Log LOG = LogFactory.getLog();

    /**
     * 查询和创建索引时选择不同的分词方式
     */
    public static enum TYPE {
        BASE_ANSJ, // 细颗粒度分词
        INDEX_ANSJ, // 索引分词
        QUERY_ANSJ, // 精准分词
        DIC_ANSJ, // 词典优先分词
        NLP_ANSJ // NLP分词
    }

    /**
     * 分词类型
     */
    private Map<String, String> args;


    /**
     * @param filter 停用词
     */
    public AnsjAnalyzer(Map<String, String> args) {
        this.args = args;
    }


    public AnsjAnalyzer(TYPE type, String dics) {
        this.args = new HashMap<String, String>();
        args.put(ANALYZER_TYPE, type.name());
        args.put(DIC, dics);
    }


    public AnsjAnalyzer(TYPE type) {
        this.args = new HashMap<String, String>();
        args.put(ANALYZER_TYPE, type.name());
    }


    @Override
    protected TokenStreamComponents createComponents(String text) {
        BufferedReader reader = new BufferedReader(new StringReader(text));
        Tokenizer tokenizer = null;
        tokenizer = getTokenizer(reader, this.args);
        return new TokenStreamComponents(tokenizer);
    }


    /**
     * 获得一个tokenizer
     * 
     * @param reader
     * @param type
     * @param filter
     * @return
     */
    public static Tokenizer getTokenizer(Reader reader, Map<String, String> args) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("to create tokenizer " + args);
        }
        Analysis analysis = null;

        String temp = null;
        String type = args.get(ANALYZER_TYPE);

        if (type == null) {
            type = AnsjAnalyzer.TYPE.BASE_ANSJ.name();
        }

        try {
            switch (AnsjAnalyzer.TYPE.valueOf(type)) {// 不匹配会抛异常
            case BASE_ANSJ:
                analysis = new BaseAnalysis();
                break;
            case INDEX_ANSJ:
                analysis = new IndexAnalysis();
                break;
            case DIC_ANSJ:
                analysis = new DicAnalysis();
                break;
            case QUERY_ANSJ:
                analysis = new ToAnalysis();
                break;
            case NLP_ANSJ:
                analysis = new NlpAnalysis();
                if (StringUtil.isNotBlank(temp = args.get(CRF))) {
                    ((NlpAnalysis) analysis).setCrfModel(CrfLibrary.get(temp));
                }
                break;
            default:
                analysis = new BaseAnalysis();
            }
        }
        catch (Exception e) {
            LOG.error(String.format("不存在的type类型 ：%s ", type));
            analysis = new BaseAnalysis();// 默认
        }

        if (reader != null) {
            analysis.resetContent(reader);
        }

        if (StringUtil.isNotBlank(temp = args.get(DIC))) { // 用户自定义词典(逗号分隔)
            String[] split = temp.split(",");
            Forest[] forests = new Forest[split.length];
            for (int i = 0; i < forests.length; i++) {
                if (StringUtil.isBlank(split[i])) {
                    continue;
                }
                forests[i] = DicLibrary.get(split[i]);
            }
            analysis.setForests(forests);
        }

        List<StopRecognition> filters = null;
        if (StringUtil.isNotBlank(temp = args.get(STOP))) { // 用户自定义词典(逗号分隔)
            String[] split = temp.split(",");
            filters = new ArrayList<StopRecognition>();
            for (String key : split) {
                StopRecognition stop = StopLibrary.get(key.trim());
                if (stop != null)
                    filters.add(stop);
            }
        }

        List<SynonymsRecgnition> synonyms = null;
        if (StringUtil.isNotBlank(temp = args.get(SYNONYMS))) { // 同义词词典(逗号分隔)
            String[] split = temp.split(",");
            synonyms = new ArrayList<SynonymsRecgnition>();
            for (String key : split) {
                SmartForest<List<String>> sf = SynonymsLibrary.get(key.trim());
                if (sf != null)
                    synonyms.add(new SynonymsRecgnition(sf));
            }
        }

        if (StringUtil.isNotBlank(temp = args.get(AmbiguityLibrary.DEFAULT))) { // 歧义词典
            analysis.setAmbiguityForest(AmbiguityLibrary.get(temp.trim()));
        }

        if (StringUtil.isNotBlank(temp = args.get(IS_NAME_RECOGNITION))) { // 是否开启人名识别
            analysis.setIsNameRecognition(Boolean.valueOf(temp));
        }

        if (StringUtil.isNotBlank(temp = args.get(IS_NUM_RECOGNITION))) { // 是否开启数字识别
            analysis.setIsNumRecognition(Boolean.valueOf(temp));
        }

        if (StringUtil.isNotBlank(temp = args.get(IS_QUANTIFIER_RECOGNITION))) { // 量词识别(是否数字和量词合并)
            analysis.setIsQuantifierRecognition(Boolean.valueOf(temp));
        }

        if (StringUtil.isNotBlank(temp = args.get(IS_REAL_NAME))) { // 是否保留原字符
            analysis.setIsRealName(Boolean.valueOf(temp));
        }

        return new AnsjTokenizer(analysis, filters, synonyms);

    }

}