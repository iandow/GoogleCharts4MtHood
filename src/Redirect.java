import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftTagTypes;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StreamedSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class Redirect extends HttpServlet {
   /* private static class Record {
        Integer temp6540;
        Integer temp5380;
        Integer rh6540;
        Integer rh5380;
        Integer windavg;
        Integer windmax;
        Integer winddir;
        Integer hourprec;
        Integer totalprec;
        Integer daysnow;
        Integer totalsnow;
        Integer pressmb;
    }*/
    static List<List> records;

    public static void getData(PrintWriter out) throws InterruptedException, IOException {
        List<String> this_record;
        records = new ArrayList<List>();
        String sourceUrlString = "";
        Source source=null;
        StreamedSource streamed_source = null;

        try {


            sourceUrlString = "http://www.nwac.us/weatherdata/mthoodmeadows/10day/";
            source=new Source(new URL(sourceUrlString));
            streamed_source=new StreamedSource(new URL(sourceUrlString));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        Pattern pattern;
        Matcher matcher;
        if (sourceUrlString.indexOf(':')==-1) sourceUrlString="file:"+sourceUrlString;
        MicrosoftTagTypes.register();
        MasonTagTypes.register();

        BufferedReader here = new BufferedReader(new StringReader(source.toString()));
        String thisLine;

        int i=0;
        while ((thisLine = here.readLine()) != null) {
            i++;
            //out.println((i + ": " + thisLine));
            pattern = Pattern.compile("\\d+\\s+");
            matcher = pattern.matcher(thisLine);

            this_record = new ArrayList<String>();
            while (matcher.find()) {
                String datapoint = matcher.group().trim();
                //out.println("<BR>Found: " + datapoint);
                this_record.add(datapoint);
            }
            if (this_record.size() == 14) {
                //while (records.size() > 20) { records.remove(0); }
                //out.println(("<BR>this_record size " + this_record.size() + ": " + this_record + ""));
                records.add(this_record);
                //out.println("<BR>records: " + records);
            }

        }

        source=new Source(new URL(sourceUrlString));
        streamed_source=new StreamedSource(new URL(sourceUrlString));

    }



    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {
            getData(out);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        //out.println("<P>" + records.size() + " records saved:</P>" + records.toString());
//        for (int i=0; i<records.size(); i++) {
//                List<String> record = records.get(i);
//                Integer hour = (Integer.valueOf(record.get(2)))/100;
//                out.println("<P>(2012," + record.get(0) + "," + record.get(1) + "," + hour + "), " + record.get(3) + ", " + record.get(4) + "</P>");
//        }

        out.println("<html>\n<head>\n"+
                "<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>" +
                "<script type=\"text/javascript\">" +
                "google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});" +
                "google.setOnLoadCallback(drawChart);" +
                "function drawChart() {" +
                "var data = new google.visualization.DataTable();" +
                "data.addColumn('datetime', 'Date');" +
                "data.addColumn('number', 'Temperature at 6540ft');" +
                "data.addColumn('number', 'Temperature at 5380ft');"+
                "data.addRows([");

        for (int i=0; i<records.size(); i++) {
            List<String> record = records.get(i);
            Integer month = (Integer.valueOf(record.get(0)))-1;
            Integer hour = (Integer.valueOf(record.get(2)))/100;
            out.println("[new Date(2012," + month + "," + record.get(1) + "," + hour + "), " + record.get(3) + ", " + record.get(4) + "]");
            if (i < ((records.size())-1)) {
                out.println(",");
            } else {
                out.println("]);");
              }
        }

                out.println("var options = {" +
                "width: 1000, height: 240," +
                "title: 'Temperature on Mt. Hood', legend: {position: 'top'}, vAxis: {title: 'Temp F'}, hAxis: {title: 'Time (PT)', format:'MMM d hh:mm'}};" +
                "var chart = new google.visualization.LineChart(document.getElementById('chart_div'));" +
                "chart.draw(data, options);" +
                "}" +
                "</script>" +
                "</head>" +
                "<body>" +
                "<div id=\"chart_div\"></div>" +
                "</body></html>");

    }
}
