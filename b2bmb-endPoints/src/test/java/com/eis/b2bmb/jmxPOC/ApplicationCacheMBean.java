package com.eis.b2bmb.jmxPOC;

public interface ApplicationCacheMBean {
	
	int getMaxCacheSize();
	void setMaxCacheSize(int value);
	int getCachedObjects();
	void clearCache();
	
}