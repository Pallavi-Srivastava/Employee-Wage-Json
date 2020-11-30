package EmployeePayRollJSONGit;

public class EmployeePayrollException extends Exception {

	enum ExceptionType {
		DatabaseException, NoSuchClass, ResourcesNotClosedException,  ConnectionFailed, CommitFailed
	}

	public ExceptionType type;

	public EmployeePayrollException(String message, ExceptionType type) {
		super(message);
		this.type = type;
	}
}
