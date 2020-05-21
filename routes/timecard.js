let express = require('express');
let router = express.Router();
let moment = require('moment');

let DataLayer = require("../companydata/index.js");
let business = require("../services/businessLogic.js");

router.get("/", function(request, response){
    try{
        let companyName = request.query.company;
        let dl = new DataLayer(companyName);
        if(request.baseUrl === "/timecards"){
            let employeeId = request.query.emp_id;
            let allTimecards = dl.getAllTimecard(employeeId);
            if(allTimecards.length > 0){
                response.status(200).json(allTimecards);
            }
            else{
                response.status(400).json({
                    message: "No timecards found"
                });
            }
        }
        else if(request.baseUrl === "/timecard"){
            let timecardId = request.query.timecard_id;
            let timecard = dl.getTimecard(timecardId);
            if(timecard != null){
                response.status(200).json(timecard);
            }
            else{
                response.status(400).json({
                    message: "No such timecard found"
                });
            }
        }
    } catch(error){
        response.status(400).json({
            message: "Error while retrieving timecard(s)"
        });
    }
});

router.post("/", function(request, response){
    if(request.baseUrl === "/timecard"){
        try{
            let companyName = request.body.company;
            let employeeId = request.body.emp_id;
            let startTime = request.body.start_time;
            let endTime = request.body.end_time;
            let dl = new DataLayer(companyName);
            let employee = dl.getEmployee(employeeId);
            if(employee != null){
                if(moment(startTime, "YYYY-MM-DD HH:mm:ss", true).isValid() && moment(endTime, "YYYY-MM-DD HH:mm:ss", true).isValid()){
                    let validateTimecardResult = business.checkTimecardDates(startTime, endTime, companyName, employeeId);
                    if(validateTimecardResult == "Validated"){
                        let insertTimecard = new dl.Timecard(startTime, endTime, employeeId);
                        insertTimecard = dl.insertTimecard(insertTimecard);
                        if(parseInt(insertTimecard.timecard_id) > 0){
                            response.status(200).json({
                                success: insertTimecard
                            });
                        }
                        else{
                            response.status(400).json({
                                error: "Unable to insert new timecard"
                            });
                        }
                    }
                    else{
                        response.status(400).json({
                            error: validateTimecardResult
                        });
                    }
                }
                else{
                    response.status(400).json({
                        error: "Start or end date is not in correct format"
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
    if(request.baseUrl === "/timecard"){
        if(request.body.company != undefined){
            try{
                let dl = new DataLayer(request.body.company);
                if(request.body.timecard_id != undefined){
                    let dbTimecard = dl.getTimecard(request.body.timecard_id);
                    if(dbTimecard != null){
                        if(request.body.emp_id != undefined){
                            if(dl.getEmployee(request.body.emp_id) == null){
                                response.status(400).json({
                                    error: "No such employee exists"
                                });
                                return;
                            }
                        }
                        let mergedTimecard = {...dbTimecard, ...request.body};
                        if(mergedTimecard.startTime != undefined && mergedTimecard.endTime != undefined){
                            if(moment(mergedTimecard.startTime, "YYYY-MM-DD HH:mm:ss", true).isValid() && moment(mergedTimecard.endTime, "YYYY-MM-DD HH:mm:ss", true).isValid()){
                                let validateTimecardResult = business.checkTimecardDates(mergedTimecard.startTime, mergedTimecard.endTime, request.body.company, mergedTimecard.emp_id);
                                if(validateTimecardResult != "Validated"){
                                    response.status(400).json({
                                        error: validateTimecardResult
                                    });
                                    return;
                                }
                            }
                            else{
                                response.status(400).json({
                                    error: "Start or end date is not in correct format"
                                });
                                return;
                            }
                        }
                        delete mergedTimecard.company;
                        mergedTimecard = dl.updateTimecard(mergedTimecard);
                        if(parseInt(mergedTimecard.timecard_id) > 0){
                            response.status(200).json({
                                success: mergedTimecard
                            });
                        }
                    }
                    else{
                        response.status(400).json({
                            error: "No such timecard exists"
                        });
                    }
                }
                else{
                    response.status(400).json({
                        error: "Timecard id not specified"
                    });
                }
            } catch(error){
                response.status(400).json({
                    error: "Error while updating timecard"
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
    if(request.baseUrl === "/timecard"){
        if(request.query.company != undefined){
            try{
                let dl = new DataLayer(request.query.company);
                if(request.query.timecard_id != undefined){
                    if(dl.getTimecard(request.query.timecard_id) != null){
                        let deleteTimecardRow = 1;
                        deleteTimecardRow = dl.deleteTimecard(request.query.timecard_id);
                        if(deleteTimecardRow >= 1){
                            response.status(200).json({
                                success: "Timecard "+request.query.timecard_id+" deleted"
                            });
                        }
                        else{
                            response.status(400).json({
                                error: "Unable to delete timecard"
                            });
                        }
                    }
                    else{
                        response.status(400).json({
                            error: "No such timecard exists"
                        });
                    }
                }
                else{
                    response.status(400).json({
                        error: "Timecard id not specified"
                    });
                }
            } catch(error){
                response.status(400).json({
                    error: "Error while deleting timecard"
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