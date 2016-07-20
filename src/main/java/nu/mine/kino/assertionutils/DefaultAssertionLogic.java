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
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.spi.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * デフォルトの、バイナリ比較処理。-logicでオプション指定しなければ、コレが呼び出される。
 * 内部でJUnitのバイト列のAssertion処理をコールしている
 * 
 * @author Masatomi KINO
 * @version $Revision$
 */
public class DefaultAssertionLogic implements Logic {
    private static final Logger logger = LoggerFactory
            .getLogger(DefaultAssertionLogic.class);

    // private Parameters params;

    public DefaultAssertionLogic(Parameters params) {
        // this.params = params;
    }

    // public Parameters getParameters() {
    // return params;
    // }

    @Override
    public void executeAssertion(Path expected, Path actual)
            throws AssertionError {
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

}
