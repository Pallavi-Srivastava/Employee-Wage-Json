package EmployeePayRollJSONGit;

import static org.junit.Assert.assertEquals;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.google.gson.Gson;
import EmployeePayRollJSONGit.EmployeePayrollService.IOService;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class EmployeePayRollTest {

	private static EmployeePayrollService employeePayrollService;

	@Before
	public void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	@BeforeClass
	public static void createcensusAnalyser() {
		employeePayrollService = new EmployeePayrollService();
		System.out.println("Welcome to the Employee Payroll Program.. ");
	}

	public EmployeePayrollData[] getEmployeeList() {
		Response response = RestAssured.get("/employee");
		System.out.println("Employee PayRoll enteries in jsonServe: " + response.asString());
		EmployeePayrollData[] arrayOfEmps = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		return arrayOfEmps;
	}

	public Response addContactToJsonServer(EmployeePayrollData newContact) {
		String gsonString = new Gson().toJson(newContact);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(gsonString);
		return request.post("/employee");
	}

	@Test
	public void addedNewEmployee_ShouldMatch201ResponseAndTotalCount() throws EmployeePayrollException {
		EmployeePayrollData gsonContacts[] = getEmployeeList();
		employeePayrollService.addNewEmployeeUsingREST(Arrays.asList(gsonContacts));
		EmployeePayrollData newContact = new EmployeePayrollData(4, "Yashi", "F", 50000.00,
				LocalDate.parse("2020-10-02"));
		Response response = addContactToJsonServer(newContact);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(201, statusCode);
		newContact = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
		employeePayrollService.addNewEmployeeUsingREST(Arrays.asList(newContact));
		long count = employeePayrollService.countEnteries(IOService.REST_IO);
		Assert.assertEquals(4, count);
	}

	@Test
	public void givenListOfEmployee_WhenAddedMultipleEmployee_ShouldMatch201ResponseAndCount()
			throws SQLException, EmployeePayrollException {
		EmployeePayrollData[] arrayOfEmps = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		EmployeePayrollData[] arrayOfEmpPayRolls = { new EmployeePayrollData(5, "Ria", "F", 80000.00, LocalDate.now()),
				new EmployeePayrollData(6, "Niku", "M", 45000.00, LocalDate.parse("2018-10-02")) };
		for (EmployeePayrollData employeePayRollData : arrayOfEmpPayRolls) {
			Response response = addContactToJsonServer(employeePayRollData);
			int statusCode = response.getStatusCode();
			assertEquals(201, statusCode);
			employeePayRollData = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
			employeePayrollService.addNewEmployeeUsingREST(Arrays.asList(employeePayRollData));
			System.out.println(employeePayRollData);
		}
		long entries = employeePayrollService.countEnteries(IOService.REST_IO);
		assertEquals(4, entries);
	}

	@Test
	public void givenNewSalaryForEmployee_WhenUpdated_ShouldMatch200Responses() throws EmployeePayrollException {
		EmployeePayrollData[] arrayOfEmps = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		employeePayrollService.updateRecord("pallavi", 6700000.00);
		EmployeePayrollData employeePayrollData = employeePayrollService.getEmployeePayrollData("pallavi");
		String employeeJson = new Gson().toJson(employeePayrollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(employeeJson);
		Response response = request.put("/employee/" + employeePayrollData.id);
		int statusCode = response.getStatusCode();
		assertEquals(200, statusCode);
	}

	@Test
	public void givenContactsInJsonServer_WhenRetrieved_ShouldMatchTotalCount() {
		EmployeePayrollData gsonContacts[] = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(gsonContacts));
		long count = employeePayrollService.countEnteries(IOService.REST_IO);
		Assert.assertEquals(4, count);
	}

	@Test
	public void givenEmployeeToDelete_WhenDeleted_ShouldMatch200ResponseAndCount() {
		EmployeePayrollData[] arrayOfEmps = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));

		EmployeePayrollData employeePayRollData = employeePayrollService.getEmployeePayrollData("pallavi");
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		Response response = request.delete("/employee/" + employeePayRollData.id);
		int statusCode = response.getStatusCode();
		assertEquals(200, statusCode);

		employeePayrollService.deleteEmployeePayRoll(employeePayRollData.name, IOService.REST_IO);
		long entries = employeePayrollService.countEnteries(IOService.REST_IO);
		assertEquals(4, entries);
	}

}
