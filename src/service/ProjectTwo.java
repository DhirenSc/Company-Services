package service;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import business.BusinessLayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import companydata.DataLayer;
import companydata.Department;
import companydata.Employee;
import companydata.Timecard;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Dhiren Chandnani
 */
@Path("CompanyServices")
public class ProjectTwo{
    @Context
    UriInfo uriInfo;

    /**
     * Deletes all Department, Employee and Timecard records in the database for a given company
     * @param companyName - name of the company to delete
     * @return Response - response json string
     */
    @Path("/company")
    @DELETE
    @Produces("application/json")
    public Response deleteCompany(
            @QueryParam("company") String companyName
    ){
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject deleteCompanyJSON = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            if(companyName != null){
                // Return retrieved department
                int companyRowsDeleted = dl.deleteCompany(companyName);
                if(companyRowsDeleted > 0){
                    deleteCompanyJSON.addProperty("Success", companyName+"'s information deleted");
                }
                else{
                    deleteCompanyJSON.addProperty("Error", "Unable to delete company");
                }
            }
            else{
                deleteCompanyJSON.addProperty("error", "Company name not found");
            }
            return Response.ok(gson.toJson(deleteCompanyJSON)).build();
        } catch (Exception e) {
            deleteCompanyJSON.addProperty("error", "Error while deleting company");
            return Response.ok(gson.toJson(deleteCompanyJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    // DEPARTMENT PATHS

    /**
     * Get a single department information
     * @param companyName - name of the company to get department from
     * @param deptId - id of the department
     * @return Response - response json string
     */
    @Path("/department")
    @GET
    @Produces("application/json")
    public Response getDepartment(
            @QueryParam("company") String companyName,
            @QueryParam("dept_id") int deptId
    ){
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject getDepartmentJSON = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            // Check if department exists
            Department department = dl.getDepartment(companyName, deptId);
            if(department != null){
                // Return retrieved department
                return Response.ok(gson.toJson(department)).build();
            }
            else{
                getDepartmentJSON.addProperty("error", "No such department found");
                return Response.ok(gson.toJson(getDepartmentJSON)).build();
            }
        } catch (Exception e) {
            getDepartmentJSON.addProperty("error", "Error while retrieving a department.");
            return Response.ok(gson.toJson(getDepartmentJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Get all departments of a company
     * @param companyName - name of the company
     * @return Response - response json string
     */
    @Path("/departments")
    @GET
    @Produces("application/json")
    public Response getDepartments(
            @QueryParam("company") String companyName
    ){
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject getDepartmentsJSON = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            List<Department> departments = dl.getAllDepartment(companyName);
            // Check if department list is not empty
            if(departments.size() > 0){
                return Response.ok(gson.toJson(departments)).build();
            }
            else{
                getDepartmentsJSON.addProperty("error", "No departments found");
                return Response.ok(gson.toJson(getDepartmentsJSON)).build();
            }
        } catch (Exception e) {
            getDepartmentsJSON.addProperty("error", "Error while retrieving all departments");
            return Response.ok(gson.toJson(getDepartmentsJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Update an existing department in the company
     * @param department - JSON string of the department to be updated
     * @return Response - response json string
     */
    @Path("/department")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateDepartment(
            String department
    ){
        BusinessLayer bl = new BusinessLayer();
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject updateDepartmentJSON = new JsonObject();
        try {
            // Make department object from api call
            Department userDepartment = gson.fromJson(department, Department.class);
            if(userDepartment.getCompany() != null){
                dl = new DataLayer(userDepartment.getCompany());
                if(userDepartment.getId() >= 0){
                    // Check department exists
                    Department dbDepartment = dl.getDepartment(userDepartment.getCompany(), userDepartment.getId());
                    if(dbDepartment != null){
                        if(userDepartment.getDeptNo() != null){
                            // Get all departments
                            List<Department> departments = dl.getAllDepartment(userDepartment.getCompany());
                            // Check if department number is unique
                            if(bl.checkDepartmentNumber(userDepartment.getDeptNo(), departments)) {
                                updateDepartmentJSON.addProperty("error", "Department number is not unique");
                                return Response.ok(gson.toJson(updateDepartmentJSON)).build();
                            }
                        }
                        Department mergedDepartment = bl.combine2Objects(dbDepartment, userDepartment);
                        mergedDepartment = dl.updateDepartment(mergedDepartment);
                        if(mergedDepartment != null){
                            updateDepartmentJSON.add("Success", gson.toJsonTree(mergedDepartment));
                        }
                        else{
                            updateDepartmentJSON.addProperty("error", "Unable to update department");
                        }
                    }
                    else{
                        updateDepartmentJSON.addProperty("error", "Department id does not exist");
                    }
                }
                else{
                    updateDepartmentJSON.addProperty("error", "Department Id not specified");
                }
            }
            else{
                updateDepartmentJSON.addProperty("error", "Company name not specified");
            }
            return Response.ok(gson.toJson(updateDepartmentJSON)).build();
        }
        catch (JsonSyntaxException je){
            updateDepartmentJSON.addProperty("error", "Invalid json");
            return Response.ok(gson.toJson(updateDepartmentJSON)).build();
        }
        catch (Exception e) {
            updateDepartmentJSON.addProperty("error", "Error while updating department");
            return Response.ok(gson.toJson(updateDepartmentJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Add a new department in the company
     * @param companyName - name of the company
     * @param departmentName - name of the department to be added
     * @param departmentNo - unique department number to be added
     * @param location - location of the department
     * @return Response - response json string
     */
    @Path("/department")
    @POST
    @Produces("application/json")
    public Response insertDepartment(
            @FormParam("company") String companyName,
            @FormParam("dept_name") String departmentName,
            @FormParam("dept_no") String departmentNo,
            @FormParam("location") String location
    ){
        BusinessLayer bl = new BusinessLayer();
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject insertDepartmentJSON = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            // get all departments
            List<Department> departments = dl.getAllDepartment(companyName);
            // check if department number already exists
            if(!(bl.checkDepartmentNumber(departmentNo, departments))){
                // get new department_id and make new department object
                Department newDepartment = new Department(3, companyName, departmentName, departmentNo, location);
                newDepartment = dl.insertDepartment(newDepartment);
                if(newDepartment.getId() > 0){
                    insertDepartmentJSON.add("Success", gson.toJsonTree(newDepartment));
                }
                else{
                    insertDepartmentJSON.addProperty("error", "Not able to insert new department");
                }
            }
            else{
                insertDepartmentJSON.addProperty("error", "Department number already exists");
            }
            return Response.ok(gson.toJson(insertDepartmentJSON)).build();
        } catch (Exception e) {
            insertDepartmentJSON.addProperty("error", "Error while inserting a new department");
            return Response.ok(gson.toJson(insertDepartmentJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Delete a department from the company
     * @param companyName - name of the company
     * @param departmentId - id of department to be deleted
     * @return Response - response json string
     */
    @Path("/department")
    @DELETE
    @Produces("application/json")
    public Response deleteDepartment(
            @QueryParam("company") String companyName,
            @QueryParam("dept_id") int departmentId
    ){
        BusinessLayer bl = new BusinessLayer();
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject deleteDepartmentJson = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            // Check if department id exists
            Department department = dl.getDepartment(companyName, departmentId);
            if(department != null){
                List<Employee> employeesToDelete = bl.getEmployeesToDelete(companyName, departmentId);
                for(Employee employee: employeesToDelete){
                    deleteEmployee(companyName, employee.getId());
                }
                // Get number of rows of department deleted
                int departmentRowsDeleted = dl.deleteDepartment(companyName, departmentId);
                if(departmentRowsDeleted >= 1){
                    deleteDepartmentJson.addProperty("Success", "Department "+department.getId()+" from "+companyName+" has been deleted");
                }
                else{
                    deleteDepartmentJson.addProperty("Error", "Department exists but was not deleted");
                }
            }
            else{
                deleteDepartmentJson.addProperty("Error", "No such department exists");
            }
            return Response.ok(gson.toJson(deleteDepartmentJson)).build();
        } catch (Exception e) {
            deleteDepartmentJson.addProperty("Error", "Error while deleting department");
            return Response.ok(gson.toJson(deleteDepartmentJson)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }


    // TIMECARD PATHS

    /**
     * Get a single timecard from its id
     * @param companyName - name of the company
     * @param timecardId - id of the timecard to be fetched
     * @return Response - repsonse json string
     */
    @Path("/timecard")
    @GET
    @Produces("application/json")
    public Response getTimecard(
            @QueryParam("company") String companyName,
            @QueryParam("timecard_id") int timecardId
    ){
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject getTimecardJSON = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            Timecard timecard = dl.getTimecard(timecardId);
            if(timecard != null){
                return Response.ok(gson.toJson(timecard)).build();
            }
            else{
                getTimecardJSON.addProperty("error", "No such timecard found");
                return Response.ok(gson.toJson(getTimecardJSON)).build();
            }
        } catch (Exception e) {
            getTimecardJSON.addProperty("error", "Error while retrieving a timecard");
            return Response.ok(gson.toJson(getTimecardJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Get all timecards of an employee
     * @param companyName - name of the company
     * @param employeeId - id of the employee
     * @return Response - response json string
     */
    @Path("/timecards")
    @GET
    @Produces("application/json")
    public Response getTimecards(
            @QueryParam("company") String companyName,
            @QueryParam("emp_id") int employeeId
    ){
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        JsonObject getTimecardsJSON = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            //department method to get department
            List<Timecard> allTimecards = dl.getAllTimecard(employeeId);
            if(allTimecards.size() > 0){
                return Response.ok(gson.toJson(allTimecards)).build();
            }
            else{
                getTimecardsJSON.addProperty("error", "No timecards found");
                return Response.ok(gson.toJson(getTimecardsJSON)).build();
            }
        } catch (Exception e) {
            getTimecardsJSON.addProperty("error", "Error while retrieving all timecards");
            return Response.ok(gson.toJson(getTimecardsJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Insert a new timecard for an employee
     * @param companyName - name of the company
     * @param employeeId - id of the employee
     * @param startTime - timecard start time
     * @param endTime - timecard end time
     * @return Response - response json string
     */
    @Path("/timecard")
    @POST
    @Produces("application/json")
    public Response insertTimecard(
            @FormParam("company") String companyName,
            @FormParam("emp_id") int employeeId,
            @FormParam("start_time") String startTime,
            @FormParam("end_time") String endTime
    ){
        BusinessLayer bl = new BusinessLayer();
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        JsonObject insertTimecardJSON = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            // Check if employee exists
            if(dl.getEmployee(employeeId) != null){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setLenient(false);
                // Parse start and end time into required format
                Date startTimeDT = dateFormat.parse(startTime);
                Date endTimeDT = dateFormat.parse(endTime);
                // Convert start and end time to timestamp
                Timestamp startTimeTS = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime).getTime());
                Timestamp endTimeTS = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime).getTime());
                // Validate start and end time with given conditions
                String validateTimecardResult = bl.checkTimecardDates(startTimeDT, endTimeDT, companyName, employeeId);
                if(validateTimecardResult.equals("Validated")){
                    Timecard insertTimecard = new Timecard(3,startTimeTS, endTimeTS, employeeId);
                    insertTimecard = dl.insertTimecard(insertTimecard);
                    if(insertTimecard.getId() > 0){
                        insertTimecardJSON.add("Success", gson.toJsonTree(insertTimecard));
                    }
                    else{
                        insertTimecardJSON.addProperty("error", "Unable to insert new timecard");
                    }
                }
                else{
                    insertTimecardJSON.addProperty("error", validateTimecardResult);
                }
            }
            else{
                insertTimecardJSON.addProperty("error", "No such employee exists");
            }
            return Response.ok(gson.toJson(insertTimecardJSON)).build();
        }
        catch (ParseException pe){
            insertTimecardJSON.addProperty("error", "Start or end date is not in correct format");
            return Response.ok(gson.toJson(insertTimecardJSON)).build();
        }
        catch (Exception e) {
            insertTimecardJSON.addProperty("error", "Error while inserting a new timecard");
            return Response.ok(gson.toJson(insertTimecardJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Update a particular timecard
     * @param timecard - JSON string of the timecard to be updated
     * @return Response - response json string
     */
    @Path("/timecard")
    @PUT
    @Produces("application/json")
    @Consumes("application/json")
    public Response updateTimecard(
            String timecard
    ){
        BusinessLayer bl = new BusinessLayer();
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        JsonObject updateTimecardJSON = new JsonObject();
        try {
            JsonObject timecardJO = gson.fromJson(timecard, JsonObject.class);
            // Check if json string has company specified
            if (timecardJO.has("company")) {
                String companyName = timecardJO.get("company").getAsString();
                timecardJO.remove("company");
                // Create timecard object from json string
                Timecard userTimecard = gson.fromJson(gson.toJson(timecardJO), Timecard.class);
                dl = new DataLayer(companyName);
                if(userTimecard.getId() > 0){
                    // Check if timecard exists
                    Timecard dbTimecard = dl.getTimecard(userTimecard.getId());
                    if(dbTimecard != null){
                        // Check if employee exists
                        if(userTimecard.getEmpId() > 0){
                            if(dl.getEmployee(userTimecard.getEmpId()) == null){
                                updateTimecardJSON.addProperty("error", "No such employee exists");
                                return Response.ok(gson.toJson(updateTimecardJSON)).build();
                            }
                        }
                        Timecard checkTimecardDates = bl.combine2Objects(dbTimecard, userTimecard);
                        // Validate Timecard dates
                        if(userTimecard.getStartTime() != null || userTimecard.getEndTime() != null){
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            dateFormat.setLenient(false);
                            Date startTimeDT = dateFormat.parse(checkTimecardDates.getStartTime().toString());
                            Date endTimeDT = dateFormat.parse(checkTimecardDates.getEndTime().toString());
                            String validateTimecardResult = bl.checkTimecardDates(startTimeDT, endTimeDT, companyName, checkTimecardDates.getEmpId());
                            if(!(validateTimecardResult.equals("Validated"))){
                                updateTimecardJSON.addProperty("error", validateTimecardResult);
                                return Response.ok(gson.toJson(updateTimecardJSON)).build();
                            }
                        }
                        checkTimecardDates = dl.updateTimecard(checkTimecardDates);
                        if(checkTimecardDates.getId() > 0){
                            updateTimecardJSON.add("Success", gson.toJsonTree(checkTimecardDates));
                        }
                        else{
                            updateTimecardJSON.addProperty("error", "Unable to update timecard");
                        }
                    }
                    else{
                        updateTimecardJSON.addProperty("error", "No such timecard exists");
                    }
                }
                else{
                    updateTimecardJSON.addProperty("error", "Timecard id not specified");
                }
            }
            else{
                updateTimecardJSON.addProperty("error", "Company name not specified");
            }
            return Response.ok(gson.toJson(updateTimecardJSON)).build();
        }
        catch (ParseException pe){
            updateTimecardJSON.addProperty("error", "Start or end date is not in correct format");
            return Response.ok(gson.toJson(updateTimecardJSON)).build();
        }
        catch (Exception e) {
            updateTimecardJSON.addProperty("error", "Error while updating an existing timecard=="+e.getMessage());
            e.printStackTrace();
            return Response.ok(gson.toJson(updateTimecardJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Delete a particular timecard
     * @param companyName - name of the company
     * @param timecardId - id of the timecard to be deleted
     * @return Response - response json string
     */
    @Path("/timecard")
    @DELETE
    @Produces("application/json")
    public Response deleteTimecard(
            @QueryParam("company") String companyName,
            @QueryParam("timecard_id") int timecardId
    ){
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject deleteTCJSON = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            // Check if timecard id exists
            if(dl.getTimecard(timecardId) != null){
                int deleteTCRow = dl.deleteTimecard(timecardId);
                if(deleteTCRow >= 1){
                    deleteTCJSON.addProperty("Success", "Timecard "+timecardId+" deleted");
                }
                else{
                    deleteTCJSON.addProperty("error", "Unable to delete timecard");
                }
            }
            else{
                deleteTCJSON.addProperty("error", "No such timecard exists");
            }
            return Response.ok(gson.toJson(deleteTCJSON)).build();
        } catch (Exception e) {
            deleteTCJSON.addProperty("error", "Error while deleting a timecard");
            return Response.ok(gson.toJson(deleteTCJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    // EMPLOYEE PATHS

    /**
     * Get details of an employee
     * @param companyName - name of the company
     * @param empId - employee id of the employee to be fetched
     * @return Response - response json string
     */
    @Path("/employee")
    @GET
    @Produces("application/json")
    public Response getEmployee(
            @QueryParam("company") String companyName,
            @QueryParam("emp_id") int empId
    ){
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject getEmployeeJSON = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            //employee method to get employee
            Employee employee = dl.getEmployee(empId);
            if(employee != null){
                return Response.ok(gson.toJson(employee)).build();
            }
            else{
                getEmployeeJSON.addProperty("error", "No such employee found");
                return Response.ok(gson.toJson(getEmployeeJSON)).build();
            }
        } catch (Exception e) {
            getEmployeeJSON.addProperty("error", "Error while retrieving an employee");
            return Response.ok(gson.toJson(getEmployeeJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Get all employees of a company
     * @param companyName - name of the company
     * @return Response - response json string
     */
    @Path("/employees")
    @GET
    @Produces("application/json")
    public Response getEmployees(
            @QueryParam("company") String companyName
    ){
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setDateFormat("yyyy-MM-dd").create();
        JsonObject getEmployeesJSON = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            // Employee method to get all employees
            List<Employee> employees = dl.getAllEmployee(companyName);
            if(employees.size() > 0){
                return Response.ok(gson.toJson(employees)).build();
            }
            else{
                getEmployeesJSON.addProperty("error", "No employees found");
                return Response.ok(gson.toJson(getEmployeesJSON)).build();
            }
        } catch (Exception e) {
            getEmployeesJSON.addProperty("error", "Error while retrieving all employees");
            return Response.ok(gson.toJson(getEmployeesJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Insert a new employee in the company database
     * @param companyName - name of th company
     * @param employeeName - name of the employee
     * @param employeeNo - employee number
     * @param hireDate - employee hire date
     * @param job - employee job title
     * @param salary - salary of the employee
     * @param departmentId - id of the department associated with the employee
     * @param managerId - manager id of the employee
     * @return Response - response json string
     */
    @Path("/employee")
    @POST
    @Produces("application/json")
    public Response insertEmployee(
            @FormParam("company") String companyName,
            @FormParam("emp_name") String employeeName,
            @FormParam("emp_no") String employeeNo,
            @FormParam("hire_date") String hireDate,
            @FormParam("job") String job,
            @FormParam("salary") double salary,
            @FormParam("dept_id") int departmentId,
            @FormParam("mng_id") int managerId
    ){
        BusinessLayer bl = new BusinessLayer();
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setDateFormat("yyyy-MM-dd").create();
        JsonObject insertEmployeeJSON = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            // Check if department exists
            Department department = dl.getDepartment(companyName, departmentId);
            if(department != null){
                // Check if manager exists or if employee is first employee
                // or if employee does not have a manager
                Employee employee = dl.getEmployee(managerId);
                if(employee != null || managerId == 0){
                    Date hireDateDT = new SimpleDateFormat("yyyy-MM-dd").parse(hireDate);
                    // Check if hire date is today or before today or if its a weekday
                    if(bl.checkHireDate(hireDateDT)){
                        // Check if employee number is unique among all employees
                        if(!(bl.checkEmployeeNo(employeeNo, companyName))){
                            Employee insertEmployee = new Employee(employeeName, employeeNo, new java.sql.Date(hireDateDT.getTime()), job, salary, departmentId, managerId);
                            // Insert new employee
                            insertEmployee = dl.insertEmployee(insertEmployee);
                            if(insertEmployee.getId() > 0){
                                insertEmployeeJSON.add("Success", gson.toJsonTree(insertEmployee));
                            }
                            else{
                                insertEmployeeJSON.addProperty("Error", "New employee not inserted");
                            }
                        }
                        else{
                            insertEmployeeJSON.addProperty("Error", "Employee number already exists");
                        }
                    }
                    else{
                        insertEmployeeJSON.addProperty("Error", "Hire Date must be today or before today. Also it must be a weekday.");
                    }
                }
                else{
                    insertEmployeeJSON.addProperty("Error", "No such manager exists");
                }
            }
            else{
                insertEmployeeJSON.addProperty("Error", "No such department exists");
            }
            return Response.ok(gson.toJson(insertEmployeeJSON)).build();
        }
        catch (ParseException pe){
            insertEmployeeJSON.addProperty("Error", "Not able to parse hire date. Format should be yyyy-MM-dd");
            return Response.ok(gson.toJson(insertEmployeeJSON)).build();
        }
        catch (Exception e) {
            insertEmployeeJSON.addProperty("error", "Error while inserting a new employee");
            return Response.ok(gson.toJson(insertEmployeeJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Update an existing employee
     * @param employee - JSON string details of the employee to be updated
     * @return Response - response json string
     */
    // UPDATE AN EXISTING EMPLOYEE
    @Path("/employee")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateEmployee(
            String employee
    ){
        BusinessLayer bl = new BusinessLayer();
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setDateFormat("yyyy-MM-dd").create();
        JsonObject updateEmployeeJSON = new JsonObject();
        try {
            // Make json object from input string
            JsonObject employeeJO = gson.fromJson(employee, JsonObject.class);
            // Check if company is specified in the json object
            if (employeeJO.has("company")) {
                String companyName = employeeJO.get("company").getAsString();
                employeeJO.remove("company");
                Employee userEmployee = gson.fromJson(gson.toJson(employeeJO), Employee.class);
                dl = new DataLayer(companyName);
                // Check if id is present in the json string
                if(userEmployee.getId() > 0){
                    Employee dbEmployee = dl.getEmployee(userEmployee.getId());
                    if(dbEmployee != null)
                    {
                        // Check if department exists
                        if(userEmployee.getDeptId() > 0){
                            if(dl.getDepartment(companyName, userEmployee.getDeptId()) == null){
                                updateEmployeeJSON.addProperty("Error", "No such department exists");
                                return Response.ok(gson.toJson(updateEmployeeJSON)).build();
                            }
                        }
                        // Check if manager exists or if employee is first employee
                        // or if employee does not have a manager
                        if(userEmployee.getMngId() > 0){
                            Employee manager = dl.getEmployee(userEmployee.getMngId());
                            if(manager == null){
                                updateEmployeeJSON.addProperty("Error", "No such manager exists");
                                return Response.ok(gson.toJson(updateEmployeeJSON)).build();
                            }
                        }
                        // Check hire date to see if its valid or is a weekday
                        if(userEmployee.getHireDate() != null){
                            Date hireDateDT = new SimpleDateFormat("yyyy-MM-dd").parse(userEmployee.getHireDate().toString());
                            // Check hire date to see if its valid or is a weekday
                            if(!(bl.checkHireDate(hireDateDT))){
                                updateEmployeeJSON.addProperty("Error", "Hire Date must be today or before today. Also it must be a weekday.");
                                return Response.ok(gson.toJson(updateEmployeeJSON)).build();
                            }
                        }
                        // Check if employee no is unique among all companies
                        if(userEmployee.getEmpNo() != null){
                            if(bl.checkEmployeeNo(userEmployee.getEmpNo(), companyName)){
                                updateEmployeeJSON.addProperty("Error", "Employee number already exists");
                                return Response.ok(gson.toJson(updateEmployeeJSON)).build();
                            }
                        }
                        Employee mergedEmployee = bl.combine2Objects(dbEmployee, userEmployee);
                        mergedEmployee = dl.updateEmployee(mergedEmployee);
                        if(mergedEmployee.getId() > 0){
                            updateEmployeeJSON.add("Success", gson.toJsonTree(mergedEmployee));
                        }
                        else{
                            updateEmployeeJSON.addProperty("Error", "New employee not inserted");
                        }
                    }
                    else{
                        updateEmployeeJSON.addProperty("error", "No such employee exists");
                    }
                }
                else{
                    updateEmployeeJSON.addProperty("error", "Employee id not specified");
                }
            } else {
                updateEmployeeJSON.addProperty("error", "Company name not specified");
            }
            return Response.ok(gson.toJson(updateEmployeeJSON)).build();

        }
        catch (ParseException pe){
            updateEmployeeJSON.addProperty("Error", "Not able to parse hire date. Format should be yyyy-MM-dd");
            return Response.ok(gson.toJson(updateEmployeeJSON)).build();
        }
        catch (Exception e) {
            updateEmployeeJSON.addProperty("error", "Error while updating an existing employee");
            return Response.ok(gson.toJson(updateEmployeeJSON)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }

    /**
     * Delete an employee from the company
     * @param companyName - name of the company
     * @param employeeId - id of the employee to be deleted
     * @return Response - response json string
     */
    @Path("/employee")
    @DELETE
    @Produces("application/json")
    public Response deleteEmployee(
            @QueryParam("company") String companyName,
            @QueryParam("emp_id") int employeeId
    ){
        BusinessLayer bl = new BusinessLayer();
        DataLayer dl = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        JsonObject deleteEmployeeJson = new JsonObject();
        try {
            dl = new DataLayer(companyName);
            // Check if employee id exists
            Employee deleteEmployee = dl.getEmployee(employeeId);
            if(deleteEmployee != null){
                // If employee is manager, assign employees he manages to another manager
                if(deleteEmployee.getJob().equals("manager")){
                    // Check if there are other employees to be reassigned
                    List<Employee> employeesToReassign = bl.getEmployeesToReassign(companyName, employeeId);
                    if(employeesToReassign.size() > 0) {
                        for (Employee employee : employeesToReassign) {
                            employee.setMngId(1);
                            dl.updateEmployee(employee);
                        }
                    }
                }
                int employeeRowDeleted = dl.deleteEmployee(employeeId);
                if(employeeRowDeleted >= 1){
                    deleteEmployeeJson.addProperty("Success", "Employee "+employeeId+" has been deleted");
                }
                else{
                    deleteEmployeeJson.addProperty("Error", "Unable to delete Employee "+employeeId);
                }
            }
            else{
                deleteEmployeeJson.addProperty("Error", "No such employee exists");
            }
            return Response.ok(gson.toJson(deleteEmployeeJson)).build();
        } catch (Exception e) {
            deleteEmployeeJson.addProperty("Error", "Error while deleting employee");
            return Response.ok(gson.toJson(deleteEmployeeJson)).build();
        } finally {
            if (dl != null) {
                dl.close();
            }
        }
    }
}