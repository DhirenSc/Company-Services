import service.*;

public class MyTester {
    public static void main(String args[]){
        ProjectTwo p2 = new ProjectTwo();
        //System.out.println(p2.helloName("DC").getEntity());
        //System.out.println(p2.getDepartment("dc6288", 3).getEntity());
        //System.out.println(p2.getDepartments("dc6288").getEntity());
        //System.out.println(p2.getEmployees("dc6288").getEntity());
        System.out.println(p2.getEmployee("dc6288",13).getEntity());
        //System.out.println(p2.getTimecards("dc6288", 3).getEntity());
        //System.out.println(p2.insertTimecard("dc6288", 3, "2020-04-14 12:30:00", "2020-04-14 13:30:00").getEntity());
        //System.out.println(p2.updateTimecard("{\"company\": \"dc6288\",\"timecard_id\": 1,\"start_time\": \"2020-04-14 11:30:00\",\"end_time\": \"2020-04-14 12:45:00\"}").getEntity());
        //System.out.println(p2.insertDepartment("dc6288", "MyDep", "d300", "washington").getEntity());
        //System.out.println(p2.updateDepartment("{\"company\":\"dc6288\",\"dept_id\":2,\"location\":\"dallas\"}").getEntity());
        //System.out.println(p2.insertEmployee("dc6288", "Tammy", "e15", "2020-04-10", "Analyst", 4000, 4, 3).getEntity());
        System.out.println(p2.updateEmployee("{ \"company\":\"dc6288\",\"emp_id\":13,\"emp_name\":\"french\",\"hire_date\":\"2020-04-13\",\"dept_id\":4,\"mng_id\":13}").getEntity());
        //System.out.println(p2.deleteEmployee("dc6288", 2).getEntity());
    }
}
