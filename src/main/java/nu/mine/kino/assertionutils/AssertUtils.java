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
//作成日: 2016/07/02

package nu.mine.kino.assertionutils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Masatomi KINO
 * @version $Revision$
 */
public class AssertUtils {

    private static final Logger logger = LoggerFactory
            .getLogger(AssertUtils.class);

    // List<String> allLines = Files.readAllLines(path,
    // Charset.forName("UTF-8"));
    // めも
    // http://acro-engineer.hatenablog.com/entry/2014/03/12/112402

    public static void assertEqualsDirWithoutException(String expectedDir,
            String actualDir, String... excludePatterns) {
        assertIsDir(expectedDir);
        assertIsDir(actualDir);
        Path expectedPath = Paths.get(expectedDir);
        Path actualDirPath = Paths.get(actualDir);

        int matcherSize = 0;
        if (excludePatterns != null) {
            matcherSize = excludePatterns.length;
        }
        PathMatcher[] matchers = new PathMatcher[matcherSize];

        for (int i = 0; i < matcherSize; i++) {
            matchers[i] = FileSystems.getDefault()
                    .getPathMatcher("glob:" + excludePatterns[i]);
        }

        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path actualFile,
                    BasicFileAttributes attrs) throws IOException {

                Path fileName = actualFile.getFileName();
                if (fileName == null || match(matchers, fileName)) {
                    logger.info("除外しました:" + actualFile);
                    return FileVisitResult.CONTINUE;
                }

                // '.から始まるファイルは除外'
                if (actualFile.toFile().getName().startsWith(".")) {
                    return FileVisitResult.CONTINUE;
                }
                // Visitorパタンで、走査したファイルから、期待値パスを算出し、それらをDIFFとる。
                // System.out.println("actualFile: " + actualFile);
                Path relativePath = actualDirPath.relativize(actualFile);
                Path expectedFile = expectedPath.resolve(relativePath);
                logger.debug("テスト対象ファイル: " + actualFile);
                logger.debug("期待値ファイル: " + expectedFile);

                assertEqualsFileWithoutException(expectedFile, actualFile);
                return FileVisitResult.CONTINUE;
            }

            /**
             * 除外条件sにマッチするかどうか。
             * 
             * @param matchers
             * @param fileName
             * @return
             */
            private boolean match(PathMatcher[] matchers, Path fileName) {
                for (PathMatcher matcher : matchers) {
                    if (matcher.matches(fileName)) {
                        return true;
                    }
                }
                return false;
            }
        };
        try {
            Files.walkFileTree(Paths.get(actualDir), visitor);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

    public static void assertEqualsFileWithoutException(String expected,
            String actual) {
        assertEqualsFileWithoutException(Paths.get(expected),
                Paths.get(actual));
    }

    public static void assertEqualsFileWithoutException(Path expected,
            Path actual) {
        try {
            assertEqualsFile(expected, actual);
        } catch (AssertionError e) {
            e.printStackTrace();
        }
    }

    /**
     * FullPathの文字列をもらって、そのファイルのバイナリレベルのDIFFをとる
     * ホントはCSV変換とかしてチェックしたいけど、暫定でファイルレベルのチェックを実施。
     *
     * @param expected
     * @param actual
     */
    public static void assertEqualsFile(Path expected, Path actual) {
        // あらかじめ配置しておいた期待値ファイルのパスを取得する
        // output1のファイルに該当する期待値ファイルのフルパスを返すメソッドをよべばよい
        assertIsFile(expected);
        assertIsFile(actual);

        try {
            byte[] expectedBytes = Files.readAllBytes(expected);
            byte[] actualBytes = Files.readAllBytes(actual);
            String message = actual
                    + "について、期待値ファイルと異なっている(バイナリチェックなので詳細はファイルを参照のこと)\n期待値ファイル:"
                    + expected;
            // Assert.assertThat(message,actualBytes,is(expectedBytes));
            Assert.assertArrayEquals(message, expectedBytes, actualBytes);
            logger.info("このファイルは期待値通りでした: " + actual);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * FullPathの文字列をもらって、そのファイルのバイナリレベルのDIFFをとる
     * ホントはCSV変換とかしてチェックしたいけど、暫定でファイルレベルのチェックを実施。
     *
     * @param expected
     * @param actual
     */
    public static void assertEqualsFile(String expected, String actual) {
        assertEqualsFile(Paths.get(expected), Paths.get(actual));
    }

    /**
     * sourceDirに対応するファイルが、destDirに存在するかをチェックする。期待値を置いておいたモノの、
     * actualのファイルが存在しないケースは 検知することが望ましい。
     * 
     * @param source
     * @param dest
     */
    public static void assertFileExists(String sourceDir, String destDir,
            String... excludePatterns) {
        assertIsDir(sourceDir);
        assertIsDir(destDir);
        Path sourcePath = Paths.get(sourceDir);
        Path destPath = Paths.get(destDir);

        int matcherSize = 0;
        if (excludePatterns != null) {
            matcherSize = excludePatterns.length;
        }
        PathMatcher[] matchers = new PathMatcher[matcherSize];
        for (int i = 0; i < matcherSize; i++) {
            matchers[i] = FileSystems.getDefault()
                    .getPathMatcher("glob:" + excludePatterns[i]);
        }

        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path sourceFile,
                    BasicFileAttributes attrs) throws IOException {

                Path fileName = sourceFile.getFileName();
                if (fileName == null || match(matchers, fileName)) {
                    return FileVisitResult.CONTINUE;
                }

                // '.から始まるファイルは除外'
                if (sourceFile.toFile().getName().startsWith(".")) {
                    return FileVisitResult.CONTINUE;
                }

                // Visitorパタンで、走査したファイルから、期待値パスを算出し、それらをDIFFとる。
                // System.out.println("actualFile: " + actualFile);
                Path relativePath = sourcePath.relativize(sourceFile);
                Path destFile = destPath.resolve(relativePath);
                if (!Files.exists(destFile)) {
                    logger.error(
                            "期待値ファイルが置いてあるのに、テスト対象ファイルが存在しない: " + destFile);
                }
                return FileVisitResult.CONTINUE;
            }

            /**
             * 除外条件sにマッチするかどうか。
             * 
             * @param matchers
             * @param fileName
             * @return
             */
            private boolean match(PathMatcher[] matchers, Path fileName) {
                for (PathMatcher matcher : matchers) {
                    if (matcher.matches(fileName)) {
                        return true;
                    }
                }
                return false;
            }
        };
        try {
            Files.walkFileTree(Paths.get(sourceDir), visitor);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

    ///////// 以下、便利Utils
    public static void assertIsDir(String dir) {
        assertIsDir(Paths.get(dir));
    }

    public static void assertIsDir(Path dir) {
        assertExists(dir);
        if (!Files.isDirectory(dir)) {
            Assert.fail(dir + " はディレクトリです。ファイルを指定してください。");
        }
    }

    public static void assertIsFile(String path) {
        assertIsFile(Paths.get(path));
    }

    public static void assertIsFile(Path path) {
        assertExists(path);
        if (Files.isDirectory(path)) {
            Assert.fail(path + " はディレクトリです。ファイルを指定してください。");
        }
    }

    public static void assertExists(String path) {
        assertExists(Paths.get(path));
    }

    public static void assertExists(Path path) {
        if (!Files.exists(path)) {
            Assert.fail(path + " が存在しません。");
        }
    }

    public static boolean isDirectory(Path path) {
        assertExists(path);
        return Files.isDirectory(path);
    }

    public static boolean isFile(Path path) {
        return !isDirectory(path);
    }

    public static boolean isDirectory(String path) {
        return isDirectory(Paths.get(path));
    }

    public static boolean isFile(String path) {
        return isFile(Paths.get(path));
    }
}
