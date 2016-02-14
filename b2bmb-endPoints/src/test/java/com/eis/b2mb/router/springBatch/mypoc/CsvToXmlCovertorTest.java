package com.eis.b2mb.router.springBatch.mypoc;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.eis.b2bmb.routerPOC.service.HiearchicalDispatcherImpl;

/*
author : harjeets
date : 3/02/2014
*/

public class CsvToXmlCovertorTest {
   
	private static final Logger LOG = LoggerFactory.getLogger(CsvToXmlCovertorTest.class);
	/*Method to read csv file and write to xml*/
//	@Test
	public void run() {

		String[] springConfig = { "file:src/test/resources/META-INF/mySpringContext.xml" };

		ApplicationContext context = new ClassPathXmlApplicationContext(springConfig);

		JobLauncher jobLauncher = (JobLauncher) context.getBean("jobLauncher");
		Job job = (Job) context.getBean("csvToXmlJob");

		try {

			JobExecution execution = jobLauncher.run(job, new JobParameters());
			LOG.debug("Exit Status : " + execution.getStatus());
		
		} catch (Exception ex) {
			LOG.error("Failed to convert file for job "+job+"        "+ex.getMessage());
		}

	}

}
