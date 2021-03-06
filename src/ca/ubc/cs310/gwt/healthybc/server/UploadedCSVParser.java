package ca.ubc.cs310.gwt.healthybc.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import ca.ubc.cs310.gwt.healthybc.client.Clinic;
import ca.ubc.cs310.gwt.healthybc.client.ClinicHours;


@SuppressWarnings("serial")
public class UploadedCSVParser extends HttpServlet
{   
	Logger logger = Logger.getLogger("uploadServletLogger");

	
	/**
	 * Pushes the request to POST
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doPost(request, response);
	}

	/**
	 * Receives form and CSV file as a request
	 * @return response: HTML listing parse result
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String resp = "";

		ServletFileUpload upload = new ServletFileUpload();

		try {
			FileItemIterator iterator = upload.getItemIterator(request);

			while (iterator.hasNext()) {

				FileItemStream item = iterator.next();

				if (item.isFormField()) {
					logger.warning("Got a form field: " + item.getFieldName());
					resp += "Error: Got a form field "+ item.getFieldName()+" <br/>";
				} else {

					if(item.getName().endsWith(".csv")){

						resp += readfile(item);

						resp += "<br><b>File "+item.getName()+" successfully uploaded</b><br/>";
						//logger.warning("File: "+item.getName());
					} else {
						logger.warning("File: " + item.getName());
						resp += "<br><b>Error: File "+item.getName()+" is not a CSV file</b><br/>";
					}
				}

			}
		} catch (Exception e){
			logger.warning("Exception occurred");
			resp += "Error: Exception occurred<br/>" + e.getMessage();
		} finally {

			response.setContentType("text/html");

			PrintWriter out = response.getWriter();

			out.println("<html>");
			out.println("<head>");
			out.println("<title>Server Response</title>");
			out.println("</head>");
			out.println("<body>");
			out.println("</body>");

			out.println(resp);

			out.println("</html>");
			out.close();
		}

	} 

	/**
	 * Parses each line in CSV file
	 * @param .CSV file to parse
	 * @return parse result in a String
	 */
	private String readfile(FileItemStream item) throws IOException {
		String message = "";

		InputStream filecontent = item.openStream();

		RemoteDataManager rdm = new RemoteDataManager();

		BufferedReader br = new BufferedReader(new InputStreamReader(filecontent));

		String line = "";
		String cvsSplitBy = ";";

		while ((line = br.readLine()) != null) {			
			
			// use semicolon as separator
			String[] cells = line.split(cvsSplitBy);

			String name = cells[0];
			String refID = cells[1];
			String phone = cells[2];
			//String website = cells[3];
			String email = cells[4];
			//String wc_acess = cells[5];
			String languages = cells[6];
			String street_no = cells[7];
			String street_name = cells[8];
			String street_type = cells[9];
			String city = cells[10];
			String pcode = cells[11];
			Double lat = Double.parseDouble(cells[12]);
			Double lon = Double.parseDouble(cells[13]); 
			//String desc = cells[14];
			String hours = cells[15];

			String address = street_no + " " + street_name + " " + street_type + " " + city;
			ClinicHours newhrs = new ClinicHours(hours);

			Clinic newClinic = new Clinic(refID, name, newhrs, lat, lon, address, pcode, email, phone, languages);
			rdm.addAndUploadClinicEntity(newClinic);
			
			message += "Added new Clinic: " + newClinic.getName() + "<br>";
		}		
		br.close();
		return message;
	}
}