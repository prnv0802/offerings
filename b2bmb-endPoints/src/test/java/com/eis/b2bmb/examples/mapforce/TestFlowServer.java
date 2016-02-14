package com.eis.b2bmb.examples.mapforce;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by Envista Tech on 10/23/2014.
 */
public class TestFlowServer {

     @Test
     public void testFlowServer() throws IOException {
         /*
         String fileName = "C:\\MapFiles\\856.edi";
         String url = "http://localhost:5656/service/Map856";

         HttpClient client = HttpClientBuilder.create().build();

         File file = new File(fileName);
         HttpPost post = new HttpPost(url);

         FileInputStream inputStream = new FileInputStream(file);

         MultipartEntityBuilder builder = MultipartEntityBuilder.create();
         builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);


         builder.addBinaryBody("file", inputStream, ContentType.DEFAULT_TEXT, fileName);

         HttpEntity entity = builder.build();

         post.setEntity(entity);
         HttpResponse response = client.execute(post);

         System.out.println("response Status:" + response.getStatusLine().getStatusCode());
         System.out.println("Msg:" + response.getStatusLine().getReasonPhrase());
         
         InputStream in = response.getEntity().getContent();

        BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
        String inputLine;
        while ((inputLine = inReader.readLine()) != null)
            System.out.println(inputLine);
        in.close();
        */
     }

}
