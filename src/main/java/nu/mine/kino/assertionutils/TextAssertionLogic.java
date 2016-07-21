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
        try {
            List<String> expectedLines = Files.readAllLines(expected, enc1);
            List<String> actualLines = Files.readAllLines(actual, enc2);

            assertThat(actualLines.size(), is(expectedLines.size()));
            for (int i = 0; i < actualLines.size(); i++) {
                assertThat(actualLines.get(i), is(expectedLines.get(i)));
            }
            logger.info("このファイルは期待値通りでした: " + actual);
        } catch (IOException e) {
            // e.printStackTrace();
            fail(e.getMessage());
        } catch (UnsupportedCharsetException e) {
            // e.printStackTrace();
            fail(e.getMessage());
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
