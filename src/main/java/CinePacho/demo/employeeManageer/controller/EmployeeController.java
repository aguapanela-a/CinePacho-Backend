package CinePacho.demo.employeeManageer.controller;

import CinePacho.demo.auth.dto.response.RegisterResponseDTO;
import CinePacho.demo.employeeManageer.dto.request.RegisterEmployeeRequestDTO;
import CinePacho.demo.employeeManageer.dto.response.EmployeesResponseDTO;
import CinePacho.demo.employeeManageer.service.EmployeeService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EmployeeController {
    private EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/admin/employees")
    public ResponseEntity<List<EmployeesResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAll());
    }

    @GetMapping("/admin/employees/{multiplexId}")
    public ResponseEntity<List<EmployeesResponseDTO>> getAllEmployees(
            @PathVariable UUID multiplexId
    ) {
        return ResponseEntity.ok(employeeService.getAllEmployeesByMultiplex(multiplexId));
    }

    //Necesita aplicar filtro JWT
    @PostMapping("/admin/register_employee")
    public ResponseEntity<RegisterResponseDTO> registerEmployee( @Valid @RequestBody RegisterEmployeeRequestDTO registerEmployeeRequestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.registerEmployee(registerEmployeeRequestDTO));
    }

    @PutMapping("/admin/update_employee")
    public ResponseEntity<RegisterResponseDTO> updateEmployee(
            @Valid @RequestBody RegisterEmployeeRequestDTO registerEmployeeRequestDTO
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.updateEmployee(registerEmployeeRequestDTO));
    }   

    @DeleteMapping("/admin/delete_employee/{uniqueCode}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long uniqueCode) {
        employeeService.deleteEmployeeByUniqueCode(uniqueCode);
        return ResponseEntity.noContent().build();
    }
}
