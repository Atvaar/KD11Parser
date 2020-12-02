package kd11parser;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files; 
import java.nio.file.*; 
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class kd11parser {
    private static Connection conn;
    private static String server = "ELEREC-PC02\\SQLEXPRESSHDDDB";
    private static String dbName = "HDD_Records";
    private static String tableUsed = "MAIN$";
    
  public static void main(String argv[]) {
      String user = "ESDTester";
      String pass = "ESDTester";
      String rptPath;
      //Connection conn;
      String url;
      
      switch (argv.length){
        case 2:
          user = argv[0];
          pass = argv[1];
          rptPath = "\\\\Echo-App\\Killdisk\\";
          System.out.println("defaulting file path to :" + rptPath);
          try{
              //SQL authentication
              url = "jdbc:sqlserver://" + server + ";databaseName=" + dbName;
              Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
              conn = DriverManager.getConnection(url, user, pass);
            }  catch (Exception e) {
                e.printStackTrace();
                System.out.println("##Failed Connection##");
            }
          break;
        case 3:
          user = argv[0];
          pass = argv[1];
          rptPath = argv[2];
          try{
              //SQL authentication
              url = "jdbc:sqlserver://;servername=ELEREC-PC02\\SQLEXPRESSHDDDB;DatabaseName=HDD_Records;user=ESDTester;password=ESDTester";
              //url = "jdbc:sqlserver://;servername=10.105.10.138\\SQLEXPRESSHDDDB;DatabaseName=HDD_Records;user=ESDTester;password=ESDTester";
              //url = "jdbc:sqlserver://" + server + ";databaseName=" + dbName;
              Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
              conn = DriverManager.getConnection(url, user, pass);
            }  catch (Exception e) {
                e.printStackTrace();
                System.out.println("##Failed Connection##");
            }
          break;
        default:
          System.out.println("Incomplete call.  Please restart the program with parameters as follows:");
          System.out.println("     java -jar kd11parser.java username password path_to_files");
          rptPath = "\\\\Echo-App\\Killdisk\\";
          try{
              //SQL authentication
              //url = "jdbc:sqlserver:/" + server + ";databaseName=" + dbName;
              url = "jdbc:sqlserver://;servername=ELEREC-PC02\\SQLEXPRESSHDDDB;DatabaseName=HDD_Records;user=ESDTester;password=ESDTester";            
              //url = "jdbc:sqlserver://;servername=10.105.10.138\\SQLEXPRESSHDDDB;DatabaseName=HDD_Records;user=ESDTester;password=ESDTester";
              Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
              conn = DriverManager.getConnection(url);
              //conn = DriverManager.getConnection(url, user, pass);
            }  catch (Exception e) {
                e.printStackTrace();
                System.out.println("##Failed Connection##");
            }
          break;
        }
        File[] files = new File(rptPath).listFiles(new FilenameFilter() {public boolean accept(File dir, String name) {return name.toLowerCase().endsWith(".xml");}});
        int myCount = files.length;
        System.out.println(myCount);
        
        for (int i = 0; i < myCount; i++){
            System.out.println("File " + (i + 1) + " of " + (myCount));
            String checkIt = workIt(files[i].getName(),rptPath);
            System.out.println(checkIt);
            
            if (checkIt.contentEquals("FAILED")){//if upload and read ok move to archive if fail move to Error
                Boolean moveCheck = moveIt(false, files[i].getName(),rptPath);
                if (moveCheck == false){
                    System.out.println("FAILED TO UPLOAD DATA");
                    //System.exit(8);
                }
            } else {
                Boolean moveCheck = moveIt(true, files[i].getName(),rptPath);
                if (moveCheck == false){
                    System.out.println("FAILED TO MOVE FILE");
                    //System.exit(9);
                }
            }//if for archive or error  
        }
    }
  private static String workIt(String fileName, String reportPath){
      String reportString = reportPath +fileName;
      System.out.println(reportString);
      String outPutString;
         try {
            File fXmlFile = new File(reportString);DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
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
            if (!uploadData(serialNum, startTime, ModelNum, hddsize, protocol, attachment, eraseMethod, WipeAndSessionEnd, WipeStatus, Tech)){
                return "FAILED";
            }
        } catch (Exception e) {e.printStackTrace(); return "FAILED";}
      return outPutString;
    }//end workIt
  
  private static Boolean moveIt(Boolean gonogo, String fileToMove, String Root){
      //if gonogo = true set destination to Archive else if gonogo = false set destination to Error
      String folder;
      String MoveMe;
      if (gonogo){
          folder = "Archive\\";
      } else {
          folder = "Error\\";
      }
      MoveMe = Root + folder + fileToMove;
       try { 
            Path temp = Files.move(Paths.get(Root + fileToMove),Paths.get(MoveMe));
            if(temp != null){ 
                System.out.println("File renamed and moved successfully"); 
            } else{ 
                System.out.println("Failed to move the file");
            }
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }//end moveIt
  
  private static Boolean uploadData(String serialNum,Date startTime,String ModelNum, String hddsize, String protocol, String attachment, String eraseMethod, Date WipeAndSessionEnd, String WipeStatus,String Tech){
    //System.out.println("WTF Test");
    try{
        String SQL = "INSERT INTO [HDD_Records].[dbo].[" + tableUsed + "](HDSerial, WipeReport, HDModel, HDCapacity,HDProtocol,HDAttachment, EraseMethod, WipeStart, WipeFinished, SessionStart, SessionEnded, WipeStatus, TechName) VALUES('"+ serialNum
            + "','" + startTime + "','" + ModelNum + "','" + hddsize + "','" + protocol + "','" + attachment + "','" + eraseMethod + "','" + startTime
            + "','" + WipeAndSessionEnd + "','" + startTime + "','"
            + WipeAndSessionEnd + "','" + WipeStatus + "','" + Tech + "')";
        System.out.println("Inserting data with: " + SQL);
        Statement stater =  conn.createStatement();
        stater.executeUpdate(SQL);
        return true;
    }catch (Exception e) {
        e.printStackTrace();
        System.out.println("FAILED TO UPLOAD: " + serialNum);//if it fails display message
        return false;
    }
  }//end uploadData
}//end class
