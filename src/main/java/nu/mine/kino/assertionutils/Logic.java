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

import java.nio.file.Path;

/**
 * @author Masatomi KINO
 * @version $Revision$
 */
public interface Logic {
    public void executeAssertion(Path expected, Path actual)
            throws AssertionError;

}
