package com.mdrsolutions.records_management.service;

import com.mdrsolutions.records_management.entity.Employer;
import com.mdrsolutions.records_management.entity.Person;
import com.mdrsolutions.records_management.repository.EmployerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployerService {
    private final EmployerRepository employerRepository;

    public EmployerService(EmployerRepository employerRepository) {
        this.employerRepository = employerRepository;
    }

    public Optional<Employer> getEmployerById(Long employerId) {
        return employerRepository.findById(employerId);
    }

    public Employer saveOrUpdateEmployer(Person person, Employer employer) {
        employer.setPerson(person);
        return employerRepository.save(employer);
    }

    public List<Employer> findEmployersByPersonId(Long personId){
        return employerRepository.findByPerson_PersonId(personId);
    }

    public void deleteEmployer(Long employerId) {
        employerRepository.deleteById(employerId);
    }
}
