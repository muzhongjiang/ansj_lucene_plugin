package org.ansj.commons;

/**
 * Copyright (C),MZJ<br>
 * 
 * lucene、solr schema文件配置参数
 * 
 * @author muzhongjiang
 * @date 2017年9月21日
 */
public interface Parameter {
    /**
     * 分词方式
     */
    public static final String ANALYZER_TYPE = "type";

    /**
     * CRF模型
     */
    public static final String CRF = "crf";

    /**
     * 用户自定义扩展词词库(逗号分隔)
     */
    public static final String DIC = "dic";

    /**
     * 用户自定义停用词词库(逗号分隔)
     */
    public static final String STOP = "stop";

    /**
     * 用户自定义同义词词库(逗号分隔)
     */
    public static final String SYNONYMS = "synonyms";

    /**
     * 用户自定义歧义词词库(逗号分隔)
     */
    public static final String AMBIGUITY = "ambiguity";

    /**
     * 是否开启人名识别
     */
    public static final String IS_NAME_RECOGNITION = "isNameRecognition";

    /**
     * 是否开启数字识别
     */
    public static final String IS_NUM_RECOGNITION = "isNumRecognition";

    /**
     * 是否量词识别(是否数字和量词合并)
     */
    public static final String IS_QUANTIFIER_RECOGNITION = "isQuantifierRecognition";

    /**
     * 是否保留原字符
     */
    public static final String IS_REAL_NAME = "isRealName";

}
