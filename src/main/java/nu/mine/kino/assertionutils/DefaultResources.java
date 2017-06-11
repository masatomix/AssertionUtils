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
//作成日: 2017/06/09

package nu.mine.kino.assertionutils;

import java.util.ListResourceBundle;

/**
 * @author Masatomi KINO
 * @version $Revision$
 */
public class DefaultResources extends ListResourceBundle {

    @Override
    protected Object[][] getContents() {
        return new Object[][] { { "modified_file_ext", "_modified" },
                { "modified_file_export", "true" },
                { "lastLineCheck", "true" } };
    }

}