var express = require('express');
var app = express();

var department = require("./routes/department.js");
var timecard = require("./routes/timecard.js");
var employee = require("./routes/employee.js");
var company = require("./routes/company.js");
app.use(express.urlencoded({ extended: false }));
app.use(express.json());
app.use('/department(s)?', department);
app.use('/timecard(s)?', timecard);
app.use('/employee(s)?', employee);
app.use('/company', company);


var server = app.listen(8081, function(){
    var host = server.address().address;
    var port = server.address().port;
    console.log("Server listening at http://%s:%s", host, port);
});