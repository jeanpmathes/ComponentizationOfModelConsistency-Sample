package tools.vitruv.methodologisttemplate.vsum;

import mir.reactions.model2Model2.Model2Model2ChangePropagationSpecification;
import mir.reactions.model2ModelView2.Model2ModelView2ChangePropagationSpecification;
import mir.reactions.modelView2Model2.ModelView2Model2ChangePropagationSpecification;
import mir.reactions.modelView2ModelView2.ModelView2ModelView2ChangePropagationSpecification;
import neojoin.viewtypes.model2_identity.Model2IdentityViewType;
import neojoin.viewtypes.model_identity.ModelIdentityViewType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.change.propagation.ChangePropagationMode;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.compmodelcons.change.ChangeDeterminationMode;
import tools.vitruv.compmodelcons.change.ViewChangePropagationSpecificationAdapterFactory;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;
import tools.vitruv.methodologisttemplate.model.model.ModelFactory;
import tools.vitruv.methodologisttemplate.model.model.System;
import tools.vitruv.methodologisttemplate.model.model2.Root;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComponentizedConsistencyExampleTest extends AbstractTest {
    @TempDir
    private Path projectPath;

    private static List<ChangePropagationSpecification> getChangePropagationSpecifications(TestVariant variant) {
        return switch (variant.configuration()) {
            case NO_VIEW_USAGE -> ViewChangePropagationSpecificationAdapterFactory.INSTANCE.createRemote(
                    Optional.empty(),
                    new Model2Model2ChangePropagationSpecification(),
                    Optional.empty(),
                    variant.changeDeterminationMode()
            );
            case VIEW_AS_SOURCE -> ViewChangePropagationSpecificationAdapterFactory.INSTANCE.createRemote(
                    Optional.of(new ModelIdentityViewType()),
                    new ModelView2Model2ChangePropagationSpecification(),
                    Optional.empty(),
                    variant.changeDeterminationMode()
            );
            case VIEW_AS_TARGET -> ViewChangePropagationSpecificationAdapterFactory.INSTANCE.createRemote(
                    Optional.empty(),
                    new Model2ModelView2ChangePropagationSpecification(),
                    Optional.of(new Model2IdentityViewType()),
                    variant.changeDeterminationMode()
            );
            case VIEW_AS_SOURCE_AND_TARGET -> ViewChangePropagationSpecificationAdapterFactory.INSTANCE.createRemote(
                    Optional.of(new ModelIdentityViewType()),
                    new ModelView2ModelView2ChangePropagationSpecification(),
                    Optional.of(new Model2IdentityViewType()),
                    variant.changeDeterminationMode()
            );
        };
    }

    private static List<TestVariant> getTestVariants() {
        List<TestVariant> result = new ArrayList<>();
        for (Configuration configuration : Configuration.values()) {
            for (ChangeDeterminationMode changeDeterminationMode : List.of(ChangeDeterminationMode.CHANGE_DERIVATION)) {
                result.add(new TestVariant(configuration, changeDeterminationMode));
            }
        }
        return result;
    }

    @ParameterizedTest
    @MethodSource("getTestVariants")
    void insertComponent(TestVariant variant) throws IOException {
        VirtualModel vsum = createVirtualModel(variant);

        addSystem(vsum, projectPath);
        addComponent(vsum);

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class, Root.class)), (View v) -> {
            var component = v.getRootObjects(System.class).iterator().next().getComponents().getFirst();
            var entity = v.getRootObjects(Root.class).iterator().next().getEntities().getFirst();

            return component.getName().equals(entity.getName());
        }));
    }

    @ParameterizedTest
    @MethodSource("getTestVariants")
    void removeComponent(TestVariant variant) throws IOException {
        VirtualModel vsum = createVirtualModel(variant);

        addSystem(vsum, projectPath);
        addComponent(vsum);

        modifyView(getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait(), (CommittableView v) -> {
            v.getRootObjects(System.class).iterator().next().getComponents().removeFirst();
        });

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class, Root.class)), (View v) ->
                v.getRootObjects(System.class).iterator().next().getComponents().isEmpty()
                        && v.getRootObjects(Root.class).iterator().next().getEntities().isEmpty()));
    }

    @ParameterizedTest
    @MethodSource("getTestVariants")
    void renameComponent(TestVariant variant) throws IOException {
        VirtualModel vsum = createVirtualModel(variant);

        addSystem(vsum, projectPath);
        addComponent(vsum, "OldName");

        modifyView(getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait(), (CommittableView v) -> {
            v.getRootObjects(System.class).iterator().next().getComponents().getFirst().setName("NewName");
        });

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(System.class, Root.class)), (View v) ->
                v.getRootObjects(System.class).iterator().next().getComponents().getFirst().getName().equals("NewName")));
    }

    @ParameterizedTest
    @MethodSource("getTestVariants")
    void testLink(TestVariant variant) throws IOException {
        VirtualModel vsum = createVirtualModel(variant);

        addSystem(vsum, projectPath);

        modifyView(getDefaultView(vsum, List.of(System.class)).withChangeDerivingTrait(), (CommittableView v) -> {
            System system = v.getRootObjects(System.class).iterator().next();

            var component1 = ModelFactory.eINSTANCE.createComponent();
            component1.setName("component1");
            var component2 = ModelFactory.eINSTANCE.createComponent();
            component2.setName("component2");
            system.getComponents().addAll(List.of(component1, component2));

            var protocol = ModelFactory.eINSTANCE.createProtocol();
            protocol.setName("exampleProtocol");
            system.getProtocols().add(protocol);

            var link = ModelFactory.eINSTANCE.createLink();
            system.getLinks().add(link);
            link.setProtocol(protocol);
            link.getComponents().addAll(List.of(component1, component2));
        });

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(Root.class)), (View v) -> {
            var root = v.getRootObjects(Root.class).iterator().next();
            return root.getLinks().size() == 1
                    && root.getLinks().getFirst().getEntities().size() == 2
                    && root.getLinks().getFirst().getEntities().stream()
                    .allMatch(c -> c.getName().startsWith("component"));
        }));
    }

    enum Configuration {
        NO_VIEW_USAGE,
        VIEW_AS_SOURCE,
        VIEW_AS_TARGET,
        VIEW_AS_SOURCE_AND_TARGET
    }

    private InternalVirtualModel createVirtualModel(TestVariant variant) throws IOException {
        InternalVirtualModel model = new VirtualModelBuilder()
                .withStorageFolder(projectPath)
                .withUserInteractorForResultProvider(new TestUserInteraction.ResultProvider(new TestUserInteraction()))
                .withChangePropagationSpecifications(getChangePropagationSpecifications(variant))
                .buildAndInitialize();
        model.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC);
        return model;
    }

    private record TestVariant(Configuration configuration, ChangeDeterminationMode changeDeterminationMode) {

    }
}
