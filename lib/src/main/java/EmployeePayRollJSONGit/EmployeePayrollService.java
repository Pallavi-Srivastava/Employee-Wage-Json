package EmployeePayRollJSONGit;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeePayrollService {

	public enum IOService {
		DB_IO, FILE_IO, REST_IO
	}

	private List<EmployeePayrollData> employeePayrollList;
	private static EmployeePayrollDBService employeePayrollDBService;

	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
	}

	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
		this();
		this.employeePayrollList = employeePayrollList;
	}

	public List<EmployeePayrollData> readEmployeePayrollData(IOService ioService)
			throws EmployeePayrollException, SQLException {
		if (ioService.equals(IOService.DB_IO))
			return this.employeePayrollList = employeePayrollDBService.readData(null, null);
		return this.employeePayrollList;
	}

	public List<EmployeePayrollData> readEmployeePayrollData(IOService ioService, String start, String end)
			throws EmployeePayrollException {
		try {
			LocalDate startLocalDate = LocalDate.parse(start);
			LocalDate endLocalDate = LocalDate.parse(end);
			if (ioService.equals(IOService.DB_IO))
				return employeePayrollDBService.readData(startLocalDate, endLocalDate);
			return this.employeePayrollList;
		} catch (EmployeePayrollException e) {
			throw new EmployeePayrollException(e.getMessage(),
					EmployeePayrollException.ExceptionType.DatabaseException);
		}
	}

	public void updateRecord(String name, double salary) throws EmployeePayrollException {
		int result = employeePayrollDBService.updateEmployeeData(name, salary);
		if (result == 0)
			return;
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if (employeePayrollData != null)
			employeePayrollData.salary = salary;
	}

	public boolean checkUpdatedRecordSyncWithDatabase(String name) throws EmployeePayrollException {
		List<EmployeePayrollData> employeePayrollData = employeePayrollDBService.getEmployeePayrollData(name);
		return employeePayrollData.get(0).equals(getEmployeePayrollData(name));
	}

	EmployeePayrollData getEmployeePayrollData(String name) {
		return this.employeePayrollList.stream()
				.filter(employeePayrollDataItem -> employeePayrollDataItem.name.endsWith(name)).findFirst()
				.orElse(null);
	}

	public int readEmployeePayrollData(String function, String gender) throws EmployeePayrollException {
		return employeePayrollDBService.readDataPayroll(function, gender);
	}

	public int readEmployeePayrollAvgData(String function, String gender) throws EmployeePayrollException {
		return employeePayrollDBService.readDataAvgPayroll(function, gender);
	}

	public void addEmployeeToPayroll(String name, double salary, LocalDate start, String gender)
			throws EmployeePayrollException {
		employeePayrollList.add(employeePayrollDBService.addEmployeeToPayrollDetails(name, salary, start, gender));
	}

	public void addNewEmployeeToPayroll(String name, double salary, LocalDate start, String gender, String department)
			throws EmployeePayrollException {
		employeePayrollList
				.add(employeePayrollDBService.addNewEmployeeToPayroll(name, salary, start, gender, department));
	}

	public void addEmployeePayrollDataMultipleThreads(List<EmployeePayrollData> employeePayRollList) {
		employeePayRollList.forEach(employeePayrollData -> {
			try {
				this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.salary,
						employeePayrollData.startDate, employeePayrollData.gender);
			} catch (EmployeePayrollException e) {
				e.printStackTrace();
			}
		});
	}

	public long countEnteries(IOService ioService) {
		if(ioService.equals(IOService.FILE_IO))
			return new EmployeePayrollService().countEnteries(ioService);
		return employeePayrollList.size();
	}

	public void addEmployeeToPayRollWIthThreads(List<EmployeePayrollData> employeePayRollList)
			throws EmployeePayrollException {
		Map<Integer, Boolean> employeeAditionStatus = new HashMap<Integer, Boolean>();
		employeePayRollList.forEach(employeePayrollData -> {
			Runnable task = () -> {
				employeeAditionStatus.put(employeePayrollData.hashCode(), false);
				System.out.println("Employee Added:" + Thread.currentThread().getName());
				try {
					this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.salary,
							employeePayrollData.startDate, employeePayrollData.gender);
				} catch (EmployeePayrollException e) {
					e.printStackTrace();
				}
				employeeAditionStatus.put(employeePayrollData.hashCode(), true);
				System.out.println("Employee Added: " + Thread.currentThread().getName());

			};
			Thread thread = new Thread(task, employeePayrollData.name);
			thread.start();
		});
		while (employeeAditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
	}

	public void updateEmployeeToPayRollWithThreads(String name, double salary) {

		Map<Integer, Boolean> employeeAditionStatus = new HashMap<Integer, Boolean>();
		Runnable task = () -> {
			employeeAditionStatus.put(name.hashCode(), false);
			try {
				this.updateRecord(name, salary);
			} catch (EmployeePayrollException e) {
				e.printStackTrace();
			}
			employeeAditionStatus.put(name.hashCode(), true);
			Thread thread = new Thread();
			thread.start();
		};
	}

	public void addNewEmployeeUsingREST(List<EmployeePayrollData> contactList) throws EmployeePayrollException {
		employeePayrollDBService.addNewContactsUsingRestAPI(contactList);
	}
	
	public void deleteEmployeePayRoll(String name, IOService ioService) {
		if(ioService.equals(IOService.REST_IO)) {
			EmployeePayrollData employeePayRollData = this.getEmployeePayrollData(name);
			employeePayrollList.remove(employeePayRollData);
		}
	}
}
