package com.eis.b2bmb.batch.job;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

/**
 *  Sample class for showing how to use the Job Launcher
 */
public class JobLauncherDetails extends QuartzJobBean {


    private static final Logger LOG = LoggerFactory.getLogger(JobLauncherDetails.class);

    static final String JOB_NAME = "jobName";

    private JobLocator jobLocator;

    private JobLauncher jobLauncher;

    /**
     * @param jobLocator {@link JobLocator}
     */
    public void setJobLocator(JobLocator jobLocator) {
        this.jobLocator = jobLocator;
    }

    /**
     * @param jobLauncher {@link JobLauncher}
     */
    public void setJobLauncher(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

  

    /**
     * @param context {@link JobExecutionContext}
     */
    protected void executeInternal(JobExecutionContext context) {

        Map<String, Object> jobDataMap = context.getMergedJobDataMap();

        String jobName = (String) jobDataMap.get(JOB_NAME);

        JobParameters jobParameters = getJobParametersFromJobMap(jobDataMap);

        try {
            jobLauncher.run(jobLocator.getJob(jobName), jobParameters);
        } catch (JobExecutionException e) {
            e.printStackTrace();
        }
    }

    //get params from jobDataAsMap property, job-quartz.xml
    private JobParameters getJobParametersFromJobMap(Map<String, Object> jobDataMap) {

        JobParametersBuilder builder = new JobParametersBuilder();

        for (Entry<String, Object> entry : jobDataMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String && !key.equals(JOB_NAME)) {
                builder.addString(key, (String) value);
            } else if (value instanceof Float || value instanceof Double) {
                builder.addDouble(key, ((Number) value).doubleValue());
            } else if (value instanceof Integer || value instanceof Long) {
                builder.addLong(key, ((Number) value).longValue());
            } else if (value instanceof Date) {
                builder.addDate(key, (Date) value);
            }
            else {
                // JobDataMap contains values which are not job parameters
              // (ignoring)
             if (LOG.isWarnEnabled())
             {
                 LOG.warn("JobDataMap contains values which are not job Paraeters: key: " + key + " value: " + value);
             }
            }
        }

        //need unique job parameter to rerun the completed job
        builder.addDate("run date", new Date());

        return builder.toJobParameters();

    }

}