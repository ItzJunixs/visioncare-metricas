package co.visioncare.vcapi.pacientes.internal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.visioncare.vcapi.pacientes.internal.service.DeactivatePatientUseCase;
import co.visioncare.vcapi.pacientes.internal.service.DeleteOpticalFormulaUseCase;
import co.visioncare.vcapi.pacientes.internal.service.GetPatientUseCase;
import co.visioncare.vcapi.pacientes.internal.service.ListOpticalFormulasUseCase;
import co.visioncare.vcapi.pacientes.internal.service.UpdateOpticalFormulaUseCase;
import co.visioncare.vcapi.pacientes.internal.service.UpdatePatientUseCase;
import co.visioncare.vcapi.pacientes.internal.service.RegisterOpticalFormulaUseCase;
import co.visioncare.vcapi.pacientes.internal.service.RegisterPatientUseCase;
import co.visioncare.vcapi.pacientes.internal.service.SearchPatientsUseCase;
import co.visioncare.vcapi.pacientes.internal.entity.OpticalFormulaRepository;
import co.visioncare.vcapi.pacientes.internal.entity.PatientRepository;

@Configuration
public class PacientesConfig {

    @Bean
    RegisterPatientUseCase registerPatientUseCase(PatientRepository patients) {
        return new RegisterPatientUseCase(patients);
    }

    @Bean
    SearchPatientsUseCase searchPatientsUseCase(PatientRepository patients) {
        return new SearchPatientsUseCase(patients);
    }

    @Bean
    GetPatientUseCase getPatientUseCase(PatientRepository patients, OpticalFormulaRepository formulas) {
        return new GetPatientUseCase(patients, formulas);
    }

    @Bean
    RegisterOpticalFormulaUseCase registerOpticalFormulaUseCase(OpticalFormulaRepository formulas, PatientRepository patients) {
        return new RegisterOpticalFormulaUseCase(formulas, patients);
    }

    @Bean
    UpdatePatientUseCase updatePatientUseCase(PatientRepository patients) {
        return new UpdatePatientUseCase(patients);
    }

    @Bean
    DeactivatePatientUseCase deactivatePatientUseCase(PatientRepository patients) {
        return new DeactivatePatientUseCase(patients);
    }

    @Bean
    ListOpticalFormulasUseCase listOpticalFormulasUseCase(PatientRepository patients, OpticalFormulaRepository formulas) {
        return new ListOpticalFormulasUseCase(patients, formulas);
    }

    @Bean
    UpdateOpticalFormulaUseCase updateOpticalFormulaUseCase(OpticalFormulaRepository formulas) {
        return new UpdateOpticalFormulaUseCase(formulas);
    }

    @Bean
    DeleteOpticalFormulaUseCase deleteOpticalFormulaUseCase(OpticalFormulaRepository formulas) {
        return new DeleteOpticalFormulaUseCase(formulas);
    }
}
