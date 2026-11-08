package co.visioncare.vcapi.pacientes.internal.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.visioncare.vcapi.pacientes.internal.service.GetPatientUseCase;
import co.visioncare.vcapi.pacientes.internal.service.RegisterOpticalFormulaUseCase;
import co.visioncare.vcapi.pacientes.internal.service.RegisterPatientUseCase;
import co.visioncare.vcapi.pacientes.internal.service.UpdatePatientUseCase;
import co.visioncare.vcapi.pacientes.internal.service.DeactivatePatientUseCase;
import co.visioncare.vcapi.pacientes.internal.service.ListOpticalFormulasUseCase;
import co.visioncare.vcapi.pacientes.internal.service.UpdateOpticalFormulaUseCase;
import co.visioncare.vcapi.pacientes.internal.service.DeleteOpticalFormulaUseCase;
import co.visioncare.vcapi.pacientes.internal.service.SearchPatientsUseCase;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormula;
import co.visioncare.vcapi.pacientes.internal.entity.Patient;
import jakarta.validation.Valid;
import co.visioncare.vcapi.pacientes.internal.dto.*;

/** HU-01, HU-02, HU-03, HU-04 — see vc-docs/07-api/contracts/openapi/pacientes-api.yaml. */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final RegisterPatientUseCase register;
    private final SearchPatientsUseCase search;
    private final GetPatientUseCase get;
    private final UpdatePatientUseCase update;
    private final DeactivatePatientUseCase deactivate;
    private final RegisterOpticalFormulaUseCase registerFormula;
    private final ListOpticalFormulasUseCase listFormulas;
    private final UpdateOpticalFormulaUseCase updateFormula;
    private final DeleteOpticalFormulaUseCase deleteFormula;

    public PatientController(RegisterPatientUseCase register, SearchPatientsUseCase search,
                             GetPatientUseCase get, UpdatePatientUseCase update,
                             DeactivatePatientUseCase deactivate, RegisterOpticalFormulaUseCase registerFormula,
                             ListOpticalFormulasUseCase listFormulas, UpdateOpticalFormulaUseCase updateFormula,
                             DeleteOpticalFormulaUseCase deleteFormula) {
        this.register = register;
        this.search = search;
        this.get = get;
        this.update = update;
        this.deactivate = deactivate;
        this.registerFormula = registerFormula;
        this.listFormulas = listFormulas;
        this.updateFormula = updateFormula;
        this.deleteFormula = deleteFormula;
    }

    @GetMapping
    public List<PatientResponse> list(@RequestParam(required = false) String q) {
        return search.execute(q).stream().map(PatientResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PatientDetailResponse byId(@PathVariable UUID id) {
        GetPatientUseCase.Result r = get.execute(id);
        return PatientDetailResponse.from(r);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse create(@RequestBody @Valid CreatePatientRequest r) {
        Patient patient = register.execute(new RegisterPatientUseCase.Command(
                r.documentType(), r.documentNumber(), r.firstName(), r.lastName(), r.phone(), r.email(), r.eps()));
        return PatientResponse.from(patient);
    }

    @PostMapping("/{id}/optical-formulas")
    @ResponseStatus(HttpStatus.CREATED)
    public FormulaResponse registerFormula(@PathVariable UUID id, @RequestBody @Valid NewFormulaRequest r) {
        OpticalFormula f = registerFormula.execute(id, new RegisterOpticalFormulaUseCase.Command(
                r.odSph(), r.odCyl(), r.odAxis(), r.oiSph(), r.oiCyl(), r.oiAxis(), r.add(),
                r.pupillaryDistance(), r.isCurrent() == null || r.isCurrent(), r.formulaDate()));
        return FormulaResponse.from(f);
    }

    @PutMapping("/{id}")
    public PatientResponse update(@PathVariable UUID id, @RequestBody @Valid CreatePatientRequest r) {
        Patient patient = update.execute(id, new RegisterPatientUseCase.Command(
                r.documentType(), r.documentNumber(), r.firstName(), r.lastName(), r.phone(), r.email(), r.eps()));
        return PatientResponse.from(patient);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        deactivate.execute(id);
    }

    @GetMapping("/{id}/optical-formulas")
    public List<FormulaResponse> formulas(@PathVariable UUID id) {
        return listFormulas.execute(id).stream().map(FormulaResponse::from).toList();
    }

    @PutMapping("/{id}/optical-formulas/{formulaId}")
    public FormulaResponse updateFormula(@PathVariable UUID id, @PathVariable UUID formulaId,
                                         @RequestBody @Valid NewFormulaRequest r) {
        OpticalFormula f = updateFormula.execute(id, formulaId, new RegisterOpticalFormulaUseCase.Command(
                r.odSph(), r.odCyl(), r.odAxis(), r.oiSph(), r.oiCyl(), r.oiAxis(), r.add(),
                r.pupillaryDistance(), r.isCurrent() == null || r.isCurrent(), r.formulaDate()));
        return FormulaResponse.from(f);
    }

    @DeleteMapping("/{id}/optical-formulas/{formulaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFormula(@PathVariable UUID id, @PathVariable UUID formulaId) {
        deleteFormula.execute(id, formulaId);
    }
}
