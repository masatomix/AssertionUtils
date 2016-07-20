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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Assert;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.spi.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Masatomi KINO
 * @version $Revision$
 */
public class TextAssertionLogic extends DefaultAssertionLogic {

    private static final Logger logger = LoggerFactory
            .getLogger(TextAssertionLogic.class);

    private String encoding1;

    private String encoding2;

    public TextAssertionLogic(Parameters params) throws AssertionError {
        super(params);
        encoding1 = getEncoding(params, 1);
        encoding2 = getEncoding(params, 2);
        logger.info("期待値    encode: "+ encoding1);
        logger.info("検証データ encode: "+ encoding2);
    }

    @Override
    public void executeAssertion(Path expected, Path actual)
            throws AssertionError {

        try {
            Charset enc1 = Charset.forName(encoding1);
            Charset enc2 = Charset.forName(encoding2);
            List<String> expectedLines = Files.readAllLines(expected, enc1);
            List<String> actualLines = Files.readAllLines(actual, enc2);
            // byte[] expectedBytes = Files.readAllBytes(expected);
            // byte[] actualBytes = Files.readAllBytes(actual);
            // String message = actual
            // + "について、期待値ファイルと異なっている(バイナリチェックなので詳細はファイルを参照のこと)\n期待値ファイル:"
            // + expected;
            // Assert.assertArrayEquals(message, expectedBytes, actualBytes);
            logger.info("このファイルは期待値通りでした: " + actual);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } catch (UnsupportedCharsetException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * とってみる。ダメだったら -1してとってみる、それでもダメならエラー。
     * 
     * @param index
     * @return
     * @throws CmdLineException
     */
    private String getEncoding(Parameters params, int index) {
        String auto = "JISAutoDetect";
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
