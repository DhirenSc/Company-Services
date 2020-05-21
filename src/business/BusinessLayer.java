package business;

import companydata.DataLayer;
import companydata.Department;
import companydata.Employee;
import companydata.Timecard;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BusinessLayer {

    /**
     * Check if department number is unique among all departments in the company
     * @param deptNo - department number to check
     * @param allDepartments - list of all departments in the company
     * @return boolean - true if department number is not unique
     */
    public boolean checkDepartmentNumber(String deptNo, List<Department> allDepartments){
        return allDepartments.stream()
                .anyMatch(o -> o.getDeptNo().equals(deptNo));
    }

    /**
     * The below method will ignore the serialVersionUID, iterate through all the fields
     * and copy the non-null values from object a --> object b if they are null in b.
     * In other words, if any field is null in b, take it from a if there its not null.
     * @param a - Object with non null properties
     * @param b - Object with null/non-null properties
     * @param <T> - Object type
     * @return - Object - return the combined object
     * @throws InstantiationException - Thrown when an application tries to create an instance of a class using the newInstance method in class Class
     * @throws IllegalAccessException - An IllegalAccessException is thrown when an application tries to reflectively create an instance (other than an array), set or get a field, or invoke a method
     * @throws NoSuchMethodException - Thrown when a particular method cannot be found
     * @throws InvocationTargetException - InvocationTargetException is a checked exception that wraps an exception thrown by an invoked method or constructor.
     */
    public <T> T combine2Objects(T a, T b) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        T result = (T) a.getClass().getDeclaredConstructor().newInstance();
        Object[] fields = Arrays.stream(a.getClass().getDeclaredFields()).filter(f -> !f.getName().equals("serialVersionUID")).toArray();
        for (Object fieldobj : fields) {
            Field field = (Field) fieldobj;
            field.setAccessible(true);
            field.set(result, field.get(b) != null ? field.get(b) : field.get(a));
        }
        return result;
    }

    /**
     * Check if hire date of the employee is today or before today.
     * Also checks if hire date is not a weekday
     * @param hireDate - hire date to be checked
     * @return boolean - True if hire date is a valid date and is not a weekday
     */
    public boolean checkHireDate(Date hireDate){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        String today = dateFormatter.format(currentDate);
        try {
            currentDate = dateFormatter.parse(today);
        } catch(ParseException pe){
            return false;
        }
        if(hireDate.equals(currentDate) || hireDate.before(currentDate)){
            return checkWeekday(hireDate);
        }
        else{
            return false;
        }
    }

    /**
     * Check if a date is a weekday or not
     * @param myDate - Date to be checked
     * @return boolean - True if date is not a weekday
     */
    public boolean checkWeekday(Date myDate){
        SimpleDateFormat dayFormatter = new SimpleDateFormat("E");
        String currentDay = dayFormatter.format(myDate);
        return !(currentDay.equals("Sat") || currentDay.equals("Sun"));
    }

    /**
     * Check start date and end date in timecards with following conditions:
     * 1. start_time must be a valid date and time equal to the current date or back to the Monday prior to the current date if the current date is not a Monday
     * 2. end_time must be a valid date and time at least 1 hour greater than the start_time and be on the same day as the start_time.
     * 3. start_time and end_time must be a weekday
     * 4. start_time and end_time must be between the hours (in 24 hour format) of 08:00:00 and 18:00:00 inclusive
     * 5. start_time must not be on the same day as any other start_time for that employee
     * @param startTime
     * @param endTime
     * @param companyName
     * @param employeeId
     * @return String - Response for each type of validation
     */
    public String checkTimecardDates(Date startTime, Date endTime, String companyName, int employeeId){
        if(checkWeekday(startTime) && checkWeekday(endTime)){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            // Check if both dates are on same day
            if(dateFormat.format(startTime).equals(dateFormat.format(endTime))){
                // Get time difference in milliseconds
                long diff = endTime.getTime() - startTime.getTime();
                long diffHours = diff / (60 * 60 * 1000) % 24;
                // Check if difference is at least an hour
                if(diffHours >= 1){
                    // Check if start time lies between current date and previous monday
                    if(checkCurrentDateWithinMonday(startTime)){
                        // Check if start and end times are between 8 and 5
                        if(checkTimeRange(startTime, endTime)){
                            // Check if start time already exists among other timecards
                            if(!(checkStartTimeInTimecards(startTime, companyName, employeeId))){
                                return "Validated";
                            }
                            else{
                                return "Timecard for start day already exist";
                            }
                        }
                        else{
                            return "Start time or End time is not between 08:00:00 and 18:00:00";
                        }
                    }
                    else{
                        return "Start time is not between today and previous monday";
                    }
                }
                else{
                    return "Difference between Start time and End time should be at least 1 hour";
                }
            }
            else{
                return "Start time and End time are not on the same day";
            }
        }
        else{
            return "One of the times is not a weekday";
        }
    }

    /**
     * Check if start_time is not on the same day as any other start_time for that employee
     * @param startTime - start time to be checked
     * @param companyName - name of the company
     * @param employeeId - id of the employee
     * @return boolean - True if start_time is on the same day as any other start time for that employee
     */
    public boolean checkStartTimeInTimecards(Date startTime, String companyName, int employeeId){
        DataLayer dl;
        try{
            dl = new DataLayer(companyName);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            List<Timecard> allTimecards = dl.getAllTimecard(employeeId);
            return allTimecards.stream()
                    .anyMatch(o -> dateFormat.format(o.getStartTime()).equals(dateFormat.format(startTime)));
        }
        catch (Exception e){
            return false;
        }
    }

    /**
     * Check if start time is between today and previous monday
     * @param startTime - start time to be checked
     * @return boolean - True if the condition is satisfied
     */
    public boolean checkCurrentDateWithinMonday(Date startTime){
        LocalDate previousLocalMonday = LocalDate.now(ZoneId.of("America/New_York")).with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        Date previousMondayDate = java.sql.Date.valueOf(previousLocalMonday);
        Date currentDate = new Date();
        return currentDate.compareTo(startTime) * startTime.compareTo(previousMondayDate) > 0;
    }

    /**
     * Check if both start time and end time are within 8:00AM to 5:00PM inclusive
     * @param startTime - start time to check
     * @param endTime - end time to check
     * @return boolean - True if condition is satisfied
     */
    public boolean checkTimeRange(Date startTime, Date endTime){
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        // Date conversions to check time between 8 to 5
        Date initCheckTime;
        Date endCheckTime;
        try {
            initCheckTime = timeFormat.parse("08:00:00");
            endCheckTime = timeFormat.parse("18:00:00");
            // new start and end times in HH:mm:ss format
            startTime = timeFormat.parse(timeFormat.format(startTime));
            endTime = timeFormat.parse(timeFormat.format(endTime));
        } catch (ParseException e) {
            return false;
        }
        return startTime.getTime() <= endCheckTime.getTime() && startTime.getTime() >= initCheckTime.getTime() && endTime.getTime() <= endCheckTime.getTime() && endTime.getTime() >= initCheckTime.getTime();
    }

    /**
     * Check if employee number is unique within all employees of the company
     * @param employeeNo - employee number to check
     * @param companyName - name of the company
     * @return boolean - True if employee number is not unique
     */
    public boolean checkEmployeeNo(String employeeNo, String companyName){
        DataLayer dl;
        try{
            dl = new DataLayer(companyName);
            List<Employee> allEmployees = dl.getAllEmployee(companyName);
            return allEmployees.stream()
                    .anyMatch(o -> o.getEmpNo().equals(employeeNo));
        } catch(Exception e){
            return false;
        }
    }

    /**
     * Check if an employee(manager) has any employees under him/her
     * @param companyName - name of the company
     * @param employeeId - id of the employee(manager)
     * @return List<Employee> - List of employees managed by the employee
     */
    public List<Employee> getEmployeesToReassign(String companyName, int employeeId){
        DataLayer dl;
        try{
            dl = new DataLayer(companyName);
            List<Employee> allEmployees = dl.getAllEmployee(companyName);
            return allEmployees.stream()
                    .filter(employee -> employee.getMngId() == employeeId)
                    .collect(Collectors.toList());
        } catch(Exception e){
            return null;
        }
    }

    /**
     * Check if a department contains employees associated with it
     * @param companyName - name of the company
     * @param departmentId - id of the department to be checked
     * @return List<Employee> - List of employees in that department
     */
    public List<Employee> getEmployeesToDelete(String companyName, int departmentId){
        DataLayer dl;
        try{
            dl = new DataLayer(companyName);
            List<Employee> allEmployees = dl.getAllEmployee(companyName);
            return allEmployees.stream().filter(employee -> employee.getDeptId() == departmentId).collect(Collectors.toList());
        } catch(Exception e){
            return null;
        }
    }
}