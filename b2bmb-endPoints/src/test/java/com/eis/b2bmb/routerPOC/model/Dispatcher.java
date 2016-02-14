package com.eis.b2bmb.routerPOC.model;

import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;

/**
 * User: mingardia
 * Date: 1/23/14
 * Time: 2:51 PM
 */
public interface Dispatcher {

    public void dispatch() throws B2BTransactionFailed, B2BNotFoundException, ValidationException;
}
