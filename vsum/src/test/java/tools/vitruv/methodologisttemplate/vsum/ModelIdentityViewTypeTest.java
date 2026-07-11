package tools.vitruv.methodologisttemplate.vsum;

import neojoin.viewtypes.model_identity.ModelIdentityFactory;
import neojoin.viewtypes.model_identity.ModelIdentityViewType;
import neojoin.viewtypes.model_identity.System;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.methodologisttemplate.model.model.ModelPackage;
import tools.vitruv.methodologisttemplate.model.model2.Root;

import java.nio.file.Path;
import java.util.List;

public class ModelIdentityViewTypeTest extends AbstractTest {
    @Test
    void insertComponent(@TempDir Path tempDir) {
        VirtualModel vsum = createVirtualModel(tempDir);

        addSystem(vsum, tempDir);
        addComponent(vsum, "Component");

        Assertions.assertTrue(assertView(getView(vsum), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().size() == 1
                    && system.getComponents().get(0).getName().equals("Component");
        }));
    }

    @Test
    void insertRouter(@TempDir Path tempDir) {
        VirtualModel vsum = createVirtualModel(tempDir);

        addSystem(vsum, tempDir);
        addRouter(vsum, "Router");

        Assertions.assertTrue(assertView(getView(vsum), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().size() == 1
                    && system.getComponents().get(0).getName().equals("Router");
        }));
    }

    @Test
    void renameComponent(@TempDir Path tempDir) {
        VirtualModel vsum = createVirtualModel(tempDir);

        addSystem(vsum, tempDir);
        addComponent(vsum, "OldName");

        modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            system.getComponents().get(0).setName("NewName");
        });

        Assertions.assertTrue(assertView(getView(vsum), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().size() == 1
                    && system.getComponents().get(0).getName().equals("NewName");
        }));

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(Root.class)), (View v) -> {
            var root = v.getRootObjects(Root.class).iterator().next();
            return root.getEntities().get(0).getName().equals("NewName");
        }));
    }

    @Test
    void deleteComponent(@TempDir Path tempDir) {
        VirtualModel vsum = createVirtualModel(tempDir);

        addSystem(vsum, tempDir);
        addComponent(vsum);

        modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            system.getComponents().remove(0);
        });

        Assertions.assertTrue(assertView(getView(vsum), (View v) -> {
            var system = v.getRootObjects(System.class).iterator().next();
            return system.getComponents().isEmpty();
        }));

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(Root.class)), (View v) -> {
            var root = v.getRootObjects(Root.class).iterator().next();
            return root.getEntities().isEmpty();
        }));
    }

    @Test
    void testLink(@TempDir Path tempDir) {
        VirtualModel vsum = createVirtualModel(tempDir);

        addSystem(vsum, tempDir);

        modifyView(getView(vsum).withChangeDerivingTrait(), (CommittableView v) -> {
            var system = v.getRootObjects(System.class).iterator().next();

            var component1 = ModelIdentityFactory.eINSTANCE.createComponent();
            component1.setName("Component1");
            var component2 = ModelIdentityFactory.eINSTANCE.createComponent();
            component2.setName("Component2");
            system.getComponents().addAll(List.of(component1, component2));

            var protocol = ModelIdentityFactory.eINSTANCE.createProtocol();
            protocol.setName("ExampleProtocol");
            system.getProtocols().add(protocol);

            var link = ModelIdentityFactory.eINSTANCE.createLink();
            system.getLinks().add(link);
            link.setProtocol(protocol);
            link.getComponents().addAll(List.of(component1, component2));
        });

        Assertions.assertTrue(assertView(getDefaultView(vsum, List.of(Root.class)), (View v) -> {
            var root = v.getRootObjects(Root.class).iterator().next();
            return root.getLinks().size() == 1
                    && root.getLinks().get(0).getEntities().size() == 2
                    && root.getLinks().get(0).getEntities().stream()
                    .allMatch(c -> c.getName().startsWith("Component"));
        }));
    }

    private VirtualModel createVirtualModel(Path tempDir) {
        // The view type loads the metamodels using their URI, so they are not automatically added to the registry.
        ModelPackage.eINSTANCE.eClass();

        return createDefaultVirtualModel(tempDir, List.of(new ModelIdentityViewType()));
    }

    private View getView(VirtualModel vsum) {
        return vsum.createSelector(vsum.getViewTypes().stream().filter(viewType -> viewType.getName().equals(ModelIdentityViewType.NAME)).findAny().orElseThrow()).createView();
    }
}
