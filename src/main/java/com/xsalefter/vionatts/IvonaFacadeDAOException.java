package com.xsalefter.vionatts;

/**
 * Aggregate exception for all operation in {@link IvonaFacadeDAO}.
 *  
 * @author xsalefter
 */
public class IvonaFacadeDAOException extends RuntimeException {

	private static final long serialVersionUID = 4304486707195838991L;

	public IvonaFacadeDAOException() {
		super();
	}

	public IvonaFacadeDAOException(String message, Throwable cause) {
		super(message, cause);
	}

	public IvonaFacadeDAOException(String message) {
		super(message);
	}

	public IvonaFacadeDAOException(Throwable cause) {
		super(cause);
	}
}
