package com.eis.b2bmb.examples.dropbox;

import com.dropbox.core.*;

import java.io.*;
import java.util.Locale;

/**
 * Created by Envista Tech on 9/25/2014.
 */
public class DropboxTest {


    public static void main(String[] args) throws IOException, DbxException {

        final String ACCCESS_CODE = "mi0z0fCyG4sAAAAAAAAADy-Tg8p9QIN77EcyZ3sEQjUmH3rDnUlhV7h6Uvk01ZYt";

        final String APP_KEY = "kyi4vt7558xmo60";
        final String APP_SECRET = "37498zfr118rz7k";

        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

        DbxRequestConfig config = new DbxRequestConfig(
                "JavaTutorial/1.0", Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        /*
        String authorizeUrl = webAuth.start();

        System.out.println("1. Go to: " + authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first)");
        System.out.println("3. Copy the authorization code.");
        String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();

        DbxAuthFinish authFinish = webAuth.finish(code);
        String accessToken = authFinish.accessToken;
        System.out.println("Access Token:"+accessToken);
        */
        DbxClient client = new DbxClient(config, ACCCESS_CODE);
        System.out.println("Linked account: " + client.getAccountInfo().displayName +" User Id:"+client.getAccountInfo().userId);

        DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
        System.out.println("Files in the root path:");
        for (DbxEntry child : listing.children) {
            System.out.println("	" + child.name + ": " + child.toString());
        }

        /*
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
            System.out.println(" " + child.name + ": " + child.toString());
        }

        FileOutputStream outputStream = new FileOutputStream("output.log-dbx.txt");
        try {
            DbxEntry.File downloadedFile = client.getFile("/output.log", null,
                    outputStream);
            System.out.println("Metadata: " + downloadedFile.toString());
        } finally {
            outputStream.close();
        }

        */

    }

}
