package com.eis.b2bmb.component.custom.POC;

import java.util.Map;

import org.apache.camel.ExchangeException;
import org.apache.camel.Headers;
import org.apache.camel.Properties;

public class MyRoutingAlg {
	
	public synchronized String getRoute(String body, @Headers Map<String, Object> headers, @Properties Map<String, Object> properties, @ExchangeException Exception exp){
		//keeping track of invocation
		System.out.println(exp);
		int invoked = 0;
		Object current = properties.get("invoked");
		if (current != null) {
		invoked = Integer.valueOf(current.toString());
		}
		invoked++;
		// and store the state back on the properties
		properties.put("invoked", invoked);
		System.out.println("invoked >>>>>>>>>>>>> count >>>>>>>>"+invoked);
		if(invoked == 1){
			String address = (String) headers.get("toAddress");
			if("file://testfile".equals(address)){
				headers.remove("toAddress");
				return "file://testfile";
			}
		}
		return null;
	}
}
