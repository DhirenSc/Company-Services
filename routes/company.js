let express = require('express');
let router = express.Router();

let DataLayer = require("../companydata/index.js");

router.delete("/", function(request, response){
    if(request.query.company != undefined){
        try{
            let dl = new DataLayer(request.query.company);
            let companyName = request.query.company;
            let companyRowsDeleted = dl.deleteCompany(companyName);
            if(parseInt(companyRowsDeleted) > 0){
                response.status(400).json({
                    success: companyName+"'s information has been deleted"
                });
            }
            else{
                response.status(400).json({
                    error: "Unable to delete company"
                });
            }
        } catch(error){
            response.status(400).json({
                error: "Error while deleting company"
            });
        }
    }
    else{
        response.status(400).json({
            error: "Company name not specified"
        });
    }
});

module.exports = router;