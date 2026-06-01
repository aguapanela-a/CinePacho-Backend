package CinePacho.demo.rooms.controller;

import CinePacho.demo.shared.auxiliaryClass.DTOResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import CinePacho.demo.rooms.dto.response.RoomDetailResponse;
import CinePacho.demo.rooms.service.RoomService;
import CinePacho.demo.reports.service.SalesReportService;
import CinePacho.demo.reports.dto.response.MultiplexSalesReport;
import java.time.LocalDate;

import java.util.UUID;
 
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {
 
    private final RoomService roomService;
    private final SalesReportService salesReportService;
    
    // @GetMapping("admin/rooms/{id}")
    // public ResponseEntity<RoomDetailResponse> getById(@PathVariable UUID id) {
    //     return ResponseEntity.ok(roomService.getById(id));
    // }


    //TODO: retornar lista de salas con la info de cada sala de un multiplex por el id de este


    @PostMapping("admin/{multiplexId}/rooms")
    public ResponseEntity<DTOResponse> create(@Valid @PathVariable UUID multiplexId) {

        roomService.create(multiplexId);
        
        // Respuesta estándar para creación de sala
        DTOResponse response = DTOResponse.withStatus(
                "Sala de cine creada con éxito",
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
  
    @DeleteMapping("admin/rooms/{id}")
    public ResponseEntity<DTOResponse> delete(@PathVariable UUID id) {
        roomService.delete(id);
        // Respuesta estándar para eliminación de sala
        DTOResponse response = DTOResponse.withStatus(
                "Sala de cine eliminada con éxito",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("admin/{multiplexId}/reports/sales/monthly")
    public ResponseEntity<MultiplexSalesReport> getMonthlySalesReportByMultiplex(
            @PathVariable UUID multiplexId,
            @RequestParam("endDate") LocalDate endDate
    ) {
        MultiplexSalesReport report = salesReportService.buildMonthlySalesReportByMultiplex(multiplexId, endDate);
        return ResponseEntity.ok(report);
    }
}
