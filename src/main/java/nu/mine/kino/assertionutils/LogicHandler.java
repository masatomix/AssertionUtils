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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Masatomi KINO
 * @version $Revision$
 */
@Slf4j
public class LogicHandler extends OptionHandler<Logic> {

    private static final Logger slogger = LoggerFactory
            .getLogger("forStackTrace");

    public LogicHandler(CmdLineParser parser, OptionDef option,
            Setter<? super Logic> setter) {
        super(parser, option, setter);
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
        Logic value = parse(params);
        setter.addValue(value);
        return params.size();
    }

    private Logic parse(Parameters params) throws CmdLineException {
        String className = params.getParameter(0);
        log.debug("className: " + className);
        print(params);

        try {
            // if (!StringUtils.isEmpty(encoding)) {
            // Logic newInstance = Logic.class.getConstructor(String.class)
            // .newInstance(encoding);
            Class<? extends Logic> clazz = Class.forName(className)
                    .asSubclass(Logic.class);
            Logic newInstance = clazz.getConstructor(Parameters.class)
                    .newInstance(params);
            return newInstance;
            // } else {
            // // Logic newInstance = Logic.class.newInstance();
            // // return newInstance;
            // Class<? extends Logic> clazz = Class.forName(className)
            // .asSubclass(Logic.class);
            //
            // Logic newInstance = clazz.newInstance();
            // return newInstance;
            // }
        } catch (ClassNotFoundException e) {
            slogger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (InstantiationException e) {
            slogger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (IllegalAccessException e) {
            slogger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (IllegalArgumentException e) {
            slogger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (InvocationTargetException e) {
            slogger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (NoSuchMethodException e) {
            slogger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (SecurityException e) {
            slogger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        }

    }

    private void print(Parameters params) throws CmdLineException {
        for (int i = 0; i < params.size(); i++) {
            log.debug("params[{}]: " + params.getParameter(i), i);
        }
    }

    @Override
    public String getDefaultMetaVariable() {
        return "N";
    }

    private String exception2String(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        return baos.toString();
    }

}
