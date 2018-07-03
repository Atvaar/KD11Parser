package kd11parser;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.sql.*;
import org.xml.sax.InputSource;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;



public class kd11parser {

  public static void main(String argv[]) {
      String user;
      String pass;
      String rptPath;
      String server;
      String tableUsed = "MAIN$";
      Connection conn;
      String url;
      
      switch (argv.length){
        case 2:
          user = argv[0];
          pass = argv[1];
          rptPath = "C:\\KDDATA\\KDParseFolder\\";
          System.out.println("defaulting file path to :" + rptPath);
          server = "ELEREC-PC02\\SQLEXPRESSHDDDB";
          try{
              //SQL authentication
              url = "jdbc:sqlserver://ELEREC-PC02\\SQLEXPRESSHDDDB;databaseName=HDD_Records";
              Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
              conn = DriverManager.getConnection(url, user, pass);
            }  catch (Exception e) {e.printStackTrace();}
          break;
        case 3:
          user = argv[0];
          pass = argv[1];
          rptPath = argv[2];
          server = "ELEREC-PC02\\SQLEXPRESSHDDDB";
          try{
              //SQL authentication
              url = "jdbc:sqlserver://MYPC\\SQLEXPRESS;databaseName=MYDB";
              Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
              conn = DriverManager.getConnection(url, user, pass);
            }  catch (Exception e) {e.printStackTrace();}
          break;
        default:
          System.out.println("Incomplete call.  Please restart the program with parameters as follows:");
          System.out.println("     java -jar kd11parser.java username password path_to_files");
          break;
        }
        /*Microsoft authentication
        String url = "jdbc:sqlserver://MYPC\\SQLEXPRESS;databaseName=MYDB;integratedSecurity=true";
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        Connection conn = DriverManager.getConnection(url);
        */
        
        //taike argv as directory to look at
        //build loop to read first xml process then delete file repeat till done.
        //put in loop for each XML file
        try {
            File fXmlFile = new File("C://Users/aross/Documents/NetBeansProjects/KD11Parser/src/kd11parser/Report-JP1572FL2E90ZK-Success-2018-05-01-08-53-53.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
        
            //begin reading the data
            NodeList nList = doc.getElementsByTagName("label");
            Node nNode = nList.item(0);
        
            //get serial model and size data
            Element eElement = (Element) nNode;
            String madData = eElement.getAttribute("value");//this is all of the following few stats.
            String [] madSplit = madData.split(", |: | \\[|\\]");
            String serialNum = madSplit[2];
        
            //remove partnumbers and WD- before upload
            serialNum = serialNum.replace("WD-","");
        
            //if found at front remove these part numbers
            String ModelNum = madSplit[0];
            String hddsize = madSplit[3];
            String protocol;
            
            //seperating model number from device type because killdisk is inconsistant.
            if (madSplit[0].endsWith(" ATA Device")){
                protocol = "ATA";
                ModelNum = ModelNum.replace(" ATA Device", "");
            } else if (madSplit[0].contains("ATA     ")){
                protocol = "ATA";
                ModelNum = ModelNum.replace("ATA     ", "");
            } else {protocol="UNK";}
            String attachment = "SCSI Disk Device";
            System.out.println("Serial Num:" + serialNum);
            
            //remove part numbers for hitachi fujitsu, and WD-
            System.out.println("Model Num:" + ModelNum);
            System.out.println("HDD Size:"+hddsize);
            System.out.println("Protocol:"+protocol);
            System.out.println("Attachment:"+attachment);
        
            Timestamp startTime;
            String eraseMethod;
            Timestamp WipeAndSessionEnd;
            String WipeStatus;
        
            nList = doc.getElementsByTagName("method");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            eraseMethod = eElement.getAttribute("value");
            System.out.println("Erase Method:"+eraseMethod);
        
            nList = doc.getElementsByTagName("result");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            WipeStatus = eElement.getElementsByTagName("conclusion").item(0).getTextContent();
            System.out.println("Result:"+WipeStatus);
            
            if (WipeStatus.equals("Disk Erase completed successfully")){
                WipeStatus = "PASSED";
                }
            else if(WipeStatus.equals("Disk Erase completed")){
                WipeStatus = "PASSED";
                }
            else if(WipeStatus.equals("Disk Erase completed with errors")){
                WipeStatus = "FAILED";
                }
            else {
                WipeStatus = "FAILED";
                }
            
            //if statement to convert to PASSED, FAILED, STOPPED
            System.out.println("Wipe Status:"+WipeStatus);
            
            //get start time stamp & start session
            nList = doc.getElementsByTagName("disk");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            String convertDT = eElement.getElementsByTagName("started").item(0).getTextContent();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = dateFormat.parse(convertDT);
            startTime = new Timestamp(date.getTime());
            System.out.println("Start Time: "+startTime);
            
            //get end time //Will have to convert start time and Add elapsed time()()()()()()()()()
            nList = doc.getElementsByTagName("elapsed");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            String elapsed =eElement.getAttribute("value");
            String elapsedTime[] = eElement.getAttribute("value").split(":");
            System.out.println("Elapsed Time: "+elapsed);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR_OF_DAY,Integer.parseInt(elapsedTime[0]));
            cal.add(Calendar.MINUTE,Integer.parseInt(elapsedTime[1]));
            cal.add(Calendar.SECOND,Integer.parseInt(elapsedTime[2]));
            date = cal.getTime();
            WipeAndSessionEnd = new Timestamp(date.getTime());
            System.out.println("The END: "+WipeAndSessionEnd);
            String reportString = serialNum+","+startTime+","+ModelNum+","+hddsize+","+protocol+","+attachment+","+eraseMethod+","+startTime+","+WipeAndSessionEnd+","+startTime+","+WipeAndSessionEnd+","+WipeStatus;
            System.out.println(reportString);
            
            //network upload and .err file if fail upload
            //erase file when complete.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  private String workIt(String fileName){
      String reportString = fileName;
         try {
            File fXmlFile = new File("C://Users/aross/Documents/NetBeansProjects/KD11Parser/src/kd11parser/Report-JP1572FL2E90ZK-Success-2018-05-01-08-53-53.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
        
            //begin reading the data
            NodeList nList = doc.getElementsByTagName("label");
            Node nNode = nList.item(0);
        
            //get serial model and size data
            Element eElement = (Element) nNode;
            String madData = eElement.getAttribute("value");//this is all of the following few stats.
            String [] madSplit = madData.split(", |: | \\[|\\]");
            String serialNum = madSplit[2];
        
            //remove partnumbers and WD- before upload
            serialNum = serialNum.replace("WD-","");
        
            //if found at front remove these part numbers
            String ModelNum = madSplit[0];
            String hddsize = madSplit[3];
            String protocol;
            
            //seperating model number from device type because killdisk is inconsistant.
            if (madSplit[0].endsWith(" ATA Device")){
                protocol = "ATA";
                ModelNum = ModelNum.replace(" ATA Device", "");
            } else if (madSplit[0].contains("ATA     ")){
                protocol = "ATA";
                ModelNum = ModelNum.replace("ATA     ", "");
            } else {protocol="UNK";}
            String attachment = "SCSI Disk Device";
            System.out.println("Serial Num:" + serialNum);
            
            //remove part numbers for hitachi fujitsu, and WD-
            System.out.println("Model Num:" + ModelNum);
            System.out.println("HDD Size:"+hddsize);
            System.out.println("Protocol:"+protocol);
            System.out.println("Attachment:"+attachment);
        
            Timestamp startTime;
            String eraseMethod;
            Timestamp WipeAndSessionEnd;
            String WipeStatus;
        
            nList = doc.getElementsByTagName("method");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            eraseMethod = eElement.getAttribute("value");
            System.out.println("Erase Method:"+eraseMethod);
        
            nList = doc.getElementsByTagName("result");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            WipeStatus = eElement.getElementsByTagName("conclusion").item(0).getTextContent();
            System.out.println("Result:"+WipeStatus);
            
            if (WipeStatus.equals("Disk Erase completed successfully")){
                WipeStatus = "PASSED";
                }
            else if(WipeStatus.equals("Disk Erase completed")){
                WipeStatus = "PASSED";
                }
            else if(WipeStatus.equals("Disk Erase completed with errors")){
                WipeStatus = "FAILED";
                }
            else {
                WipeStatus = "FAILED";
                }
            
            //if statement to convert to PASSED, FAILED, STOPPED
            System.out.println("Wipe Status:"+WipeStatus);
            
            //get start time stamp & start session
            nList = doc.getElementsByTagName("disk");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            String convertDT = eElement.getElementsByTagName("started").item(0).getTextContent();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = dateFormat.parse(convertDT);
            startTime = new Timestamp(date.getTime());
            System.out.println("Start Time: "+startTime);
            
            //get end time //Will have to convert start time and Add elapsed time()()()()()()()()()
            nList = doc.getElementsByTagName("elapsed");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            String elapsed =eElement.getAttribute("value");
            String elapsedTime[] = eElement.getAttribute("value").split(":");
            System.out.println("Elapsed Time: "+elapsed);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR_OF_DAY,Integer.parseInt(elapsedTime[0]));
            cal.add(Calendar.MINUTE,Integer.parseInt(elapsedTime[1]));
            cal.add(Calendar.SECOND,Integer.parseInt(elapsedTime[2]));
            date = cal.getTime();
            WipeAndSessionEnd = new Timestamp(date.getTime());
            System.out.println("The END: "+WipeAndSessionEnd);
            reportString = serialNum+","+startTime+","+ModelNum+","+hddsize+","+protocol+","+attachment+","+eraseMethod+","+startTime+","+WipeAndSessionEnd+","+startTime+","+WipeAndSessionEnd+","+WipeStatus;
            System.out.println(reportString);
            
        } catch (Exception e) {e.printStackTrace();}
      return reportString;
    }

}
