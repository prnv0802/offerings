package com.eis.b2bmb.api.v1.services;

import com.eis.b2bmb.endpts.ssh.nsoftware.OpenFileKey;
import org.apache.commons.pool.impl.StackObjectPool;

/**
 *  A factory class which creates objects which are 
 *  in turn used by object pooling. Provides life cycle methods 
 *  of the object which is to be pooled. Objects created by 
 *  this factory are required to implement {@link com.eis.b2bmb.api.v1.util.Poolable}
 *         
 * @see org.apache.commons.pool.PoolableObjectFactory
 * @see org.apache.commons.pool.BasePoolableObjectFactory
 * @author sudhakars
 * date - 19/05/14
 */
public class OpenFileKeyService {
        /**
         * pool of OpenFileKey objects. 
         */
        private StackObjectPool openFileKeyPool;// Why isn't this a generic? can avoid unnecessary casting

        /**
         * getter method
         * @return openFileKeyPool - {@link StackObjectPool}
         */
        public StackObjectPool getOpenFileKeyPool() {
            return openFileKeyPool;
        }

        /**
         * Setter method
         * @param openFileKeyPool - {@link StackObjectPool}
         * @param openFileKeyPool
         */
        public void setOpenFileKeyPool(StackObjectPool openFileKeyPool) {
            this.openFileKeyPool = openFileKeyPool;
        }

        /**
         * Returns a new openFileKey object form the pool.
         * @return {@link OpenFileKey} object from object pool
         * @throws Exception - exception while fetching from pool
         */
        public OpenFileKey newOpenFileKey() throws Exception {
            return (OpenFileKey) openFileKeyPool.borrowObject();
        }

        /**
         * @param openFileKey
         * The openFileKey object which is to be returned to pool.
         * @throws Exception - exception while fetching from pool
         */
        public void returnOpenFileKey(OpenFileKey openFileKey) throws Exception{
            openFileKeyPool.returnObject(openFileKey);
        }
}
