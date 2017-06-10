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
import org.kohsuke.args4j.spi.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * 基本的には{@link TextAssertionLogic}と同様の比較を行うが、行をスペースをデリミタとした可変長ファイルと見なし、
 * さらにmagicNumber (=2)
 * 列だけ除去してテキスト比較するサンプル。日付などが先頭列に入っていて、そこは比較除外としたいなどを想定し、サンプルを作成している。
 * 
 * @author Masatomi KINO
 * @version $Revision$
 * @see TextAssertionLogic
 */
@Slf4j
public class CSV2AssertionLogic extends TextAssertionLogic {

    public CSV2AssertionLogic(Parameters params) throws AssertionError {
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
        assertThat("デリミタで分割後の列数比較エラー", actualArray.length,
                is(equalTo(expectedArray.length)));

        // とりあえず、magicNumber 分カラムをとっちゃう
        if (actualArray.length > magicNumber) {

            String[] convertActualArray = removeColumns(actualArray,
                    magicNumber);
            String[] convertExpectedArray = removeColumns(expectedArray,
                    magicNumber);

            assertThat(magicNumber + " 列数分除去後の列数比較エラー",
                    convertActualArray.length, is(convertExpectedArray.length));

            String message = "期待値ファイルとの比較エラー: ";
            // Assert.assertArrayEquals(message, convertExpectedArray,
            // convertActualArray);
            assertThat(message, convertActualArray, is(convertExpectedArray));

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
