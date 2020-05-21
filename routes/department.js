let express = require('express');
let router = express.Router();

let DataLayer = require("../companydata/index.js");
let business = require("../services/businessLogic.js");

router.get("/", function(request, response){
    try{
        let companyName = request.query.company;
        let dl = new DataLayer(companyName);
        if(request.baseUrl === "/departments"){
            let allDepartments = dl.getAllDepartment(companyName);
            if(allDepartments.length > 0){
                response.status(200).json(allDepartments);
            }
            else{
                response.status(400).json({
                    message: "No department found"
                });
            }
        }
        else if(request.baseUrl === "/department"){
            let departmentId = request.query.dept_id;
            let department = dl.getDepartment(companyName, departmentId);
            if(department != null){
                response.status(200).json(department);
            }
            else{
                response.status(400).json({
                    message: "No such department found"
                })
            }
        }
    } catch(error){
        response.status(400).json({
            message: "Error while retrieving department(s)"
        });
    }
});

router.post("/", function(request, response){
    if(request.baseUrl === "/department"){
        try{
            let companyName = request.body.company;
            let departmentNo = request.body.dept_no;
            let dl = new DataLayer(companyName);
            let allDepartments = dl.getAllDepartment(companyName, departmentNo);
            if(!business.checkDepartmentNumber(departmentNo, allDepartments)){
                let newDepartment = new dl.Department(companyName, request.body.dept_name, departmentNo, request.body.location);
                newDepartment = dl.insertDepartment(newDepartment);
                newDepartment.dept_id = parseInt(newDepartment.dept_id);
                if(newDepartment.dept_id > 0){
                    response.status(200).json({
                        success: newDepartment
                    });
                }
                else{
                    response.status(400).json({
                        error: "Not able to insert new department"
                    });
                }
            }
            else{
                response.status(400).json({
                    error: "Department number already exists"
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
    if(request.baseUrl === "/department"){
        if(request.body.company != undefined){
            try{
                let dl = new DataLayer(request.body.company);
                if(request.body.dept_id > 0){
                    let dbDepartment = dl.getDepartment(request.body.company, request.body.dept_id);
                    if(dbDepartment != null){
                        if(request.body.dept_no != undefined){
                            let allDepartments = dl.getAllDepartment(request.body.company);
                            if(business.checkDepartmentNumber(request.body.dept_no, allDepartments)){
                                response.status(400).json({
                                    error: "Department number is not unique"
                                });
                                return;
                            }
                        }
                        let mergedDepartment = {...dbDepartment, ...request.body};
                        mergedDepartment = dl.updateDepartment(mergedDepartment);
                        mergedDepartment.dept_id = parseInt(mergedDepartment.dept_id);
                        if(mergedDepartment != null){
                            response.status(200).json({
                                success: mergedDepartment
                            });
                        }
                        else{
                            response.status(400).json({
                                error: "Unable to update department"
                            });
                        }
                    }
                    else{
                        response.status(400).json({
                            error: "Department id does not exist"
                        });
                    }
                }
                else{
                    response.status(400).json({
                        error: "Department id not specified"
                    });
                }
            } catch(error){
                response.status(400).json({
                    error: "Error while updating department"
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
    if(request.baseUrl === "/department"){
        if(request.query.company != undefined){
            try{
                let dl = new DataLayer(request.query.company);
                if(request.query.dept_id){
                    let companyName = request.query.company;
                    let departmentId = parseInt(request.query.dept_id);
                    let department = dl.getDepartment(companyName, departmentId);
                    if(department != null){
                        let employeesToDelete = business.getEmployeesToDeleteByDepartment(companyName, departmentId);
                        employeesToDelete.forEach(employee => {
                            if(business.deleteEmployee(companyName, parseInt(employee.emp_id))){
                                response.status(400).json({
                                    error: "Unable to delete employees of that department"
                                });
                                return;
                            }
                        });
                        let departmentRowsDeleted = dl.deleteDepartment(companyName, departmentId);
                        if(parseInt(departmentRowsDeleted) >= 1){
                            response.status(200).json({
                                success: "Department "+departmentId+" from "+companyName+" has been deleted"
                            });
                        }
                        else{
                            response.status(400).json({
                                error: "Department exists but was not deleted"
                            });
                        }
                    }
                    else{
                        response.status(400).json({
                            error: "No such department exists"
                        });
                    }
                }
                else{
                    response.status(400).json({
                        error: "Department id not specified"
                    });
                }
            } catch(error){
                response.status(400).json({
                    error: "Error while deleting department"
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