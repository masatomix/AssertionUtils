/******************************************************************************
 * Copyright (c) 2014 Masatomi KINO and others. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *      Masatomi KINO - initial API and implementation
 * $Id$
 ******************************************************************************/
//作成日: 2016/07/19

package nu.mine.kino.assertionutils;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.spi.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * 基本的には{@link TextAssertionLogic}と同様の比較を行うが、各行を、タブでデリミタした可変長ファイルと見なし、
 * さらに設定に記述されたカラム番号(0始まり)の列は除外して、テキスト比較する。 たとえば、日付列などがあって、そこは比較除外としたいなどを想定。
 * 
 * @author Masatomi KINO
 * @version $Revision$
 * @see TextAssertionLogic
 */
@Slf4j
public class CSVAssertionLogic3 extends TextAssertionLogic {

    private static final Logger slogger = LoggerFactory
            .getLogger("forStackTrace");

    private Map<String, Integer[]> map = new HashMap<String, Integer[]>();

    public CSVAssertionLogic3(Parameters params) throws AssertionError {
        super(params);
        createSettings();
    }

    /**
     * propertiesファイルから設定情報を読み込んでおく。
     */
    private void createSettings() {
        ResourceBundle excludeResourceBundle = null;
        try {
            excludeResourceBundle = ResourceBundle.getBundle("excludeColumns");
            Enumeration<String> keys = excludeResourceBundle.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                String[] split = excludeResourceBundle.getString(key)
                        .split(",");
                map.put(key, toInteger(split));
            }
        } catch (java.util.MissingResourceException e) {
            String message = "除外カラムを指定するファイルが見つかりません。除外カラムはナシという挙動で継続しますが、必要ならクラスパス上に excludeColumns.propertiesを配置してください。({})";
            log.warn(message, e.getMessage());
        }
    }

    // String[] -> Integer[]
    private Integer[] toInteger(String[] split) {
        Integer[] ret = new Integer[split.length];
        for (int i = 0; i < split.length; i++) {
            ret[i] = Integer.parseInt(split[i]);
        }
        return ret;
    }

    // Integer[] -> int[]
    private int[] toInteger(Integer[] split) {
        int[] ret = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            ret[i] = split[i];
        }
        return ret;
    }

    /**
     * 実際に、カラムを除外する処理はココ。
     * 
     * @see nu.mine.kino.assertionutils.TextAssertionLogic#readText(java.nio.file.Path,
     *      java.nio.charset.Charset)
     */
    @Override
    public List<String> readText(Path path, Charset enc) {

        List<String> retLines = null;
        List<String> tmpLines = null;
        try {
            retLines = Files.readAllLines(path, enc);
            // 以下の処理は、除外カラムの設定ファイルに、該当ファイルがあった場合の列除外処理
            String key = path.toFile().getName();
            if (this.map.containsKey(key)) {
                int[] columns = toInteger(map.get(key));
                log.debug("{} は除外カラム設定があります{}", path, columns);
                tmpLines = removeColumn(retLines, columns);
            }
        } catch (IOException e) {
            slogger.error("このファイルの読み込みでエラー: {},({})", path, e.getMessage());
            slogger.error("", e);
            String message = "「java.nio.charset.MalformedInputException: Input length = xx」 \nというエラーの場合は、"
                    + "指定した文字エンコーディングと読み込んだファイルのそれとの相違であることが多いので、"
                    + "文字エンコーディングをチェックしましょう。";
            slogger.error(message);
            fail(e.getMessage());
        }

        // 列除外処理をした場合、こちらのオブジェクトがNULLでないので、そちらを返す。
        if (tmpLines != null) {
            retLines = tmpLines;
        }

        // ファイル出力。
        String newName = path.toFile().getName() + AssertUtils.getModifiedExt();
        String parentPath = path.getParent().toFile().getAbsolutePath();
        writeFile(Paths.get(parentPath, newName), retLines, enc);
        return retLines;

    }



    /**
     * 指定したファイルを出力
     * 
     * @param path
     * @param lines
     * @param enc
     */
    private void writeFile(Path path, List<String> lines, Charset enc) {
        try {
            Files.write(path, lines, enc);
        } catch (IOException e) {
            slogger.error("このファイルの読み込みでエラー: {},({})", path, e.getMessage());
            slogger.error("", e);
            fail(e.getMessage());
        }
    }

    private List<String> removeColumn(List<String> originals, int[] columns) {
        List<String> removedLines = new ArrayList<String>();
        for (String line : originals) {
            removedLines.add(removeColumn(line, columns));
        }
        return removedLines;
    }

    private String removeColumn(String line, int[] columns) {
        String delimiter = "\t";
        String[] originalStrs = StringUtils.splitPreserveAllTokens(line,
                delimiter);
        // Max探すのメンドイのでとりあえず一番右。
        log.debug("指定長:{}", columns[columns.length - 1]);
        log.debug("カラム数:{}", originalStrs.length);
        if (columns[columns.length - 1] >= originalStrs.length) {
            log.info("このレコードはカラム数が、除外列番号より少ないです。{}", line);
            return line;
        }
        String[] removedStrs = ArrayUtils.removeAll(originalStrs, columns);
        String returnStr = StringUtils.join(removedStrs, delimiter);
        return returnStr;

    }

}
