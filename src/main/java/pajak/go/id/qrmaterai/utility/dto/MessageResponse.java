/**
 *
 */
package pajak.go.id.qrmaterai.utility.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * @author 810201402
 */
@Data
@AllArgsConstructor
public class MessageResponse {

    private HttpStatus code;
    private String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;


    public MessageResponse() {

    }

    public MessageResponse(String message, HttpStatus httpStatus) {
        this.code = httpStatus;
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

}
