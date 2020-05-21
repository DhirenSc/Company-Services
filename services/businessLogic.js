let DataLayer = require("../companydata/index.js");
let moment = require("moment");
module.exports = {
    checkDepartmentNumber: function(departmentNo, allDepartments){
        if(allDepartments.some(department => department.dept_no === departmentNo)){
            return true;
        }
        else{
            return false;
        }
    },
    getEmployeesToDeleteByDepartment: function(companyName, departmentId){
        try {
            let dl = new DataLayer(companyName);
            let allEmployees = dl.getAllEmployee(companyName);
            // Get all employees to delete
            return allEmployees.filter(employee => parseInt(employee.dept_id) == parseInt(departmentId));
        } catch (error) {
            return null
        }
    },
    getEmployeesToReassign: function(companyName, employeeId){
        try{
            let dl = new DataLayer(companyName);
            let allEmployees = dl.getAllEmployee(companyName);
            return allEmployees.filter(employee => parseInt(employee.mng_id) == employeeId);
        } catch(error){
            return null;
        }
    },
    deleteEmployee: function(companyName, employeeId){
        try {
            let dl = new DataLayer(companyName);
            let employee = dl.getEmployee(employeeId);
            let employeeDeleteRow = 1;
            let employeeJob = employee["job"];
            if(employeeJob.includes("manager")){
                // Check if there are other employees to be reassigned
                let employeesToReassign = this.getEmployeesToReassign(companyName, employeeId);
                if(employeesToReassign.length > 0) {
                    employeesToReassign.forEach(employee => {
                        employee.mng_id = 1;
                        //dl.updateEmployee(employee);
                    });
                }
            }
            //employeeDeleteRow = dl.deleteEmployee(employeeId);
            if(employeeDeleteRow >= 1){
                return true;
            }
            else{
                return false;
            }
        } catch (error) {
            return false
        }
    },
    checkWeekday: function(date){
        let dayOfWeek = moment(date).day();
        if(dayOfWeek == 6 || dayOfWeek == 0){
            return false;
        }
        else{
            return true;
        }
    },
    checkStartTimeAlreadyExists: function(startTime, companyName, employeeId){
        try {
            let dl = new DataLayer(companyName);
            let allTimecards = dl.getAllTimecard(employeeId);
            if(allTimecards.some(timecard => moment(timecard.start_time).isSame(startTime, 'day'))){
                return true;
            }
            else{
                return false;
            }
        } catch (error) {
            return false;
        }
    },
    checkTimecardDates: function(startTime, endTime, companyName, employeeId){
        if(this.checkWeekday(startTime) && this.checkWeekday(endTime)){
            if(moment(startTime).isSame(endTime, 'day')){
                if(moment.duration(moment(endTime).diff(moment(startTime))).as('hours') >= 1){
                    let lastMonday = moment().startOf('isoweek');
                    let today = moment();
                    if(moment(startTime).isBetween(lastMonday, today, 'days', '[]')){
                        let startHourPart = moment(startTime, "YYYY-MM-DD HH:mm:ss").hour();
                        let endHourPart = moment(endTime, "YYYY-MM-DD HH:mm:ss").hour();
                        if(startHourPart >= 8 && startHourPart <= 18 && endHourPart >= 8 && endHourPart <= 18){
                            if(!this.checkStartTimeAlreadyExists(startTime, companyName, employeeId)){
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
    },
    checkEmployeeNumber: function(employeeNo, allEmployees){
        if(allEmployees.some(employee => employee.emp_no === employeeNo)){
            return true;
        }
        else{
            return false;
        }
    }
};