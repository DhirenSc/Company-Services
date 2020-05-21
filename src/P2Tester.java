import java.io.*;
import org.apache.http.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.message.*;
import org.apache.http.entity.*;
import org.apache.http.util.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.*;
import java.time.*;
import java.time.temporal.*;

class P2Tester {

    /**** NOTE: This file does NOT test all boundaries and all validations ****/

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: \"base context\" \"company name\"");
            System.exit(1);
        }

        try {
            String baseUrl = "http://localhost:8080/"+args[0]+"/resources/CompanyServices/";
            String company = args[1];

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost postRequest;
            HttpGet getRequest;
            HttpPut putRequest;
            HttpDelete deleteRequest;
            HttpEntity entity;

            HttpResponse response;

            List<NameValuePair> params;
            int dept_id = 0;
            int emp_id = 0;
            int timecard_id = 0;
            String json = "";
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = df.format(new Date());
            LocalDate dateToUse;


//insert initial department so will get bad later
            System.out.println("Insert initial department");
            System.out.println("=========================");
            postRequest = new HttpPost(baseUrl+"department");

            params = new ArrayList<NameValuePair>(4);
            params.add(new BasicNameValuePair("company", company));
            params.add(new BasicNameValuePair("dept_name", "mystery"));
            params.add(new BasicNameValuePair("dept_no", "bdfvksTest"));
            params.add(new BasicNameValuePair("location", "RIT"));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            response = client.execute(postRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);

            }

            System.out.println("End InsertInitialDepartment - \n\n\n");

//insert department
            System.out.println("Insert department - BAD");
            System.out.println("=======================");
            postRequest = new HttpPost(baseUrl+"department");

            params = new ArrayList<NameValuePair>(4);
            params.add(new BasicNameValuePair("company", company));
            params.add(new BasicNameValuePair("dept_name", "mystery"));
            params.add(new BasicNameValuePair("dept_no", "bdfvksTest")); //bad
            params.add(new BasicNameValuePair("location", "RIT"));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            response = client.execute(postRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End InsertDepartment - BAD\n\n\n");

            System.out.println("Insert department - GOOD");
            System.out.println("========================");

            postRequest = new HttpPost(baseUrl+"department");

            params = new ArrayList<NameValuePair>(4);
            params.add(new BasicNameValuePair("company", company));
            params.add(new BasicNameValuePair("dept_name", "mystery"));
            params.add(new BasicNameValuePair("dept_no", "testbdfvks"));
            params.add(new BasicNameValuePair("location", "RIT"));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            response = client.execute(postRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);

                dept_id = P2Tester.getId(responseContent,"dept_id");

            } else {
                dept_id = 0;
            }

            System.out.println("End InsertDepartment - GOOD\n\n\n");


//update department
            System.out.println("Update department - BAD");
            System.out.println("=======================");

            putRequest = new HttpPut(baseUrl+"department?");

            json = "{\"company\":\""+company+"\",\"dept_id\":0,\"dept_name\":\"NEW NAME\","+
                    "\"dept_no\":\"bdfvksNEW\",\"location\":\"MAINE\"}";


            putRequest.setEntity(new StringEntity(json,
                    ContentType.APPLICATION_JSON));
            response = client.execute(putRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End UpdateDepartment - BAD\n\n\n");


            System.out.println("Update department - GOOD");
            System.out.println("========================");

            putRequest = new HttpPut(baseUrl+"department");

            json = "{\"company\":\""+company+"\",\"dept_id\":"+dept_id+",\"dept_name\":\"NEW NAME\","+
                    "\"dept_no\":\"bdfvksNEW\",\"location\":\"MAINE\"}";

            putRequest.setEntity(new StringEntity(json,
                    ContentType.APPLICATION_JSON));
            response = client.execute(putRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }
            System.out.println("End UpdateDepartment - GOOD\n\n\n");

//delete department
            System.out.println("Delete department - BAD");
            System.out.println("=======================");

            deleteRequest = new HttpDelete(baseUrl+"department?company="+company+"&dept_id=0");

            response = client.execute(deleteRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End DeleteDepartment - BAD\n\n\n");


            System.out.println("Delete department - GOOD");
            System.out.println("========================");

            deleteRequest = new HttpDelete(baseUrl+"department?company="+company+"&dept_id="+dept_id);

            response = client.execute(deleteRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }


            System.out.println("End DeleteDepartment - GOOD\n\n\n");


//insert department good again
            System.out.println("Insert department - GOOD");
            System.out.println("========================");

            postRequest = new HttpPost(baseUrl+"department");

            params = new ArrayList<NameValuePair>(4);
            params.add(new BasicNameValuePair("company", company));
            params.add(new BasicNameValuePair("dept_name", "mystery"));
            params.add(new BasicNameValuePair("dept_no", "15bdfvks"));
            params.add(new BasicNameValuePair("location", "RIT"));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            response = client.execute(postRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);

                dept_id = P2Tester.getId(responseContent,"dept_id");

            } else {
                dept_id = 0;
            }


            System.out.println("End InsertDepartment - GOOD\n\n\n");

//get department
            System.out.println("Get department - BAD");
            System.out.println("====================");
            getRequest = new HttpGet(baseUrl+"department?company="+company+"&dept_id=0");
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Department - BAD\n\n\n");

            System.out.println("Get department - GOOD");
            System.out.println("=====================");

            getRequest = new HttpGet(baseUrl+"department?company="+company+"&dept_id="+dept_id);
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Department - GOOD\n\n\n");

//get departments
            System.out.println("Get departments");
            System.out.println("===============");
            getRequest = new HttpGet(baseUrl+"departments?company="+company);
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Departments\n\n\n");


//employee

//insert employee
            System.out.println("Insert employee - BAD");
            System.out.println("=======================");
            postRequest = new HttpPost(baseUrl+"employee");

            dateToUse =
                    LocalDate.now( ZoneId.systemDefault() )
                            .with( TemporalAdjusters.previous( DayOfWeek.SUNDAY ) ) ;

            df = new SimpleDateFormat("yyyy-MM-dd");
            dateStr = df.format(java.sql.Date.valueOf(dateToUse));

            params = new ArrayList<NameValuePair>(4);
            params.add(new BasicNameValuePair("company", company));
            params.add(new BasicNameValuePair("emp_name", "French"));
            params.add(new BasicNameValuePair("emp_no", company+"-French1"));
            params.add(new BasicNameValuePair("hire_date", dateStr));
            params.add(new BasicNameValuePair("job","test job"));
            params.add(new BasicNameValuePair("salary", "50000.0"));
            params.add(new BasicNameValuePair("dept_id", String.valueOf(dept_id)));
            params.add(new BasicNameValuePair("mng_id", "0"));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            response = client.execute(postRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End InsertEmployee - BAD\n\n\n");

            System.out.println("Insert employee - GOOD");
            System.out.println("=======================");
            postRequest = new HttpPost(baseUrl+"employee");

            dateToUse =
                    LocalDate.now( ZoneId.systemDefault() )
                            .with( TemporalAdjusters.previous( DayOfWeek.MONDAY ) ) ;

            df = new SimpleDateFormat("yyyy-MM-dd");
            dateStr = df.format(java.sql.Date.valueOf(dateToUse));

            params = new ArrayList<NameValuePair>(4);
            params.add(new BasicNameValuePair("company", company));
            params.add(new BasicNameValuePair("emp_name", "French"));
            params.add(new BasicNameValuePair("emp_no", "bdfvksEmp"));
            params.add(new BasicNameValuePair("hire_date", dateStr));
            params.add(new BasicNameValuePair("job","test job"));
            params.add(new BasicNameValuePair("salary", "50000.0"));
            params.add(new BasicNameValuePair("dept_id", String.valueOf(dept_id)));
            params.add(new BasicNameValuePair("mng_id", "0"));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            response = client.execute(postRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
                emp_id = P2Tester.getId(responseContent,"emp_id");

            } else {
                emp_id = 0;
            }

            System.out.println("End InsertEmployee - GOOD\n\n\n");

//update employee
            System.out.println("Update employee - BAD");
            System.out.println("=======================");

            putRequest = new HttpPut(baseUrl+"employee");

            json = "{\"company\":\""+company+"\",\"emp_id\":0,\"emp_name\":\"french\","+
                    "\"emp_no\":\"bdfvksNew\",\"hire_date\":"+
                    "\""+dateStr+"\",\"job\":\"new job\",\"salary\":60000.0,"+
                    "\"dept_id\":"+dept_id+",\"mng_id\":0}";

            putRequest.setEntity(new StringEntity(json,
                    ContentType.APPLICATION_JSON));
            response = client.execute(putRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End UpdateEmployee - BAD\n\n\n");


            System.out.println("Update Employee - GOOD");
            System.out.println("========================");

            putRequest = new HttpPut(baseUrl+"employee");

            json = "{\"company\":\""+company+"\",\"emp_id\":"+emp_id+",\"emp_name\":\"french\","+
                    "\"emp_no\":\"bdfvksTest\",\"hire_date\":"+
                    "\""+dateStr+"\",\"job\":\"new job\",\"salary\":60000.0,"+
                    "\"dept_id\":"+dept_id+",\"mng_id\":0}";

            putRequest.setEntity(new StringEntity(json,
                    ContentType.APPLICATION_JSON));
            response = client.execute(putRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End UpdateEmployee - GOOD\n\n\n");

//delete employee
            System.out.println("Delete Employee - BAD");
            System.out.println("=======================");

            deleteRequest = new HttpDelete(baseUrl+"employee?company="+company+"&emp_id=0");

            response = client.execute(deleteRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End DeleteEmployee - BAD\n\n\n");


            System.out.println("Delete Employee - GOOD");
            System.out.println("========================");

            deleteRequest = new HttpDelete(baseUrl+"employee?company="+company+"&emp_id="+emp_id);

            response = client.execute(deleteRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }


            System.out.println("End DeleteEmployee - GOOD\n\n\n");


//insert employee good again
            System.out.println("Insert employee - GOOD");
            System.out.println("========================");

            postRequest = new HttpPost(baseUrl+"employee");

            dateToUse =
                    LocalDate.now( ZoneId.systemDefault() )
                            .with( TemporalAdjusters.previous( DayOfWeek.MONDAY ) ) ;

            df = new SimpleDateFormat("yyyy-MM-dd");
            dateStr = df.format(java.sql.Date.valueOf(dateToUse));

            params = new ArrayList<NameValuePair>(4);
            params.add(new BasicNameValuePair("company", company));
            params.add(new BasicNameValuePair("emp_name", "French"));
            params.add(new BasicNameValuePair("emp_no", "NewBdfvks"));
            params.add(new BasicNameValuePair("hire_date", dateStr));
            params.add(new BasicNameValuePair("job","test job"));
            params.add(new BasicNameValuePair("salary", "50000.0"));
            params.add(new BasicNameValuePair("dept_id", String.valueOf(dept_id)));
            params.add(new BasicNameValuePair("mng_id", "0"));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            response = client.execute(postRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
                emp_id = P2Tester.getId(responseContent,"emp_id");

            } else {
                emp_id = 0;
            }

            System.out.println("End InsertDepartment - GOOD\n\n\n");

//get employee
            System.out.println("Get Employee - BAD");
            System.out.println("====================");
            getRequest = new HttpGet(baseUrl+"employee?company="+company+"&emp_id=0");
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Employee - BAD\n\n\n");

            System.out.println("Get Employee - GOOD");
            System.out.println("=====================");

            getRequest = new HttpGet(baseUrl+"employee?company="+company+"&emp_id="+emp_id);
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Employee - GOOD\n\n\n");

//get employees
            System.out.println("Get Employees");
            System.out.println("===============");
            getRequest = new HttpGet(baseUrl+"employees?company="+company);
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Employees\n\n\n");

//timecard

//insert timecard
            System.out.println("Insert timecard - BAD 1");
            System.out.println("=======================");
            postRequest = new HttpPost(baseUrl+"timecard");

            dateToUse =
                    LocalDate.now( ZoneId.systemDefault() )
                            .with( TemporalAdjusters.previous( DayOfWeek.SUNDAY ) ) ;

            df = new SimpleDateFormat("yyyy-MM-dd");
            dateStr = df.format(java.sql.Date.valueOf(dateToUse))+" 08:00:00";

            params = new ArrayList<NameValuePair>(4);
            params.add(new BasicNameValuePair("company", company));
            params.add(new BasicNameValuePair("emp_id", String.valueOf(emp_id)));
            params.add(new BasicNameValuePair("start_time", dateStr));
            params.add(new BasicNameValuePair("end_time", dateStr));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            response = client.execute(postRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End InsertTimecard - BAD 1\n\n\n");

            System.out.println("Insert timecard - BAD 2");
            System.out.println("=======================");
            postRequest = new HttpPost(baseUrl+"timecard");

            dateToUse =
                    LocalDate.now( ZoneId.systemDefault() )
                            .with( TemporalAdjusters.previous( DayOfWeek.MONDAY ) ) ;

            df = new SimpleDateFormat("yyyy-MM-dd");
            dateStr = df.format(java.sql.Date.valueOf(dateToUse))+" 08:00:00";

            params = new ArrayList<NameValuePair>(4);
            params.add(new BasicNameValuePair("company", company));
            params.add(new BasicNameValuePair("emp_id", String.valueOf(emp_id)));
            params.add(new BasicNameValuePair("start_time", dateStr));
            params.add(new BasicNameValuePair("end_time", dateStr));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            response = client.execute(postRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End InsertTimecard - BAD 2\n\n\n");

            System.out.println("Insert Timecard - GOOD");
            System.out.println("=======================");
            postRequest = new HttpPost(baseUrl+"timecard");

            dateToUse =
                    LocalDate.now( ZoneId.systemDefault() )
                            .with( TemporalAdjusters.previous( DayOfWeek.MONDAY ) ) ;

            df = new SimpleDateFormat("yyyy-MM-dd");
            dateStr = df.format(java.sql.Date.valueOf(dateToUse))+" 08:00:00";
            String dateStr2 = df.format(java.sql.Date.valueOf(dateToUse))+" 17:00:00";

            params = new ArrayList<NameValuePair>(4);
            params.add(new BasicNameValuePair("company", company));
            params.add(new BasicNameValuePair("emp_id", String.valueOf(emp_id)));
            params.add(new BasicNameValuePair("start_time", dateStr));
            params.add(new BasicNameValuePair("end_time", dateStr2));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            response = client.execute(postRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
                timecard_id = P2Tester.getId(responseContent,"timecard_id");

            } else {
                timecard_id = 0;
            }

            System.out.println("End InsertTimecard - GOOD\n\n\n");


//update timecard
            System.out.println("Update timecard - BAD");
            System.out.println("=======================");

            putRequest = new HttpPut(baseUrl+"timecard");

            dateToUse =
                    LocalDate.now( ZoneId.systemDefault() )
                            .with( TemporalAdjusters.previous( DayOfWeek.MONDAY ) ) ;

            df = new SimpleDateFormat("yyyy-MM-dd");
            dateStr = df.format(java.sql.Date.valueOf(dateToUse))+" 09:00:00";
            dateStr2 = df.format(java.sql.Date.valueOf(dateToUse))+" 16:00:00";

            json = "{\"company\":\""+company+"\",\"timecard_id\":"+timecard_id+",\"start_time\":\""+dateStr+
                    "\",\"end_time\":\""+dateStr+"\",\"emp_id\":"+emp_id+"}";

            putRequest.setEntity(new StringEntity(json,
                    ContentType.APPLICATION_JSON));
            response = client.execute(putRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End UpdateTimecard - BAD\n\n\n");


            System.out.println("Update Timecard - GOOD");
            System.out.println("========================");

            putRequest = new HttpPut(baseUrl+"timecard");

            json = "{\"company\":\""+company+"\",\"timecard_id\":"+timecard_id+",\"start_time\":\""+dateStr+
                    "\",\"end_time\":\""+dateStr2+"\",\"emp_id\":"+emp_id+"}";

            putRequest.setEntity(new StringEntity(json,
                    ContentType.APPLICATION_JSON));
            response = client.execute(putRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End UpdateTimecard - GOOD\n\n\n");

//delete Timecard
            System.out.println("Delete Timecard - BAD");
            System.out.println("=======================");

            deleteRequest = new HttpDelete(baseUrl+"timecard?company="+company+"&timecard_id=0");

            response = client.execute(deleteRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End DeleteTimecard - BAD\n\n\n");


            System.out.println("Delete Timecard - GOOD");
            System.out.println("========================");

            deleteRequest = new HttpDelete(baseUrl+"timecard?company="+company+"&timecard_id="+timecard_id);

            response = client.execute(deleteRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }


            System.out.println("End DeleteTimecard - GOOD\n\n\n");


//insert timecard good again
            System.out.println("Insert Timecard - GOOD");
            System.out.println("=======================");
            postRequest = new HttpPost(baseUrl+"timecard");

            dateToUse =
                    LocalDate.now( ZoneId.systemDefault() )
                            .with( TemporalAdjusters.previous( DayOfWeek.MONDAY ) ) ;

            df = new SimpleDateFormat("yyyy-MM-dd");
            dateStr = df.format(java.sql.Date.valueOf(dateToUse))+" 08:00:00";
            dateStr2 = df.format(java.sql.Date.valueOf(dateToUse))+" 17:00:00";

            params = new ArrayList<NameValuePair>(4);
            params.add(new BasicNameValuePair("company", company));
            params.add(new BasicNameValuePair("emp_id", String.valueOf(emp_id)));
            params.add(new BasicNameValuePair("start_time", dateStr));
            params.add(new BasicNameValuePair("end_time", dateStr2));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            response = client.execute(postRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
                timecard_id = P2Tester.getId(responseContent,"timecard_id");

            } else {
                timecard_id = 0;
            }

            System.out.println("End InsertTimecard - GOOD\n\n\n");


//get timecard
            System.out.println("Get Timecard - BAD");
            System.out.println("=====================");

            getRequest = new HttpGet(baseUrl+"timecard?company="+company+"&timecard_id=0");
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Timecard - BAD\n\n\n");


            System.out.println("Get Timecard - GOOD");
            System.out.println("=====================");

            getRequest = new HttpGet(baseUrl+"timecard?company="+company+"&timecard_id="+timecard_id);
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Timecard - GOOD\n\n\n");

//get timecards
            System.out.println("Get Timecards");
            System.out.println("===============");
            getRequest = new HttpGet(baseUrl+"timecards?company="+company+"&emp_id="+emp_id);
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Timecards\n\n\n");

//delete all
            System.out.println("Delete Company - GOOD");
            System.out.println("========================");

            deleteRequest = new HttpDelete(baseUrl+"company?company="+company);

            response = client.execute(deleteRequest);
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }


            System.out.println("End DeleteCompany - GOOD\n\n\n");

//get timecards
            System.out.println("Get Timecards");
            System.out.println("===============");
            getRequest = new HttpGet(baseUrl+"timecards?company="+company+"&emp_id="+emp_id);
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Timecards\n\n\n");

//get employees
            System.out.println("Get Employees");
            System.out.println("===============");
            getRequest = new HttpGet(baseUrl+"employees?company="+company);
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Employees\n\n\n");

//get departments
            System.out.println("Get departments");
            System.out.println("===============");
            getRequest = new HttpGet(baseUrl+"departments?company="+company);
            response = client.execute(getRequest);

            //get and print the response
            System.out.println("Status Code: "+response.getStatusLine().getStatusCode());
            entity = response.getEntity();

            if (entity != null) {
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                System.out.println(responseContent);
            }

            System.out.println("End Get Departments\n\n\n");


        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

    } //main

    private static int getId(String haystack, String needle) {

        haystack = haystack.replaceAll("[^a-zA-z0-9:,]","");
        haystack = haystack.replaceAll(",",":");
        String[] test = haystack.split(":");
        return Integer.parseInt(test[Arrays.asList(test).indexOf(needle)+1]);

    }

}
