package com.neighborparking.web;

import com.neighborparking.domain.Vehicle;
import com.neighborparking.repository.VehicleRepository;
import com.neighborparking.security.SecuritySupport;
import com.neighborparking.service.DomainSupport;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleRepository repository;

    public VehicleController(VehicleRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Vehicle> list() {
        return repository.findAllByUserIdOrderByCreatedAtDesc(SecuritySupport.currentUser().getUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicle create(@Valid @RequestBody VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setUserId(SecuritySupport.currentUser().getUserId());
        apply(vehicle, request);
        return repository.save(vehicle);
    }

    @PutMapping("/{id}")
    public Vehicle update(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        Vehicle vehicle = repository.findByIdAndUserId(id, SecuritySupport.currentUser().getUserId())
                .orElseThrow(() -> DomainSupport.notFound("车辆不存在"));
        apply(vehicle, request);
        return repository.save(vehicle);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        Vehicle vehicle = repository.findByIdAndUserId(id, SecuritySupport.currentUser().getUserId())
                .orElseThrow(() -> DomainSupport.notFound("车辆不存在"));
        vehicle.setActive(Boolean.FALSE);
        repository.save(vehicle);
    }

    private void apply(Vehicle vehicle, VehicleRequest request) {
        vehicle.setPlateNumber(request.getPlateNumber().trim().toUpperCase());
        vehicle.setVehicleType(request.getVehicleType().trim());
        vehicle.setActive(Boolean.TRUE);
    }

    @Data
    @NoArgsConstructor
    public static class VehicleRequest {
        @NotBlank(message = "车牌号不能为空")
        @Size(max = 24, message = "车牌号最多 24 个字符")
        private String plateNumber;

        @NotBlank(message = "车辆类型不能为空")
        @Size(max = 32, message = "车辆类型最多 32 个字符")
        private String vehicleType;
    }
}
