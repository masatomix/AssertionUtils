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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.spi.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 指定されたファイルをテキストファイルとして、比較する。
 * 
 * <ul>
 * <li>まずは改行コードのチェックを行い、おなじ改行コードかどうかを比較する。</li>
 * <li>つぎに 行単位でファイルを読みこんで、行単位でテキスト比較する。</li>
 * <li>最後に最終行の改行の存在をチェックし、それらが等しいかどうかを比較する。最後のこの比較は、上記二つの比較だけだと
 * 行単位で処理してしまい、最終行の差異をチェックできていないため。</li>
 * </ul>
 * 
 * @author Masatomi KINO
 * @version $Revision$
 */
@Slf4j
public class TextAssertionLogic extends DefaultAssertionLogic {
    private static final Logger slogger = LoggerFactory
            .getLogger("forStackTrace");

    @Getter
    private Charset enc1;

    @Getter
    private Charset enc2;

    public TextAssertionLogic(Parameters params) throws AssertionError {
        super(params);
        enc1 = createCharset(params, 1);
        enc2 = createCharset(params, 2);
        log.info("期待値    encode: " + enc1.displayName());
        log.info("検証データ encode: " + enc2.displayName());
    }

    private Charset createCharset(Parameters params, int index) {
        String encoding = getEncoding(params, index);
        if (StringUtils.isEmpty(encoding)) {
            return Charset.defaultCharset();
        }
        return Charset.forName(encoding);
    }

    @Override
    public void executeAssertion(Path expected, Path actual)
            throws AssertionError {

        LINE_SEPARATOR eSeparator = getLineSeparator(expected, enc1);
        LINE_SEPARATOR aSeparator = getLineSeparator(actual, enc2);

        // 両方がnullでないなら、チェック。
        // そもそも異なる場合はエラーだが、後続でチェックできるのでココではやらない
        if (eSeparator != null && aSeparator != null) {
            assertThat("改行コードの比較エラー", aSeparator, is(eSeparator));
        }
        log.debug("改行コードは{}です", aSeparator);

        List<String> expectedLines = readText(expected, enc1);
        List<String> actualLines = readText(actual, enc2);

        assertThat("ファイル行数の比較エラー", actualLines.size(),
                is(expectedLines.size()));
        for (int i = 0; i < actualLines.size(); i++) {
            String actualLine = actualLines.get(i);
            String expectedLine = expectedLines.get(i);
            executeTextAssertion(expectedLine, actualLine);
        }

        LINE_SEPARATOR eLastLineSepa = getLastStrAndIsLineSeparator(expected,
                enc1);
        LINE_SEPARATOR aLastLineSepa = getLastStrAndIsLineSeparator(actual,
                enc2);

        // 片方がNULLでないなら、assertでエラーになるハズ。
        // 両方NULLなら、NULL通しの比較はtrueで問題なし。
        if (eLastLineSepa == null || aLastLineSepa == null) {
            assertThat("末尾の改行コードの比較エラー", aLastLineSepa, is(eLastLineSepa));
        }

    }

    /**
     * 末尾に改行コードがあるかをチェックする。 改行コードではない場合はNULL。改行コードの場合は、それを意味するenumを返す。
     * 
     * @param path
     * @param enc
     * @return
     */
    private LINE_SEPARATOR getLastStrAndIsLineSeparator(Path path,
            Charset enc) {
        byte[] bytes = readBinary(path);
        String target = new String(bytes, enc);

        if (target.endsWith("\r\n")) {
            return LINE_SEPARATOR.CRLF;
        } else if (target.endsWith("\r")) {
            return LINE_SEPARATOR.CR;
        }
        if (target.endsWith("\n")) {
            return LINE_SEPARATOR.LF;
        }
        return null;
    }

    /**
     * 改行コードを返す。改行がない場合はNULL:
     * 
     * @param path
     * @param charsetName
     * @return
     */
    private LINE_SEPARATOR getLineSeparator(Path path, Charset charsetName) {
        String LINE_SEPARATOR_PATTERN = "\r\n|[\n\r\u2028\u2029\u0085]";
        try (Scanner scan = new Scanner(path, charsetName.name())) {
            String lineSeparator = scan
                    .findWithinHorizon(LINE_SEPARATOR_PATTERN, 0);
            if (StringUtils.isEmpty(lineSeparator)) {
                return null;
            }
            if (lineSeparator.contains("\r")) {
                if (lineSeparator.contains("\r\n")) {
                    return LINE_SEPARATOR.CRLF;
                } else {
                    return LINE_SEPARATOR.CR;
                }
            } else if (lineSeparator.contains("\n")) {
                return LINE_SEPARATOR.LF;
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
        return null;
    }

    public List<String> readText(Path path, Charset enc) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(path, enc);
        } catch (IOException e) {
            slogger.error("このファイルの読み込みでエラー: {},({})", path, e.getMessage());
            slogger.error("", e);
            String message = "「java.nio.charset.MalformedInputException: Input length = xx」 \nというエラーの場合は、"
                    + "指定した文字エンコーディングと読み込んだファイルのそれとの相違であることが多いので、"
                    + "文字エンコーディングをチェックしましょう。";
            slogger.error(message);
            fail(e.getMessage());
        }
        return lines;
    }

    private byte[] readBinary(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            slogger.error("このファイルの読み込みでエラー: {},({})", path, e.getMessage());
            slogger.error("", e);
            fail(e.getMessage());
        }
        return new byte[0];
    }

    public void executeTextAssertion(String expectedLine, String actualLine) {
        String message = "期待値ファイルとの比較エラー: ";
        assertThat(message, actualLine, is(expectedLine));
    }

    /**
     * とってみる。ダメだったら -1してとってみる、それでもダメならエラー。
     * 
     * @param index
     * @return
     * @throws CmdLineException
     */
    private String getEncoding(Parameters params, int index) {
        // String auto = "JISAutoDetect";
        String auto = null;

        try {
            return params.getParameter(index);
        } catch (CmdLineException e) {
            if (index == 1) {
                return auto;
            }
            try {
                return params.getParameter(index - 1);
            } catch (CmdLineException e1) {
                return auto;
            }
        }
    }

    enum LINE_SEPARATOR {
        CR, CRLF, LF;
    }

}
