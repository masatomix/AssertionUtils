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
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.spi.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Masatomi KINO
 * @version $Revision$
 */
public class TextAssertionLogic extends DefaultAssertionLogic {
    private static final Logger slogger = LoggerFactory
            .getLogger("forStackTrace");

    private static final Logger logger = LoggerFactory
            .getLogger(TextAssertionLogic.class);

    private Charset enc1;

    private Charset enc2;

    public TextAssertionLogic(Parameters params) throws AssertionError {
        super(params);
        enc1 = createCharset(params, 1);
        enc2 = createCharset(params, 2);
        logger.info("期待値    encode: " + enc1.displayName());
        logger.info("検証データ encode: " + enc2.displayName());
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
        List<String> expectedLines = readText(expected, enc1);
        List<String> actualLines = readText(actual, enc2);

        try {
            assertThat(actualLines.size(), is(expectedLines.size()));
            for (int i = 0; i < actualLines.size(); i++) {
                String actualLine = actualLines.get(i);
                String expectedLine = expectedLines.get(i);
                executeTextAssertion(expectedLine, actualLine);
            }
        } catch (UnsupportedCharsetException e) {
            slogger.error("", e);
            fail(e.getMessage());
        }
    }

    private List<String> readText(Path path, Charset enc) {
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

    public void executeTextAssertion(String expectedLine, String actualLine) {
        assertThat(actualLine, is(expectedLine));
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

}
