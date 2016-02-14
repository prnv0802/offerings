package com.eis.b2bmb.examples.interceptor;

import java.util.LinkedHashMap;
import java.util.Map;

public class QuickAndDirtyContext extends LinkedHashMap<String, Object>
            implements InterceptorContext<String, Object>
    {
        /**
         * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
         * with the specified initial capacity and load factor.
         *
         * @param initialCapacity the initial capacity
         * @param loadFactor      the load factor
         * @throws IllegalArgumentException if the initial capacity is negative
         *                                            or the load factor is nonpositive
         */
        QuickAndDirtyContext(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
        }

        /**
         * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
         * with the specified initial capacity and a default load factor (0.75).
         *
         * @param initialCapacity the initial capacity
         * @throws IllegalArgumentException if the initial capacity is negative
         */
        QuickAndDirtyContext(int initialCapacity) {
            super(initialCapacity);
        }

        /**
         * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
         * with the default initial capacity (16) and load factor (0.75).
         */
        QuickAndDirtyContext() {
        }

        /**
         * Constructs an insertion-ordered <tt>LinkedHashMap</tt> instance with
         * the same mappings as the specified map.  The <tt>LinkedHashMap</tt>
         * instance is created with a default load factor (0.75) and an initial
         * capacity sufficient to hold the mappings in the specified map.
         *
         * @param m the map whose mappings are to be placed in this map
         * @throws NullPointerException if the specified map is null
         */
        QuickAndDirtyContext(Map<? extends String, ?> m) {
            super(m);
        }

        /**
         * Constructs an empty <tt>LinkedHashMap</tt> instance with the
         * specified initial capacity, load factor and ordering mode.
         *
         * @param initialCapacity the initial capacity
         * @param loadFactor      the load factor
         * @param accessOrder     the ordering mode - <tt>true</tt> for
         *                        access-order, <tt>false</tt> for insertion-order
         * @throws IllegalArgumentException if the initial capacity is negative
         *                                            or the load factor is nonpositive
         */
        QuickAndDirtyContext(int initialCapacity, float loadFactor, boolean accessOrder) {
            super(initialCapacity, loadFactor, accessOrder);
        }
    }