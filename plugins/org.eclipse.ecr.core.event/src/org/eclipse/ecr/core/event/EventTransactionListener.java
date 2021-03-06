/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.eclipse.ecr.core.event;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 * @deprecated was put in place in 5.1 version and maintained for compatibility only.
 */
public interface EventTransactionListener {

    /**
     * Invoked multiple time each time a session is created
     * inside a transaction.
     */
    void transactionStarted();

    /**
     * Invoked multiple time when the transaction is roll-backing, one for each
     * session supported by the transaction.
     */
    void transactionRollbacked();

    /**
     * Invoked multiple time when the transaction is committing, one for
     * each session supported by the transaction.
     */
    void transactionCommitted();

}
