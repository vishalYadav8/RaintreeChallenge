import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportGenerator {
    private static final String FOLDER_PATH = "C:\\Users\\sunil kumar\\Desktop\\RT Test\\QoS_logs\\H--RAINTREE-PARKER94-\\";
    private static  final String REPORT_PATH="C:\\Users\\sunil kumar\\Desktop\\RT Test\\QoS_logs\\";
    private static LocalDate startDate;
    private static LocalDate endDate;
    private static final String disconnectMessage = " Client is disconnected from agent.";

    public Map<String, Integer> filterDisconnectRecords(Path folderPath,String message) {
        LocalDate recordDate;
        Map<String, Integer> reportMap = new HashMap<>();
        //fetch file Names in FOLDER_PATH
        List<String> fileNames=fetchFileNames(folderPath);
        // go through the list of files
        for (String file : fileNames) {
            try {
                // put the file's name and its content into the data structure
                List<String> lines = Files.readAllLines(folderPath.resolve(file));
                for (String s : lines) {
                    if(s.equals(""))
                        continue;
                    String[] filteredData = s.split("\\|");
                    //assuming record year is of same as of file date year
                    recordDate = LocalDate.parse(filteredData[1].substring(filteredData[1].indexOf("(") + 1, filteredData[1].indexOf("(") + 6) + "/" + file.subSequence(file.lastIndexOf("\\")+1,file.lastIndexOf("\\")+5), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    if (checkDateInRange(recordDate) && filteredData[2].contains(message)) {

                        String computerName = filteredData[0].substring(filteredData[0].indexOf(":") + 1, filteredData[0].indexOf(" ", filteredData[0].indexOf(":")));
                        if (reportMap.containsKey(computerName)) {
                            reportMap.replace(computerName, reportMap.get(computerName) + 1);
                        } else {
                            reportMap.put(computerName, 1);
                        }
                    }
                }
                //linesOfFiles.put(file, lines);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return reportMap;
    }

    private Boolean checkDateInRange(LocalDate recordDate) {
        return recordDate.isBefore(endDate) && recordDate.isAfter(startDate);
    }

    private static LocalDate convertToDate(String input) {
        return LocalDate.parse(input, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
    public static  List<String> fetchFileNames(Path folderPath) {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderPath))
    {
        for (Path path : directoryStream) {
            fileNames.add(path.toString());
        }
    }
     catch(IOException ex)
    {
        System.err.println("Error reading files");
        ex.printStackTrace();
    }
       return fileNames;
}

public static File createReport(Map<String,Integer> inputDetails,String path)
{
    File reportFile=null;
  try {
    reportFile=new File(path);

    BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile,true));
    writer.append("Date Range: ").append(String.valueOf(startDate)).append(" to ").append(String.valueOf(endDate));
    writer.newLine();
    writer.newLine();
    writer.append("Computer Name Number of Disconnects");
    writer.newLine();
      inputDetails.forEach((key, value) -> {
          try {
              writer.append(key).append(" ").append(String.valueOf(value));
              writer.newLine();
          } catch (IOException e) {
              e.printStackTrace();
          }
      });
    writer.close();
}
  catch (IOException e)
  {
      e.printStackTrace();
  }
return reportFile;
}
    public static void main(String[] args) throws IOException {
        ReportGenerator reader=new ReportGenerator();
        StringBuilder builtPath=new StringBuilder();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please Specify Date Range to generate report");
        System.out.println("Please Enter the start Date");
        startDate = convertToDate(scanner.next());
        System.out.println("Enter the end date ");
        endDate = convertToDate(scanner.next());
        Path folderPath= Paths.get(FOLDER_PATH);
        Map<String,Integer> s=reader.filterDisconnectRecords(folderPath,disconnectMessage);
        File reportFile=createReport(s,builtPath.append(REPORT_PATH).append("ReportFile").append(LocalDate.now()).append(".txt").toString());
        FileReader fr=new FileReader(reportFile);
        int i;
        while((i=fr.read())!=-1)
            System.out.print((char)i);
        fr.close();
    }
}
