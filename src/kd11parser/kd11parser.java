package kd11parser;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.*; 
import java.nio.file.Files; 
import java.nio.file.*; 

import java.sql.*;
//import org.xml.sax.InputSource;
//import java.io.StringReader;
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
          rptPath = "Z://";
          System.out.println("defaulting file path to :" + rptPath);
          server = "ELEREC-PC02\\SQLEXPRESSHDDDB";
          try{
              //SQL authentication
              url = "jdbc:sqlserver://ELEREC-PC02\\SQLEXPRESSHDDDB;databaseName=HDD_Records";
              Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
              conn = DriverManager.getConnection(url, user, pass);
            }  catch (Exception e) {e.printStackTrace(); System.out.println("##Failed Connection##");}
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
            }  catch (Exception e) {e.printStackTrace(); System.out.println("##Failed Connection##");}
          break;
        default:
          System.out.println("Incomplete call.  Please restart the program with parameters as follows:");
          System.out.println("     java -jar kd11parser.java username password path_to_files");
          rptPath = "Z://";
          break;
        }
        /*Microsoft authentication
        String url = "jdbc:sqlserver://MYPC\\SQLEXPRESS;databaseName=MYDB;integratedSecurity=true";
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        Connection conn = DriverManager.getConnection(url);
        
        taike argv as directory to look at
        build loop to read first xml process then delete file repeat till done.
        put in loop for each XML file
        */
        
        //if directory has xml files do...
        //read first file name
        //decode first file
        System.out.println("###-Function Output-###");
        System.out.println(workIt("Report-6VV3TLC4-Success-2019-06-03-11-21-23.xml",rptPath));
        //upload file to log and database
        //move complete file to new folder      
        
    }
  private static String workIt(String fileName, String reportPath){
      String reportString = fileName;
      String outPutString;
         try {
            File fXmlFile = new File(reportPath + fileName);DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
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
        
            //remove partnumbers and WD- before upload and known part numbers
            serialNum = serialNum.replace("WD-","");
            serialNum = serialNum.replace("JP1570","");
            serialNum = serialNum.replace("JP1572","");
            serialNum = serialNum.replace("JP1530","");
            serialNum = serialNum.replace("JP1532","");
            serialNum = serialNum.replace("JPB570","");
            serialNum = serialNum.replace("JP1592","");
            serialNum = serialNum.replace("VFC200","");
            serialNum = serialNum.replace("VFA200","");
            serialNum = serialNum.replace("VFG200","");
            serialNum = serialNum.replace("VFJ200","");
            serialNum = serialNum.replace("VFJ201","");
            serialNum = serialNum.replace("SCA207","");
            serialNum = serialNum.replace("SCK207","");
            serialNum = serialNum.replace("SCA2N7","");
            serialNum = serialNum.replace("GEM030","");
            serialNum = serialNum.replace("GEM330","");
            serialNum = serialNum.replace("GEK333","");
            serialNum = serialNum.replace("GEK033","");
            serialNum = serialNum.replace("GEK060","");
            serialNum = serialNum.replace("GEM360","");
            serialNum = serialNum.replace("GEK330","");
            serialNum = serialNum.replace("GEK030","");
            serialNum = serialNum.replace("GEK360","");
            serialNum = serialNum.replace("GEL330","");
            serialNum = serialNum.replace("GEL030","");
            serialNum = serialNum.replace("GEM060","");
        
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
            //System.out.println("Serial Num:" + serialNum);
            
            //remove part numbers for hitachi fujitsu, and WD-
            //System.out.println("Model Num:" + ModelNum);
            //System.out.println("HDD Size:"+hddsize);
            //System.out.println("Protocol:"+protocol);
            //System.out.println("Attachment:"+attachment);
        
            Timestamp startTime;
            String eraseMethod;
            Timestamp WipeAndSessionEnd;
            String WipeStatus;
            String Tech;
        
            nList = doc.getElementsByTagName("method");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            eraseMethod = eElement.getAttribute("value");
            //System.out.println("Erase Method:"+eraseMethod);
        
            nList = doc.getElementsByTagName("result");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            WipeStatus = eElement.getElementsByTagName("conclusion").item(0).getTextContent();
            //System.out.println("Result:"+WipeStatus);
            
            if (WipeStatus.equals("Disk Erase completed successfully")){
                WipeStatus = "PASSED";
            }
            else if(WipeStatus.equals("Disk Erase completed with errors")){
                WipeStatus = "FAILED";
            }
            else {
                WipeStatus = "FAILED";
            }
            //if statement to convert to PASSED, FAILED, STOPPED
            //System.out.println("Wipe Status:"+WipeStatus);
            
            //get start time stamp & start session
            nList = doc.getElementsByTagName("disk");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            String convertDT = eElement.getElementsByTagName("started").item(0).getTextContent();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = dateFormat.parse(convertDT);
            startTime = new Timestamp(date.getTime());
            //System.out.println("Start Time: "+startTime);
            
            //get end time //Will have to convert start time and Add elapsed time()()()()()()()()()
            nList = doc.getElementsByTagName("elapsed");
            nNode = nList.item(0);
            eElement = (Element) nNode;
            String elapsed =eElement.getAttribute("value");
            String elapsedTime[] = eElement.getAttribute("value").split(":");
            //System.out.println("Elapsed Time: "+elapsed);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR_OF_DAY,Integer.parseInt(elapsedTime[0]));
            cal.add(Calendar.MINUTE,Integer.parseInt(elapsedTime[1]));
            cal.add(Calendar.SECOND,Integer.parseInt(elapsedTime[2]));
            date = cal.getTime();
            WipeAndSessionEnd = new Timestamp(date.getTime());
            //System.out.println("The END: "+WipeAndSessionEnd);
            
            //pull the user name for this record
            try{
                nList = doc.getElementsByTagName("technician");
                nNode = nList.item(0);
                eElement = (Element) nNode;
                Tech = eElement.getElementsByTagName("name").item(0).getTextContent();
                //System.out.println("Tech:  "+Tech);
            }
            catch (Exception e) {
                e.printStackTrace();
                Tech = "UNK";
                //System.out.println("Tech:  "+Tech);
            }
            
            outPutString = serialNum+","+startTime+","+ModelNum+","+hddsize+","+protocol+","+attachment+","+eraseMethod+","+startTime+","+WipeAndSessionEnd+","+startTime+","+WipeAndSessionEnd+","+WipeStatus+","+Tech;
            //System.out.println(outPutString);
        } catch (Exception e) {e.printStackTrace(); return "FAILED";}
      return outPutString;
    }//end workIt
  
  private Boolean moveIt(Boolean gonogo, String fileToMove){
      return true;
    }//end moveIt

}//end class
