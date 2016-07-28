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

import static nu.mine.kino.assertionutils.AssertUtils.*;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Masatomi KINO
 * @version $Revision$
 */
public class AssertMain {
    private static final Logger logger = LoggerFactory
            .getLogger(AssertMain.class);

    public static void main(String[] args) {
        // print internal state
        // LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        // StatusPrinter.print(lc);

        // try (ConfigurableApplicationContext context = new
        // ClassPathXmlApplicationContext(
        // "applicationContext.xml")) {
        AssertMain main = new AssertMain();
        // AssertMain main = context.getBean(AssertMain.class);
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            printUsage(parser);
            return;
        }
        main.execute();
        // }
    }

    private static void printUsage(CmdLineParser parser) {
        System.out.println("usage:");
        parser.printSingleLineUsage(System.out);
        System.out.println();
        System.out.println("対象ファイル名は -include xx を繰り返し指定することで複数ファイルを指定可能");
        System.out.println("除外ファイル名は -exclude xx を繰り返し指定することで複数ファイルを指定可能");
        System.out.println(
                "例: java -jar AssertionUtils-0.0.x-SNAPSHOT-jar-with-dependencies.jar -i /home/userA -o /home/userB -exclude .bashrc -exclude .bash_history");

        System.out.println();
        System.out.println("テキスト比較する場合(期待値・検証値ファイルともMS932で読み込む)");
        System.out.println(
                "例: java -jar AssertionUtils-0.0.x-SNAPSHOT-jar-with-dependencies.jar -i /home/userA -o /home/userB -logic nu.mine.kino.assertionutils.TextAssertionLogic MS932");

        System.out.println();
        System.out.println(
                "CSV比較する場合(暫定処理として、半角スペースをデリミタとして分割して、先頭2列は除去してCSV項目比較)");
        System.out.println(
                "例: java -jar AssertionUtils-0.0.x-SNAPSHOT-jar-with-dependencies.jar -i /home/userA -o /home/userB -logic nu.mine.kino.assertionutils.CSVAssertionLogic MS932");
                // System.out.println();
                // parser.printUsage(System.out);

        // -i
        // /Users/masatomix/Documents/workspace_new/etfsmaBatchJava2/expected
        // -o /Users/masatomix/Desktop/actual -exclude d018.txt
        // -logic nu.mine.kino.assertionutils.TextAssertionLogic MS932
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

//     @Option(name = "-xxxx", required = true)
//     private static String xxx;

    public void execute() {
        logger.info("logic = {}", logic);
        logger.info("検証データディレクトリ = {}", output);
        logger.info("期待値ディレクトリ = {}", input);
        printArray("対象ファイル = {}", includes);
        printArray("除外ファイル = {}", excludes);
        logger.info("------ 検証開始 ------");

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
        logger.info("------ 検証終了 ------");

    }

    private void printArray(String format, String... array) {
        if (array != null && array.length > 0) {
            for (String string : array) {
                logger.info(format, string);
            }
        }
    }
}
