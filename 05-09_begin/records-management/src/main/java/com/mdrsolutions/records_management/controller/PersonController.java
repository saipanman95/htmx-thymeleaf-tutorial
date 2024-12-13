package com.mdrsolutions.records_management.controller;

import com.mdrsolutions.records_management.controller.dto.MissingDetailsDto;
import com.mdrsolutions.records_management.entity.*;
import com.mdrsolutions.records_management.service.*;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.FragmentsRendering;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

@Controller
public class PersonController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonController.class);
    private final PersonService personService;
    private final CheckPersonMissingFieldService missingFieldService;
    private final EmailService emailService;
    private final PhoneNumberService phoneNumberService;
    private final PersonAddressService personAddressService;
    private final EmployerService employerService;

    public PersonController(PersonService personService,
                            CheckPersonMissingFieldService missingFieldService,
                            EmailService emailService, PhoneNumberService phoneNumberService, PersonAddressService personAddressService, EmployerService employerService) {
        this.personService = personService;
        this.missingFieldService = missingFieldService;
        this.emailService = emailService;
        this.phoneNumberService = phoneNumberService;
        this.personAddressService = personAddressService;
        this.employerService = employerService;
    }


    @GetMapping("/person/{personId}")
    public String getPersonDetails(@PathVariable("personId") Long personId, Model model) {
        LOGGER.info("getPersonDetails(...)");
        Person person = personService.getPersonById(personId);
        MissingDetailsDto missingDetailsDto = missingFieldService.checkForMissingFields(person);
        model.addAttribute("person", person);
        model.addAttribute("missingDetailsCount", missingDetailsDto.getMissingCount());
        model.addAttribute("missingDetailsList", missingDetailsDto.getMissingFields());
        return "dashboard/person-info :: personal-info";
    }

    @HxPushUrl(HtmxValue.TRUE)
    @GetMapping("/person/view/{personId}")
    public String viewPersonFullDetails(@PathVariable("personId") Long personId, Model model) {
        LOGGER.info("viewPersonFullDetails(...) - Loading full details view for person ID: {}", personId);
        Person person = personService.getPersonById(personId);
        MissingDetailsDto missingDetailsDto = missingFieldService.checkForMissingFields(person);
        model.addAttribute("person", person);
        model.addAttribute("missingDetailsCount", missingDetailsDto.getMissingCount());
        model.addAttribute("missingDetailsList", missingDetailsDto.getMissingFields());

        return "person/person-full-details";
    }

    @HxReplaceUrl
    @GetMapping("/person/edit/{personId}")
    public String editPersonFullDetails(@PathVariable("personId") Long personId, Model model) {
        LOGGER.info("editPersonFullDetails(...) - Loading full details view for person ID: {}", personId);
        Person person = personService.getPersonById(personId);
        MissingDetailsDto missingDetailsDto = missingFieldService.checkForMissingFields(person);
        model.addAttribute("person", person);
        return "person/modify/editable-person-info :: editable-person-info";
    }

    @GetMapping("/person/cancel/{personId}")
    public String cancelPersonInfo(@PathVariable("personId") Long personId, Model model){
        LOGGER.info("viewPersonInfo(...) - returning to the person-info form from cancel");
        Person person = personService.getPersonById(personId);
        model.addAttribute("person", person);

        return "person/person-info :: personal-info";
    }

    @PostMapping("/person/update/{personId}")
    public String updatePersonFullDetails(@PathVariable("personId") Long personId,
                                          @ModelAttribute("person") Person updatedPerson,
                                          Model model) {
        LOGGER.info("updatePersonFullDetails(...) - Updating details for person ID: {}", personId);

        try {
            // Fetch the existing person from the database
            Person existingPerson = personService.getPersonById(personId);

            // Update the details of the existing person with the new values
            existingPerson.setPrefix(updatedPerson.getPrefix());
            existingPerson.setFirstName(updatedPerson.getFirstName());
            existingPerson.setMiddleName(updatedPerson.getMiddleName());
            existingPerson.setLastName(updatedPerson.getLastName());
            existingPerson.setSuffix(updatedPerson.getSuffix());
            existingPerson.setPersonType(updatedPerson.getPersonType());
            existingPerson.setLegalGuardianType(updatedPerson.getLegalGuardianType());
            existingPerson.setEmploymentStatus(updatedPerson.getEmploymentStatus());

            // Save the updated person back to the database
            personService.savePerson(existingPerson);

            // Add success message
            model.addAttribute("message", "Person details updated successfully.");

            // Redirect to the full details view after a successful update
            return "redirect:/person/view/" + personId;
        } catch (Exception e) {
            LOGGER.error("Error updating person details: {}", e.getMessage());

            // Add error message to the model if there's an issue
            model.addAttribute("errorMessage", "Failed to update person details.");

            // Return the edit page with the error message if something went wrong
            return "person/edit-person-info";
        }
    }

    @GetMapping("/person/{personId}/email/add")
    @HxRequest(triggerId = "add-email-button", target = "email-list")
    public String showAddEmailForm(@PathVariable("personId") Long personId, Model model) {
        model.addAttribute("email", new Email());
        model.addAttribute("personId", personId);
        model.addAttribute("edit",false);
        return "person/modify/editable-email-form :: email-form";
    }

    @GetMapping(value = "/person/{personId}/email/edit/{emailId}")
    @HxRequest(triggerName = "edit")
    public String showEditEmailForm(@PathVariable("personId") Long personId,
                                    @RequestHeader(value = "HX-Trigger", required = false) String elementId,
                                    @PathVariable("emailId") Long emailId, Model model,
                                    HtmxRequest htmxRequest) {
        LOGGER.info("hx-trigger id {}", elementId);
        LOGGER.info(htmxRequest.getTriggerId());
        Optional<Email> emailById = emailService.getEmailById(emailId);// Assuming you have a service to get an email by ID
        if (emailById.isPresent()) {
            model.addAttribute("email", emailById.get());
            model.addAttribute("personId", personId);
            model.addAttribute("edit",true);
            return "person/modify/editable-email-form :: email-form";
        }
        model.addAttribute("errorMessage", "email does not exist");
        return "person/modify/editable-email-form :: email-form";
    }

    @PutMapping(value = "/person/{personId}/email/update")
    @HxRequest
    public View updateEmail(@ModelAttribute Email email,
                            @PathVariable("personId") Long personId,
                            Model model) {

        // Verify that 'email' here contains the ID correctly and not the email string.
        LOGGER.info("Updating email for personId: {}, emailId: {}", personId, email.getEmailId());
        LOGGER.info("email address = {}", email.getEmailAddress());

        Person person = personService.getPersonById(personId);

        boolean emailExists = emailService.emailExistsForPerson(email.getEmailAddress(), person);

        if(emailExists){
            LOGGER.info("emailExists = {}",emailExists);
            // Email already exists, set error message
            String alertMessage = "The email address " + email.getEmailAddress() + " already exists for this person.";
            String alertLevel = "danger"; // Bootstrap class for error

            // Add the alert message and level to the model
            model.addAttribute("alertMessage", alertMessage);
            model.addAttribute("alertLevel", alertLevel);

            // Set HX-Retarget to the alert message placeholder
            return FragmentsRendering
                    .with("fragments/alert-message :: alert-message")
                    .header(HtmxResponseHeader.HX_RETARGET.getValue(), "#email-alert-message")
                    .build();
        } else {
            emailService.saveOrUpdateEmail(person, email);
            model.addAttribute("emails", person.getEmails());
            model.addAttribute("personId", personId);

            String message = email.getEmailAddress() + " was updated";
            String jsonTrigger = "{\"emailUpdated\":{\"level\":\"info\",\"target\":\"#email-alert-message\", \"message\":\"" + message + "\"}}";

            return FragmentsRendering
                    .with("person/emails-info :: emails-info")
                    .header(HtmxResponseHeader.HX_TRIGGER.getValue(), jsonTrigger)
                    .build();
        }
    }


    @PostMapping(value = "/person/{personId}/email/save")
    @HxRequest
    public View saveEmail(@ModelAttribute Email email,
                                  @PathVariable("personId") Long personId,
                                  Model model) {
        // Verify that 'email' here contains the ID correctly and not the email string.
        LOGGER.info("Saving email for personId: {}, emailId: {}", personId, email.getEmailId());

        Person person = personService.getPersonById(personId);
        emailService.saveOrUpdateEmail(person, email);
        MissingDetailsDto missingDetailsDto = missingFieldService.checkForMissingFields(person);

        model.addAttribute("missingDetailsCount", missingDetailsDto.getMissingCount());
        model.addAttribute("missingDetailsList", missingDetailsDto.getMissingFields());
        model.addAttribute("emails", person.getEmails());
        model.addAttribute("personId", personId);

        return FragmentsRendering
                .with("person/emails-info :: emails-info")
                .fragment("person/person-mark-for-review :: mark-for-review-info")
                .build();
    }


    @DeleteMapping("/person/{personId}/email/delete/{emailId}")
    public View deleteEmail(@PathVariable("personId") Long personId,
                                    @PathVariable("emailId") Long emailId,
                                    Model model) {
        LOGGER.info("deleteEmail(...) - emailId {}", emailId);
        Optional<Email> emailById = emailService.getEmailById(emailId);

        emailService.deleteEmail(emailId);
        Person person = personService.getPersonById(personId);
        MissingDetailsDto missingDetailsDto = missingFieldService.checkForMissingFields(person);

        model.addAttribute("email", emailById.get());
        model.addAttribute("personId", personId);
        model.addAttribute("fadeOut",true);
        model.addAttribute("missingDetailsCount", missingDetailsDto.getMissingCount());
        model.addAttribute("missingDetailsList", missingDetailsDto.getMissingFields());

        LOGGER.info("completed call to delete email");
        return FragmentsRendering
                .with("person/email-item :: email-item")
                .fragment("person/person-mark-for-review :: mark-for-review-info")
                .build();
    }

    //phones
    @GetMapping("/person/{personId}/phones")
    public String presentPhones(@PathVariable("personId") Long personId, Model model){
        List<PhoneNumber> phones = phoneNumberService.findPhonesByPersonId(personId);
        model.addAttribute("phoneNumbers", phones);
        return "person/phones-info :: phones-info";
    }

    @GetMapping("/person/{personId}/phone/add")
    public String showAddPhoneForm(@PathVariable("personId") Long personId, Model model) {
        model.addAttribute("phone", new PhoneNumber());
        model.addAttribute("personId", personId);
        return "person/phones-info :: phone-form";
    }

    @GetMapping("/person/{personId}/phone/edit/{phoneId}")
    public String showEditPhoneForm(@PathVariable("personId") Long personId,
                                    @PathVariable("phoneId") Long phoneId, Model model) {
       // Optional<Email> emailById = emailService.getEmailById(phoneId);// Assuming you have a service to get an email by ID
        Optional<PhoneNumber> phoneNumber = phoneNumberService.getPhoneNumberById(phoneId);
        if (phoneNumber.isPresent()) {
            model.addAttribute("phone", phoneNumber.get());
            model.addAttribute("personId", personId);
            return "person/phones-info :: phone-form";
        }
        model.addAttribute("errorMessage", "phone does not exist");
        return "person/phones-info :: phone-form";
    }

    @PostMapping(value = "/person/{personId}/phone/save")
    @HxRequest
    public View savePhone(@ModelAttribute PhoneNumber phoneNumber,
                            @PathVariable("personId") Long personId,
                            Model model) {
        LOGGER.info("Saving phone for personId: {}, phoneId: {}", personId, phoneNumber.getPhoneId());
        Person person = personService.getPersonById(personId);

        String alertMessage;
        String alertLevel = "success";

        if(phoneNumber.getNumber() == null || phoneNumber.getNumber().isEmpty() || phoneNumber.getNumber().isBlank()){
            LOGGER.info("Phone number is blank");
            alertMessage = "This phone number cannot be blank.";
            alertLevel = "danger";

            model.addAttribute("alertMessage", alertMessage);
            model.addAttribute("alertLevel", alertLevel);
            model.addAttribute("phone", phoneNumber);
            model.addAttribute("personId", personId);

            return FragmentsRendering
                    .with("person/phones-info :: phones-info")
                    .header(HtmxResponseHeader.HX_RESELECT.getValue(), "#phone-alert-message")
                    .header(HtmxResponseHeader.HX_RESWAP.getValue(), HxSwapType.BEFORE_BEGIN.getValue())
                    .build();
        }

        if (phoneNumberService.isDuplicatePhoneNumberForPerson(person, phoneNumber.getNumber())) {
            // Handling duplicate phone number scenario
            alertMessage = "This phone number already exists.";
            alertLevel = "danger";

        } else {
            // Save or update phone number
            phoneNumberService.saveOrUpdatePhone(person, phoneNumber);

            alertMessage = "Phone number saved successfully.";
            Set<PhoneNumber> updatedPhoneNumbers = person.getPhoneNumbers();

            model.addAttribute("phoneNumbers", updatedPhoneNumbers);
        }

        model.addAttribute("phone", phoneNumber);
        model.addAttribute("personId", personId);
        model.addAttribute("alertMessage", alertMessage);
        model.addAttribute("alertLevel", alertLevel);

        return FragmentsRendering
                .with("person/phones-info :: phones-info")
                .header(HtmxResponseHeader.HX_RESELECT.getValue(), "#phone-alert-message")
                .header(HtmxResponseHeader.HX_RESWAP.getValue(), HxSwapType.BEFORE_BEGIN.getValue())
                .build();

    }


    @DeleteMapping("/person/{personId}/phone/delete/{phoneId}")
    public View deletePhone(@PathVariable("personId") Long personId,
                            @PathVariable("phoneId") Long phoneId,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        LOGGER.info("deleteEmail(...) - phoneId {}", phoneId);
        Optional<PhoneNumber> phoneNumberById = phoneNumberService.getPhoneNumberById(phoneId);

        phoneNumberService.deletePhone(phoneId);
        Person person = personService.getPersonById(personId);
        MissingDetailsDto missingDetailsDto = missingFieldService.checkForMissingFields(person);

        model.addAttribute("phone", phoneNumberById.get());
        model.addAttribute("personId", personId);
        model.addAttribute("fadeOut",true);
        model.addAttribute("missingDetailsCount",missingDetailsDto.getMissingCount());
        model.addAttribute("missingDetailsList", missingDetailsDto.getMissingFields());

        return FragmentsRendering
                .with("person/phone-item :: phone-item")
                .fragment("person/person-mark-for-review :: mark-for-review-info")
                .build();
    }


    //person address section
    @GetMapping("/person/{personId}/person-address/add")
    public String showAddPersonAddressForm(@PathVariable("personId") Long personId, Model model) {
        model.addAttribute("address", new PersonAddress());
        model.addAttribute("personId", personId);
        return "person/modify/editable-person-address-form :: address-form";
    }

    @GetMapping("/person/{personId}/person-address/edit/{addressId}")
    public String showEditPersonAddressForm(@PathVariable("personId") Long personId,
                                    @PathVariable("addressId") Long addressId, Model model) {
        // Optional<Email> emailById = emailService.getEmailById(phoneId);// Assuming you have a service to get an email by ID
        Optional<PersonAddress> personAddress = personAddressService.getPersonAddressById(addressId);
        if (personAddress.isPresent()) {
            model.addAttribute("address", personAddress.get());
            model.addAttribute("addressId", addressId);
            return "person/modify/editable-person-address-form :: address-form";
        }
        model.addAttribute("errorMessage", "phone does not exist");
        return "person/modify/editable-phone-form :: phone-form";
    }

    @PostMapping("/person/{personId}/person-address/save")
    public String savePersonAddress(@ModelAttribute PersonAddress personAddress, @PathVariable("personId") Long personId, Model model) {
        // Verify that 'personAddress' here contains the ID correctly and not the email string.
        LOGGER.info("Saving phone for personId: {}, addressId: {}", personId, personAddress.getAddressId());
        Person person = personService.getPersonById(personId);
        personAddressService.saveOrUpdatePersonAddress(person, personAddress);
        return "redirect:/person/view/" + personId;
    }


    @DeleteMapping("/person/{personId}/person-address/delete/{addressId}")
    public RedirectView deletePersonAddress(@PathVariable("personId") Long personId, @PathVariable("addressId") Long addressId, RedirectAttributes redirectAttributes) {
        LOGGER.info("deletePersonAddress(...) - addressId {}", addressId);
        personAddressService.deletePersonAddress(addressId);
        redirectAttributes.addFlashAttribute("successMessage", "Person Address Deleted!");
        RedirectView redirectView = new RedirectView("/person/view/" + personId);
        redirectView.setStatusCode(HttpStatus.SEE_OTHER);
        LOGGER.info("completed call to person address");
        return redirectView;
    }

    //employers-info
    @GetMapping("/person/{personId}/employer/add")
    public String showAddEmployerForm(@PathVariable("personId") Long personId, Model model) {
        model.addAttribute("employer", new Employer());
        model.addAttribute("personId", personId);
        return "person/modify/editable-employers-form :: employer-form";
    }

    @GetMapping("/person/{personId}/employer/edit/{employerId}")
    public String showEditEmployerForm(@PathVariable("personId") Long personId,
                                            @PathVariable("employerId") Long employerId, Model model) {
        Optional<Employer> employer = employerService.getEmployerById(employerId);
        if (employer.isPresent()) {
            model.addAttribute("employer", employer.get());
            model.addAttribute("employerId", employerId);
            return "person/modify/editable-employers-form :: employer-form";
        }
        model.addAttribute("errorMessage", "Employer does not exist");
        return "person/modify/editable-employers-form :: employer-form";
    }

    @PostMapping("/person/{personId}/employer/save")
    public void saveEmployer(@ModelAttribute Employer employer,
                               @PathVariable("personId") Long personId,
                               Model model
                               ) {
        // Verify that 'personAddress' here contains the ID correctly and not the email string.
        LOGGER.info("Saving phone for personId: {}, employerId: {}", personId, employer.getEmployerId());
        Person person = personService.getPersonById(personId);
        employerService.saveOrUpdateEmployer(person, employer);
        //return "redirect:/person/view/" + personId;
    }


    @DeleteMapping("/person/{personId}/employer/delete/{addressId}")
    public RedirectView deleteEmployer(@PathVariable("personId") Long personId, @PathVariable("employerId") Long employerId, RedirectAttributes redirectAttributes) {
        LOGGER.info("deleteEmployer(...) - employerId {}", employerId);
        employerService.deleteEmployer(employerId);
        redirectAttributes.addFlashAttribute("successMessage", "Employer Deleted!");
        RedirectView redirectView = new RedirectView("/person/view/" + personId);
        redirectView.setStatusCode(HttpStatus.SEE_OTHER);
        LOGGER.info("completed call to Employer");
        return redirectView;
    }
}
