/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florent Guillaume
 */

package org.eclipse.ecr.core.storage.sql;

import java.io.Serializable;

import javax.transaction.xa.XAResource;

import org.eclipse.ecr.core.api.IterableQueryResult;
import org.eclipse.ecr.core.api.Lock;
import org.eclipse.ecr.core.query.QueryFilter;
import org.eclipse.ecr.core.storage.PartialList;
import org.eclipse.ecr.core.storage.StorageException;

/**
 * A {@link Mapper} maps {@link Row}s to and from the database.
 */
public interface Mapper extends RowMapper, XAResource {

    /**
     * Identifiers assigned by a server to identify a client mapper and its
     * repository.
     */
    public static final class Identification implements Serializable {
        private static final long serialVersionUID = 1L;

        public final String repositoryId;

        public final String mapperId;

        public Identification(String repositoryId, String mapperId) {
            this.repositoryId = repositoryId;
            this.mapperId = mapperId;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '(' + repositoryId + ','
                    + mapperId + ')';
        }
    }

    /**
     * Returns the repository id and mapper id assigned.
     * <p>
     * This is used in remote stateless mode to be able to identify to which
     * mapper an incoming connection is targeted, and from which repository
     * instance.
     *
     * @return the repository and mapper identification
     * @throws StorageException when initial connection failed (for a NetMapper)
     */
    Identification getIdentification() throws StorageException;

    // used for reflection
    String GET_IDENTIFICATION = "getIdentification";

    void close() throws StorageException;

    // used for reflection
    String CLOSE = "close";

    // TODO
    int getTableSize(String tableName) throws StorageException;

    /**
     * Creates the necessary structures in the database.
     */
    // TODO
    void createDatabase() throws StorageException;

    /*
     * ========== Methods returning non-Rows ==========
     */

    /*
     * ----- Root -----
     */

    /**
     * Gets the root id for a given repository, if registered.
     *
     * @param repositoryId the repository id, usually 0
     * @return the root id, or null if not found
     */
    Serializable getRootId(Serializable repositoryId) throws StorageException;

    /**
     * Records the newly generated root id for a given repository.
     *
     * @param repositoryId the repository id, usually 0
     * @param id the root id
     */
    void setRootId(Serializable repositoryId, Serializable id)
            throws StorageException;

    /*
     * ----- Version/Proxy -----
     */

    /**
     * Gets the id of a version given a version series id and a label.
     *
     * @param versionSeriesId the version series id
     * @param label the label
     * @return the id of the version, or {@code null} if not found
     */
    Serializable getVersionIdByLabel(Serializable versionSeriesId, String label)
            throws StorageException;

    /**
     * Gets the id of the last version given a version series id.
     *
     * @param versionSeriesId the version series id
     * @return the id of the last version, or {@code null} if not found
     */
    Serializable getLastVersionId(Serializable versionSeriesId)
            throws StorageException;

    /*
     * ----- Query -----
     */

    /**
     * Makes a NXQL query to the database.
     *
     * @param query the query
     * @param queryFilter the query filter
     * @param countTotal if {@code true}, count the total size without
     *            limit/offset
     * @return the list of matching document ids
     */
    PartialList<Serializable> query(String query, QueryFilter queryFilter,
            boolean countTotal) throws StorageException;

    /**
     * Makes a query to the database and returns an iterable (which must be
     * closed when done).
     *
     * @param query the query
     * @param queryType the query type
     * @param queryFilter the query filter
     * @param params optional query-type-dependent parameters
     * @return an iterable, which <b>must</b> be closed when done
     */
    // queryFilter used for principals and permissions
    IterableQueryResult queryAndFetch(String query, String queryType,
            QueryFilter queryFilter, Object... params) throws StorageException;

    /*
     * ----- ACLs -----
     */

    void updateReadAcls() throws StorageException;

    void rebuildReadAcls() throws StorageException;

    /*
     * ----- Clustering -----
     */

    /**
     * Informs the cluster that this node exists.
     */
    void createClusterNode() throws StorageException;

    /**
     * Removes this node from the cluster.
     */
    void removeClusterNode() throws StorageException;

    /**
     * Inserts the invalidation rows for the other cluster nodes.
     */
    void insertClusterInvalidations(Invalidations invalidations)
            throws StorageException;

    /**
     * Gets the invalidations from other cluster nodes.
     */
    Invalidations getClusterInvalidations() throws StorageException;

    /*
     * ----- Locking -----
     */

    /**
     * Gets the lock state of a document.
     * <p>
     * If the document does not exist, {@code null} is returned.
     *
     * @param id the document id
     * @return the existing lock, or {@code null} when there is no lock
     */
    Lock getLock(Serializable id) throws StorageException;

    /**
     * Sets a lock on a document.
     * <p>
     * If the document is already locked, returns its existing lock status
     * (there is no re-locking, {@link #removeLock} must be called first).
     *
     * @param id the document id
     * @param lock the lock object to set
     * @return {@code null} if locking succeeded, or the existing lock if
     *         locking failed, or a
     */
    Lock setLock(Serializable id, Lock lock) throws StorageException;

    /**
     * Removes a lock from a document.
     * <p>
     * The previous lock is returned.
     * <p>
     * If {@code owner} is {@code null} then the lock is unconditionally
     * removed.
     * <p>
     * If {@code owner} is not {@code null}, it must match the existing lock
     * owner for the lock to be removed. If it doesn't match, the returned lock
     * will return {@code true} for {@link Lock#getFailed}.
     *
     * @param id the document id
     * @param the owner to check, or {@code null} for no check
     * @param force {@code true} to just do the remove and not return the
     *            previous lock
     * @return the previous lock
     */
    Lock removeLock(Serializable id, String owner, boolean force)
            throws StorageException;

}
