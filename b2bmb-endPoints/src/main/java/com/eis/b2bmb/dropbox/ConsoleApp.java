package com.eis.b2bmb.dropbox;

import com.dropbox.core.*;

import java.io.*;
import java.util.Locale;


/**
 * User: mingardia
 * Date: 9/25/14
 * Time: 12:37 PM
 */
public class ConsoleApp {


    /**
     * Your application key
     */
    static final String APP_KEY = "INSERT_APP_KEY";

    /**
     * Your apps' secret
     */
    static final String APP_SECRET = "INSERT_APP_SECRET";

    /**
     * gets the access token
     *
     * @return the access token
     * @throws IOException  if there is a problem
     * @throws DbxException if there is a problem
     */
    public static String getAccessToken() throws IOException, DbxException {



        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

        DbxRequestConfig config = new DbxRequestConfig(
                "JavaTutorial/1.0", Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);

        String authorizeUrl = webAuth.start();
        System.out.println("1. Go to: " + authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first)");
        System.out.println("3. Copy the authorization code.");
        String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();

        DbxAuthFinish authFinish = webAuth.finish(code);
        String accessToken = authFinish.accessToken;

        return accessToken;

    }

    /**
     * main
     *
     * @param args arguments
     * @throws IOException  problem
     * @throws DbxException problem
     */
    public static void main(String[] args) throws IOException, DbxException {

        final String accessToken = " YOUR ACCESS TOKEN HERE";

        DbxRequestConfig config = new DbxRequestConfig(
                "JavaTutorial/1.0", Locale.getDefault().toString());

        DbxClient client = new DbxClient(config, accessToken);
        System.out.println("Linked account: " + client.getAccountInfo().displayName);

        File inputFile = new File("output.log");
        FileInputStream inputStream = new FileInputStream(inputFile);
        try {
            DbxEntry.File uploadedFile = client.uploadFile("/output.log",
                    DbxWriteMode.add(), inputFile.length(), inputStream);
            System.out.println("Uploaded: " + uploadedFile.toString());
        } finally {
            inputStream.close();
        }

        DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
        System.out.println("Files in the root path:");
        for (DbxEntry child : listing.children) {
            System.out.println("   " + child.name + ": " + child.toString());
        }

        FileOutputStream outputStream = new FileOutputStream("output.log-dbx.txt");
        try {
            DbxEntry.File downloadedFile = client.getFile("/output.log", null,
                    outputStream);
            System.out.println("Metadata: " + downloadedFile.toString());
        } finally {
            outputStream.close();
        }


    }
}
