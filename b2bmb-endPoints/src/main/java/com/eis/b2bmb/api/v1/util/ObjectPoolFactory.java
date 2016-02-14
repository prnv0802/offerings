package com.eis.b2bmb.api.v1.util;

import org.apache.commons.pool.BasePoolableObjectFactory;
/**
 * This is the factory of the poolable objects.
 * @author sudhakars
 * date 19/05/14
 */
public class ObjectPoolFactory extends BasePoolableObjectFactory {
    /**
     * poolable class object
     */
    private final Class<Poolable> poolableObj;

    /**
     * @return Class<{@link Poolable}> - return poolable object.
     */
    protected Class<Poolable> getPoolableObject(){
        return poolableObj;
    }

    /**
     * @param poolableObj - {@link java.lang.Class<Poolable>}
     */
    protected ObjectPoolFactory(Class<Poolable> poolableObj){
        this.poolableObj = poolableObj;
    }

    /**
     * 
     * @param className - string
     * @return factory - {@link ObjectPoolFactory}
     * @throws ClassNotFoundException - class not found
     */
    public static ObjectPoolFactory newInstance(String className) throws ClassNotFoundException{
        Class<Poolable> poolObj= (Class<Poolable>) Class.forName(className);
        ObjectPoolFactory factory = new ObjectPoolFactory(poolObj);
        return factory;
    }

    @Override
    /**
     * called whenever an new instance is needed
     * @return Object - poolable object instance.
     * @see org.apache.commons.pool.PoolableObjectFactory#makeObject
     */
    public Object makeObject() throws Exception {
        return poolableObj.newInstance();
    }

    /**
     * invoked on every instance when it is returned to the pool.
     * @param obj - Object return to the pool.
     * @see org.apache.commons.pool.PoolableObjectFactory#makeObject
     */
    public void passivateObject(Object obj) {
        if(obj instanceof Poolable){
            Poolable poolObj= (Poolable) obj;
            poolObj.clear();
        }else{
            throw new RuntimeException("Object has to be instance of Poolable");
        }
    }

    /**
     * to validate the object.
     * @param obj - Object
     * @return boolean - valid
     */
    public boolean validateObject(Object obj){
        if(obj instanceof Poolable){
            return true;
        }
        return false;
    }
}
