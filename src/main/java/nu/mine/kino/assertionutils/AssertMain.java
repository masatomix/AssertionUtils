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

        AssertMain main = new AssertMain();
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.out.println("usage:");
            parser.printSingleLineUsage(System.out);
            System.out.println();
            System.out.println("除外ファイル名は -exclude xx を繰り返し指定することで複数ファイルを指定可能");
            System.out.println(
                    "例: java -jar AssertionUtils-0.0.1-SNAPSHOT-jar-with-dependencies.jar -i /home/userA -o /home/userB -exclude .bashrc -exclude .bash_history");
            // System.out.println();
            // parser.printUsage(System.out);
            return;
        }
        main.execute();
    }

    @Option(name = "-i", metaVar = "期待値ディレクトリ", required = true, usage = "input file")
    private static String input;

    @Option(name = "-o", metaVar = "検証データディレクトリ", required = true, usage = "output file")
    private static String output;

    // @Option(name = "-logic", metaVar = "logic", required = false, usage =
    // "Logic名/ bin/txt")
    // private static String logic;

    @Option(name = "-exclude", metaVar = "除外ファイル名", required = false, usage = "*.logなど除外ファイルを指定")
    private static String[] excludes;

    // @Option(name = "-xxxx", required = true)
    // private static String xxx;

    public void execute() {
        logger.info("検証データディレクトリ = {}", output);
        logger.info("期待値ディレクトリ = {}", input);
        // logger.debug("logic = {}", logic);
        printArray("除外ファイル = {}", excludes);

        // Startのディレクトリの存在チェックを実施
        assertExists(input);
        assertExists(output);

        if (isDirectory(output)) {
            assertEqualsDirWithoutException(input, output, excludes);
        } else {
            assertEqualsFileWithoutException(input, output);
        }

        // ココに、期待値ファイルがあるのに、実績ファイルが存在しないチェックが必要だ。
        // 上の処理はActualのディレクトリを Visitorしたけど、
        // ExpectedをVisitorして、そんざいしないActualを警告出すか。
        // → 実装済み
        assertFileExists(input, output, excludes);

    }

    private void printArray(String format, String... array) {
        for (String string : array) {
            logger.info(format, string);
        }
    }
}
