package tools.vitruv.methodologisttemplate.vsum;

import mir.reactions.model2Model2.Model2Model2ChangePropagationSpecification;
import mir.reactions.model2ModelView2.Model2ModelView2ChangePropagationSpecification;
import mir.reactions.modelView2Model2.ModelView2Model2ChangePropagationSpecification;
import neojoin.viewtypes.model2_identity.Model2IdentityViewType;
import neojoin.viewtypes.model_identity.ModelIdentityViewType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.vitruv.change.propagation.ChangePropagationMode;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.compmodelcons.change.ViewChangePropagationSpecificationAdapterFactory;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;
import tools.vitruv.methodologisttemplate.model.model.System;
import tools.vitruv.methodologisttemplate.model.model2.Root;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ComponentizedConsistencyExampleTest extends AbstractTest {
    @TempDir
    private Path projectPath;

    private static List<ChangePropagationSpecification> getChangePropagationSpecifications(Configuration configuration) {
        return switch (configuration) {
            case NO_VIEW_USAGE -> ViewChangePropagationSpecificationAdapterFactory.INSTANCE.createRemote(
                    Optional.empty(),
                    new Model2Model2ChangePropagationSpecification(),
                    Optional.empty());
            case VIEW_AS_SOURCE -> ViewChangePropagationSpecificationAdapterFactory.INSTANCE.createRemote(
                    Optional.of(new ModelIdentityViewType()),
                    new ModelView2Model2ChangePropagationSpecification(),
                    Optional.empty()
            );
            case VIEW_AS_TARGET -> ViewChangePropagationSpecificationAdapterFactory.INSTANCE.createRemote(
                    Optional.empty(),
                    new Model2ModelView2ChangePropagationSpecification(),
                    Optional.of(new Model2IdentityViewType())
            );
            case VIEW_AS_SOURCE_AND_TARGET -> ViewChangePropagationSpecificationAdapterFactory.INSTANCE.createRemote(
                    Optional.of(new ModelIdentityViewType()),
                    new Model2ModelView2ChangePropagationSpecification(),
                    Optional.of(new Model2IdentityViewType())
            );
        };
    }

    @ParameterizedTest
    @EnumSource(Configuration.class)
    void insertComponent(Configuration configuration) throws IOException {
        VirtualModel vsum = createVirtualModel(configuration);

        addSystem(vsum, projectPath);
        addComponent(vsum);

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class, Root.class)), (View v) -> {
            var component = v.getRootObjects(System.class).iterator().next().getComponents().getFirst();
            var entity = v.getRootObjects(Root.class).iterator().next().getEntities().getFirst();

            return component.getName().equals(entity.getName());
        }));
    }

    private InternalVirtualModel createVirtualModel(Configuration configuration) throws IOException {
        InternalVirtualModel model = new VirtualModelBuilder()
                .withStorageFolder(projectPath)
                .withUserInteractorForResultProvider(new TestUserInteraction.ResultProvider(new TestUserInteraction()))
                .withChangePropagationSpecifications(getChangePropagationSpecifications(configuration))
                .buildAndInitialize();
        model.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC);
        return model;
    }

    enum Configuration {
        NO_VIEW_USAGE,
        VIEW_AS_SOURCE,
        VIEW_AS_TARGET,
        VIEW_AS_SOURCE_AND_TARGET
    }
}
