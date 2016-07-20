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

/**
 * @author Masatomi KINO
 * @version $Revision$
 */
public class LogicHandler extends OptionHandler<Logic> {

    private static final Logger logger = LoggerFactory
            .getLogger(LogicHandler.class);

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
        logger.debug("className: " + className);
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
            logger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (InstantiationException e) {
            logger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (IllegalAccessException e) {
            logger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (IllegalArgumentException e) {
            logger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (InvocationTargetException e) {
            logger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (NoSuchMethodException e) {
            logger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        } catch (SecurityException e) {
            logger.error(exception2String(e));
            throw new CmdLineException(owner, e);
        }

    }

    private void print(Parameters params) throws CmdLineException {
        for (int i = 0; i < params.size(); i++) {
            logger.debug("params[{}]: " + params.getParameter(i), i);
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
