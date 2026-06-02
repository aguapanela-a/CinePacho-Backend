package CinePacho.demo.employeeManageer.dto.response;

import java.math.BigDecimal;
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
    String rol;
    String indentityCard;
    BigDecimal salary;
    Long uniqueCode;
    String nameMultiplex;
    LocalDate startDate;
    LocalDate roleUpdatedAt;

}
