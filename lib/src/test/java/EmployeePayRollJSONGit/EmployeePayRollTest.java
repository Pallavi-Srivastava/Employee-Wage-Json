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
		long count = employeePayrollService
				.countEnteries(IOService.REST_IO);
		Assert.assertEquals(3, count);
	}
}
