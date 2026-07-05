package tools.vitruv.methodologisttemplate.vsum;

import mir.reactions.model2Model2.Model2Model2ChangePropagationSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.change.propagation.ChangePropagationMode;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.compmodelcons.change.ViewChangePropagationSpecificationAdapterFactory;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;
import tools.vitruv.methodologisttemplate.model.model.System;
import tools.vitruv.methodologisttemplate.model.model2.Root;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ComponentizedConsistencyExampleTest extends AbstractTest {
    // @Test
    // todo: get green
    void insertComponentUsingViewBasedConsistency(@TempDir Path tempDir) {
        InternalVirtualModel vsum = createViewBasedVirtualModel(tempDir);
        addSystem(vsum, tempDir);
        addComponent(vsum);
        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(tools.vitruv.methodologisttemplate.model.model.System.class, Root.class)), (View v) -> {
            // assert that a component has been inserted, a entity has been created and that
            // both have the same name
            // Note: to make the test result easier to understand, these different effects
            // should be tested one by one
            return v.getRootObjects(System.class).iterator().next()
                    .getComponents().get(0).getName()
                    .equals(v.getRootObjects(Root.class).iterator().next()
                            .getEntities().get(0).getName());
        }));
    }

    private InternalVirtualModel createViewBasedVirtualModel(Path projectPath) {
        InternalVirtualModel model = new VirtualModelBuilder()
                .withStorageFolder(projectPath)
                .withUserInteractorForResultProvider(new TestUserInteraction.ResultProvider(new TestUserInteraction()))
                .withChangePropagationSpecifications(ViewChangePropagationSpecificationAdapterFactory.INSTANCE.createRemote(
                        Optional.of(null /* todo: put view here */),
                        new Model2Model2ChangePropagationSpecification(),
                        Optional.empty()))
                .buildAndInitialize();
        model.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC);
        return model;
    }
}
