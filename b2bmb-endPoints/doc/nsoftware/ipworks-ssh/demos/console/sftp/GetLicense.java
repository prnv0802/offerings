import ipworksssh.Sftp;

public class GetLicense {


    /**
     * Print out the license for /nsoftware
     * @param args - any arguments
     */
     public static void main(String args[])
     {

         //As2sender component = new As2sender();
         //As2receiver component = new As2receiver();
         //Edireader component = new Edireader();
       
         Sftp component  = new Sftp();

         //Temporary Project
         System.out.println("License:" + component.getRuntimeLicense());

     }
}
