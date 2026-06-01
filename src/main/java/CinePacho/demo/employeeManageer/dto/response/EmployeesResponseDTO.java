package CinePacho.demo.employeeManageer.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
@AllArgsConstructor
public class EmployeesResponseDTO {
    
    String name;
    String email;
    String phoneNumber;
    String role;
    Long uniqueCode;
    String nameMultiplex;
    LocalDate startDate;
    LocalDate roleUpdatedAt;

}
