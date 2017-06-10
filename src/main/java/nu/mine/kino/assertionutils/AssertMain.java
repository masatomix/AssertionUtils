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
//作成日: 2016/07/13

package nu.mine.kino.assertionutils;

import static nu.mine.kino.assertionutils.AssertUtils.assertEqualsDirWithoutException;
import static nu.mine.kino.assertionutils.AssertUtils.assertEqualsFileWithoutException;
import static nu.mine.kino.assertionutils.AssertUtils.assertExists;
import static nu.mine.kino.assertionutils.AssertUtils.assertFileExists;
import static nu.mine.kino.assertionutils.AssertUtils.isDirectory;

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * @author Masatomi KINO
 * @version $Revision$
 */
@Slf4j
public class AssertMain {

    public static void main(String[] args) {
        // print internal state
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);

        AssertMain main = new AssertMain();
        CmdLineParser parser = new CmdLineParser(main);
        // 前処理。コマンドライン引数のチェック
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            printUsage(parser);
            return;
        }

        try {
            main.init();
            main.execute();
        } finally {
            CacheManager manager = CacheManager.getInstance();
            manager.shutdown();
        }
    }

    private void init() {
        CacheManager manager = CacheManager.getInstance();
        String cacheName = "settingsCache";
        manager.addCache(cacheName);

        Cache settingsCache = manager.getCache(cacheName);
        String propertyFile = "assertUtils";
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(propertyFile);
            doSettings(settingsCache, bundle);
        } catch (java.util.MissingResourceException e) {
            String message = "設定ファイルが存在しません。必要ならクラスパス上に {}.propertiesを配置してください。({})";
            doSettings(settingsCache, ResourceBundle
                    .getBundle("nu.mine.kino.assertionutils.DefaultResources"));
            log.warn(message, propertyFile, e.getMessage());
        }

        List<String> keys = settingsCache.getKeys();
        for (String key : keys) {
            Element element = settingsCache.get(key);
            log.debug("key[{}]:{}", element.getObjectKey(),
                    element.getObjectValue());
        }
    }

    private void doSettings(Cache settingsCache, ResourceBundle bundle) {
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            settingsCache.put(new Element(key, bundle.getString(key)));
        }
    }

    private static void printUsage(CmdLineParser parser) {
        System.out.println("usage:");
        parser.printSingleLineUsage(System.out);
        System.out.println();
        System.out.println("対象ファイル名は -include xx を繰り返し指定することで複数ファイルを指定可能");
        System.out.println("除外ファイル名は -exclude xx を繰り返し指定することで複数ファイルを指定可能");
        System.out.println(
                "-includeと-excludeを両方指定した場合は、はじめに -include でマッチするものだけを抽出し、つぎに -exclude にマッチするものを除外します。");
        System.out.println("Windowsなどの場合は、-include \"*.log\" などと囲むようにしてください。");
        System.out.println();
        System.out.println(
                " 例: java -jar AssertionUtils-0.0.x-SNAPSHOT-jar-with-dependencies.jar -i /home/userA -o /home/userB -exclude .bashrc -exclude .bash_history");

        System.out.println();
        System.out.println("テキスト比較する場合(期待値・検証値ファイルともMS932で読み込む)");
        System.out.println(
                " 例: java -jar AssertionUtils-0.0.x-SNAPSHOT-jar-with-dependencies.jar -i /home/userA -o /home/userB -logic nu.mine.kino.assertionutils.TextAssertionLogic MS932");

        System.out.println("　※ -logic を記載する場合は、最後に書くようにしてください。");
        System.out.println();
        System.out.println(
                "CSV比較する場合(暫定処理として、半角スペースをデリミタとして分割して、先頭2列は除去してCSV項目比較)");
        System.out.println(
                " 例: java -jar AssertionUtils-0.0.x-SNAPSHOT-jar-with-dependencies.jar -i /home/userA -o /home/userB -logic nu.mine.kino.assertionutils.CSVAssertionLogic MS932");
        System.out.println("　※ -logic を記載する場合は、最後に書くようにしてください。");

    }

    @Option(name = "-i", metaVar = "期待値ディレクトリ", required = true, usage = "input file")
    private static String input;

    @Option(name = "-o", metaVar = "検証データディレクトリ", required = true, usage = "output file")
    private static String output;

    @Option(name = "-logic", metaVar = "検証ロジッククラス", handler = LogicHandler.class)
    private static Logic logic = new DefaultAssertionLogic(null);

    @Option(name = "-exclude", metaVar = "除外ファイル名", required = false, usage = "*.logなど除外ファイルを指定")
    private static String[] excludes;

    @Option(name = "-include", metaVar = "対象ファイル名", required = false)
    private static String[] includes;

    // @Option(name = "-xxxx", required = true)
    // private static String xxx;

    public void execute() {
        log.info("logic = {}", logic);
        log.info("検証データディレクトリ = {}", output);
        log.info("期待値ディレクトリ = {}", input);
        printArray("対象ファイル = {}", includes);
        printArray("除外ファイル = {}", excludes);
        log.info("------ 検証開始 ------");

        // Startのディレクトリの存在チェックを実施
        assertExists(input);
        assertExists(output);

        if (isDirectory(output)) {
            assertEqualsDirWithoutException(input, output, logic, includes,
                    excludes);
        } else {
            assertEqualsFileWithoutException(input, output, logic);
        }

        // ココに、期待値ファイルがあるのに、実績ファイルが存在しないチェックが必要だ。
        // 上の処理はActualのディレクトリを Visitorしたけど、
        // ExpectedをVisitorして、そんざいしないActualを警告出すか。
        // → 実装済み
        assertFileExists(input, output, excludes);
        log.info("------ 検証終了 ------");

    }

    private void printArray(String format, String... array) {
        if (array != null && array.length > 0) {
            for (String string : array) {
                log.info(format, string);
            }
        }
    }
}
