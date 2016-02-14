/**
 * 
 */
package com.eis.b2bmb.endpts.as2;

import inedi.InEDIException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author harjeets
 *
 */
public interface AS2Receiver {
         
          /**
           * Method to execute the mandatory logic for getting data from @HttpServletRequest 
           * 
           * 
          * @param request - @HttpServletRequest instance will be used by as2 receiver bean for getting edi data
          * @param response - @HttpServletResponse instance will be used to send synchronous response
          * @throws inedi.InEDIException - InEDIException thrown by AS2 receiver module.
          */
         public void executeRequest(HttpServletRequest request,
                                    HttpServletResponse response) throws InEDIException ;

}
