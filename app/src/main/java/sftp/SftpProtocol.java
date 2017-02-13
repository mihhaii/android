package sftp;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import static org.junit.Assert.assertTrue;

/**
 * Created by mp_13 on 1/21/2017.
 */
public class SftpProtocol {

    // Creating FTP Client instance
    FTPSClient ftp = null;

    // Constructor to connect to the FTP Server
    public SftpProtocol(String host, int port, String username, String password) throws Exception{

        ftp = new FTPSClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;
        ftp.connect(host,port);
        System.out.println("FTP URL is:"+ftp.getDefaultPort());
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }
        ftp.login(username, password);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
        ftp.execPBSZ(0);
        ftp.execPROT("P");
    }


    // Method to upload the File on the FTP Server
    public void uploadFTPFile(String localFileFullName, String fileName, String hostDir)
            throws Exception
    {
        try {
            InputStream input = new FileInputStream(new File(localFileFullName));

            this.ftp.storeFile(hostDir + fileName, input);
        }
        catch(Exception e){

        }
    }

    // Method to upload the File on the FTP Server
    public void uploadFTPFile(File file, String hostDir)
            throws Exception
    {
        try {
            InputStream input = new FileInputStream(file);

            this.ftp.storeFile(hostDir, input);
        }
        catch(Exception e){
           e.printStackTrace();


        }
    }

    // Download the FTP File from the FTP Server
    public void downloadFTPFile(String source, String destination) {
        try (FileOutputStream fos = new FileOutputStream(destination)) {
            this.ftp.retrieveFile(source, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // list the files in a specified directory on the FTP
    public boolean listFTPFiles(String directory, String fileName) throws IOException {
        // lists files and directories in the current working directory
        boolean verificationFilename = false;
        FTPFile[] files = ftp.listFiles(directory);
        for (FTPFile file : files) {
            String details = file.getName();
            System.out.println(details);
            if(details.equals(fileName))
            {
                System.out.println("Correct Filename");
                verificationFilename=details.equals(fileName);
                assertTrue("Verification Failed: The filename is not updated at the CDN end.",details.equals(fileName));
            }
        }

        return verificationFilename;
    }

    // Disconnect the connection to FTP
    public void disconnect(){
        if (this.ftp.isConnected()) {
            try {
                this.ftp.logout();
                this.ftp.disconnect();
            } catch (IOException f) {
                // do nothing as file is already saved to server
            }
        }
    }


    private static void showServerReply(FTPSClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }
   /* public static void main(String[] args) {
        String server = "79.116.63.35";
        int port = 990;
        String user = "admin";
        String pass = "admin";
        FTPSClient ftpClient = new FTPSClient("TLS",false);
        try {
            ftpClient.connect(server, port);
            showServerReply(ftpClient);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("Operation failed. Server reply code: " + replyCode);
                return;
            }
            boolean success = ftpClient.login(user, pass);
            showServerReply(ftpClient);
            if (!success) {
                System.out.println("Could not login to the server");
                return;
            } else {
                System.out.println("LOGGED IN SERVER");
            }
        } catch (IOException ex) {
            System.out.println("Oops! Something wrong happened");
            ex.printStackTrace();
        }
    }*/

    // Main method to invoke the above methods
    public static void main(String[] args) {
        try {
            SftpProtocol ftpobj = new SftpProtocol("79.116.63.35", 990, "admin", "admin");
            ftpobj.uploadFTPFile("C:\\Users\\mp_13\\Hello.txt", "Hello.txt", "/");
          //  ftpobj.downloadFTPFile("Shruti.txt", "/users/shruti/Shruti.txt");
        //    System.out.println("FTP File downloaded successfully");
         //   boolean result = ftpobj.listFTPFiles("/users/shruti", "shruti.txt");
        //    System.out.println(result);
            ftpobj.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}
