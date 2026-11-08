package co.visioncare.vcapi.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The `boundary check` of 05-architecture/boundary-enforcement.md: a module may use another
 * module's public facade, never its internal package; inside a module the layers only depend
 * downwards (controller, service, entity).
 */
@AnalyzeClasses(packages = "co.visioncare.vcapi", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundariesTest {

    @ArchTest
    static final ArchRule pacientes_internals_are_private = noClasses().that()
            .resideOutsideOfPackage("co.visioncare.vcapi.pacientes..")
            .should().dependOnClassesThat().resideInAPackage("co.visioncare.vcapi.pacientes.internal..");

    @ArchTest
    static final ArchRule inventario_internals_are_private = noClasses().that()
            .resideOutsideOfPackage("co.visioncare.vcapi.inventario..")
            .should().dependOnClassesThat().resideInAPackage("co.visioncare.vcapi.inventario.internal..");

    @ArchTest
    static final ArchRule ordenes_internals_are_private = noClasses().that()
            .resideOutsideOfPackage("co.visioncare.vcapi.ordenes..")
            .should().dependOnClassesThat().resideInAPackage("co.visioncare.vcapi.ordenes.internal..");

    @ArchTest
    static final ArchRule facturacion_internals_are_private = noClasses().that()
            .resideOutsideOfPackage("co.visioncare.vcapi.facturacion..")
            .should().dependOnClassesThat().resideInAPackage("co.visioncare.vcapi.facturacion.internal..");

    @ArchTest
    static final ArchRule usuarios_internals_are_private = noClasses().that()
            .resideOutsideOfPackage("co.visioncare.vcapi.usuarios..")
            .should().dependOnClassesThat().resideInAPackage("co.visioncare.vcapi.usuarios.internal..");

    @ArchTest
    static final ArchRule entities_do_not_know_the_outer_layers = noClasses().that()
            .resideInAPackage("..internal.entity..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..internal.service..", "..internal.controller..", "..internal.repository..");

    @ArchTest
    static final ArchRule controllers_do_not_touch_repositories = noClasses().that()
            .resideInAPackage("..internal.controller..")
            .should().dependOnClassesThat().resideInAPackage("..internal.repository..");
}
