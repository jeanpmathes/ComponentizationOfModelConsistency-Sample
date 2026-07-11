package tools.vitruv.methodologisttemplate.vsum;

import mir.reactions.model2Model2.Model2Model2ChangePropagationSpecification;
import neojoin.viewtypes.model2_identity.Model2IdentityViewType;
import neojoin.viewtypes.model_identity.ModelIdentityViewType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.change.propagation.ChangePropagationMode;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.compmodelcons.change.ViewChangePropagationSpecificationAdapterFactory;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;
import tools.vitruv.methodologisttemplate.model.model.ModelPackage;
import tools.vitruv.methodologisttemplate.model.model.System;
import tools.vitruv.methodologisttemplate.model.model2.Model2Package;
import tools.vitruv.methodologisttemplate.model.model2.Root;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ComponentizedConsistencyExampleTest extends AbstractTest {
    @Test
    void insertComponentUsingViewBasedConsistency(@TempDir Path tempDir) {
        InternalVirtualModel vsum = createVirtualModel(tempDir);

        addSystem(vsum, tempDir);
        addComponent(vsum);

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class, Root.class)), (View v) -> {
            var component = v.getRootObjects(System.class).iterator().next().getComponents().get(0);
            var entity = v.getRootObjects(Root.class).iterator().next().getEntities().get(0);

            return component.getName().equals(entity.getName());
        }));
    }

    private InternalVirtualModel createVirtualModel(Path projectPath) {
        // The view type loads the metamodels using their URI, so they are not automatically added to the registry.
        ModelPackage.eINSTANCE.eClass();
        Model2Package.eINSTANCE.eClass(); // todo: find a nicer solution to this

        InternalVirtualModel model = new VirtualModelBuilder()
                .withStorageFolder(projectPath)
                .withUserInteractorForResultProvider(new TestUserInteraction.ResultProvider(new TestUserInteraction()))
                .withChangePropagationSpecifications(ViewChangePropagationSpecificationAdapterFactory.INSTANCE.createRemote(
                        Optional.of(new ModelIdentityViewType()),
                        new Model2Model2ChangePropagationSpecification(),
                        Optional.of(new Model2IdentityViewType())))
                .buildAndInitialize();
        model.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC);
        return model;
    }
}
