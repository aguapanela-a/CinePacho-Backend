package CinePacho.demo.employeeManageer.controller;

import CinePacho.demo.auth.dto.response.RegisterResponseDTO;
import CinePacho.demo.employeeManageer.dto.request.RegisterEmployeeRequestDTO;
import CinePacho.demo.employeeManageer.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EmployeeController {
    private EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    //Necesita aplicar filtro JWT
    @PostMapping("/admin/register_employee")
    public ResponseEntity<RegisterResponseDTO> registerEmployee( @Valid @RequestBody RegisterEmployeeRequestDTO registerEmployeeRequestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.registerEmployee(registerEmployeeRequestDTO));
    }
}
