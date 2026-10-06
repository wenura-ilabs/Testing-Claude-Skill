package com.example.booking.web;

import java.util.List;
import java.util.stream.Collectors;

import com.example.booking.repository.ServiceOfferingRepository;
import com.example.booking.web.dto.ServiceResponse;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

	private final ServiceOfferingRepository serviceRepository;

	public ServiceController(ServiceOfferingRepository serviceRepository) {
		this.serviceRepository = serviceRepository;
	}

	@GetMapping
	public List<ServiceResponse> list() {
		return serviceRepository.findAll(Sort.by("name")).stream()
			.map(ServiceResponse::new)
			.collect(Collectors.toList());
	}

}
