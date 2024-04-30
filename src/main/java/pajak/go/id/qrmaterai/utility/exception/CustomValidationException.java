package pajak.go.id.qrmaterai.utility.exception;

import org.springframework.http.HttpStatus;

public class CustomValidationException extends RuntimeException {

	private HttpStatus status;
	private String pesan;
	
	public CustomValidationException(HttpStatus status, String pesan) {
		super();
		this.status = status;
		this.pesan = pesan;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public String getPesan() {
		return pesan;
	}

	public void setPesan(String pesan) {
		this.pesan = pesan;
	}
	

}
