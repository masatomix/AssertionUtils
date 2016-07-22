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

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.kohsuke.args4j.spi.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Masatomi KINO
 * @version $Revision$
 */
public class CSVAssertionLogic extends TextAssertionLogic {

    private static final Logger logger = LoggerFactory
            .getLogger(CSVAssertionLogic.class);

    public CSVAssertionLogic(Parameters params) throws AssertionError {
        super(params);
    }

    @Override
    public void executeTextAssertion(String expected, String actual) {
        int magicNumber = 2;
        String delimiter = " ";
        String[] expectedArray = StringUtils.splitPreserveAllTokens(expected,
                delimiter);
        String[] actualArray = StringUtils.splitPreserveAllTokens(actual,
                delimiter);
        assertThat(actualArray.length, is(equalTo(expectedArray.length)));

        // とりあえず、magicNumber 分カラムをとっちゃう
        if (actualArray.length > magicNumber) {

            String[] convertActualArray = removeColumns(actualArray,
                    magicNumber);
            String[] convertExpectedArray = removeColumns(expectedArray,
                    magicNumber);

            assertThat(convertActualArray.length,
                    is(convertExpectedArray.length));

            String message = actual + "について、期待値ファイルと異なっている\n期待値ファイル:"
                    + expected;
            Assert.assertArrayEquals(message, convertExpectedArray,
                    convertActualArray);

        }
    }

    private String[] removeColumns(String[] actualArray, int columnNumber) {
        String[] convertActualArray = new String[actualArray.length
                - columnNumber];
        System.arraycopy(actualArray, columnNumber, convertActualArray, 0,
                actualArray.length - columnNumber);
        return convertActualArray;
    }

}
