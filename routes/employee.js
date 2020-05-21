let express = require('express');
let router = express.Router();
let moment = require('moment');

let DataLayer = require("../companydata/index.js");
let business = require("../services/businessLogic.js");

router.get("/", function(request, response){
    try{
        let companyName = request.query.company;
        let dl = new DataLayer(companyName);
        if(request.baseUrl === "/employees"){
            let allEmployees = dl.getAllEmployee(companyName);
            if(allEmployees.length > 0){
                response.status(200).json(allEmployees);
            }
            else{
                response.status(400).json({
                    message: "No Employees found"
                });
            }
        }
        else if(request.baseUrl === "/employee"){
            let employeeId = request.query.emp_id;
            let employee = dl.getEmployee(employeeId);
            if(employee != null){
                response.status(200).json(employee);
            }
            else{
                response.status(400).json({
                    message: "No such employee found"
                });
            }
        }
    } catch(error){
        response.status(400).json({
            message: "Error while retrieving employee(s)"
        });
    }
});

router.post("/", function(request, response){
    if(request.baseUrl === "/employee"){
        try{
            let companyName = request.body.company;
            let dl = new DataLayer(companyName);
            let department = dl.getDepartment(companyName, request.body.dept_id);
            if(department != null){
                let employee = dl.getEmployee(request.body.mng_id);
                if(employee != null || parseInt(request.body.mng_id) == 0){
                    let hireDate = moment(request.body.hire_date, "YYYY-MM-DD")
                    if(moment().diff(hireDate) >= 0 && business.checkWeekday(request.body.hireDate)){
                        let allEmployees = dl.getAllEmployee(companyName);
                        if(!business.checkEmployeeNumber(request.body.emp_no, allEmployees)){
                            let insertEmployee = new dl.Employee(request.body.emp_name, request.body.emp_no, request.body.hire_date, request.body.job, request.body.salary, request.body.dept_id, request.body.mng_id);
                            insertEmployee = dl.insertEmployee(insertEmployee);
                            if(parseInt(insertEmployee.emp_id) > 0){
                                response.status(200).json({
                                    success: insertEmployee
                                });
                            }
                            else{
                                response.status(400).json({
                                    error: "New Employee not inserted"
                                });
                            }
                        }
                        else{
                            response.status(400).json({
                                error: "Employee number already exists"
                            });
                        }
                    }
                    else{
                        response.status(400).json({
                            error: "Hire Date must be today or before today. Also it must be a weekday."
                        });
                    }
                }
                else{
                    response.status(400).json({
                        error: "No such manager exists"
                    });
                }
            }
            else{
                response.status(400).json({
                    error: "No such department exists"
                });
            }
        } catch(error){
            response.status(400).json({
                error: error.toString()
            });
        }
    }
    else{
        response.status(400).json({
            error: "Bad URL"
        });
    }
});

router.put("/", function(request, response){
    if(request.baseUrl === "/employee"){
        if(request.body.company != undefined){
            try{
                let dl = new DataLayer(request.body.company);
                let companyName = request.body.company;
                if(parseInt(request.body.emp_id) > 0){
                    let dbEmployee = dl.getEmployee(request.body.emp_id);
                    if(dbEmployee != null){
                        if(parseInt(request.body.dept_id) > 0){
                            if(dl.getDepartment(companyName, request.body.dept_id) == null){
                                response.status(400).json({
                                    error: "No such department exists"
                                });
                                return;
                            }
                        }
                        if(parseInt(request.body.mng_id) > 0){
                            let managerEmployee = dl.getEmployee(request.body.mng_id);
                            if(managerEmployee == null){
                                response.status(400).json({
                                    error: "No such manager exists"
                                });
                                return;
                            }
                        }
                        if(request.body.hire_date != undefined){
                            let hireDate = moment(request.body.hire_date, "YYYY-MM-DD")
                            if(!(moment().diff(hireDate) >= 0 && business.checkWeekday(request.body.hireDate))){
                                response.status(400).json({
                                    error: "Hire Date must be today or before today. Also it must be a weekday."
                                });
                                return;
                            }
                        }
                        if(request.body.emp_no != undefined){
                            let allEmployees = dl.getAllEmployee(companyName);
                            if(business.checkEmployeeNumber(request.body.emp_no, allEmployees)){
                                response.status(400).json({
                                    error: "Employee number already exists"
                                });
                                return;
                            }
                        }
                        let mergedEmployee = {...dbEmployee, ...request.body};
                        delete mergedEmployee.company;
                        mergedEmployee = dl.updateEmployee(mergedEmployee);
                        if(parseInt(mergedEmployee.emp_id) > 0){
                            response.status(200).json({
                                success: mergedEmployee
                            });
                        }
                        else{
                            response.status(400).json({
                                error: "Unable to update employee"
                            });
                        }
                    }
                    else{
                        response.status(400).json({
                            error: "No such employee exists"
                        });
                    }
                }
                else{
                    response.status(400).json({
                        error: "Employee id not specified"
                    });
                }
            } catch(error){
                response.status(400).json({
                    error: "Error while updating employee"
                });
            }
        }
        else{
            response.status(400).json({
                error: "Company name not specified"
            });
        }
    }
    else{
        response.status(400).json({
            error: "Bad URL"
        });
    }
});

router.delete("/", function(request, response){
    if(request.baseUrl === "/employee"){
        if(request.query.company != undefined){
            try{
                let dl = new DataLayer(request.query.company);
                let companyName = request.query.company;
                let deleteEmployee = dl.getEmployee(request.query.emp_id);
                if(deleteEmployee != null){
                    if(business.deleteEmployee(companyName, request.query.emp_id)){
                        response.status(200).json({
                            success: "Employee "+request.query.emp_id+" has been deleted"
                        });
                    }
                    else{
                        response.status(400).json({
                            error: "Unable to delete employee"
                        });
                    }
                }
                else{
                    response.status(400).json({
                        error: "No such employee exists"
                    });
                }
            } catch(error){
                response.status(400).json({
                    error: "Error while deleting employee"
                });
            }
        }
        else{
            response.status(400).json({
                error: "Company name not specified"
            });
        }
    }
    else{
        response.status(400).json({
            error: "Bad URL"
        });
    }
});

module.exports = router;