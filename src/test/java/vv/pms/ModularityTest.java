package vv.pms;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * A JUnit test class responsible for verifying the Spring Modulith structure
 * and generating architectural documentation snippets.
 */
class ModularityTest {

    /**
     * Test 1: Verifies the structural integrity of the application modules.
     * This test fails if any module dependency rules are violated
     * (e.g., circular dependencies, accessing internal packages of another module).
     */
    @Test
    void verifyApplicationModules() {
        // Automatically discovers modules based on top-level packages under vv.pms
        // and checks all defined architectural rules.
        ApplicationModules.of(Application.class).verify();
    }

    // -------------------------------------------------------------------------

    /**
     * Test 2 (Optional but Recommended): Generates documentation snippets.
     * These snippets can be included in your project documentation (e.g., README)
     * to visualize the module structure and dependencies.
     * * Output files are created in the 'target/modulith-docs/' directory.
     */
    @Test
    void writeDocumentationSnippets() {
        var modules = ApplicationModules.of(Application.class).verify();

        new Documenter(modules)
                // Generates a PlantUML diagram of the overall module arrangement
                .writeModulesAsPlantUml();
    }
}